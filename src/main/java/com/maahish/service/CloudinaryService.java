package com.maahish.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.maahish.config.CloudinaryProperties;
import com.maahish.dto.response.FileUploadResponse;
import com.maahish.exception.BadRequestException;
import com.maahish.exception.CloudinaryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private final Cloudinary cloudinary;
    private final CloudinaryProperties cloudinaryProperties;

    public FileUploadResponse uploadProductImage(MultipartFile file) {
        String publicId = cloudinaryProperties.getFolder() + "/" + UUID.randomUUID();
        return uploadImage(file, publicId);
    }

    public FileUploadResponse uploadReviewPhoto(MultipartFile file) {
        String publicId = cloudinaryProperties.getFolder() + "/reviews/" + UUID.randomUUID();
        return uploadImage(file, publicId);
    }

    public FileUploadResponse uploadSellerBusinessLogo(MultipartFile file) {
        String publicId = cloudinaryProperties.getFolder() + "/sellers/logos/" + UUID.randomUUID();
        return uploadImage(file, publicId);
    }

    public FileUploadResponse uploadSellerProfileImage(MultipartFile file) {
        String publicId = cloudinaryProperties.getFolder() + "/sellers/profiles/" + UUID.randomUUID();
        return uploadImage(file, publicId);
    }

    private FileUploadResponse uploadImage(MultipartFile file, String publicId) {
        validateFile(file);
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "image",
                            "overwrite", false
                    )
            );

            String secureUrl = (String) result.get("secure_url");
            String storedPublicId = (String) result.get("public_id");
            if (!StringUtils.hasText(secureUrl) || !StringUtils.hasText(storedPublicId)) {
                throw new CloudinaryException("Cloudinary upload succeeded but returned an incomplete response");
            }

            return FileUploadResponse.builder()
                    .url(secureUrl)
                    .publicId(storedPublicId)
                    .filename(storedPublicId)
                    .originalFilename(StringUtils.cleanPath(file.getOriginalFilename()))
                    .build();
        } catch (IOException ex) {
            throw new BadRequestException("Failed to read image file");
        } catch (BadRequestException | CloudinaryException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new CloudinaryException("Cloudinary upload failed: " + ex.getMessage(), ex);
        }
    }

    public void deleteImage(String publicId) {
        if (!StringUtils.hasText(publicId)) {
            return;
        }
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = cloudinary.uploader().destroy(
                    publicId,
                    ObjectUtils.asMap("resource_type", "image", "invalidate", true)
            );
            String resultStatus = result.get("result") != null ? result.get("result").toString() : "";
            if (!"ok".equals(resultStatus) && !"not found".equals(resultStatus)) {
                log.warn("Unexpected Cloudinary delete result for {}: {}", publicId, resultStatus);
            }
        } catch (Exception ex) {
            log.warn("Failed to delete Cloudinary image {}: {}", publicId, ex.getMessage());
        }
    }

    public String extractPublicIdFromUrl(String url) {
        if (!StringUtils.hasText(url) || !url.contains("res.cloudinary.com")) {
            return null;
        }
        int uploadIdx = url.indexOf("/upload/");
        if (uploadIdx < 0) {
            return null;
        }
        String path = url.substring(uploadIdx + "/upload/".length());
        if (path.matches("^v\\d+/.*")) {
            path = path.replaceFirst("^v\\d+/", "");
        }
        int dot = path.lastIndexOf('.');
        int slash = path.lastIndexOf('/');
        if (dot > slash) {
            path = path.substring(0, dot);
        }
        return StringUtils.hasText(path) ? path : null;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Please select an image file");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("Image must be smaller than 10 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new BadRequestException("Only JPG, PNG, WEBP, and GIF images are allowed");
        }
    }
}
