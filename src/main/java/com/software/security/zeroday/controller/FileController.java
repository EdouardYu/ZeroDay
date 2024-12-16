package com.software.security.zeroday.controller;

import com.software.security.zeroday.dto.file.FileDTO;
import com.software.security.zeroday.dto.file.FileIdDTO;
import com.software.security.zeroday.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@AllArgsConstructor
@RestController
@RequestMapping("file")
public class FileController {
    private final FileService fileService;

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FileIdDTO uploadFile(@RequestParam("file") MultipartFile file) {
        return this.fileService.uploadFile(file);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(path = "profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public FileIdDTO uploadProfilePicture(@RequestParam("file") MultipartFile file) {
        return this.fileService.uploadProfilePicture(file);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(path = "{folder}/{filename}")
    public ResponseEntity<byte[]> getFile(@PathVariable String folder, @PathVariable("filename") String fileName){
        FileDTO file = this.fileService.getFile(folder, fileName);

        return ResponseEntity.ok()
            .contentType(file.getMimeType())
            .header("Content-Disposition", "inline; filename*=UTF-8''"
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"))
            .body(file.getContent());
    }
}

