package com.dyn.robot.utils;

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
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.List;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

	// from
	// https://www.journaldev.com/966/java-gzip-example-compress-decompress-file
	public static void decompressGzipFile(String gzipFile, String newFile) {
		try {
			FileInputStream fis = new FileInputStream(gzipFile);
			GZIPInputStream gis = new GZIPInputStream(fis);
			FileOutputStream fos = new FileOutputStream(newFile);
			byte[] buffer = new byte[1024];
			int len;
			while ((len = gis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
			}
			// close resources
			fos.close();
			gis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void downloadFile(String url, File dest) {
		try {
			String md5 = null;
			if (dest.exists()) {
				md5 = FileUtils.getMD5(dest);
			}

			URL u = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) u.openConnection();
			if (md5 != null) {
				connection.setRequestProperty("If-None-Match", md5);
			}
			connection.connect();

			// local copy is up-to-date
			if (connection.getResponseCode() == 304) {
				return;
			}
			FileUtils.createFileSafely(dest);

			BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
			BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest));

			final int len = connection.getContentLength();

			int readBytes = 0;
			byte[] block;

			while (readBytes < len) {
				block = new byte[8192];
				int readNow = in.read(block);
				if (readNow > 0) {
					out.write(block, 0, readNow);
				}
				readBytes += readNow;
			}
			out.flush();
			out.close();
			in.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		}
	}

	// this method is copied from original launcher, as the MD5-ing function
	// needs to be the same
	public static String getMD5(File file) throws Exception {
		DigestInputStream stream = null;
		try {
			stream = new DigestInputStream(new FileInputStream(file), MessageDigest.getInstance("MD5"));
			byte[] buffer = new byte[65536];

			int read = stream.read(buffer);
			while (read >= 1) {
				read = stream.read(buffer);
			}
		} catch (Exception ignored) {
			return null;
		} finally {
			stream.close();
		}

		return String.format("%1$032x", new BigInteger(1, stream.getMessageDigest().digest()));
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

	/**
	 * Unzip it
	 *
	 * @param zipFile
	 *            input zip file
	 * @param output
	 *            zip file output folder
	 */
	public static void unZip(String zipFile, String outputFolder) {

		byte[] buffer = new byte[1024];

		try {

			// get the zip file content
			ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));
			// get the zipped file list entry
			ZipEntry ze = zis.getNextEntry();

			while (ze != null) {

				String fileName = ze.getName();
				File newFile = new File(outputFolder + File.separator + fileName);

				// create all non exists folders
				// else you will hit FileNotFoundException for compressed folder
				new File(newFile.getParent()).mkdirs();

				FileOutputStream fos = new FileOutputStream(newFile);

				int len;
				while ((len = zis.read(buffer)) > 0) {
					fos.write(buffer, 0, len);
				}

				fos.close();
				ze = zis.getNextEntry();
			}

			zis.closeEntry();
			zis.close();

			RobotMod.logger.info("Done Unzipping File");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
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
