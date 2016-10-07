package com.dyn.robot.code.grammar;

import java.io.IOException;
import java.io.PrintStream;

public class GrammarTokenManager implements GrammarConstants {
	static final long[] jjbitVec0 = new long[] { 0, 0, -1, -1 };
	static final int[] jjnextStates = new int[] { 2, 3 };
	public static final String[] jjstrLiteralImages = new String[] { "", null, null, null, null, null, "dynrobot:if",
			"dynrobot:then", "dynrobot:elseIf", "dynrobot:else", "dynrobot:end", "dynrobot:while", "dynrobot:do",
			"dynrobot:for", "dynrobot:repeat", "dynrobot:to", "dynrobot:break", "dynrobot:equalTo", null,
			"dynrobot:variable", "dynrobot:true", "dynrobot:false", "dynrobot:number", "dynrobot:string",
			"dynrobot:block", "dynrobot:item", "dynrobot:or", "dynrobot:and", "dynrobot:notEqualTo",
			"dynrobot:lessThan", "dynrobot:lessThanOrEqualTo", "dynrobot:greaterThan", "dynrobot:greaterThanOrEqualTo",
			"dynrobot:plus", "dynrobot:minus", "dynrobot:times", "dynrobot:dividedBy", "dynrobot:not",
			"dynrobot:moveForward", "dynrobot:moveBack", "dynrobot:moveUp", "dynrobot:moveDown", "dynrobot:turnLeft",
			"dynrobot:turnRight", "dynrobot:dig", "dynrobot:digUp", "dynrobot:digDown", "dynrobot:detect",
			"dynrobot:detectUp", "dynrobot:detectDown", "dynrobot:place", "dynrobot:placeUp", "dynrobot:placeDown",
			"dynrobot:attack", "dynrobot:attackUp", "dynrobot:attackDown", "dynrobot:equipLeft", "dynrobot:equipRight",
			"dynrobot:drop", "dynrobot:dropUp", "dynrobot:dropDown", "dynrobot:suck", "dynrobot:suckUp",
			"dynrobot:suckDown", "dynrobot:compare", "dynrobot:compareUp", "dynrobot:compareDown",
			"dynrobot:getItemCount", "dynrobot:queryRedstone", "dynrobot:queryRedstoneUp", "dynrobot:queryRedstoneDown",
			"dynrobot:random", "dynrobot:programDisk", "dynrobot:inspect", "dynrobot:inspectUp", "dynrobot:inspectDown",
			"dynrobot:inspectSlot", "dynrobot:select", "dynrobot:setRedstone", "dynrobot:setRedstoneUp",
			"dynrobot:setRedstoneDown", "dynrobot:randomNumber", "dynrobot:say", "dynrobot:comment" };
	public static final String[] lexStateNames = new String[] { "DEFAULT" };
	static final long[] jjtoToken = new long[] { -7, 1048575 };
	static final long[] jjtoSkip = new long[] { 6, 0 };
	public PrintStream debugStream = System.out;
	int curLexState = 0;
	int defaultLexState = 0;
	int jjnewStateCnt;
	int jjround;
	int jjmatchedPos;
	int jjmatchedKind;
	protected SimpleCharStream input_stream;
	private final int[] jjrounds = new int[9];
	private final int[] jjstateSet = new int[18];
	protected char curChar;

	public GrammarTokenManager(SimpleCharStream stream) {
		input_stream = stream;
	}

	public GrammarTokenManager(SimpleCharStream stream, int lexState) {
		this.ReInit(stream);
		SwitchTo(lexState);
	}

	public Token getNextToken() {
		int curPos;
		block11: {
			curPos = 0;
			do {
				try {
					curChar = input_stream.BeginToken();
				} catch (IOException e) {
					jjmatchedKind = 0;
					jjmatchedPos = -1;
					Token matchedToken = jjFillToken();
					return matchedToken;
				}
				try {
					input_stream.backup(0);
					while ((curChar <= ' ') && ((4294967808L & (1 << curChar)) != 0)) {
						curChar = input_stream.BeginToken();
					}
				} catch (IOException e1) {
					continue;
				}
				jjmatchedKind = Integer.MAX_VALUE;
				jjmatchedPos = 0;
				curPos = jjMoveStringLiteralDfa0_0();
				if (jjmatchedKind == Integer.MAX_VALUE) {
					break block11;
				}
				if ((jjmatchedPos + 1) < curPos) {
					input_stream.backup(curPos - jjmatchedPos - 1);
				}
				if ((jjtoToken[jjmatchedKind >> 6] & (1 << (jjmatchedKind & 63))) != 0) {
					break;
				}
			} while (true);
			Token matchedToken = jjFillToken();
			return matchedToken;
		}
		int error_line = input_stream.getEndLine();
		int error_column = input_stream.getEndColumn();
		String error_after = null;
		boolean EOFSeen = false;
		try {
			input_stream.readChar();
			input_stream.backup(1);
		} catch (IOException e1) {
			EOFSeen = true;
			error_after = curPos <= 1 ? "" : input_stream.GetImage();
			if ((curChar == '\n') || (curChar == '\r')) {
				++error_line;
				error_column = 0;
			}
			++error_column;
		}
		if (!EOFSeen) {
			input_stream.backup(1);
			error_after = curPos <= 1 ? "" : input_stream.GetImage();
		}
		throw new TokenMgrError(EOFSeen, curLexState, error_line, error_column, error_after, curChar, 0);
	}

	private void jjAddStates(int start, int end) {
		do {
			jjstateSet[jjnewStateCnt++] = jjnextStates[start];
		} while (start++ != end);
	}

	private void jjCheckNAdd(int state) {
		if (jjrounds[state] != jjround) {
			jjstateSet[jjnewStateCnt++] = state;
			jjrounds[state] = jjround;
		}
	}

	private void jjCheckNAddTwoStates(int state1, int state2) {
		jjCheckNAdd(state1);
		jjCheckNAdd(state2);
	}

	protected Token jjFillToken() {
		String im = jjstrLiteralImages[jjmatchedKind];
		String curTokenImage = im == null ? input_stream.GetImage() : im;
		int beginLine = input_stream.getBeginLine();
		int beginColumn = input_stream.getBeginColumn();
		int endLine = input_stream.getEndLine();
		int endColumn = input_stream.getEndColumn();
		Token t = Token.newToken(jjmatchedKind, curTokenImage);
		t.beginLine = beginLine;
		t.endLine = endLine;
		t.beginColumn = beginColumn;
		t.endColumn = endColumn;
		return t;
	}

