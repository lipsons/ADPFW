//package com.mecglobal.s3automation.java;

import java.util.Comparator;

public class MecClient {

	private String name;
	private String id;

	public MecClient(String n, String i) {
		name = n;
		id = i;
	}
	
	public MecClient() {
		
	};

	@Override
	public String toString() {
		return "[Client name=" + name + ", Client ID=" + id + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public static Comparator<MecClient> clientComparator = new Comparator<MecClient>() {

		@Override
		public int compare(MecClient o1, MecClient o2) {
			String clientName1 = o1.getName().toUpperCase();
			String clientName2 = o2.getName().toUpperCase();
			
			return clientName1.compareTo(clientName2);
		}
	};
}
