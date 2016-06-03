//package com.mecglobal.s3automation.java;

public class MecShellUnzipper extends MecShellScript {

	private String genFilePath;


	public MecShellUnzipper(String sh, String sc) {
		shellPath = sh;
		scriptPath = sc;
	}


	@Override
	public String toString() {
		return "MecShellUnzipper [genFilePath=" + genFilePath + ", shellPath=" + shellPath + ", scriptPath="
				+ scriptPath + "]";
	}

	public String getGenFilePath() {
		return genFilePath;
	}

	public void setGenFilePath(String genFilePath) {
		this.genFilePath = genFilePath;
	}
	
	public void runScript() {

		try {
			ProcessBuilder pb = new ProcessBuilder(shellPath, scriptPath, this.genFilePath);
			pb.inheritIO();
			Process p = pb.start();
			int errCode = p.waitFor();
			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