	/*
	 * Enabled force condition propagation Lifted jumps to return sites
	 */
	private int jjMoveNfa_0(int startState, int curPos) {
		int startsAt = 0;
		jjnewStateCnt = 9;
		int i = 1;
		jjstateSet[0] = startState;
		int kind = Integer.MAX_VALUE;
		do {
			long l;
			if (++jjround == Integer.MAX_VALUE) {
				ReInitRounds();
			}
			if (curChar < '@') {
				l = 1 << curChar;
				block20: do {
					switch (jjstateSet[--i]) {
					case 1: {
						if ((287948901175001088L & l) != 0) {
							if (kind > 3) {
								kind = 3;
							}
							jjCheckNAdd(0);
							break;
						}
						if (curChar == '\n') {
							if (kind <= 18) {
								break;
							}
							kind = 18;
							break;
						}
						if (curChar == '\r') {
							jjstateSet[jjnewStateCnt++] = 6;
							break;
						}
						if (curChar != '\"') {
							break;
						}
						jjCheckNAddTwoStates(2, 3);
						break;
					}
					case 0: {
						if ((287948901175001088L & l) == 0) {
							continue block20;
						}
						if (kind > 3) {
							kind = 3;
						}
						jjCheckNAdd(0);
						break;
					}
					case 2: {
						if ((-17179869185L & l) == 0) {
							break;
						}
						jjCheckNAddTwoStates(2, 3);
						break;
					}
					case 3: {
						if ((curChar != '\"') || (kind <= 4)) {
							continue block20;
						}
						kind = 4;
						break;
					}
					case 5: {
						if ((287948901175001088L & l) == 0) {
							continue block20;
						}
						if (kind > 5) {
							kind = 5;
						}
						jjstateSet[jjnewStateCnt++] = 5;
						break;
					}
					case 6: {
						if ((curChar != '\n') || (kind <= 18)) {
							continue block20;
						}
						kind = 18;
						break;
					}
					case 7: {
						if (curChar != '\r') {
							break;
						}
						jjstateSet[jjnewStateCnt++] = 6;
						break;
					}
					case 8: {
						if ((curChar != '\n') || (kind <= 18)) {
							continue block20;
						}
						kind = 18;
						break;
					}
					}
				} while (i != startsAt);
			} else if (curChar < '?') {
				l = 1 << (curChar & 63);
				block21: do {
					switch (jjstateSet[--i]) {
					case 1:
					case 5: {
						if ((576460745995190270L & l) == 0) {
							continue block21;
						}
						if (kind > 5) {
							kind = 5;
						}
						jjCheckNAdd(5);
						break;
					}
					case 2: {
						jjAddStates(0, 1);
						break;
					}
					}
				} while (i != startsAt);
			} else {
				int i2 = (curChar & 255) >> 6;
				long l2 = 1 << (curChar & 63);
				do {
					switch (jjstateSet[--i]) {
					case 2: {
						if ((jjbitVec0[i2] & l2) == 0) {
							break;
						}
						jjAddStates(0, 1);
						break;
					}
					}
				} while (i != startsAt);
			}
			if (kind != Integer.MAX_VALUE) {
				jjmatchedKind = kind;
				jjmatchedPos = curPos;
				kind = Integer.MAX_VALUE;
			}
			++curPos;
			i = jjnewStateCnt;
			jjnewStateCnt = startsAt;
			startsAt = 9 - jjnewStateCnt;
			if (i == startsAt) {
				return curPos;
			}
			try {
				curChar = input_stream.readChar();
				continue;
			} catch (IOException e) {
			}
			return curPos;
		} while (true);
	}

	private int jjMoveStringLiteralDfa0_0() {
		switch (curChar) {
		case 'c': {
			return jjMoveStringLiteralDfa1_0(-262208, 1048575);
		}
		}
		return jjMoveNfa_0(1, 0);
	}

