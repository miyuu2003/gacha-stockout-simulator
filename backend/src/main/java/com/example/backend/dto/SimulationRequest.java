package com.example.backend.dto;
import java.time.OffsetDateTime;
import jakarta.validation.constraints.*;
/**
 * POST /api/v1/simulations リクエスト内容
 * JSON -> Javaオブジェクト へ自動変換される（Jackson）
 */
public class SimulationRequest {
    //入力パラメーターフィールド
    @NotBlank
    @Size(max = 100)
    private String productName;

    @NotNull
    private Popularity popularity;

    @NotNull
    private OffsetDateTime releaseAt;

    @NotNull
    private StoreType storeType;

    @NotNull
    @Min(1)
    private Integer initialStock;

    @NotNull
    private Boolean snsBoostEnabled;

    @NotNull
    @Min(1)
    @Max(72)
    private Integer simulationHours;

    @NotNull
    private Integer timeBucketMinutes;

    @NotNull
    @Min(100)
    @Max(10000)
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

    public Popularity getPopularity() {
        return popularity;
    }
    public void setPopularity(Popularity popularity) {
        this.popularity = popularity;
    }

    public OffsetDateTime getReleaseAt() {
        return releaseAt;
    }
    public void setReleaseAt(OffsetDateTime releaseAt) {
        this.releaseAt = releaseAt;
    }

    public StoreType getStoreType() {
        return storeType;
    }
    public void setStoreType(StoreType storeType) {
        this.storeType = storeType;
    }

    public Integer getInitialStock() {
        return initialStock;
    }
    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }

    public Boolean getSnsBoostEnabled() {
        return snsBoostEnabled;
    }
    public void setSnsBoostEnabled(Boolean snsBoostEnabled) {
        this.snsBoostEnabled = snsBoostEnabled;
    }

    public Integer getSimulationHours() {
        return simulationHours;
    }
    public void setSimulationHours(Integer simulationHours) {
        this.simulationHours = simulationHours;
    }

    public Integer getTimeBucketMinutes() {
        return timeBucketMinutes;
    }
    public void setTimeBucketMinutes(Integer timeBucketMinutes) {
        this.timeBucketMinutes = timeBucketMinutes;
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
