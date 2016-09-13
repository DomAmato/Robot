package com.dyn.robot.code.grammar;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.dyn.robot.code.IconCategory;
import com.dyn.robot.code.Range;
import com.dyn.robot.code.SyntaxError;
import com.dyn.robot.code.SyntaxSuggestion;
import com.dyn.robot.code.parsetree.AssignmentStatement;
import com.dyn.robot.code.parsetree.BinaryExpression;
import com.dyn.robot.code.parsetree.BinaryOperator;
import com.dyn.robot.code.parsetree.Block;
import com.dyn.robot.code.parsetree.BooleanConstant;
import com.dyn.robot.code.parsetree.BreakStatement;
import com.dyn.robot.code.parsetree.Comment;
import com.dyn.robot.code.parsetree.CommentStatement;
import com.dyn.robot.code.parsetree.CompiledProgram;
import com.dyn.robot.code.parsetree.Expression;
import com.dyn.robot.code.parsetree.ExpressionType;
import com.dyn.robot.code.parsetree.ForStatement;
import com.dyn.robot.code.parsetree.Function;
import com.dyn.robot.code.parsetree.FunctionCallExpression;
import com.dyn.robot.code.parsetree.FunctionCallStatement;
import com.dyn.robot.code.parsetree.IfStatement;
import com.dyn.robot.code.parsetree.InvalidExpression;
import com.dyn.robot.code.parsetree.InvalidStatement;
import com.dyn.robot.code.parsetree.InvalidVariable;
import com.dyn.robot.code.parsetree.Node;
import com.dyn.robot.code.parsetree.NumericConstant;
import com.dyn.robot.code.parsetree.PureFunction;
import com.dyn.robot.code.parsetree.RepeatStatement;
import com.dyn.robot.code.parsetree.Statement;
import com.dyn.robot.code.parsetree.StringConstant;
import com.dyn.robot.code.parsetree.UnaryExpression;
import com.dyn.robot.code.parsetree.UnaryOperator;
import com.dyn.robot.code.parsetree.Variable;
import com.dyn.robot.code.parsetree.VariableExpression;
import com.dyn.robot.code.parsetree.WhileStatement;

public class Grammar implements GrammarConstants {
	private static int[] jj_la1_0;
	private static int[] jj_la1_1;
	private static int[] jj_la1_2;
	static {
		Grammar.jj_la1_init_0();
		Grammar.jj_la1_init_1();
		Grammar.jj_la1_init_2();
	}

	public static CompiledProgram compile(String title, String code, List<SyntaxError> errors,
			List<SyntaxSuggestion> suggestions) throws TokenMgrError {
		try {
			Grammar parser = new Grammar(new ByteArrayInputStream(code.getBytes("UTF-8")), "UTF-8");
			try {
				parser.errors = errors;
				parser.suggestions = suggestions;
				return parser.Program(title);
			} catch (ParseException e) {
				errors.add(new SyntaxError(parser.eatLine(), "gui.dynrobot:syntax_error.generic"));
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException();
		}
	}

	private static void jj_la1_init_0() {
		jj_la1_0 = new int[] { 878656, 878656, 616512, 256, 512, 66584576, 0, 0, -268304384, 134217728, 67108864,
				3145728, 58720256, -268304384, 0, 0, 0, 8, 8, 0, 0, 0 };
	}

	private static void jj_la1_init_1() {
		jj_la1_1 = new int[] { -64, -64, -64, 0, 0, -28, 24, 6, 1, 0, 0, 0, 0, 1, 6, 24, 36, 0, 0, -64, -64, 0 };
	}

	private static void jj_la1_init_2() {
		jj_la1_2 = new int[] { 1048575, 1048575, 1048575, 0, 0, 524287, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 524287,
				8191, 516096 };
	}

	private static Range range(int t) {
		return new Range(t - 1, 1);
	}

	private static Range range(int t, int size) {
		return new Range(t - 1, size);
	}

	private static Range range(Node t) {
		return t.getRange();
	}

	private static Range range(Node t, Node u) {
		return new Range(Grammar.range(t), Grammar.range(u));
	}

	private static Range range(Range t, Range u) {
		return new Range(t, u);
	}

	private static Range range(Token t) {
		return new Range(t.beginLine - 1, 1);
	}

	private static Range range(Token t, Token u) {
		return new Range(Grammar.range(t), Grammar.range(u));
	}

	private int m_lastEatenLine = -1;

	private List<SyntaxError> errors;

	private List<SyntaxSuggestion> suggestions;

	public GrammarTokenManager token_source;

	SimpleCharStream jj_input_stream;

	public Token token;

	public Token jj_nt;

	private int jj_ntk;

	private int jj_gen;

	private final int[] jj_la1 = new int[22];

	private List<int[]> jj_expentries = new ArrayList<int[]>();

	private int[] jj_expentry;

	private int jj_kind = -1;

	public Grammar(GrammarTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 22; ++i) {
			jj_la1[i] = -1;
		}
	}

	public Grammar(InputStream stream) {
		this(stream, null);
	}

	public Grammar(InputStream stream, String encoding) {
		try {
			jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		token_source = new GrammarTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 22; ++i) {
			jj_la1[i] = -1;
		}
	}

	public Grammar(Reader stream) {
		jj_input_stream = new SimpleCharStream(stream, 1, 1);
		token_source = new GrammarTokenManager(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 22; ++i) {
			jj_la1[i] = -1;
		}
	}

