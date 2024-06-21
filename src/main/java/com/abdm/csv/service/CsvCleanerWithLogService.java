package com.abdm.csv.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class CsvCleanerWithLogService {

    @Value("${special.characters}")
    private String specialCharacters;

    @Value("${columns.name.to.clean}")
    private String columnsNameToClean;

    public ByteArrayInputStream downloadCleanedCsvAndLog(MultipartFile file) throws IOException {
        List<String> specialCharList = List.of(specialCharacters.split(","));
        List<String> columnsToClean = List.of(columnsNameToClean.split(","));
        List<CSVRecord> records;
        String[] headers;

        // Prepare cleaned data CSV
        StringWriter cleanedDataWriter = new StringWriter();
        try (CSVPrinter cleanedCsvPrinter = new CSVPrinter(cleanedDataWriter, CSVFormat.DEFAULT.withHeader())) {
            CSVParser csvParser = new CSVParser(new InputStreamReader(file.getInputStream()), CSVFormat.DEFAULT.withFirstRecordAsHeader());
            headers = csvParser.getHeaderMap().keySet().toArray(new String[0]);
            records = csvParser.getRecords();

            for (CSVRecord record : records) {
                List<String> cleanedRecord = cleanRecord(record, headers, specialCharList, columnsToClean);
                cleanedCsvPrinter.printRecord(cleanedRecord);
            }
        }

        // Prepare log data CSV
        StringWriter logDataWriter = new StringWriter();
        try (CSVPrinter logCsvPrinter = new CSVPrinter(logDataWriter, CSVFormat.DEFAULT.withHeader("Row Number", "Column Name", "Old Value", "New Value"))) {
            for (CSVRecord record : records) {
                logChanges(logCsvPrinter, record, headers, columnsToClean);
            }
        }

        // Create a zip file containing both CSVs
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(baos)) {
            addToZip(cleanedDataWriter.toString().getBytes(), "cleaned_data.csv", zipOut);
            addToZip(logDataWriter.toString().getBytes(), "change_log.csv", zipOut);
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private List<String> cleanRecord(CSVRecord record, String[] headers, List<String> specialCharList,
                                     List<String> columnsToClean) {
        List<String> values = new ArrayList<>();
        for (String header : headers) {
            String value = record.get(header);
            if (columnsToClean.contains(header)) {
                for (String specialChar : specialCharList) {
                    value = value.replace(specialChar, "");
                }
            }
            values.add(value);
        }
        return values;
    }

    private void logChanges(CSVPrinter logPrinter, CSVRecord record, String[] headers, List<String> columnsToClean) throws IOException {
        for (String header : headers) {
            if (columnsToClean.contains(header)) {
                String oldValue = record.get(header);
                String newValue = oldValue;
                for (String specialChar : specialCharacters.split(",")) {
                    newValue = newValue.replace(specialChar, "");
                }
                if (!oldValue.equals(newValue)) {
                    logPrinter.printRecord(record.getRecordNumber(), header, oldValue, newValue);
                }
            }
        }
    }

    private void addToZip(byte[] content, String fileName, ZipOutputStream zipOut) throws IOException {
        ZipEntry zipEntry = new ZipEntry(fileName);
        zipOut.putNextEntry(zipEntry);
        zipOut.write(content);
        zipOut.closeEntry();
    }
}
