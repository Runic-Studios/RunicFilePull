package com.runicfilepull;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.IOUtils;

public class URLUtil {

	public static String encodeStringForURL(String value) {
		try {
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException ex) {
			throw new RuntimeException(ex.getCause());
		}
	}

	@SuppressWarnings("deprecation")
	public static String getDataFromURL(String urlString) {
		try {
			URL url = new URL(urlString);
			String data = IOUtils.toString(url.openStream());
			return data;
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return null;
	}
	
	public static String formatGitlabFilePath(String path) {
		String formattedPath;
		formattedPath = path.startsWith("/") ? path.substring(1) : path;
		formattedPath = path.endsWith("/") ? path.substring(0, path.length() - 2) : path;
		StringBuilder builder = new StringBuilder();
		builder.append("https://gitlab.com/api/v4/projects/");
		builder.append(Plugin.getInstance().getConfig().getString("project-id"));
		builder.append("/repository/files/");
		builder.append(encodeStringForURL(formattedPath));
		builder.append("/raw?ref=");
		builder.append(Plugin.getInstance().getConfig().getString("branch"));
		builder.append("&private_token=");
		builder.append(Plugin.getInstance().getConfig().getString("access-token"));
		return builder.toString();
	}
	
	public static String formatGitlabFolderPath(String path) {
		String formattedPath;
		formattedPath = path.startsWith("/") ? path.substring(1) : path;
		formattedPath = path.endsWith("/") ? path.substring(0, path.length() - 2) : path;
		StringBuilder builder = new StringBuilder();
		builder.append("https://gitlab.com/api/v4/projects/");
		builder.append(Plugin.getInstance().getConfig().getString("project-id"));
		builder.append("/repository/tree?path=");
		builder.append(encodeStringForURL(formattedPath));
		builder.append("&ref=");
		builder.append(Plugin.getInstance().getConfig().getString("branch"));
		builder.append("&private_token=");
		builder.append(Plugin.getInstance().getConfig().getString("access-token"));
		return builder.toString();
	}

}
