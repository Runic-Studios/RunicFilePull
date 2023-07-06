package com.runicrealms.plugin;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class FileUtils {

    /**
     * Takes a base64 string, decodes its contents, and writes them to a file.
     */
    public static void writeBase64ToFile(String base64, File file) throws Exception {
        StringBuilder base64Builder = new StringBuilder(base64.replaceAll("\\n", ""));
        while (base64Builder.length() % 4 > 0) { // Base64 uses equals signs as padding characters. The number of characters must be divisble by four. If it isn't then we manually correct it.
            base64Builder.append('=');
        }
        base64 = base64Builder.toString();
        PrintWriter writer = new PrintWriter(file);
        writer.print(Base64Coder.decodeString(base64));
        writer.close();
    }

    /**
     * Sends a GET request to the given URL with a simple bearer auth token
     */
    public static String getWithAuth(String url, String authToken) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "token " + authToken);
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append("\n");
        }
        return output.toString();
    }

    /**
     * Delete a directory recursively
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (directory == null || !directory.exists() || !directory.isDirectory()) return;
        Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * Deletes a directory's contents then replaces them with a zip file's contents
     */
    public static void clearAndUnzipDirectory(File zipFile, File destinationDirectory) throws IOException {
        // Clear the destination directory
        Path dirToClear = destinationDirectory.toPath();
        Files.walkFileTree(dirToClear, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });

        // Now unzip the file
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path path = Paths.get(destinationDirectory.getPath(), zipEntry.getName());
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(path);
                } else {
                    Files.createDirectories(path.getParent());
                    Files.copy(zis, path, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Zips the contents of a directory to a zipfile, then moves that zipfile to a destination directory
     */
    public static void zipDirectoryAndMove(File sourceDirectory, File zipFile, File destinationDirectory) throws IOException {
        Path zipPath = zipFile.toPath();
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zipPath))) {
            Path sourceDirPath = sourceDirectory.toPath();
            try (Stream<Path> walkStream = Files.walk(sourceDirPath)) {
                walkStream.filter(path -> !Files.isDirectory(path))
                        .forEach(path -> {
                            ZipEntry zipEntry = new ZipEntry(sourceDirPath.relativize(path).toString());
                            try {
                                zos.putNextEntry(zipEntry);
                                Files.copy(path, zos);
                                zos.closeEntry();
                            } catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        });
            }
        }

        // Now move the zip file to the destination
        Path destination = destinationDirectory.toPath().resolve(zipPath.getFileName());
        Files.move(zipPath, destination, StandardCopyOption.REPLACE_EXISTING);
    }


}
