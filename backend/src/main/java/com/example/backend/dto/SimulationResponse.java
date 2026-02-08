package com.example.backend.dto;
import java.time.OffsetDateTime;
import java.util.List;
/**
 * POST /api/v1/simulations レスポンス内容
 * Javaオブジェクト -> JSON へ自動変換される（Jackson）
 */
public class SimulationResponse {
    //出力パラメーターフィールド
    private OffsetDateTime soldOutAt;
    private double soldOutProbability;
    private List<OffsetDateTime> recommendations;
    private Meta meta;

    //引数なしコンストラクタ
    public SimulationResponse() {}

    //追加Meta情報
    public record Meta(
        String modelVersion,
        int runs,
        Integer seed,
        OffsetDateTime generatedAt
    ) {}
    
    //getter-setter
    public OffsetDateTime getSoldOutAt() {
        return soldOutAt;
    }
    public void setSoldOutAt(OffsetDateTime soldOutAt) {
        this.soldOutAt = soldOutAt; 
    }

    public double getSoldOutProbability() {
        return soldOutProbability;
    }
    public void setSoldOutProbability(double soldOutProbability) {
        this.soldOutProbability = soldOutProbability;   
    }

    public List<OffsetDateTime> getRecommendations() {
        return recommendations;
    }
    public void setRecommendations(List<OffsetDateTime> recommendations) {
        this.recommendations = recommendations;
    }

    public Meta getMeta() {
        return meta;
    }
    public void setMeta(Meta meta) {
        this.meta = meta;
    }
}
