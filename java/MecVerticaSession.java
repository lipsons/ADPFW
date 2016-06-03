//package com.mecglobal.s3automation.java;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Date;

public class MecVerticaSession {

	private String sessionName;
	private String sessionId;
	private String sessionType;
	private Date feedStartDate;
	private Date feedEndDate;
	private String fileName;

	public MecVerticaSession(String sn, String st, Date ff, Date ft,
			String f) {
		this.sessionName = sn;
		this.sessionType = st;
		this.feedStartDate = ff;
		this.feedEndDate = ft;
		this.fileName = f;
	}

	public MecVerticaSession() {
		
	}

	@Override
	public String toString() {
		return "MecVerticaSession [sessionName=" + sessionName + ", sessionId=" + sessionId + ", sessionType="
				+ sessionType + ", feedStartDate=" + feedStartDate + ", feedEndDate=" + feedEndDate + ", fileName="
				+ fileName + "]";
	}

	public String getSessionName() {
		return sessionName;
	}

	public void setSessionName(String sessionName) {
		this.sessionName = sessionName;
	}

	public String getSessionId() {
		return sessionId;
	}

	public String getSessionType() {
		return sessionType;
	}

	public void setSessionType(String sessionType) {
		this.sessionType = sessionType;
	}
	
	public Date getFeedStartDate() {
		return feedStartDate;
	}

	public void setFeedStartDate(Date feedStartDate) {
		this.feedStartDate = feedStartDate;
	}

	public Date getFeedEndDate() {
		return feedEndDate;
	}

	public void setFeedEndDate(Date feedEndDate) {
		this.feedEndDate = feedEndDate;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void generateSessionId(Connection conn, String clientName, String databaseTable, String databaseSchema) {
		SecureRandom random = new SecureRandom();
		sessionName = clientName + ":" + sessionType;
		boolean sessionIdCreated = false;

		while (sessionIdCreated == false) {
			System.out.println("System generating session ID.");
			
			sessionId = new BigInteger(130, random).toString(32);

			PreparedStatement selectStatement = null;

			String sqlSelectMdSession = "SELECT COUNT(*) AS row_count" + " FROM ( SELECT id FROM " + databaseSchema
					+ "." + databaseTable + " WHERE id = ? ) AS session_check;";

			try {
				selectStatement = conn.prepareStatement(sqlSelectMdSession);

				selectStatement.setString(1, sessionId);

				ResultSet myResult = selectStatement.executeQuery();

				while (myResult.next()) {
					if (myResult.getInt("row_count") == 0) {

						PreparedStatement insertStatement = null;

						String sqlInsertMdSession = "insert into " + databaseSchema + "." + databaseTable
								+ " (id, session_name)" + " values(?, ?);";

						try {
							insertStatement = conn.prepareStatement(sqlInsertMdSession);

							insertStatement.setString(1, sessionId);
							insertStatement.setString(2, sessionName);

							insertStatement.executeUpdate();

							sessionIdCreated = true;
							insertStatement.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("Session ID already exists. System generating new session ID.");
					}
				}
				selectStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}

	public void generateSessionIdForFeed(Connection conn, String clientName, String databaseTable, String databaseSchema) {
		SecureRandom random = new SecureRandom();
		sessionName = clientName + ":" + sessionType;
		boolean sessionIdCreated = false;

		while (sessionIdCreated == false) {
			System.out.println("System generating session ID.");
			
			this.sessionId = new BigInteger(130, random).toString(32);

			PreparedStatement selectStatement = null;

			String sqlSelectMdSession = "SELECT COUNT(*) AS row_count" + " FROM ( SELECT id FROM " + databaseSchema
					+ "." + databaseTable + " WHERE id = ? ) AS session_check;";

			try {
				selectStatement = conn.prepareStatement(sqlSelectMdSession);

				selectStatement.setString(1, this.sessionId);

				ResultSet myResult = selectStatement.executeQuery();

				while (myResult.next()) {
					if (myResult.getInt("row_count") == 0) {

						PreparedStatement insertStatement = null;

						String sqlInsertMdSession = "insert into " + databaseSchema + "." + databaseTable
								+ " (id, session_name, feed_start_date, feed_end_date, file_name)" 
								+ " values(?, ?, ?, ?, ?);";

						try {
							insertStatement = conn.prepareStatement(sqlInsertMdSession);

							insertStatement.setString(1, this.sessionId);
							insertStatement.setString(2, this.sessionName);
							insertStatement.setDate(3, this.feedStartDate);
							insertStatement.setDate(4, this.feedEndDate);
							insertStatement.setString(5, this.fileName);

							insertStatement.executeUpdate();

							sessionIdCreated = true;
							insertStatement.close();
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {
						System.out.println("Session ID already exists. System generating new session ID.");
					}
				}
				selectStatement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
}
