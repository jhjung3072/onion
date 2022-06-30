package com.onion.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;


import com.onion.FileUploadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;


//아마존S3 파일 업로드 및 삭제 클래스
public class AmazonS3Util {
	private static final Logger LOGGER = LoggerFactory.getLogger(FileUploadUtil.class);
	private static final String BUCKET_NAME;
	
	static {
		BUCKET_NAME=System.getenv("AWS_BUCKET_NAME");
	}
	
	// 폴더 내 파일 리스트
	public static List<String> listFolder(String folderName) {
		S3Client client= S3Client.builder().build();
		ListObjectsRequest listRequest= ListObjectsRequest.builder()
					.bucket(BUCKET_NAME).prefix(folderName).build();
		
		ListObjectsResponse response=client.listObjects(listRequest);
		
		List<S3Object>contents=response.contents();
		ListIterator<S3Object> listIterator=contents.listIterator();
		
		List<String> listKeys=new ArrayList<>();
		while (listIterator.hasNext()) {
			S3Object object=listIterator.next();
			listKeys.add(object.key());
			
		}
		return listKeys;
	}
	
	// 파일 업로드
	public static void uploadFile(String folderName, String fileName,InputStream inputStream) {
		S3Client client=S3Client.builder().build();
		
		PutObjectRequest request=PutObjectRequest.builder().bucket(BUCKET_NAME)
				.key(folderName+"/"+fileName).acl("public-read").build(); // 경로 및 public 설정
		
		try(inputStream){
			int contentLength=inputStream.available();
			client.putObject(request, RequestBody.fromInputStream(inputStream, contentLength));
		}catch(IOException ex) {
			LOGGER.error("Amazon S3에 업로드할 수 없습니다.",ex);
		}
	}
	
	// 파일 삭제
	public static void deleteFile(String fileName) {
		S3Client client=S3Client.builder().build();
		
		DeleteObjectRequest request=DeleteObjectRequest.builder().bucket(BUCKET_NAME)
				.key(fileName).build();
		
		client.deleteObject(request);
	}
	
	// 폴더 삭제
	public static void removeFolder(String folderName) {
		S3Client client=S3Client.builder().build();
		ListObjectsRequest listRequest=ListObjectsRequest.builder()
					.bucket(BUCKET_NAME).prefix(folderName).build();
		
		ListObjectsResponse response=client.listObjects(listRequest);
		
		List<S3Object>contents=response.contents();
		ListIterator<S3Object>listIterator=contents.listIterator();
		
		while (listIterator.hasNext()) {
			S3Object object=listIterator.next();
			DeleteObjectRequest request=DeleteObjectRequest.builder().bucket(BUCKET_NAME)
					.key(object.key()).build();
			
			client.deleteObject(request);
			System.out.println("삭제했습니다 : " + object.key());
		}
	}
}
