package com.example.backend.service;
import com.example.backend.dto.SimulationRequest;
import com.example.backend.dto.SimulationResponse;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.time.OffsetDateTime;
/**
 * シミュレーション処理サービス
 */
@Service
public class SimulationService {

    public SimulationResponse run(SimulationRequest req) {

        OffsetDateTime baseTime = req.getReleaseAt();
        int initialStock = req.getInitialStock();
        int simulationHours = req.getSimulationHours();
        int bucketMinutes = req.getTimeBucketMinutes();
        int runs = req.getRuns();

        Random random = (req.getSeed() != null)
                ? new Random(req.getSeed())
                : new Random();

        // --- 需要ベース設定 ---
        double baseLambda;
        switch (req.getPopularity()) {
            case LOW -> baseLambda = 5;
            case MEDIUM -> baseLambda = 15;
            case HIGH -> baseLambda = 30;
            default -> baseLambda = 10;
        }

        double storeMultiplier;
        switch (req.getStoreType()) {
            case LARGE -> storeMultiplier = 1.2;
            case STATION -> storeMultiplier = 1.5;
            case SMALL -> storeMultiplier = 0.8;
            default -> storeMultiplier = 1.0;
        }

        double snsMultiplier = req.getSnsBoostEnabled() ? 1.3 : 1.0;

        double lambda = baseLambda * storeMultiplier * snsMultiplier;

        int totalBuckets = (simulationHours * 60) / bucketMinutes;

        List<SimulationResponse.InventoryPoint> inventorySeries = new ArrayList<>();
        List<SimulationResponse.RemainingProbabilityPoint> remainingProb = new ArrayList<>();

        int stock = initialStock;
        OffsetDateTime currentTime = baseTime;
        OffsetDateTime soldOutAt = null;

        inventorySeries.add(new SimulationResponse.InventoryPoint(currentTime, stock));
        remainingProb.add(new SimulationResponse.RemainingProbabilityPoint(currentTime, 1.0));

        for (int i = 1; i <= totalBuckets; i++) {

            // 簡易需要モデル（Gaussian）
            int demand = (int) Math.max(0, random.nextGaussian() * (lambda * 0.3) + lambda);

            stock -= demand;
            stock = Math.max(stock, 0);

            currentTime = currentTime.plusMinutes(bucketMinutes);

            inventorySeries.add(new SimulationResponse.InventoryPoint(currentTime, stock));
            remainingProb.add(new SimulationResponse.RemainingProbabilityPoint(
                    currentTime,
                    stock > 0 ? 1.0 : 0.0
            ));

            if (stock == 0 && soldOutAt == null) {
                soldOutAt = currentTime;
            }
        }

        SimulationResponse res = new SimulationResponse();
        res.setSoldOutAt(soldOutAt);
        res.setSoldOutProbability(soldOutAt != null ? 1.0 : 0.0); // MVPは簡易
        res.setInventorySeries(inventorySeries);
        res.setRemainingProbabilityByTime(remainingProb);

        res.setRecommendations(List.of(
                soldOutAt != null
                        ? "初日に売り切れる可能性が高いです"
                        : "シミュレーション期間内では売り切れません"
        ));

        res.setMeta(new SimulationResponse.Meta(
                "mvp-2",
                runs,
                req.getSeed(),
                OffsetDateTime.now()
        ));

        return res;
    }
}
