//package com.mecglobal.s3automation.java;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * MecSftp is the class for all connections to an SFTP server.  
 * 
 * @author brandon.nieves
 * @version 1.1
 * @see com.jcraft.jsch.JSch
 */
public class MecSftpDeprecated {
//	private Session session;
//	private Channel channel;
//	private ChannelSftp channelSftp;
//
//	/**
//	 * Class constructor specifying the session, channel, and SFTP-channel objects.
//	 * 
//	 * @param s the session representing a connection to a SSH server
//	 * @param c the channel associated with the session
//	 * @param cs the channel connected to an SFTP server
//	 */
//	public MecSftp(Session s, Channel c, ChannelSftp cs) {
//		session = s;
//		channel = c;
//		channelSftp = cs;
//	}
//
//	/**
//	 * Creates a new MecSftp object.
//	 */
//	public MecSftp() {
//
//	}
//
//	/**
//	 * Prints all attributes
//	 */
//	public String toString() {
//		return "Session=" + session + "\nChannel=" + channel + "\nChannelSFTP=" + channelSftp;
//	}
//
//	/**
//	 * @return current session
//	 */
//	public Session getSession() {
//		return session;
//	}
//
//	/**
//	 * @param session session to set
//	 */
//	public void setSession(Session session) {
//		this.session = session;
//	}
//	
//	/**
//	 * @return current channel
//	 */
//	public Channel getChannel() {
//		return channel;
//	}
//
//	/**
//	 * @param channel channel to set
//	 */
//	public void setChannel(Channel channel) {
//		this.channel = channel;
//	}
//
//	/**
//	 * @return current channelSftp
//	 */
//	public ChannelSftp getChannelSftp() {
//		return channelSftp;
//	}
//
//	/**
//	 * @param channelSftp channelSftp to set
//	 */
//	public void setChannelSftp(ChannelSftp channelSftp) {
//		this.channelSftp = channelSftp;
//	}
//	
//	/**
//	 * Creates a new connection to the client's SFTP server. This method will return an MecSftp object--
//	 * maintaining an active connection until the session has been disconnected. 
//	 * <p>
//	 * Important: Please ensure that the client's credentials and/or public key has been saved to NFS
//	 * (e.g. generalFilePath + "." + packageType + &lt;file separator&gt; + clientName) 
//	 * 
//	 * @param clientName the client's name registered in the Vertica table cross_client_resources.DIM_REGISTRAR
//	 * @param generalFilePath the client's general file path on NFS
//	 * @param packageType the connection method used to acquire the client's media
//	 * @return MecSftp the MecSftp object 
//	 */
//	public static MecSftp connect(String clientName, String generalFilePath, String packageType) {
//		
//	    Session session = null;
//	    Channel channel = null;
//
//	    MecSftp sftp = new MecSftp();
//	    
//		// create file string from parameters
//		String file = generalFilePath + "." + packageType + File.separator + clientName;
//
//		// create variables to create MecFtp object
//		String ftpServer = null;
//		int ftpPort = 0;
//		String ftpUser = null;
//		String ftpPassword = null;
//		String fileKeyName = null;
//
//		// create a dictionary of values from the credentials file
//		Map<String, String> map = new HashMap<String, String>();
//
//		BufferedReader in = null;
//
//		// read credentials file and assign values to variables
//		try {
//			in = new BufferedReader(new FileReader(file));
//
//			String line = in.readLine();
//
//			while (line != null) {
//				String[] tokens = line.split("=");
//				map.put(tokens[0].trim(), tokens[1].trim());
//				line = in.readLine();
//			}
//			in.close();
//
//			Iterator<Map.Entry<String, String>> it = map.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, String> pair = (Map.Entry<String, String>) it.next();
//
//				if (pair.getKey().equals("server")) {
//					ftpServer = (String) pair.getValue();
//				} else if (pair.getKey().equals("port")) {
//					ftpPort = Integer.parseInt((String) pair.getValue());
//				} else if (pair.getKey().equals("user")) {
//					ftpUser = (String) pair.getValue();
//				} else if (pair.getKey().equals("pass")) {
//					ftpPassword = (String) pair.getValue();
//				} else if (pair.getKey().equals("key")) {
//					fileKeyName = (String) pair.getValue();
//				} else {
//					System.out.println("The key \"" + pair.getKey() + "\" is unknown at " + file);
//				}
//
//			}
//
//		}
//
//		catch (IOException io) {
//			System.out.println("An IO Exception occurred");
//			io.printStackTrace();
//		}
//		// create an MecFtp object
//		MecFtpCredentials ftpProfile = new MecFtpCredentials(ftpServer, ftpPort, ftpUser, ftpPassword, fileKeyName);
//		
//		String fileKey = generalFilePath + "." + packageType + File.separator + ftpProfile.getKey();
//		
//		try {
//			JSch jsch = new JSch();
//			File privateKeyFile = new File(fileKey);
//			if (privateKeyFile.exists()){
//				try {
//					jsch.addIdentity(fileKey);
//				} catch (JSchException e) {
//					e.printStackTrace();
//				}
//			}
//			try {
//				session = jsch.getSession(ftpProfile.getUser(), ftpProfile.getServer(), ftpProfile.getPort());
//			} catch (JSchException e) {
//				e.printStackTrace();
//			}
//			if (!privateKeyFile.exists()){
//				session.setPassword(ftpProfile.getPassword());
//			}
//
//			session.setConfig("StrictHostKeyChecking", "no");
//			
//			System.out.println("Connecting to " + ftpProfile.getServer());
//			try {
//				session.connect();
//			} catch (JSchException e) {
//				e.printStackTrace();
//			}
//			System.out.println("Connected to " + ftpProfile.getServer());
//
//			try {
//				channel = session.openChannel("sftp");
//			} catch (JSchException e) {
//				e.printStackTrace();
//			}
//			try {
//				channel.connect();
//			} catch (JSchException e) {
//				e.printStackTrace();
//			}
//			sftp = new MecSftp(session, channel, (ChannelSftp) channel);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}	
//		return sftp;
//	}
//
//	/**
//	 * Terminates the active SFTP connection.
//	 *  
//	 * @param MecSftp the MecSftp object
//	 */
//	public static void disconnect(MecSftp sftp) {
//		System.out.println("Disconnecting from SFTP");
//		Session session = sftp.getSession();
//		Channel channel = sftp.getChannel();
//		ChannelSftp channelSftp = sftp.getChannelSftp();
//		
//		if (channelSftp.isConnected()) {
//			try {
//				session.disconnect();
//				channel.disconnect();
//				channelSftp.quit();
//				System.out.println("SFTP has been successfully disconnected.");
//			} catch (Exception ioe) {
//				System.out.println("SFTP failed to disconnect from ftp server.");
//			}
//		}
//	}
//	
//	/**
//	 * Lists the client's files on the SFTP server that matches the file-name string patterns registered in the 
//	 * Vertica table cross_client_resources.DIM_PACKAGE. 
//	 * <p>
//	 * How date parts are represented in the file pattern: 
//	 * <p>
//	 * Sample date: January 31, 2016
//	 * <ul>
//	 * <li>%Y=2016
//	 * <li>%y=16
//	 * <li>%m=01
//	 * <li>%d=31
//	 * <li>%dt=2016-01-31
//	 * </ul> 
//	 * 
//	 * @param sftp the MecSftp object
//	 * @param sftpDirectory the remote directory of the SFTP server 
//	 * @param filePattern the string pattern representing the file name 
//	 * @param fileExtension the file extension
//	 * @param year the year (yyyy) 
//	 * @param month the month (mm)
//	 * @param day the day (dd)
//	 * @param genFilePath the client's general file path in NFS 
//	 * @return HashMap This is a map having a file-name as the key and a corresponding
//	 * MecFile object as its value 
//	 */
//	public static HashMap<String, MecFile> listFiles(MecSftp sftp, String sftpDirectory, final String filePattern,
//			final String fileExtension, final String year, final String month, final String day,
//			final String genFilePath) {
//		
//		ChannelSftp channelSftp = sftp.getChannelSftp();
//		
//		// create array of files
//		HashMap<String, MecFile> sftpFiles = new HashMap<String, MecFile>();
//
//		String searchDate = year + "-?" + month + "-?" + day;
//		String yearAbbr = year.substring(2);
//		String patternToSeachFor = filePattern.replaceAll("\\%dt", searchDate)
//				.replaceAll("\\%Y", year).replaceAll("\\%m", month).replaceAll("\\%d", day)
//				.replaceAll("\\%y", yearAbbr);
//		
//		try {
//			String dirToSearch = sftpDirectory;
//			String fileToSearch = sftpDirectory+patternToSeachFor;
//			
//			channelSftp.cd(dirToSearch);
//			
//			@SuppressWarnings("unchecked")
//			Vector<ChannelSftp.LsEntry> fileList = channelSftp.ls(fileToSearch);
//			
//			if (fileList != null && fileList.size() > 0) {
//				for(ChannelSftp.LsEntry aFile : fileList){
//					MecFile file = new MecFile();
//					file.setGeneralFilePath(genFilePath);
//					file.setFileRemoteDirectory(sftpDirectory);
//					file.setFileName(aFile.getFilename());
//					String fileName = genFilePath + aFile.getFilename();
//					sftpFiles.put(fileName, file);
//				}
//			}
//		} catch (SftpException e) {
//			e.printStackTrace();
//		}
//
//		return sftpFiles;
//	}
//
//	/**
//	 * Gets the file from the SFTP server. This method opens a new connection to the SFTP server and closes
//	 * it once the file has been downloaded.
//	 * 
//	 * @param fileName the name of the file
//	 * @param generalFilePath the client's general file path in NFS 
//	 * @param sftpDirectory the remote directory of the SFTP server 
//	 * @param client the client's name registered in the Vertica table cross_client_resources.DIM_REGISTRAR
//	 * @param packageType the connection method used to acquire the client's media
//	 * @throws SftpException if an error occurs while using the SFTP protocol
//	 */
//	public static void getFile(String fileName, String generalFilePath, String sftpDirectory
//			, String client, String packageType) throws SftpException {
//		// connect to SFTP
//		MecSftp sftp = connect(client, generalFilePath, packageType);
//
//		try {
//			// open SFTP Channel
//			ChannelSftp channelSftp = sftp.getChannelSftp();
//
//			// Download file from SFTP
//			System.out.println("Initializing download request for " + fileName);
//			String filePath = generalFilePath + fileName;
//			String remoteFile = sftpDirectory + fileName;
//			File downloadFile = new File(filePath);
//
//			channelSftp.cd(sftpDirectory);
//			
//			OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
//			InputStream inputStream = channelSftp.get(remoteFile);
//
//			byte[] bytesArray = new byte[4096];
//			int bytesRead = -1;
//			while ((bytesRead = inputStream.read(bytesArray)) != -1) {
//				outputStream.write(bytesArray, 0, bytesRead);
//			}
//
//			System.out.println(fileName + " has been downloaded successfully");
//			
//			outputStream.close();
//			inputStream.close();
//		} catch (IOException ex) {
//			System.out.println("Error: " + ex.getMessage());
//			ex.printStackTrace();
//		} finally {
//			disconnect(sftp);
//		}
//	}
}
