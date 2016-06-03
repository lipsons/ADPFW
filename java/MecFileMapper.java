//package com.mecglobal.s3automation.java;

import java.util.Comparator;

public class MecFileMapper {
	private String filePattern;
	private String sourceDirectory;
	private String fileExtension;
	private int skipHeaderRows;
	private String fileDelimiter;
	private boolean keepFile;
	private String databaseTable;
	private String databaseSchema;
	
	public MecFileMapper(String filePattern, String sourceDirectory, String fileExtension, int skipHeaderRows,
			String fileDelimiter, boolean keepFile, String databaseTable, String databaseSchema) {
		this.filePattern = filePattern;
		this.sourceDirectory = sourceDirectory;
		this.fileExtension = fileExtension;
		this.skipHeaderRows = skipHeaderRows;
		this.fileDelimiter = fileDelimiter;
		this.keepFile = keepFile;
		this.databaseTable = databaseTable;
		this.databaseSchema = databaseSchema;
	}
	
	public MecFileMapper(){
		
	}

	@Override
	public String toString() {
		return "MecFileMapper [filePattern=" + filePattern + ", sourceDirectory=" + sourceDirectory + ", fileExtension="
				+ fileExtension + ", skipHeaderRows=" + skipHeaderRows + ", fileDelimiter=" + fileDelimiter
				+ ", keepFile=" + keepFile + ", databaseTable=" + databaseTable + ", databaseSchema=" + databaseSchema
				+ "]";
	}

	public String getFilePattern() {
		return filePattern;
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}

	public String getSourceDirectory() {
		return sourceDirectory;
	}

	public void setSourceDirectory(String sourceDirectory) {
		this.sourceDirectory = sourceDirectory;
	}

	public String getFileExtension() {
		return fileExtension;
	}

	public void setFileExtension(String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public int getSkipHeaderRows() {
		return skipHeaderRows;
	}

	public void setSkipHeaderRows(int skipHeaderRows) {
		this.skipHeaderRows = skipHeaderRows;
	}

	public String getFileDelimiter() {
		return fileDelimiter;
	}

	public void setFileDelimiter(String fileDelimiter) {
		this.fileDelimiter = fileDelimiter;
	}

	public boolean getKeepFile() {
		return keepFile;
	}

	public void setKeepFile(boolean keepFile) {
		this.keepFile = keepFile;
	}

	public String getDatabaseTable() {
		return databaseTable;
	}

	public void setDatabaseTable(String databaseTable) {
		this.databaseTable = databaseTable;
	}

	public String getDatabaseSchema() {
		return databaseSchema;
	}

	public void setDatabaseSchema(String databaseSchema) {
		this.databaseSchema = databaseSchema;
	}
	
	public static Comparator<MecFileMapper> fileMapperComparator = new Comparator<MecFileMapper>() {

		@Override
		public int compare(MecFileMapper o1, MecFileMapper o2) {
			int value1 = o1.filePattern.compareToIgnoreCase(o2.filePattern);
			return value1;
		}
		
	};

}
