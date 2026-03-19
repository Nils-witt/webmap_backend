package dev.nilswitt.webmap.base.ui.views;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.dom.Style;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadEvent;
import com.vaadin.flow.server.streams.InMemoryUploadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.spring.security.AuthenticationContext;
import dev.nilswitt.webmap.base.ui.ViewToolbar;
import dev.nilswitt.webmap.base.ui.views.components.MapItemEditDialog;
import dev.nilswitt.webmap.base.ui.views.components.MapItemPermissionsDialog;
import dev.nilswitt.webmap.base.ui.views.filters.MapItemFilter;
import dev.nilswitt.webmap.entities.*;
import dev.nilswitt.webmap.entities.repositories.*;
import dev.nilswitt.webmap.security.PermissionVerifier;
import jakarta.annotation.security.RolesAllowed;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

@Log4j2
@Route("ui/map/items")
@Menu(order = 1, icon = "vaadin:map-marker", title = "Map Items")
@RolesAllowed("MAPITEM_VIEW")
public class MapItemView extends VerticalLayout {
    private final Grid<MapItem> mapItemGrid = new Grid<>();
    private final Button createBtn = new Button("Create");
    private final MapItemEditDialog editDialog;
    private final MapItemPermissionsDialog permissionsDialog;

    private final MapItemRepository mapItemRepository;
    private final MapItemFilter mapItemFilter;
    private final MapGroupRepository mapGroupRepository;
    private final AuthenticationContext authenticationContext;
    private final PermissionVerifier permissionVerifier;

    public MapItemView(MapItemRepository mapItemRepository, AuthenticationContext authenticationContext, SecurityGroupRepository securityGroupRepository,
                       SecurityGroupPermissionsRepository securityGroupPermissionsRepository,
                       UserPermissionsRepository userPermissionsRepository,
                       UserRepository userRepository, PermissionVerifier permissionVerifier, MapGroupRepository mapGroupRepository) {
        this.permissionVerifier = permissionVerifier;
        this.mapItemRepository = mapItemRepository;
        this.authenticationContext = authenticationContext;
        this.mapGroupRepository = mapGroupRepository;
        this.editDialog = new MapItemEditDialog(mapItem -> {
            this.mapItemRepository.save(mapItem);
            this.mapItemGrid.getDataProvider().refreshAll();
        }, mapGroupRepository);
        this.permissionsDialog = new MapItemPermissionsDialog(userPermissionsRepository, userRepository, securityGroupRepository, securityGroupPermissionsRepository);
        this.configureCreateButton();
        this.configureGrid();
        this.mapItemFilter = new MapItemFilter(securityGroupExample -> this.mapItemGrid.getDataProvider().refreshAll(), mapGroupRepository);
        this.mapItemFilter.setUp(this.mapItemGrid);

        this.setSizeFull();
        this.setPadding(false);
        this.setSpacing(false);
        this.getStyle().setOverflow(Style.Overflow.HIDDEN);


        this.add(new ViewToolbar("Map Item List", ViewToolbar.group(createImportButton(), configureExportButton(), this.createBtn)));
        this.add(this.mapItemGrid, this.editDialog);
    }

