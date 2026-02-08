package com.example.backend.service;
import com.example.backend.dto.SimulationRequest;
import com.example.backend.dto.SimulationResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import java.time.OffsetDateTime;
/**
 * シミュレーション処理サービス
 * 後にモデル実装予定
 */
@Service
public class SimulationService {
    public SimulationResponse run(SimulationRequest req) {
        OffsetDateTime baseTime = (req.getReleaseAt() != null) ? req.getReleaseAt() : OffsetDateTime.now();

        SimulationResponse res = new SimulationResponse();
        res.setSoldOutAt(baseTime.plusHours(4));   // 仮
        res.setSoldOutProbability(0.82);      // 仮

        int initialStock = (req.getInitialStock() != null) ? req.getInitialStock() : 120;
        res.setRemainingProbabilityByTime(List.of(
                new SimulationResponse.RemainingProbabilityPoint(baseTime, 1.0),
                new SimulationResponse.RemainingProbabilityPoint(baseTime.plusHours(2), 0.67),
                new SimulationResponse.RemainingProbabilityPoint(baseTime.plusHours(4), 0.18)
        ));
        res.setInventorySeries(List.of(
                new SimulationResponse.InventoryPoint(baseTime, initialStock),
                new SimulationResponse.InventoryPoint(baseTime.plusHours(2), Math.max(initialStock - 46, 0)),
                new SimulationResponse.InventoryPoint(baseTime.plusHours(4), Math.max(initialStock - 111, 0))
        ));

        res.setRecommendations(List.of(
                "発売日午前中の来店が有利です",
                "この条件では初日夕方以降の在庫確率は低いです"
        ));

        int runs = (req.getRuns() != null) ? req.getRuns() : 1000;

        res.setMeta(new SimulationResponse.Meta(
                "mvp-1",
                runs,
                req.getSeed(),
                OffsetDateTime.now()
        ));

        return res;
    }
}
