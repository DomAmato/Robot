package com.dyn.robot.code.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

public class SimpleCharStream {
	public static final boolean staticFlag = false;
	int bufsize;
	int available;
	int tokenBegin;
	public int bufpos = -1;
	protected int[] bufline;
	protected int[] bufcolumn;
	protected int column = 0;
	protected int line = 1;
	protected boolean prevCharIsCR = false;
	protected boolean prevCharIsLF = false;
	protected Reader inputStream;
	protected char[] buffer;
	protected int maxNextCharInd = 0;
	protected int inBuf = 0;
	protected int tabSize = 8;
	protected boolean trackLineColumn = true;

	public SimpleCharStream(InputStream dstream) {
		this(dstream, 1, 1, 4096);
	}

	public SimpleCharStream(InputStream dstream, int startline, int startcolumn) {
		this(dstream, startline, startcolumn, 4096);
	}

	public SimpleCharStream(InputStream dstream, int startline, int startcolumn, int buffersize) {
		this(new InputStreamReader(dstream), startline, startcolumn, buffersize);
	}

	public SimpleCharStream(InputStream dstream, String encoding) throws UnsupportedEncodingException {
		this(dstream, encoding, 1, 1, 4096);
	}

	public SimpleCharStream(InputStream dstream, String encoding, int startline, int startcolumn)
			throws UnsupportedEncodingException {
		this(dstream, encoding, startline, startcolumn, 4096);
	}

	public SimpleCharStream(InputStream dstream, String encoding, int startline, int startcolumn, int buffersize)
			throws UnsupportedEncodingException {
		this(encoding == null ? new InputStreamReader(dstream) : new InputStreamReader(dstream, encoding), startline,
				startcolumn, buffersize);
	}

	public SimpleCharStream(Reader dstream) {
		this(dstream, 1, 1, 4096);
	}

	public SimpleCharStream(Reader dstream, int startline, int startcolumn) {
		this(dstream, startline, startcolumn, 4096);
	}

	public SimpleCharStream(Reader dstream, int startline, int startcolumn, int buffersize) {
		inputStream = dstream;
		line = startline;
		column = startcolumn - 1;
		available = bufsize = buffersize;
		buffer = new char[buffersize];
		bufline = new int[buffersize];
		bufcolumn = new int[buffersize];
	}

	public void adjustBeginLineColumn(int newLine, int newCol) {
		int i;
		int start = tokenBegin;
		int len = bufpos >= tokenBegin ? (bufpos - tokenBegin) + inBuf + 1
				: (bufsize - tokenBegin) + bufpos + 1 + inBuf;
		int j = 0;
		int k = 0;
		int nextColDiff = 0;
		int columnDiff = 0;
		for (i = 0; i < len; ++i) {
			j = start % bufsize;
			if (bufline[j] != bufline[k = ++start % bufsize]) {
				break;
			}
			bufline[j] = newLine;
			nextColDiff = (columnDiff + bufcolumn[k]) - bufcolumn[j];
			bufcolumn[j] = newCol + columnDiff;
			columnDiff = nextColDiff;
		}
		if (i < len) {
			bufline[j] = newLine++;
			bufcolumn[j] = newCol + columnDiff;
			while (i++ < len) {
				j = start % bufsize;
				if (bufline[j] != bufline[++start % bufsize]) {
					bufline[j] = newLine++;
					continue;
				}
				bufline[j] = newLine;
			}
		}
		line = bufline[j];
		column = bufcolumn[j];
	}

	public void backup(int amount) {
		inBuf += amount;
		if ((bufpos -= amount) < 0) {
			bufpos += bufsize;
		}
	}

	public char BeginToken() throws IOException {
		tokenBegin = -1;
		char c = readChar();
		tokenBegin = bufpos;
		return c;
	}

	public void Done() {
		buffer = null;
		bufline = null;
		bufcolumn = null;
	}

	protected void ExpandBuff(boolean wrapAround) {
		char[] newbuffer = new char[bufsize + 2048];
		int[] newbufline = new int[bufsize + 2048];
		int[] newbufcolumn = new int[bufsize + 2048];
		try {
			if (wrapAround) {
				System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
				System.arraycopy(buffer, 0, newbuffer, bufsize - tokenBegin, bufpos);
				buffer = newbuffer;
				System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
				System.arraycopy(bufline, 0, newbufline, bufsize - tokenBegin, bufpos);
				bufline = newbufline;
				System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
				System.arraycopy(bufcolumn, 0, newbufcolumn, bufsize - tokenBegin, bufpos);
				bufcolumn = newbufcolumn;
				maxNextCharInd = bufpos += bufsize - tokenBegin;
			} else {
				System.arraycopy(buffer, tokenBegin, newbuffer, 0, bufsize - tokenBegin);
				buffer = newbuffer;
				System.arraycopy(bufline, tokenBegin, newbufline, 0, bufsize - tokenBegin);
				bufline = newbufline;
				System.arraycopy(bufcolumn, tokenBegin, newbufcolumn, 0, bufsize - tokenBegin);
				bufcolumn = newbufcolumn;
				maxNextCharInd = bufpos -= tokenBegin;
			}
		} catch (Throwable t) {
			throw new Error(t.getMessage());
		}
		bufsize += 2048;
		available = bufsize;
		tokenBegin = 0;
	}

