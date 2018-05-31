package com.rabbit.gui.component.code.parser;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNState;
import org.antlr.v4.runtime.atn.ActionTransition;
import org.antlr.v4.runtime.atn.AtomTransition;
import org.antlr.v4.runtime.atn.BasicBlockStartState;
import org.antlr.v4.runtime.atn.BasicState;
import org.antlr.v4.runtime.atn.BlockEndState;
import org.antlr.v4.runtime.atn.EpsilonTransition;
import org.antlr.v4.runtime.atn.LoopEndState;
import org.antlr.v4.runtime.atn.PlusBlockStartState;
import org.antlr.v4.runtime.atn.PlusLoopbackState;
import org.antlr.v4.runtime.atn.PrecedencePredicateTransition;
import org.antlr.v4.runtime.atn.RuleStartState;
import org.antlr.v4.runtime.atn.RuleStopState;
import org.antlr.v4.runtime.atn.RuleTransition;
import org.antlr.v4.runtime.atn.SetTransition;
import org.antlr.v4.runtime.atn.StarBlockStartState;
import org.antlr.v4.runtime.atn.StarLoopEntryState;
import org.antlr.v4.runtime.atn.StarLoopbackState;
import org.antlr.v4.runtime.atn.Transition;
import org.antlr.v4.runtime.misc.Pair;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rabbit.gui.utils.UtilityFunctions;

public class AntlrAutoCompletionSuggester {

	public class EditorContext {

		public String code;

		public EditorContext(String code) {
			this.code = code;
		}

		public List<Token> preceedingTokens() {
			return Lexer2List(new Python3Lexer(new ANTLRInputStream(code)));
		}
	}

	private class MyTokenStream {

		private List<Token> tokens;
		private int start;

		public MyTokenStream(List<Token> tokens) {
			this(tokens, 0);
		}

		public MyTokenStream(List<Token> tokens, int start) {
			this.tokens = tokens;
			this.start = start;
		}

		public boolean atCaret() {
			return next().getType() < 0;
		}

		public MyTokenStream move() {
			return new MyTokenStream(tokens, start + 1);
		}

		public Token next() {
			if (start >= tokens.size()) {
				return new CommonToken(-1);
			} else {
				return tokens.get(start);
			}
		}
	}

	private class ParserStack {

		private List<ATNState> states;

		public ParserStack() {
			states = Lists.newArrayList();
		}

		public ParserStack(List<ATNState> states) {
			this.states = states;
		}

		public Pair<Boolean, ParserStack> process(ATNState state) {
			if ((state instanceof RuleStartState) || (state instanceof StarBlockStartState)
					|| (state instanceof BasicBlockStartState) || (state instanceof PlusBlockStartState)
					|| (state instanceof StarLoopEntryState)) {
				List<ATNState> temp = Lists.newArrayList();
				temp.addAll(states);
				temp.add(state);
				return new Pair<>(true, new ParserStack(temp));
			} else if (state instanceof BlockEndState) {
				if (UtilityFunctions.getLastElement(states) == ((BlockEndState) state).startState) {
					return new Pair<>(true, new ParserStack(UtilityFunctions.minusLast(states)));
				} else {
					return new Pair<>(false, this);
				}
			} else if (state instanceof LoopEndState) {
				boolean cont = (UtilityFunctions.getLastElement(states) instanceof StarLoopEntryState)
						&& (((StarLoopEntryState) UtilityFunctions
								.getLastElement(states)).loopBackState == ((LoopEndState) state).loopBackState);
				if (cont) {
					return new Pair<>(true, new ParserStack(UtilityFunctions.minusLast(states)));
				} else {
					return new Pair<>(false, this);
				}
			} else if (state instanceof RuleStopState) {
				boolean cont = (UtilityFunctions.getLastElement(states) instanceof RuleStartState)
						&& (((RuleStartState) UtilityFunctions.getLastElement(states)).stopState == state);
				if (cont) {
					return new Pair<>(true, new ParserStack(UtilityFunctions.minusLast(states)));
				} else {
					return new Pair<>(false, this);
				}
			} else if ((state instanceof BasicState) || (state instanceof StarLoopbackState)
					|| (state instanceof PlusLoopbackState)) {
				return new Pair<>(true, this);
			} else {
				throw new UnsupportedOperationException(state.getClass().getCanonicalName());
			}
		}

	}

	public class TokenType {

		final int type;

		public TokenType(int type) {
			this.type = type;
		}

		@Override
		public boolean equals(Object o) {
			return (int) o == type;
		}

		public int getType() {
			return type;
		}
	}

	private String[] ruleNames;

	private Vocabulary vocabulary;

	private ATN atn;

	private final int CARET_TOKEN_TYPE = -10;

	public AntlrAutoCompletionSuggester(String[] ruleNames, Vocabulary vocabulary, ATN atn) {
		this.ruleNames = ruleNames;
		this.vocabulary = vocabulary;
		this.atn = atn;
	}

