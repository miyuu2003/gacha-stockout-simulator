package com.example.backend.dto;
import java.time.OffsetDateTime;
/**
 * POST /api/v1/simulations リクエスト内容
 * JSON -> Javaオブジェクト へ自動変換される（Jackson）
 */
public class SimulationRequest {
    //入力パラメーターフィールド
    private String productName;
    private String popularity;
    private OffsetDateTime releaseAt;
    private String storeType;
    private Integer initialStock;
    private Integer runs;
    private Integer seed;

    //引数なしコンストラクタ
    public SimulationRequest() {}

    //getter-setter
    public String getProductName() {
        return productName; 
    }
    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getPopularity() {
        return popularity;
    }
    public void setPopularity(String popularity) {
        this.popularity = popularity;
    }

    public OffsetDateTime getReleaseAt() {
        return releaseAt;
    }
    public void setReleaseAt(OffsetDateTime releaseAt) {
        this.releaseAt = releaseAt;
    }

    public String getStoreType() {
        return storeType;
    }
    public void setStoreType(String storeType) {
        this.storeType = storeType;
    }

    public Integer getInitialStock() {
        return initialStock;
    }
    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }

    public Integer getRuns() {
        return runs;
    }
    public void setRuns(Integer runs) {
        this.runs = runs;
    }

    public Integer getSeed() {
        return seed;
    }
    public void setSeed(Integer seed) {
        this.seed = seed;
    }
}
