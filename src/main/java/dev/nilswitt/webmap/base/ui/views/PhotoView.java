package dev.nilswitt.webmap.base.ui.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.entities.Photo;
import dev.nilswitt.webmap.entities.SecurityGroup;
import dev.nilswitt.webmap.entities.User;
import dev.nilswitt.webmap.entities.repositories.PhotoRepository;
import dev.nilswitt.webmap.records.PictureConfig;
import dev.nilswitt.webmap.security.PermissionVerifier;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@Log4j2
@Route("ui/photos")
@Menu(order = 4, icon = "vaadin:camera", title = "Photos")
@RolesAllowed("PHOTO_VIEW")
public class PhotoView extends VerticalLayout {

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    private final Grid<Photo> photoGrid = new Grid<>();
    private final Button uploadBtn = new Button("Upload");

    private final PhotoRepository photoRepository;
    private final PictureConfig pictureConfig;
    private final AuthenticationContext authenticationContext;
    private final PermissionVerifier permissionVerifier;

    public PhotoView(PhotoRepository photoRepository,
                     PictureConfig pictureConfig,
                     AuthenticationContext authenticationContext,
                     PermissionVerifier permissionVerifier) {
        this.photoRepository = photoRepository;
        this.pictureConfig = pictureConfig;
        this.authenticationContext = authenticationContext;
        this.permissionVerifier = permissionVerifier;

        configureUploadButton();
        configureGrid();

        setSizeFull();
        setPadding(false);
        setSpacing(false);
        getStyle().setOverflow(Style.Overflow.HIDDEN);

        add(new ViewToolbar("Photo List", ViewToolbar.group(configureExportButton(), uploadBtn)));
        add(photoGrid);
    }

