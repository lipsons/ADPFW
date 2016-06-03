//package com.mecglobal.s3automation.java;

public class MecShellReader extends MecShellScript {

	private String sessionId;
	private int componentId;
	private int batchId;
	private boolean outputFile; 
	

	public MecShellReader(String sh, String sc, String si, int c, int b, boolean o) {
		shellPath = sh;
		scriptPath = sc;
		sessionId = si;
		componentId = c;
		batchId = b;
		outputFile = o;
	}

	public MecShellReader(String sh, String sc) {
		shellPath = sh;
		scriptPath = sc;
	}

	public String toString() {
		return "MecShellReader [sessionId=" + sessionId + ", componentId=" + componentId + ", batchId=" + batchId
				+ ", outputFile=" + outputFile + "]";
	}	

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public int getComponentId() {
		return componentId;
	}

	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}
	
	public int getBatchId() {
		return batchId;
	}

	public void setBatchId(int batchId) {
		this.batchId = batchId;
	}
	
	public boolean isOutputFile() {
		return outputFile;
	}

	public void setOutputFile(boolean outputFile) {
		this.outputFile = outputFile;
	}

	public void runScript() {
		String outputFileAsText = null; 
		if (this.outputFile == true){
			outputFileAsText = "1";
		} else {
			outputFileAsText = "0";
		}
		System.out.println(String.valueOf(batchId) + "|" + String.valueOf(componentId) + "|" + sessionId
				 + "|" + outputFileAsText);
		try {
			ProcessBuilder pb = new ProcessBuilder(shellPath, scriptPath
					, String.valueOf(batchId), String.valueOf(componentId), sessionId, outputFileAsText);
			pb.inheritIO();
			Process p = pb.start();
			int errCode = p.waitFor();
			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