	private int jjMoveStringLiteralDfa1_0(long active0, long active1) {
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(0, active0, active1);
			return 1;
		}
		switch (curChar) {
		case 'o': {
			return jjMoveStringLiteralDfa2_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(0, active0, active1);
	}

	private int jjMoveStringLiteralDfa10_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(8, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(9, active0, active1);
			return 10;
		}
		switch (curChar) {
		case 'a': {
			return jjMoveStringLiteralDfa11_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(9, active0, active1);
	}

	private int jjMoveStringLiteralDfa11_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(9, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(10, active0, active1);
			return 11;
		}
		switch (curChar) {
		case 'f': {
			return jjMoveStringLiteralDfa12_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(10, active0, active1);
	}

	private int jjMoveStringLiteralDfa12_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(10, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(11, active0, active1);
			return 12;
		}
		switch (curChar) {
		case 't': {
			return jjMoveStringLiteralDfa13_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(11, active0, active1);
	}

	private int jjMoveStringLiteralDfa13_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(11, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(12, active0, active1);
			return 13;
		}
		switch (curChar) {
		case 'e': {
			return jjMoveStringLiteralDfa14_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(12, active0, active1);
	}

	private int jjMoveStringLiteralDfa14_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(12, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(13, active0, active1);
			return 14;
		}
		switch (curChar) {
		case 'd': {
			return jjMoveStringLiteralDfa15_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(13, active0, active1);
	}

	private int jjMoveStringLiteralDfa15_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(13, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(14, active0, active1);
			return 15;
		}
		switch (curChar) {
		case 'u': {
			return jjMoveStringLiteralDfa16_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(14, active0, active1);
	}

	private int jjMoveStringLiteralDfa16_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(14, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(15, active0, active1);
			return 16;
		}
		switch (curChar) {
		case ':': {
			return jjMoveStringLiteralDfa17_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(15, active0, active1);
	}

	private int jjMoveStringLiteralDfa17_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(15, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(16, active0, active1);
			return 17;
		}
		switch (curChar) {
		case 'a': {
			return jjMoveStringLiteralDfa18_0(active0, 63050394917404672L, active1, 0);
		}
		case 'b': {
			return jjMoveStringLiteralDfa18_0(active0, 0x1010000, active1, 0);
		}
		case 'c': {
			return jjMoveStringLiteralDfa18_0(active0, 0, active1, 524295);
		}
		case 'd': {
			return jjMoveStringLiteralDfa18_0(active0, 2018721009502261248L, active1, 0);
		}
		case 'e': {
			return jjMoveStringLiteralDfa18_0(active0, 216172782113916672L, active1, 0);
		}
		case 'f': {
			return jjMoveStringLiteralDfa18_0(active0, 0x202000, active1, 0);
		}
		case 'g': {
			return jjMoveStringLiteralDfa18_0(active0, 6442450944L, active1, 8);
		}
		case 'i': {
			return jjMoveStringLiteralDfa18_0(active0, 33554496, active1, 7680);
		}
		case 'l': {
			return jjMoveStringLiteralDfa18_0(active0, 0x60000000, active1, 0);
		}
		case 'm': {
			return jjMoveStringLiteralDfa18_0(active0, 4140348473344L, active1, 0);
		}
		case 'n': {
			return jjMoveStringLiteralDfa18_0(active0, 137711583232L, active1, 0);
		}
		case 'o': {
			return jjMoveStringLiteralDfa18_0(active0, 0x4000000, active1, 0);
		}
		case 'p': {
			return jjMoveStringLiteralDfa18_0(active0, 7881307937832960L, active1, 256);
		}
		case 'q': {
			return jjMoveStringLiteralDfa18_0(active0, 0, active1, 112);
		}
		case 'r': {
			return jjMoveStringLiteralDfa18_0(active0, 16384, active1, 131200);
		}
		case 's': {
			return jjMoveStringLiteralDfa18_0(active0, -2305843009205305344L, active1, 385024);
		}
		case 't': {
			return jjMoveStringLiteralDfa18_0(active0, 13228500353152L, active1, 0);
		}
		case 'v': {
			return jjMoveStringLiteralDfa18_0(active0, 524288, active1, 0);
		}
		case 'w': {
			return jjMoveStringLiteralDfa18_0(active0, 2048, active1, 0);
		}
		}
		return jjStartNfa_0(16, active0, active1);
	}

	private int jjMoveStringLiteralDfa18_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(16, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(17, active0, active1);
			return 18;
		}
		switch (curChar) {
		case 'a': {
			return jjMoveStringLiteralDfa19_0(active0, 2621440, active1, 393344);
		}
		case 'e': {
			return jjMoveStringLiteralDfa19_0(active0, 985164029116416L, active1, 122888);
		}
		case 'f': {
			if ((active0 & 64) == 0) {
				break;
			}
			return jjStopAtPos(18, 6);
		}
		case 'h': {
			return jjMoveStringLiteralDfa19_0(active0, 2176, active1, 0);
		}
		case 'i': {
			return jjMoveStringLiteralDfa19_0(active0, 123265561395200L, active1, 0);
		}
		case 'l': {
			return jjMoveStringLiteralDfa19_0(active0, 7881307954610944L, active1, 0);
		}
		case 'n': {
			return jjMoveStringLiteralDfa19_0(active0, 134218752, active1, 7680);
		}
		case 'o': {
			if ((active0 & 4096) != 0) {
				return jjStopAtPos(18, 12);
			}
			if ((active0 & 32768) != 0) {
				return jjStopAtPos(18, 15);
			}
			return jjMoveStringLiteralDfa19_0(active0, 4260876001280L, active1, 524295);
		}
		case 'q': {
			return jjMoveStringLiteralDfa19_0(active0, 216172782113914880L, active1, 0);
		}
		case 'r': {
			if ((active0 & 0x4000000) != 0) {
				return jjStopAtPos(18, 26);
			}
			return jjMoveStringLiteralDfa19_0(active0, 2017612639505547264L, active1, 256);
		}
		case 't': {
			return jjMoveStringLiteralDfa19_0(active0, 63050394825129984L, active1, 0);
		}
		case 'u': {
			return jjMoveStringLiteralDfa19_0(active0, -2305829815069966336L, active1, 112);
		}
		}
		return jjStartNfa_0(17, active0, active1);
	}

	private int jjMoveStringLiteralDfa19_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(17, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(18, active0, active1);
			return 19;
		}
		switch (curChar) {
		case 'a': {
			return jjMoveStringLiteralDfa20_0(active0, 7881299347898368L, active1, 0);
		}
		case 'c': {
			return jjMoveStringLiteralDfa20_0(active0, -2305843009213693952L, active1, 0);
		}
		case 'd': {
			if ((active0 & 1024) != 0) {
				return jjStopAtPos(19, 10);
			}
			if ((active0 & 0x8000000) == 0) {
				break;
			}
			return jjStopAtPos(19, 27);
		}
		case 'e': {
			return jjMoveStringLiteralDfa20_0(active0, 6476071040L, active1, 112);
		}
		case 'g': {
			if ((active0 & 0x100000000000L) != 0) {
				jjmatchedKind = 44;
				jjmatchedPos = 19;
			}
			return jjMoveStringLiteralDfa20_0(active0, 0x600000000000L, active1, 0);
		}
		case 'i': {
			return jjMoveStringLiteralDfa20_0(active0, 2048, active1, 0);
		}
		case 'l': {
			return jjMoveStringLiteralDfa20_0(active0, 0x200000, active1, 8192);
		}
		case 'm': {
			return jjMoveStringLiteralDfa20_0(active0, 34363932672L, active1, 524295);
		}
		case 'n': {
			return jjMoveStringLiteralDfa20_0(active0, 0x400000000L, active1, 131200);
		}
		case 'o': {
			return jjMoveStringLiteralDfa20_0(active0, 2017612633078759424L, active1, 256);
		}
		case 'p': {
			return jjMoveStringLiteralDfa20_0(active0, 16384, active1, 0);
		}
		case 'r': {
			if ((active0 & 8192) != 0) {
				return jjStopAtPos(19, 13);
			}
			return jjMoveStringLiteralDfa20_0(active0, 13194148446208L, active1, 0);
		}
		case 's': {
			return jjMoveStringLiteralDfa20_0(active0, 1610613504, active1, 7680);
		}
		case 't': {
			if ((active0 & 0x2000000000L) != 0) {
				jjmatchedKind = 37;
				jjmatchedPos = 19;
			}
			return jjMoveStringLiteralDfa20_0(active0, 64035557470109696L, active1, 114696);
		}
		case 'u': {
			return jjMoveStringLiteralDfa20_0(active0, 216172790704898048L, active1, 0);
		}
		case 'v': {
			return jjMoveStringLiteralDfa20_0(active0, 4191888080896L, active1, 0);
		}
		case 'y': {
			if ((active1 & 262144) == 0) {
				break;
			}
			return jjStopAtPos(19, 82);
		}
		}
		return jjStartNfa_0(18, active0, active1);
	}

	private int jjMoveStringLiteralDfa2_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(0, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(1, active0, active1);
			return 2;
		}
		switch (curChar) {
		case 'm': {
			return jjMoveStringLiteralDfa3_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(1, active0, active1);
	}

	private int jjMoveStringLiteralDfa20_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(18, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(19, active0, active1);
			return 20;
		}
		switch (curChar) {
		case 'D': {
			return jjMoveStringLiteralDfa21_0(active0, 0x400000000000L, active1, 0);
		}
		case 'E': {
			return jjMoveStringLiteralDfa21_0(active0, 0x10000000, active1, 0);
		}
		case 'I': {
			return jjMoveStringLiteralDfa21_0(active0, 0, active1, 8);
		}
		case 'R': {
			return jjMoveStringLiteralDfa21_0(active0, 0, active1, 114688);
		}
		case 'U': {
			return jjMoveStringLiteralDfa21_0(active0, 0x200000000000L, active1, 0);
		}
		case 'a': {
			return jjMoveStringLiteralDfa21_0(active0, 63050401225834496L, active1, 0);
		}
		case 'b': {
			return jjMoveStringLiteralDfa21_0(active0, 0x400000, active1, 0);
		}
		case 'c': {
			return jjMoveStringLiteralDfa21_0(active0, 7881299364675584L, active1, 0);
		}
		case 'd': {
			return jjMoveStringLiteralDfa21_0(active0, 0, active1, 131200);
		}
		case 'e': {
			if ((active0 & 512) != 0) {
				jjmatchedKind = 9;
				jjmatchedPos = 20;
			} else if ((active0 & 0x100000) != 0) {
				return jjStopAtPos(20, 20);
			}
			return jjMoveStringLiteralDfa21_0(active0, 989319946846464L, active1, 8192);
		}
		case 'g': {
			return jjMoveStringLiteralDfa21_0(active0, 0, active1, 256);
		}
		case 'i': {
			return jjMoveStringLiteralDfa21_0(active0, 216172850842173440L, active1, 0);
		}
		case 'k': {
			if ((active0 & 0x2000000000000000L) != 0) {
				jjmatchedKind = 61;
				jjmatchedPos = 20;
			}
			return jjMoveStringLiteralDfa21_0(active0, -4611686018427387904L, active1, 0);
		}
		case 'l': {
			return jjMoveStringLiteralDfa21_0(active0, 2048, active1, 0);
		}
		case 'm': {
			if ((active0 & 0x2000000) != 0) {
				return jjStopAtPos(20, 25);
			}
			return jjMoveStringLiteralDfa21_0(active0, 0, active1, 524288);
		}
		case 'n': {
			if ((active0 & 128) != 0) {
				return jjStopAtPos(20, 7);
			}
			return jjMoveStringLiteralDfa21_0(active0, 0xC0000000000L, active1, 0);
		}
		case 'p': {
			if ((active0 & 0x400000000000000L) != 0) {
				jjmatchedKind = 58;
				jjmatchedPos = 20;
			}
			return jjMoveStringLiteralDfa21_0(active0, 1729382256910270464L, active1, 7687);
		}
		case 'r': {
			return jjMoveStringLiteralDfa21_0(active0, 0, active1, 112);
		}
		case 's': {
			if ((active0 & 0x200000000L) != 0) {
				return jjStopAtPos(20, 33);
			}
			return jjMoveStringLiteralDfa21_0(active0, 1612709888, active1, 0);
		}
		case 'u': {
			return jjMoveStringLiteralDfa21_0(active0, 0x400000000L, active1, 0);
		}
		}
		return jjStartNfa_0(19, active0, active1);
	}

	private int jjMoveStringLiteralDfa21_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(19, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(20, active0, active1);
			return 21;
		}
		switch (curChar) {
		case 'B': {
			return jjMoveStringLiteralDfa22_0(active0, 0x8000000000L, active1, 0);
		}
		case 'D': {
			return jjMoveStringLiteralDfa22_0(active0, -8070448333224673280L, active1, 0);
		}
		case 'F': {
			return jjMoveStringLiteralDfa22_0(active0, 0x4000000000L, active1, 0);
		}
		case 'I': {
			return jjMoveStringLiteralDfa22_0(active0, 256, active1, 0);
		}
		case 'L': {
			return jjMoveStringLiteralDfa22_0(active0, 0x40000000000L, active1, 0);
		}
		case 'R': {
			return jjMoveStringLiteralDfa22_0(active0, 0x80000000000L, active1, 0);
		}
		case 'T': {
			return jjMoveStringLiteralDfa22_0(active0, 0x60000000, active1, 0);
		}
		case 'U': {
			return jjMoveStringLiteralDfa22_0(active0, 5188147870242439168L, active1, 0);
		}
		case 'a': {
			return jjMoveStringLiteralDfa22_0(active0, 540672, active1, 7);
		}
		case 'c': {
			return jjMoveStringLiteralDfa22_0(active0, 64035557201674240L, active1, 8192);
		}
		case 'd': {
			return jjMoveStringLiteralDfa22_0(active0, 0x1000000000L, active1, 0);
		}
		case 'e': {
			if ((active0 & 2048) != 0) {
				return jjStopAtPos(21, 11);
			}
			if ((active0 & 0x200000) != 0) {
				return jjStopAtPos(21, 21);
			}
			if ((active0 & 0x4000000000000L) != 0) {
				jjmatchedKind = 50;
				jjmatchedPos = 21;
			}
			return jjMoveStringLiteralDfa22_0(active0, 6755399445250048L, active1, 646656);
		}
		case 'k': {
			if ((active0 & 65536) != 0) {
				return jjStopAtPos(21, 16);
			}
			if ((active0 & 0x1000000) == 0) {
				break;
			}
			return jjStopAtPos(21, 24);
		}
		case 'l': {
			return jjMoveStringLiteralDfa22_0(active0, 131072, active1, 0);
		}
		case 'n': {
			return jjMoveStringLiteralDfa22_0(active0, 0x800000, active1, 0);
		}
		case 'o': {
			return jjMoveStringLiteralDfa22_0(active0, 0x400000000000L, active1, 131200);
		}
		case 'p': {
			if ((active0 & 0x200000000000L) != 0) {
				return jjStopAtPos(21, 45);
			}
			return jjMoveStringLiteralDfa22_0(active0, 0x300000000000000L, active1, 0);
		}
		case 'q': {
			return jjMoveStringLiteralDfa22_0(active0, 0x10000000, active1, 0);
		}
		case 'r': {
			return jjMoveStringLiteralDfa22_0(active0, 0, active1, 256);
		}
		case 's': {
			if ((active0 & 0x400000000L) != 0) {
				return jjStopAtPos(21, 34);
			}
			if ((active0 & 0x800000000L) == 0) {
				break;
			}
			return jjStopAtPos(21, 35);
		}
		case 't': {
			return jjMoveStringLiteralDfa22_0(active0, 6442450944L, active1, 8);
		}
		case 'y': {
			return jjMoveStringLiteralDfa22_0(active0, 0, active1, 112);
		}
		}
		return jjStartNfa_0(20, active0, active1);
	}

	private int jjMoveStringLiteralDfa22_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(20, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(21, active0, active1);
			return 22;
		}
		switch (curChar) {
		case 'D': {
			return jjMoveStringLiteralDfa23_0(active0, 0x10000000000000L, active1, 0);
		}
		case 'L': {
			return jjMoveStringLiteralDfa23_0(active0, 0x100000000000000L, active1, 0);
		}
		case 'R': {
			return jjMoveStringLiteralDfa23_0(active0, 0x200000000000000L, active1, 112);
		}
		case 'T': {
			return jjMoveStringLiteralDfa23_0(active0, 131072, active1, 0);
		}
		case 'U': {
			return jjMoveStringLiteralDfa23_0(active0, 0x8000000000000L, active1, 0);
		}
		case 'a': {
			return jjMoveStringLiteralDfa23_0(active0, 0x8000000000L, active1, 256);
		}
		case 'b': {
			return jjMoveStringLiteralDfa23_0(active0, 524288, active1, 0);
		}
		case 'c': {
			return jjMoveStringLiteralDfa23_0(active0, 0, active1, 7680);
		}
		case 'd': {
			return jjMoveStringLiteralDfa23_0(active0, 0, active1, 114688);
		}
		case 'e': {
			return jjMoveStringLiteralDfa23_0(active0, 4473208438784L, active1, 8);
		}
		case 'f': {
			if ((active0 & 256) == 0) {
				break;
			}
			return jjStopAtPos(22, 8);
		}
		case 'g': {
			if ((active0 & 0x800000) == 0) {
				break;
			}
			return jjStopAtPos(22, 23);
		}
		case 'h': {
			return jjMoveStringLiteralDfa23_0(active0, 0x60000000, active1, 0);
		}
		case 'i': {
			return jjMoveStringLiteralDfa23_0(active0, 0x80000000000L, active1, 0);
		}
		case 'k': {
			if ((active0 & 0x20000000000000L) != 0) {
				jjmatchedKind = 53;
				jjmatchedPos = 22;
			}
			return jjMoveStringLiteralDfa23_0(active0, 0xC0000000000000L, active1, 0);
		}
		case 'm': {
			if ((active1 & 128) != 0) {
				jjmatchedKind = 71;
				jjmatchedPos = 22;
			}
			return jjMoveStringLiteralDfa23_0(active0, 0, active1, 131072);
		}
		case 'n': {
			return jjMoveStringLiteralDfa23_0(active0, 0, active1, 524288);
		}
		case 'o': {
			return jjMoveStringLiteralDfa23_0(active0, -8070448058346766336L, active1, 0);
		}
		case 'p': {
			if ((active0 & 0x10000000000L) != 0) {
				return jjStopAtPos(22, 40);
			}
			if ((active0 & 0x800000000000000L) != 0) {
				return jjStopAtPos(22, 59);
			}
			if ((active0 & 0x4000000000000000L) == 0) {
				break;
			}
			return jjStopAtPos(22, 62);
		}
		case 'r': {
			if ((active0 & 0x400000) != 0) {
				return jjStopAtPos(22, 22);
			}
			return jjMoveStringLiteralDfa23_0(active0, 0, active1, 7);
		}
		case 't': {
			if ((active0 & 16384) != 0) {
				return jjStopAtPos(22, 14);
			}
			if ((active0 & 0x800000000000L) != 0) {
				jjmatchedKind = 47;
				jjmatchedPos = 22;
			} else if ((active1 & 8192) != 0) {
				return jjStopAtPos(22, 77);
			}
			return jjMoveStringLiteralDfa23_0(active0, 0x3000000000000L, active1, 0);
		}
		case 'u': {
			return jjMoveStringLiteralDfa23_0(active0, 0x10000000, active1, 0);
		}
		case 'w': {
			return jjMoveStringLiteralDfa23_0(active0, 0x400000000000L, active1, 0);
		}
		}
		return jjStartNfa_0(21, active0, active1);
	}

	private int jjMoveStringLiteralDfa23_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(21, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(22, active0, active1);
			return 23;
		}
		switch (curChar) {
		case 'D': {
			return jjMoveStringLiteralDfa24_0(active0, 36591746972385280L, active1, 0);
		}
		case 'N': {
			return jjMoveStringLiteralDfa24_0(active0, 0, active1, 131072);
		}
		case 'U': {
			return jjMoveStringLiteralDfa24_0(active0, 18295873486192640L, active1, 0);
		}
		case 'a': {
			return jjMoveStringLiteralDfa24_0(active0, 0x70000000, active1, 0);
		}
		case 'c': {
			return jjMoveStringLiteralDfa24_0(active0, 0x8000000000L, active1, 0);
		}
		case 'd': {
			return jjMoveStringLiteralDfa24_0(active0, 0x1000000000L, active1, 0);
		}
		case 'e': {
			if ((active1 & 1) != 0) {
				jjmatchedKind = 64;
				jjmatchedPos = 23;
			}
			return jjMoveStringLiteralDfa24_0(active0, 0x100000000000000L, active1, 118);
		}
		case 'f': {
			return jjMoveStringLiteralDfa24_0(active0, 0x40000000000L, active1, 0);
		}
		case 'g': {
			return jjMoveStringLiteralDfa24_0(active0, 0x80000000000L, active1, 0);
		}
		case 'i': {
			return jjMoveStringLiteralDfa24_0(active0, 0x200000000000000L, active1, 0);
		}
		case 'l': {
			return jjMoveStringLiteralDfa24_0(active0, 524288, active1, 0);
		}
		case 'm': {
			return jjMoveStringLiteralDfa24_0(active0, 0, active1, 264);
		}
		case 'n': {
			if ((active0 & 0x400000000000L) == 0) {
				break;
			}
			return jjStopAtPos(23, 46);
		}
		case 'o': {
			if ((active0 & 131072) != 0) {
				return jjStopAtPos(23, 17);
			}
			return jjMoveStringLiteralDfa24_0(active0, 0x10000000000000L, active1, 0);
		}
		case 'p': {
			if ((active0 & 0x8000000000000L) == 0) {
				break;
			}
			return jjStopAtPos(23, 51);
		}
		case 'r': {
			return jjMoveStringLiteralDfa24_0(active0, 281320357888L, active1, 0);
		}
		case 's': {
			return jjMoveStringLiteralDfa24_0(active0, 0, active1, 114688);
		}
		case 't': {
			if ((active1 & 512) != 0) {
				jjmatchedKind = 73;
				jjmatchedPos = 23;
			} else if ((active1 & 524288) != 0) {
				return jjStopAtPos(23, 83);
			}
			return jjMoveStringLiteralDfa24_0(active0, 0, active1, 7168);
		}
		case 'w': {
			return jjMoveStringLiteralDfa24_0(active0, -8070448333224673280L, active1, 0);
		}
		}
		return jjStartNfa_0(22, active0, active1);
	}

	private int jjMoveStringLiteralDfa24_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(22, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(23, active0, active1);
			return 24;
		}
		switch (curChar) {
		case 'B': {
			return jjMoveStringLiteralDfa25_0(active0, 0x1000000000L, active1, 0);
		}
		case 'C': {
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 8);
		}
		case 'D': {
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 2308);
		}
		case 'S': {
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 4096);
		}
		case 'T': {
			return jjMoveStringLiteralDfa25_0(active0, 6442450944L, active1, 0);
		}
		case 'U': {
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 1026);
		}
		case 'd': {
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 112);
		}
		case 'e': {
			if ((active0 & 524288) == 0) {
				break;
			}
			return jjStopAtPos(24, 19);
		}
		case 'f': {
			return jjMoveStringLiteralDfa25_0(active0, 0x100000000000000L, active1, 0);
		}
		case 'g': {
			return jjMoveStringLiteralDfa25_0(active0, 0x200000000000000L, active1, 0);
		}
		case 'h': {
			return jjMoveStringLiteralDfa25_0(active0, 0x80000000000L, active1, 0);
		}
		case 'k': {
			if ((active0 & 0x8000000000L) == 0) {
				break;
			}
			return jjStopAtPos(24, 39);
		}
		case 'l': {
			return jjMoveStringLiteralDfa25_0(active0, 0x10000000, active1, 0);
		}
		case 'n': {
			if ((active0 & 0x20000000) != 0) {
				jjmatchedKind = 29;
				jjmatchedPos = 24;
			} else {
				if ((active0 & 0x20000000000L) != 0) {
					return jjStopAtPos(24, 41);
				}
				if ((active0 & 0x1000000000000000L) != 0) {
					return jjStopAtPos(24, 60);
				}
				if ((active0 & Long.MIN_VALUE) != 0) {
					return jjStopAtPos(24, 63);
				}
			}
			return jjMoveStringLiteralDfa25_0(active0, 0x40000000, active1, 0);
		}
		case 'o': {
			return jjMoveStringLiteralDfa25_0(active0, 36591746972385280L, active1, 0);
		}
		case 'p': {
			if ((active0 & 0x1000000000000L) != 0) {
				return jjStopAtPos(24, 48);
			}
			if ((active0 & 0x40000000000000L) == 0) {
				break;
			}
			return jjStopAtPos(24, 54);
		}
		case 't': {
			if ((active0 & 0x40000000000L) != 0) {
				return jjStopAtPos(24, 42);
			}
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 114688);
		}
		case 'u': {
			return jjMoveStringLiteralDfa25_0(active0, 0, active1, 131072);
		}
		case 'w': {
			return jjMoveStringLiteralDfa25_0(active0, 4503874505277440L, active1, 0);
		}
		}
		return jjStartNfa_0(23, active0, active1);
	}

	private int jjMoveStringLiteralDfa25_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(23, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(24, active0, active1);
			return 25;
		}
		switch (curChar) {
		case 'O': {
			return jjMoveStringLiteralDfa26_0(active0, 0x40000000, active1, 0);
		}
		case 'T': {
			return jjMoveStringLiteralDfa26_0(active0, 0x10000000, active1, 0);
		}
		case 'a': {
			return jjMoveStringLiteralDfa26_0(active0, 0x4000000000L, active1, 0);
		}
		case 'h': {
			return jjMoveStringLiteralDfa26_0(active0, 144115194518306816L, active1, 0);
		}
		case 'i': {
			return jjMoveStringLiteralDfa26_0(active0, 0, active1, 256);
		}
		case 'l': {
			return jjMoveStringLiteralDfa26_0(active0, 0, active1, 4096);
		}
		case 'm': {
			return jjMoveStringLiteralDfa26_0(active0, 0, active1, 131072);
		}
		case 'n': {
			if ((active0 & 0x10000000000000L) == 0) {
				break;
			}
			return jjStopAtPos(25, 52);
		}
		case 'o': {
			return jjMoveStringLiteralDfa26_0(active0, 0, active1, 116748);
		}
		case 'p': {
			if ((active1 & 2) != 0) {
				return jjStopAtPos(25, 65);
			}
			if ((active1 & 1024) == 0) {
				break;
			}
			return jjStopAtPos(25, 74);
		}
		case 's': {
			return jjMoveStringLiteralDfa26_0(active0, 0, active1, 112);
		}
		case 't': {
			if ((active0 & 0x80000000000L) != 0) {
				return jjStopAtPos(25, 43);
			}
			if ((active0 & 0x100000000000000L) == 0) {
				break;
			}
			return jjStopAtPos(25, 56);
		}
		case 'w': {
			return jjMoveStringLiteralDfa26_0(active0, 36591746972385280L, active1, 0);
		}
		case 'y': {
			if ((active0 & 0x1000000000L) == 0) {
				break;
			}
			return jjStopAtPos(25, 36);
		}
		}
		return jjStartNfa_0(24, active0, active1);
	}

	private int jjMoveStringLiteralDfa26_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(24, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(25, active0, active1);
			return 26;
		}
		switch (curChar) {
		case 'a': {
			return jjMoveStringLiteralDfa27_0(active0, 6442450944L, active1, 0);
		}
		case 'b': {
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 131072);
		}
		case 'n': {
			if ((active0 & 0x2000000000000L) != 0) {
				return jjStopAtPos(26, 49);
			}
			if ((active0 & 0x80000000000000L) != 0) {
				return jjStopAtPos(26, 55);
			}
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 114688);
		}
		case 'o': {
			if ((active0 & 0x10000000) != 0) {
				return jjStopAtPos(26, 28);
			}
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 4096);
		}
		case 'r': {
			return jjMoveStringLiteralDfa27_0(active0, 0x4040000000L, active1, 0);
		}
		case 's': {
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 256);
		}
		case 't': {
			if ((active0 & 0x200000000000000L) != 0) {
				return jjStopAtPos(26, 57);
			}
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 112);
		}
		case 'u': {
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 8);
		}
		case 'w': {
			return jjMoveStringLiteralDfa27_0(active0, 0, active1, 2052);
		}
		}
		return jjStartNfa_0(25, active0, active1);
	}

	private int jjMoveStringLiteralDfa27_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(25, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(26, active0, active1);
			return 27;
		}
		switch (curChar) {
		case 'E': {
			return jjMoveStringLiteralDfa28_0(active0, 0x40000000, active1, 0);
		}
		case 'd': {
			if ((active0 & 0x4000000000L) == 0) {
				break;
			}
			return jjStopAtPos(27, 38);
		}
		case 'e': {
			if ((active1 & 16384) != 0) {
				jjmatchedKind = 78;
				jjmatchedPos = 27;
			}
			return jjMoveStringLiteralDfa28_0(active0, 0, active1, 229376);
		}
		case 'k': {
			if ((active1 & 256) == 0) {
				break;
			}
			return jjStopAtPos(27, 72);
		}
		case 'n': {
			if ((active0 & 0x80000000L) != 0) {
				jjmatchedKind = 31;
				jjmatchedPos = 27;
			} else {
				if ((active1 & 4) != 0) {
					return jjStopAtPos(27, 66);
				}
				if ((active1 & 2048) != 0) {
					return jjStopAtPos(27, 75);
				}
			}
			return jjMoveStringLiteralDfa28_0(active0, 0x100000000L, active1, 8);
		}
		case 'o': {
			return jjMoveStringLiteralDfa28_0(active0, 0, active1, 112);
		}
		case 't': {
			if ((active1 & 4096) == 0) {
				break;
			}
			return jjStopAtPos(27, 76);
		}
		}
		return jjStartNfa_0(26, active0, active1);
	}

	private int jjMoveStringLiteralDfa28_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(26, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(27, active0, active1);
			return 28;
		}
		switch (curChar) {
		case 'D': {
			return jjMoveStringLiteralDfa29_0(active0, 0, active1, 65536);
		}
		case 'O': {
			return jjMoveStringLiteralDfa29_0(active0, 0x100000000L, active1, 0);
		}
		case 'U': {
			return jjMoveStringLiteralDfa29_0(active0, 0, active1, 32768);
		}
		case 'n': {
			return jjMoveStringLiteralDfa29_0(active0, 0, active1, 112);
		}
		case 'q': {
			return jjMoveStringLiteralDfa29_0(active0, 0x40000000, active1, 0);
		}
		case 'r': {
			if ((active1 & 131072) == 0) {
				break;
			}
			return jjStopAtPos(28, 81);
		}
		case 't': {
			if ((active1 & 8) == 0) {
				break;
			}
			return jjStopAtPos(28, 67);
		}
		}
		return jjStartNfa_0(27, active0, active1);
	}

	private int jjMoveStringLiteralDfa29_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(27, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(28, active0, active1);
			return 29;
		}
		switch (curChar) {
		case 'e': {
			if ((active1 & 16) != 0) {
				jjmatchedKind = 68;
				jjmatchedPos = 29;
			}
			return jjMoveStringLiteralDfa30_0(active0, 0, active1, 96);
		}
		case 'o': {
			return jjMoveStringLiteralDfa30_0(active0, 0, active1, 65536);
		}
		case 'p': {
			if ((active1 & 32768) == 0) {
				break;
			}
			return jjStopAtPos(29, 79);
		}
		case 'r': {
			return jjMoveStringLiteralDfa30_0(active0, 0x100000000L, active1, 0);
		}
		case 'u': {
			return jjMoveStringLiteralDfa30_0(active0, 0x40000000, active1, 0);
		}
		}
		return jjStartNfa_0(28, active0, active1);
	}

	private int jjMoveStringLiteralDfa3_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(1, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(2, active0, active1);
			return 3;
		}
		switch (curChar) {
		case 'p': {
			return jjMoveStringLiteralDfa4_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(2, active0, active1);
	}

	private int jjMoveStringLiteralDfa30_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(28, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(29, active0, active1);
			return 30;
		}
		switch (curChar) {
		case 'D': {
			return jjMoveStringLiteralDfa31_0(active0, 0, active1, 64);
		}
		case 'E': {
			return jjMoveStringLiteralDfa31_0(active0, 0x100000000L, active1, 0);
		}
		case 'U': {
			return jjMoveStringLiteralDfa31_0(active0, 0, active1, 32);
		}
		case 'a': {
			return jjMoveStringLiteralDfa31_0(active0, 0x40000000, active1, 0);
		}
		case 'w': {
			return jjMoveStringLiteralDfa31_0(active0, 0, active1, 65536);
		}
		}
		return jjStartNfa_0(29, active0, active1);
	}

	private int jjMoveStringLiteralDfa31_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(29, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(30, active0, active1);
			return 31;
		}
		switch (curChar) {
		case 'l': {
			return jjMoveStringLiteralDfa32_0(active0, 0x40000000, active1, 0);
		}
		case 'n': {
			if ((active1 & 65536) == 0) {
				break;
			}
			return jjStopAtPos(31, 80);
		}
		case 'o': {
			return jjMoveStringLiteralDfa32_0(active0, 0, active1, 64);
		}
		case 'p': {
			if ((active1 & 32) == 0) {
				break;
			}
			return jjStopAtPos(31, 69);
		}
		case 'q': {
			return jjMoveStringLiteralDfa32_0(active0, 0x100000000L, active1, 0);
		}
		}
		return jjStartNfa_0(30, active0, active1);
	}

	private int jjMoveStringLiteralDfa32_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(30, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(31, active0, active1);
			return 32;
		}
		switch (curChar) {
		case 'T': {
			return jjMoveStringLiteralDfa33_0(active0, 0x40000000, active1, 0);
		}
		case 'u': {
			return jjMoveStringLiteralDfa33_0(active0, 0x100000000L, active1, 0);
		}
		case 'w': {
			return jjMoveStringLiteralDfa33_0(active0, 0, active1, 64);
		}
		}
		return jjStartNfa_0(31, active0, active1);
	}

	private int jjMoveStringLiteralDfa33_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(31, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(32, active0, active1);
			return 33;
		}
		switch (curChar) {
		case 'a': {
			return jjMoveStringLiteralDfa34_0(active0, 0x100000000L, active1, 0);
		}
		case 'n': {
			if ((active1 & 64) == 0) {
				break;
			}
			return jjStopAtPos(33, 70);
		}
		case 'o': {
			if ((active0 & 0x40000000) == 0) {
				break;
			}
			return jjStopAtPos(33, 30);
		}
		}
		return jjStartNfa_0(32, active0, active1);
	}

	private int jjMoveStringLiteralDfa34_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(32, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(33, active0, 0);
			return 34;
		}
		switch (curChar) {
		case 'l': {
			return jjMoveStringLiteralDfa35_0(active0, 0x100000000L);
		}
		}
		return jjStartNfa_0(33, active0, 0);
	}

	private int jjMoveStringLiteralDfa35_0(long old0, long active0) {
		if ((active0 &= old0) == 0) {
			return jjStartNfa_0(33, old0, 0);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(34, active0, 0);
			return 35;
		}
		switch (curChar) {
		case 'T': {
			return jjMoveStringLiteralDfa36_0(active0, 0x100000000L);
		}
		}
		return jjStartNfa_0(34, active0, 0);
	}

	private int jjMoveStringLiteralDfa36_0(long old0, long active0) {
		if ((active0 &= old0) == 0) {
			return jjStartNfa_0(34, old0, 0);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(35, active0, 0);
			return 36;
		}
		switch (curChar) {
		case 'o': {
			if ((active0 & 0x100000000L) == 0) {
				break;
			}
			return jjStopAtPos(36, 32);
		}
		}
		return jjStartNfa_0(35, active0, 0);
	}

	private int jjMoveStringLiteralDfa4_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(2, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(3, active0, active1);
			return 4;
		}
		switch (curChar) {
		case 'u': {
			return jjMoveStringLiteralDfa5_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(3, active0, active1);
	}

	private int jjMoveStringLiteralDfa5_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(3, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(4, active0, active1);
			return 5;
		}
		switch (curChar) {
		case 't': {
			return jjMoveStringLiteralDfa6_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(4, active0, active1);
	}

	private int jjMoveStringLiteralDfa6_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(4, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(5, active0, active1);
			return 6;
		}
		switch (curChar) {
		case 'e': {
			return jjMoveStringLiteralDfa7_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(5, active0, active1);
	}

	private int jjMoveStringLiteralDfa7_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(5, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(6, active0, active1);
			return 7;
		}
		switch (curChar) {
		case 'r': {
			return jjMoveStringLiteralDfa8_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(6, active0, active1);
	}

	private int jjMoveStringLiteralDfa8_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(6, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(7, active0, active1);
			return 8;
		}
		switch (curChar) {
		case 'c': {
			return jjMoveStringLiteralDfa9_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(7, active0, active1);
	}

	private int jjMoveStringLiteralDfa9_0(long old0, long active0, long old1, long active1) {
		if (((active0 &= old0) | (active1 &= old1)) == 0) {
			return jjStartNfa_0(7, old0, old1);
		}
		try {
			curChar = input_stream.readChar();
		} catch (IOException e) {
			jjStopStringLiteralDfa_0(8, active0, active1);
			return 9;
		}
		switch (curChar) {
		case 'r': {
			return jjMoveStringLiteralDfa10_0(active0, -262208, active1, 1048575);
		}
		}
		return jjStartNfa_0(8, active0, active1);
	}

	private final int jjStartNfa_0(int pos, long active0, long active1) {
		return jjMoveNfa_0(jjStopStringLiteralDfa_0(pos, active0, active1), pos + 1);
	}

	private int jjStopAtPos(int pos, int kind) {
		jjmatchedKind = kind;
		jjmatchedPos = pos;
		return pos + 1;
	}

	private final int jjStopStringLiteralDfa_0(int pos, long active0, long active1) {
		switch (pos) {
		case 0: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				return 5;
			}
			return -1;
		}
		case 1: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 1;
				return 5;
			}
			return -1;
		}
		case 2: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 2;
				return 5;
			}
			return -1;
		}
		case 3: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 3;
				return 5;
			}
			return -1;
		}
		case 4: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 4;
				return 5;
			}
			return -1;
		}
		case 5: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 5;
				return 5;
			}
			return -1;
		}
		case 6: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 6;
				return 5;
			}
			return -1;
		}
		case 7: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 7;
				return 5;
			}
			return -1;
		}
		case 8: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 8;
				return 5;
			}
			return -1;
		}
		case 9: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 9;
				return 5;
			}
			return -1;
		}
		case 10: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 10;
				return 5;
			}
			return -1;
		}
		case 11: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 11;
				return 5;
			}
			return -1;
		}
		case 12: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 12;
				return 5;
			}
			return -1;
		}
		case 13: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 13;
				return 5;
			}
			return -1;
		}
		case 14: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 14;
				return 5;
			}
			return -1;
		}
		case 15: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				jjmatchedKind = 5;
				jjmatchedPos = 15;
				return 5;
			}
			return -1;
		}
		case 16: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 17: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 18: {
			if (((active0 & -262208) != 0) || ((active1 & 1048575) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 19: {
			if (((active0 & -67408000) != 0) || ((active1 & 1048575) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 20: {
			if (((active0 & -17729826632832L) != 0) || ((active1 & 786431) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 21: {
			if (((active0 & -2594091123816576768L) != 0) || ((active1 & 786431) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 22: {
			if (((active0 & -2595252259654057728L) != 0) || ((active1 & 786431) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 23: {
			if (((active0 & -7792548066652192768L) != 0) || ((active1 & 778111) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 24: {
			if (((active0 & -7794870235210186752L) != 0) || ((active1 & 253310) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 25: {
			if (((active0 & 257277276188573696L) != 0) || ((active1 & 253310) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 26: {
			if (((active0 & 180707217710776320L) != 0) || ((active1 & 252284) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 27: {
			if (((active0 & 282394099712L) != 0) || ((active1 & 252284) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 28: {
			if (((active0 & 5368709120L) != 0) || ((active1 & 229496) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 29: {
			if (((active0 & 5368709120L) != 0) || ((active1 & 98416) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 30: {
			if (((active0 & 5368709120L) != 0) || ((active1 & 65632) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 31: {
			if (((active0 & 5368709120L) != 0) || ((active1 & 65632) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 32: {
			if (((active0 & 5368709120L) != 0) || ((active1 & 64) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 33: {
			if (((active0 & 5368709120L) != 0) || ((active1 & 64) != 0)) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 34: {
			if ((active0 & 0x100000000L) != 0) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		case 35: {
			if ((active0 & 0x100000000L) != 0) {
				if (jjmatchedPos < 15) {
					jjmatchedKind = 5;
					jjmatchedPos = 15;
				}
				return -1;
			}
			return -1;
		}
		}
		return -1;
	}

	public void ReInit(SimpleCharStream stream) {
		jjnewStateCnt = 0;
		jjmatchedPos = 0;
		curLexState = defaultLexState;
		input_stream = stream;
		ReInitRounds();
	}

	public void ReInit(SimpleCharStream stream, int lexState) {
		this.ReInit(stream);
		SwitchTo(lexState);
	}

	private void ReInitRounds() {
		jjround = -2147483647;
		int i = 9;
		while (i-- > 0) {
			jjrounds[i] = Integer.MIN_VALUE;
		}
	}

	public void setDebugStream(PrintStream ds) {
		debugStream = ds;
	}

	public void SwitchTo(int lexState) {
		if ((lexState >= 1) || (lexState < 0)) {
			throw new TokenMgrError("Error: Ignoring invalid lexical state : " + lexState + ". State unchanged.", 2);
		}
		curLexState = lexState;
	}
}