    private void configureUploadButton() {
        uploadBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        uploadBtn.addClickListener(event -> {
            User user = currentUser();
            if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.CREATE, SecurityGroup.UserRoleTypeEnum.PHOTO)) {
                Notification.show("You do not have permission to upload photos.");
                return;
            }
            openUploadDialog();
        });
    }

    private void openUploadDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Upload Photo");

        InMemoryUploadHandler uploadHandler = UploadHandler.inMemory((metadata, data) -> {
            User user = currentUser();
            Photo newPhoto = new Photo();
            newPhoto.setAuthor(user);
            newPhoto = photoRepository.save(newPhoto);

            String originalFilename = metadata.fileName();
            String fileExtension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";

            try {
                LocalDateTime now = LocalDateTime.now();
                String dailyPath = pictureConfig.localPath() + "/" + now.getYear() + "/"
                        + now.getMonthValue() + "/" + now.getDayOfMonth();
                Files.createDirectories(Path.of(dailyPath));

                String filePath = dailyPath + "/" + newPhoto.getId() + fileExtension;
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    fos.write(data);
                }

                newPhoto.setPath(now.getYear() + "/" + now.getMonthValue() + "/"
                        + now.getDayOfMonth() + "/" + newPhoto.getId() + fileExtension);
                newPhoto.setName("Photo " + newPhoto.getId());
                photoRepository.save(newPhoto);
            } catch (IOException e) {
                log.error("Error saving photo file: {}", e.getMessage(), e);
            }

            Photo finalPhoto = newPhoto;
            getUI().ifPresent(ui -> ui.access(() -> {
                photoGrid.getDataProvider().refreshAll();
                Notification.show("Photo '" + finalPhoto.getName() + "' uploaded successfully.");
                dialog.close();
            }));
        });

        Upload upload = new Upload(uploadHandler);
        upload.setAcceptedFileTypes("image/*");
        upload.setMaxFiles(1);

        Button closeButton = new Button("Close", e -> dialog.close());
        dialog.add(upload, closeButton);
        dialog.setWidth("400px");
        dialog.open();
    }

    private Anchor configureExportButton() {
        Button exportButton = new Button("Export CSV");
        Anchor downloadLink = new Anchor((DownloadEvent event) -> {
            event.setFileName("photos.csv");
            try (OutputStream outputStream = event.getOutputStream()) {
                StringBuilder sb = new StringBuilder();
                sb.append("ID,Name,Path,Latitude,Longitude,Altitude,ExpiresAt\n");
                list(Pageable.unpaged()).forEach(photo -> {
                    sb.append(photo.getId()).append(",");
                    sb.append(escapeCsv(photo.getName())).append(",");
                    sb.append(escapeCsv(photo.getPath())).append(",");
                    if (photo.getPosition() != null) {
                        sb.append(photo.getPosition().getLatitude()).append(",");
                        sb.append(photo.getPosition().getLongitude()).append(",");
                        sb.append(photo.getPosition().getAltitude());
                    } else {
                        sb.append(",,,");
                    }
                    sb.append(",");
                    sb.append("\n");
                });
                outputStream.write(sb.toString().getBytes());
            } catch (IOException e) {
                log.error("Error exporting photos CSV: {}", e.getMessage(), e);
            }
        }, "");
        downloadLink.add(exportButton);
        return downloadLink;
    }

    private void configureGrid() {
        photoGrid.setItemsPageable(this::list);
        photoGrid.addColumn(Photo::getName).setHeader("Name").setSortable(true);
        photoGrid.addColumn(Photo::getPath).setHeader("Path");
        photoGrid.addColumn(photo -> photo.getPosition() != null
                ? photo.getPosition().getLatitude() : "").setHeader("Latitude");
        photoGrid.addColumn(photo -> photo.getPosition() != null
                ? photo.getPosition().getLongitude() : "").setHeader("Longitude");
        photoGrid.addColumn(photo -> photo.getPosition() != null
                ? photo.getPosition().getAltitude() : "").setHeader("Altitude");

        photoGrid.setEmptyStateText("There are no photos");
        photoGrid.setSizeFull();
        photoGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        new PhotoContextMenu(photoGrid);
    }

    private List<Photo> list(Pageable pageable) {
        return photoRepository.findAll(pageable).stream().toList();
    }

    private User currentUser() {
        return authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private void openDeleteDialog(Photo photo) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Photo");
        confirmDialog.setText("Are you sure you want to delete photo '" + photo.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            if (photo.getPath() != null) {
                try {
                    Files.deleteIfExists(Paths.get(pictureConfig.localPath() + "/" + photo.getPath()));
                } catch (IOException ex) {
                    log.error("Error deleting photo file: {}", ex.getMessage(), ex);
                }
            }
            photoRepository.delete(photo);
            photoGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            remove(confirmDialog);
        });
        add(confirmDialog);
        confirmDialog.open();
    }

    private void openPreviewDialog(Photo photo) {
        Dialog previewDialog = new Dialog();
        previewDialog.setHeaderTitle(photo.getName());

        try {
            Path imagePath = Paths.get(pictureConfig.localPath() + "/" + photo.getPath());
            if (Files.exists(imagePath)) {
                Image image = new Image(DownloadHandler.forFile(new File(imagePath.toUri())), photo.getName());
                image.setMaxWidth("100%");
                previewDialog.add(image);
            } else {
                previewDialog.add("Image file not found on disk.");
            }
        } catch (Exception e) {
            previewDialog.add("Could not load image: " + e.getMessage());
        }

        Button closeButton = new Button("Close", ev -> previewDialog.close());
        previewDialog.add(closeButton);
        previewDialog.setWidth("600px");
        previewDialog.open();
    }

    private class PhotoContextMenu extends GridContextMenu<Photo> {
        public PhotoContextMenu(Grid<Photo> target) {
            super(target);
            addItem("Preview", event -> event.getItem().ifPresent(PhotoView.this::openPreviewDialog));
            addItem("Delete", event -> event.getItem().ifPresent(photo -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.PHOTO)) {
                    Notification.show("You do not have permission to delete photos.");
                    return;
                }
                openDeleteDialog(photo);
            }));
            setDynamicContentHandler(Objects::nonNull);
        }
    }
}

