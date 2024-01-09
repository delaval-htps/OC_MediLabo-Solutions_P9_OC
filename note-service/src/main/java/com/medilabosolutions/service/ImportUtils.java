package com.medilabosolutions.service;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImportUtils {

    private ImportUtils() {
        throw new IllegalStateException("Utility Class");
    }

    public static InputStream jsonStreamFromResource(String resource) throws IOException {
        Resource input = new ClassPathResource(resource);
        log.info("resource = {} / file = {}", input.getURL(), input.getFilename());
        return input.getInputStream();
    }
}
