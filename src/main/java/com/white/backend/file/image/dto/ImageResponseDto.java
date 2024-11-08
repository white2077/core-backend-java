package com.white.backend.file.image.dto;

import com.white.backend.file.image.entity.Image;
import lombok.Builder;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * DTO for {@link com.white.backend.file.image.entity.Image}
 */
@Builder
public record ImageResponseDto(

        String publicId,

        String name,

        String url,

        Long size,

        String extension,

        Timestamp createdAt,

        Timestamp updatedAt,

        Timestamp deletedAt,

        Boolean isDeleted

) implements Serializable {

    public static ImageResponseDto from(Image image) {

        return ImageResponseDto.builder()

                .publicId(image.getPublicId())

                .name(image.getName())

                .url(image.getUrl())

                .size(image.getSize())

                .extension(image.getExtension())

                .createdAt(image.getCreatedAt())

                .updatedAt(image.getUpdatedAt())

                .deletedAt(image.getDeletedAt())

                .isDeleted(image.getIsDeleted())

                .build();

    }

}