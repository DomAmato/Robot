package com.dyn.robot.code.grammar;

public abstract interface GrammarConstants {
	public static final int EOF = 0;
	public static final int NUMBER = 3;
	public static final int QUOTED_STRING = 4;
	public static final int SYMBOL = 5;
	public static final int IF = 6;
	public static final int THEN = 7;
	public static final int ELSEIF = 8;
	public static final int ELSE = 9;
	public static final int END = 10;
	public static final int WHILE = 11;
	public static final int DO = 12;
	public static final int FOR = 13;
	public static final int REPEAT = 14;
	public static final int TO = 15;
	public static final int BREAK = 16;
	public static final int EQUALS = 17;
	public static final int NEWLINE = 18;
	public static final int DEFAULT = 0;
	public static final String[] tokenImage = { "<EOF>", "\" \"", "\"\\t\"", "<NUMBER>", "<QUOTED_STRING>", "<SYMBOL>",
			"\"dynrobot:if\"", "\"dynrobot:then\"", "\"dynrobot:elseIf\"", "\"dynrobot:else\"", "\"dynrobot:end\"",
			"\"dynrobot:while\"", "\"dynrobot:do\"", "\"dynrobot:for\"", "\"dynrobot:repeat\"", "\"dynrobot:to\"",
			"\"dynrobot:break\"", "\"dynrobot:equalTo\"", "<NEWLINE>", "\"dynrobot:variable\"", "\"dynrobot:true\"",
			"\"dynrobot:false\"", "\"dynrobot:number\"", "\"dynrobot:string\"", "\"dynrobot:block\"",
			"\"dynrobot:item\"", "\"dynrobot:or\"", "\"dynrobot:and\"", "\"dynrobot:notEqualTo\"",
			"\"dynrobot:lessThan\"", "\"dynrobot:lessThanOrEqualTo\"", "\"dynrobot:greaterThan\"",
			"\"dynrobot:greaterThanOrEqualTo\"", "\"dynrobot:plus\"", "\"dynrobot:minus\"", "\"dynrobot:times\"",
			"\"dynrobot:dividedBy\"", "\"dynrobot:not\"", "\"dynrobot:moveForward\"", "\"dynrobot:moveBack\"",
			"\"dynrobot:moveUp\"", "\"dynrobot:moveDown\"", "\"dynrobot:turnLeft\"", "\"dynrobot:turnRight\"",
			"\"dynrobot:dig\"", "\"dynrobot:digUp\"", "\"dynrobot:digDown\"", "\"dynrobot:detect\"",
			"\"dynrobot:detectUp\"", "\"dynrobot:detectDown\"", "\"dynrobot:place\"", "\"dynrobot:placeUp\"",
			"\"dynrobot:placeDown\"", "\"dynrobot:attack\"", "\"dynrobot:attackUp\"", "\"dynrobot:attackDown\"",
			"\"dynrobot:equipLeft\"", "\"dynrobot:equipRight\"", "\"dynrobot:drop\"", "\"dynrobot:dropUp\"",
			"\"dynrobot:dropDown\"", "\"dynrobot:suck\"", "\"dynrobot:suckUp\"", "\"dynrobot:suckDown\"",
			"\"dynrobot:compare\"", "\"dynrobot:compareUp\"", "\"dynrobot:compareDown\"", "\"dynrobot:getItemCount\"",
			"\"dynrobot:queryRedstone\"", "\"dynrobot:queryRedstoneUp\"", "\"dynrobot:queryRedstoneDown\"",
			"\"dynrobot:random\"", "\"dynrobot:programDisk\"", "\"dynrobot:inspect\"", "\"dynrobot:inspectUp\"",
			"\"dynrobot:inspectDown\"", "\"dynrobot:inspectSlot\"", "\"dynrobot:select\"", "\"dynrobot:setRedstone\"",
			"\"dynrobot:setRedstoneUp\"", "\"dynrobot:setRedstoneDown\"", "\"dynrobot:randomNumber\"",
			"\"dynrobot:say\"", "\"dynrobot:comment\"" };
}
