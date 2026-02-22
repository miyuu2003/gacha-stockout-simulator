package com.example.backend.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SimulationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    /**
     * ヘルスチェックおよびシミュレーションAPIの統合テスト
     * - GET /api/v1/health の正常応答
     * - POST /api/v1/simulations の正常系
     * - POST /api/v1/simulations の 422（@Valid制約違反）
     * - POST /api/v1/simulations の 400（malformed JSON / enum不正 / 日時フォーマット不正）
     */
    @Test
    void health_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("gacha-stockout-backend"));
    }

    @Test
    void simulations_shouldReturn200_whenRequestIsValid() throws Exception {
        String requestBody = """
                {
                  "productName": "人気キャラコレクションVol.1",
                  "popularity": "HIGH",
                  "releaseAt": "2026-02-10T10:00:00+09:00",
                  "storeType": "LARGE",
                  "initialStock": 120,
                  "snsBoostEnabled": true,
                  "simulationHours": 24,
                  "timeBucketMinutes": 30,
                  "runs": 1000,
                  "seed": 42
                }
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soldOutProbability").exists())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.meta.modelVersion").value("mvp-1"));
    }

    @Test
    void simulations_shouldReturn422_whenValidationFails() throws Exception {
        String invalidRequestBody = """
                {
                  "productName": "人気キャラコレクションVol.1",
                  "popularity": "HIGH",
                  "releaseAt": "2026-02-10T10:00:00+09:00",
                  "storeType": "LARGE",
                  "snsBoostEnabled": true,
                  "simulationHours": 24,
                  "timeBucketMinutes": 30,
                  "runs": 1000
                }
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequestBody))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void simulations_shouldReturn400_whenJsonIsMalformed() throws Exception {
        String malformedJson = """
                {
                  "productName": "人気キャラコレクションVol.1",
                  "popularity": "HIGH",
                  "releaseAt": "2026-02-10T10:00:00+09:00",
                  "storeType": "LARGE",
                  "initialStock": 120,
                  "snsBoostEnabled": true,
                  "simulationHours": 24,
                  "timeBucketMinutes": 30,
                  "runs": 1000
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("MALFORMED_JSON"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void simulations_shouldReturn400_whenEnumValueIsInvalid() throws Exception {
        String invalidEnumBody = """
                {
                  "productName": "人気キャラコレクションVol.1",
                  "popularity": "VERY_HIGH",
                  "releaseAt": "2026-02-10T10:00:00+09:00",
                  "storeType": "LARGE",
                  "initialStock": 120,
                  "snsBoostEnabled": true,
                  "simulationHours": 24,
                  "timeBucketMinutes": 30,
                  "runs": 1000
                }
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidEnumBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FORMAT"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void simulations_shouldReturn400_whenDateFormatIsInvalid() throws Exception {
        String invalidDateBody = """
                {
                  "productName": "人気キャラコレクションVol.1",
                  "popularity": "HIGH",
                  "releaseAt": "2026/02/10 10:00:00",
                  "storeType": "LARGE",
                  "initialStock": 120,
                  "snsBoostEnabled": true,
                  "simulationHours": 24,
                  "timeBucketMinutes": 30,
                  "runs": 1000
                }
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidDateBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FORMAT"))
                .andExpect(jsonPath("$.details").isArray());
    }
}
