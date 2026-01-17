package dev.nilswitt.webmap.views.components;

import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadEvent;
import com.vaadin.flow.server.streams.UploadHandler;
import dev.nilswitt.webmap.entities.MapOverlay;
import dev.nilswitt.webmap.entities.repositories.MapOverlayRepository;
import dev.nilswitt.webmap.records.OverlayConfig;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UploadOverlayDialog extends Dialog {

    Logger logger = LoggerFactory.getLogger(UploadOverlayDialog.class);
    private final OverlayConfig overlayConfig;
    private final MapOverlay mapOverlay;
    private final MapOverlayRepository mapOverlayRepository;

    public UploadOverlayDialog(MapOverlayRepository mapOverlayRepository, MapOverlay mapOverlay, OverlayConfig overlayConfig) {
        this.overlayConfig = overlayConfig;
        this.mapOverlay = mapOverlay;
        this.mapOverlayRepository = mapOverlayRepository;
        this.add(getFormLayout());

        this.setHeaderTitle("Upload overlay");
    }

    public FormLayout getFormLayout() {
        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        UploadOverlayHandler uploadOverlayHandler = new UploadOverlayHandler();
        Upload upload = new Upload(uploadOverlayHandler);
        upload.setMaxFiles(1);
        upload.setAcceptedFileTypes(".zip");

        formLayout.add(upload);
        return formLayout;
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());

        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


    public class UploadOverlayHandler implements UploadHandler {

        @Override
        public void handleUploadRequest(UploadEvent event) throws IOException {
            String fileName = event.getFileName();

            try (InputStream inputStream = event.getInputStream()) {
                File targetFile = new File("uploads/" + fileName);
                targetFile.getParentFile().mkdirs();
                try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                    inputStream.transferTo(outputStream);
                }
                int newLayerVersion = mapOverlay.getLayerVersion() + 1;

                File destDir = Path.of(overlayConfig.basePath(), mapOverlay.getId().toString(), String.valueOf(newLayerVersion)).toFile();
                if (!destDir.exists()) {
                    destDir.mkdirs();
                } else {
                    FileUtils.deleteDirectory(destDir);
                    destDir.mkdirs();
                }

                byte[] buffer = new byte[1024];
                ZipInputStream zis = new ZipInputStream(new FileInputStream("uploads/" + fileName));
                ZipEntry zipEntry = zis.getNextEntry();
                while (zipEntry != null) {
                    File newFile = newFile(destDir, zipEntry);
                    if (zipEntry.getName().startsWith(".") || zipEntry.getName().startsWith("_")) {
                        // IGnore hidden files
                    } else if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        // write file content
                        FileOutputStream fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        fos.close();
                    }
                    zipEntry = zis.getNextEntry();
                }

                zis.closeEntry();
                zis.close();
                mapOverlay.setLayerVersion(newLayerVersion);
                mapOverlayRepository.save(mapOverlay);
            } catch (IOException e) {
                Notification.show("Error uploading file");
            }
        }
    }
}
