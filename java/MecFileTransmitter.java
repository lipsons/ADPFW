//package com.mecglobal.s3automation.java;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MecFileTransmitter extends MecTimeMethods implements MecFileAcquirer{

	private String sourceName;
	private String sourceType;
	private String destination;
	private String archive;
	private int lookbackWindow;
	private ArrayList<MecFileMapper> searchPatternList; 
	private String clientName;
	private int registrarId;
	private ArrayList<MecFile> fileList;
	
	public MecFileTransmitter(String sn, String st, String d, String a, int l, String c, int r) {
		this.sourceName = sn;
		this.sourceType = st;
		this.destination = d;
		this.archive = a;
		this.lookbackWindow = l;
		this.searchPatternList = new ArrayList<MecFileMapper>();
		this.clientName = c;
		this.registrarId = r;
		this.fileList = new ArrayList<MecFile>();
	}

	public MecFileTransmitter(String sourceType, String destination, String clientName) {
		this.sourceType = sourceType;
		this.destination = destination;
		this.clientName = clientName;
		this.searchPatternList = new ArrayList<MecFileMapper>();
		this.fileList = new ArrayList<MecFile>();
	}

	@Override
	public String toString() {
		return "MecFileTransmitter [Registrar ID=" + registrarId + ", Client=" + clientName + ", Source=" 
				+ sourceName + ", Type=" + sourceType + ", Destination Path=" + destination + ", Archive Path=" 
				+ archive + ", Lookback Window=" + lookbackWindow + "]";
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public String getArchive() {
		return archive;
	}

	public void setArchive(String archive) {
		this.archive = archive;
	}

	public int getLookbackWindow() {
		return lookbackWindow;
	}

	public void setLookbackWindow(int lookbackWindow) {
		this.lookbackWindow = lookbackWindow;
	}
	
	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}
	
	public int getRegistrarId() {
		return registrarId;
	}

	public void setRegistrarId(int registrarId) {
		this.registrarId = registrarId;
	}

	public ArrayList<MecFileMapper> getSearchPatternList() {
		return searchPatternList;
	}

	public ArrayList<MecFile> getFileList() {
		return fileList;
	}

	public void addSearchPattern(MecFileMapper fileMapper) {
		this.searchPatternList.add(fileMapper);
	}
	
	public void addFile(MecFile file) {
		this.fileList.add(file);
	}
	
	public ArrayList<MecFileMapper> addDateToSearchPatternList() {
		ArrayList<MecFileMapper> newSearchPatternList = new ArrayList<MecFileMapper>();
		try {
			String startDate = getStartDate(this.lookbackWindow);
			String endDate = getStartDate(1);

			ArrayList<String> allDates = listDates(startDate, daysBetween(startDate, endDate));
			
			for (int s = 0; s < this.searchPatternList.size(); s++) {


				for (int t = 0; t < allDates.size(); t++) {
					String year = getYear(allDates.get(t));
					String month = getMonth(allDates.get(t));
					String day = getDay(allDates.get(t));

					String searchDate = year + "-?" + month + "-?" + day;
					String yearAbbr = year.substring(2);
					String searchPatternWithDate = this.searchPatternList.get(s).getFilePattern().replaceAll("\\%dt", searchDate)
							.replaceAll("\\%Y", year).replaceAll("\\%m", month).replaceAll("\\%d", day)
							.replaceAll("\\%y", yearAbbr);

					searchPatternWithDate = searchPatternWithDate.replaceAll("\\*", ".*");

//					if (this.sourceType=="ftp") {
//						searchPatternWithDate = searchPatternWithDate.replaceAll("\\*", ".*");
//						System.out.println(searchPatternWithDate);
//					} else if (this.sourceType=="baseLoad") {
//						searchPatternWithDate = searchPatternWithDate.replaceAll("\\*", ".*");
//						System.out.println(searchPatternWithDate);
//						String fileExtension = "." + this.searchPatternList.get(s).getFileExtension();
//						searchPatternWithDate = searchPatternWithDate.replaceAll("\\*", ".*")
//								.replaceAll(fileExtension,"");
//					}
					MecFileMapper newMapper = new MecFileMapper(searchPatternWithDate
							, this.searchPatternList.get(s).getSourceDirectory(), this.searchPatternList.get(s).getFileExtension()
							, this.searchPatternList.get(s).getSkipHeaderRows(), this.searchPatternList.get(s).getFileDelimiter()
							, this.searchPatternList.get(s).getKeepFile(), this.searchPatternList.get(s).getDatabaseTable()
							, this.searchPatternList.get(s).getDatabaseSchema());
					newSearchPatternList.add(newMapper);
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return newSearchPatternList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<MecFile> listFiles() {
		ArrayList<MecFile> files = new ArrayList<MecFile>();
		try {
			ArrayList<MecFileMapper> searchPatternList = new ArrayList<MecFileMapper>();
			searchPatternList = addDateToSearchPatternList();
			
			for (int s=0; s < searchPatternList.size(); s++) {
				ArrayList<MecFile> filesForStaging = new ArrayList<MecFile>();
				
				String initCapSourceType = this.sourceType.substring(0,1).toUpperCase().
						concat(this.sourceType.substring(1,this.sourceType.length()).toLowerCase());
//				Class<?> cls = Class.forName("com.mecglobal.s3automation.java.Mec" + initCapSourceType);
				Class<?> cls = Class.forName("Mec" + initCapSourceType);
				Object obj = cls.newInstance();
				
				Method method = cls.getDeclaredMethod("setGeneralFilePath", String.class);
				method.invoke(obj, this.destination);
				
				method = cls.getDeclaredMethod("setProtocol", String.class);
				method.invoke(obj, this.sourceType);
				
				method = cls.getDeclaredMethod("setClientName", String.class);
				method.invoke(obj, this.clientName);
				
				method = cls.getDeclaredMethod("setRemoteDirectory", String.class);
				method.invoke(obj, searchPatternList.get(s).getSourceDirectory());
				
				method = cls.getDeclaredMethod("setFilePattern", String.class);
				method.invoke(obj, searchPatternList.get(s).getFilePattern());
				
				method = cls.getDeclaredMethod("connect");
				method.invoke(obj);			
				
				method = cls.getDeclaredMethod("listFiles");
//				files.addAll((ArrayList<MecFile>) method.invoke(obj));
				filesForStaging.addAll((ArrayList<MecFile>) method.invoke(obj));
				
				for (int f=0; f<filesForStaging.size(); f++){
					if (this.sourceType=="baseLoad") {
						MecFile stagingFile = new MecFile(filesForStaging.get(f).getFileName()
								, filesForStaging.get(f).getSourceDirectory(), searchPatternList.get(s).getFileExtension()
								, searchPatternList.get(s).getSkipHeaderRows(), searchPatternList.get(s).getFileDelimiter()
								, searchPatternList.get(s).getKeepFile(), searchPatternList.get(s).getDatabaseTable()
								, searchPatternList.get(s).getDatabaseSchema());
						files.add(stagingFile);
					} else {
						MecFile stagingFile = new MecFile(filesForStaging.get(f).getFileName()
								, searchPatternList.get(s).getSourceDirectory(), searchPatternList.get(s).getFileExtension()
								, searchPatternList.get(s).getSkipHeaderRows(), searchPatternList.get(s).getFileDelimiter()
								, searchPatternList.get(s).getKeepFile(), searchPatternList.get(s).getDatabaseTable()
								, searchPatternList.get(s).getDatabaseSchema());
						files.add(stagingFile);
					}
				}
				
				method = cls.getDeclaredMethod("disconnect");
				method.invoke(obj);	
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	
		return files;
	}

	@Override
	public void downloadFile(String fileName) {

		try {
			ArrayList<MecFile> remoteFiles = this.fileList;
			for (MecFile file : remoteFiles) {

				if (file.getFileName() == fileName) {

					String initCapSourceType = this.sourceType.substring(0, 1).toUpperCase()
							.concat(this.sourceType.substring(1, this.sourceType.length()).toLowerCase());
//					Class<?> cls = Class.forName("com.mecglobal.s3automation.java.Mec" + initCapSourceType);
					Class<?> cls = Class.forName("Mec" + initCapSourceType);
					Object obj = cls.newInstance();

					Method method = cls.getDeclaredMethod("setGeneralFilePath", String.class);
					method.invoke(obj, this.destination);

					method = cls.getDeclaredMethod("setProtocol", String.class);
					method.invoke(obj, this.sourceType);

					method = cls.getDeclaredMethod("setClientName", String.class);
					method.invoke(obj, this.clientName);

					method = cls.getDeclaredMethod("setRemoteDirectory", String.class);
					method.invoke(obj, file.getSourceDirectory());

					method = cls.getDeclaredMethod("connect");
					method.invoke(obj);

					method = cls.getDeclaredMethod("downloadFile", String.class);
					method.invoke(obj, file.getFileName());

					method = cls.getDeclaredMethod("disconnect");
					method.invoke(obj);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public void downloadAllFiles() {

		try {
			ArrayList<MecFile> remoteFiles = this.fileList;
			Collections.sort(remoteFiles, MecFile.fileComparator);
			
			if (remoteFiles.size()==0) {
				System.out.println("No files found!");
			}
			
			for (MecFile file : remoteFiles) {

					String initCapSourceType = this.sourceType.substring(0, 1).toUpperCase()
							.concat(this.sourceType.substring(1, this.sourceType.length()).toLowerCase());
					Class<?> cls = Class.forName("Mec" + initCapSourceType);
					Object obj = cls.newInstance();

					Method method = cls.getDeclaredMethod("setGeneralFilePath", String.class);
					method.invoke(obj, this.destination);

					method = cls.getDeclaredMethod("setProtocol", String.class);
					method.invoke(obj, this.sourceType);

					method = cls.getDeclaredMethod("setClientName", String.class);
					method.invoke(obj, this.clientName);

					method = cls.getDeclaredMethod("setRemoteDirectory", String.class);
					method.invoke(obj, file.getSourceDirectory());

					method = cls.getDeclaredMethod("connect");
					method.invoke(obj);

					method = cls.getDeclaredMethod("downloadFile", String.class);
					method.invoke(obj, file.getFileName());

					method = cls.getDeclaredMethod("disconnect");
					method.invoke(obj);
				
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	
	public static Comparator<MecFileTransmitter> transmitterComparator = new Comparator<MecFileTransmitter>() {

		@Override
		public int compare(MecFileTransmitter o1, MecFileTransmitter o2) {
			int value1 = o1.sourceName.compareToIgnoreCase(o2.sourceName);
			return value1;
		}
		
	};
}
