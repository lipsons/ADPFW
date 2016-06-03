
//package com.mecglobal.s3automation.java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.amazonaws.util.StringUtils;

import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

public class MecClientDriver {
	static final String SHELL_PATH = "/bin/bash";
	static final String SHELL_SCRIPT_HOME = "/raid0/Cross_Client_Resources/bin/com/mecglobal/s3automation/scripts/";
	static final String HOME_DIRECTORY = "raid0";
	static final String PROGRAM_NAME = "Cross_Client_Resources";
	static final String DATABASE_NAME = "vertica";
	static final String VERTICA_FILE_NAME = "connect";
	static final String SCHEMA = "Cross_Client_Resources";
	static final String SYS_SCHEMA = "SysOps";
	static final String CLIENT_TABLE = "md_client";
	static final String SESSION_TABLE = "md_session";
	static final String BATCH_TABLE = "md_batch";
	static final String COMPONENT_TABLE = "md_component";
	static final String SESSION_DETAIL_TABLE = "md_session_detail";
	static final String REGISTRAR_TABLE = "dim_registrar";
	static final String PACKAGE_TABLE = "dim_package";
	static final String FILE_TABLE = "dim_file";
	static final String TIME_TABLE = "dim_time";
	static final String LOADING_SOURCE_TYPE = "baseLoad";
	static final String S3_DIRECTORY = "uploadToS3";
	static final String SQL_OUTPUT_DIRECTORY = "sql_output";
	static final String BUCKET_NAME = "diap.dev.us-east-1.mec-cookiecutter";
	static final String DM_SYNC_ID = "awsSync";
	static final String DM_SYNC_LOAD_SCRIPT = "datamart_sync_load.sh";
	static final String SFTP_SCRIPT = "upload_sftp.sh";
	static final String CMI_BUCKET_NAME = "mec-clickdata";

	public static void main(String[] args) {
		MecClient client = new MecClient();
		MecVerticaSession session = new MecVerticaSession();
		MecVertica vertica = new MecVertica(HOME_DIRECTORY, PROGRAM_NAME, DATABASE_NAME, VERTICA_FILE_NAME);
		vertica.connect();

		if (args.length > 0) {

			client.setId(args[0]);
			client.setName(args[1]);
			session.setSessionType("session_initiated_by_program");
			session.generateSessionId(vertica.getConnection(), client.getName(), SESSION_TABLE, SCHEMA);
			System.out.println("\nYour session ID for " + client.getName() + " is " + session.getSessionId());
			ArrayList<String> allFiles = new ArrayList<String>();
			ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA, REGISTRAR_TABLE,
					client.getId());
			if (client.getId().equalsIgnoreCase(DM_SYNC_ID)) {

				if (transmitterList.size() > 1) {
					System.out.println(
							"\nMultiple sources found. Proceeding to show files available from all active sources.");
				}

				for (int t = 0; t < transmitterList.size(); t++) {
					System.out.println("\nUnzipping files on " + transmitterList.get(t).getDestination() + " :");

					String unzipShellScriptName = "unzip_file.sh";
					MecShellUnzipper unzipper = new MecShellUnzipper(SHELL_PATH,
							SHELL_SCRIPT_HOME + unzipShellScriptName);
					unzipper.setGenFilePath(transmitterList.get(t).getDestination());
					System.out.println(unzipper.toString());
					unzipper.runScript();

					System.out.println("\nLoading files on " + transmitterList.get(t).getDestination() + " :");

					// add search patterns to file transmitter
					ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
							transmitterList.get(t).getRegistrarId());
					for (int m = 0; m < fileMapperList.size(); m++) {
						transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
					}

					ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
					searchPatternList = transmitterList.get(t).addDateToSearchPatternList();
					ArrayList<MecFile> filesToLoad = new ArrayList<MecFile>();
					Set<MecFile> stagingFiles = new HashSet<MecFile>();

					for (int s = 0; s < searchPatternList.size(); s++) {
						File dirToSearch = new File(transmitterList.get(t).getDestination());
						MecFileFilter filter = new MecFileFilter(searchPatternList.get(s).getFilePattern());
						File[] result = dirToSearch.listFiles(filter);
						Arrays.sort(result);

						if (result != null && result.length > 0) {
							for (File aFile : result) {
								MecFile stagingFile = new MecFile(aFile.getName(),
										transmitterList.get(t).getDestination(),
										searchPatternList.get(s).getFileExtension(),
										searchPatternList.get(s).getSkipHeaderRows(),
										searchPatternList.get(s).getFileDelimiter(),
										searchPatternList.get(s).getKeepFile(),
										searchPatternList.get(s).getDatabaseTable(),
										searchPatternList.get(s).getDatabaseSchema());
								stagingFiles.add(stagingFile);

							}
						}
					}
					filesToLoad.addAll(stagingFiles);

					if (filesToLoad.size() == 0) {
						System.out.println("No files found to load!");
					}

					Collections.sort(filesToLoad, MecFile.fileComparator);

					for (int f = 0; f < filesToLoad.size(); f++) {
						String loadShellScriptName = DM_SYNC_LOAD_SCRIPT;
						MecShellLoader processor = new MecShellLoader(SHELL_PATH,
								SHELL_SCRIPT_HOME + loadShellScriptName);
						processor.setSessionId(session.getSessionId());
						processor.setFileName(filesToLoad.get(f).getFileName());
						processor.setClientName(client.getName());
						processor.setDataDirectory(transmitterList.get(t).getDestination());
						processor.setTable(filesToLoad.get(f).getDatabaseTable());
						processor.setSchema(filesToLoad.get(f).getDatabaseSchema());
						processor.setFileSchema(SCHEMA);
						processor.setSystemSchema(SYS_SCHEMA);
						processor.setSkipRows(filesToLoad.get(f).getSkipHeaderRows());
						processor.setKeepFile(filesToLoad.get(f).getKeepFile());
						processor.setFileDelimiter(filesToLoad.get(f).getFileDelimiter());
						System.out.println(processor.toString());
						processor.runScript();

					}

				}

			} else {
				if (transmitterList.size() > 1) {
					System.out.println(
							"\nMultiple sources found. Proceeding to show files available from all active sources.");
				}

				for (int t = 0; t < transmitterList.size(); t++) {
					System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");

					// add search patterns to file transmitter
					ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
							transmitterList.get(t).getRegistrarId());
					for (int m = 0; m < fileMapperList.size(); m++) {
						transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
					}

					HashMap<String, Integer> loadedFiles = vertica.getLoadedFiles(SCHEMA, FILE_TABLE);
					HashMap<String, MecFile> remoteFiles = new HashMap<String, MecFile>();

					ArrayList<MecFile> files = transmitterList.get(t).listFiles();

					for (int f = 0; f < files.size(); f++) {

						String[] fileTokens = files.get(f).getFileName().split("\\.(?=[^\\.]+$)");
						String fileNameBase = fileTokens[0].trim();
						String fileExtension = fileTokens[1].trim();

						remoteFiles.put(transmitterList.get(t).getDestination().toString() + fileNameBase,
								files.get(f));

					}

					Iterator<Map.Entry<String, MecFile>> it = remoteFiles.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<String, MecFile> fileToLoad = (Map.Entry<String, MecFile>) it.next();
						if (loadedFiles.containsKey(fileToLoad.getKey())) {
							System.out.println(
									fileToLoad.getValue().getFileName() + " has already been loaded into Vertica");
						} else {
							transmitterList.get(t).addFile(fileToLoad.getValue());
						}
						it.remove();
					}

					transmitterList.get(t).downloadAllFiles();

					System.out.println("\nLoading files on " + transmitterList.get(t).getDestination() + " :");

					transmitterList.get(t).setSourceType(LOADING_SOURCE_TYPE);

					ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
					searchPatternList = transmitterList.get(t).addDateToSearchPatternList();
					ArrayList<MecFile> filesToLoad = new ArrayList<MecFile>();
					Set<MecFile> stagingFiles = new HashSet<MecFile>();

					for (int s = 0; s < searchPatternList.size(); s++) {
						File dirToSearch = new File(transmitterList.get(t).getDestination());
						MecFileFilter filter = new MecFileFilter(searchPatternList.get(s).getFilePattern());
						File[] result = dirToSearch.listFiles(filter);
						Arrays.sort(result);

						if (result != null && result.length > 0) {
							for (File aFile : result) {

								MecFile stagingFile = new MecFile(aFile.getName(),
										transmitterList.get(t).getDestination(),
										searchPatternList.get(s).getFileExtension(),
										searchPatternList.get(s).getSkipHeaderRows(),
										searchPatternList.get(s).getFileDelimiter(),
										searchPatternList.get(s).getKeepFile(),
										searchPatternList.get(s).getDatabaseTable(),
										searchPatternList.get(s).getDatabaseSchema());

								stagingFiles.add(stagingFile);

							}
						}
					}
					filesToLoad.addAll(stagingFiles);

