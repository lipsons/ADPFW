//package com.mecglobal.s3automation.java;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

public class MecSftp implements MecServer, MecFileAcquirer {

	private String generalFilePath;
	private String protocol;
	private String clientName;
	private String remoteDirectory;
	private String filePattern;
	private Session session;
	private Channel channel;
	private ChannelSftp channelSftp;
	
	public MecSftp(String g, String p, String c) {
		this.generalFilePath = g;
		this.protocol = p;
		this.clientName = c;
	}
	
	public MecSftp(){
		
	}
	
	@Override
	public String toString() {
		return "MecSftp [generalFilePath=" + generalFilePath + ", protocol=" + protocol + ", clientName=" + clientName
				+ ", remoteDirectory=" + remoteDirectory + ", filePattern=" + filePattern + "]";
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

	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
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
		String fileKeyName = null;

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
				} else if (pair.getKey().equals("key")) {
					fileKeyName = (String) pair.getValue();
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
		MecFtpCredentials ftpProfile = new MecFtpCredentials(ftpServer, ftpPort, ftpUser, ftpPassword, fileKeyName);
		
		String fileKey = generalFilePath + "." + protocol + File.separator + ftpProfile.getKey();
		
		try {
			JSch jsch = new JSch();
			File privateKeyFile = new File(fileKey);
			if (privateKeyFile.exists()){
				try {
					jsch.addIdentity(fileKey);
				} catch (JSchException e) {
					e.printStackTrace();
				}
			}
			try {
				this.session = jsch.getSession(ftpProfile.getUser(), ftpProfile.getServer(), ftpProfile.getPort());
			} catch (JSchException e) {
				e.printStackTrace();
			}
			if (!privateKeyFile.exists()){
				this.session.setPassword(ftpProfile.getPassword());
			}

			this.session.setConfig("StrictHostKeyChecking", "no");
			
//			System.out.println("Connecting to " + ftpProfile.getServer());
			try {
				this.session.connect();
			} catch (JSchException e) {
				e.printStackTrace();
			}
//			System.out.println("Connected to " + ftpProfile.getServer());

			try {
				this.channel = this.session.openChannel("sftp");
			} catch (JSchException e) {
				e.printStackTrace();
			}
			try {
				this.channel.connect();
			} catch (JSchException e) {
				e.printStackTrace();
			}
			this.channelSftp = (ChannelSftp) this.channel;
		} catch (Exception ex) {
			ex.printStackTrace();
		}	
		
	}
	
	@Override
	public void disconnect() {
//		System.out.println("Disconnecting from SFTP");
		
		if (this.channelSftp.isConnected()) {
			try {
				this.session.disconnect();
				this.channel.disconnect();
				this.channelSftp.quit();
//				System.out.println("SFTP has been successfully disconnected.");
			} catch (Exception ioe) {
				System.out.println("SFTP failed to disconnect from ftp server.");
			}
		}
	}
	
	@Override
	public ArrayList<MecFile> listFiles() {
		
		// create array of files
		ArrayList<MecFile> files = new ArrayList<MecFile>();

		try {
			String dirToSearch = this.remoteDirectory;
			String fileToSearch = this.remoteDirectory+this.filePattern;
			
			this.channelSftp.cd(dirToSearch);
			
			@SuppressWarnings("unchecked")
			Vector<ChannelSftp.LsEntry> fileList = this.channelSftp.ls(fileToSearch);
			
			if (fileList != null && fileList.size() > 0) {
				for(ChannelSftp.LsEntry aFile : fileList){
					MecFile file = new MecFile(aFile.getFilename(), this.remoteDirectory);
					files.add(file);
				}
			}
		} catch (SftpException e) {
			e.printStackTrace();
		}

		return files;
	}
	
	@Override
	public void downloadFile(String fileName) {

		try {
			// Download file from SFTP
			System.out.println("Initializing download request for " + fileName);
			String filePath = this.generalFilePath + fileName;
			String remoteFile = this.remoteDirectory + fileName;
			File downloadFile = new File(filePath);
			
			this.channelSftp.cd(this.remoteDirectory);
			
			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
			InputStream inputStream = this.channelSftp.get(remoteFile);

			byte[] bytesArray = new byte[4096];
			int bytesRead = -1;
			while ((bytesRead = inputStream.read(bytesArray)) != -1) {
				outputStream.write(bytesArray, 0, bytesRead);
			}

			System.out.println(fileName + " has been downloaded successfully");
			
			outputStream.close();
			inputStream.close();
		} catch (IOException | SftpException ex) {
			System.out.println("Error: " + ex.getMessage());
			ex.printStackTrace();
		}
			
	}

}
