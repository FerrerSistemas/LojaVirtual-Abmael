package com.lojavirtualabmael.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@Service
public class S3Service {

	@Autowired
	private AmazonS3 s3Client;

	@Value("${s3.bucket}")
	String bucketName;

	private Logger LOG = LoggerFactory.getLogger(S3Service.class);

	public URI upLoadFile(MultipartFile multipartFile) {
		try {
			
			String fileName = multipartFile.getOriginalFilename();			
			InputStream is = multipartFile.getInputStream();
			String contentType = multipartFile.getContentType();
			
			return upLoadFile(is, fileName, contentType);
			
			} catch (IOException e) {
				throw new RuntimeException("Erro de IO "+ e.getMessage());
			}
			
	}

	public URI upLoadFile (InputStream is, String fileName, String contentType) {
		
		try {
		
			ObjectMetadata meta = new ObjectMetadata();
			
			LOG.info("Iniciando o Upload");	
			s3Client.putObject(new PutObjectRequest(bucketName, fileName, is, meta));				
			LOG.info("Upload Finalizado");
			
			
				return s3Client.getUrl(bucketName, fileName).toURI();
			} catch (URISyntaxException e) {
				throw new  RuntimeException("Erro ao converter  URL  para URI");
			}
	

	}
}

	
	
	/*
	
	
} catch(

		AmazonServiceException e)
{
	LOG.info("AmazonServiceException " + e.getErrorMessage());
	LOG.info("Status code " + e.getErrorCode());
}catch(
		AmazonClientException e)
{
	LOG.info("AmazonClientException " + e.getMessage());

}

*/