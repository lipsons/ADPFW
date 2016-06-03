//package com.mecglobal.s3automation.java;

public class MecShellLoader extends MecShellScript{

	private String clientName;
	private String fileName;
	private String dataDirectory;
	private String table;
	private String schema;
	private String fileSchema;
	private String systemSchema;
	private int skipRows;
	private Boolean keepFile;
	private String fileDelimiter;
	private String sessionId;
	
	public MecShellLoader(String sh, String sc, String c, String f, String d, String t
			, String sm, String fs, String ss, int sk, Boolean k, String fd) {
		shellPath = sh;
		scriptPath = sc;
		clientName = c;
		fileName = f;
		dataDirectory = d;
		table = t;
		schema = sm;
		fileSchema = fs;
		systemSchema = ss;
		skipRows = sk;
		keepFile = k;
		fileDelimiter = fd;
		
	}
	
	public MecShellLoader(String sh, String sc) {
		shellPath = sh;
		scriptPath = sc;
	}
	
	public String toString() {
		return "MecDataLoader [clientName=" + clientName + ", fileName=" + fileName + ", dataDirectory=" + dataDirectory
				+ ", table=" + table + ", schema=" + schema + ", fileSchema=" + fileSchema + ", systemSchema="
				+ systemSchema + ", skipRows=" + skipRows + ", keepFile=" + keepFile + ", fileDelimiter="
				+ fileDelimiter + ", shellPath=" + shellPath + ", scriptPath=" + scriptPath + "]";
	}

	public String getClientName() {
		return clientName;
	}

	public void setClientName(String clientName) {
		this.clientName = clientName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDataDirectory() {
		return dataDirectory;
	}

	public void setDataDirectory(String dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getSchema() {
		return schema;
	}

	public void setSchema(String schema) {
		this.schema = schema;
	}

	public String getFileSchema() {
		return fileSchema;
	}

	public void setFileSchema(String fileSchema) {
		this.fileSchema = fileSchema;
	}

	public String getSystemSchema() {
		return systemSchema;
	}

	public void setSystemSchema(String systemSchema) {
		this.systemSchema = systemSchema;
	}

	public int getSkipRows() {
		return skipRows;
	}

	public void setSkipRows(int skipRows) {
		this.skipRows = skipRows;
	}

	public Boolean getKeepFile() {
		return keepFile;
	}

	public void setKeepFile(Boolean keepFile) {
		this.keepFile = keepFile;
	}

	public String getFileDelimiter() {
		return fileDelimiter;
	}

	public void setFileDelimiter(String fileDelimiter) {
		this.fileDelimiter = fileDelimiter;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void runScript() {
		String nfsDirectory = this.dataDirectory.replaceFirst("raid0", "nfs");
		String keepFileAsText = null; 
		if (this.keepFile == true){
			keepFileAsText = "1";
		} else {
			keepFileAsText = "0";
		}
		System.out.println(this.clientName + "|" + this.fileName + "|" + this.dataDirectory + "|" + nfsDirectory
				 + "|" + this.schema + "|" + this.table + "|" + this.fileSchema + "|" + this.systemSchema
				 + "|" + String.valueOf(this.skipRows) + "|" + keepFileAsText + "|" + this.fileDelimiter + "|" + this.sessionId);
		try {
			ProcessBuilder pb = new ProcessBuilder(shellPath, scriptPath, this.clientName
					, this.fileName, this.dataDirectory, nfsDirectory, this.schema, this.table, this.fileSchema, this.systemSchema
					, String.valueOf(this.skipRows), keepFileAsText, this.fileDelimiter, this.sessionId);
			pb.inheritIO();
			Process p = pb.start();
			int errCode = p.waitFor();
			System.out.println("Echo command executed, any error? " + (errCode == 0 ? "No" : "Yes"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