	private String describe(ATNState s) {
		if (s instanceof RuleStartState) {
			return "rule start (stop -> " + ((RuleStartState) s).stopState + ") isLeftRec "
					+ ((RuleStartState) s).isLeftRecursiveRule + "  (ruleIndex=" + s.ruleIndex + ")";
		} else if (s instanceof RuleStopState) {
			return "rule stop (ruleIndex=" + s.ruleIndex + ")";
		} else if (s instanceof BasicState) {
			return "basic";
		} else if (s instanceof PlusBlockStartState) {
			return "plus block start (loopback " + ((PlusBlockStartState) s).loopBackState + ")";
		} else if (s instanceof StarBlockStartState) {
			return "star block start";
		} else if (s instanceof StarLoopEntryState) {
			return "star loop entry start (loopback " + ((StarLoopEntryState) s).loopBackState + ") prec "
					+ ((StarLoopEntryState) s).isPrecedenceDecision;
		} else if (s instanceof StarLoopbackState) {
			return "star loopback";
		} else if (s instanceof BasicBlockStartState) {
			return "basic block start";
		} else if (s instanceof BlockEndState) {
			return "block end (start " + ((BlockEndState) s).startState + ")";
		} else if (s instanceof PlusLoopbackState) {
			return "plus loopback";
		} else if (s instanceof LoopEndState) {
			return "loop end (loopback " + ((LoopEndState) s).loopBackState + ")";
		} else {
			return "UNKNOWN " + s.getClass().getSimpleName();
		}
	}

	private String describe(String[] ruleNames, Vocabulary vocabulary, ATNState s, Transition t) {
		return String.format("%d %s TR %s", s.stateNumber, describe(s), describe(ruleNames, vocabulary, t));
	}

	private String describe(String[] ruleNames, Vocabulary vocabulary, Transition t) {
		if (t instanceof EpsilonTransition) {
			return "(e)";
		} else if (t instanceof RuleTransition) {
			return "rule " + ruleNames[((RuleTransition) t).ruleIndex] + " precedence "
					+ ((RuleTransition) t).precedence;
		} else if (t instanceof AtomTransition) {
			return "atom(" + vocabulary.getSymbolicName(((AtomTransition) t).label) + ")";
		} else if (t instanceof SetTransition) {
			String retStr = "set(";
			for (Integer it : ((SetTransition) t).set.toList()) {
				retStr += vocabulary.getSymbolicName(it) + ", ";
			}
			return retStr + ")";
		} else if (t instanceof ActionTransition) {
			return "action";
		} else if (t instanceof PrecedencePredicateTransition) {
			return "precedence predicate " + ((PrecedencePredicateTransition) t).precedence;
		} else {
			return "UNKNOWN " + t.getClass().getSimpleName();
		}
	}

	private boolean isCompatibleWithStack(ATNState state, ParserStack parserStack) {
		Pair<Boolean, ParserStack> res = parserStack.process(state);
		if (!res.a) {
			return false;
		}
		if (state.epsilonOnlyTransitions) {
			for (Transition it : state.getTransitions()) {
				if (isCompatibleWithStack(it.target, res.b)) {
					return true;
				}
			}
			return false;
		} else {
			return true;
		}
	}

	private List<Token> Lexer2List(Lexer lexer) {
		List<Token> res = new LinkedList<>();
		Token next;
		do {
			next = lexer.nextToken();
			if (next.getChannel() == 0) {
				if (next.getType() < 0) {
					next = new CommonToken(CARET_TOKEN_TYPE);
				}
				res.add(next);
			}
		} while (next.getType() >= 0);
		return res;
	}

	private void process(String[] ruleNames, Vocabulary vocabulary, ATNState state, MyTokenStream tokens,
			Set<TokenType> collector, ParserStack parserStack, Set<Integer> alreadyPassed, List<String> history) {
		boolean atCaret = tokens.atCaret();
		Pair<Boolean, ParserStack> stackRes = parserStack.process(state);
		if (!stackRes.a) {
			return;
		}
		for (Transition it : Arrays.asList(state.getTransitions())) {
			String desc = describe(ruleNames, vocabulary, state, it);
			if (it.isEpsilon()) {
				if (!alreadyPassed.contains(it.target.stateNumber)) {
					alreadyPassed.add(it.target.stateNumber);
					history.add(desc);
					process(ruleNames, vocabulary, it.target, tokens, collector, stackRes.b, alreadyPassed, history);
				}
			} else if (it instanceof AtomTransition) {
				Token nextTokenType = tokens.next();
				if (atCaret) {
					if (isCompatibleWithStack(it.target, parserStack)) {
						collector.add(new TokenType(((AtomTransition) it).label));
					}
				} else {
					if (nextTokenType.getType() == ((AtomTransition) it).label) {
						alreadyPassed = new HashSet<>();
						history.add(desc);
						process(ruleNames, vocabulary, it.target, tokens.move(), collector, stackRes.b, alreadyPassed,
								history);
					}
				}
			} else if (it instanceof SetTransition) {
				Token nextTokenType = tokens.next();
				for (Integer label : it.label().toList()) {
					if (atCaret) {
						if (isCompatibleWithStack(it.target, parserStack)) {
							collector.add(new TokenType(label));
						}
					} else {
						if (nextTokenType.getType() == label) {
							alreadyPassed = new HashSet<>();
							history.add(desc);
							process(ruleNames, vocabulary, it.target, tokens.move(), collector, stackRes.b,
									alreadyPassed, history);
						}
					}
				}
			} else {
				throw new UnsupportedOperationException(it.getClass().getCanonicalName());
			}
		}
	}

	public Set<TokenType> suggestions(EditorContext editorContext) {
		Set<TokenType> collector = Sets.newHashSet();
		List<String> history = Lists.newArrayList();
		history.add("start");
		process(ruleNames, vocabulary, atn.states.get(0), new MyTokenStream(editorContext.preceedingTokens()),
				collector, new ParserStack(), Sets.newHashSet(), history);
		return collector;
	}
}
