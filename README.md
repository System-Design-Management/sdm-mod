# SDM Mod

[![Minecraft Version](https://img.shields.io/badge/Minecraft-%5B1.21.6%5D-green.svg)](https://www.minecraft.net)
[![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-lightgrey.svg)](https://fabricmc.net/)

Minecraft (Java Edition) Fabric 向け MOD リポジトリです。

---

## 📖 概要 (Overview)

このリポジトリは、System Design Management 向けの Minecraft Fabric MOD を開発するためのものです。

## ⚙️ 必須環境 (Requirements)

開発・テストは以下の環境を前提としています。

* **Minecraft:** `[1.21.6]`
* **Mod Loader:** [Fabric Loader](https://fabricmc.net/use/installer/)
* **API:** [Fabric API](https://modrinth.com/mod/fabric-api)

## セットアップ手順

Firstly, clone this repository using Git:

- 開発者向け
```sh
git clone git@github.com:System-Design-Management/sdm-mod.git
```

- mod を利用する方向け
```sh
git clone https://github.com/System-Design-Management/sdm-mod.git
```

Once you've opened the project in your IDE, it should automatically load the project's Gradle configuration and perform the necessary setup tasks.

IntelliJ IDEA
- If you're using the command line, you can use the following Gradle commands to start the game:
  - `./gradlew runServer`: Start the game in server mode.（ログのみ表示される）
  - `./gradlew runClient`: Start the game in client mode.（ゲーム画面が起動）
-  mod の挙動をゲーム画面で確認するときは、サーバーモードでゲームを開始したままクライアントモードでゲームを開始すればよい。

### Gradle コマンドが使えない場合

このリポジトリでは `./gradlew` を使う想定ですが、`gradlew` が存在しない状態だと以下のようなエラーになります。

- `zsh: no such file or directory: ./gradlew`
- `zsh: permission denied: ./gradle`

`./gradle` は実行ファイルではなくディレクトリのため、`./gradle runServer` は実行できません。

その場合は、まずローカルに Gradle をインストールして wrapper を再生成してください。

macOS:

```sh
brew install gradle
gradle wrapper
./gradlew runServer
```

Windows (PowerShell):

```powershell
winget install Gradle.Gradle
gradle wrapper
.\gradlew.bat runServer
```

クライアントを起動する場合は、wrapper 再生成後に以下を実行してください。

macOS:

```sh
./gradlew runClient
```

Windows (PowerShell):

```powershell
.\gradlew.bat runClient
```

[Fabric公式ドキュメント](https://docs.fabricmc.net/develop/getting-started/)を参考にしてください。
IDEは[IntelliJ IDEA](https://www.jetbrains.com/ja-jp/idea/) (community版) を推奨しています。（無料）

## ストーリー開始コマンド

ストーリー開始時の初期化は、ワールド起動時の自動実行ではなくコマンド実行で行います。

- コマンド: `/sdm_story start`
- 実行権限: 権限レベル 2 以上（ゲーム開始時にプレイヤーへコマンド権限を自動付与）

このコマンドを実行すると、以下の初期化が行われます。

- 実績解除の告知を無効化する
- 時刻を夜に設定し、以後変化しないようにする
- 天気を晴れに設定し、以後変化しないようにする
- ストーリー進行状態を開始地点に戻す
- 全プレイヤーのインベントリとエンダーチェストを空にする
- 全プレイヤーの体力、満腹度、呼吸ゲージ、経験値などを初期状態に戻す
- 全プレイヤーをアドベンチャーモードにする
- 各プレイヤーを実行主体にして `function thepa:give/revolver` を順に実行する
- 各プレイヤーを実行主体にして `function thepa:give/bullets` を順に実行する

開始地点へのテレポートは、座標確定後に追加予定です。

## 📚 参考ドキュメント (Reference)

MOD開発の学習には、以下のFabric公式ドキュメントが役立ちます。

* **Fabric ドキュメント (Getting Started):**
    * https://docs.fabricmc.net/develop/getting-started/
    * https://maven.fabricmc.net/docs/yarn-1.21.6+build.1/
    * https://maven.fabricmc.net/docs/fabric-api-0.128.2+1.21.6/
    * https://maven.fabricmc.net/docs/fabric-loader-0.17.3/

## 🔫 Guns++ 導入方針

このリポジトリでは、銃要素を Java の mod 依存として追加せず、`Guns++ 5.8.7+mod` を外部の mod jar として利用する方針にします。

- `Point Blank` や `GeckoLib` はこのプロジェクトの Gradle 依存に含めません。
- `Guns++` は `build.gradle` には追加しません。
- `Guns++` の導入は Minecraft 実行環境側で行います。

### 必要な配布物

- `guns++-5.8.7.jar`

`Guns++ V5.8.7` は Fabric / Forge / NeoForge / Quilt に対応し、Minecraft `1.21.6-1.21.10` を対象にしています。

### 導入手順

1. `guns++-5.8.7.jar` を取得する
2. jar を `run/mods/` に配置する
3. `./gradlew runClient` で起動する
4. タイトル画面の mod 一覧またはログで `Guns++` が読み込まれていることを確認する

### このリポジトリとの関係

- このリポジトリは Fabric 1.21.6 の mod 開発用です。
- `Guns++` はゲーム実行時に `run/mods/` へ追加して併用します。
- Java コードから `Guns++` のクラスや API を参照する前提ではありません。
- 連携が必要な場合は、まず `Guns++` が公開している連携手段の有無を確認してください。

## 📜 ライセンス (License)

MIT LICENSE
