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
     * ヘルスチェックおよびシミュレーションAPIの統合テストを最低限のみ実装
     * GET /api/v1/health が 200 を返す
     * POST /api/v1/simulations 正常リクエストで 200 を返す
     * POST /api/v1/simulations バリデーション不正（initialStock 欠落）で 422 を返す
     * 
     * 今後、サービス層のモック化や異常系テストなどを追加予定
     */
    @Test
    void health_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("backend"));
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
}
