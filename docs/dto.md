# DTO仕様（MVP）

本ドキュメントは `POST /api/v1/simulations` で利用する DTO を定義する。  
API 全体の仕様は `docs/api.md` を参照。
- Base Path: `/api/v1`
- Content-Type: `application/json`
- Date-Time: RFC 3339（例: `2026-02-10T10:00:00+09:00`）
- Timezone: リクエストで明示しない場合は `Asia/Tokyo`（MVP）

## 1. Request DTO（入力）

想定クラス名: `SimulationRequest`

| フィールド | 型 | 必須 | 説明 |
|---|---|---|---|
| `productName` | `string` | 必須 | UI表示用の名前（シミュレーション結果の見出し） |
| `popularity` | `string` | 必須 | 需要の強さ。需要が強いほど来店数や購入回数が増える前提 |
| `releaseAt` | `string (date-time)` | 必須 | 時系列の起点（この時刻を0分として集計） |
| `storeType` | `string` | 必須 | 店舗特性（来店しやすさ、初期在庫傾向など） |
| `initialStock` | `integer` | 必須 | 初期在庫数（MVPは手入力固定） |
| `snsBoostEnabled` | `boolean` | 必須 | `true` なら需要係数を上げる想定（v1はダミー可） |
| `simulationHours` | `integer` | 必須 | 何時間先までシミュレーションするか |
| `timeBucketMinutes` | `integer` | 必須 | 何分ごとに集計するか（グラフ粒度） |
| `runs` | `integer` | 必須 | モンテカルロ試行回数（増えるほど滑らかだが重い） |
| `seed` | `integer` | 任意 | 再現性確保用。同条件で同じ結果を得たいときに指定 |

### Request 値の想定（MVP）

- `popularity`: `LOW | MEDIUM | HIGH`
- `storeType`: `LARGE | STATION | SMALL`
- `releaseAt`: RFC 3339 形式（例: `2026-02-10T10:00:00+09:00`）

### Request バリデーション（MVP）

| フィールド | 制約 |
|---|---|
| `productName` | 必須、1〜100文字 |
| `popularity` | 必須、`LOW \| MEDIUM \| HIGH` のいずれか |
| `releaseAt` | 必須、RFC 3339 形式の日時 |
| `storeType` | 必須、`LARGE \| STATION \| SMALL` のいずれか |
| `initialStock` | 必須、`1` 以上 |
| `snsBoostEnabled` | 必須、boolean |
| `simulationHours` | 必須、`1` 以上 `72` 以下 |
| `timeBucketMinutes` | 必須、`5 \| 10 \| 15 \| 30 \| 60` のいずれか |
| `runs` | 必須、`100` 以上 `10000` 以下 |
| `seed` | 任意、指定時は整数 |

### バリデーションエラー時の扱い

- `HTTP 422 Unprocessable Entity` を返す
- レスポンス形式は `docs/api.md` の「4.2 異常系」に従う
- JSON構文不正や JSON→DTO 変換失敗（enum不正、日時フォーマット不正など）は `HTTP 400 Bad Request`

### Request 例

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

## 2. Response DTO（出力）

想定クラス名: `SimulationResponse`

| フィールド | 型 | 必須 | 説明 |
|---|---|---|---|
| `soldOutAt` | `string (date-time) \| null` | 必須 | 売り切れ見込み時刻（期間内に売り切れない場合は `null`） |
| `soldOutProbability` | `number` | 必須 | シミュレーション期間内で売り切れる確率 |
| `remainingProbabilityByTime` | `array` | 必須 | 時間帯別の「残っている確率」 |
| `inventorySeries` | `array` | 必須 | 平均在庫推移（期待値） |
| `recommendations` | `array<string>` | 必須 | 来店行動への提案文 |
| `meta` | `object` | 必須 | モデル情報・試行回数・生成時刻など |

### Response 内部DTO

- `remainingProbabilityByTime[]`
  - `time`: `string (date-time)`
  - `remainingProbability`: `number`（0.0〜1.0）

- `inventorySeries[]`
  - `time`: `string (date-time)`
  - `expectedRemaining`: `number`

- `meta`
  - `modelVersion`: `string`
  - `runs`: `integer`
  - `seed`: `integer | null`
  - `generatedAt`: `string (date-time)`

### Response 例

```json
{
  "soldOutAt": "2026-02-10T14:30:00+09:00",
  "soldOutProbability": 0.82,
  "remainingProbabilityByTime": [
    { "time": "2026-02-10T10:00:00+09:00", "remainingProbability": 1.0 },
    { "time": "2026-02-10T12:00:00+09:00", "remainingProbability": 0.67 }
  ],
  "inventorySeries": [
    { "time": "2026-02-10T10:00:00+09:00", "expectedRemaining": 120 },
    { "time": "2026-02-10T12:00:00+09:00", "expectedRemaining": 74 }
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
