//package com.mecglobal.s3automation.java;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

public abstract class MecFtpMethods {
//
//	public static FTPClient connectFtp(String clientName, String generalFilePath, String packageType) {
//		// create file string from parameters
//		String file = generalFilePath + "." + packageType + File.separator + clientName;
//
//		// create variables to create MecFtp object
//		String ftpServer = null;
//		int ftpPort = 0;
//		String ftpUser = null;
//		String ftpPassword = null;
//
//		// create a dictionary of values from the credentials file
//		Map<String, String> map = new HashMap<String, String>();
//
//		BufferedReader in = null;
//
//		// read credentials file and assign values to variables
//		try {
//			in = new BufferedReader(new FileReader(file));
//
//			String line = in.readLine();
//
//			while (line != null) {
//				String[] tokens = line.split("=");
//				map.put(tokens[0].trim(), tokens[1].trim());
//				line = in.readLine();
//			}
//			in.close();
//
//			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
//
//				if (pair.getKey().equals("server")) {
//					ftpServer = (String) pair.getValue();
//				} else if (pair.getKey().equals("port")) {
//					ftpPort = Integer.parseInt((String) pair.getValue());
//				} else if (pair.getKey().equals("user")) {
//					ftpUser = (String) pair.getValue();
//				} else if (pair.getKey().equals("pass")) {
//					ftpPassword = (String) pair.getValue();
//				} else {
//					System.out.println("The key \"" + pair.getKey() + "\" is unknown at " + file);
//				}
//
//			}
//
//		}
//
//		catch (IOException io) {
//			System.out.println("An IO Exception occurred");
//			io.printStackTrace();
//		}
//
//		// create an MecFtp object
//		MecFtpCredentials ftp = new MecFtpCredentials(ftpServer, ftpPort, ftpUser, ftpPassword);
//
//		System.out.println("Connecting to " + ftp.getServer());
//		FTPClient ftpClient = new FTPClient();
//		try {
//			ftpClient.connect(ftp.getServer(), ftp.getPort());
//			ftpClient.enterLocalPassiveMode();
//			ftpClient.login(ftp.getUser(), ftp.getPassword());
//		} catch (IOException ex) {
//			System.out.println("Error: " + ex.getMessage());
//			ex.printStackTrace();
//		}
//		return ftpClient;
//	}
//
//	public static void disconnectFtp(FTPClient ftp) {
//		System.out.println("Disconnecting from FTP");
//		try {
//			if (ftp.isConnected()) {
//				ftp.logout();
//				ftp.disconnect();
//			}
//		} catch (IOException ex) {
//			ex.printStackTrace();
//		}
//	}
//
//	public static HashMap<String, MecFile> listFtpFiles(FTPClient ftp, String ftpDirectory, final String filePattern,
//			final String fileExtension, final String year, final String month, final String day,
//			final String genFilePath) {
//		// create array of files
//		HashMap<String, MecFile> ftpFiles = new HashMap<String, MecFile>();
//
//		try {
//			ftp.enterLocalPassiveMode();
//			ftp.setFileType(FTP.BINARY_FILE_TYPE);
//			// create a filter based on file pattern
//			FTPFileFilter filter = new FTPFileFilter() {
//
//				@Override
//				public boolean accept(FTPFile ftpFile) {
//					String stringToSearch = ftpFile.getName();
//					String searchDate = year + "-?" + month + "-?" + day;
//					String yearAbbr = year.substring(2);
//					String patternToSeachFor = filePattern.replaceAll("\\%dt", searchDate).replaceAll("\\*", ".*")
//							.replaceAll("\\%Y", year).replaceAll("\\%m", month).replaceAll("\\%d", day)
//							.replaceAll("\\%y", yearAbbr);
//					Pattern p = Pattern.compile(patternToSeachFor, Pattern.CASE_INSENSITIVE);
//					Matcher m = p.matcher(stringToSearch);
//					return (ftpFile.isFile() && m.matches());
//				}
//			};
//
//			String dirToSearch = ftpDirectory;
//
//			FTPFile[] result = ftp.listFiles(dirToSearch, filter);
//
//			if (result != null && result.length > 0) {
//				for (FTPFile aFile : result) {
//					MecFile file = new MecFile();
//					file.setGeneralFilePath(genFilePath);
//					file.setFileRemoteDirectory(ftpDirectory);
//					file.setFileName(aFile.getName());
//					String fileName = genFilePath + aFile.getName();
//					ftpFiles.put(fileName, file);
//				}
//			}
//		} catch (IOException ex) {
//			System.out.println("Error: " + ex.getMessage());
//			ex.printStackTrace();
//		}
//
//		return ftpFiles;
//	}
//
//	public static void getFtpFile(String fileName, String genFilePath, String ftpDirectory, String client,
//			String procedureType) {
////		MecFtpMethods ftp = new MecFtpMethods();
//		// connect to FTP
//		FTPClient ftpClient = connectFtp(client, genFilePath, procedureType);
//
//		try {
//			ftpClient.enterLocalPassiveMode();
//			ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
//			// Download file from FTP
//			System.out.println("Initializing download request for " + fileName);
//			String filePath = genFilePath + fileName;
//			String remoteFile = ftpDirectory + fileName;
//			File downloadFile = new File(filePath);
//
//			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
//			InputStream inputStream = ftpClient.retrieveFileStream(remoteFile);
//
//			byte[] bytesArray = new byte[4096];
//			int bytesRead = -1;
//			while ((bytesRead = inputStream.read(bytesArray)) != -1) {
//				outputStream.write(bytesArray, 0, bytesRead);
//			}
//
//			boolean success = ftpClient.completePendingCommand();
//			if (success) {
//				System.out.println(fileName + " has been downloaded successfully");
//			}
//			outputStream.close();
//			inputStream.close();
//		} catch (IOException ex) {
//			System.out.println("Error: " + ex.getMessage());
//			ex.printStackTrace();
//		} finally {
//			try {
//				if (ftpClient.isConnected()) {
//					ftpClient.logout();
//					ftpClient.disconnect();
//				}
//			} catch (IOException ex) {
//				ex.printStackTrace();
//			}
//
//		}
//	}
//
//
}
