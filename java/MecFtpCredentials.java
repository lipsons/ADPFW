//package com.mecglobal.s3automation.java;

public class MecFtpCredentials {

	private String server;
	private int port;
	private String user;
	private String pass;
	private String key;

	public MecFtpCredentials(String s, int po, String u, String pas) {
		server = s;
		port = po;
		user = u;
		pass = pas;
	}

	// constructor with signature for SFTP connection using public key file
	public MecFtpCredentials(String s, int po, String u, String pas, String k) {
		server = s;
		port = po;
		user = u;
		pass = pas;
		key = k;
	}

	public MecFtpCredentials() {

	}

	public String toString() {
		return "server=" + server + "\nport=" + port + "\nuser=" + user + "\npass=" + pass;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return pass;
	}

	public void setPassword(String pass) {
		this.pass = pass;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
