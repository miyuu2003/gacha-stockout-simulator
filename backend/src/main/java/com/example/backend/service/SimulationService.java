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
        OffsetDateTime now = OffsetDateTime.now();

        SimulationResponse res = new SimulationResponse();
        res.setSoldOutAt(now.plusHours(4));   // 仮
        res.setSoldOutProbability(0.82);      // 仮

        res.setRecommendations(List.of(
                "発売日午前中の来店が有利です",
                "この条件では初日夕方以降の在庫確率は低いです"
        ));

        int runs = (req.getRuns() != null) ? req.getRuns() : 1000;

        res.setMeta(new SimulationResponse.Meta(
                "mvp-1",
                runs,
                req.getSeed(),
                now
        ));

        return res;
    }
}
