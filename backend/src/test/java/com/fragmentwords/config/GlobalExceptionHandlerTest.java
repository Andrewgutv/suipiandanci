package com.fragmentwords.config;

import com.fragmentwords.common.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(new ExceptionThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();
    }

    @Test
    void badRequestExceptionsReturnHttp400() throws Exception {
        mockMvc.perform(get("/bad-request").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400))
            .andExpect(jsonPath("$.message").value("bad request"));
    }

    @Test
    void unauthorizedExceptionsReturnHttp401() throws Exception {
        mockMvc.perform(get("/unauthorized").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code").value(401))
            .andExpect(jsonPath("$.message").value("unauthorized"));
    }

    @Test
    void notFoundExceptionsReturnHttp404() throws Exception {
        mockMvc.perform(get("/not-found").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("missing"));
    }

    @Test
    void conflictExceptionsReturnHttp409() throws Exception {
        mockMvc.perform(get("/conflict").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.code").value(409))
            .andExpect(jsonPath("$.message").value("conflict"));
    }

    @Test
    void unhandledExceptionsReturnHttp500() throws Exception {
        mockMvc.perform(get("/internal-error").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.message").value("boom"));
    }

    @RestController
    static class ExceptionThrowingController {

        @GetMapping("/bad-request")
        void badRequest() {
            throw new IllegalArgumentException("bad request");
        }

        @GetMapping("/unauthorized")
        void unauthorized() {
            throw new SecurityException("unauthorized");
        }

        @GetMapping("/not-found")
        void notFound() {
            throw new ResourceNotFoundException("missing");
        }

        @GetMapping("/conflict")
        void conflict() {
            throw new IllegalStateException("conflict");
        }

        @GetMapping("/internal-error")
        void internalError() {
            throw new RuntimeException("boom");
        }
    }
}
