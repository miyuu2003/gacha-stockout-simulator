# EC2 デプロイ手順（Amazon Linux 2023 / Nginx / Spring Boot / React）

本ドキュメントは  
**EC2 1台構成で「フロントエンド（静的配信）＋バックエンド（REST API）」を運用するための手順メモ**である。

- フロントエンド：React + Vite（静的ファイル）
- バックエンド：Java + Spring Boot（API）
- OS：Amazon Linux 2023
- Webサーバ：Nginx
- デプロイ方式：手動（学習・MVP用途）

---

## 構成概要
ブラウザ
↓ HTTP(80)
EC2
├─ Nginx
│    ├─ /        → React 静的ファイル
│    └─ /api     → Spring Boot(localhost:8080)
└─ Spring Boot

- 外部には **80 / 443 のみ公開**
- Spring Boot の 8080 は **内部通信専用**

---

## 1. AWSアカウント初期設定

### 1.1 セキュリティ
- ルートユーザー
  - MFA 有効化
- 管理者 IAM ユーザー
  - `AdministratorAccess` を付与
  - MFA 有効化
- 以降の作業は **IAMユーザーで実施**

### 1.2 コスト管理
- AWS Budgets
  - $5 / $10 でアラート設定
- Free Tier / クレジット制の内容を事前確認

---

## 2. EC2 作成

### 2.1 インスタンス
- AMI：Amazon Linux 2023
- インスタンスタイプ：t3.micro
- ストレージ：8GB（gp3）

### 2.2 セキュリティグループ
| ポート | 許可元 |
|------|------|
| 22 | 自分のIPのみ |
| 80 | 0.0.0.0/0 |
| 443 | 0.0.0.0/0 |
| 8080 | ❌ 開放しない |

---

## 3. EC2 初期セットアップ

### 3.1 SSH 接続
```bash
chmod 400 gacha-ec2-key.pem
ssh -i gacha-ec2-key.pem ec2-user@<Public IPv4>
```
### 3.2 システム更新
```bash
sudo dnf update -y
```
### 3.3 Java(Spring Boot用)
```bash
sudo dnf install -y java-17-amazon-corretto
java -version
```
### 3.4 NginX
```bash
sudo dnf install -y nginx
sudo systemctl enable nginx
sudo systemctl start nginx
```
- ブラウザで http://<Public IPv4> 
- Nginx のデフォルトページ（Welcome to nginx!）が表示されることを確認
---

## 4. ローカルでのビルド

EC2 には **ビルド済み成果物のみ** を配置する。  
Node.js や Gradle を EC2 に入れないことで、環境をシンプルに保つ。

### 4.1 Backend（Spring Boot）

```bash
cd backend
./gradlew bootJar
```
成功時に以下にJARファイルを生成
- backend/build/libs/*.jar

### 4.2 Frontend (React + Vite)
```bash
cd frontend
npm install
npm run build
```
成功時に以下にJARファイルを生成
- frontend/dist/

## 5. EC2への配置 
### 5.1 配置用ディレクトリ作成（EC2）
```bash
sudo mkdir -p /opt/gacha/backend
sudo mkdir -p /var/www/gacha
sudo chown -R ec2-user:ec2-user /opt/gacha /var/www/gacha
```
-  /opt/gacha/backend：Spring Boot の JAR 配置場所
-  /var/www/gacha：React の静的ファイル配置場所

### 5.2 ファイル転送（ローカル → EC2）
```bash
# backend
scp -i gacha-ec2-key.pem backend/build/libs/app.jar \
  ec2-user@<Public IPv4>:/opt/gacha/backend/app.jar
# frontend
scp -i gacha-ec2-key.pem -r frontend/dist/* \
  ec2-user@<Public IPv4>:/var/www/gacha/
```

## 6. Spring Bootの起動（systemd）
EC2 再起動後も自動で API が起動するよう、systemd サービスとして登録する。
### 6.1 サービス定義作成
```bash
sudo vi /etc/systemd/system/gacha-backend.service
```
```ini
[Unit]
Description=Gacha Stockout Simulator Backend
After=network.target

[Service]
User=ec2-user
ExecStart=/usr/bin/java -jar /opt/gacha/backend/app.jar
Restart=always
Environment=SPRING_PROFILES_ACTIVE=prod

[Install]
WantedBy=multi-user.target
```

### 6.2 起動・有効化
```bash
sudo systemctl daemon-reload
sudo systemctl enable gacha-backend
sudo systemctl start gacha-backend
```

### 6.3 動作確認（EC2内）
```bash
curl http://localhost:8080
# または API エンドポイント
```

## 7. Nginx設定（/と/apiの振り分け）
Nginx を フロントエンド配信＋API リバースプロキシとして設定する。

### 7.1 設定ファイル作成
```bash
sudo vi /etc/nginx/conf.d/gacha.conf
```
```nginx
server {
    listen 80;
    server_name _;

    root /var/www/gacha;
    index index.html;

    location / {
        try_files $uri /index.html;
    }

    location /api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```
### 7.2 設定反映
```bash
sudo nginx -t
sudo systemctl reload nginx
```
## 8. 動作確認
	•	ブラウザで http://<Public IPv4>
	•	React アプリが表示される
	•	フロントエンド操作により /api が呼ばれ
	•	Spring Boot のレスポンスが返る

⸻

## 9. EC2 の停止と再開

### 9.1 停止
	•	開発していない時間は EC2 を Stop
	•	停止中は SSH / ブラウザともに接続不可

### 9.2 再開
	•	Start 後、再びアクセス可能
	•	Public IPv4 が変わる可能性あり

⸻

## 10. 本構成の方針
	•	無料枠・クレジット消費を抑えるため EC2 1台構成
	•	API を外部公開せず、Nginx 経由に限定
	•	学習・MVP 段階では手動デプロイとする