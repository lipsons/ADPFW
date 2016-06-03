//package com.mecglobal.s3automation.java;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.AbortMultipartUploadRequest;
import com.amazonaws.services.s3.model.CompleteMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class MecS3Methods {
//	private static String BUCKET_NAME = "diap.dev.us-east-1.mec-cookiecutter";
//
//	// upload files not in S3
//	public static void uploadFiles(String clientName) {
//		
//		MecClient client = new MecClient();
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);	
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			String directoryPath = client.getGeneralFilePath();
//			String s3prefix = client.getS3Prefix();
//			File dir = new File(directoryPath);
//			HashMap<String, MecS3> s3objects = new HashMap<String, MecS3>();
//			
//			if(dir.isDirectory()){
//		        File[] zipFiles = dir.listFiles();
//				Arrays.sort(zipFiles);
//		        for(File file : zipFiles){
//		        	if(file.isFile() && file.getName().endsWith(".gz")){
//		        		String keyName = s3prefix + file.getName(); 
//		        		s3objects.put(keyName, new MecS3(BUCKET_NAME, keyName, file.getName(), file.length()));
//		            }
//		        }
//			}
//
//			// get loaded files and remove any matches from the file-to-load set
//			HashMap<String, Long> existingS3objects = listObjects(clientName);
//
//			Map<String, Long> filesForQueue = new HashMap<String, Long>();
//
//			Iterator<Map.Entry<String, MecS3>> it = s3objects.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, MecS3> fileToLoad = (Map.Entry<String, MecS3>) it.next();
//				if (existingS3objects.containsKey(fileToLoad.getKey())) {
//					System.out.println(fileToLoad.getValue().getFileName() 
//							+ " has already been loaded into S3");
//		    		System.out.println("Deleting " + fileToLoad.getValue().getFileName()  + " ..");
//		    		Path filePath = Paths.get(client.getGeneralFilePath() + fileToLoad.getValue().getFileName() );
//		    		MecFileMethods.deleteFile(filePath);
//				} else {
//					filesForQueue.put(fileToLoad.getValue().getFileName(),
//							fileToLoad.getValue().getFileSize());
//				}
//				it.remove();
//			}
//
//			SortedSet<String> keys = new TreeSet<String>(filesForQueue.keySet());
//			for (String key : keys) {
//		    	System.out.println("Loading " + key + " - Size: "+ filesForQueue.get(key));
//		    	if (filesForQueue.get(key)>(5*1024*1024)){
//		    		System.out.println(key + " is a big file");
//		    		uploadBigFile(key, client.getGeneralFilePath(), client.getS3Prefix());
//		    		System.out.println("Deleting " + key + " ..");
//		    		Path filePath = Paths.get(client.getGeneralFilePath() + key);
//		    		MecFileMethods.deleteFile(filePath);
//		    	} else {
//		    		System.out.println(key + " is a small file");
//		    		uploadSmallFile(key, client.getGeneralFilePath(), client.getS3Prefix());
//		    		System.out.println("Deleting " + key + " ..");
//		    		Path filePath = Paths.get(client.getGeneralFilePath() + key);
//		    		MecFileMethods.deleteFile(filePath);
//		    	}
//			}
//	    } catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//
//	public static void uploadFilesExternally(String clientName, String directoryPath) {
//		
//		MecClient client = new MecClient();
//		try {
//			Connection conn = MecVerticaMethods.connect();
//			client = MecVerticaMethods.getClient(conn, clientName);	
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//		try {
//			String s3prefix = client.getS3Prefix();
//			File dir = new File(directoryPath);
//			HashMap<String, MecS3> s3objects = new HashMap<String, MecS3>();
//			
//			if(dir.isDirectory()){
//		        File[] zipFiles = dir.listFiles();
//				Arrays.sort(zipFiles);
//		        for(File file : zipFiles){
//		        	if(file.isFile()){
//		        		String keyName = s3prefix + file.getName(); 
//		        		s3objects.put(keyName, new MecS3(BUCKET_NAME, keyName, file.getName(), file.length()));
//		            }
//		        }
//			}
//
//			// get loaded files and remove any matches from the file-to-load set
//			HashMap<String, Long> existingS3objects = listObjects(clientName);
//
//			Map<String, Long> filesForQueue = new HashMap<String, Long>();
//
//			Iterator<Map.Entry<String, MecS3>> it = s3objects.entrySet().iterator();
//			while (it.hasNext()) {
//				Map.Entry<String, MecS3> fileToLoad = (Map.Entry<String, MecS3>) it.next();
//				if (existingS3objects.containsKey(fileToLoad.getKey())) {
//					System.out.println(fileToLoad.getValue().getFileName() 
//							+ " has already been loaded into S3");
//		    		System.out.println("Deleting " + fileToLoad.getValue().getFileName()  + " ..");
//		    		Path filePath = Paths.get(client.getGeneralFilePath() + fileToLoad.getValue().getFileName() );
//		    		MecFileMethods.deleteFile(filePath);
//				} else {
//					filesForQueue.put(fileToLoad.getValue().getFileName(),
//							fileToLoad.getValue().getFileSize());
//				}
//				it.remove();
//			}
//
//			SortedSet<String> keys = new TreeSet<String>(filesForQueue.keySet());
//			for (String key : keys) {
//		    	System.out.println("Loading " + key + " - Size: "+ filesForQueue.get(key));
//		    	if (filesForQueue.get(key)>(5*1024*1024)){
//		    		System.out.println(key + " is a big file");
//		    		uploadBigFile(key, client.getGeneralFilePath(), client.getS3Prefix());
//		    		System.out.println("Deleting " + key + " ..");
//		    		Path filePath = Paths.get(client.getGeneralFilePath() + key);
//		    		MecFileMethods.deleteFile(filePath);
//		    	} else {
//		    		System.out.println(key + " is a small file");
//		    		uploadSmallFile(key, client.getGeneralFilePath(), client.getS3Prefix());
//		    		System.out.println("Deleting " + key + " ..");
//		    		Path filePath = Paths.get(client.getGeneralFilePath() + key);
//		    		MecFileMethods.deleteFile(filePath);
//		    	}
//			}
//	    } catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
//	
//	public static void uploadBigFile(String fileName, String genFilePath, String s3prefix) {
//        AWSCredentials credentials = null;
//        try {
//            credentials = new ProfileCredentialsProvider("default").getCredentials();
//        } catch (Exception e) {
//            throw new AmazonClientException(
//                    "Cannot load the credentials from the credential profiles file. " +
//                    "Please make sure that your credentials file is at the correct " +
//                    "location, and is in valid format.",
//                    e);
//        }
//		AmazonS3 s3Client = new AmazonS3Client(credentials);
//		File file = new File(genFilePath + fileName);
//		String key = s3prefix + fileName;
//
//		// Create a list of UploadPartResponse objects. You get one of these for
//		// each part upload.
//		List<PartETag> partETags = new ArrayList<PartETag>();
//		System.out.println("Initializing multi-part request");
//
//		// Step 1: Initialize.
//		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(BUCKET_NAME, key);
//		InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
//		long contentLength = file.length();
//		long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.
//
//		try {
//			// Step 2: Upload parts.
//			System.out.println("Uploading parts");
//			long filePosition = 0;
//
//			ExecutorService pool = Executors.newFixedThreadPool(10);
//			List<Future<Object>> partList = new ArrayList<Future<Object>>();
//
//			for (int i = 1; filePosition < contentLength; i++) {
//				// Last part can be less than 5 MB. Adjust part size.
//				partSize = Math.min(partSize, (contentLength - filePosition));
//				String uploadId = initResponse.getUploadId();
//				Callable<Object> partUpload = new MecS3UploadPart(BUCKET_NAME, key, i, filePosition, file, partSize,
//						uploadId);
//				Future<Object> f = pool.submit(partUpload);
//				partList.add(f);
//				filePosition += partSize;
//			}
//
//			for (Future<Object> f : partList) {
//				partETags.add((PartETag) f.get());
//			}
//			
//			// closing thread pool
//			pool.shutdown();
//			try {
//				pool.awaitTermination(60, TimeUnit.MINUTES);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//			
//			// Step 3: Complete.
//			System.out.println("Completing request.");
//			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(BUCKET_NAME, key,
//					initResponse.getUploadId(), partETags);
//			s3Client.completeMultipartUpload(compRequest);
//		} catch (Exception e) {
//			System.out.println("Aborting multi part upload.");
//			s3Client.abortMultipartUpload(
//					new AbortMultipartUploadRequest(BUCKET_NAME, key, initResponse.getUploadId()));
//		}
//	}
//
//	public static void uploadSmallFile(String fileName, String genFilePath, String s3prefix) {
//      AWSCredentials credentials = null;
//      try {
//          credentials = new ProfileCredentialsProvider("default").getCredentials();
//      } catch (Exception e) {
//          throw new AmazonClientException(
//                  "Cannot load the credentials from the credential profiles file. " +
//                  "Please make sure that your credentials file is at the correct " +
//                  "location, and is in valid format.",
//                  e);
//      }
//		AmazonS3 s3 = new AmazonS3Client(credentials);
//		File file = new File(genFilePath + fileName);
//		String key = s3prefix + fileName;
//
//		try {
//			s3.putObject(new PutObjectRequest(BUCKET_NAME, key, file));
//		} catch (AmazonServiceException ase) {
//			System.out.println("Caught an AmazonServiceException, which means your request made it "
//					+ "to Amazon S3, but was rejected with an error response for some reason.");
//			System.out.println("Error Message:    " + ase.getMessage());
//			System.out.println("HTTP Status Code: " + ase.getStatusCode());
//			System.out.println("AWS Error Code:   " + ase.getErrorCode());
//			System.out.println("Error Type:       " + ase.getErrorType());
//			System.out.println("Request ID:       " + ase.getRequestId());
//		} catch (AmazonClientException ace) {
//			System.out.println("Caught an AmazonClientException, which means the client encountered "
//					+ "a serious internal problem while trying to communicate with S3, "
//					+ "such as not being able to access the network.");
//			System.out.println("Error Message: " + ace.getMessage());
//		}
//	}
//
//	public static HashMap<String, Long> listObjects(String clientName) {
//		AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());
//
//		HashMap<String, Long> s3objects = new HashMap<String, Long>();
//		
//		try {
//			Connection conn = MecVerticaMethods.connect();
//
//			MecClient client = MecVerticaMethods.getClient(conn, clientName);
//
//			String s3prefix = client.getS3Prefix();
//
//			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(BUCKET_NAME)
//					.withPrefix(s3prefix);
//			ObjectListing objectListing;
//
//			do {
//				objectListing = s3.listObjects(listObjectsRequest);
//				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
//					s3objects.put(objectSummary.getKey(), objectSummary.getSize());
//				}
//				listObjectsRequest.setMarker(objectListing.getNextMarker());
//			} while (objectListing.isTruncated());
//
//			// disconnect from Vertica
//			MecVerticaMethods.disconnect(conn);
//		} catch (AmazonServiceException ase) {
//			System.out.println("Caught an AmazonServiceException, which means your request made it "
//					+ "to Amazon S3, but was rejected with an error response for some reason.");
//			System.out.println("Error Message:    " + ase.getMessage());
//			System.out.println("HTTP Status Code: " + ase.getStatusCode());
//			System.out.println("AWS Error Code:   " + ase.getErrorCode());
//			System.out.println("Error Type:       " + ase.getErrorType());
//			System.out.println("Request ID:       " + ase.getRequestId());
//		} catch (AmazonClientException ace) {
//			System.out.println("Caught an AmazonClientException, which means the client encountered "
//					+ "a serious internal problem while trying to communicate with S3, "
//					+ "such as not being able to access the network.");
//			System.out.println("Error Message: " + ace.getMessage());
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		return s3objects;
//	}
//
//	// download files not listed in table DIM_FILE
//	public static void downloadObjects() {
//
//	}
//
//	public static void listPartialUploads() {
//
//	}
//
//	public static void resumePartialUploads() {
//
//	}
//
//	public static void abortPartialUploads() {
//
//	}
}
