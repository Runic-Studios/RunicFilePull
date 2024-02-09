package com.runicrealms.plugin.filepull;

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
     * Writes a string (probably decoded with decodeBase64) to a file.
     */
    public static void writeToFile(String contents, File file) throws Exception {
        PrintWriter writer = new PrintWriter(file);
        writer.print(contents);
        writer.close();
    }

    /**
     * Takes a base64 string and decodes it.
     */
    public static String decodeBase64(String base64) throws Exception {
        StringBuilder base64Builder = new StringBuilder(base64.replaceAll("\\n", ""));
        while (base64Builder.length() % 4 > 0) { // Base64 uses equals signs as padding characters. The number of characters must be divisble by four. If it isn't then we manually correct it.
            base64Builder.append('=');
        }
        return Base64Coder.decodeString(base64Builder.toString());
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
