package com.dom.robot.utils;

import java.io.File;
import java.util.List;

import net.minecraft.util.EnumFacing;

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

	public static float getAngleFromFacing(EnumFacing dir) {
		switch (dir) {
		case SOUTH:
			return 0;
		case NORTH:
			return 180;
		case EAST:
			return 270;
		case WEST:
			return 90;
		default:
			return 0;
		}
	}
}
