package com.onion.product;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.onion.FileUploadUtil;
import com.onion.config.AmazonS3Util;
import com.onion.domain.product.Product;
import com.onion.domain.product.ProductImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;


public class ProductSaveHelper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductSaveHelper.class);

	// 보조 이미지 삭제
	static void deleteExtraImagesWeredRemovedOnForm(Product product) {
		String extraImageDir = "product-images/" + product.getId() + "/extras";
		List<String> listObjectKeys=AmazonS3Util.listFolder(extraImageDir);

		// 삭제 방식 : 예) product-images/1/카메라.jpg -> 마지막 슬래쉬"/" 부터 파일이름
		for (String objectKey:listObjectKeys) {
			int lastIndexOfSlash=objectKey.lastIndexOf("/");
			String fileName=objectKey.substring(lastIndexOfSlash+1,objectKey.length());

			// 상품 이미지에 존재하지 않는다면(삭제되었다면)
			if (!product.containsImageName(fileName)) {
				// 이미지 파일 삭제
				AmazonS3Util.deleteFile(objectKey);
				System.out.println("보조 이미지 삭제됨: "+ objectKey);
			}
		}
	}

	// 보조이미지 파일명 설정
	static void setExistingExtraImageNames(String[] imageIDs, String[] imageNames,
										   Product product) {
		if (imageIDs == null || imageIDs.length == 0) return;

		Set<ProductImage> images = new HashSet<>();

		// 해당 메인 이미지 길이 만큼 반복하면서 이미지 이름 설정
		for (int count = 0; count < imageIDs.length; count++) {
			Integer id = Integer.parseInt(imageIDs[count]);
			String name = imageNames[count];

			images.add(new ProductImage(id, name, product));
		}

		product.setImages(images);

	}



	// 상품 이미지 저장
	static void saveUploadedImages(MultipartFile mainImageMultipart,
								   MultipartFile[] extraImageMultiparts, Product savedProduct) throws IOException {
		if (!mainImageMultipart.isEmpty()) {
			String fileName = StringUtils.cleanPath(mainImageMultipart.getOriginalFilename());
			String uploadDir = "product-images/" + savedProduct.getId();

			List<String> listObjectKeys=AmazonS3Util.listFolder(uploadDir);

			//메인 이미지 저장
			// 폴더 내부를 순환하면서 기존의 메인 이미지를 삭제후 재업로드
			for (String objectKey:listObjectKeys) {
				// "/extras/" 가 아니라면 메인 이미지라는 의미
				if (!objectKey.contains("/extras/")) {
					AmazonS3Util.deleteFile(objectKey);
				}
			}
			AmazonS3Util.uploadFile(uploadDir, fileName, mainImageMultipart.getInputStream());
		}

		// 보조 이미지가 있다면,
		if (extraImageMultiparts.length > 0) {
			String uploadDir = "product-images/" + savedProduct.getId() + "/extras";

			for (MultipartFile multipartFile : extraImageMultiparts) {
				if (multipartFile.isEmpty()) continue;

				String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
				AmazonS3Util.uploadFile(uploadDir, fileName, multipartFile.getInputStream());
			}
		}

	}

	// 새 보조이미지 파일명 설정
	static void setNewExtraImageNames(MultipartFile[] extraImageMultiparts, Product product) {
		if (extraImageMultiparts.length > 0) {
			for (MultipartFile multipartFile : extraImageMultiparts) {
				if (!multipartFile.isEmpty()) {
					String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());
					// 기존에 등록되지 않았다면 추가하기
					if (!product.containsImageName(fileName)) {
						product.addExtraImage(fileName);
					}
				}
			}
		}
	}

	// 메인 이미지 파일명 설정
	static void setMainImageName(MultipartFile mainImageMultipart, Product product) {
		if (!mainImageMultipart.isEmpty()) {
			String fileName = StringUtils.cleanPath(mainImageMultipart.getOriginalFilename());
			product.setMainImage(fileName);
		}
	}
}