	public final Expression AddExpression(ExpressionType expectedType) throws ParseException {
		BinaryOperator operator = null;
		Expression right = null;
		Expression left = MultiplyExpression(expectedType);
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 33:
		case 34: {
			operator = AddOperator();
			right = AddExpression(operator.getExpectedInputRight(left.getType()));
			break;
		}
		default: {
			jj_la1[7] = jj_gen;
		}
		}
		if ((operator != null) && (right != null)) {
			left = new BinaryExpression(Grammar.range(left, right), left, operator, right);
		}
		if ("" != null) {
			return left;
		}
		throw new Error("Missing return statement in function");
	}

	private int addLineWithIndent(int position, int indent) {
		int x = Math.min(indent, 11);
		int y = position >= 0 ? (position / 12) + 1 : 0;
		return x + (y * 12);
	}

	public final BinaryOperator AddOperator() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 33: {
			Token t = jj_consume_token(33);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "+", ExpressionType.Number, ExpressionType.Number);
		}
		case 34: {
			Token t = jj_consume_token(34);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "-", ExpressionType.Number, ExpressionType.Number);
		}
		default: {
			jj_la1[14] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final Expression AndExpression(ExpressionType expectedType) throws ParseException {
		BinaryOperator operator = null;
		Expression right = null;
		Expression left = CompareExpression(expectedType);
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 27: {
			operator = AndOperator();
			right = AndExpression(operator.getExpectedInputRight(left.getType()));
			break;
		}
		default: {
			jj_la1[9] = jj_gen;
		}
		}
		if ((operator != null) && (right != null)) {
			left = new BinaryExpression(Grammar.range(left, right), left, operator, right);
		}
		if ("" != null) {
			return left;
		}
		throw new Error("Missing return statement in function");
	}

	public final BinaryOperator AndOperator() throws ParseException {
		Token t = jj_consume_token(27);
		jj_consume_token(18);
		if ("" != null) {
			return new BinaryOperator(Grammar.range(t), "and", ExpressionType.Boolean, ExpressionType.Boolean);
		}
		throw new Error("Missing return statement in function");
	}

	public final Function ArgumentFunction() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 77: {
			Token t = jj_consume_token(77);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.select( %arg% )", ExpressionType.Nil, ExpressionType.Number,
					false);
		}
		case 78: {
			Token t = jj_consume_token(78);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtleedu.setRedstone( %arg% )", ExpressionType.Nil,
					ExpressionType.Boolean, false);
		}
		case 79: {
			Token t = jj_consume_token(79);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtleedu.setRedstoneUp( %arg% )", ExpressionType.Nil,
					ExpressionType.Boolean, false);
		}
		case 80: {
			Token t = jj_consume_token(80);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtleedu.setRedstoneDown( %arg% )", ExpressionType.Nil,
					ExpressionType.Boolean, false);
		}
		case 81: {
			Token t = jj_consume_token(81);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "math.random( %arg% )", ExpressionType.Number,
					ExpressionType.Number, false);
		}
		case 82: {
			Token t = jj_consume_token(82);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtleedu.say( %arg% )", ExpressionType.Nil, ExpressionType.Unknown,
					false);
		}
		default: {
			jj_la1[21] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final AssignmentStatement AssignmentStatement() throws ParseException {
		Range assignRange;
		Variable variable = Variable();
		try {
			Token assign = jj_consume_token(17);
			jj_consume_token(18);
			assignRange = Grammar.range(assign);
		} catch (ParseException e) {
			assignRange = this.peekLine();
			this.expect(IconCategory.Assign, new IconCategory[0]);
		}
		Expression expression = Expression(ExpressionType.Unknown);
		if ("" != null) {
			return new AssignmentStatement(Grammar.range(variable, expression), assignRange.getStartLine(), variable,
					expression);
		}
		throw new Error("Missing return statement in function");
	}

	public final /* varargs */ Block Block(int parentIndent, boolean allowBreak, IconCategory... endCategories)
			throws ParseException {
		ArrayList<Statement> statements;
		Range range;
		block9: {
			statements = new ArrayList<Statement>();
			range = this.peekLine(0);
			block7: do {
				switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
				case 6:
				case 11:
				case 13:
				case 14:
				case 16:
				case 18:
				case 19:
				case 38:
				case 39:
				case 40:
				case 41:
				case 42:
				case 43:
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
				case 49:
				case 50:
				case 51:
				case 52:
				case 53:
				case 54:
				case 55:
				case 56:
				case 57:
				case 58:
				case 59:
				case 60:
				case 61:
				case 62:
				case 63:
				case 64:
				case 65:
				case 66:
				case 67:
				case 68:
				case 69:
				case 70:
				case 71:
				case 72:
				case 73:
				case 74:
				case 75:
				case 76:
				case 77:
				case 78:
				case 79:
				case 80:
				case 81:
				case 82:
				case 83: {
					break;
				}
				default: {
					jj_la1[0] = jj_gen;
					break block9;
				}
				}
				switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
				case 6:
				case 11:
				case 13:
				case 14:
				case 16:
				case 19:
				case 38:
				case 39:
				case 40:
				case 41:
				case 42:
				case 43:
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
				case 49:
				case 50:
				case 51:
				case 52:
				case 53:
				case 54:
				case 55:
				case 56:
				case 57:
				case 58:
				case 59:
				case 60:
				case 61:
				case 62:
				case 63:
				case 64:
				case 65:
				case 66:
				case 67:
				case 68:
				case 69:
				case 70:
				case 71:
				case 72:
				case 73:
				case 74:
				case 75:
				case 76:
				case 77:
				case 78:
				case 79:
				case 80:
				case 81:
				case 82:
				case 83: {
					Statement statement = MaybeStatement(allowBreak);
					statements.add(statement);
					range.expandToFit(statement.getRange());
					continue block7;
				}
				case 18: {
					jj_consume_token(18);
					continue block7;
				}
				}
				break;
			} while (true);
			jj_la1[1] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		if ("" != null) {
			return new Block(range, statements, parentIndent, allowBreak, endCategories);
		}
		throw new Error("Missing return statement in function");
	}

	public final BooleanConstant BooleanConstant() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 20: {
			Token t = jj_consume_token(20);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BooleanConstant(Grammar.range(t), true);
		}
		case 21: {
			Token t = jj_consume_token(21);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BooleanConstant(Grammar.range(t), false);
		}
		default: {
			jj_la1[11] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final BreakStatement BreakStatement() throws ParseException {
		Token t = jj_consume_token(16);
		jj_consume_token(18);
		if ("" != null) {
			return new BreakStatement(Grammar.range(t));
		}
		throw new Error("Missing return statement in function");
	}

	public final Comment Comment() throws ParseException {
		Token t = jj_consume_token(83);
		Token u = jj_consume_token(4);
		jj_consume_token(18);
		String comment = u.image.substring(1, u.image.length() - 1);
		if ("" != null) {
			return new Comment(Grammar.range(t, u), comment);
		}
		throw new Error("Missing return statement in function");
	}

	public final CommentStatement CommentStatement() throws ParseException {
		Comment comment = Comment();
		if ("" != null) {
			return new CommentStatement(Grammar.range(comment), comment);
		}
		throw new Error("Missing return statement in function");
	}

	public final Expression CompareExpression(ExpressionType expectedType) throws ParseException {
		BinaryOperator operator = null;
		Expression right = null;
		Expression left = AddExpression(expectedType);
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 17:
		case 28:
		case 29:
		case 30:
		case 31:
		case 32: {
			operator = CompareOperator();
			right = CompareExpression(operator.getExpectedInputRight(left.getType()));
			break;
		}
		default: {
			jj_la1[8] = jj_gen;
		}
		}
		if ((operator != null) && (right != null)) {
			left = new BinaryExpression(Grammar.range(left, right), left, operator, right);
		}
		if ("" != null) {
			return left;
		}
		throw new Error("Missing return statement in function");
	}

	public final BinaryOperator CompareOperator() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 17: {
			Token t = jj_consume_token(17);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "==", ExpressionType.Unknown, ExpressionType.Boolean);
		}
		case 28: {
			Token t = jj_consume_token(28);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "~=", ExpressionType.Unknown, ExpressionType.Boolean);
		}
		case 29: {
			Token t = jj_consume_token(29);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "<", ExpressionType.Number, ExpressionType.Boolean);
		}
		case 30: {
			Token t = jj_consume_token(30);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "<=", ExpressionType.Number, ExpressionType.Boolean);
		}
		case 31: {
			Token t = jj_consume_token(31);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), ">", ExpressionType.Number, ExpressionType.Boolean);
		}
		case 32: {
			Token t = jj_consume_token(32);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), ">=", ExpressionType.Number, ExpressionType.Boolean);
		}
		default: {
			jj_la1[13] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final void disable_tracing() {
	}

	public Range eatLine() {
		int lastLine = Math.max(token.beginLine, m_lastEatenLine);
		int nextLine = lastLine + 1;
		Token nextToken = peekToken();
		while ((nextToken.kind != 0) && (nextToken.beginLine <= nextLine)) {
			eatToken();
			nextToken = peekToken();
		}
		m_lastEatenLine = nextLine;
		return Grammar.range(nextLine);
	}

	private Token eatToken() {
		if (token.kind == 0) {
			return token;
		}
		if (token.next == null) {
			token.next = token_source.getNextToken();
		}
		token = token.next;
		return token;
	}

	public final void enable_tracing() {
	}

	public /* varargs */ void expect(IconCategory expectedCategory, IconCategory... moreExpectedCategories) {
		this.expect(eatLine(), expectedCategory, moreExpectedCategories);
	}

	public /* varargs */ void expect(Range range, IconCategory expectedCategory,
			IconCategory... moreExpectedCategories) {
		errors.add(
				new SyntaxError(range, "gui.dynrobot:syntax_error.expected", expectedCategory, moreExpectedCategories));
		suggestions.add(new SyntaxSuggestion(range, true, expectedCategory, moreExpectedCategories));
	}

	public /* varargs */ Range expectBlockEnd(Block block, IconCategory firstValidEndCategory,
			IconCategory... moreValidEndCategories) {
		Range endRange;
		Range blockRange = block.getRange();
		int limit = peekToken().kind == 0 ? 264 : this.peekLine().getStartLine();
		int nextSpace = blockRange.getEndLine();
		int nextLine = addLineWithIndent(blockRange.getEndLine() - 1, block.getParentIndent());
		if ((nextLine + 12) < limit) {
			endRange = new Range(nextLine + 12, 1);
			blockRange.expandToTouch(endRange);
		} else if (nextLine < limit) {
			endRange = new Range(nextLine, 1);
			blockRange.expandToTouch(endRange);
		} else {
			endRange = nextSpace < limit ? new Range(nextSpace, 1) : eatLine();
		}
		this.expect(endRange, firstValidEndCategory, moreValidEndCategories);
		return endRange;
	}

	public final Expression Expression(ExpressionType expectedType) throws ParseException {
		Expression left = OrExpression(expectedType);
		if ("" != null) {
			return left;
		}
		throw new Error("Missing return statement in function");
	}

	public final ForStatement ForStatement() throws ParseException {
		Range assignRange;
		Range endRange;
		Variable variable;
		Token forToken = jj_consume_token(13);
		jj_consume_token(18);
		Range forRange = Grammar.range(forToken);
		try {
			variable = Variable();
		} catch (ParseException e) {
			variable = new InvalidVariable(this.peekLine());
			this.expect(IconCategory.Variable, new IconCategory[0]);
		}
		try {
			Token assignToken = jj_consume_token(17);
			jj_consume_token(18);
			assignRange = Grammar.range(assignToken);
		} catch (ParseException e) {
			assignRange = this.peekLine();
			this.expect(IconCategory.Assign, new IconCategory[0]);
		}
		Expression start = Expression(ExpressionType.Number);
		try {
			jj_consume_token(15);
			jj_consume_token(18);
		} catch (ParseException e) {
			this.expect(IconCategory.getOperatorFromType(start.getType()), IconCategory.To);
		}
		Expression finish = Expression(ExpressionType.Number);
		try {
			jj_consume_token(12);
			jj_consume_token(18);
		} catch (ParseException e) {
			this.expect(IconCategory.getOperatorFromType(finish.getType()), IconCategory.Do);
		}
		Block block = Block(forRange.getStartLine() % 12, true, IconCategory.End);
		try {
			Token endToken = jj_consume_token(10);
			jj_consume_token(18);
			endRange = Grammar.range(endToken);
			block.getRange().expandToTouch(endRange);
		} catch (ParseException e) {
			endRange = expectBlockEnd(block, IconCategory.End, new IconCategory[0]);
		}
		if ("" != null) {
			return new ForStatement(Grammar.range(forRange, endRange), assignRange.getStartLine(), variable, start,
					finish, block);
		}
		throw new Error("Missing return statement in function");
	}

	public final Function Function() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 38: {
			Token t = jj_consume_token(38);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.forward()", ExpressionType.Boolean, true);
		}
		case 39: {
			Token t = jj_consume_token(39);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.back()", ExpressionType.Boolean, true);
		}
		case 40: {
			Token t = jj_consume_token(40);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.up()", ExpressionType.Boolean, true);
		}
		case 41: {
			Token t = jj_consume_token(41);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.down()", ExpressionType.Boolean, true);
		}
		case 42: {
			Token t = jj_consume_token(42);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.turnLeft()", ExpressionType.Nil, false);
		}
		case 43: {
			Token t = jj_consume_token(43);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.turnRight()", ExpressionType.Nil, false);
		}
		case 44: {
			Token t = jj_consume_token(44);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.dig()", ExpressionType.Boolean, true);
		}
		case 45: {
			Token t = jj_consume_token(45);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.digUp()", ExpressionType.Boolean, true);
		}
		case 46: {
			Token t = jj_consume_token(46);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.digDown()", ExpressionType.Boolean, true);
		}
		case 47: {
			Token t = jj_consume_token(47);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.detect()", ExpressionType.Boolean, true);
		}
		case 48: {
			Token t = jj_consume_token(48);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.detectUp()", ExpressionType.Boolean, true);
		}
		case 49: {
			Token t = jj_consume_token(49);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.detectDown()", ExpressionType.Boolean, true);
		}
		case 50: {
			Token t = jj_consume_token(50);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.place()", ExpressionType.Boolean, true);
		}
		case 51: {
			Token t = jj_consume_token(51);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.placeUp()", ExpressionType.Boolean, true);
		}
		case 52: {
			Token t = jj_consume_token(52);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.placeDown()", ExpressionType.Boolean, true);
		}
		case 53: {
			Token t = jj_consume_token(53);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.attack()", ExpressionType.Boolean, false);
		}
		case 54: {
			Token t = jj_consume_token(54);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.attackUp()", ExpressionType.Boolean, false);
		}
		case 55: {
			Token t = jj_consume_token(55);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.attackDown()", ExpressionType.Boolean, false);
		}
		case 56: {
			Token t = jj_consume_token(56);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.equipLeft()", ExpressionType.Boolean, true);
		}
		case 57: {
			Token t = jj_consume_token(57);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.equipRight()", ExpressionType.Boolean, true);
		}
		case 58: {
			Token t = jj_consume_token(58);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.drop()", ExpressionType.Boolean, true);
		}
		case 59: {
			Token t = jj_consume_token(59);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.dropUp()", ExpressionType.Boolean, true);
		}
		case 60: {
			Token t = jj_consume_token(60);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.dropDown()", ExpressionType.Boolean, true);
		}
		case 61: {
			Token t = jj_consume_token(61);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.suck()", ExpressionType.Boolean, true);
		}
		case 62: {
			Token t = jj_consume_token(62);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.suckUp()", ExpressionType.Boolean, true);
		}
		case 63: {
			Token t = jj_consume_token(63);
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t), "turtle.suckDown()", ExpressionType.Boolean, true);
		}
		case 64: {
			Token t = jj_consume_token(64);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.compare()", ExpressionType.Boolean, true);
		}
		case 65: {
			Token t = jj_consume_token(65);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.compareUp()", ExpressionType.Boolean, true);
		}
		case 66: {
			Token t = jj_consume_token(66);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.compareDown()", ExpressionType.Boolean, true);
		}
		case 67: {
			Token t = jj_consume_token(67);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtle.getItemCount()", ExpressionType.Number, true);
		}
		case 68: {
			Token t = jj_consume_token(68);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.checkRedstone()", ExpressionType.Boolean, true);
		}
		case 69: {
			Token t = jj_consume_token(69);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.checkRedstoneUp()", ExpressionType.Boolean, true);
		}
		case 70: {
			Token t = jj_consume_token(70);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.checkRedstoneDown()", ExpressionType.Boolean, true);
		}
		case 71: {
			Token t = jj_consume_token(71);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "math.random( 2 ) > 1", ExpressionType.Boolean, true);
		}
		case 72: {
			Token t = jj_consume_token(72);
			Token u = jj_consume_token(5);
			String title = u.image;
			if ("" == null) {
				break;
			}
			return new Function(Grammar.range(t, u), title + "()", ExpressionType.Nil, false);
		}
		case 73: {
			Token t = jj_consume_token(73);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.inspect()", ExpressionType.String, false);
		}
		case 74: {
			Token t = jj_consume_token(74);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.inspectUp()", ExpressionType.String, false);
		}
		case 75: {
			Token t = jj_consume_token(75);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.inspectDown()", ExpressionType.String, false);
		}
		case 76: {
			Token t = jj_consume_token(76);
			if ("" == null) {
				break;
			}
			return new PureFunction(Grammar.range(t), "turtleedu.getItemName()", ExpressionType.String, false);
		}
		default: {
			jj_la1[20] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final FunctionCallExpression FunctionCallExpression() throws ParseException {
		Token countToken = null;
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 38:
		case 39:
		case 40:
		case 41:
		case 42:
		case 43:
		case 44:
		case 45:
		case 46:
		case 47:
		case 48:
		case 49:
		case 50:
		case 51:
		case 52:
		case 53:
		case 54:
		case 55:
		case 56:
		case 57:
		case 58:
		case 59:
		case 60:
		case 61:
		case 62:
		case 63:
		case 64:
		case 65:
		case 66:
		case 67:
		case 68:
		case 69:
		case 70:
		case 71:
		case 72:
		case 73:
		case 74:
		case 75:
		case 76: {
			int count;
			Function function = Function();
			switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
			case 3: {
				countToken = jj_consume_token(3);
				break;
			}
			default: {
				jj_la1[17] = jj_gen;
			}
			}
			jj_consume_token(18);
			count = countToken != null ? Integer.parseInt(countToken.image) : 1;
			if ("" == null) {
				break;
			}
			return new FunctionCallExpression(Grammar.range(function), function, count);
		}
		case 77:
		case 78:
		case 79:
		case 80:
		case 81:
		case 82: {
			int count;
			Function function = ArgumentFunction();
			switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
			case 3: {
				countToken = jj_consume_token(3);
				break;
			}
			default: {
				jj_la1[18] = jj_gen;
			}
			}
			jj_consume_token(18);
			Expression argument = Expression(function.getArgumentType());
			count = countToken != null ? Integer.parseInt(countToken.image) : 1;
			if ("" == null) {
				break;
			}
			return new FunctionCallExpression(Grammar.range(function, argument), function, argument, count);
		}
		default: {
			jj_la1[19] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final FunctionCallStatement FunctionCallStatement() throws ParseException {
		FunctionCallExpression function = FunctionCallExpression();
		if ("" != null) {
			return new FunctionCallStatement(Grammar.range(function), function);
		}
		throw new Error("Missing return statement in function");
	}

	public ParseException generateParseException() {
		int i;
		jj_expentries.clear();
		boolean[] la1tokens = new boolean[84];
		if (jj_kind >= 0) {
			la1tokens[jj_kind] = true;
			jj_kind = -1;
		}
		for (i = 0; i < 22; ++i) {
			if (jj_la1[i] != jj_gen) {
				continue;
			}
			for (int j = 0; j < 32; ++j) {
				if ((jj_la1_0[i] & (1 << j)) != 0) {
					la1tokens[j] = true;
				}
				if ((jj_la1_1[i] & (1 << j)) != 0) {
					la1tokens[32 + j] = true;
				}
				if ((jj_la1_2[i] & (1 << j)) == 0) {
					continue;
				}
				la1tokens[64 + j] = true;
			}
		}
		for (i = 0; i < 84; ++i) {
			if (!la1tokens[i]) {
				continue;
			}
			jj_expentry = new int[1];
			jj_expentry[0] = i;
			jj_expentries.add(jj_expentry);
		}
		int[][] exptokseq = new int[jj_expentries.size()][];
		for (int i2 = 0; i2 < jj_expentries.size(); ++i2) {
			exptokseq[i2] = jj_expentries.get(i2);
		}
		return new ParseException(token, exptokseq, tokenImage);
	}

	public final Token getNextToken() {
		token = token.next != null ? token.next : (token.next = token_source.getNextToken());
		jj_ntk = -1;
		++jj_gen;
		return token;
	}

	public final Token getToken(int index) {
		Token t = token;
		for (int i = 0; i < index; ++i) {
			t = t.next != null ? t.next : (t.next = token_source.getNextToken());
		}
		return t;
	}

	public final IfStatement IfStatement(boolean allowBreak) throws ParseException {
		Range endRange;
		Block block;
		ArrayList<IfStatement.ElseIf> elseIfs = new ArrayList<IfStatement.ElseIf>();
		Block elseBlock = null;
		Block lastBlock = null;
		Token ifToken = jj_consume_token(6);
		jj_consume_token(18);
		Range ifRange = Grammar.range(ifToken);
		Expression condition = Expression(ExpressionType.Boolean);
		try {
			jj_consume_token(7);
			jj_consume_token(18);
		} catch (ParseException e) {
			this.expect(IconCategory.getOperatorFromType(condition.getType()), IconCategory.Then);
		}
		lastBlock = block = Block(ifRange.getStartLine() % 12, allowBreak, IconCategory.ElseIf, IconCategory.Else,
				IconCategory.End);
		block12: do {
			switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
			case 8: {
				break;
			}
			default: {
				jj_la1[3] = jj_gen;
				break block12;
			}
			}
			Token elseIfToken = jj_consume_token(8);
			jj_consume_token(18);
			Expression elseIfCondition = Expression(ExpressionType.Boolean);
			try {
				jj_consume_token(7);
				jj_consume_token(18);
			} catch (ParseException e) {
				this.expect(IconCategory.getOperatorFromType(elseIfCondition.getType()), IconCategory.Then);
			}
			Block elseIfBlock = Block(ifRange.getStartLine() % 12, allowBreak, IconCategory.ElseIf, IconCategory.Else,
					IconCategory.End);
			lastBlock.getRange().expandToTouch(Grammar.range(elseIfToken));
			lastBlock = elseIfBlock;
			elseIfs.add(new IfStatement.ElseIf(elseIfCondition, elseIfBlock));
		} while (true);
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 9: {
			Token elseToken = jj_consume_token(9);
			jj_consume_token(18);
			elseBlock = Block(ifRange.getStartLine() % 12, allowBreak, IconCategory.End);
			lastBlock.getRange().expandToTouch(Grammar.range(elseToken));
			lastBlock = elseBlock;
			break;
		}
		default: {
			jj_la1[4] = jj_gen;
		}
		}
		try {
			Token endToken = jj_consume_token(10);
			jj_consume_token(18);
			endRange = Grammar.range(endToken);
			lastBlock.getRange().expandToTouch(endRange);
		} catch (ParseException e) {
			endRange = elseBlock != null ? expectBlockEnd(lastBlock, IconCategory.End, new IconCategory[0])
					: expectBlockEnd(lastBlock, IconCategory.ElseIf, IconCategory.Else, IconCategory.End);
		}
		if ("" != null) {
			return new IfStatement(Grammar.range(ifRange, endRange), condition, block, elseIfs, elseBlock);
		}
		throw new Error("Missing return statement in function");
	}

	private Token jj_consume_token(int kind) throws ParseException {
		Token oldToken = token;
		token = oldToken.next != null ? token.next : (token.next = token_source.getNextToken());
		jj_ntk = -1;
		if (token.kind == kind) {
			++jj_gen;
			return token;
		}
		token = oldToken;
		jj_kind = kind;
		throw generateParseException();
	}

	private int jj_ntk_f() {
		jj_nt = token.next;
		if (jj_nt == null) {
			token.next = token_source.getNextToken();
			jj_ntk = token.next.kind;
			return jj_ntk;
		}
		jj_ntk = jj_nt.kind;
		return jj_ntk;
	}

	public final Statement MaybeStatement(boolean allowBreak) throws ParseException {
		block3: {
			Range firstLine = this.peekLine();
			try {
				Statement result = Statement(allowBreak);
				if ("" != null) {
					return result;
				}
			} catch (ParseException e) {
				Range badLine = eatLine();
				if ("" == null) {
					break block3;
				}
				return new InvalidStatement(Grammar.range(firstLine, badLine));
			}
		}
		throw new Error("Missing return statement in function");
	}

	public final Expression MultiplyExpression(ExpressionType expectedType) throws ParseException {
		BinaryOperator operator = null;
		Expression right = null;
		Expression left = SimpleExpression(expectedType);
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 35:
		case 36: {
			operator = MultiplyOperator();
			right = MultiplyExpression(operator.getExpectedInputRight(left.getType()));
			break;
		}
		default: {
			jj_la1[6] = jj_gen;
		}
		}
		if ((operator != null) && (right != null)) {
			left = new BinaryExpression(Grammar.range(left, right), left, operator, right);
		}
		if ("" != null) {
			return left;
		}
		throw new Error("Missing return statement in function");
	}

	public final BinaryOperator MultiplyOperator() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 35: {
			Token t = jj_consume_token(35);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "*", ExpressionType.Number, ExpressionType.Number);
		}
		case 36: {
			Token t = jj_consume_token(36);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new BinaryOperator(Grammar.range(t), "/", ExpressionType.Number, ExpressionType.Number);
		}
		default: {
			jj_la1[15] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final NumericConstant NumericConstant() throws ParseException {
		Token t = jj_consume_token(22);
		Token u = jj_consume_token(3);
		jj_consume_token(18);
		if ("" != null) {
			return new NumericConstant(Grammar.range(t, u), Integer.parseInt(u.image));
		}
		throw new Error("Missing return statement in function");
	}

	public final Expression OrExpression(ExpressionType expectedType) throws ParseException {
		BinaryOperator operator = null;
		Expression right = null;
		Expression left = AndExpression(expectedType);
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 26: {
			operator = OrOperator();
			right = OrExpression(operator.getExpectedInputRight(left.getType()));
			break;
		}
		default: {
			jj_la1[10] = jj_gen;
		}
		}
		if ((operator != null) && (right != null)) {
			left = new BinaryExpression(Grammar.range(left, right), left, operator, right);
		}
		if ("" != null) {
			return left;
		}
		throw new Error("Missing return statement in function");
	}

	public final BinaryOperator OrOperator() throws ParseException {
		Token t = jj_consume_token(26);
		jj_consume_token(18);
		if ("" != null) {
			return new BinaryOperator(Grammar.range(t), "or", ExpressionType.Boolean, ExpressionType.Boolean);
		}
		throw new Error("Missing return statement in function");
	}

	private Range peekLine() {
		return this.peekLine(1);
	}

	private Range peekLine(int size) {
		int lastLine = Math.max(token.beginLine, m_lastEatenLine);
		int nextLine = lastLine + 1;
		return Grammar.range(nextLine, size);
	}

	private Token peekToken() {
		if (token.kind == 0) {
			return token;
		}
		if (token.next == null) {
			token.next = token_source.getNextToken();
		}
		return token.next;
	}

	public final CompiledProgram Program(String title) throws ParseException {
		Block block = Block(-1, false, new IconCategory[0]);
		try {
			jj_consume_token(0);
		} catch (ParseException e) {
			Token token = null;
			Range lastTokenRange = null;
			do {
				token = getNextToken();
				if (token.kind == 0) {
					break;
				}
				Range tokenRange = Grammar.range(token);
				if (!tokenRange.equals(lastTokenRange) && (token.kind != 18)) {
					block.getRange().expandToFit(tokenRange);
					errors.add(
							new SyntaxError(tokenRange, "gui.dynrobot:syntax_error.expected", IconCategory.Statement));
				}
				lastTokenRange = tokenRange;
			} while (true);
		}
		block.getRange().expandToFit(new Range(0, 264));
		if ("" != null) {
			return new CompiledProgram(title, block);
		}
		throw new Error("Missing return statement in function");
	}

	public void ReInit(GrammarTokenManager tm) {
		token_source = tm;
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 22; ++i) {
			jj_la1[i] = -1;
		}
	}

	public void ReInit(InputStream stream) {
		this.ReInit(stream, null);
	}

	public void ReInit(InputStream stream, String encoding) {
		try {
			jj_input_stream.ReInit(stream, encoding, 1, 1);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 22; ++i) {
			jj_la1[i] = -1;
		}
	}

	public void ReInit(Reader stream) {
		jj_input_stream.ReInit(stream, 1, 1);
		token_source.ReInit(jj_input_stream);
		token = new Token();
		jj_ntk = -1;
		jj_gen = 0;
		for (int i = 0; i < 22; ++i) {
			jj_la1[i] = -1;
		}
	}

	public final RepeatStatement RepeatStatement() throws ParseException {
		Range endRange;
		Token repeatToken = jj_consume_token(14);
		jj_consume_token(18);
		Range repeatRange = Grammar.range(repeatToken);
		Expression count = Expression(ExpressionType.Number);
		try {
			jj_consume_token(12);
			jj_consume_token(18);
		} catch (ParseException e) {
			this.expect(IconCategory.getOperatorFromType(count.getType()), IconCategory.Do);
		}
		Block block = Block(repeatRange.getStartLine() % 12, true, IconCategory.End);
		try {
			Token endToken = jj_consume_token(10);
			jj_consume_token(18);
			endRange = Grammar.range(endToken);
			block.getRange().expandToTouch(endRange);
		} catch (ParseException e) {
			endRange = expectBlockEnd(block, IconCategory.End, new IconCategory[0]);
		}
		if ("" != null) {
			return new RepeatStatement(Grammar.range(repeatRange, endRange), count, block);
		}
		throw new Error("Missing return statement in function");
	}

	public final Expression SimpleExpression(ExpressionType expectedType) throws ParseException {
		block11: {
			Range firstLine = this.peekLine();
			try {
				Expression result;
				switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {

				case 19: {
					result = VariableExpression();
					break;
				}
				case 20:
				case 21: {
					result = BooleanConstant();
					break;
				}
				case 22: {
					result = NumericConstant();
					break;
				}
				case 23:
				case 24:
				case 25: {
					result = StringConstant();
					break;
				}
				case 38:
				case 39:
				case 40:
				case 41:
				case 42:
				case 43:
				case 44:
				case 45:
				case 46:
				case 47:
				case 48:
				case 49:
				case 50:
				case 51:
				case 52:
				case 53:
				case 54:
				case 55:
				case 56:
				case 57:
				case 58:
				case 59:
				case 60:
				case 61:
				case 62:
				case 63:
				case 64:
				case 65:
				case 66:
				case 67:
				case 68:
				case 69:
				case 70:
				case 71:
				case 72:
				case 73:
				case 74:
				case 75:
				case 76:
				case 77:
				case 78:
				case 79:
				case 80:
				case 81:
				case 82: {
					result = FunctionCallExpression();
					break;
				}
				case 34:
				case 37: {
					result = UnaryExpression();
					break;
				}
				default: {
					jj_la1[5] = jj_gen;
					jj_consume_token(-1);
					throw new ParseException();
				}
				}
				if ("" != null) {
					return result;
				}
			} catch (ParseException e) {
				Range badLine = eatLine();
				if ("" == null) {
					break block11;
				}
				return new InvalidExpression(Grammar.range(firstLine, badLine), expectedType);
			}
		}
		throw new Error("Missing return statement in function");
	}

	public final Statement Statement(boolean allowBreak) throws ParseException {
		Statement result;
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 6: {
			result = IfStatement(allowBreak);
			break;
		}
		case 11: {
			result = WhileStatement();
			break;
		}
		case 13: {
			result = ForStatement();
			break;
		}
		case 14: {
			result = RepeatStatement();
			break;
		}
		case 38:
		case 39:
		case 40:
		case 41:
		case 42:
		case 43:
		case 44:
		case 45:
		case 46:
		case 47:
		case 48:
		case 49:
		case 50:
		case 51:
		case 52:
		case 53:
		case 54:
		case 55:
		case 56:
		case 57:
		case 58:
		case 59:
		case 60:
		case 61:
		case 62:
		case 63:
		case 64:
		case 65:
		case 66:
		case 67:
		case 68:
		case 69:
		case 70:
		case 71:
		case 72:
		case 73:
		case 74:
		case 75:
		case 76:
		case 77:
		case 78:
		case 79:
		case 80:
		case 81:
		case 82: {
			result = FunctionCallStatement();
			break;
		}
		case 19: {
			result = AssignmentStatement();
			break;
		}
		case 83: {
			result = CommentStatement();
			break;
		}
		case 16: {
			result = BreakStatement();
			break;
		}
		default: {
			jj_la1[2] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		if ("" != null) {
			return result;
		}
		throw new Error("Missing return statement in function");
	}

	public final StringConstant StringConstant() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 23: {
			Token t = jj_consume_token(23);
			Token u = jj_consume_token(4);
			jj_consume_token(18);
			String string = u.image.substring(1, u.image.length() - 1);
			if ("" == null) {
				break;
			}
			return new StringConstant(Grammar.range(t, u), string);
		}
		case 24: {
			Token t = jj_consume_token(24);
			Token u = jj_consume_token(4);
			jj_consume_token(18);
			String blockName = u.image.substring(1, u.image.length() - 1);
			if ("" == null) {
				break;
			}
			return new StringConstant(Grammar.range(t, u), blockName);
		}
		case 25: {
			Token t = jj_consume_token(25);
			Token u = jj_consume_token(4);
			jj_consume_token(18);
			String itemName = u.image.substring(1, u.image.length() - 1);
			if ("" == null) {
				break;
			}
			return new StringConstant(Grammar.range(t, u), itemName);
		}
		default: {
			jj_la1[12] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final UnaryExpression UnaryExpression() throws ParseException {
		UnaryOperator operator = UnaryOperator();
		Expression expression = Expression(operator.getExpectedInput());
		if ("" != null) {
			return new UnaryExpression(Grammar.range(operator, expression), operator, expression);
		}
		throw new Error("Missing return statement in function");
	}

	public final UnaryOperator UnaryOperator() throws ParseException {
		switch (jj_ntk == -1 ? jj_ntk_f() : jj_ntk) {
		case 37: {
			Token t = jj_consume_token(37);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new UnaryOperator(Grammar.range(t), "not", ExpressionType.Boolean, ExpressionType.Boolean);
		}
		case 34: {
			Token t = jj_consume_token(34);
			jj_consume_token(18);
			if ("" == null) {
				break;
			}
			return new UnaryOperator(Grammar.range(t), "-", ExpressionType.Number, ExpressionType.Number);
		}
		default: {
			jj_la1[16] = jj_gen;
			jj_consume_token(-1);
			throw new ParseException();
		}
		}
		throw new Error("Missing return statement in function");
	}

	public final Variable Variable() throws ParseException {
		Token t = jj_consume_token(19);
		Token u = jj_consume_token(5);
		jj_consume_token(18);
		String name = u.image;
		if ("" != null) {
			return new Variable(Grammar.range(t, u), name);
		}
		throw new Error("Missing return statement in function");
	}

	public final VariableExpression VariableExpression() throws ParseException {
		Variable variable = Variable();
		if ("" != null) {
			return new VariableExpression(Grammar.range(variable), variable);
		}
		throw new Error("Missing return statement in function");
	}

	public final WhileStatement WhileStatement() throws ParseException {
		Range endRange;
		Token whileToken = jj_consume_token(11);
		jj_consume_token(18);
		Range whileRange = Grammar.range(whileToken);
		Expression condition = Expression(ExpressionType.Boolean);
		try {
			jj_consume_token(12);
			jj_consume_token(18);
		} catch (ParseException e) {
			this.expect(IconCategory.getOperatorFromType(condition.getType()), IconCategory.Do);
		}
		Block block = Block(whileRange.getStartLine() % 12, true, IconCategory.End);
		try {
			Token endToken = jj_consume_token(10);
			jj_consume_token(18);
			endRange = Grammar.range(endToken);
			block.getRange().expandToTouch(endRange);
		} catch (ParseException e) {
			endRange = expectBlockEnd(block, IconCategory.End, new IconCategory[0]);
		}
		if ("" != null) {
			return new WhileStatement(Grammar.range(whileRange, endRange), condition, block);
		}
		throw new Error("Missing return statement in function");
	}
}
