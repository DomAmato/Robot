package com.dyn.robot.programs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import dan200.computercraft.api.filesystem.IWritableMount;

public class Program {
	private IWritableMount m_storage;
	private String m_path;
	private String m_title;
	private ArrayList<String> m_codeContent;

	public Program(IWritableMount storage, String path, String title) {
		m_storage = storage;
		m_path = path;
		m_title = title;
		m_codeContent = new ArrayList();
	}

	public Program(IWritableMount storage, String path, String title, List<String> lines) {
		m_storage = storage;
		m_path = path;
		m_title = title;
		m_codeContent = new ArrayList(lines);
	}

	public Program(String title) {
		this(null, null, title);
	}

	public Program(String title, List<String> lines) {
		this(null, null, title, lines);
	}

	public void addLine(String line) {
		m_codeContent.add(line);
	}

	public void addLines(List<String> newLines) {
		m_codeContent.addAll(newLines);
	}

	public void clear() {
		m_codeContent.clear();
	}

	public int getLength() {
		return m_codeContent.size();
	}

	public String getLine(int index) {
		if ((index >= 0) && (index < m_codeContent.size())) {
			return m_codeContent.get(index);
		}
		return null;
	}

	public List<String> getLines() {
		return m_codeContent;
	}

	public String getPath() {
		return m_path;
	}

	public String getTitle() {
		return m_title;
	}

	public void load() {
		if (m_storage != null) {
			try {
				setTitle("");
				clear();
				if (m_storage.exists(m_path)) {
					InputStream stream = m_storage.openForRead(m_path);
					InputStreamReader isr;
					try {
						isr = new InputStreamReader(stream, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						isr = new InputStreamReader(stream);
					}
					BufferedReader reader = new BufferedReader(isr);
					try {
						String title = reader.readLine();
						if (title != null) {
							setTitle(title);
						}
					} finally {
						reader.close();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();

				setTitle("");
				clear();
			}
		}
	}

	public void save() {
		if (m_storage != null) {
			saveAs(m_storage, m_path);
		}
	}

	public void saveAs(IWritableMount storage, String path) {
		try {
			OutputStream stream = storage.openForWrite(path);
			PrintWriter writer = new PrintWriter(stream);
			try {
				writer.println(getTitle());

				for (int i = 0; i < getLength(); i++) {
					String line = getLine(i);
					writer.println(line);
				}

			} finally {
				writer.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setLine(int index, String line) {
		if (index >= 0) {
			while (m_codeContent.size() <= index) {
				m_codeContent.add("");
			}
			m_codeContent.set(index, line);
		}
	}

	public void setPath(String path) {
		m_path = path;
	}

	public void setTitle(String title) {
		m_title = title;
	}
}
