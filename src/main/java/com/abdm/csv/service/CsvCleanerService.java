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

@Service
public class CsvCleanerService {

    @Value("${special.characters}")
    private String specialCharacters;

    @Value("${columns.name.to.clean}")
    private String columnsNameToClean;

    public ByteArrayInputStream cleanCsvFile(MultipartFile file) throws IOException {
        List<String> specialCharList = List.of(specialCharacters.split(","));
        List<String> columnsToClean = List.of(columnsNameToClean.split(","));
        List<CSVRecord> records;
        String[] headers;

        try (Reader reader = new InputStreamReader(file.getInputStream())) {
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            headers = csvParser.getHeaderMap().keySet().toArray(new String[0]);
            records = csvParser.getRecords();
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(outputStream), CSVFormat.DEFAULT.withHeader(headers))) {
            for (CSVRecord record : records) {
                List<String> cleanedRecord = cleanRecord(record, headers, specialCharList, columnsToClean);
                csvPrinter.printRecord(cleanedRecord);
            }
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private List<String> cleanRecord(CSVRecord record, String[] headers, List<String> specialCharList, List<String> columnsToClean) {
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

}
