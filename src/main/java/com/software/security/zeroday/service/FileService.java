package com.software.security.zeroday.service;

import com.software.security.zeroday.dto.file.FileDTO;
import com.software.security.zeroday.dto.file.FileIdDTO;
import com.software.security.zeroday.entity.File;
import com.software.security.zeroday.entity.Post;
import com.software.security.zeroday.entity.User;
import com.software.security.zeroday.entity.enumeration.FileExtension;
import com.software.security.zeroday.entity.enumeration.FileType;
import com.software.security.zeroday.repository.FileRepository;
import com.software.security.zeroday.security.util.SanitizationUtil;
import com.software.security.zeroday.service.exception.ConstraintException;
import com.software.security.zeroday.service.exception.FileNotFoundException;
import com.software.security.zeroday.service.exception.InvalidFileException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Transactional
@AllArgsConstructor
@Service
public class FileService {
    private final FileRepository fileRepository;
    private final SanitizationUtil sanitizationUtil;

    private final Path rootLocation = Paths.get("src/main/resources/static");

    public FileIdDTO uploadFile(MultipartFile file) {
        return saveFile(file, FileExtension.values());
    }

    public FileIdDTO uploadProfilePicture(MultipartFile file) {
        return saveFile(file, new FileExtension[]{
            FileExtension.JPEG,
            FileExtension.JPG,
            FileExtension.PNG,
            FileExtension.GIF,
            FileExtension.WEBP,
            FileExtension.SVG,
            FileExtension.HEIC
        });
    }

    private FileIdDTO saveFile(MultipartFile file, FileExtension[] allowedFileExtensions) {
        if (file == null || file.isEmpty())
            throw new InvalidFileException("File is empty or missing");

        String contentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();

        if (contentType == null || originalFilename == null || originalFilename.isBlank())
            throw new InvalidFileException("Content type or filename is missing");

        String fileExtension = this.extractFileExtension(originalFilename);
        this.validateMimeTypeAndExtension(contentType, fileExtension, allowedFileExtensions);

        String uniqueFileName = this.generateUniqueFileName();

        File tempFile = File.builder()
            .name(uniqueFileName)
            .extension(FileExtension.valueOf(fileExtension.toUpperCase()))
            .size(file.getSize())
            .uploadedAt(Instant.now())
            .processed(false)
            .build();

        this.validateFileSize(file.getSize(), tempFile.getType());

        if (FileExtension.SVG.getMimeType().equalsIgnoreCase(contentType)) {
            String content = this.sanitizationUtil.sanitizeSvg(file);
            this.saveSanitizedSvg(content, uniqueFileName, fileExtension);
        } else {
            this.saveFile(file, uniqueFileName, fileExtension);
        }

        tempFile = this.fileRepository.save(tempFile);

        return FileIdDTO.builder().id(tempFile.getId()).build();

    }

    private void validateMimeTypeAndExtension(String contentType, String fileExtension, FileExtension[] allowedFileExtensions) {
        boolean isValid = Stream.of(allowedFileExtensions)
            .anyMatch(extension ->
                extension.getMimeType().equalsIgnoreCase(contentType) &&
                    extension.name().equalsIgnoreCase(fileExtension)
            );

        if (!isValid)
            throw new InvalidFileException("Unsupported file type or extension, only " + Stream.of(allowedFileExtensions)
                .map(FileExtension::name)
                .reduce((a, b) -> a + ", " + b)
                .orElseThrow() + " are allowed");
    }


