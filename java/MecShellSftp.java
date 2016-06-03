//package com.mecglobal.s3automation.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MecShellSftp extends MecShellScript{

	private String generalFilePath;
	private String clientName;
	private String userName;
	private String host;
	private String password;
	private String port;
	private String remoteFolder;
	private String localFolder;
	private String filePattern;
	private String remotePrompt;
	
	public MecShellSftp(String sh, String sc, String g, String c) {
		this.shellPath = sh;
		this.scriptPath = sc;
		this.generalFilePath = g;
		this.clientName = c;

	}
	
	public MecShellSftp(String sh, String sc) {
		this.shellPath = sh;
		this.scriptPath = sc;
	}



	@Override
	public String toString() {
		return "MecShellSftp [generalFilePath=" + generalFilePath + ", clientName=" + clientName + ", userName="
				+ userName + ", host=" + host + ", port=" + port + ", remoteFolder=" + remoteFolder + ", localFolder="
				+ localFolder + ", filePattern=" + filePattern + ", shellPath=" + shellPath + ", scriptPath="
				+ scriptPath + "]";
	}



	public String getGeneralFilePath() {
		return generalFilePath;
	}

	public void setGeneralFilePath(String generalFilePath) {
		this.generalFilePath = generalFilePath;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getRemoteFolder() {
		return remoteFolder;
	}

	public void setRemoteFolder(String remoteFolder) {
		this.remoteFolder = remoteFolder;
	}

	public String getLocalFolder() {
		return localFolder;
	}

	public void setLocalFolder(String localFolder) {
		this.localFolder = localFolder;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}
	
	public void getCredentials() {
		
	    // create file string from parameters
		String file = generalFilePath + ".sftp" + File.separator + clientName;
		
		this.localFolder = generalFilePath + "uploadToFTP";

		// create a dictionary of values from the credentials file
		Map<String, String> map = new HashMap<String, String>();

		BufferedReader in = null;

		// read credentials file and assign values to variables
		try {
			in = new BufferedReader(new FileReader(file));

			String line = in.readLine();

			while (line != null) {
				String[] tokens = line.split("=");
				map.put(tokens[0].trim(), tokens[1].trim());
				line = in.readLine();
			}
			in.close();

			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();

				if (pair.getKey().equals("server")) {
					this.host = (String) pair.getValue();
				} else if (pair.getKey().equals("port")) {
					this.port = (String)pair.getValue();
				} else if (pair.getKey().equals("user")) {
					this.userName = (String) pair.getValue();
				} else if (pair.getKey().equals("pass")) {
					this.password = (String) pair.getValue();
				} else if (pair.getKey().equals("directory")) {
					this.remoteFolder = (String) pair.getValue();
				} else if (pair.getKey().equals("remotePrompt")) {
					this.remotePrompt = (String) pair.getValue();
				} else {
					System.out.println("The key \"" + pair.getKey() + "\" is unknown at " + file);
				}

			}

		}

		catch (IOException io) {
			System.out.println("An IO Exception occurred");
			io.printStackTrace();
		}
	}
	
	public void runScript() {
		
		String userAndHost = this.userName + "@" + this.host;
		
		System.out.println(userAndHost + "|" +  this.port
				+ "|" + this.password + "|" + this.remoteFolder + "|" + this.localFolder
				+ "|" + this.filePattern + "|" + this.remotePrompt);
		
		try {
			ProcessBuilder pb = new ProcessBuilder(shellPath, scriptPath, userAndHost, this.port
					, this.password, this.remoteFolder, this.localFolder, this.filePattern, this.remotePrompt);
			pb.inheritIO();
			Process p = pb.start();
			int errCode = p.waitFor();
			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	} 
	
}
