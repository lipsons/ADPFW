//package com.mecglobal.s3automation.java;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;

public abstract class MecTimeMethods {

	public static int daysBetween(String startDate, String endDate) {
		int diffDays = 0;
		try {
			String dateFormat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			Date startDateObj = format.parse(startDate + " 00:00:00");
			Date endDateObj = format.parse(endDate + " 00:00:00");
			long diff = endDateObj.getTime() - startDateObj.getTime();
			diffDays = (int) (diff / (24 * 60 * 60 * 1000));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return diffDays;
	}
	
	public static String getStartDate(int lookbackWindow) throws ParseException{
		DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
		
		DateTime dt = new DateTime();
		DateTime daysEarlier = dt.minusDays(lookbackWindow);
				
		Date date = inputFormat.parse(daysEarlier.toString());
		String outputText = outputFormat.format(date);
		return outputText;
	}

	public static String getYear(String date) {
		return date.substring(0, date.indexOf("-")).trim();
	}

	public static String getMonth(String date) {
		return date.substring(date.indexOf("-", 2) + 1, date.indexOf("-") + 3).trim();
	}

	public static String getDay(String date) {
		return date.substring(date.lastIndexOf("-") + 1, date.length()).trim();
	}

	public static ArrayList<String> listDates(String startDate, int daysDiff) {
		ArrayList<String> allDates = new ArrayList<String>();
		try {
			String dateFormat = "yyyy-MM-dd";
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Calendar c = Calendar.getInstance();
			c.setTime(sdf.parse(startDate));
			allDates.add((String) sdf.format(c.getTime()));
			for (int i = 0; i < daysDiff; i++) {
				c.add(Calendar.DATE, 1);
				allDates.add((String) sdf.format(c.getTime()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return allDates;

	}

	public static java.util.Date formatDate(String date) {
		java.util.Date startDateObj = null;
		try {
			String dateFormat = "yyyy-MM-dd HH:mm:ss";
			SimpleDateFormat format = new SimpleDateFormat(dateFormat);
			startDateObj = format.parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return startDateObj;
	}
	
	public static Date getStartDateFromMediaQuarter(Connection conn, String databaseTable
			, String databaseSchema, int mediaYear, int mediaQuarter){

		PreparedStatement selectStatement = null;
		Date startDate = new Date();
		
		String sqlSelectMdClient = "SELECT min(actualdate) as start_date"
				+ " FROM " + databaseSchema	+ "." + databaseTable
				+ " WHERE media_year = ? and media_quarter = ?;";

		try {selectStatement = conn.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setInt(1, mediaYear);
			selectStatement.setInt(2, mediaQuarter);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				startDate = myResult.getDate("start_date");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return startDate;
	}
	
	public static Date getEndDateFromMediaQuarter(Connection conn, String databaseTable
			, String databaseSchema, int mediaYear, int mediaQuarter){

		PreparedStatement selectStatement = null;
		Date endDate = new Date();
		
		String sqlSelectMdClient = "SELECT max(actualdate) as end_date"
				+ " FROM " + databaseSchema	+ "." + databaseTable
				+ " WHERE media_year = ? and media_quarter = ?;";

		try {selectStatement = conn.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setInt(1, mediaYear);
			selectStatement.setInt(2, mediaQuarter);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				endDate = myResult.getDate("end_date");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return endDate;
	}
	
	public static Date getStartDateFromMediaMonth(Connection conn, String databaseTable
			, String databaseSchema, int mediaYear, int mediaMonth){

		PreparedStatement selectStatement = null;
		Date startDate = new Date();
		
		String sqlSelectMdClient = "SELECT min(actualdate) as start_date"
				+ " FROM " + databaseSchema	+ "." + databaseTable
				+ " WHERE media_year = ? and media_month = ?;";

		try {selectStatement = conn.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setInt(1, mediaYear);
			selectStatement.setInt(2, mediaMonth);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				startDate = myResult.getDate("start_date");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return startDate;
	}
	
	public static Date getEndDateFromMediaMonth(Connection conn, String databaseTable
			, String databaseSchema, int mediaYear, int mediaMonth){


		PreparedStatement selectStatement = null;
		Date endDate = new Date();
		
		String sqlSelectMdClient = "SELECT max(actualdate) as end_date"
				+ " FROM " + databaseSchema	+ "." + databaseTable
				+ " WHERE media_year = ? and media_month = ?;";

		try {selectStatement = conn.prepareStatement(sqlSelectMdClient);
			
			selectStatement.setInt(1, mediaYear);
			selectStatement.setInt(2, mediaMonth);

			ResultSet myResult = selectStatement.executeQuery();

			while (myResult.next()) {
				endDate = myResult.getDate("end_date");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return endDate;
	
	}
}
