package com.rabbit.gui.component.code.parser;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenFactory;
import org.antlr.v4.runtime.TokenSource;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class CollectorTokenSource implements TokenSource {

	private final TokenSource source;
	private final Set<Integer> collectTokenTypes = Sets.newHashSet();
	private final LinkedList<Token> collectedTokens = Lists.newLinkedList();
	private final boolean collectAll;

	public CollectorTokenSource(TokenSource source) {
		super();
		this.source = source;
		collectAll = true;
	}

	public CollectorTokenSource(TokenSource source, Collection<Integer> collectTokenTypes) {
		super();
		this.source = source;
		this.collectTokenTypes.addAll(collectTokenTypes);
		collectAll = false;
	}

	@Override
	public int getCharPositionInLine() {
		return source.getCharPositionInLine();
	}

	public LinkedList<Token> getCollectedTokens() {
		return collectedTokens;
	}

	@Override
	public CharStream getInputStream() {
		return source.getInputStream();
	}

	@Override
	public int getLine() {
		return source.getLine();
	}

	@Override
	public String getSourceName() {
		return "Collect hidden channel " + source.getSourceName();
	}

	@Override
	public TokenFactory<?> getTokenFactory() {
		return source.getTokenFactory();
	}

	@Override
	public Token nextToken() {
		Token nextToken = source.nextToken();
		if (shouldCollect(nextToken)) {
			collectedTokens.add(nextToken);
		}
		return nextToken;
	}

	@Override
	public void setTokenFactory(TokenFactory<?> factory) {
		source.setTokenFactory(factory);
	}

	protected boolean shouldCollect(Token nextToken) {
		return collectAll || collectTokenTypes.contains(nextToken.getType());
	}

}
