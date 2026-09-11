package com.josyantl.joblens.job.interfaces.rest;

import com.josyantl.joblens.job.infrastructure.persistence.SpringDataJobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SpringDataJobApplicationRepository springDataRepository;

    @BeforeEach
    void clearDatabase() {
        springDataRepository.deleteAll();
    }

    @Test
    void createsJobApplication() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Example Company",
                                  "position": "Backend Engineer",
                                  "description": "Java Spring Boot PostgreSQL"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.company").value("Example Company"))
                .andExpect(jsonPath("$.position").value("Backend Engineer"))
                .andExpect(jsonPath("$.status").value("SAVED"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void rejectsBlankRequiredFields() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "",
                                  "position": "Backend Engineer",
                                  "description": "Java Spring Boot PostgreSQL"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsJobApplications() throws Exception {
        mockMvc.perform(post("/api/applications")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "company": "Example Company",
                                  "position": "Backend Engineer",
                                  "description": "Java Spring Boot PostgreSQL"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/applications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company").value("Example Company"))
                .andExpect(jsonPath("$[0].position").value("Backend Engineer"))
                .andExpect(jsonPath("$[0].status").value("SAVED"));
    }
}
