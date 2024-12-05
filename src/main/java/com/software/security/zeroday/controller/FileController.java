package com.software.security.zeroday.controller;

import com.software.security.zeroday.dto.file.FileDTO;
import com.software.security.zeroday.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@AllArgsConstructor
@RestController
@RequestMapping("file")
public class FileController {
    private final FileService fileService;

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FileDTO uploadFile(@RequestParam("file") MultipartFile file) {
        return this.fileService.uploadFile(file);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FileDTO uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        return this.fileService.uploadProfilePicture(file);
    }
}

