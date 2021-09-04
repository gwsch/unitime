package org.unitime.timetable.api;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
public class SpringConfig implements WebMvcConfigurer {
	  
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/swagger-ui/**")
		  	.addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
	        .resourceChain(true);
	    registry.addResourceHandler("/images/**").addResourceLocations("/images/");
	    registry.addResourceHandler("/unitime/**").addResourceLocations("/unitime/");
	    registry.addResourceHandler("/styles/**").addResourceLocations("/styles/");
	    registry.addResourceHandler("/scripts/**").addResourceLocations("/scripts/");
	    registry.addResourceHandler("/leaflet/**").addResourceLocations("/leaflet/");
	    registry.addResourceHandler("/user/**").addResourceLocations("/user/");
	    registry.addResourceHandler("/admin/**").addResourceLocations("/admin/");
	    registry.addResourceHandler("/tt/**").addResourceLocations("/tt/");
	    registry.addResourceHandler("/exam/**").addResourceLocations("/exam/");
	    registry.addResourceHandler("/sct/**").addResourceLocations("/sct/");
	    registry.addResourceHandler("/help/**").addResourceLocations("/help/");
	    registry.addResourceHandler("/layouts/**").addResourceLocations("/layouts/");
	}

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
	    registry.addViewController("/swagger-ui/").setViewName("forward:" + "/swagger-ui/index.html");
	}

}
