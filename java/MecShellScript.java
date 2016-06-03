//package com.mecglobal.s3automation.java;

public class MecShellScript implements MecScript{

	protected String shellPath;
	protected String scriptPath;

	public MecShellScript() {
	}

	public String toString() {
		return "Shell Path: " + shellPath + "\nScript Path: " + scriptPath;
	}

	public String getShellPath() {
		return shellPath;
	}

	public void setShellPath(String shellPath) {
		this.shellPath = shellPath;
	}

	public String getScriptPath() {
		return scriptPath;
	}

	public void setScriptPath(String scriptPath) {
		this.scriptPath = scriptPath;
	}

	public void runScript() {
		
	}
}