	protected void FillBuff() throws IOException {
		if (maxNextCharInd == available) {
			if (available == bufsize) {
				if (tokenBegin > 2048) {
					maxNextCharInd = 0;
					bufpos = 0;
					available = tokenBegin;
				} else if (tokenBegin < 0) {
					maxNextCharInd = 0;
					bufpos = 0;
				} else {
					ExpandBuff(false);
				}
			} else if (available > tokenBegin) {
				available = bufsize;
			} else if ((tokenBegin - available) < 2048) {
				ExpandBuff(true);
			} else {
				available = tokenBegin;
			}
		}
		try {
			int i = inputStream.read(buffer, maxNextCharInd, available - maxNextCharInd);
			if (i == -1) {
				inputStream.close();
				throw new IOException();
			}
			maxNextCharInd += i;
			return;
		} catch (IOException e) {
			--bufpos;
			backup(0);
			if (tokenBegin == -1) {
				tokenBegin = bufpos;
			}
			throw e;
		}
	}

	public int getBeginColumn() {
		return bufcolumn[tokenBegin];
	}

	public int getBeginLine() {
		return bufline[tokenBegin];
	}

	@Deprecated
	public int getColumn() {
		return bufcolumn[bufpos];
	}

	public int getEndColumn() {
		return bufcolumn[bufpos];
	}

	public int getEndLine() {
		return bufline[bufpos];
	}

	public String GetImage() {
		if (bufpos >= tokenBegin) {
			return new String(buffer, tokenBegin, (bufpos - tokenBegin) + 1);
		}
		return new String(buffer, tokenBegin, bufsize - tokenBegin) + new String(buffer, 0, bufpos + 1);
	}

	@Deprecated
	public int getLine() {
		return bufline[bufpos];
	}

	public char[] GetSuffix(int len) {
		char[] ret = new char[len];
		if ((bufpos + 1) >= len) {
			System.arraycopy(buffer, (bufpos - len) + 1, ret, 0, len);
		} else {
			System.arraycopy(buffer, bufsize - (len - bufpos - 1), ret, 0, len - bufpos - 1);
			System.arraycopy(buffer, 0, ret, len - bufpos - 1, bufpos + 1);
		}
		return ret;
	}

	public int getTabSize() {
		return tabSize;
	}

	boolean getTrackLineColumn() {
		return trackLineColumn;
	}

	public char readChar() throws IOException {
		if (inBuf > 0) {
			--inBuf;
			if (++bufpos == bufsize) {
				bufpos = 0;
			}
			return buffer[bufpos];
		}
		if (++bufpos >= maxNextCharInd) {
			FillBuff();
		}
		char c = buffer[bufpos];
		UpdateLineColumn(c);
		return c;
	}

	public void ReInit(InputStream dstream) {
		this.ReInit(dstream, 1, 1, 4096);
	}

	public void ReInit(InputStream dstream, int startline, int startcolumn) {
		this.ReInit(dstream, startline, startcolumn, 4096);
	}

	public void ReInit(InputStream dstream, int startline, int startcolumn, int buffersize) {
		this.ReInit(new InputStreamReader(dstream), startline, startcolumn, buffersize);
	}

	public void ReInit(InputStream dstream, String encoding) throws UnsupportedEncodingException {
		this.ReInit(dstream, encoding, 1, 1, 4096);
	}

	public void ReInit(InputStream dstream, String encoding, int startline, int startcolumn)
			throws UnsupportedEncodingException {
		this.ReInit(dstream, encoding, startline, startcolumn, 4096);
	}

	public void ReInit(InputStream dstream, String encoding, int startline, int startcolumn, int buffersize)
			throws UnsupportedEncodingException {
		this.ReInit(encoding == null ? new InputStreamReader(dstream) : new InputStreamReader(dstream, encoding),
				startline, startcolumn, buffersize);
	}

	public void ReInit(Reader dstream) {
		this.ReInit(dstream, 1, 1, 4096);
	}

	public void ReInit(Reader dstream, int startline, int startcolumn) {
		this.ReInit(dstream, startline, startcolumn, 4096);
	}

	public void ReInit(Reader dstream, int startline, int startcolumn, int buffersize) {
		inputStream = dstream;
		line = startline;
		column = startcolumn - 1;
		if ((buffer == null) || (buffersize != buffer.length)) {
			available = bufsize = buffersize;
			buffer = new char[buffersize];
			bufline = new int[buffersize];
			bufcolumn = new int[buffersize];
		}
		prevCharIsCR = false;
		prevCharIsLF = false;
		maxNextCharInd = 0;
		inBuf = 0;
		tokenBegin = 0;
		bufpos = -1;
	}

	public void setTabSize(int i) {
		tabSize = i;
	}

	void setTrackLineColumn(boolean tlc) {
		trackLineColumn = tlc;
	}

	protected void UpdateLineColumn(char c) {
		++column;
		if (prevCharIsLF) {
			prevCharIsLF = false;
			column = 1;
			++line;
		} else if (prevCharIsCR) {
			prevCharIsCR = false;
			if (c == '\n') {
				prevCharIsLF = true;
			} else {
				column = 1;
				++line;
			}
		}
		switch (c) {
		case '\r': {
			prevCharIsCR = true;
			break;
		}
		case '\n': {
			prevCharIsLF = true;
			break;
		}
		case '\t': {
			--column;
			column += tabSize - (column % tabSize);
			break;
		}
		}
		bufline[bufpos] = line;
		bufcolumn[bufpos] = column;
	}
}
