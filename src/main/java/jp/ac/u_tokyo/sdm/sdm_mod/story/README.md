# Story Package Layout

`story` パッケージは、ストーリー進行全体の共通基盤と各フェーズ実装を分けて管理します。

## Directory Policy

- `command/`: ストーリー関連コマンドの登録と実行入口
- `registry/`: 章やフェーズ定義の登録
- `runtime/`: 実行中の進行制御
- `service/`: ストーリー演出や初期化などの処理単位
- `state/`: 永続化や同期対象になる進行状態
- `phase1/` - `phase6/`: 各フェーズ固有の実装

## Phase Folder Rule

各 `phaseN/` には以下を置く想定です。

- フェーズ固有の進行制御クラス
- イベント、演出、判定ロジック
- そのフェーズに閉じた補助クラス
- `README.md`: 目的、責務、実装予定メモ

複数フェーズで共通利用する処理は、`service/` や `runtime/` など共通パッケージへ切り出します。
