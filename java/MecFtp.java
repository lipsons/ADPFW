//package com.mecglobal.s3automation.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

public class MecFtp implements MecServer, MecFileAcquirer {

	private String generalFilePath;
	private String protocol;
	private String clientName;
	private FTPClient connection;
	private String remoteDirectory;
	private String filePattern;

	public MecFtp(String g, String p, String c) {
		this.generalFilePath = g;
		this.protocol = p;
		this.clientName = c;
	}
	
	public MecFtp(){
		
	}

	@Override
	public String toString() {
		return "MecFtp [generalFilePath=" + generalFilePath + ", protocol=" + protocol + ", clientName=" + clientName
				+ ", remoteDirectory=" + remoteDirectory + ", filePattern=" + filePattern + "]";
	}
	
	public String getGeneralFilePath() {
		return generalFilePath;
	}

	public void setGeneralFilePath(String generalFilePath) {
		this.generalFilePath = generalFilePath;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getRemoteDirectory() {
		return remoteDirectory;
	}

	public void setRemoteDirectory(String remoteDirectory) {
		this.remoteDirectory = remoteDirectory;
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	@Override
	public void connect() {
		// create file string from parameters
		String file = generalFilePath + "." + protocol + File.separator + clientName;

		// create variables to create MecFtp object
		String ftpServer = null;
		int ftpPort = 0;
		String ftpUser = null;
		String ftpPassword = null;

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
					ftpServer = (String) pair.getValue();
				} else if (pair.getKey().equals("port")) {
					ftpPort = Integer.parseInt((String) pair.getValue());
				} else if (pair.getKey().equals("user")) {
					ftpUser = (String) pair.getValue();
				} else if (pair.getKey().equals("pass")) {
					ftpPassword = (String) pair.getValue();
				} else {
					System.out.println("The key \"" + pair.getKey() + "\" is unknown at " + file);
				}

			}

		}

		catch (IOException io) {
			System.out.println("An IO Exception occurred");
			io.printStackTrace();
		}

		// create an MecFtp object
		MecFtpCredentials ftp = new MecFtpCredentials(ftpServer, ftpPort, ftpUser, ftpPassword);

//		System.out.println("Connecting to " + ftp.getServer());
		this.connection = new FTPClient();
		try {
			this.connection.connect(ftp.getServer(), ftp.getPort());
			this.connection.enterLocalPassiveMode();
			this.connection.login(ftp.getUser(), ftp.getPassword());
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
	}

	@Override
	public void disconnect() {
//		System.out.println("Disconnecting from FTP");
		try {
			if (this.connection.isConnected()) {
				this.connection.logout();
				this.connection.disconnect();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public ArrayList<MecFile> listFiles() {
		// create array of files
		ArrayList<MecFile> files = new ArrayList<MecFile>();

		try {
			this.connection.enterLocalPassiveMode();
			this.connection.setFileType(FTP.BINARY_FILE_TYPE);
			// create a filter based on file pattern
			MecFileFilter filter = new MecFileFilter(this.filePattern);

			FTPFile[] result = this.connection.listFiles(this.remoteDirectory, filter);

			if (result != null && result.length > 0) {
				for (FTPFile aFile : result) {
					MecFile file = new MecFile(aFile.getName());
					files.add(file);
				}
			}
		} catch (IOException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}

		return files;
	}

	@Override
	public void downloadFile(String fileName) {
		// TODO Auto-generated method stub

	}
}
