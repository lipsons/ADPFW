//package com.mecglobal.s3automation.java;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPFileFilter;

public class MecFileFilter implements FTPFileFilter, FilenameFilter{

	private String patternToSearchFor;
	
	public MecFileFilter(String patternToSearchFor) {
		this.patternToSearchFor = patternToSearchFor;
	}

	@Override
	public String toString() {
		return "MecFileFilter [patternToSearchFor=" + patternToSearchFor + "]";
	}

	public String getPatternToSearchFor() {
		return patternToSearchFor;
	}

	public void setPatternToSearchFor(String patternToSearchFor) {
		this.patternToSearchFor = patternToSearchFor;
	}

	@Override
	public boolean accept(FTPFile ftpFile) {
		String stringToSearch = ftpFile.getName();
		Pattern p = Pattern.compile(this.patternToSearchFor, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(stringToSearch);
		return (ftpFile.isFile() && m.matches());
	}

	@Override
	public boolean accept(File dirToSearch, String fileName) {
		String stringToSearch = fileName;
		Pattern p = Pattern.compile(this.patternToSearchFor, Pattern.CASE_INSENSITIVE);
		Matcher m = p.matcher(stringToSearch);
		return (m.matches());
//		return (m.matches() && !stringToSearch.endsWith(".zip")
//				&& !stringToSearch.endsWith(".gz"));
	}
}
