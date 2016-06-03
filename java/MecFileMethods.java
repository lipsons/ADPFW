//package com.mecglobal.s3automation.java;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTPClient;

import com.jcraft.jsch.SftpException;

public class MecFileMethods{

//	public static ArrayList<String> listFiles(String clientName) throws ParseException {
//
//		ArrayList<String> allFiles = new ArrayList<String>();
//
//		MecClient client = new MecClient();
//
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);
//			String packageType = client.getPackageType();
//			
//			String startDate = MecTimeMethods.getStartDate(client.getLookbackWindow());
//
//			String endDate = MecTimeMethods.getStartDate(1);
//
//			ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
//					MecTimeMethods.daysBetween(startDate, endDate));
//
//			// get list of packages
//			ArrayList<MecFile> filePackages = MecVerticaMethods.getFilePackages(conn, client.getRegistrarId());
//
//			Map<String, MecFile> fileMap = new HashMap<String, MecFile>();
//
//			FTPClient ftpClient = new FTPClient();
//			MecSftp sftp = new MecSftp();
//			
//			if(packageType.equalsIgnoreCase("ftp")){
//				// connect to FTP
//				ftpClient = MecFtpMethods.connectFtp(clientName, client.getGeneralFilePath()
//						, client.getPackageType());
//				}
//			else if(packageType.equalsIgnoreCase("sftp")){
//				sftp = MecSftp.connect(clientName, client.getGeneralFilePath(), client.getPackageType());
//			}
//			
//			for (int f = 0; f < filePackages.size(); f++) {
//
//				for (int t = 0; t < allDates.size(); t++) {
//					String year = MecTimeMethods.getYear(allDates.get(t));
//					String month = MecTimeMethods.getMonth(allDates.get(t));
//					String day = MecTimeMethods.getDay(allDates.get(t));
//
//					if(packageType.equalsIgnoreCase("ftp")){
//						HashMap<String, MecFile> ftpFiles = MecFtpMethods.listFtpFiles(ftpClient,
//								filePackages.get(f).getFileRemoteDirectory(), filePackages.get(f).getFilePattern(),
//								filePackages.get(f).getFileType(), year, month, day, client.getGeneralFilePath());
//						fileMap.putAll(ftpFiles);
//					}
//					else if(packageType.equalsIgnoreCase("sftp")){
//						HashMap<String, MecFile> sftpFiles = MecSftp.listFiles(sftp,
//								filePackages.get(f).getFileRemoteDirectory(), filePackages.get(f).getFilePattern(),
//								filePackages.get(f).getFileType(), year, month, day, client.getGeneralFilePath());
//						fileMap.putAll(sftpFiles);
//					}
//				}
//			}
//
//			Iterator<Map.Entry<String, MecFile>> it = fileMap.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, MecFile> fileToLoad = (Map.Entry<String, MecFile>) it.next();
//				allFiles.add(fileToLoad.getKey());
//				it.remove();
//			}
//
//			// get loaded files and remove any matches from the file-to-load set
//			HashMap<String, Integer> loadedFiles = MecVerticaMethods.getLoadedFiles(conn);
//			Set<String> loadedFileNames = new HashSet<String>(loadedFiles.keySet());
//			allFiles.removeAll(loadedFileNames);
//
//			// disconnect from FTP
//			if(packageType.equalsIgnoreCase("ftp")){
//				MecFtpMethods.disconnectFtp(ftpClient);
//			}
//			else if(packageType.equalsIgnoreCase("sftp")){
//				MecSftp.disconnect(sftp);
//			}
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		java.util.Collections.sort(allFiles);
//		return allFiles;
//	}
//
//	public static void getFiles(String clientName) throws ParseException, SftpException {
//
//		MecClient client = new MecClient();
//
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);
//
//			String packageType = client.getPackageType();
//			String genFilePath = client.getGeneralFilePath();
//			int registrarId = client.getRegistrarId();
//			int lookbackWindow = client.getLookbackWindow();
//
//			String startDate = MecTimeMethods.getStartDate(lookbackWindow);
//
//			String endDate = MecTimeMethods.getStartDate(1);
//
//			ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
//					MecTimeMethods.daysBetween(startDate, endDate));
//
//			// get list of packages
//			ArrayList<MecFile> filePackages = MecVerticaMethods.getFilePackages(conn, registrarId);
//
//			FTPClient ftpClient = new FTPClient();
//			MecSftp sftp = new MecSftp();
//			
//			if(packageType.equalsIgnoreCase("ftp")){
//				// connect to FTP
//				ftpClient = MecFtpMethods.connectFtp(clientName, client.getGeneralFilePath()
//						, client.getPackageType());
//				}
//			else if(packageType.equalsIgnoreCase("sftp")){
//				sftp = MecSftp.connect(clientName, client.getGeneralFilePath(), client.getPackageType());
//			}
//			Map<String, MecFile> fileMap = new HashMap<String, MecFile>();
//
//			for (int f = 0; f < filePackages.size(); f++) {
//
//				for (int t = 0; t < allDates.size(); t++) {
//					String year = MecTimeMethods.getYear(allDates.get(t));
//					String month = MecTimeMethods.getMonth(allDates.get(t));
//					String day = MecTimeMethods.getDay(allDates.get(t));
//
//					if(packageType.equalsIgnoreCase("ftp")){
//						HashMap<String, MecFile> ftpFiles = MecFtpMethods.listFtpFiles(ftpClient,
//								filePackages.get(f).getFileRemoteDirectory(), filePackages.get(f).getFilePattern(),
//								filePackages.get(f).getFileType(), year, month, day, client.getGeneralFilePath());
//						fileMap.putAll(ftpFiles);
//					}
//					else if(packageType.equalsIgnoreCase("sftp")){
//						HashMap<String, MecFile> sftpFiles = MecSftp.listFiles(sftp,
//								filePackages.get(f).getFileRemoteDirectory(), filePackages.get(f).getFilePattern(),
//								filePackages.get(f).getFileType(), year, month, day, client.getGeneralFilePath());
//						fileMap.putAll(sftpFiles);
//					}
//				}
//			}
//
//			// disconnect from FTP
//			if(packageType.equalsIgnoreCase("ftp")){
//				MecFtpMethods.disconnectFtp(ftpClient);
//			}
//			else if(packageType.equalsIgnoreCase("sftp")){
//				MecSftp.disconnect(sftp);
//			}
//
//			// get loaded files and remove any matches from the file-to-load set
//			HashMap<String, Integer> loadedFiles = MecVerticaMethods.getLoadedFiles(conn);
//
//			// download files
//			System.out.println("Downloading files ...");
//
//			Map<String, String> filesForQueue = new HashMap<String, String>();
//
//			Iterator<Map.Entry<String, MecFile>> it = fileMap.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, MecFile> fileToLoad = (Map.Entry<String, MecFile>) it.next();
//				if (loadedFiles.containsKey(fileToLoad.getKey().substring(0, fileToLoad.getKey().lastIndexOf(".")))) {
//					System.out.println(fileToLoad.getValue().getFileName() 
//							+ " has already been loaded into Vertica");
//				} 
//				else if (loadedFiles.containsKey(fileToLoad.getKey())) {
//					System.out.println(fileToLoad.getValue().getFileName() 
//							+ " has already been loaded into Vertica");
//				} else {
//					filesForQueue.put(fileToLoad.getValue().getFileName(),
//							fileToLoad.getValue().getFileRemoteDirectory());
//				}
//				it.remove();
//			}
//
//			SortedSet<String> keys = new TreeSet<String>(filesForQueue.keySet());
//			for (String key : keys) {
//				if(packageType.equalsIgnoreCase("ftp")){
//					MecFtpMethods.getFtpFile(key, genFilePath, filesForQueue.get(key), clientName, packageType);
//				}
//				else if(packageType.equalsIgnoreCase("sftp")){
//					MecSftp.getFile(key, genFilePath, filesForQueue.get(key), clientName, packageType);
//				}
//			}
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void getFilesMultithread(String clientName) throws ParseException {
//
//		MecClient client = new MecClient();
//
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);
//
//			String packageType = client.getPackageType();
//			String genFilePath = client.getGeneralFilePath();
//			int registrarId = client.getRegistrarId();
//			int lookbackWindow = client.getLookbackWindow();
//
//			String startDate = MecTimeMethods.getStartDate(lookbackWindow);
//
//			String endDate = MecTimeMethods.getStartDate(1);
//
//			ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
//					MecTimeMethods.daysBetween(startDate, endDate));
//
//			// get list of packages
//			ArrayList<MecFile> filePackages = MecVerticaMethods.getFilePackages(conn, registrarId);
//
//			FTPClient ftpClient = new FTPClient();
//			MecSftp sftp = new MecSftp();
//			
//			if(packageType.equalsIgnoreCase("ftp")){
//				// connect to FTP
//				ftpClient = MecFtpMethods.connectFtp(clientName, client.getGeneralFilePath()
//						, client.getPackageType());
//				}
//			else if(packageType.equalsIgnoreCase("sftp")){
//				sftp = MecSftp.connect(clientName, client.getGeneralFilePath(), client.getPackageType());
//			}
//			Map<String, MecFile> fileMap = new HashMap<String, MecFile>();
//
//			for (int f = 0; f < filePackages.size(); f++) {
//
//				for (int t = 0; t < allDates.size(); t++) {
//					String year = MecTimeMethods.getYear(allDates.get(t));
//					String month = MecTimeMethods.getMonth(allDates.get(t));
//					String day = MecTimeMethods.getDay(allDates.get(t));
//
//					if(packageType.equalsIgnoreCase("ftp")){
//						HashMap<String, MecFile> ftpFiles = MecFtpMethods.listFtpFiles(ftpClient,
//								filePackages.get(f).getFileRemoteDirectory(), filePackages.get(f).getFilePattern(),
//								filePackages.get(f).getFileType(), year, month, day, client.getGeneralFilePath());
//						fileMap.putAll(ftpFiles);
//					}
//					else if(packageType.equalsIgnoreCase("sftp")){
//						HashMap<String, MecFile> sftpFiles = MecSftp.listFiles(sftp,
//								filePackages.get(f).getFileRemoteDirectory(), filePackages.get(f).getFilePattern(),
//								filePackages.get(f).getFileType(), year, month, day, client.getGeneralFilePath());
//						fileMap.putAll(sftpFiles);
//					}
//				}
//			}
//
//			// disconnect from FTP
//			if(packageType.equalsIgnoreCase("ftp")){
//				MecFtpMethods.disconnectFtp(ftpClient);
//			}
//			else if(packageType.equalsIgnoreCase("sftp")){
//				MecSftp.disconnect(sftp);
//			}
//
//			// get loaded files and remove any matches from the file-to-load set
//			HashMap<String, Integer> loadedFiles = MecVerticaMethods.getLoadedFiles(conn);
//			
//			// download files
//			System.out.println("Downloading files ...");
//
//			Map<String, String> filesForQueue = new HashMap<String, String>();
//			
//			Iterator<Map.Entry<String, MecFile>> it = fileMap.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, MecFile> fileToLoad = (Map.Entry<String, MecFile>) it.next();
//				
//				if (loadedFiles.containsKey(fileToLoad.getKey().substring(0, fileToLoad.getKey().lastIndexOf(".")))) {
//					System.out.println(fileToLoad.getValue().getFileName() 
//							+ " has already been loaded into Vertica");
//				} 
//				else if (loadedFiles.containsKey(fileToLoad.getKey())) {
//					System.out.println(fileToLoad.getValue().getFileName() 
//							+ " has already been loaded into Vertica");
//				} 
//				else {
//					filesForQueue.put(fileToLoad.getValue().getFileName(),
//							fileToLoad.getValue().getFileRemoteDirectory());
//				}
//				it.remove();
//			}
//			
//			ExecutorService pool = Executors.newFixedThreadPool(10);
//			SortedSet<String> keys = new TreeSet<String>(filesForQueue.keySet());
//			for (String key : keys) {
//				Runnable myRunnable = new MecFileFtpDownloadTask(key, genFilePath, filesForQueue.get(key)
//						, clientName, packageType);
//				pool.execute(myRunnable);
//			}
//			// closing thread pool
//			pool.shutdown();
//			try {
//				pool.awaitTermination(60, TimeUnit.MINUTES);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void unzipFiles(String clientName) {
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			MecClient client = MecVerticaMethods.getClient(conn, clientName);
//
//			String genFilePath = client.getGeneralFilePath();
//
//			File dirToSearch = new File(client.getGeneralFilePath());
//
//			File[] result = dirToSearch.listFiles();
//
//			if (result != null && result.length > 0) {
//				for (File aFile : result) {
//					String fileName = aFile.getName();
//					String filePath = genFilePath + fileName;
//					// System.out.println(aFile.getName());
//					if (aFile.isDirectory()
//							&& !fileName.substring(fileName.length() - 3, fileName.length()).toUpperCase().equals(".GZ")
//							&& !fileName.substring(fileName.length() - 3, fileName.length()).toUpperCase().equals(".ZIP")) {
//						continue;
//					} else if (fileName.substring(fileName.length() - 3, fileName.length()).toUpperCase().equals(".GZ")) {
//						// create the output file without the .gz extension.
//						String extractFilePath = genFilePath + fileName.substring(0, fileName.length() - 3);
//						extractGzipFile(filePath, extractFilePath);
//					} else if (fileName.substring(fileName.length() - 4, fileName.length()).toUpperCase()
//							.equals(".ZIP")) {
//						// create the output file without the .zip extension.
//						String extractFilePath = genFilePath + fileName.substring(0, fileName.length() - 4);
//						extractZipFile(filePath, extractFilePath, genFilePath);
//					}
//				}
//			}
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void deleteFile(Path path){
//		try {
//		    Files.delete(path);
//		    System.out.println(path + " has been deleted");
//		} catch (NoSuchFileException x) {
//		    System.err.format("%s: no such" + " file or directory%n", path);
//		} catch (DirectoryNotEmptyException x) {
//		    System.err.format("%s not empty%n", path);
//		} catch (IOException x) {
//		    // File permission problems are caught here.
//		    System.err.println(x);
//		}
//	}
//	
//	public static void loadFiles(String clientName) {
//		String fileSchema = "Cross_Client_Resources";
//		String systemSchema = "SysOps";
//		MecClient client = new MecClient();
//
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);
//
//			String genFilePath = client.getGeneralFilePath();
//			int registrarId = client.getRegistrarId();
//			int lookbackWindow = client.getLookbackWindow();
//
//			String startDate = MecTimeMethods.getStartDate(lookbackWindow);
//			String endDate = MecTimeMethods.getStartDate(1);
//
//			ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
//					MecTimeMethods.daysBetween(startDate, endDate));
//
//			// get list of packages
//			ArrayList<MecFile> filePackages = MecVerticaMethods.getFilePackagesForVertica(conn, registrarId);
//
//			for (int f = 0; f < filePackages.size(); f++) {
//				
//				for (int t = 0; t < allDates.size(); t++) {
//					final String year = MecTimeMethods.getYear(allDates.get(t));
//					final String month = MecTimeMethods.getMonth(allDates.get(t));
//					final String day = MecTimeMethods.getDay(allDates.get(t));
//
//					final File dirToSearch = new File(genFilePath);
//					final String filePattern = filePackages.get(f).getFilePattern();
////					final String fileType = filePackages.get(f).getFileType();
//
//					FilenameFilter filter = new FilenameFilter() {
//
//						@Override
//						public boolean accept(File dirToSearch, String fileName) {
//
//							String stringToSearch = fileName;
//							String searchDate = year + "-?" + month + "-?" + day;
//							String yearAbbr = year.substring(2);
//							String patternToSeachFor = filePattern.replaceAll("\\%dt", searchDate)
//									.replaceAll("\\*", ".*").replaceAll("\\%Y", year).replaceAll("\\%m", month)
//									.replaceAll("\\%d", day).replaceAll("\\%y", yearAbbr).replace(".gz", "")
//									.replace(".zip", "");
//							Pattern p = Pattern.compile(patternToSeachFor, Pattern.CASE_INSENSITIVE);
//							Matcher m = p.matcher(stringToSearch);
//							return (m.matches() && !stringToSearch.endsWith(".zip")
//									&& !stringToSearch.endsWith(".gz"));
//						}
//					};
//
//					File[] result = dirToSearch.listFiles(filter);
//					Arrays.sort(result);
//
//					if (result != null && result.length > 0) {
//						for (File aFile : result) {
//							System.out.println("Loading " + aFile.getName() + "..");
//							MecFileMethods.loadSingleFile(clientName, aFile.getName(), genFilePath
//									, filePackages.get(f).getDbTable(), filePackages.get(f).getDbSchema()
//									, fileSchema, systemSchema, filePackages.get(f).getSkipHeaderRows()
//									, filePackages.get(f).getKeepFile(), filePackages.get(f).getFileDelimiter());
//						}
//					}
//				}
//			}
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void loadFilesForTestingOnly(String clientName) {
//
//		MecClient client = new MecClient();
//
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);
//
//			String genFilePath = client.getGeneralFilePath();
//			int registrarId = client.getRegistrarId();
//			int lookbackWindow = client.getLookbackWindow();
//
//			String startDate = MecTimeMethods.getStartDate(lookbackWindow);
//			String endDate = MecTimeMethods.getStartDate(1);
//
//			ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
//					MecTimeMethods.daysBetween(startDate, endDate));
//
//			// get list of packages
//			ArrayList<MecFile> filePackages = MecVerticaMethods.getFilePackagesForVertica(conn, registrarId);
//
//			for (int f = 0; f < filePackages.size(); f++) {
//				
//				for (int t = 0; t < allDates.size(); t++) {
//					final String year = MecTimeMethods.getYear(allDates.get(t));
//					final String month = MecTimeMethods.getMonth(allDates.get(t));
//					final String day = MecTimeMethods.getDay(allDates.get(t));
//
//					final File dirToSearch = new File(genFilePath);
//					final String filePattern = filePackages.get(f).getFilePattern();
////					final String fileType = filePackages.get(f).getFileType();
//
//					FilenameFilter filter = new FilenameFilter() {
//
//						@Override
//						public boolean accept(File dirToSearch, String fileName) {
//
//							String stringToSearch = fileName;
//							String searchDate = year + "-?" + month + "-?" + day;
//							String yearAbbr = year.substring(2);
//							String patternToSeachFor = filePattern.replaceAll("\\%dt", searchDate)
//									.replaceAll("\\*", ".*").replaceAll("\\%Y", year).replaceAll("\\%m", month)
//									.replaceAll("\\%d", day).replaceAll("\\%y", yearAbbr).replace(".gz", "")
//									.replace(".zip", "");
//							Pattern p = Pattern.compile(patternToSeachFor, Pattern.CASE_INSENSITIVE);
//							Matcher m = p.matcher(stringToSearch);
//							return (m.matches() && !stringToSearch.endsWith(".zip")
//									&& !stringToSearch.endsWith(".gz"));
//						}
//					};
//
//					File[] result = dirToSearch.listFiles(filter);
//					Arrays.sort(result);
//
//					if (result != null && result.length > 0) {
//						for (File aFile : result) {
//							System.out.println("Loading " + aFile.getName() + " ...");
//							MecFileMethods.loadSingleFile(clientName, aFile.getName(), genFilePath,
//									filePackages.get(f).getDbTable(), filePackages.get(f).getDbSchema(),
//									filePackages.get(f).getDbSchema(), filePackages.get(f).getDbSchema(),
//									filePackages.get(f).getSkipHeaderRows(), filePackages.get(f).getKeepFile(),
//									filePackages.get(f).getFileDelimiter());
//						}
//					}
//				}
//			}
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void loadSingleFile(String clientName, String fileName, String dataDirectory, String table, String schema,
//			String fileSchema, String systemSchema, int skipRows, Boolean keepFile, String fileDelimiter) {
//
//		String SCRIPT_NAME = "vertica_data_load.sh";
//		String DIRECTORY = "/raid0/Cross_Client_Resources/bin/com/mecglobal/s3automation/scripts/";
//		String nfsDirectory = dataDirectory.replaceFirst("raid0", "nfs");
//		String skipRowsAsText = String.valueOf(skipRows);
//		String keepFileAsText = null; 
//		if (keepFile == true){
//			keepFileAsText = "1";
//		} else {
//			keepFileAsText = "0";
//		}
//
//		try {
//			ProcessBuilder pb = new ProcessBuilder("/bin/bash", DIRECTORY + SCRIPT_NAME, clientName
//					, fileName, dataDirectory, nfsDirectory, schema, table, fileSchema, systemSchema
//					, skipRowsAsText, keepFileAsText, fileDelimiter);
//			pb.inheritIO();
//			Process p = pb.start(); // Start the process.
//			// p.waitFor(); // Wait for the process to finish.
//			int errCode = p.waitFor();
//			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
////			System.out.println("Echo Output:\n" + processOutput(p.getInputStream()));
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	private static void extractGzipFile(String filePath, String extractFilePath) throws FileNotFoundException {
//
//		FileInputStream fileIn = new FileInputStream(filePath);
//
//		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(extractFilePath));
//
//		byte[] bytesIn = new byte[2048];
//		int read = 0;
//		try {
//			GZIPInputStream inputStreamGzip = new GZIPInputStream(fileIn);
//
//			while ((read = inputStreamGzip.read(bytesIn)) != -1) {
//				bos.write(bytesIn, 0, read);
//			}
//			inputStreamGzip.close();
//			System.out.println(filePath + " was decompressed successfully!");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			bos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private static void extractZipFile(String filePath, String extractFilePath, String genFilePath)
//			throws FileNotFoundException {
//		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(extractFilePath));
//
//		FileInputStream fileIn = new FileInputStream(filePath);
//
//		ZipInputStream inputStream = new ZipInputStream(fileIn);
//
//		ZipEntry entry;
//		try {
//			while ((entry = inputStream.getNextEntry()) != null) {
//				// System.out.println("entry: " + entry.getName() + ", " +
//				// entry.getSize());
//				String unZipFileName = entry.getName();
//				File newFile = new File(genFilePath + unZipFileName);
//
//				// System.out.println("file unzip : " + filePath);
//				new File(newFile.getParent()).mkdirs();
//
//				byte[] bytesArray = new byte[4096];
//				int len;
//				while ((len = inputStream.read(bytesArray)) > 0) {
//					bos.write(bytesArray, 0, len);
//
//				}
//				bos.close();
//				entry = inputStream.getNextEntry();
//			}
//			inputStream.closeEntry();
//			inputStream.close();
//
//			System.out.println(filePath + " was decompressed successfully!");
//
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		try {
//			bos.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

}
