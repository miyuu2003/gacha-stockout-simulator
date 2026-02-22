# API仕様（MVP）

本ドキュメントは `gacha-stockout-simulator` のバックエンド API 仕様（ver.1）を定義する。
DTO（Request/Response）の詳細は `docs/dto.md` を参照。

## 1. 基本方針

- アーキテクチャ: REST API（JSON）
- バージョン: `/api/v1`
- 文字コード: UTF-8
- 日時フォーマット: RFC 3339（例: `2026-02-07T10:00:00+09:00`）
- タイムゾーン: リクエストで明示しない場合は `Asia/Tokyo`
- Content-Type: `application/json`

## 2. ベースURL

- ローカル開発: `http://localhost:8080/api/v1`
- EC2 本番想定: `http://<Public IPv4>/api/v1`
  - Nginx の `/api` リバースプロキシ経由

## 3. 認証・認可（MVP）

- MVP では認証なし
- 将来は API Key または JWT を導入予定

## 4. 共通レスポンス仕様

### 4.1 正常系
- `Content-Type: application/json`

### 4.2 異常系

```json
{
  "code": "VALIDATION_ERROR",
  "message": "Invalid request parameters",
  "details": [
    {
      "field": "initialStock",
      "reason": "must be greater than 0"
    }
  ],
  "timestamp": "2026-02-07T12:00:00+09:00"
}
```

### 4.3 主なHTTPステータス

- `200 OK`: 正常
- `400 Bad Request`: リクエストが解釈できない（malformed JSON / enum不正 / 日時フォーマット不正 など）
- `422 Unprocessable Entity`: バリデーションエラー（@Valid の制約違反）
- `500 Internal Server Error`: サーバー内部エラー

## 5. エンドポイント

### 5.1 ヘルスチェック

- Method: `GET`
- Path: `/health`（フルパス: `/api/v1/health`）
- 説明: 稼働確認用

レスポンス例:

```json
{
  "status": "UP",
  "service": "gacha-stockout-backend",
  "time": "2026-02-07T12:00:00+09:00"
}
```

### 5.2 シミュレーション実行（MVP主API）

- Method: `POST`
- Path: `/simulations`（フルパス: `/api/v1/simulations`）
- 説明: 条件を入力し、在庫推移と枯渇予測を返す（同期実行）
- DTO定義: docs/dto.md を参照（Request/Responseのフィールド・型）

### リクエスト

```json
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
```

### バリデーション
※ 制約値・許容値は docs/dto.md の定義に従う。
- バリデーション失敗時は 422 Unprocessable Entity を返す
- エラーボディは「4.2 異常系（共通エラー形式）」に従う
- enum 不正（例: popularity が LOW|MEDIUM|HIGH 以外）も 422 扱い

### レスポンス

```json
{
  "soldOutAt": "2026-02-10T14:30:00+09:00",
  "soldOutProbability": 0.82,
  "remainingProbabilityByTime": [
    { "time": "2026-02-10T10:00:00+09:00", "remainingProbability": 1.0 },
    { "time": "2026-02-10T12:00:00+09:00", "remainingProbability": 0.67 },
    { "time": "2026-02-10T14:30:00+09:00", "remainingProbability": 0.18 }
  ],
  "inventorySeries": [
    { "time": "2026-02-10T10:00:00+09:00", "expectedRemaining": 120 },
    { "time": "2026-02-10T12:00:00+09:00", "expectedRemaining": 74 },
    { "time": "2026-02-10T14:30:00+09:00", "expectedRemaining": 9 }
  ],
  "recommendations": [
    "発売日午前中の来店が有利です",
    "この条件では初日夕方以降の在庫確率は低いです"
  ],
  "meta": {
    "modelVersion": "mvp-1",
    "runs": 1000,
    "seed": 42,
    "generatedAt": "2026-02-07T12:00:00+09:00"
  }
}
```

### 補足

- soldOutAt は、シミュレーション期間内に売り切れ見込みがない場合 null
- soldOutProbability は、シミュレーション期間内で売り切れる確率
- remainingProbabilityByTime / inventorySeries はフロントの時系列グラフ表示用

## 6. フロント連携メモ

- 開発時は `frontend`（Vite）から `backend`（Spring Boot:8080）を呼ぶ
- 本番時は Nginx 経由で `/api/*` に統一
- CORS は開発環境のみ `http://localhost:5173` を許可

## 7. 今後の拡張候補

- `GET /simulations/{id}`: 実行結果の再取得
- `POST /simulations:batch`: 条件比較の一括実行
- 条件保存・再シミュレーション
- SNS拡散係数の詳細パラメータ化
