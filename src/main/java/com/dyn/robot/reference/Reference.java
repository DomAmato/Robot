package com.dyn.robot.reference;

public class Reference {

	// we cant use a pipe character here since the resource location
	// cannot contain them and then all of our textures will be missing
	public static final String MOD_ID = "robot";
	public static final String VERSION = "1.0";
	public static final String MOD_NAME = "Robot";

	public static final String SERVER_PROXY_CLASS = "com.dyn.robot.proxy.Server";
	public static final String CLIENT_PROXY_CLASS = "com.dyn.robot.proxy.Client";
}
