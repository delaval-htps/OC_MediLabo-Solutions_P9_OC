package com.medilabosolutions;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
			List<String> jsonlines = ImportUtils.linesFromResource(source);
			log.info("lines : {}",jsonlines.toString());
			String result = importJsonService.importTo(jsonlines);
			log.info("import {} - result:{}", source, result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
