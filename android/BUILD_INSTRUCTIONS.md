# ファイトカレンダー - ビルド・インストール手順

## 🚀 APKビルド手順

### 必要環境
- **Android Studio**: Iguana (2023.2.1) 以降
- **JDK**: 17以降
- **Android SDK**: API Level 34
- **Kotlin**: 1.9.10

### 1. プロジェクト準備

```bash
# プロジェクトディレクトリに移動
cd android/

# Gradle Wrapper権限設定（Linux/Mac）
chmod +x gradlew

# 依存関係ダウンロード
./gradlew build --refresh-dependencies
```

### 2. デバッグAPKビルド

```bash
# デバッグビルド（開発・テスト用）
./gradlew assembleDebug

# 出力先
# android/app/build/outputs/apk/debug/app-debug.apk
```

### 3. リリースAPKビルド

```bash
# リリースビルド（署名あり）
./gradlew assembleRelease

# 出力先
# android/app/build/outputs/apk/release/app-release.apk
```

## 📱 APKインストール手順

### Android端末での有効化
1. **設定** → **セキュリティ** → **不明なアプリのインストール**
2. **Chrome**（またはファイルマネージャー）を選択
3. **この提供元のアプリを許可** をON

### インストール方法

#### 方法A: ADB使用
```bash
# USBデバッグ有効化後
adb install app-debug.apk
```

#### 方法B: 直接インストール
1. APKファイルを端末にコピー
2. ファイルマネージャーでAPKをタップ
3. **インストール** をタップ

## 🔧 開発環境セットアップ

### Android Studio設定
1. **File** → **Open** → `android/`フォルダを選択
2. **SDK Manager**で以下をインストール：
   - Android API 34
   - Android Build Tools 34.0.0
   - Kotlin Multiplatform Mobile plugin

### 初回ビルド注意点
- **インターネット接続必須**: 初回は依存関係ダウンロードに時間がかかります
- **メモリ設定**: 大きなプロジェクトのため、Android StudioのRAM割り当てを増やすことを推奨

## 🧪 テスト実行

```bash
# 単体テスト
./gradlew test

# インストルメントテスト（要エミュレータ/実機）
./gradlew connectedAndroidTest

# Lintチェック
./gradlew lint
```

## 📋 ビルド成果物

### APKファイル
- **app-debug.apk**: 開発・テスト用（約15MB）
- **app-release.apk**: 配布用・最適化済み（約12MB）

### 同梱内容
- ✅ **メインアプリ**: デイ画面・オンボーディング
- ✅ **ホーム画面ウィジェット**: Glance大ウィジェット
- ✅ **鯨アイコン一式**: ランチャー・通知・モノクローム
- ✅ **Material3テーマ**: ライト/ダーク対応

## ⚠️ 既知の制限事項

### データ層未実装
- **UsageStatsManager**: 現在はモックデータ表示
- **Calendar Provider**: カレンダー連携は基盤のみ
- **Room Database**: 永続化は次フェーズ実装

### 権限要求
アプリ初回起動時に以下権限を要求：
- ✅ **使用状況アクセス**: アプリ使用時間取得用
- ✅ **カレンダー読み書き**: 空き時間検出・イベント作成用
- ✅ **通知**: 同期完了通知用

## 🔄 次回更新予定

### v1.2 実装予定
1. **実データ連携**: UsageStats・CalendarProvider
2. **設定画面**: 稼働時間・同期設定
3. **アプリ管理**: カテゴリ手動割当

### トラブルシューティング
- **ビルドエラー**: `./gradlew clean` → 再ビルド
- **署名エラー**: デバッグビルド推奨（開発用）
- **インストールエラー**: 既存版アンインストール後再試行

---

**📱 ファイトカレンダー v1.1**  
**最終更新**: 2024年8月20日  
**対象**: Android 8.0+ (API 26)