//package com.mecglobal.s3automation.java;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MecVertica implements MecServer {

	private Connection connection;
	private String homeDirectory;
	private String programName;
	private String databaseName;
	private String fileName;

	public MecVertica(String h, String p, String d, String f) {
		homeDirectory = h;
		programName = p;
		databaseName = d;
		fileName = f;
	}

	public Connection getConnection() {
		return connection;
	}

	public String getHomeDirectory() {
		return homeDirectory;
	}

	public void setHomeDirectory(String homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	public String getProgramName() {
		return programName;
	}

	public void setProgramName(String programName) {
		this.programName = programName;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void setDatabaseName(String databaseName) {
		this.databaseName = databaseName;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void connect() {
		System.out.println("Connecting to Vertica");

		// credentials file location
		String file = File.separator + homeDirectory + File.separator + programName + File.separator + "."
				+ databaseName + File.separator + fileName;
		String url = null;
		String user = null;
		String password = null;

		// create a dictionary of values from the credentials file
		Map<String, String> map = new HashMap<String, String>();

		BufferedReader in = null;

		// read credentials file and assign values to variables
		try {
			in = new BufferedReader(new FileReader(file));

			String line = in.readLine();

			while (line != null) {
				String[] tokens = line.split("=");
				map.put(tokens[0].trim(), tokens[1].trim());
				line = in.readLine();
			}
			in.close();

			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();

				if (pair.getKey().equals("url")) {
					url = (String) pair.getValue();
				} else if (pair.getKey().equals("user")) {
					user = (String) pair.getValue();
				} else if (pair.getKey().equals("pass")) {
					password = (String) pair.getValue();
				} else {
					System.out.println("The key \"" + pair.getKey() + "\" is unknown at " + file);
				}

			}

		} catch (IOException io) {
			System.out.println("An IO Exception occurred");
			io.printStackTrace();
		}

		try {

			Class.forName("com.vertica.jdbc.Driver");

		} catch (ClassNotFoundException e) {

			// Could not find the driver class. Likely an issue
			// with finding the .jar file.

			System.err.println("Could not find the JDBC driver class.");

			e.printStackTrace();

		}

		try {
			this.connection = (DriverManager.getConnection(url, user, password));
		} catch (SQLException e) {
			System.err.println("Could not connect to the database.\n");
			e.printStackTrace();

		}
	}

	public void disconnect() {

		System.out.println("Disconnecting from Vertica");
		try {
			if (!this.connection.isClosed()) {
				this.connection.close();
			}
		} catch (SQLException e) {

			e.printStackTrace();
		}

	}
	
	public ArrayList<MecClient> listClients(String databaseSchema, String databaseTable){
		ArrayList<MecClient> clientList = new ArrayList<MecClient>();
		
		PreparedStatement selectStatement = null;

		String sqlSelectMdClient = "SELECT DISTINCT client_name, client_id" + " FROM " + databaseSchema
				+ "." + databaseTable + ";";

		try {
			selectStatement = connection.prepareStatement(sqlSelectMdClient);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				MecClient client = new MecClient(myResult.getString("client_name")
						, myResult.getString("client_id"));
				clientList.add(client);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(clientList, MecClient.clientComparator);
		return clientList;
		
	}

	public ArrayList<MecFileTransmitter> listFileAcquisitionPackages(String databaseSchema, String databaseTable
			, String clientId){
		ArrayList<MecFileTransmitter> fileTransmitterList = new ArrayList<MecFileTransmitter>();
		
		PreparedStatement selectStatement = null;

		String sqlSelectDimRegistrar = "SELECT DISTINCT media_type, package_type, general_file_path"
				+ ", s3_prefix, lb_window_days, client_name, id"
				+ " FROM " + databaseSchema	+ "." + databaseTable 
				+ " WHERE is_active = true and client_id = ?;";

		try {
			selectStatement = connection.prepareStatement(sqlSelectDimRegistrar);
			
			selectStatement.setString(1, clientId);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				MecFileTransmitter fileTransmitter = new MecFileTransmitter(
						myResult.getString("media_type"), myResult.getString("package_type")
						, myResult.getString("general_file_path"), myResult.getString("s3_prefix")
						, myResult.getInt("lb_window_days"), myResult.getString("client_name")
						, myResult.getInt("id"));
				fileTransmitterList.add(fileTransmitter);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(fileTransmitterList, MecFileTransmitter.transmitterComparator);
		return fileTransmitterList;
	}
	
	public ArrayList<MecFileMapper> listFileMaps(String databaseSchema, String databaseTable
			, int registrarId) {
		ArrayList<MecFileMapper> fileMapperList = new ArrayList<MecFileMapper>();
		
		PreparedStatement selectStatement = null;

		String sqlSelectDimPackage = "SELECT DISTINCT file_pattern, source_directory, file_extension"
				+ ", skip_header_rows, file_delimiter, keep_file, db_table, db_schema"
				+ " FROM " + databaseSchema	+ "." + databaseTable 
				+ " WHERE is_active = true and registrar_id = ?;";

		try {
			selectStatement = connection.prepareStatement(sqlSelectDimPackage);
			
			selectStatement.setInt(1, registrarId);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				MecFileMapper fileMapper = new MecFileMapper(
						myResult.getString("file_pattern"), myResult.getString("source_directory")
						, myResult.getString("file_extension"), myResult.getInt("skip_header_rows")
						, myResult.getString("file_delimiter"), myResult.getBoolean("keep_file")
						, myResult.getString("db_table"), myResult.getString("db_schema"));
				fileMapperList.add(fileMapper);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(fileMapperList, MecFileMapper.fileMapperComparator);
		return fileMapperList;
	}
	
	public ArrayList<String> listBatchProcesses(String databaseSchema, String databaseTable
			, String clientId){
		ArrayList<String> batchList = new ArrayList<String>();
		
		PreparedStatement selectStatement = null;

		String sqlSelectMdClient = "SELECT DISTINCT process_name"
				+ " FROM " + databaseSchema	+ "." + databaseTable 
				+ " WHERE client_id = ?;";

		try {
			selectStatement = connection.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setString(1, clientId);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				batchList.add(myResult.getString("process_name"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(batchList);
		return batchList;
	}
	
	public ArrayList<MecVerticaBatch> listBatches(String databaseSchema, String databaseTable
			, String clientId, String processName){
		ArrayList<MecVerticaBatch> batchList = new ArrayList<MecVerticaBatch>();
		
		PreparedStatement selectStatement = null;

		String sqlSelectMdClient = "SELECT DISTINCT id, batch_name, process_name"
				+ ", execution_order, client_id" 
				+ " FROM " + databaseSchema	+ "." + databaseTable 
				+ " WHERE client_id = ? and process_name = ?;";

		try {
			selectStatement = connection.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setString(1, clientId);
			selectStatement.setString(2, processName);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				MecVerticaBatch batch = new MecVerticaBatch(myResult.getInt("id")
						, myResult.getString("batch_name"), myResult.getString("process_name")
						, myResult.getInt("execution_order"), myResult.getString("client_id"));
				batchList.add(batch);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(batchList, MecVerticaBatch.batchComparator);
		return batchList;
	}
	
	public ArrayList<MecVerticaComponent> listComponents(String databaseSchema, String databaseTable
			, int batchId){
		ArrayList<MecVerticaComponent> componentList = new ArrayList<MecVerticaComponent>();
		
		PreparedStatement selectStatement = null;

		String sqlSelectMdClient = "SELECT DISTINCT id, component, batch_id"
				+ ", sql_script_path, execution_order, output_file, file_name_pattern"
				+ ", file_output_id" 
				+ " FROM " + databaseSchema	+ "." + databaseTable 
				+ " WHERE batch_id = ?"
				+ "	ORDER BY execution_order;";

		try {
			selectStatement = connection.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setInt(1, batchId);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				MecVerticaComponent component = new MecVerticaComponent(myResult.getInt("id")
						, myResult.getString("component"), myResult.getInt("batch_id")
						, myResult.getString("sql_script_path"), myResult.getInt("execution_order")
						, myResult.getBoolean("output_file"), myResult.getString("file_name_pattern")
						, myResult.getInt("file_output_id"));
				componentList.add(component);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Collections.sort(componentList, MecVerticaComponent.componentComparator);
		return componentList;
	}
	
	public int getComponentErrorCode(String databaseSchema, String databaseTable, int batchId, String sessionId
			, int componentId){
		PreparedStatement selectStatement = null;
		int errorCode = 0;
		
		String sqlSelectMdClient = "SELECT error_code"
				+ " FROM " + databaseSchema	+ "." + databaseTable + " as ses"
				+ " INNER JOIN (SELECT MAX(session_start) as max_date, batch_id, component_id"
				+ " , session_id FROM " + databaseSchema	+ "." + databaseTable
				+ " GROUP BY batch_id, component_id, session_id) as agg"
				+ " ON ses.session_start = max_date AND ses.batch_id = agg.batch_id"
				+ " AND ses.component_id = agg.component_id AND ses.session_id = agg.session_id"
				+ " WHERE ses.batch_id = ? and ses.session_id = ? and ses.component_id = ?;";

		try {selectStatement = connection.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setInt(1, batchId);
			selectStatement.setString(2, sessionId);
			selectStatement.setInt(3, componentId);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				errorCode = myResult.getInt("error_code");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return errorCode;
	}
	
	public HashMap<String, Integer> getLoadedFiles(String databaseSchema
			, String databaseTable) {

		HashMap<String, Integer> loadedFiles = new HashMap<String, Integer>();

		PreparedStatement preparedStatement = null;

		String selectSql = "SELECT distinct FILE_NAME, FILE_ID"
				+ " FROM "+ databaseSchema	+ "." + databaseTable;

		try {
			preparedStatement = connection.prepareStatement(selectSql);

			ResultSet myResult = preparedStatement.executeQuery();

			while (myResult.next()) {
				loadedFiles.put(myResult.getString("FILE_NAME"), myResult.getInt("FILE_ID"));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return loadedFiles;
	}
}
