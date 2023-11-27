package com.medilabosolutions.service;

import java.io.File;
import java.io.IOException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class ImportUtils {

    private ImportUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static File jsonFileFromResource(String resource) throws IOException {
        Resource input = new ClassPathResource(resource);
        return input.getFile();
    }
}
