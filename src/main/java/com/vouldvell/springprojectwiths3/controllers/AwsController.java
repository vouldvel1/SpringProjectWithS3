package com.vouldvell.springprojectwiths3.controllers;

import com.vouldvell.springprojectwiths3.enumerations.FileType;
import com.vouldvell.springprojectwiths3.services.AwsService;
import lombok.SneakyThrows;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/s3bucketstorage")
public class AwsController {

    private final AwsService awsService;

    public AwsController(AwsService awsService) {
        this.awsService = awsService;
    }

    @GetMapping("/{bucketName}")
    public ResponseEntity<?> listFiles(@PathVariable String bucketName) {
        final var body = awsService.listFiles(bucketName);
        return ResponseEntity.ok().body(body);
    }

    @PostMapping("/{bucketName}")
    @SneakyThrows(IOException.class)
    public ResponseEntity<?> uploadFile(@PathVariable("bucketName") String bucketName,
                                        @RequestParam("file") MultipartFile file,
                                        @RequestParam(required = false) String path) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        if (path != null) {
            path += "/";
        }

        var fileName = path + StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        var contentType = file.getContentType();
        var fileSize = file.getSize();
        var inputStream = file.getInputStream();

        awsService.uploadFile(bucketName, fileName, fileSize, contentType, inputStream);
        return ResponseEntity.ok().body("File uploaded successfully");
    }

    @SneakyThrows
    @GetMapping("/{bucketName}/{fileName}")
    public ResponseEntity<?> downloadFile(@PathVariable String bucketName, @PathVariable String fileName,
                                          @RequestParam(required = false) String path) {

        if (path != null) {
            path += "/";
        }

        final var body = awsService.downloadFile(bucketName, path + fileName);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(FileType.fromFilename(fileName))
                .body(body.toByteArray());
    }

    @DeleteMapping("/{bucketName}/{fileName}")
    public ResponseEntity<?> deleteFile(@PathVariable String bucketName, @PathVariable String fileName,
                                        @RequestParam(required = false) String path) {

        if (path != null) {
            path += "/";
        }

        awsService.deleteFile(bucketName, path + fileName);
        return ResponseEntity.ok().build();
    }


}
