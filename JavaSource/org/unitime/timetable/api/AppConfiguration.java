package org.unitime.timetable.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.oas.annotations.EnableOpenApi;

@Configuration
@EnableWebMvc
@ComponentScan("org.unitime.timetable.api")
@EnableOpenApi
public class AppConfiguration {

}