					if (filesToLoad.size() == 0) {
						System.out.println("No files found to load!");
					}

					Collections.sort(filesToLoad, MecFile.fileComparator);

					for (int f = 0; f < filesToLoad.size(); f++) {

						String[] fileTokens = filesToLoad.get(f).getFileName().split("\\.(?=[^\\.]+$)");
						String fileNameBase = fileTokens[0].trim();
						String fileExtension = fileTokens[1].trim();

						if (fileExtension.toUpperCase().equals("GZ")) {
							filesToLoad.get(f).extract();
							filesToLoad.get(f).setFileName(fileNameBase);
						} else if (fileExtension.toUpperCase().equals("ZIP")) {
							filesToLoad.get(f).extract();
							filesToLoad.get(f).setFileName(fileNameBase);
						}

						String loadShellScriptName = "vertica_data_load.sh";
						MecShellLoader processor = new MecShellLoader(SHELL_PATH,
								SHELL_SCRIPT_HOME + loadShellScriptName);
						processor.setSessionId(session.getSessionId());
						processor.setFileName(filesToLoad.get(f).getFileName());
						processor.setClientName(client.getName());
						processor.setDataDirectory(transmitterList.get(t).getDestination());
						processor.setTable(filesToLoad.get(f).getDatabaseTable());
						processor.setSchema(filesToLoad.get(f).getDatabaseSchema());
						processor.setFileSchema(SCHEMA);
						processor.setSystemSchema(SYS_SCHEMA);
						processor.setSkipRows(filesToLoad.get(f).getSkipHeaderRows());
						processor.setKeepFile(filesToLoad.get(f).getKeepFile());
						processor.setFileDelimiter(filesToLoad.get(f).getFileDelimiter());
						System.out.println(processor.toString());
						processor.runScript();

						String zipFrom = transmitterList.get(t).getDestination() + fileNameBase + "." + fileExtension;
						String zipTo = transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator
								+ fileNameBase + "." + fileExtension;
						Path movefrom = FileSystems.getDefault().getPath(zipFrom);
						Path target = FileSystems.getDefault().getPath(zipTo);

						try {
							Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
						} catch (IOException e) {
							System.err.println(e);
						}
					}

					System.out.println("Uploading files to S3 bucket");
					MecS3 s3 = new MecS3();

					System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");
					s3.setBucketName(BUCKET_NAME);

					s3.setS3Prefix(transmitterList.get(t).getArchive());

					s3.uploadFiles(transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator);

