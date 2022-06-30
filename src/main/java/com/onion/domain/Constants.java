package com.onion.domain;

// 아마존 S3 연결을 위한 환경변수 값 불러오기
public class Constants {
	public static final String S3_BASE_URI;
	
	static {
		String bucketName=System.getenv("AWS_BUCKET_NAME");
		String region=System.getenv("AWS_REGION");
		String pattern="https://%s.s3.%s.amazonaws.com";
		
		S3_BASE_URI=bucketName==null ? "" : String.format(pattern, bucketName,region);
	}
	
}
