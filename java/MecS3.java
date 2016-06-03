//package com.mecglobal.s3automation.java;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadRequest;
import com.amazonaws.services.s3.model.InitiateMultipartUploadResult;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.PartETag;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class MecS3 implements MecServer, MecFileAcquirer{

	private String bucketName;
	private String keyName;
	private String s3Prefix;
	private String nfsDirectory;


	public MecS3(String bucketName, String keyName, String s3Prefix) {
		super();
		this.bucketName = bucketName;
		this.keyName = keyName;
		this.s3Prefix = s3Prefix;
	}

	public MecS3() {

	}


	@Override
	public String toString() {
		return "MecS3 [bucketName=" + bucketName + ", keyName=" + keyName + ", s3Prefix=" + s3Prefix + "]";
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public String getKeyName() {
		return keyName;
	}

	public void setKeyName(String keyName) {
		this.keyName = keyName;
	}

	public String getS3Prefix() {
		return s3Prefix;
	}

	public void setS3Prefix(String s3Prefix) {
		this.s3Prefix = s3Prefix;
	}

	public String getNfsDirectory() {
		return nfsDirectory;
	}

	public void setNfsDirectory(String nfsDirectory) {
		this.nfsDirectory = nfsDirectory;
	}

	@Override
	public void connect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ArrayList<MecFile> listFiles() {
		AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());

		ArrayList<MecFile> files = new ArrayList<MecFile>();
		
		try {
			ListObjectsRequest listObjectsRequest = new ListObjectsRequest().withBucketName(this.bucketName)
					.withPrefix(this.s3Prefix);
			ObjectListing objectListing;

			do {
				objectListing = s3.listObjects(listObjectsRequest);
				for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
					MecFile s3File = new MecFile(objectSummary.getKey());
					files.add(s3File);
				}
				listObjectsRequest.setMarker(objectListing.getNextMarker());
			} while (objectListing.isTruncated());

		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
		return files;
	}

	@Override
	public void downloadFile(String fileName) {
		AmazonS3 s3 = new AmazonS3Client(new ProfileCredentialsProvider());
		
  		String keyName = fileName;	
  		String[] bits = fileName.split("/"); 
    	String keyFileName = bits[bits.length-1];
    	
    	try {
	        System.out.println("Downloading " + keyFileName);//an object");
	        S3Object object = s3.getObject(new GetObjectRequest(this.bucketName, keyName));
	        System.out.println("Content-Type: "  + object.getObjectMetadata().getContentType());
	  
			InputStream reader = new BufferedInputStream(object.getObjectContent());
			byte[] buf = new byte[1024];
			File file = new File(this.nfsDirectory + keyFileName);			
			OutputStream writer = new BufferedOutputStream(new FileOutputStream(file));
	
			int count;
			while((count = reader.read(buf)) != -1){
				if(Thread.interrupted()){
					throw new InterruptedException();
				}
				writer.write(buf, 0, count);
			}
	      
			writer.flush();
	        writer.close();
	        reader.close();
    	} catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
	
	public void uploadFiles(String directoryPath) {
		
		try {
			File dir = new File(directoryPath);
			HashMap<String, MecFile> s3objects = new HashMap<String, MecFile>();
			
			if(dir.isDirectory()){
		        File[] zipFiles = dir.listFiles();
				Arrays.sort(zipFiles);
		        for(File file : zipFiles){
		        	if(file.isFile()){
		        		String keyName = this.s3Prefix + file.getName(); 
		        		s3objects.put(keyName, new MecFile(file.getName(), file.length()));
		            }
		        }
			}

			Map<String, Long> filesForQueue = new HashMap<String, Long>();

			Iterator<Map.Entry<String, MecFile>> it = s3objects.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, MecFile> fileToLoad = (Map.Entry<String, MecFile>) it.next();
				filesForQueue.put(fileToLoad.getValue().getFileName(),
						fileToLoad.getValue().getFileSize());
				it.remove();
			}

			SortedSet<String> keys = new TreeSet<String>(filesForQueue.keySet());
			for (String key : keys) {
		    	System.out.println("Loading " + key + " - Size: "+ filesForQueue.get(key));
		    	if (filesForQueue.get(key)>(5*1024*1024)){
		    		System.out.println(key + " is a big file");
		    		uploadBigFile(key, directoryPath, this.s3Prefix);
		    		System.out.println("Deleting " + key + " ..");
		    		Path filePath = Paths.get(directoryPath + key);
		    		try {
		    		    Files.delete(filePath);
		    		    System.out.println(filePath + " has been deleted");
		    		} catch (NoSuchFileException x) {
		    		    System.err.format("%s: no such" + " file or directory%n", filePath);
		    		} catch (DirectoryNotEmptyException x) {
		    		    System.err.format("%s not empty%n", filePath);
		    		} catch (IOException x) {
		    		    // File permission problems are caught here.
		    		    System.err.println(x);
		    		}
		    	} else {
		    		System.out.println(key + " is a small file");
		    		uploadSmallFile(key, directoryPath, this.s3Prefix);
		    		System.out.println("Deleting " + key + " ..");
		    		Path filePath = Paths.get(directoryPath + key);
		    		try {
		    		    Files.delete(filePath);
		    		    System.out.println(filePath + " has been deleted");
		    		} catch (NoSuchFileException x) {
		    		    System.err.format("%s: no such" + " file or directory%n", filePath);
		    		} catch (DirectoryNotEmptyException x) {
		    		    System.err.format("%s not empty%n", filePath);
		    		} catch (IOException x) {
		    		    // File permission problems are caught here.
		    		    System.err.println(x);
		    		}
		    	}
			}
	    } catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void uploadBigFile(String fileName, String genFilePath, String s3prefix) {
        AWSCredentials credentials = null;
        try {
            credentials = new ProfileCredentialsProvider("default").getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                    "Please make sure that your credentials file is at the correct " +
                    "location, and is in valid format.",
                    e);
        }
		AmazonS3 s3Client = new AmazonS3Client(credentials);
		File file = new File(genFilePath + fileName);
		String key = s3prefix + fileName;

		// Create a list of UploadPartResponse objects. You get one of these for
		// each part upload.
		List<PartETag> partETags = new ArrayList<PartETag>();
		System.out.println("Initializing multi-part request");

		// Step 1: Initialize.
		InitiateMultipartUploadRequest initRequest = new InitiateMultipartUploadRequest(this.bucketName, key);
		InitiateMultipartUploadResult initResponse = s3Client.initiateMultipartUpload(initRequest);
		long contentLength = file.length();
		long partSize = 5 * 1024 * 1024; // Set part size to 5 MB.

		try {
			// Step 2: Upload parts.
			System.out.println("Uploading parts");
			long filePosition = 0;

			ExecutorService pool = Executors.newFixedThreadPool(10);
			List<Future<Object>> partList = new ArrayList<Future<Object>>();

			for (int i = 1; filePosition < contentLength; i++) {
				// Last part can be less than 5 MB. Adjust part size.
				partSize = Math.min(partSize, (contentLength - filePosition));
				String uploadId = initResponse.getUploadId();
				Callable<Object> partUpload = new MecS3UploadPart(this.bucketName, key, i, filePosition, file, partSize,
						uploadId);
				Future<Object> f = pool.submit(partUpload);
				partList.add(f);
				filePosition += partSize;
			}

			for (Future<Object> f : partList) {
				partETags.add((PartETag) f.get());
			}
			
			// closing thread pool
			pool.shutdown();
			try {
				pool.awaitTermination(60, TimeUnit.MINUTES);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			// Step 3: Complete.
			System.out.println("Completing request.");
			CompleteMultipartUploadRequest compRequest = new CompleteMultipartUploadRequest(this.bucketName, key,
					initResponse.getUploadId(), partETags);
			s3Client.completeMultipartUpload(compRequest);
		} catch (Exception e) {
			System.out.println("Aborting multi part upload.");
			s3Client.abortMultipartUpload(
					new AbortMultipartUploadRequest(this.bucketName, key, initResponse.getUploadId()));
		}
	}

	public void uploadSmallFile(String fileName, String genFilePath, String s3prefix) {
      AWSCredentials credentials = null;
      try {
          credentials = new ProfileCredentialsProvider("default").getCredentials();
      } catch (Exception e) {
          throw new AmazonClientException(
                  "Cannot load the credentials from the credential profiles file. " +
                  "Please make sure that your credentials file is at the correct " +
                  "location, and is in valid format.",
                  e);
      }
		AmazonS3 s3 = new AmazonS3Client(credentials);
		File file = new File(genFilePath + fileName);
		String key = s3prefix + fileName;

		try {
			s3.putObject(new PutObjectRequest(this.bucketName, key, file));
		} catch (AmazonServiceException ase) {
			System.out.println("Caught an AmazonServiceException, which means your request made it "
					+ "to Amazon S3, but was rejected with an error response for some reason.");
			System.out.println("Error Message:    " + ase.getMessage());
			System.out.println("HTTP Status Code: " + ase.getStatusCode());
			System.out.println("AWS Error Code:   " + ase.getErrorCode());
			System.out.println("Error Type:       " + ase.getErrorType());
			System.out.println("Request ID:       " + ase.getRequestId());
		} catch (AmazonClientException ace) {
			System.out.println("Caught an AmazonClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with S3, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message: " + ace.getMessage());
		}
	}
}