					s3.setS3Prefix(transmitterList.get(t).getArchive() + SQL_OUTPUT_DIRECTORY + File.separator);
					s3.uploadFiles(transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator
							+ SQL_OUTPUT_DIRECTORY + File.separator);

				}
			}

			ArrayList<MecVerticaBatch> batches = new ArrayList<MecVerticaBatch>();

			if (args[2].equalsIgnoreCase("auto_feed_daily")) {

				try {
					String startDate = MecTimeMethods.getStartDate(2);
					String endDate = MecTimeMethods.getStartDate(2);

					ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
							MecTimeMethods.daysBetween(startDate, endDate));

					for (int t = 0; t < allDates.size(); t++) {

						batches = vertica.listBatches(SCHEMA, BATCH_TABLE, client.getId(), args[2]);

						int dateOption = 0;
						int mediaYear = 0;
						int mediaQuarter = 0;
						int mediaMonth = 0;

						Date feedStartDate = new Date();

						String previousDay = allDates.get(t).toString();

						SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
						feedStartDate = format.parse(previousDay);

						java.sql.Date sqlStartDate = new java.sql.Date(feedStartDate.getTime());

						session.setFeedStartDate((java.sql.Date) sqlStartDate);
						session.setFeedEndDate((java.sql.Date) sqlStartDate);

						System.out.println(session.toString());

						batchLoop: for (int b = 0; b < batches.size(); b++) {
							System.out.println("Processing batch \"" + batches.get(b).getBatchName() + "\"");
							boolean breakLoop = false;
							int errorCode = 0;
							ArrayList<MecVerticaComponent> components = vertica.listComponents(SCHEMA, COMPONENT_TABLE,
									batches.get(b).getBatchId());
							componentLoop: for (int c = 0; c < components.size(); c++) {
								System.out.println(
										"\nProcessing component \"" + components.get(c).getComponentName() + "\"");

								DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
								String fileDate = df.format(feedStartDate);
								String fileYear = MecTimeMethods.getYear(fileDate);
								String fileMonth = MecTimeMethods.getMonth(fileDate);
								String fileDay = MecTimeMethods.getDay(fileDate);

								String feedFileName = components.get(c).getFileNamePattern()
										.replaceAll("\\%media_quarter", String.valueOf(mediaQuarter))
										.replaceAll("\\%media_year", String.valueOf(mediaYear))
										.replaceAll("\\%Y", fileYear).replaceAll("\\%m", fileMonth)
										.replaceAll("\\%d", fileDay);

								session.setFileName(feedFileName);
								System.out.println(session.toString());
								session.generateSessionIdForFeed(vertica.getConnection(), client.getName(),
										SESSION_TABLE, SCHEMA);

								String shellScriptName = "run_wwe_feed_daily.sh";
								MecShellExporter processor = new MecShellExporter(SHELL_PATH,
										SHELL_SCRIPT_HOME + shellScriptName);
								processor.setSessionId(session.getSessionId());
								processor.setComponentId(components.get(c).getComponentId());
								processor.setBatchId(components.get(c).getBatchId());
								processor.setOutputFile(components.get(c).isOutputFile());
								processor.setOutputFileId(components.get(c).getFileOutputId());
								processor.setFileName(session.getFileName());
								processor.setStartDate(df.format(session.getFeedStartDate()));
								processor.setEndDate(df.format(session.getFeedEndDate()));
								System.out.println(processor.toString());
								processor.runScript();

								// MecShellSftp sftp = new
								// MecShellSftp(SHELL_PATH,
								// SHELL_SCRIPT_HOME + SFTP_SCRIPT,
								// transmitterList.get(t).getDestination()
								// , client.getName());
								//
								// sftp.getCredentials();
								//
								// sftp.setGeneralFilePath(transmitterList.get(t).getDestination());
								//
								// sftp.setFilePattern(session.getFileName()+
								// "*");
								//
								// System.out.println("\nUploading files to " +
								// sftp.getRemoteFolder() + "..");
								//
								// System.out.println(sftp.toString());
								//
								// sftp.runScript();

								if (!components.get(c).isOutputFile()) {
									errorCode = vertica.getComponentErrorCode(SCHEMA, SESSION_DETAIL_TABLE,
											components.get(c).getBatchId(), session.getSessionId(),
											components.get(c).getComponentId());

									if (errorCode == 1) {
										System.out.println("Result: PASS - \"" + components.get(c).getComponentName()
												+ "\" Component ID= " + components.get(c).getComponentId());
									} else if (errorCode == 9 || errorCode == 8) {
										System.out.println(
												"Error 9: unrecognized sql script. Please review the table \"md_component\" and/or"
														+ " the \"sql_scripts\" folder");
										breakLoop = true;
										break componentLoop;
									} else if (errorCode != 1) {
										System.out.println("Result: FAIL - \"" + components.get(c).getComponentName()
												+ "\" Component ID= " + components.get(c).getComponentId());
										breakLoop = true;
									}
								}
								System.out.println("Break loop:" + breakLoop);
							}

							if (breakLoop == true) {
								System.out.println(
										"\nBatch \"" + batches.get(b).getBatchName() + "\" failed. Process has ended.");
								break;
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else if (!args[2].equals("none")) {
				batches = vertica.listBatches(SCHEMA, BATCH_TABLE, client.getId(), args[2]);

				batchLoop: for (int b = 0; b < batches.size(); b++) {
					System.out.println("Processing batch \"" + batches.get(b).getBatchName() + "\"");
					boolean breakLoop = false;
					int errorCode = 0;
					ArrayList<MecVerticaComponent> components = vertica.listComponents(SCHEMA, COMPONENT_TABLE,
							batches.get(b).getBatchId());
					componentLoop: for (int c = 0; c < components.size(); c++) {
						System.out.println("\nProcessing component \"" + components.get(c).getComponentName() + "\"");
						String shellScriptName = "run_qa_sql.sh";
						MecShellReader processor = new MecShellReader(SHELL_PATH, SHELL_SCRIPT_HOME + shellScriptName);
						processor.setSessionId(session.getSessionId());
						processor.setComponentId(components.get(c).getComponentId());
						processor.setBatchId(components.get(c).getBatchId());
						processor.setOutputFile(components.get(c).isOutputFile());
						processor.runScript();

						if (!components.get(c).isOutputFile()) {
							errorCode = vertica.getComponentErrorCode(SCHEMA, SESSION_DETAIL_TABLE,
									components.get(c).getBatchId(), session.getSessionId(),
									components.get(c).getComponentId());

							if (errorCode == 1) {
								System.out.println("Result: PASS - \"" + components.get(c).getComponentName()
										+ "\" Component ID= " + components.get(c).getComponentId());
							} else if (errorCode == 9 || errorCode == 8) {
								System.out.println(
										"Error 9: unrecognized sql script. Please review the table \"md_component\" and/or"
												+ " the \"sql_scripts\" folder");
								breakLoop = true;
								break componentLoop;
							} else if (errorCode != 1) {
								System.out.println("Result: FAIL - \"" + components.get(c).getComponentName()
										+ "\" Component ID= " + components.get(c).getComponentId());
								breakLoop = true;
							}
						}
						System.out.println("Break loop:" + breakLoop);
					}

					if (breakLoop == true) {
						System.out.println(
								"\nBatch \"" + batches.get(b).getBatchName() + "\" failed. Process has ended.");
						break;
					}
				}
			}

		} else {

			Scanner scan = new Scanner(System.in);
			int ans;

			while (true) {
				menu();
				do {
					System.out.println("\nMENU CHOICE:");
					while (!scan.hasNextInt()) {
						System.out.println("please enter a valid number!");
						scan.next();
					}
					ans = scan.nextInt();

					if (ans == 1) {
						System.out.println("\nList of available clients in Vertica: ");
						ArrayList<MecClient> clients = vertica.listClients(SCHEMA, CLIENT_TABLE);
						Map<Integer, MecClient> clientMap = new HashMap<Integer, MecClient>();
						for (int c = 0; c < clients.size(); c++) {
							clientMap.put(c + 1, clients.get(c));
						}

						Iterator<Entry<Integer, MecClient>> it = clientMap.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Integer, MecClient> clientListPair = (Map.Entry<Integer, MecClient>) it.next();
							System.out.println(
									"(" + clientListPair.getKey() + ")	" + clientListPair.getValue().toString());
						}

						int clientKey;
						do {
							System.out.println("\nEnter number to select a client: ");
							while (!scan.hasNextInt()) {
								System.out.println("please enter a valid number!");
								scan.next();
							}
							clientKey = scan.nextInt();
						} while (clientKey <= 0 || clientKey > clientMap.size());

						scan.nextLine();
						System.out.println("\nYou have selected " + clientMap.get(clientKey).toString()
								+ "\nDo you wish to continue (y/n)?");
						boolean condition = true;
						while (condition == true) {
							String dec = scan.nextLine();
							if (dec.equalsIgnoreCase("y")) {
								client = clientMap.get(clientKey);
								session.setSessionType("session_initiated_by_user");
								session.generateSessionId(vertica.getConnection(), client.getName(), SESSION_TABLE,
										SCHEMA);
								System.out.println(
										"\nYour session ID for " + client.getName() + " is " + session.getSessionId());
								condition = false;
							} else if (dec.equalsIgnoreCase("n")) {
								condition = false;
							} else {
								System.out.println("Please enter either Y or N.");
								condition = true;
							}
						}
					}
					if (ans == 2) {
						Map<Integer, MecFile> remoteFilesForSelection = new HashMap<Integer, MecFile>();
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to show files available from all active sources.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");

							// add search patterns to file transmitter
							ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
									transmitterList.get(t).getRegistrarId());
							for (int m = 0; m < fileMapperList.size(); m++) {
								transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
							}

							ArrayList<MecFile> files = transmitterList.get(t).listFiles();
							for (int f = 0; f < files.size(); f++) {
								remoteFilesForSelection.put(f + 1, files.get(f));
								transmitterList.get(t).addFile(files.get(f));
							}

							Iterator<Entry<Integer, MecFile>> it = remoteFilesForSelection.entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry<Integer, MecFile> remoteFilePair = (Map.Entry<Integer, MecFile>) it.next();
								System.out.println("(" + remoteFilePair.getKey() + ")	"
										+ remoteFilePair.getValue().getFileName());
							}

							boolean moreDownloadRequest = true;
							while (moreDownloadRequest == true) {
								int fileKey;
								do {
									System.out.println("\nEnter number to select a file to download: ");
									while (!scan.hasNextInt()) {
										System.out.println("please enter a valid number!");
										scan.next();
									}
									fileKey = scan.nextInt();
								} while (fileKey <= 0 || fileKey > remoteFilesForSelection.size());

								scan.nextLine();
								System.out.println(
										"\nYou have selected " + remoteFilesForSelection.get(fileKey).toString()
												+ "\nDo you wish to continue (y/n)?");
								boolean condition = true;
								while (condition == true) {
									String dec = scan.nextLine();
									if (dec.equalsIgnoreCase("y")) {
										transmitterList.get(t)
												.downloadFile(remoteFilesForSelection.get(fileKey).getFileName());
										condition = false;
									} else if (dec.equalsIgnoreCase("n")) {
										condition = false;
									} else {
										System.out.println("Please enter either Y or N.");
										condition = true;
									}
								}

								System.out.println("\nDo you wish to download another file (y/n)?");
								boolean properResponse = true;
								while (properResponse == true) {
									String dec = scan.nextLine();
									if (dec.equalsIgnoreCase("y")) {
										moreDownloadRequest = true;
										properResponse = false;
									} else if (dec.equalsIgnoreCase("n")) {
										moreDownloadRequest = false;
										properResponse = false;
									} else {
										System.out.println("Please enter either Y or N.");
										properResponse = true;
									}
								}

							}

						}

					}
					if (ans == 3) {
						Map<Integer, MecFile> localFilesForSelection = new HashMap<Integer, MecFile>();
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to show all files available to load into Vertica.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getDestination() + " :");

							transmitterList.get(t).setSourceType(LOADING_SOURCE_TYPE);

							// add search patterns to file transmitter
							ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
									transmitterList.get(t).getRegistrarId());
							for (int m = 0; m < fileMapperList.size(); m++) {
								transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
							}

							ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
							searchPatternList = transmitterList.get(t).addDateToSearchPatternList();
							ArrayList<MecFile> files = new ArrayList<MecFile>();
							Set<MecFile> stagingFiles = new HashSet<MecFile>();

							for (int s = 0; s < searchPatternList.size(); s++) {
								System.out.println(transmitterList.get(t).getDestination());
								File dirToSearch = new File(transmitterList.get(t).getDestination());
								MecFileFilter filter = new MecFileFilter(searchPatternList.get(s).getFilePattern());
								File[] result = dirToSearch.listFiles(filter);
								Arrays.sort(result);

								if (result != null && result.length > 0) {
									for (File aFile : result) {
										System.out.println(searchPatternList.get(s).toString());
										if (aFile.getName().contains("_check")
												&& client.getId().equals("11022200790598")) {
											MecFile stagingFile = new MecFile(aFile.getName(),
													transmitterList.get(t).getDestination(),
													searchPatternList.get(s).getFileExtension(), 2,
													searchPatternList.get(s).getFileDelimiter(), true,
													"base_atlas_search_display_check",
													searchPatternList.get(s).getDatabaseSchema());
											stagingFiles.add(stagingFile);
										} else if (!aFile.getName().contains("_check")
												&& client.getId().equals("11022200790598")) {
											MecFile stagingFile = new MecFile(aFile.getName(),
													transmitterList.get(t).getDestination(),
													searchPatternList.get(s).getFileExtension(),
													searchPatternList.get(s).getSkipHeaderRows(),
													searchPatternList.get(s).getFileDelimiter(),
													searchPatternList.get(s).getKeepFile(),
													searchPatternList.get(s).getDatabaseTable(),
													searchPatternList.get(s).getDatabaseSchema());
											stagingFiles.add(stagingFile);
										} else {
											MecFile stagingFile = new MecFile(aFile.getName(),
													transmitterList.get(t).getDestination(),
													searchPatternList.get(s).getFileExtension(),
													searchPatternList.get(s).getSkipHeaderRows(),
													searchPatternList.get(s).getFileDelimiter(),
													searchPatternList.get(s).getKeepFile(),
													searchPatternList.get(s).getDatabaseTable(),
													searchPatternList.get(s).getDatabaseSchema());
											stagingFiles.add(stagingFile);
										}
									}
								}
							}
							files.addAll(stagingFiles);

							if (files.size() == 0) {
								System.out.println("No files found!");
								break;
							}

							for (int f = 0; f < files.size(); f++) {
								localFilesForSelection.put(f + 1, files.get(f));
							}

							Iterator<Entry<Integer, MecFile>> it = localFilesForSelection.entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry<Integer, MecFile> localFilePair = (Map.Entry<Integer, MecFile>) it.next();
								System.out.println(
										"(" + localFilePair.getKey() + ")	" + localFilePair.getValue().getFileName());
							}

							boolean moreDownloadRequest = true;
							while (moreDownloadRequest == true) {
								int fileKey;
								do {
									System.out.println("\nEnter number to select a file to load into Vertica: ");
									while (!scan.hasNextInt()) {
										System.out.println("please enter a valid number!");
										scan.next();
									}
									fileKey = scan.nextInt();
								} while (fileKey <= 0 || fileKey > localFilesForSelection.size());

								scan.nextLine();
								System.out
										.println("\nYou have selected " + localFilesForSelection.get(fileKey).toString()
												+ "\nDo you wish to continue (y/n)?");
								boolean condition = true;
								while (condition == true) {
									String dec = scan.nextLine();
									if (dec.equalsIgnoreCase("y")) {
										String[] fileTokens = localFilesForSelection.get(fileKey).getFileName()
												.split("\\.(?=[^\\.]+$)");
										String fileNameBase = fileTokens[0].trim();
										String fileExtension = fileTokens[1].trim();
										if (fileExtension.toUpperCase().equals("GZ")) {
											localFilesForSelection.get(fileKey).extract();
											localFilesForSelection.get(fileKey).setFileName(fileNameBase);
										} else if (fileExtension.toUpperCase().equals("ZIP")) {
											localFilesForSelection.get(fileKey).extract();
											localFilesForSelection.get(fileKey).setFileName(fileNameBase);
										}

										String loadShellScriptName = "vertica_data_load.sh";
										MecShellLoader processor = new MecShellLoader(SHELL_PATH,
												SHELL_SCRIPT_HOME + loadShellScriptName);
										processor.setSessionId(session.getSessionId());
										processor.setFileName(localFilesForSelection.get(fileKey).getFileName());
										processor.setClientName(client.getName());
										processor.setDataDirectory(transmitterList.get(t).getDestination());// localFilesForSelection.get(fileKey).getSourceDirectory());
										processor.setTable(localFilesForSelection.get(fileKey).getDatabaseTable());
										processor.setSchema(localFilesForSelection.get(fileKey).getDatabaseSchema());
										processor.setFileSchema(SCHEMA);
										processor.setSystemSchema(SYS_SCHEMA);
										processor.setSkipRows(localFilesForSelection.get(fileKey).getSkipHeaderRows());
										processor.setKeepFile(localFilesForSelection.get(fileKey).getKeepFile());
										processor.setFileDelimiter(
												localFilesForSelection.get(fileKey).getFileDelimiter());
										System.out.println(processor.toString());
										processor.runScript();
										condition = false;

										String zipFrom = transmitterList.get(t).getDestination() + fileNameBase + "."
												+ fileExtension;
										String zipTo = transmitterList.get(t).getDestination() + S3_DIRECTORY
												+ File.separator + fileNameBase + "." + fileExtension;
										Path movefrom = FileSystems.getDefault().getPath(zipFrom);
										Path target = FileSystems.getDefault().getPath(zipTo);

										try {
											Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
										} catch (IOException e) {
											System.err.println(e);
										}
									} else if (dec.equalsIgnoreCase("n")) {
										condition = false;
									} else {
										System.out.println("Please enter either Y or N.");
										condition = true;
									}
								}

								System.out.println("\nDo you wish to load another file into Vertica (y/n)?");
								boolean properResponse = true;
								while (properResponse == true) {
									String dec = scan.nextLine();
									if (dec.equalsIgnoreCase("y")) {
										moreDownloadRequest = true;
										properResponse = false;
									} else if (dec.equalsIgnoreCase("n")) {
										moreDownloadRequest = false;
										properResponse = false;
									} else {
										System.out.println("Please enter either Y or N.");
										properResponse = true;
									}
								}

							}

						}

					}
					if (ans == 4) {
						System.out.println("\nList of available processes for " + client.getName());

						ArrayList<String> processes = vertica.listBatchProcesses(SCHEMA, BATCH_TABLE, client.getId());

						Map<Integer, String> processMap = new HashMap<Integer, String>();
						for (int c = 0; c < processes.size(); c++) {
							processMap.put(c + 1, processes.get(c));
						}

						Iterator<Entry<Integer, String>> it = processMap.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Integer, String> clientListPair = (Map.Entry<Integer, String>) it.next();
							System.out.println(
									"(" + clientListPair.getKey() + ")	" + clientListPair.getValue().toString());
						}

						int clientKey;
						do {
							System.out.println("\nEnter number to select a process: ");
							while (!scan.hasNextInt()) {
								System.out.println("please enter a valid number!");
								scan.next();
							}
							clientKey = scan.nextInt();
						} while (clientKey <= 0 || clientKey > processMap.size());

						scan.nextLine();
						System.out.println("\nYou have selected " + processMap.get(clientKey).toString()
								+ "\nDo you wish to continue (y/n)?");
						boolean condition = true;
						ArrayList<MecVerticaBatch> batches = new ArrayList<MecVerticaBatch>();
						while (condition == true) {
							String dec = scan.nextLine();
							if (dec.equalsIgnoreCase("y")) {
								batches = vertica.listBatches(SCHEMA, BATCH_TABLE, client.getId(),
										processMap.get(clientKey));

								condition = false;
							} else if (dec.equalsIgnoreCase("n")) {
								condition = false;
							} else {
								System.out.println("Please enter either Y or N.");
								condition = true;
							}
						}

						if (batches.size() > 0) {
							System.out.println("\nList of available batches for " + client.getName());

							Map<Integer, MecVerticaBatch> batchMap = new LinkedHashMap<Integer, MecVerticaBatch>();
							for (int c = 0; c < batches.size(); c++) {
								batchMap.put(c + 1, batches.get(c));
							}

							Iterator<Entry<Integer, MecVerticaBatch>> batchIterator = batchMap.entrySet().iterator();
							while (batchIterator.hasNext()) {
								Map.Entry<Integer, MecVerticaBatch> clientListPair = (Map.Entry<Integer, MecVerticaBatch>) batchIterator
										.next();
								System.out.println(
										"(" + clientListPair.getKey() + ")	" + clientListPair.getValue().toString());
							}

							int batchKey;
							do {
								System.out.println(
										"\nWarning: The batch you select will be the starting point of the process."
												+ "\nEnter number to select a batch: ");
								while (!scan.hasNextInt()) {
									System.out.println("please enter a valid number!");
									scan.next();
								}
								batchKey = scan.nextInt();
							} while (batchKey <= 0 || batchKey > batchMap.size());

							scan.nextLine();
							System.out.println("\nYou have selected " + batchMap.get(batchKey).toString()
									+ "\nDo you wish to continue (y/n)?");
							boolean batchResponse = true;
							while (batchResponse == true) {
								String dec = scan.nextLine();
								if (dec.equalsIgnoreCase("y")) {
									List<Integer> keyList = new ArrayList<Integer>(batchMap.keySet());
									int firstBatchIndex = batchKey - 1;
									batchLoop: for (int b = firstBatchIndex; b < batchMap.size(); b++) {
										System.out.println("Processing batch \""
												+ batchMap.get(keyList.get(b)).getBatchName() + "\"");
										boolean breakLoop = false;
										int errorCode = 0;
										ArrayList<MecVerticaComponent> components = vertica.listComponents(SCHEMA,
												COMPONENT_TABLE, batchMap.get(keyList.get(b)).getBatchId());
										componentLoop: for (int c = 0; c < components.size(); c++) {
											System.out.println("\nProcessing component \""
													+ components.get(c).getComponentName() + "\"");
											String shellScriptName = "run_qa_sql.sh";
											MecShellReader processor = new MecShellReader(SHELL_PATH,
													SHELL_SCRIPT_HOME + shellScriptName);
											processor.setSessionId(session.getSessionId());
											processor.setComponentId(components.get(c).getComponentId());
											processor.setBatchId(components.get(c).getBatchId());
											processor.setOutputFile(components.get(c).isOutputFile());
											processor.runScript();

											if (!components.get(c).isOutputFile()) {
												errorCode = vertica.getComponentErrorCode(SCHEMA, SESSION_DETAIL_TABLE,
														components.get(c).getBatchId(), session.getSessionId(),
														components.get(c).getComponentId());

												if (errorCode == 1) {
													System.out.println("Result: PASS - \""
															+ components.get(c).getComponentName() + "\" Component ID= "
															+ components.get(c).getComponentId());
												} else if (errorCode == 9 || errorCode == 8) {
													System.out.println(
															"Error 9: unrecognized sql script. Please review the table \"md_component\" and/or"
																	+ " the \"sql_scripts\" folder");
													breakLoop = true;
													break componentLoop;
												} else if (errorCode != 1) {
													System.out.println("Result: FAIL - \""
															+ components.get(c).getComponentName() + "\" Component ID= "
															+ components.get(c).getComponentId());
													breakLoop = true;
												}
											}
											System.out.println("Break loop:" + breakLoop);
										}

										if (breakLoop == true) {
											System.out
													.println("\nBatch \"" + batchMap.get(keyList.get(b)).getBatchName()
															+ "\" failed. Process has ended.");
											break;
										}
									}

									batchResponse = false;
								} else if (dec.equalsIgnoreCase("n")) {
									batchResponse = false;
								} else {
									System.out.println("Please enter either Y or N.");
									batchResponse = true;
								}
							}
						} else {
							System.out.println("No batches selected for processing. Returning back to Main Menu.");
						}

					}
					if (ans == 5) {

						System.out.println("Uploading files to S3 bucket");
						MecS3 s3 = new MecS3();
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to load files from all active sources.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");
							s3.setBucketName(BUCKET_NAME);

							s3.setS3Prefix(transmitterList.get(t).getArchive());
							s3.uploadFiles(transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator);

							s3.setS3Prefix(transmitterList.get(t).getArchive() + SQL_OUTPUT_DIRECTORY + File.separator);
							s3.uploadFiles(transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator
									+ SQL_OUTPUT_DIRECTORY + File.separator);
						}

					}
					if (ans == 6) {

						MecS3 s3 = new MecS3();
						Map<Integer, MecFile> remoteFilesForSelection = new HashMap<Integer, MecFile>();
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to show files available from all active sources.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");
							s3.setBucketName(BUCKET_NAME);
							s3.setS3Prefix(transmitterList.get(t).getArchive());
							s3.setNfsDirectory(transmitterList.get(t).getDestination());

							ArrayList<MecFile> files = s3.listFiles();
							for (int f = 0; f < files.size(); f++) {
								remoteFilesForSelection.put(f + 1, files.get(f));
							}

							Iterator<Entry<Integer, MecFile>> it = remoteFilesForSelection.entrySet().iterator();
							while (it.hasNext()) {
								Map.Entry<Integer, MecFile> remoteFilePair = (Map.Entry<Integer, MecFile>) it.next();
								System.out.println("(" + remoteFilePair.getKey() + ")	"
										+ remoteFilePair.getValue().getFileName());
							}

							boolean moreDownloadRequest = true;
							while (moreDownloadRequest == true) {
								int fileKey;
								do {
									System.out.println("\nEnter number to select a file to download: ");
									while (!scan.hasNextInt()) {
										System.out.println("please enter a valid number!");
										scan.next();
									}
									fileKey = scan.nextInt();
								} while (fileKey <= 0 || fileKey > remoteFilesForSelection.size());

								scan.nextLine();
								System.out.println(
										"\nYou have selected " + remoteFilesForSelection.get(fileKey).toString()
												+ "\nDo you wish to continue (y/n)?");
								boolean condition = true;
								while (condition == true) {
									String dec = scan.nextLine();
									if (dec.equalsIgnoreCase("y")) {
										s3.downloadFile(remoteFilesForSelection.get(fileKey).getFileName());
										condition = false;
									} else if (dec.equalsIgnoreCase("n")) {
										condition = false;
									} else {
										System.out.println("Please enter either Y or N.");
										condition = true;
									}
								}

								System.out.println("\nDo you wish to download another file (y/n)?");
								boolean properResponse = true;
								while (properResponse == true) {
									String dec = scan.nextLine();
									if (dec.equalsIgnoreCase("y")) {
										moreDownloadRequest = true;
										properResponse = false;
									} else if (dec.equalsIgnoreCase("n")) {
										moreDownloadRequest = false;
										properResponse = false;
									} else {
										System.out.println("Please enter either Y or N.");
										properResponse = true;
									}
								}

							}

						}

					}
					if (ans == 7) {
						MecS3 s3 = new MecS3();
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to download files available from all active sources.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");
							s3.setBucketName(BUCKET_NAME);
							s3.setS3Prefix(transmitterList.get(t).getArchive());
							s3.setNfsDirectory(transmitterList.get(t).getDestination());

							ArrayList<MecFile> files = s3.listFiles();
							for (int f = 0; f < files.size(); f++) {
								s3.downloadFile(files.get(f).getFileName());
							}

						}
					}
					if (ans == 8) {

						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to show all files available to load into Vertica.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getDestination() + " :");

							transmitterList.get(t).setSourceType(LOADING_SOURCE_TYPE);

							// add search patterns to file transmitter
							ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
									transmitterList.get(t).getRegistrarId());
							for (int m = 0; m < fileMapperList.size(); m++) {
								transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
							}

							ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
							searchPatternList = transmitterList.get(t).addDateToSearchPatternList();
							ArrayList<MecFile> files = new ArrayList<MecFile>();
							Set<MecFile> stagingFiles = new HashSet<MecFile>();

							for (int s = 0; s < searchPatternList.size(); s++) {
								File dirToSearch = new File(transmitterList.get(t).getDestination());
								MecFileFilter filter = new MecFileFilter(searchPatternList.get(s).getFilePattern());
								File[] result = dirToSearch.listFiles(filter);
								Arrays.sort(result);

								if (result != null && result.length > 0) {
									for (File aFile : result) {
										if (aFile.getName().contains("_check")
												&& client.getId().equals("11022200790598")) {
											MecFile stagingFile = new MecFile(aFile.getName(),
													transmitterList.get(t).getDestination(),
													searchPatternList.get(s).getFileExtension(), 2,
													searchPatternList.get(s).getFileDelimiter(), true,
													"base_atlas_search_display_check",
													searchPatternList.get(s).getDatabaseSchema());
											stagingFiles.add(stagingFile);
										} else if (!aFile.getName().contains("_check")
												&& client.getId().equals("11022200790598")) {
											MecFile stagingFile = new MecFile(aFile.getName(),
													transmitterList.get(t).getDestination(),
													searchPatternList.get(s).getFileExtension(),
													searchPatternList.get(s).getSkipHeaderRows(),
													searchPatternList.get(s).getFileDelimiter(),
													searchPatternList.get(s).getKeepFile(),
													searchPatternList.get(s).getDatabaseTable(),
													searchPatternList.get(s).getDatabaseSchema());
											stagingFiles.add(stagingFile);
										} else {
											MecFile stagingFile = new MecFile(aFile.getName(),
													transmitterList.get(t).getDestination(),
													searchPatternList.get(s).getFileExtension(),
													searchPatternList.get(s).getSkipHeaderRows(),
													searchPatternList.get(s).getFileDelimiter(),
													searchPatternList.get(s).getKeepFile(),
													searchPatternList.get(s).getDatabaseTable(),
													searchPatternList.get(s).getDatabaseSchema());
											stagingFiles.add(stagingFile);
										}
									}
								}
							}
							files.addAll(stagingFiles);

							if (files.size() == 0) {
								System.out.println("No files found!");
								break;
							}

							Collections.sort(files, MecFile.fileComparator);

							for (int f = 0; f < files.size(); f++) {

								String[] fileTokens = files.get(f).getFileName().split("\\.(?=[^\\.]+$)");
								String fileNameBase = fileTokens[0].trim();
								String fileExtension = fileTokens[1].trim();

								String loadShellScriptName = "WWE_fact_load.sh";
								MecShellLoader processor = new MecShellLoader(SHELL_PATH,
										SHELL_SCRIPT_HOME + loadShellScriptName);
								processor.setSessionId(session.getSessionId());
								processor.setFileName(files.get(f).getFileName());
								processor.setClientName(client.getName());
								processor.setDataDirectory(transmitterList.get(t).getDestination());
								processor.setTable(files.get(f).getDatabaseTable());
								processor.setSchema(files.get(f).getDatabaseSchema());
								processor.setFileSchema(SCHEMA);
								processor.setSystemSchema(SYS_SCHEMA);
								processor.setSkipRows(files.get(f).getSkipHeaderRows());
								processor.setKeepFile(files.get(f).getKeepFile());
								processor.setFileDelimiter(files.get(f).getFileDelimiter());
								System.out.println(processor.toString());
								processor.runScript();

								String zipFrom = transmitterList.get(t).getDestination() + fileNameBase + "."
										+ fileExtension;
								String zipTo = transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator
										+ fileNameBase + "." + fileExtension;
								Path movefrom = FileSystems.getDefault().getPath(zipFrom);
								Path target = FileSystems.getDefault().getPath(zipTo);

								try {
									Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									System.err.println(e);
								}
							}

						}

					}

					// if (ans == 9) {
					//
					//// String zippedFile =
					// "NetworkImpression_869_03-01-2016.log.gz";
					//// MecFile gzFile = new MecFile (zippedFile);
					//// gzFile.setSourceDirectory(GEN_PATH);
					//// gzFile.extract();
					//
					// // create file string from parameters
					// String file = GEN_PATH + File.separator +
					// "NetworkImpression_869_03-01-2016.log";//"Activity_5281222_03-01-2016.log";
					// String convertedFile = GEN_PATH + File.separator +
					// "converted_NetworkImpression_869_03-01-2016.log";//"converted_Activity_5281222_03-01-2016.log";
					//
					// // create a dictionary of values from the credentials
					// file
					// Map<String, String> map = new HashMap<String, String>();
					//
					// File inFile = new File(file);
					// File outFile = new File(convertedFile);
					//
					//
					// BufferedReader in = null;
					// PrintWriter out = null;
					//
					// // read credentials file and assign values to variables
					// try {
					// in = new BufferedReader(new FileReader(inFile));
					// out = new PrintWriter(new BufferedWriter(new
					// FileWriter(outFile)));
					//
					// String line = in.readLine();
					//
					//
					// while (line != null) {
					// StringTokenizer t = new StringTokenizer(line,"þ");
					//// StringBuilder sb = new StringBuilder();
					// List<String> sb = new LinkedList();
					// while (t.hasMoreTokens()){
					// sb.add('"'+ t.nextToken().trim() + '"');
					// }
					// String newSb = String.join("|", sb);
					// out.println(newSb.toString());
					// line = in.readLine();
					//
					//
					//
					//// System.out.println(newSb);
					//// String[] tokens = line.split("\xfe");
					//// StringBuilder sb = new StringBuilder();
					//// for (int i=0;i<tokens.length;i++)
					////
					//// {
					////
					//// sb.append('"'+ tokens[i].trim() + '"');
					//// System.out.println(tokens[i].trim());
					//// }
					//// map.put(tokens[0].trim(), tokens[1].trim());
					//// out.println(newSb.toString());
					//// line = in.readLine();
					// }
					// in.close();
					//
					//
					// }
					//
					// catch (IOException io) {
					// System.out.println("An IO Exception occurred");
					// io.printStackTrace();
					// }
					// finally // finally always runs no matter what so close
					// the file here!
					// {
					// // close the file. Java is neurotic - it worried "but
					// what if it is already closed?" so needs another try/catch
					// try{
					// out.close();
					// }
					// catch (Exception e) {} // note the {} - means "do
					// nothing". I wanted it closed anyway.
					// }
					// }
					if (ans == 9) {
						try {
							String startDate = MecTimeMethods.getStartDate(64);
							String endDate = MecTimeMethods.getStartDate(1);

							ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
									MecTimeMethods.daysBetween(startDate, endDate));

							for (int t = 0; t < allDates.size(); t++) {
								String year = MecTimeMethods.getYear(allDates.get(t));
								String month = MecTimeMethods.getMonth(allDates.get(t));
								String day = MecTimeMethods.getDay(allDates.get(t));
								String loadShellScriptName = "wwe_opzipfile.sh";
								MecShellWweFeed processor = new MecShellWweFeed(SHELL_PATH,
										SHELL_SCRIPT_HOME + loadShellScriptName);
								processor.setRowId("1");
								processor.setFileDate(allDates.get(t).toString());
								;
								System.out.println(processor.toString());
								processor.runScript();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (ans == 10) {
						try {
							String startDate = MecTimeMethods.getStartDate(64);
							String endDate = MecTimeMethods.getStartDate(1);

							ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
									MecTimeMethods.daysBetween(startDate, endDate));

							for (int t = 0; t < allDates.size(); t++) {
								String year = MecTimeMethods.getYear(allDates.get(t));
								String month = MecTimeMethods.getMonth(allDates.get(t));
								String day = MecTimeMethods.getDay(allDates.get(t));
								String loadShellScriptName = "wwe_opzipfile.sh";
								MecShellWweFeed processor = new MecShellWweFeed(SHELL_PATH,
										SHELL_SCRIPT_HOME + loadShellScriptName);
								processor.setRowId("2");
								processor.setFileDate(allDates.get(t).toString());
								;
								System.out.println(processor.toString());
								processor.runScript();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (ans == 11) {
						try {
							String startDate = MecTimeMethods.getStartDate(7);
							String endDate = MecTimeMethods.getStartDate(1);

							ArrayList<String> allDates = MecTimeMethods.listDates(startDate,
									MecTimeMethods.daysBetween(startDate, endDate));

							for (int t = 0; t < allDates.size(); t++) {
								String year = MecTimeMethods.getYear(allDates.get(t));
								String month = MecTimeMethods.getMonth(allDates.get(t));
								String day = MecTimeMethods.getDay(allDates.get(t));
								String loadShellScriptName = "wwe_opzipfile.sh";
								MecShellWweFeed processor = new MecShellWweFeed(SHELL_PATH,
										SHELL_SCRIPT_HOME + loadShellScriptName);
								processor.setRowId("3");
								processor.setFileDate(allDates.get(t).toString());
								;
								System.out.println(processor.toString());
								processor.runScript();
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					if (ans == 12) {

						System.out.println("\nList of available processes for " + client.getName());

						ArrayList<String> processes = vertica.listBatchProcesses(SCHEMA, BATCH_TABLE, client.getId());

						Map<Integer, String> processMap = new HashMap<Integer, String>();
						for (int c = 0; c < processes.size(); c++) {
							processMap.put(c + 1, processes.get(c));
						}

						Iterator<Entry<Integer, String>> it = processMap.entrySet().iterator();
						while (it.hasNext()) {
							Map.Entry<Integer, String> clientListPair = (Map.Entry<Integer, String>) it.next();
							System.out.println(
									"(" + clientListPair.getKey() + ")	" + clientListPair.getValue().toString());
						}

						int clientKey;
						do {
							System.out.println("\nEnter number to select a process: ");
							while (!scan.hasNextInt()) {
								System.out.println("please enter a valid number!");
								scan.next();
							}
							clientKey = scan.nextInt();
						} while (clientKey <= 0 || clientKey > processMap.size());

						scan.nextLine();
						System.out.println("\nYou have selected " + processMap.get(clientKey).toString()
								+ "\nDo you wish to continue (y/n)?");
						boolean condition = true;
						ArrayList<MecVerticaBatch> batches = new ArrayList<MecVerticaBatch>();
						while (condition == true) {
							String dec = scan.nextLine();
							if (dec.equalsIgnoreCase("y")) {
								batches = vertica.listBatches(SCHEMA, BATCH_TABLE, client.getId(),
										processMap.get(clientKey));

								condition = false;
							} else if (dec.equalsIgnoreCase("n")) {
								condition = false;
							} else {
								System.out.println("Please enter either Y or N.");
								condition = true;
							}
						}

						int dateOption = 0;
						int mediaYear = 0;
						int mediaQuarter = 0;
						int mediaMonth = 0;
						Date feedStartDate = new Date();
						Date feedEndDate = new Date();

						System.out.println("\nDo you need to select a date range for your feed (y/n)?");

						boolean dateRangeRequired = true;
						while (dateRangeRequired == true) {
							String dec = scan.nextLine();
							if (dec.equalsIgnoreCase("y")) {
								System.out.println("========================================================");
								System.out.println("\nDate Options for Feed:");
								System.out.println("\n\t1. Media Quarter");
								System.out.println("\n\t2. Media Month");
								System.out.println("\n\t3. Custom Calendar Range");
								System.out.println("========================================================");
								do {
									System.out.println("\nEnter number to select a date option: ");
									while (!scan.hasNextInt()) {
										System.out.println("please enter a valid number!");
										scan.next();
									}
									dateOption = scan.nextInt();
								} while (dateOption <= 0 || dateOption > 3);

								if (dateOption == 1) {
									System.out.println("\nEnter media year:");
									mediaYear = scan.nextInt();
									System.out.println("\nEnter media quarter (1-4):");
									mediaQuarter = scan.nextInt();

									System.out.println(String.valueOf(mediaYear));
									System.out.println(String.valueOf(mediaQuarter));

									feedStartDate = MecTimeMethods.getStartDateFromMediaQuarter(vertica.getConnection(),
											TIME_TABLE, SCHEMA, mediaYear, mediaQuarter);
									feedEndDate = MecTimeMethods.getEndDateFromMediaQuarter(vertica.getConnection(),
											TIME_TABLE, SCHEMA, mediaYear, mediaQuarter);
								} else if (dateOption == 2) {
									System.out.println("\nEnter media year:");
									mediaYear = scan.nextInt();
									System.out.println("\nEnter media month (1-12):");
									mediaMonth = scan.nextInt();
									feedStartDate = MecTimeMethods.getStartDateFromMediaMonth(vertica.getConnection(),
											TIME_TABLE, SCHEMA, mediaYear, mediaMonth);
									feedEndDate = MecTimeMethods.getEndDateFromMediaMonth(vertica.getConnection(),
											TIME_TABLE, SCHEMA, mediaYear, mediaMonth);
								} else if (dateOption == 3) {
									System.out.println("\nEnter calendar start date (YYYY-MM-DD):");
									String calendarStartDate = scan.nextLine();
									feedStartDate = MecTimeMethods.formatDate(calendarStartDate);
									System.out.println("\nEnter calendar end date (YYYY-MM-DD):");
									String calendarEndDate = scan.nextLine();
									feedEndDate = MecTimeMethods.formatDate(calendarEndDate);
								}
								dateRangeRequired = false;
							} else if (dec.equalsIgnoreCase("n")) {
								dateRangeRequired = false;
							} else {
								System.out.println("Please enter either Y or N.");
								dateRangeRequired = true;
							}
						}

						session.setFeedStartDate((java.sql.Date) feedStartDate);
						session.setFeedEndDate((java.sql.Date) feedEndDate);

						System.out.println(session.toString());

						if (batches.size() > 0) {
							System.out.println("\nList of available batches for " + client.getName());

							Map<Integer, MecVerticaBatch> batchMap = new LinkedHashMap<Integer, MecVerticaBatch>();
							for (int c = 0; c < batches.size(); c++) {
								batchMap.put(c + 1, batches.get(c));
							}

							Iterator<Entry<Integer, MecVerticaBatch>> batchIterator = batchMap.entrySet().iterator();
							while (batchIterator.hasNext()) {
								Map.Entry<Integer, MecVerticaBatch> clientListPair = (Map.Entry<Integer, MecVerticaBatch>) batchIterator
										.next();
								System.out.println(
										"(" + clientListPair.getKey() + ")	" + clientListPair.getValue().toString());
							}

							int batchKey;
							do {
								System.out.println(
										"\nWarning: The batch you select will be the starting point of the process."
												+ "\nEnter number to select a batch: ");
								while (!scan.hasNextInt()) {
									System.out.println("please enter a valid number!");
									scan.next();
								}
								batchKey = scan.nextInt();
							} while (batchKey <= 0 || batchKey > batchMap.size());

							scan.nextLine();
							System.out.println("\nYou have selected " + batchMap.get(batchKey).toString()
									+ "\nDo you wish to continue (y/n)?");
							boolean batchResponse = true;
							while (batchResponse == true) {
								String dec = scan.nextLine();
								if (dec.equalsIgnoreCase("y")) {
									List<Integer> keyList = new ArrayList<Integer>(batchMap.keySet());
									int firstBatchIndex = batchKey - 1;
									batchLoop: for (int b = firstBatchIndex; b < batchMap.size(); b++) {
										System.out.println("Processing batch \""
												+ batchMap.get(keyList.get(b)).getBatchName() + "\"");
										boolean breakLoop = false;
										int errorCode = 0;
										ArrayList<MecVerticaComponent> components = vertica.listComponents(SCHEMA,
												COMPONENT_TABLE, batchMap.get(keyList.get(b)).getBatchId());
										componentLoop: for (int c = 0; c < components.size(); c++) {
											System.out.println("\nProcessing component \""
													+ components.get(c).getComponentName() + "\"");

											Date today = new Date();
											DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
											String fileDate = df.format(today);
											String fileYear = MecTimeMethods.getYear(fileDate);
											String fileMonth = MecTimeMethods.getMonth(fileDate);
											String fileDay = MecTimeMethods.getDay(fileDate);

											String feedFileName = components.get(c).getFileNamePattern()
													.replaceAll("\\%media_quarter", String.valueOf(mediaQuarter))
													.replaceAll("\\%media_year", String.valueOf(mediaYear))
													.replaceAll("\\%Y", fileYear).replaceAll("\\%m", fileMonth)
													.replaceAll("\\%d", fileDay);

											session.setFileName(feedFileName);
											System.out.println(session.toString());
											session.generateSessionIdForFeed(vertica.getConnection(), client.getName(),
													SESSION_TABLE, SCHEMA);

											String shellScriptName = "run_mm240_feed_new_format.sh";
											MecShellExporter processor = new MecShellExporter(SHELL_PATH,
													SHELL_SCRIPT_HOME + shellScriptName);
											processor.setSessionId(session.getSessionId());
											processor.setComponentId(components.get(c).getComponentId());
											processor.setBatchId(components.get(c).getBatchId());
											processor.setOutputFile(components.get(c).isOutputFile());
											processor.setOutputFileId(components.get(c).getFileOutputId());
											processor.setFileName(session.getFileName());
											processor.setStartDate(df.format(session.getFeedStartDate()));
											processor.setEndDate(df.format(session.getFeedEndDate()));
											System.out.println(processor.toString());
											processor.runScript();

											if (!components.get(c).isOutputFile()) {
												errorCode = vertica.getComponentErrorCode(SCHEMA, SESSION_DETAIL_TABLE,
														components.get(c).getBatchId(), session.getSessionId(),
														components.get(c).getComponentId());

												if (errorCode == 1) {
													System.out.println("Result: PASS - \""
															+ components.get(c).getComponentName() + "\" Component ID= "
															+ components.get(c).getComponentId());
												} else if (errorCode == 9 || errorCode == 8) {
													System.out.println(
															"Error 9: unrecognized sql script. Please review the table \"md_component\" and/or"
																	+ " the \"sql_scripts\" folder");
													breakLoop = true;
													break componentLoop;
												} else if (errorCode != 1) {
													System.out.println("Result: FAIL - \""
															+ components.get(c).getComponentName() + "\" Component ID= "
															+ components.get(c).getComponentId());
													breakLoop = true;
												}
											}
											System.out.println("Break loop:" + breakLoop);
										}

										if (breakLoop == true) {
											System.out
													.println("\nBatch \"" + batchMap.get(keyList.get(b)).getBatchName()
															+ "\" failed. Process has ended.");
											break;
										}
									}

									batchResponse = false;
								} else if (dec.equalsIgnoreCase("n")) {
									batchResponse = false;
								} else {
									System.out.println("Please enter either Y or N.");
									batchResponse = true;
								}
							}
						} else {
							System.out.println("No batches selected for processing. Returning back to Main Menu.");
						}

					}
					if (ans == 13) {
						MecS3 s3 = new MecS3();
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to download restated files from S3 bucket.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getSourceName() + " :");
							s3.setBucketName(BUCKET_NAME);
							s3.setS3Prefix(transmitterList.get(t).getArchive() + "RestatedData/");
							s3.setNfsDirectory(transmitterList.get(t).getDestination());

							ArrayList<MecFile> files = s3.listFiles();
							for (int f = 0; f < files.size(); f++) {
								s3.downloadFile(files.get(f).getFileName());
							}

						}
					}
					if (ans == 14) {
						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to show files available from all active sources.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nLoading files on " + transmitterList.get(t).getDestination() + " :");

							transmitterList.get(t).setSourceType(LOADING_SOURCE_TYPE);

							// add search patterns to file transmitter
							ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
									transmitterList.get(t).getRegistrarId());
							for (int m = 0; m < fileMapperList.size(); m++) {
								transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
							}

							ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
							searchPatternList = transmitterList.get(t).addDateToSearchPatternList();
							ArrayList<MecFile> filesToLoad = new ArrayList<MecFile>();
							Set<MecFile> stagingFiles = new HashSet<MecFile>();

							for (int s = 0; s < searchPatternList.size(); s++) {
								File dirToSearch = new File(transmitterList.get(t).getDestination());
								MecFileFilter filter = new MecFileFilter(searchPatternList.get(s).getFilePattern());
								File[] result = dirToSearch.listFiles(filter);
								Arrays.sort(result);

								if (result != null && result.length > 0) {
									for (File aFile : result) {
										MecFile stagingFile = new MecFile(aFile.getName(),
												transmitterList.get(t).getDestination(),
												searchPatternList.get(s).getFileExtension(),
												searchPatternList.get(s).getSkipHeaderRows(),
												searchPatternList.get(s).getFileDelimiter(),
												searchPatternList.get(s).getKeepFile(),
												searchPatternList.get(s).getDatabaseTable(),
												searchPatternList.get(s).getDatabaseSchema());
										stagingFiles.add(stagingFile);

									}
								}
							}
							filesToLoad.addAll(stagingFiles);

							if (filesToLoad.size() == 0) {
								System.out.println("No files found to load!");
							}

							Collections.sort(filesToLoad, MecFile.fileComparator);

							for (int f = 0; f < filesToLoad.size(); f++) {

								String[] fileTokens = filesToLoad.get(f).getFileName().split("\\.(?=[^\\.]+$)");
								String fileNameBase = fileTokens[0].trim();
								String fileExtension = fileTokens[1].trim();

								if (fileExtension.toUpperCase().equals("GZ")) {
									filesToLoad.get(f).extract();
									filesToLoad.get(f).setFileName(fileNameBase);
								} else if (fileExtension.toUpperCase().equals("ZIP")) {
									filesToLoad.get(f).extract();
									filesToLoad.get(f).setFileName(fileNameBase);
								}

								String loadShellScriptName = "vertica_data_load.sh";
								MecShellLoader processor = new MecShellLoader(SHELL_PATH,
										SHELL_SCRIPT_HOME + loadShellScriptName);
								processor.setSessionId(session.getSessionId());
								processor.setFileName(filesToLoad.get(f).getFileName());
								processor.setClientName(client.getName());
								processor.setDataDirectory(transmitterList.get(t).getDestination());
								processor.setTable(filesToLoad.get(f).getDatabaseTable());
								processor.setSchema(filesToLoad.get(f).getDatabaseSchema());
								processor.setFileSchema(SCHEMA);
								processor.setSystemSchema(SYS_SCHEMA);
								processor.setSkipRows(filesToLoad.get(f).getSkipHeaderRows());
								processor.setKeepFile(filesToLoad.get(f).getKeepFile());
								processor.setFileDelimiter(filesToLoad.get(f).getFileDelimiter());
								System.out.println(processor.toString());
								processor.runScript();

								String zipFrom = transmitterList.get(t).getDestination() + fileNameBase + "."
										+ fileExtension;
								String zipTo = transmitterList.get(t).getDestination() + S3_DIRECTORY + File.separator
										+ fileNameBase + "." + fileExtension;
								Path movefrom = FileSystems.getDefault().getPath(zipFrom);
								Path target = FileSystems.getDefault().getPath(zipTo);

								try {
									Files.move(movefrom, target, StandardCopyOption.REPLACE_EXISTING);
								} catch (IOException e) {
									System.err.println(e);
								}
							}

						}

					}
					if (ans == 15) {

						String biogenGeneralFilePath = File.separator + HOME_DIRECTORY + File.separator + "DFA"
								+ File.separator + "Biogen" + File.separator;
						String s3Prefix = "NetworkActivity";

						AWSCredentials credentials = null;
						try {
							credentials = new ProfileCredentialsProvider("cmi-biogen").getCredentials();
						} catch (Exception e) {
							throw new AmazonClientException(
									"Cannot load the credentials from the credential profiles file. "
											+ "Please make sure that your credentials file is at the correct "
											+ "location (\\home\\vadmin\\.aws\\credentials), and is in valid format.",
									e);
						}

						AmazonS3 s3 = new AmazonS3Client(credentials);

						try {
							ListObjectsRequest listObjectsRequest = new ListObjectsRequest()
									.withBucketName(CMI_BUCKET_NAME).withPrefix(s3Prefix);
							ObjectListing objectListing;

							do {
								objectListing = s3.listObjects(listObjectsRequest);
								for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {

									System.out.println("Downloading " + objectSummary.getKey());
									S3Object object = s3
											.getObject(new GetObjectRequest(CMI_BUCKET_NAME, objectSummary.getKey()));
									System.out.println("Content-Type: " + object.getObjectMetadata().getContentType());

									InputStream reader = new BufferedInputStream(object.getObjectContent());
									byte[] buf = new byte[1024];
									File file = new File(biogenGeneralFilePath + objectSummary.getKey());
									OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));

									int count;
									while ((count = reader.read(buf)) != -1) {
										if (Thread.interrupted()) {
											throw new InterruptedException();
										}
										writer.write(buf, 0, count);
									}

									writer.flush();
									writer.close();
									reader.close();
								}
								listObjectsRequest.setMarker(objectListing.getNextMarker());
							} while (objectListing.isTruncated());

						} catch (AmazonServiceException ase) {
							System.out.println("Caught an AmazonServiceException, which means your request made it "
									+ "to Amazon S3, but was rejected with an error response for some reason.");
							System.out.println("Error Message:    " + ase.getMessage());
							System.out.println("HTTP Status Code: " + ase.getStatusCode());
							System.out.println("AWS Error Code:   " + ase.getErrorCode());
							System.out.println("Error Type:       " + ase.getErrorType());
							System.out.println("Request ID:       " + ase.getRequestId());
						} catch (AmazonClientException ace) {
							System.out.println("Caught an AmazonClientException, which means the client encountered "
									+ "a serious internal problem while trying to communicate with S3, "
									+ "such as not being able to access the network.");
							System.out.println("Error Message: " + ace.getMessage());
						} catch (FileNotFoundException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
					if (ans == 16) {

						ArrayList<MecFileTransmitter> transmitterList = vertica.listFileAcquisitionPackages(SCHEMA,
								REGISTRAR_TABLE, client.getId());
						if (transmitterList.size() > 1) {
							System.out.println(
									"\nMultiple sources found. Proceeding to show all files available to load into Vertica.");
						}

						for (int t = 0; t < transmitterList.size(); t++) {
							System.out.println("\nListing files on " + transmitterList.get(t).getDestination() + " :");

							transmitterList.get(t).setSourceType(LOADING_SOURCE_TYPE);

							// add search patterns to file transmitter
							ArrayList<MecFileMapper> fileMapperList = vertica.listFileMaps(SCHEMA, PACKAGE_TABLE,
									transmitterList.get(t).getRegistrarId());
							for (int m = 0; m < fileMapperList.size(); m++) {
								transmitterList.get(t).addSearchPattern(fileMapperList.get(m));
							}

							ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
							searchPatternList = transmitterList.get(t).addDateToSearchPatternList();
							ArrayList<MecFile> files = new ArrayList<MecFile>();
							Set<MecFile> stagingFiles = new HashSet<MecFile>();

							for (int s = 0; s < searchPatternList.size(); s++) {
								File dirToSearch = new File(transmitterList.get(t).getDestination());
								MecFileFilter filter = new MecFileFilter(searchPatternList.get(s).getFilePattern());
								File[] result = dirToSearch.listFiles(filter);
								Arrays.sort(result);

								if (result != null && result.length > 0) {
									for (File aFile : result) {
										MecFile stagingFile = new MecFile(aFile.getName(),
												transmitterList.get(t).getDestination(),
												searchPatternList.get(s).getFileExtension(),
												searchPatternList.get(s).getSkipHeaderRows(),
												searchPatternList.get(s).getFileDelimiter(),
												searchPatternList.get(s).getKeepFile(),
												searchPatternList.get(s).getDatabaseTable(),
												searchPatternList.get(s).getDatabaseSchema());
										stagingFiles.add(stagingFile);

									}
								}

							}
							files.addAll(stagingFiles);

							if (files.size() == 0) {
								System.out.println("No files found!");
								break;
							}

							Collections.sort(files, MecFile.fileComparator);

							for (int f = 0; f < files.size(); f++) {

								String loadShellScriptName = "Biogen_fact_load.sh";
								MecShellLoader processor = new MecShellLoader(SHELL_PATH,
										SHELL_SCRIPT_HOME + loadShellScriptName);
								processor.setSessionId(session.getSessionId());
								processor.setFileName(files.get(f).getFileName());
								processor.setClientName(client.getName());
								processor.setDataDirectory(transmitterList.get(t).getDestination());
								processor.setTable(files.get(f).getDatabaseTable());
								processor.setSchema(files.get(f).getDatabaseSchema());
								processor.setFileSchema(SCHEMA);
								processor.setSystemSchema(SYS_SCHEMA);
								processor.setSkipRows(files.get(f).getSkipHeaderRows());
								processor.setKeepFile(files.get(f).getKeepFile());
								processor.setFileDelimiter(files.get(f).getFileDelimiter());
								System.out.println(processor.toString());
								processor.runScript();
							}

						}

					}

					if (ans == 17) {
						vertica.disconnect();
						System.out.println("Program has ended");
						System.exit(0);
					}
				} while (ans < 1 || ans > 17);

			}

		}
	}

	public static void menu() {
		System.out.println("\n**Main Menu**");
		System.out.println("1.	Select client");
		System.out.println("2.	Download file(s)");
		System.out.println("3.	Copy file(s) to Vertica");
		System.out.println("4.	Process data");
		System.out.println("5.	Upload to S3 bucket");
		System.out.println("6.	Download from S3 bucket");
		System.out.println("7.	***(WWE ONLY) Download ALL files from Client Folder in S3 bucket");
		System.out.println("8.	***(WWE ONLY) Copy ALL file(s) to Vertica");
		System.out.println("9.	***(WWE ONLY) Output File - Impression");
		System.out.println("10.	***(WWE ONLY) Output File - Click");
		System.out.println("11.	***(WWE ONLY) Output File - Activity");
		System.out.println("12.	Generate feed");
		System.out.println("13.	Download ALL restated files from Client Folder in S3 bucket");
		System.out.println("14.	Copy ALL file(s) to Vertica");
		System.out.println("15.	***(BIOGEN ONLY) Download ALL files from Client Folder in S3 bucket");
		System.out.println("16.	***(BIOGEN ONLY) Copy ALL file(s) to Vertica");
		System.out.println("17.	Exit");
	}

}