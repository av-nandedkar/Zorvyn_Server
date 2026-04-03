package com.example.zorvyn;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ZorvynApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
    }

    @Test
    void viewerCannotCreateRecord() throws Exception {
        String token = loginAndGetToken("viewer@zorvyn.local", "Viewer@123");

        String payload = "{"
                + "\"amount\":1200,"
                + "\"type\":\"EXPENSE\","
                + "\"category\":\"Books\","
                + "\"date\":\"2026-04-01\","
                + "\"notes\":\"Test\""
                + "}";

        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    @Test
    void analystCanReadRecords() throws Exception {
        String token = loginAndGetToken("analyst@zorvyn.local", "Analyst@123");

        mockMvc.perform(get("/api/v1/records")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void viewerCanReadDashboardSummary() throws Exception {
        String token = loginAndGetToken("viewer@zorvyn.local", "Viewer@123");

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalIncome").exists())
                .andExpect(jsonPath("$.totalExpense").exists())
                .andExpect(jsonPath("$.netBalance").exists());
    }

    @Test
    void analystCannotDeleteRecords() throws Exception {
        String token = loginAndGetToken("analyst@zorvyn.local", "Analyst@123");

        mockMvc.perform(delete("/api/v1/records/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanManageUsers() throws Exception {
        String token = loginAndGetToken("admin@zorvyn.local", "Admin@123");

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").exists())
                .andExpect(jsonPath("$[0].status").exists());
    }

    @Test
    void analystCannotReadAdminOwnedRecordById() throws Exception {
        String token = loginAndGetToken("analyst@zorvyn.local", "Analyst@123");

        mockMvc.perform(get("/api/v1/records/1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminValidationErrorWhenAmountMissing() throws Exception {
        String token = loginAndGetToken("admin@zorvyn.local", "Admin@123");

        String payload = "{"
                + "\"type\":\"EXPENSE\","
                + "\"category\":\"Tools\","
                + "\"date\":\"2026-04-01\""
                + "}";

        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCreatesViewerAndBlocksAdminActions() throws Exception {
        String registerPayload = "{"
                + "\"name\":\"Demo Viewer\","
                + "\"email\":\"demo.viewer@zorvyn.local\","
                + "\"password\":\"StrongPass@123\""
                + "}";

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.user.role").value("VIEWER"))
                .andReturn();

        String registerResponse = registerResult.getResponse().getContentAsString();
        int start = registerResponse.indexOf("\"token\":\"") + 9;
        int end = registerResponse.indexOf("\"", start);
        String viewerToken = registerResponse.substring(start, end);

        String payload = "{"
                + "\"amount\":1999,"
                + "\"type\":\"EXPENSE\","
                + "\"category\":\"Restricted\","
                + "\"date\":\"2026-04-03\""
                + "}";

        mockMvc.perform(post("/api/v1/records")
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isForbidden());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        String payload = "{"
                + "\"email\":\"" + email + "\","
                + "\"password\":\"" + password + "\""
                + "}";

        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        int start = response.indexOf("\"token\":\"") + 9;
        int end = response.indexOf("\"", start);
        return response.substring(start, end);
    }

}
