package com.dyn.robot.programs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dan200.computercraft.ComputerCraft;
import dan200.computercraft.core.filesystem.FileMount;
import dan200.computercraft.shared.util.IDAssigner;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class UserProgramLibrary {
	public static final int MAX_SIZE = 32;
	private static Map<String, UserProgramLibrary> s_libraryCache = new HashMap();

	public static UserProgramLibrary getLibrary(World world, String userName) {
		if (!s_libraryCache.containsKey(userName)) {
			s_libraryCache.put(userName, new UserProgramLibrary(world, userName));
		}
		return s_libraryCache.get(userName);
	}

	public static void resetLibraryCache() {
		s_libraryCache.clear();
	}

	private String m_userName;

	private FileMount m_storage;

	private List<Program> m_programs;

	private UserProgramLibrary(World world, String userName) {
		m_userName = userName;
		m_storage = ((FileMount) ComputerCraft.createSaveDirMount(world, "computer/library/" + m_userName,
				ComputerCraft.computerSpaceLimit));

		m_programs = new ArrayList();
		buildIndex();
	}

	private void buildIndex() {
		ArrayList<String> paths = new ArrayList();
		try {
			m_storage.list("", paths);
		} catch (IOException e) {
			e.printStackTrace();
			paths.clear();
		}
		ArrayList<Integer> numbers = new ArrayList(paths.size());
		for (int i = 0; i < paths.size(); i++) {
			String path = paths.get(i);
			try {
				if (!m_storage.isDirectory(path)) {
					try {
						int number = Integer.parseInt(path);
						numbers.add(Integer.valueOf(number));
						if (numbers.size() >= 32) {
							break;
						}
					} catch (NumberFormatException e) {
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Collections.sort(numbers);
		if (numbers.size() > 0) {
			for (int i = 0; i < numbers.size(); i++) {
				int number = numbers.get(i).intValue();
				Program program = new Program(m_storage, Integer.toString(number),
						StatCollector.translateToLocal("gui.dynrobot:remote.library.default_name"));

				program.load();
				m_programs.add(program);
			}
		} else {
			Program program = new Program(m_storage, findUnusedPath(),
					StatCollector.translateToLocal("gui.dynrobot:remote.library.default_name"));

			program.save();
			m_programs.add(program);
		}
	}

	public int createProgram(String title) {
		String path = findUnusedPath();
		if (path != null) {
			Program program = new Program(m_storage, path, title);
			program.save();

			int index = m_programs.size();
			m_programs.add(program);
			return index;
		}
		return -1;
	}

	public void deleteProgram(int index) {
		if ((index >= 0) && (index < m_programs.size())) {
			String path = m_programs.get(index).getPath();
			m_programs.remove(index);
			try {
				if (m_storage.exists(path)) {
					m_storage.delete(path);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private String findUnusedPath() {
		if (m_programs.size() < 32) {
			File dir = m_storage.getRealPath("");
			int nextID = IDAssigner.getNextIDFromDirectory(dir);
			return Integer.toString(nextID);
		}
		return null;
	}

	public String findUnusedTitle(String suggestedTitle) {
		if (lookupProgramByTitle(suggestedTitle) >= 0) {
			int oldNumber = 1;
			String titlePrefix = suggestedTitle;
			for (int i = 1; i < suggestedTitle.length(); i++) {
				String endString = suggestedTitle.substring(suggestedTitle.length() - i);
				try {
					oldNumber = Integer.parseInt(endString);
					titlePrefix = suggestedTitle.substring(0, suggestedTitle.length() - i);
				} catch (NumberFormatException e) {
					break;
				}
			}
			int i = oldNumber + 1;
			String programTitle;
			do {
				programTitle = titlePrefix + Integer.toString(i);
				i++;
			} while (lookupProgramByTitle(programTitle) >= 0);
			return programTitle;
		}
		return suggestedTitle;
	}

	public Program getProgram(int index) {
		if ((index >= 0) && (index < m_programs.size())) {
			return m_programs.get(index);
		}
		return null;
	}

	public int getSize() {
		return m_programs.size();
	}

	public String getUserName() {
		return m_userName;
	}

	public int insertCopyOf(Program program) {
		String path = findUnusedPath();
		if (path != null) {
			Program copy = new Program(m_storage, path, findUnusedTitle(program.getTitle()));

			copy.addLines(program.getLines());
			copy.save();

			int index = m_programs.size();
			m_programs.add(copy);
			return index;
		}
		return -1;
	}

	public int lookupProgramByPath(String path) {
		for (int i = 0; i < m_programs.size(); i++) {
			Program program = m_programs.get(i);
			if (program.getPath().equals(path)) {
				return i;
			}
		}
		return -1;
	}

	public int lookupProgramByTitle(String title) {
		for (int i = 0; i < m_programs.size(); i++) {
			Program program = m_programs.get(i);
			if (program.getTitle().equals(title)) {
				return i;
			}
		}
		return -1;
	}
}
