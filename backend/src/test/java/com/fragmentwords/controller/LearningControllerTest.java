package com.fragmentwords.controller;

import com.fragmentwords.common.ResourceNotFoundException;
import com.fragmentwords.config.GlobalExceptionHandler;
import com.fragmentwords.service.LearningProgressService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LearningControllerTest {

    private MockMvc mockMvc;
    private LearningProgressService learningProgressService;

    @BeforeEach
    void setUp() {
        learningProgressService = mock(LearningProgressService.class);
        LearningController controller = new LearningController();
        ReflectionTestUtils.setField(controller, "learningProgressService", learningProgressService);

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(new GlobalExceptionHandler())
            .setMessageConverters(new MappingJackson2HttpMessageConverter())
            .build();
    }

    @Test
    void feedbackValidationReturnsHttp400() throws Exception {
        mockMvc.perform(
                post("/api/v1/learning/feedback")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"isKnown\":true}")
            )
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void wordProgressReturnsHttp404WhenServiceThrowsNotFound() throws Exception {
        when(learningProgressService.getWordProgress("device-1", null, 99L))
            .thenThrow(new ResourceNotFoundException("missing"));

        mockMvc.perform(
                get("/api/v1/learning/word/99")
                    .header("X-Device-Id", "device-1")
                    .accept(MediaType.APPLICATION_JSON)
            )
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.message").value("missing"));
    }
}
