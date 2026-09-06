package com.haytam.urlshortener.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@Component
public class StartupLogger implements CommandLineRunner {

    private final RequestMappingHandlerMapping handlerMapping;

    public StartupLogger(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    @Override
    public void run(String... args) {

        System.out.println();
        System.out.println("==========================================");
        System.out.println("          URL SHORTENER API");
        System.out.println("==========================================");

        System.out.println("Endpoints:");

        handlerMapping.getHandlerMethods().
                forEach((mapping,
                         handlerMethod) -> {

            mapping.getMethodsCondition()
                    .getMethods()
                    .forEach(method -> {

                        mapping.getPathPatternsCondition()
                                .getPatternValues()
                                .forEach(path -> {

                                    System.out.printf(
                                            "  %-6s %s%n",
                                            method,
                                            path
                                    );

                                });
                    });
        });

        System.out.println("==========================================");
        System.out.println();
    }
}