//package com.mecglobal.s3automation.java;

import java.util.ArrayList;

public interface MecFileAcquirer {
	ArrayList<MecFile> listFiles();
	void downloadFile(String fileName);
}
