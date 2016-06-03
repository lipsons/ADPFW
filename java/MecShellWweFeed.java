//package com.mecglobal.s3automation.java;

public class MecShellWweFeed extends MecShellScript {

	private String rowId;
	private String fileDate;

	public MecShellWweFeed(String sh, String sc, String r, String f) {
		shellPath = sh;
		scriptPath = sc;
		rowId = r;
		fileDate = f;
	}

	public MecShellWweFeed(String sh, String sc) {
		shellPath = sh;
		scriptPath = sc;
	}


	@Override
	public String toString() {
		return "MecShellWweFeed [rowId=" + rowId + ", fileDate=" + fileDate + "]";
	}




	public String getRowId() {
		return rowId;
	}




	public void setRowId(String rowId) {
		this.rowId = rowId;
	}




	public String getFileDate() {
		return fileDate;
	}




	public void setFileDate(String fileDate) {
		this.fileDate = fileDate;
	}




	public void runScript() {
		try {
			ProcessBuilder pb = new ProcessBuilder(shellPath, scriptPath, this.rowId, this.fileDate);
			Process p = pb.start();
			int errCode = p.waitFor();
			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
