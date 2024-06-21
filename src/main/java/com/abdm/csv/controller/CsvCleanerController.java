package com.abdm.csv.controller;

import com.abdm.csv.service.CsvCleanerService;
import com.abdm.csv.service.CsvCleanerWithLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@RestController
public class CsvCleanerController {

    @Autowired
    private CsvCleanerService csvCleanerService;

    @Autowired
    private CsvCleanerWithLogService csvCleanerWithLogService;

    @PostMapping("/cleanCsv")
    public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("Please upload a valid CSV file.", HttpStatus.BAD_REQUEST);
        }

        try {
            ByteArrayInputStream cleanedCsv = csvCleanerService.cleanCsvFile(file);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=cleaned.csv");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(cleanedCsv));

        } catch (IOException e) {
            return new ResponseEntity<>("Failed to process the file.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/cleanCsvWithLog")
    public ResponseEntity<byte[]> downloadCleanedCsvAndLog(@RequestParam("file") MultipartFile file) throws IOException {
        ByteArrayInputStream zipInputStream = csvCleanerWithLogService.downloadCleanedCsvAndLog(file);

        byte[] zipBytes = new byte[zipInputStream.available()];
        zipInputStream.read(zipBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "cleaned_data_and_change_log.zip");

        return ResponseEntity.ok()
                .headers(headers)
                .body(zipBytes);
    }
}
