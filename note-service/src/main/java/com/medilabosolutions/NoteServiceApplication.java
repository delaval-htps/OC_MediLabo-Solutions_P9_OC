package com.medilabosolutions;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import com.medilabosolutions.service.ImportJsonService;
import com.medilabosolutions.service.ImportUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@SpringBootApplication
@EnableDiscoveryClient
@RequiredArgsConstructor
@Slf4j
public class NoteServiceApplication implements ApplicationRunner {

	@Value("${mongodb.source.load.data}")
	private String source;
	
	private final ImportJsonService importJsonService;

	public static void main(String[] args) {
		SpringApplication.run(NoteServiceApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		try {
			InputStream jsonStream = ImportUtils.jsonStreamFromResource(source);
			String result = importJsonService.importTo(jsonStream);
			log.info("import {} - result:{}", source, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
