package com.white.backend.file.image.repository;

import com.white.backend.file.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;

public interface ImageRepository extends JpaRepository<Image, String> {

    void deleteByPublicId(@NonNull String publicId);

}