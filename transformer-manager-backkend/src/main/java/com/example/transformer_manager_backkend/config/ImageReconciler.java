package com.example.transformer_manager_backkend.config;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.transformer_manager_backkend.entity.Image;
import com.example.transformer_manager_backkend.repository.ImageRepository;

@Component
public class ImageReconciler implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ImageReconciler.class);

    private final ImageRepository imageRepository;

    @Value("${upload.directory}")
    private String uploadDir;

    public ImageReconciler(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // On startup, attempt to ensure images referenced in DB can be served.
        // If an original is missing locally but a boxed variant exists under
        // uploads/analysis, update the image path to the boxed variant so the UI
        // can render something useful.
        for (Image img : imageRepository.findAll()) {
            String filePath = img.getFilePath();
            if (filePath == null || !filePath.startsWith("/uploads/")) {
                continue;
            }

            String relative = filePath.substring("/uploads/".length());
            if (isReadable(Paths.get(uploadDir).resolve(relative))) {
                continue; // ok
            }

            // Also try repo-level ../uploads fallback
            if (isReadable(Paths.get(uploadDir).getParent() != null
                    ? Paths.get(uploadDir).getParent().resolve("uploads").resolve(relative)
                    : Paths.get("../uploads").resolve(relative))) {
                continue; // served via WebConfig fallback
            }

            // Try boxed variant under analysis folder
            String baseName = stripExtension(relative);
            String boxedNameJpg = baseName + "_boxed.jpg";
            String boxedNamePng = baseName + "_boxed.png";

            Path boxedLocal = Paths.get(uploadDir, "analysis").resolve(boxedNameJpg);
            Path boxedLocalPng = Paths.get(uploadDir, "analysis").resolve(boxedNamePng);

            Path boxedRepo = Paths.get(uploadDir).getParent() != null
                    ? Paths.get(uploadDir).getParent().resolve("uploads").resolve("analysis").resolve(boxedNameJpg)
                    : Paths.get("../uploads/analysis").resolve(boxedNameJpg);
            Path boxedRepoPng = Paths.get(uploadDir).getParent() != null
                    ? Paths.get(uploadDir).getParent().resolve("uploads").resolve("analysis").resolve(boxedNamePng)
                    : Paths.get("../uploads/analysis").resolve(boxedNamePng);

            Path chosen = null;
            if (isReadable(boxedLocal)) {
                chosen = boxedLocal;
            } else if (isReadable(boxedLocalPng)) {
                chosen = boxedLocalPng;
            } else if (isReadable(boxedRepo)) {
                chosen = boxedRepo;
            } else if (isReadable(boxedRepoPng)) {
                chosen = boxedRepoPng;
            }

            if (chosen != null) {
                String newWebPath = "/uploads/analysis/" + chosen.getFileName().toString();
                logger.info("Re-linking missing image {} -> {}", filePath, newWebPath);
                img.setFilePath(newWebPath);
                imageRepository.save(img);
            } else {
                logger.warn("Image missing and no boxed variant found: {}", filePath);
            }
        }
    }

    private boolean isReadable(Path p) {
        try {
            return p != null && Files.exists(p) && Files.isReadable(p);
        } catch (Exception e) {
            return false;
        }
    }

    private String stripExtension(String name) {
        int slash = name.lastIndexOf('/') >= 0 ? name.lastIndexOf('/') : name.lastIndexOf('\\');
        String fileOnly = slash >= 0 ? name.substring(slash + 1) : name;
        int dot = fileOnly.lastIndexOf('.');
        if (dot > 0) {
            fileOnly = fileOnly.substring(0, dot);
        }
        return fileOnly;
    }
}
