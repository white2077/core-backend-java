package com.white.backend.file.image.controller;

import com.white.backend.file.image.dto.ImageResponseDto;
import com.white.backend.file.image.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/file/image")
@RequiredArgsConstructor
@Tag(name = "Image - API", description = "All operations related to image upload")
public class ImageController {

    private final ImageService imageService;

    @GetMapping
    @Operation(
            summary = "Retrieve paginated list of images",
            description = "Returns a paginated list of images stored in the system. " +
                    "This includes metadata such as image URL, name, size, and extension.")
    public Page<ImageResponseDto> getImages(Pageable pageable) {

        return imageService.getImagePage(pageable);

    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Upload an image file",
            description = "Uploads an image file to the cloud storage. Only images with valid formats (e.g., JPG, PNG) are allowed.")
    public String uploadFile(@RequestPart("image") MultipartFile image) throws IOException {

        return imageService.uploadImageFile(image);

    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an image file",
            description = "Deletes an image file based on its unique public ID. " +
                    "If the image exists, it will be permanently removed from cloud storage.")
    public String deleteImage(@PathVariable("id") String id) {

        return "Image deleted " + imageService.deleteImageFile(id);

    }

}
