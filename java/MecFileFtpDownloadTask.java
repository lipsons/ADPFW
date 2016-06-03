//package com.mecglobal.s3automation.java;

import org.apache.commons.net.ftp.FTPClient;

import com.jcraft.jsch.SftpException;

public class MecFileFtpDownloadTask implements Runnable {

	String fileName;
	String generalFilePath;
	String fileRemoteDirectory;
	String client;
	String procedureType;
	FTPClient ftpConnection;

	public MecFileFtpDownloadTask(String fn, String g, String fr, String c, String p) {
		fileName = fn;
		generalFilePath = g;
		fileRemoteDirectory = fr;
		client = c;
		procedureType = p;
	}
	
	public MecFileFtpDownloadTask(String key, String genFilePath, String value, FTPClient ftpClient) {
		fileName = key;
		generalFilePath = genFilePath;
		fileRemoteDirectory = value;
		ftpConnection = ftpClient;
	}

	public String toString() {
		return "File name: " + fileName + "\nGeneral filepath: " + generalFilePath + "\nFile remote directory: "
				+ fileRemoteDirectory;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getGeneralFilePath() {
		return generalFilePath;
	}

	public void setGeneralFilePath(String generalFilePath) {
		this.generalFilePath = generalFilePath;
	}

	public String getFileRemoteDirectory() {
		return fileRemoteDirectory;
	}

	public void setFileRemoteDirectory(String fileRemoteDirectory) {
		this.fileRemoteDirectory = fileRemoteDirectory;
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public String getProcedureType() {
		return procedureType;
	}

	public void setProcedureType(String procedureType) {
		this.procedureType = procedureType;
	}

	@Override
	public void run() {
//		if(procedureType.equalsIgnoreCase("ftp")){
//			MecFtpMethods.getFtpFile(fileName, generalFilePath, fileRemoteDirectory, client, procedureType);
//		}
//		else if(procedureType.equalsIgnoreCase("sftp")){
//			try {
//				MecSftp.getFile(fileName, generalFilePath, fileRemoteDirectory, client, procedureType);
//			} catch (SftpException e) {
//				e.printStackTrace();
//			}
//		}
	}

}
