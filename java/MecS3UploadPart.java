//package com.mecglobal.s3automation.java;

import java.io.File;
import java.util.concurrent.Callable;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.UploadPartRequest;
import com.amazonaws.services.s3.model.UploadPartResult;

public class MecS3UploadPart implements Callable<Object>{

	private String existingBucketName;
	private String keyName;
	private int partNumber;
	private long filePosition;
	private File file;
	private long partSize;
	private String uploadId;
	
	public MecS3UploadPart(String e, String k, int p, long fp, File f, long ps, String u){
		this.existingBucketName=e;
		this.keyName=k;
		this.partNumber=p;
		this.filePosition=fp;
		this.file=f;
		this.partSize=ps;
		this.uploadId=u;
	}
	
	public String toString() {
		return "Existing bucket name: " + existingBucketName + "\nKey name: " + keyName + "\nPart number: " + partNumber
				+ "\nFile position: " + filePosition + "\nFile: " + file + "\nPart size: " + partSize + "\nUpload ID: "
				+ uploadId;
	}

	public String getExistingBucketName() {
		return existingBucketName;
	}

	public void setExistingBucketName(String existingBucketName) {
		this.existingBucketName = existingBucketName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public int getPartNumber() {
		return partNumber;
	}

	public void setPartNumber(int partNumber) {
		this.partNumber = partNumber;
	}

	public long getFilePosition() {
		return filePosition;
	}

	public void setFilePosition(long filePosition) {
		this.filePosition = filePosition;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public long getPartSize() {
		return partSize;
	}

	public void setPartSize(long partSize) {
		this.partSize = partSize;
	}

	public String getUploadId() {
		return uploadId;
	}

	public void setUploadId(String uploadId) {
		this.uploadId = uploadId;
	}
	
	@Override
	public Object call() throws Exception {
        System.out.println("Uploading part" + Integer.toString(partNumber));            	
        // Create request to upload a part.
        UploadPartRequest uploadRequest = new UploadPartRequest()
            .withBucketName(existingBucketName).withKey(keyName)
            .withUploadId(uploadId).withPartNumber(partNumber)
            .withFileOffset(filePosition)
            .withFile(file)
            .withPartSize(partSize);

        AmazonS3 s3Client = new AmazonS3Client(new ProfileCredentialsProvider()); 
        UploadPartResult partETag = s3Client.uploadPart(uploadRequest);
		return partETag.getPartETag();
	}
	
}
