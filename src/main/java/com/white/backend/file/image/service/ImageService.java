package com.white.backend.file.image.service;

import com.cloudinary.Cloudinary;
import com.white.backend.file.image.dto.ImageResponseDto;
import com.white.backend.file.image.entity.Image;
import com.white.backend.file.image.error.FileError;
import com.white.backend.file.image.repository.ImageRepository;
import com.white.backend.shared.exception.HttpResponseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Service class for managing image-related operations.
 * Handles image uploads to the Cloudinary cloud service, deletion of images from Cloudinary,
 * and storage/retrieval of image metadata in the database.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final Cloudinary cloudinary;  // Cloudinary instance for uploading and deleting images
    private final ImageRepository imageRepository;  // Repository for managing image records in the database

    /**
     * Retrieves a paginated list of images.
     *
     * @param pageable contains pagination and sorting information (page number, size, sorting)
     * @return a page of ImageResponseDto objects containing image metadata
     */
    public Page<ImageResponseDto> getImagePage(Pageable pageable) {
        try {
            log.info("{}, Fetching images - page: {}, size: {}", this.getClass().getSimpleName(), pageable.getPageNumber(), pageable.getPageSize());

            // Retrieve all images from the database according to the pagination settings and map each Image entity to an ImageResponseDto
            return imageRepository.findAll(pageable).map(ImageResponseDto::from);

        } catch (HttpResponseException e) {
            log.error("Error fetching images: {}", e.getMessage());

            // Propagate the custom HttpResponseException for further handling by calling code
            throw e;
        }
    }

    /**
     * Uploads an image file to Cloudinary and saves its metadata in the database.
     *
     * @param file the image file to be uploaded, provided as a MultipartFile
     * @return the URL of the uploaded image as stored in Cloudinary
     * @throws IOException if there is an issue during file upload or file processing
     */
    public String uploadImageFile(MultipartFile file) throws IOException {

        try {

            log.info("{}, Uploading image file: {}", this.getClass().getSimpleName(), file.getName());

            // Ensure that the uploaded file is of type image (checking MIME type)
            if (!Objects.requireNonNull(file.getContentType()).startsWith("image")) {
                throw FileError.FILE_IS_NOT_IMAGE.exception();  // Throw custom error if file is not an image
            }

            // Generate a unique public ID for the image file using UUID and upload it to Cloudinary
            String imageId = cloudinary.uploader()
                    .upload(file.getBytes(), Map.of("public_id", UUID.randomUUID().toString())) // Convert file to bytes for upload
                    .get("public_id")
                    .toString();

            // Generate a URL for the uploaded image in Cloudinary using the image ID
            String url = cloudinary.url().generate(imageId);

            // Get File extension
            String extension = Objects.requireNonNull(file.getOriginalFilename()).substring(file.getOriginalFilename().lastIndexOf(".") + 1);

            // Create a new Image entity with the file's metadata to store in the database
            Image image = Image.builder()
                    .publicId(imageId)                  // Cloudinary public ID for identifying the image
                    .name(file.getOriginalFilename())    // Original filename of the uploaded file
                    .url(url)                           // URL of the uploaded image in Cloudinary
                    .size(file.getSize())               // File size in bytes
                    .extension(extension)    // MIME type of the file (e.g., "image/jpeg")
                    .build();

            // Save the image metadata in the database using ImageRepository
            imageRepository.save(image);

            log.info("{}, Image successfully uploaded: {}", this.getClass().getSimpleName(), url);

            // Return the URL of the uploaded image
            return url;

        } catch (HttpResponseException e) {
            log.error("Error uploading image: {}", e.getMessage());

            // Propagate HttpResponseException to handle the error in calling code
            throw e;

        }

    }

    /**
     * Deletes an image from Cloudinary and removes its metadata from the database.
     *
     * @param publicId the unique public ID of the image to be deleted from Cloudinary
     * @return a string indicating the result of the deletion from Cloudinary
     * @throws FileError custom error thrown if there are issues during image deletion
     */
    @Transactional
    // Marks the operation as transactional to ensure data consistency between Cloudinary and the database
    public String deleteImageFile(String publicId) {

        try {

            log.info("{}, Deleting image with public ID: {}", this.getClass().getSimpleName(), publicId);

            // Send request to Cloudinary to delete the image and invalidate any cached versions
            String deletedId = cloudinary.uploader()
                    .destroy(publicId, Map.of("invalidate", true))
                    .get("result")
                    .toString();

            // Remove the image record from the database by public ID
            imageRepository.deleteByPublicId(publicId);

            log.info("{}, Image successfully deleted: {}", this.getClass().getSimpleName(), publicId);

            // Return the result from Cloudinary deletion request
            return deletedId;

        } catch (HttpResponseException e) {
            log.error("Error while deleting image: {}", e.getMessage());

            // Propagate HttpResponseException to notify calling code of error
            throw e;

        } catch (IOException e) {
            log.error("IOException occurred while deleting file: {}", e.getMessage());

            // Throw custom error defined in FileError for issues during deletion
            throw FileError.HAVE_ERROR_WHILE_DELETING_FILE.exception();
        }

    }

}
