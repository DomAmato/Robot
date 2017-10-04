package com.dyn.robot.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import java.util.regex.Pattern;

import com.dyn.robot.RobotMod;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class FileUtils {

	private static Pattern pattern = Pattern.compile("[\\:*?\"<>|]+");

	public static void copyFile(File from, File to) throws Exception {
		FileUtils.createFileSafely(to);
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(from));
		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(to));
		byte[] block;
		while (bis.available() > 0) {
			block = new byte[8192];
			final int readNow = bis.read(block);
			bos.write(block, 0, readNow);
		}
		bos.flush();
		bos.close();
		bis.close();
	}

	public static void createFileSafely(File file) throws IOException {
		File parentFile = new File(file.getParent());
		if (!parentFile.exists()) {
			if (!parentFile.mkdirs()) {
				throw new IOException("Unable to create parent file: " + file.getParent());
			}
		}
		if (file.exists()) {
			if (!file.delete()) {
				if (!file.renameTo(new File(file.getParentFile(), "old-" + file.getName()))) {
					throw new IOException(
							"Couldn't delete '".concat(file.getName()).concat("'").concat(" Try Signing in again"));
				}
			}
		}
		if (!file.createNewFile()) {
			throw new IOException("Couldn't create '".concat(file.getAbsolutePath()).concat("'"));
		}
	}

	public static void createJsonFile(JsonObject json, File dest) {
		try {
			Gson gson = new Gson();
			FileUtils.createFileSafely(dest);
			FileOutputStream fOut = new FileOutputStream(dest);
			OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
			myOutWriter.append(gson.toJson(json));
			myOutWriter.close();
			fOut.close();
		} catch (IOException e) {
			RobotMod.logger.error("Failed Writing JSON file", e);
		}
	}

	public static String readFile(File file) {
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} catch (FileNotFoundException e) {
			RobotMod.logger.error("Could not find File", e);
		} catch (IOException e) {
			RobotMod.logger.error("Failed during read IO process", e);
		}
		return null;
	}

	public static byte[] readFully(InputStream stream) throws IOException {
		byte[] data = new byte[4096];
		ByteArrayOutputStream entryBuffer = new ByteArrayOutputStream();
		int len;
		do {
			len = stream.read(data);
			if (len > 0) {
				entryBuffer.write(data, 0, len);
			}
		} while (len != -1);

		return entryBuffer.toByteArray();
	}

	/**
	 * replace illegal characters in a filename with "_" illegal characters : : \ /
	 * * ? | < >
	 *
	 * @param name
	 * @return
	 */
	public static String sanitizeFilename(String name) {
		String val = FileUtils.pattern.matcher(name).replaceAll("_");

		return val;
	}

	public static void writeFile(File dest, List<String> strs) throws IOException {
		FileUtils.createFileSafely(dest);
		FileWriter fw = new FileWriter(dest);
		for (String str : strs) {
			fw.write(str + System.lineSeparator());
		}
		fw.flush();
		fw.close();
	}

	public static void writeFile(File dest, String str) throws IOException {
		FileUtils.createFileSafely(dest);
		FileWriter fw = new FileWriter(dest);
		fw.write(str);
		fw.flush();
		fw.close();
	}
}
