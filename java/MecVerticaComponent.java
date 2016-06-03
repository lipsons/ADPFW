//package com.mecglobal.s3automation.java;

import java.util.Comparator;

public class MecVerticaComponent {
	private int componentId;
	private String componentName;
	private int batchId;
	private String sqlScriptPath;
	private int executionOrder;
	private boolean outputFile;
	private String fileNamePattern;
	private int fileOutputId;
	
	public MecVerticaComponent(int ci, String cn, int b, String s, int e, boolean o, String fn, int fo) {
		this.componentId = ci;
		this.componentName = cn;
		this.batchId = b;
		this.sqlScriptPath = s;
		this.executionOrder = e;
		this.outputFile = o;
		this.fileNamePattern = fn;
		this.fileOutputId = fo;
	}
	
	@Override
	public String toString() {
		return "MecVerticaComponent [componentId=" + componentId + ", componentName=" + componentName + ", batchId="
				+ batchId + ", sqlScriptPath=" + sqlScriptPath + ", executionOrder=" + executionOrder + ", outputFile="
				+ outputFile + ", fileNamePattern=" + fileNamePattern + ", fileOutputId=" + fileOutputId + "]";
	}

	public int getComponentId() {
		return componentId;
	}

	public void setComponentId(int componentId) {
		this.componentId = componentId;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public int getBatchId() {
		return batchId;
	}

	public void setBatchId(int batchId) {
		this.batchId = batchId;
	}

	public String getSqlScriptPath() {
		return sqlScriptPath;
	}

	public void setSqlScriptPath(String sqlScriptPath) {
		this.sqlScriptPath = sqlScriptPath;
	}

	public int getExecutionOrder() {
		return executionOrder;
	}

	public void setExecutionOrder(int executionOrder) {
		this.executionOrder = executionOrder;
	}
	
	public boolean isOutputFile() {
		return outputFile;
	}

	public void setOutputFile(boolean outputFile) {
		this.outputFile = outputFile;
	}

	public String getFileNamePattern() {
		return fileNamePattern;
	}

	public void setFileNamePattern(String fileNamePattern) {
		this.fileNamePattern = fileNamePattern;
	}

	public int getFileOutputId() {
		return fileOutputId;
	}

	public void setFileOutputId(int fileOutputId) {
		this.fileOutputId = fileOutputId;
	}

	public static Comparator<MecVerticaComponent> componentComparator = new Comparator<MecVerticaComponent>() {

		@Override
		public int compare(MecVerticaComponent o1, MecVerticaComponent o2) {
			int value1 = o1.executionOrder-o2.executionOrder;
			if (value1 == 0) {
				return o1.batchId-o2.batchId;
			}
			return o1.executionOrder-o2.executionOrder;
		}
		
	};
}