    private void configureCreateButton() {
        this.createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        this.createBtn.addClickListener(event -> {
            User user = currentUser();
            if (!PermissionVerifier.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPITEM,
                    SecurityGroup.UserRoleScopeEnum.CREATE)) {
                Notification.show("You cannot create map items");
                return;
            }
            this.editDialog.open(null);
        });
    }

    private Anchor configureExportButton() {
        Button exportButton = new Button("Export");
        Anchor downloadLink = new Anchor((DownloadEvent event) -> {
            event.setFileName("items.csv");
            var anchor = event.getOwningComponent();
            event.getResponse().setHeader("Cache-Control", "public, max-age=3600");
            try (OutputStream outputStream = event.getOutputStream()) {
                StringWriter sw = new StringWriter();
                String[] HEADERS = {"id", "name", "mapGroup", "latitude", "longitude", "altitude"};

                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setHeader(HEADERS).get();

                try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat)) {
                    this.list(Pageable.unpaged()).forEach(mapItem -> {
                        try {
                            printer.printRecord(
                                    mapItem.getId(),
                                    mapItem.getName(),
                                    mapItem.getMapGroup() != null ? mapItem.getMapGroup().getName() : "None",
                                    mapItem.getPosition().getLatitude(),
                                    mapItem.getPosition().getLongitude(),
                                    mapItem.getPosition().getAltitude()
                            );
                        } catch (IOException e) {
                            log.error("Error while exporting map items", e);
                        }
                    });
                } catch (IOException e) {
                    log.error("Error while exporting map items", e);
                }
                outputStream.write(sw.toString().getBytes());
            }
            event.getUI().access(() -> { /* UI updates */});
        }, "");
        downloadLink.add(exportButton);
        return downloadLink;
    }

    private Button createImportButton() {
        Button importButton = new Button("Import");
        importButton.addClickListener(event -> {
            Dialog importDialog = new Dialog();
            importDialog.setHeaderTitle("Import Map Items");
            ComboBox<String> mapGroupComboBox = new ComboBox<>("Map Group");
            ComboBox<String> nameComboBox = new ComboBox<>("Name");
            ComboBox<String> latitudeComboBox = new ComboBox<>("Latitude");
            ComboBox<String> longitudeComboBox = new ComboBox<>("Longitude");


            AtomicReference<String> strData = new AtomicReference<>("");
            InMemoryUploadHandler inMemoryHandler = UploadHandler
                    .inMemory((metadata, data) -> {
                        strData.set(new String(data));
                        CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                                .setDelimiter(";").get();
                        Iterable<CSVRecord> records = csvFormat.parse(new StringReader(new String(data)));
                        CSVRecord header = records.iterator().next();
                        UI.getCurrent().access(() -> {
                            HashSet<String> columnNames = new HashSet<>();

                            for (int i = 0; i < header.size(); i++) {
                                String columnName = header.get(i);
                                columnNames.add(columnName);
                            }
                            mapGroupComboBox.setItems(columnNames);
                            nameComboBox.setItems(columnNames);
                            latitudeComboBox.setItems(columnNames);
                            longitudeComboBox.setItems(columnNames);
                        });
                    });
            Upload upload = new Upload(inMemoryHandler);
            upload.setMaxFiles(1);

            importDialog.add(upload);
            FormLayout formLayout = new FormLayout();
            formLayout.add(mapGroupComboBox);
            formLayout.add(nameComboBox);
            formLayout.add(latitudeComboBox);
            formLayout.add(longitudeComboBox);

            importDialog.add(formLayout);


            Button startimportButton = new Button("Import", e -> {
                CSVFormat csvFormat = CSVFormat.DEFAULT.builder()
                        .setDelimiter(";").get();
                try {
                    Iterable<CSVRecord> records = csvFormat.parse(new StringReader(strData.get()));
                    Iterator<CSVRecord> iterator = records.iterator();
                    CSVRecord header = iterator.next();

                    if (nameComboBox.isEmpty()) {
                        log.warn("No name column selected, skipping import");
                        return;
                    }
                    int nameRow = -1;
                    int mapGroupRow = -1;
                    int latitudeRow = -1;
                    int longitudeRow = -1;

                    for (int i = 0; i < header.size(); i++) {
                        String columnName = header.get(i);
                        if (columnName.equals(nameComboBox.getValue())) {
                            nameRow = i;
                        }
                        if (columnName.equals(mapGroupComboBox.getValue())) {
                            mapGroupRow = i;
                        }
                        if (columnName.equals(latitudeComboBox.getValue())) {
                            latitudeRow = i;
                        }
                        if (columnName.equals(longitudeComboBox.getValue())) {
                            longitudeRow = i;
                        }
                    }
                    int finalNameRow = nameRow;
                    int finalLongitudeRow = longitudeRow;
                    int finalLatitudeRow = latitudeRow;
                    int finalMapGroupRow = mapGroupRow;
                    iterator.forEachRemaining(record -> {
                        try {
                            MapItem mapItem = new MapItem();
                            mapItem.setName(record.get(finalNameRow));
                            if (record.get(finalMapGroupRow) != null) {
                                String groupName = record.get(finalMapGroupRow);
                                if (groupName != null && !groupName.isEmpty()) {
                                    MapGroup mapGroup = mapGroupRepository.findByName(groupName).orElse(null);

                                    if (mapGroup == null) {
                                        mapGroup = new MapGroup();
                                        mapGroup.setName(groupName);
                                        mapGroup = mapGroupRepository.save(mapGroup);
                                    }

                                    mapItem.setMapGroup(mapGroup);
                                }
                            }
                            mapItem.setPosition(new EmbeddedPosition());
                            mapItem.getPosition().setLatitude(Double.valueOf(record.get(finalLatitudeRow)));
                            mapItem.getPosition().setLongitude(Double.valueOf(record.get(finalLongitudeRow)));
                            mapItemRepository.save(mapItem);
                            log.info(mapItem.toString());
                        } catch (Exception ex) {
                            UI.getCurrent().access(() -> Notification.show("Import of row " + record.getRecordNumber() + " failed: " + ex.getMessage()));
                        }
                    });

                    mapItemGrid.getDataProvider().refreshAll();
                    Notification.show("Import successful");
                } catch (IOException ex) {
                    Notification.show("Import failed: " + ex.getMessage());
                }
            });
            importDialog.add(startimportButton);

            Button closeButton = new Button("Close", e -> importDialog.close());
            importDialog.add(closeButton);
            importDialog.setWidth("400px");
            importDialog.open();
        });
        return importButton;
    }

    private void configureGrid() {
        this.mapItemGrid.setItemsPageable(this::list);
        this.mapItemGrid.addColumn(MapItem::getName).setKey(String.valueOf(MapItemFilter.Columns.NAME)).setHeader("Name");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getMapGroup() != null ? mapItem.getMapGroup().getName() : "None").setKey("mapGroup").setHeader("Map Group");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLatitude()).setHeader("Latitude");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getLongitude()).setHeader("Longitude");
        this.mapItemGrid.addColumn(mapItem -> mapItem.getPosition().getAltitude()).setHeader("Altitude");

        this.mapItemGrid.setEmptyStateText("There are no map items");
        this.mapItemGrid.setSizeFull();
        this.mapItemGrid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        new MapItemContextMenu(this.mapItemGrid);

    }

    private User currentUser() {
        return this.authenticationContext.getAuthenticatedUser(User.class).orElse(null);
    }

    private void openDeleteDialog(MapItem mapItem) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete Map Item");
        confirmDialog.setText("Are you sure you want to delete map item '" + mapItem.getName() + "'?");
        confirmDialog.setCancelable(true);
        confirmDialog.setConfirmText("Delete");
        confirmDialog.addConfirmListener(e -> {
            this.mapItemRepository.delete(mapItem);
            this.mapItemGrid.getDataProvider().refreshAll();
            confirmDialog.close();
            this.remove(confirmDialog);
        });
        this.add(confirmDialog);
        confirmDialog.open();
    }

    public List<MapItem> list(Pageable pageable) {
        return this.mapItemRepository.findAll(this.mapItemFilter.getExample(), pageable).stream().toList();
    }

    private class MapItemContextMenu extends GridContextMenu<MapItem> {
        public MapItemContextMenu(Grid<MapItem> target) {
            super(target);
            this.addItem("Permissions", event -> event.getItem().ifPresent(mapOverlay -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.ADMIN, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
                    Notification.show("You cannot edit overlay permissions");
                    return;
                }
                permissionsDialog.open(mapOverlay);
            }));
            this.addItem("Edit", event -> event.getItem().ifPresent(mapItem -> {
                User user = currentUser();
                if (!PermissionVerifier.hasAnyScope(user, SecurityGroup.UserRoleTypeEnum.MAPITEM, SecurityGroup.UserRoleScopeEnum.EDIT, SecurityGroup.UserRoleScopeEnum.CREATE)) {
                    Notification.show("You cannot edit map items");
                    return;
                }
                editDialog.open(mapItem);
            }));
            this.addItem("Delete", event -> event.getItem().ifPresent(mapItem -> {
                User user = currentUser();
                if (!permissionVerifier.hasAccess(user, SecurityGroup.UserRoleScopeEnum.DELETE, SecurityGroup.UserRoleTypeEnum.MAPITEM)) {
                    Notification.show("You cannot delete map items");
                    return;
                }
                MapItemView.this.openDeleteDialog(mapItem);
            }));
            setDynamicContentHandler(Objects::nonNull);
        }
    }
}
