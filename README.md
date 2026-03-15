# Extra Items

[![Minecraft Version](https://img.shields.io/badge/Minecraft-%5B1.21.6%5D-green.svg)](https://www.minecraft.net)
[![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-lightgrey.svg)](https://fabricmc.net/)

Minecraft (Java Edition) Fabric 向け MOD リポジトリです。

---

## 📖 概要 (Overview)

このリポジトリは、アイテムの追加を行うためのものです。

## ⚙️ 必須環境 (Requirements)

開発・テストは以下の環境を前提としています。

* **Minecraft:** `[1.21.6]`
* **Mod Loader:** [Fabric Loader](https://fabricmc.net/use/installer/)
* **API:** [Fabric API](https://modrinth.com/mod/fabric-api)

## セットアップ手順

Firstly, clone this repository using Git:

- 開発者向け
```sh
git clone git@github.com:System-Design-Management/mod_test-template-1.21.6.git
```

- mod を利用する方向け
```sh
git clone https://github.com/System-Design-Management/mod_test-template-1.21.6.git
```

Once you've opened the project in your IDE, it should automatically load the project's Gradle configuration and perform the necessary setup tasks.

IntelliJ IDEA
- If you're using the command line, you can use the following Gradle commands to start the game:
  - `./gradlew runClient`: Start the game in client mode.（ゲーム画面が起動）
  - `./gradlew runServer`: Start the game in server mode.（ログのみ表示される）
-  mod の挙動をゲーム画面で確認するときは、サーバーモードでゲームを開始したままクライアントモードでゲームを開始すればよい。

[Fabric公式ドキュメント](https://docs.fabricmc.net/develop/getting-started/)を参考にしてください。
IDEは[IntelliJ IDEA](https://www.jetbrains.com/ja-jp/idea/) (community版) を推奨しています。（無料）

## 📚 参考ドキュメント (Reference)

MOD開発の学習には、以下のFabric公式ドキュメントが役立ちます。

* **Fabric ドキュメント (Getting Started):**
    * https://docs.fabricmc.net/develop/getting-started/
    * https://maven.fabricmc.net/docs/yarn-1.21.6+build.1/
    * https://maven.fabricmc.net/docs/fabric-api-0.128.2+1.21.6/
    * https://maven.fabricmc.net/docs/fabric-loader-0.17.3/

## 📜 ライセンス (License)

MIT LICENSE
