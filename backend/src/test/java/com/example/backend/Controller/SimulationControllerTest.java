package com.example.backend.Controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
     * - POST /api/v1/simulations の正常系（レスポンス契約の必須項目）
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
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.soldOutAt").exists())
                .andExpect(jsonPath("$.soldOutProbability").isNumber())
                .andExpect(jsonPath("$.remainingProbabilityByTime").isArray())
                .andExpect(jsonPath("$.remainingProbabilityByTime.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.remainingProbabilityByTime[0].time").exists())
                .andExpect(jsonPath("$.remainingProbabilityByTime[0].remainingProbability").isNumber())
                .andExpect(jsonPath("$.inventorySeries").isArray())
                .andExpect(jsonPath("$.inventorySeries.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.inventorySeries[0].time").exists())
                .andExpect(jsonPath("$.inventorySeries[0].expectedRemaining").isNumber())
                .andExpect(jsonPath("$.recommendations").isArray())
                .andExpect(jsonPath("$.recommendations.length()").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.meta.modelVersion").value("mvp-1"))
                .andExpect(jsonPath("$.meta.runs").value(1000))
                .andExpect(jsonPath("$.meta.seed").value(42))
                .andExpect(jsonPath("$.meta.generatedAt").exists());
    }

    @Test
    void simulations_shouldAllowNullSeed_whenSeedIsOmitted() throws Exception {
        String requestBodyWithoutSeed = """
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
                }
                """;

        mockMvc.perform(post("/api/v1/simulations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyWithoutSeed))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.soldOutAt").exists())
                .andExpect(jsonPath("$.meta.modelVersion").value("mvp-1"))
                .andExpect(jsonPath("$.meta.runs").value(1000))
                .andExpect(jsonPath("$.meta.seed").value(nullValue()))
                .andExpect(jsonPath("$.meta.generatedAt").exists());
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
