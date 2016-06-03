//package com.mecglobal.s3automation.java;

public class MecShellExporter extends MecShellScript {

	private String sessionId;
	private int componentId;
	private int batchId;
	private boolean outputFile;
	private int outputFileId;
	private String fileName;
	private String startDate;
	private String endDate;

	public MecShellExporter(String sh, String sc, String si, int ci, int bi, boolean of, int fi
			, String fn, String sd, String ed) {
		shellPath = sh;
		scriptPath = sc;
		sessionId = si;
		componentId = ci;
		batchId = bi;
		outputFile = of;
		outputFileId = fi;
		fileName = fn;
		startDate = sd;
		endDate = ed;
	}
	
	public MecShellExporter(String sh, String sc) {
		shellPath = sh;
		scriptPath = sc;
	}

	@Override
	public String toString() {
		return "MecShellExporter [sessionId=" + sessionId + ", componentId=" + componentId + ", batchId=" + batchId
				+ ", outputFile=" + outputFile + ", outputFileId=" + outputFileId + ", fileName=" + fileName
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", shellPath=" + shellPath + ", scriptPath="
				+ scriptPath + "]";
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

	public int getOutputFileId() {
		return outputFileId;
	}

	public void setOutputFileId(int outputFileId) {
		this.outputFileId = outputFileId;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public void runScript() {
		String outputFileAsText = null;
		if (this.outputFile == true) {
			outputFileAsText = "1";
		} else {
			outputFileAsText = "0";
		}
		System.out.println(String.valueOf(batchId) + "|" + String.valueOf(componentId) + "|" + sessionId + "|"
				+ outputFileAsText + "|" + String.valueOf(this.outputFileId) + "|" + this.fileName + "|"
				+ this.startDate + "|" + this.endDate);
		try {
			ProcessBuilder pb = new ProcessBuilder(shellPath, scriptPath, String.valueOf(batchId),
					String.valueOf(componentId), sessionId, outputFileAsText, String.valueOf(this.outputFileId),
					this.fileName, this.startDate, this.endDate);
			pb.inheritIO();
			Process p = pb.start();
			int errCode = p.waitFor();
			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
