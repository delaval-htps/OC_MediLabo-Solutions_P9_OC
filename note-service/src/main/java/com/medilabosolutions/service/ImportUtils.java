package com.medilabosolutions.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ImportUtils {

    private ImportUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static List<String> linesFromResource(String resource) throws IOException {
        Resource input = new ClassPathResource(resource);
        Path path = input.getFile().toPath();
        return Files.readAllLines(path);
    }
}
