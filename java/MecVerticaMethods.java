//package com.mecglobal.s3automation.java;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

public class MecVerticaMethods {

//	final static String HOME_DIRECTORY = "raid0";
//	final static String PROGRAM_NAME = "Cross_Client_Resources";
//	final static String DATABASE_NAME = "vertica";
//	final static String FILE_NAME = "connect";
////	final static String SCHEMA = "Cross_Client_Resources";
////	final static String SCHEMA = "GoDaddyUS_Workbench";
////	final static String SCHEMA = "UnitedUS_Workbench";
//	final static String SCHEMA = "ATT_Digital_Workbench";
//	final static boolean STATUS = true;
//
//	public static ArrayList<MecFileTransmitter> getClientRegistrar(Connection conn) {
//
//		ArrayList<MecFileTransmitter> clientList = new ArrayList<MecFileTransmitter>();
//
//		Statement mySelect = null;
//		try {
//			mySelect = conn.createStatement();
//			ResultSet myResult = mySelect.executeQuery("SELECT distinct ID, CLIENT_NAME"
//					+ ", CLIENT_ID, MEDIA_TYPE, PACKAGE_TYPE, GENERAL_FILE_PATH, S3_PREFIX" + ", LB_WINDOW_DAYS"
//					+ " FROM "+ SCHEMA +".Dim_Registrar" + " WHERE IS_ACTIVE = true;");
//
//			while (myResult.next()) {
//				MecFileTransmitter client = new MecFileTransmitter();
//				client.setMediaType(myResult.getString("MEDIA_TYPE"));
//				client.setPackageType(myResult.getString("PACKAGE_TYPE"));
//				client.setGeneralFilePath(myResult.getString("GENERAL_FILE_PATH").replaceAll("//", File.separator));
//				client.setS3Prefix(myResult.getString("S3_PREFIX"));
//				client.setRegistrarId(myResult.getInt("ID"));
//				client.setLookbackWindow(myResult.getInt("LB_WINDOW_DAYS"));
//				clientList.add(client);
//			}
//			mySelect.close();
//		} catch (SQLException e) {
//
//			e.printStackTrace();
//		}
//		return clientList;
//
//	}
//
//	public static ArrayList<MecFile> getFilePackages(Connection conn, int registrarId)
//			throws SQLException {
//
//		ArrayList<MecFile> filePackages = new ArrayList<MecFile>();
//
//		PreparedStatement preparedStatement = null;
//
//		String selectSql = "SELECT distinct ID, DB_TABLE, DB_SCHEMA, SOURCE_DIRECTORY"
//				+ ", FILE_PATTERN, FILE_EXTENSION FROM "+ SCHEMA +".Dim_Package" 
//				+ " WHERE IS_ACTIVE = ? AND REGISTRAR_ID = ?"
//				+ " AND FILE_PATTERN <> '' AND SOURCE_DIRECTORY <> '';";
//
//		try {
//			preparedStatement = conn.prepareStatement(selectSql);
//
//			preparedStatement.setBoolean(1, STATUS);
//			preparedStatement.setInt(2, registrarId);
//
//			ResultSet myResult = preparedStatement.executeQuery();
//
//			while (myResult.next()) {
//				MecFile file = new MecFile();
//				file.setDbTable(myResult.getString("DB_TABLE"));
//				file.setDbSchema(myResult.getString("DB_SCHEMA"));
//				file.setFileRemoteDirectory(myResult.getString("SOURCE_DIRECTORY").replaceAll("//", File.separator));
//				file.setFilePattern(myResult.getString("FILE_PATTERN"));
//				file.setFileType(myResult.getString("FILE_EXTENSION"));
//				filePackages.add(file);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			if (preparedStatement != null) {
//				preparedStatement.close();
//			}
//		}
//		return filePackages;
//	}
//
//	public static ArrayList<MecFile> getFilePackagesForVertica(Connection conn, int registrarId)
//			throws SQLException {
//
//		ArrayList<MecFile> filePackages = new ArrayList<MecFile>();
//
//		PreparedStatement preparedStatement = null;
//
//		String selectSql = "SELECT distinct DB_TABLE, DB_SCHEMA,"
//				+ " FILE_PATTERN, FILE_EXTENSION, SKIP_HEADER_ROWS,"
//				+ " FILE_DELIMITER, KEEP_FILE"
//				+ " FROM "+ SCHEMA +".Dim_Package" 
//				+ " WHERE IS_ACTIVE = ? AND REGISTRAR_ID = ?"
//				+ " AND DB_TABLE <> '' AND DB_SCHEMA <> '';";
//
//		try {
//			preparedStatement = conn.prepareStatement(selectSql);
//
//			preparedStatement.setBoolean(1, STATUS);
//			preparedStatement.setInt(2, registrarId);
//
//			ResultSet myResult = preparedStatement.executeQuery();
//
//			while (myResult.next()) {
//				MecFile file = new MecFile();
//				file.setDbTable(myResult.getString("DB_TABLE"));
//				file.setDbSchema(myResult.getString("DB_SCHEMA"));
//				file.setFilePattern(myResult.getString("FILE_PATTERN"));
//				file.setFileType(myResult.getString("FILE_EXTENSION"));
//				file.setSkipHeaderRows(myResult.getInt("SKIP_HEADER_ROWS"));
//				file.setFileDelimiter(myResult.getString("FILE_DELIMITER"));
//				file.setKeepFile(myResult.getBoolean("KEEP_FILE"));
//				filePackages.add(file);
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			if (preparedStatement != null) {
//				preparedStatement.close();
//			}
//		}
//		return filePackages;
//	}
//	
//	public static HashMap<String, Integer> getLoadedFiles(Connection conn) throws SQLException {
//
//		HashMap<String, Integer> loadedFiles = new HashMap<String, Integer>();
//
//		PreparedStatement preparedStatement = null;
//
//		String selectSql = "SELECT distinct FILE_NAME as FILE_NAME_NEW, FILE_ID"
//				+ " FROM "+ SCHEMA +".Dim_File;";
//
//		try {
//			preparedStatement = conn.prepareStatement(selectSql);
//
//			ResultSet myResult = preparedStatement.executeQuery();
//
//			while (myResult.next()) {
//				loadedFiles.put(myResult.getString("FILE_NAME_NEW"), myResult.getInt("FILE_ID"));
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		} finally {
//			if (preparedStatement != null) {
//				preparedStatement.close();
//			}
//		}
//		return loadedFiles;
//	}
}