    private String extractFileExtension(String filename) {
        if (!filename.contains("."))
            throw new InvalidFileException("Extension not found");

        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private String generateUniqueFileName() {
        String uniqueFileName;
        do uniqueFileName = UUID.randomUUID().toString();
        while (this.fileRepository.existsByName(uniqueFileName));

        return uniqueFileName;
    }

    private void validateFileSize(long size, FileType type) {
        // image: 5MB, video: 100MB
        long maxSize = type == FileType.IMAGE ? 5 * 1024 * 1024 : 100 * 1024 * 1024;
        if (size > maxSize)
            throw new MaxUploadSizeExceededException(maxSize);
    }

    public File finalizeProfilePictureUpload(Long fileId, User user, Post post) {
        if (user != null) {
            File oldProfilePicture = user.getPicture();
            if (oldProfilePicture != null) {
                oldProfilePicture.setUser(null);
                this.fileRepository.save(oldProfilePicture);
            }
        }

        File file = this.validateAndFetchFile(fileId);

        this.moveFile(file);

        file.setProcessed(true);
        file.setUser(user);
        file.setPost(post);

        return this.fileRepository.save(file);
    }

    public File finalizeFileUpload(Long fileId, Post post) {
        File file = this.validateAndFetchFile(fileId);

        if (post.getFile() != null)
            this.removePostOldFile(post.getFile());

        this.moveFile(file);

        file.setProcessed(true);
        file.setPost(post);

        return this.fileRepository.save(file);
    }

    private File validateAndFetchFile(Long fileId) {
        return fileRepository.findById(fileId)
            .orElseThrow(() -> new RuntimeException("Temporary file not found in the database"));
    }

    private void removePostOldFile(File oldFile) {
        if (oldFile.getUser() != null)
            throw new ConstraintException("The file is associated with a user profile picture, you cannot delete or modify the file");

        this.deleteFile(oldFile);
    }

    public String getProfilePicture(Long userId) {
        return this.fileRepository.findByUser(userId)
            .filter(file -> file.getType() == FileType.IMAGE)
            .map(this::getFileName)
            .orElse("user.png");
    }

    public void removePostFileAndAllDescendantsFiles(Post post) {
        Stream<File> postAndAllDescendantsFiles = this.fileRepository.findPostAndDescendantsFiles(post.getId());

        postAndAllDescendantsFiles.forEach(file -> {
            if (file.getUser() != null) {
                file.setPost(null);
                this.fileRepository.save(file);
            } else {
                this.deleteFile(file);
            }
        });
    }

    public void deleteAllUserFiles(User user) {
        Stream<File> userFiles = this.fileRepository.findUserAndPostsDescendantsFiles(user.getId());
        userFiles.forEach(this::deleteFile);
    }

    @Scheduled(cron = "@daily")
    public void removeUnprocessedFiles() {
        Instant now = Instant.now();
        log.info("Start deletion of unprocessed files at: {}", now);

        Stream<File> unprocessedFiles = this.fileRepository.findAllByProcessedAndUploadedAtBefore(
            false,
            now.minus(1, ChronoUnit.DAYS)
        );

        unprocessedFiles.forEach(file -> {
            try {
                this.fileRepository.delete(file);
                this.deleteFile(file);

                String fileName = this.getFileName(file);
                log.info("Deleted unprocessed file: {} from storage and database", fileName);
            } catch (RuntimeException e) {
                log.error("Unprocessed file deletion error", e);
            }
        });

        log.info("Deletion unprocessed files completed at: {}", Instant.now());
    }

    private void saveFile(MultipartFile file, String uniqueFileName, String fileExtension) {
        Path destinationFile = this.rootLocation
            .resolve(FileType.TEMP.getFolder())
            .resolve(uniqueFileName + "." + fileExtension)
            .normalize()
            .toAbsolutePath();

        try {
            Files.copy(file.getInputStream(), destinationFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store temporary file", e);
        }
    }

    public FileDTO getFile(String folder, String fileName) {
        folder = this.cleanPathVariable(folder);
        fileName = this.cleanPathVariable(fileName);

        Path filePath = this.rootLocation
            .resolve(folder)
            .resolve(fileName)
            .normalize()
            .toAbsolutePath();

        if (!Files.isReadable(filePath))
            throw new FileNotFoundException("File not found or not readable");

        try {
            String detectedMimeType = Files.probeContentType(filePath);
            MediaType mimeType = detectedMimeType != null
                ? MediaType.parseMediaType(detectedMimeType)
                : MediaType.APPLICATION_OCTET_STREAM;

            byte[] content = Files.readAllBytes(filePath);

            return FileDTO.builder()
                .mimeType(mimeType)
                .content(content)
                .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file from storage", e);
        }
    }

    public String cleanPathVariable(String rawPathVariable) {
        if (rawPathVariable == null || rawPathVariable.isEmpty()) return "";
        return rawPathVariable.replaceAll("[\"'<>\\[\\]]", "").trim();
    }

    private void saveSanitizedSvg(String sanitizedContent, String uniqueFileName, String fileExtension) {
        Path destinationFile = this.rootLocation
            .resolve(FileType.TEMP.getFolder())
            .resolve(uniqueFileName + "." + fileExtension)
            .normalize()
            .toAbsolutePath();

        try {
            Files.writeString(destinationFile, sanitizedContent);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store temporary file", e);
        }
    }

    private void moveFile(File file) {
        String fileName = this.getFileName(file);

        Path tempFilePath = this.rootLocation
            .resolve(FileType.TEMP.getFolder())
            .resolve(fileName)
            .normalize()
            .toAbsolutePath();

        Path finalFilePath = this.rootLocation
            .resolve(file.getType().getFolder())
            .resolve(fileName)
            .normalize()
            .toAbsolutePath();

        try {
            Files.move(tempFilePath, finalFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to move file: " + fileName + " to final location", e);
        }
    }

    private void deleteFile(File file) {
        String folder = file.isProcessed() ?  file.getType().getFolder() : FileType.TEMP.getFolder();
        String fileName = this.getFileName(file);

        Path filePath = this.rootLocation
            .resolve(folder)
            .resolve(fileName)
            .normalize()
            .toAbsolutePath();

        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + fileName, e);
        }
    }

    private String getFileName(File file) {
        return file.getName() + "." + file.getExtension().name().toLowerCase();
    }
}
