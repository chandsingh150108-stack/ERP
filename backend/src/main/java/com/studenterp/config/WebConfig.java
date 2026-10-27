package com.studenterp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer hibernateModuleCustomizer() {
        return builder -> builder.modulesToInstall(new Hibernate6Module());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/departments").setViewName("forward:/index.html");
        registry.addViewController("/programs").setViewName("forward:/index.html");
        registry.addViewController("/students").setViewName("forward:/index.html");
        registry.addViewController("/faculty").setViewName("forward:/faculty/index.html");
        registry.addViewController("/faculty/").setViewName("forward:/faculty/index.html");
        registry.addViewController("/courses").setViewName("forward:/index.html");
        registry.addViewController("/sections").setViewName("forward:/index.html");
        registry.addViewController("/announcements").setViewName("forward:/index.html");
        registry.addViewController("/events").setViewName("forward:/events/index.html");
        registry.addViewController("/events/").setViewName("forward:/events/index.html");
        registry.addViewController("/scholarships").setViewName("forward:/index.html");
        registry.addViewController("/complaints").setViewName("forward:/index.html");
        registry.addViewController("/backlogs").setViewName("forward:/index.html");
        registry.addViewController("/library").setViewName("forward:/index.html");
        registry.addViewController("/vault").setViewName("forward:/vault/index.html");
        registry.addViewController("/vault/").setViewName("forward:/vault/index.html");
        registry.addViewController("/notehub").setViewName("forward:/notehub/index.html");
        registry.addViewController("/notehub/").setViewName("forward:/notehub/index.html");
        registry.addViewController("/notehub/course").setViewName("forward:/notehub/course.html");
        registry.addViewController("/notehub/course/").setViewName("forward:/notehub/course.html");
        registry.addViewController("/notehub/extra").setViewName("forward:/notehub/extra.html");
        registry.addViewController("/notehub/extra/").setViewName("forward:/notehub/extra.html");
        registry.addViewController("/academics").setViewName("forward:/academics/index.html");
        registry.addViewController("/academics/").setViewName("forward:/academics/index.html");
        registry.addViewController("/accounts").setViewName("forward:/accounts/index.html");
        registry.addViewController("/accounts/").setViewName("forward:/accounts/index.html");
        registry.addViewController("/assignment").setViewName("forward:/assignment/index.html");
        registry.addViewController("/assignment/").setViewName("forward:/assignment/index.html");
        registry.addViewController("/timetable").setViewName("forward:/timetable/index.html");
        registry.addViewController("/timetable/").setViewName("forward:/timetable/index.html");
        registry.addViewController("/certificate").setViewName("forward:/certificate/index.html");
        registry.addViewController("/certificate/").setViewName("forward:/certificate/index.html");
        registry.addViewController("/examination").setViewName("forward:/examination/index.html");
        registry.addViewController("/examination/").setViewName("forward:/examination/index.html");
        registry.addViewController("/feedback").setViewName("forward:/feedback/index.html");
        registry.addViewController("/feedback/").setViewName("forward:/feedback/index.html");
    }
}
