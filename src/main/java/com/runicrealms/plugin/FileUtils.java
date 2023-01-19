package com.runicrealms.plugin;

import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

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

}
