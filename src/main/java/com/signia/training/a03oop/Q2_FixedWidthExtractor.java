package com.signia.training.a03oop;

import com.signia.training.a01basics.FixedWidthParser;

import java.nio.file.Path;
import java.util.List;

public class Q2_FixedWidthExtractor implements Q2_DataExtractor {

    @Override
    public List<String[]> extract(Path path) {
        return FixedWidthParser.parse(path);
    }

    @Override
    public String name() {
        return "Fixed Width";
    }

    @Override
    public boolean supports(Path path) {
        return path != null
                && path.toString()
                .toLowerCase()
                .endsWith(".txt");
    }
}