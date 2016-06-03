//package com.mecglobal.s3automation.java;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MecFile extends MecFileMapper implements MecFileExtractor{

	private String fileName;
	private Date processedDate;
	private long fileSize;

	public MecFile(String f) {
		super();
		this.fileName = f;
	}
	
	public MecFile(String f, long s){
		super();
		this.fileName = f;
		this.fileSize = s;
	}

	public MecFile(String f, String s, String fe, int sh, String fd, boolean k, String dt, String ds) {
		super();
		this.fileName = f;
		super.setSourceDirectory(s);
		super.setFileExtension(fe);
		super.setSkipHeaderRows(sh);
		super.setFileDelimiter(fd);
		super.setKeepFile(k);
		super.setDatabaseTable(dt);
		super.setDatabaseSchema(ds);
	}

	public MecFile(String f, String r) {
		super();
		this.fileName = f;
		super.setSourceDirectory(r);
	}

	@Override
	public String toString() {
		return "MecFile [fileName=" + fileName + " " + super.toString() + "]";
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Date getProcessedDate() {
		return processedDate;
	}

	public void setProcessedDate(Date processedDate) {
		this.processedDate = processedDate;
	}
	
	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	@Override
	public void extract() {
		String [] fileTokens = this.fileName.split("\\.(?=[^\\.]+$)");
		String fileNameBase = fileTokens[0].trim();
		String fileExtension = fileTokens[1].trim();
		String filePath = super.getSourceDirectory() + this.fileName;
		String extractFilePath = super.getSourceDirectory() + fileNameBase;
		
		if (fileExtension.toUpperCase().equals("GZ")){
			System.out.println("GZ file recognized. Proceeding to unzip file...");
			
			byte[] bytesIn = new byte[2048];
			int read = 0;
			try {
				FileInputStream fileIn = new FileInputStream(filePath);

				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(extractFilePath));

				GZIPInputStream inputStreamGzip = new GZIPInputStream(fileIn);

				while ((read = inputStreamGzip.read(bytesIn)) != -1) {
					bos.write(bytesIn, 0, read);
				}
				inputStreamGzip.close();
				bos.close();
				System.out.println(filePath + " was decompressed successfully!");
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (fileExtension.toUpperCase().equals("ZIP")) {
			System.out.println("ZIP file recognized. Proceeding to unzip file...");

			try {
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(extractFilePath));

				FileInputStream fileIn = new FileInputStream(filePath);

				ZipInputStream inputStream = new ZipInputStream(fileIn);

				ZipEntry entry;
				
				while ((entry = inputStream.getNextEntry()) != null) {
					// System.out.println("entry: " + entry.getName() + ", " +
					// entry.getSize());
					String unZipFileName = entry.getName();
					File newFile = new File(super.getSourceDirectory() + unZipFileName);

					// System.out.println("file unzip : " + filePath);
					new File(newFile.getParent()).mkdirs();

					byte[] bytesArray = new byte[4096];
					int len;
					while ((len = inputStream.read(bytesArray)) > 0) {
						bos.write(bytesArray, 0, len);

					}
					bos.close();
					entry = inputStream.getNextEntry();
				}
				inputStream.closeEntry();
				inputStream.close();

				System.out.println(filePath + " was decompressed successfully!");

			} catch (IOException e) {
				e.printStackTrace();
			}
	
		}
	}

	public static Comparator<MecFile> fileComparator = new Comparator<MecFile>() {

		@Override
		public int compare(MecFile o1, MecFile o2) {
			int value1 = o1.fileName.compareToIgnoreCase(o2.fileName);
			return value1;
		}
		
	};
}
