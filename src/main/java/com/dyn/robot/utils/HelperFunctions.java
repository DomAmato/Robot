package com.dyn.robot.utils;

import java.io.File;
import java.util.List;

public class HelperFunctions {

	/**
	 * Traverse a directory and get all files, and add the file into List
	 *
	 * @param directoryName
	 * @param files
	 */

	public static void generateFileList(String directoryName, List<File> files) {
		File directory = new File(directoryName);
		if (directory != null) {
			// get all the files from a directory
			File[] fList = directory.listFiles();
			for (File file : fList) {
				if (file.isFile()) {
					files.add(file);
				} else if (file.isDirectory()) {
					HelperFunctions.generateFileList(file.getAbsolutePath(), files);
				}
			}
		}

	}

	/*************
	 * Traverse a directory and get everything including folder names
	 *
	 * @param directoryName
	 * @param files
	 */
	public static void generateFileListWithFolders(String directoryName, List<File> files) {
		File directory = new File(directoryName);
		if (directory != null) {
			// get all the files from a directory
			File[] fList = directory.listFiles();
			if (fList != null) {
				for (File file : fList) {
					if (file.isFile()) {
						files.add(file);
					} else if (file.isDirectory()) {
						HelperFunctions.generateFileList(file.getAbsolutePath(), files);
						files.add(file);
					}
				}
			}
		}

	}
}
