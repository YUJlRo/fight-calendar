# ファイトカレンダー - Android MVP実装

## 📱 概要
Android版ファイトカレンダーのMVP実装です。端末内完結、オフライン優先、外部送信なしの時間管理アプリです。

## 🎨 ブランド・デザイン

### ブランドカラー
- **Primary**: #81CAC4 (ターコイズ)
- **アイコンモチーフ**: 鯨（ミニマルデザイン）
- **トーン**: ゲーミフィケーション＋クリーン

### Material3 カラーパレット
- **Light Theme**: Primary #81CAC4, Container #BAEFEB, Tertiary #FF7A59
- **Dark Theme**: Primary #5DB5AF, Container #0E3432, Tertiary #FF8A6B

### 固定カテゴリ色
- **仕事**: #2BB673 (緑)
- **学習**: #2F6DF6 (青)  
- **娯楽・SNS**: #FF7A59 (オレンジ)
- **ツール**: #7E57C2 (紫)

## 📄 ライセンス・出典
アイコン素材の詳細は [NOTICE-ICONS.md](./NOTICE-ICONS.md) をご確認ください。

## 🎯 主要機能

### ✅ 実装済み
1. **プロジェクト構造とManifest設定**
   - 権限: PACKAGE_USAGE_STATS, READ/WRITE_CALENDAR, POST_NOTIFICATIONS
   - minSdk 26, targetSdk 34+
   - Hilt DI, Room, WorkManager, Compose, Glance

2. **Roomデータベース**
   - AppEntity: アプリ情報管理
   - UsageSessionEntity: 使用セッション記録
   - DailyAggregateEntity: 日次集計データ
   - FreeSlotEntity: 空き時間スロット
   - SettingsEntity: アプリ設定

3. **UsageStatsManager連携**
   - 使用イベントからセッション復元
   - 5分未満セッション除外
   - 自動カテゴリ分類（4カテゴリ固定）

4. **Calendar Provider連携**
   - 透明イベント作成・更新
   - 日次サマリ（All-day, Free）
   - 時間帯ログ（1時間単位）
   - #fightcalendarタグでの一括削除

5. **空き時間検出アルゴリズム**
   - 稼働時間内のBusyでない時間を計算
   - 60分以上の連続区間のみ抽出
   - 重複イベントのマージ処理

6. **Jetpack Compose UI**
   - ✅ デイ画面（勝率、24hタイムライン、空き時間リスト）
   - ✅ オンボーディング4ステップ（権限、カレンダー選択、稼働帯設定）
   - ✅ Material3テーマ適用（#81CAC4基調）
   - ✅ 24時間タイムライン＋ミニタイムライン（カテゴリ色分け）

7. **大ウィジェット (Glance)**
   - ✅ 勝率・合計使用時間表示
   - ✅ 24セグメントのミニタイムライン
   - ✅ 空き時間サマリー＋次の空き枠
   - ✅ 「ブロック」CTA＋アプリ起動ボタン

8. **ブランド仕上げ**
   - ✅ 鯨アイコン（CC0、ミニマルデザイン）
   - ✅ ランチャーアイコン一式
   - ✅ スプラッシュスクリーン（#81CAC4背景）
   - ✅ 通知アイコン（白単色、24dp）

### 🚧 実装予定（残りタスク）
9. **カテゴリ管理画面**
   - アプリ一覧・検索
   - 辞書サジェスト＋手動割当

10. **設定画面**
    - 稼働/睡眠帯設定（TimePicker）
    - タイムライン同期ON/OFF
    - 一括削除ボタン
   - 稼働時間設定

9. **WorkManager同期**
   - 日次0:30バッチ処理
   - ウィジェット更新

10. **テスト・ドキュメント**
    - ユニットテスト
    - プライバシーポリシー

## 🏗️ アーキテクチャ

```
app/
├── data/
│   ├── db/           # Room データベース
│   ├── usage/        # UsageStats処理
│   └── calendar/     # Calendar Provider連携
├── domain/
│   ├── model/        # ドメインモデル
│   └── usecase/      # ビジネスロジック
├── ui/
│   ├── onboarding/   # オンボーディング
│   ├── home/         # メイン画面
│   ├── widgets/      # ウィジェット
│   └── components/   # 共通コンポーネント
└── worker/           # バックグラウンド処理
```

## 🎮 ゲーミフィケーション要素

### 勝率システム
- 計算式: `(仕事+学習時間) / 稼働時間 × 100`
- リアルタイム表示
- 目標値: 50% (設定可能)

### ストリーク
- 連続達成日数を記録
- 3日連続でバッジ表示

### クエスト
- 空き時間ブロック成功でコンボ積み上げ
- 日次・週次目標設定

## 📊 データフロー

1. **UsageStatsManager** → セッション復元 → **Room DB**
2. **Calendar Provider** → Busyイベント取得 → **空き時間計算**
3. **日次集計** → **勝率・ストリーク計算** → **ウィジェット更新**
4. **透明イベント作成** → **Googleカレンダー反映**

## 🔒 プライバシー・セキュリティ

- **完全オフライン**: 外部送信一切なし
- **端末内完結**: データは全てローカルDB
- **権限最小化**: 必要最小限の権限のみ
- **透明性**: オープンソース、トラッカー不使用

## 🚀 ビルド・実行

```bash
# 依存関係インストール
./gradlew build

# デバッグビルド
./gradlew assembleDebug

# リリースビルド
./gradlew assembleRelease

# テスト実行
./gradlew test
```

## 📋 必要権限

1. **PACKAGE_USAGE_STATS**: アプリ使用時間取得
2. **READ_CALENDAR**: カレンダー予定読み取り
3. **WRITE_CALENDAR**: 透明イベント作成
4. **POST_NOTIFICATIONS**: 同期結果通知

## 🎯 受け入れ基準

- [x] 権限付与後、デイ画面に当日のカテゴリ割合表示
- [x] 稼働時間に基づく勝率計算・表示
- [x] ウィジェットに勝率、ミニタイムライン、空き時間表示
- [x] カレンダーに日次サマリ作成（重複回避）
- [x] タイムライン同期ON時の時間帯イベント作成
- [x] 60分以上の空き時間のみ表示
- [x] #fightcalendarイベントの一括削除
- [x] 外部送信なし（ネットワーク不要）

## 📦 配布準備

- **GitHub Releases**: 署名APK
- **F-Droid / IzzyOnDroid**: オープンソース配布
- **プライバシー重視**: トラッカー・広告なし

## 🔧 開発ツール

- **Android Studio**: Iguana以降
- **Kotlin**: 1.9.10
- **Compose**: 2023.10.01
- **Room**: 2.6.1
- **Hilt**: 2.48
- **WorkManager**: 2.9.0
- **Glance**: 1.0.0

## 📄 ライセンス

MIT License - 無料配布、改変可能

---

**完全オフライン・プライバシー重視の時間管理アプリ**  
**開発**: Android Senior Engineer  
**バージョン**: 1.0.0 MVP