//package com.mecglobal.s3automation.java;

import java.util.Comparator;

public class MecVerticaBatch {

	private int batchId;
	private String batchName;
	private String processName;
	private int executionOrder;
	private String clientId;
	
	public MecVerticaBatch(int bi, String bn, String p, int e, String c) {
		this.batchId = bi;
		this.batchName = bn;
		this.processName = p;
		this.executionOrder = e;
		this.clientId = c;
	}

	@Override
	public String toString() {
		return "MecVerticaBatch [Batch ID=" + batchId + ", Batch Name=" + batchName + ", Process Name=" + processName
				+ ", Execution Order=" + executionOrder + ", Client ID=" + clientId + "]";
	}

	public int getBatchId() {
		return batchId;
	}

	public void setBatchId(int batchId) {
		this.batchId = batchId;
	}

	public String getBatchName() {
		return batchName;
	}

	public void setBatchName(String batchName) {
		this.batchName = batchName;
	}

	public String getProcessName() {
		return processName;
	}

	public void setProcessName(String processName) {
		this.processName = processName;
	}

	public int getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}
	
	public static Comparator<MecVerticaBatch> batchComparator = new Comparator<MecVerticaBatch>() {

		@Override
		public int compare(MecVerticaBatch o1, MecVerticaBatch o2) {
			int value1 = o1.processName.compareToIgnoreCase(o2.processName);
			if (value1 == 0) {
				int value2 = o1.executionOrder-o2.executionOrder;
				if (value2 == 0) {
					return o1.batchName.compareToIgnoreCase(o2.batchName);
				} else {
					return value2;
				}
			}
			return value1;
		}
		
	};
	
}
