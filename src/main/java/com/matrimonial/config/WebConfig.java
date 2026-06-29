package com.matrimonial.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CONFIG: WebConfig
 *
 * Serves locally uploaded photos as static files.
 *
 * Problem without this:
 *   Photos are saved to disk at: uploads/photos/{userId}/{uuid}.jpg
 *   Backend returns photoUrl: /uploads/photos/{userId}/{uuid}.jpg
 *   Browser requests GET /uploads/photos/{userId}/{uuid}.jpg
 *   → Spring returns 404 because it doesn't know where to find the file.
 *
 * Fix:
 *   Map the URL pattern /uploads/** to the physical folder on disk.
 *   Spring then serves those files as static resources.
 *
 * Layer: Config (no business logic)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * Maps GET /uploads/** → physical folder on disk.
     * "file:" prefix tells Spring this is a filesystem path (not classpath).
     * Trailing slash is required for directory mapping.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
            .addResourceHandler("/uploads/**")
            .addResourceLocations("file:" + uploadDir + "/../");
    }
}
