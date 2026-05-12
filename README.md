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

### IDE 開発時にバックアップしたワールドを使う方法

`./gradlew runClient` で起動する開発環境は、通常の `.minecraft/saves/` ではなく、このリポジトリの `run/saves/` をワールド保存先として使います。

既存ワールドのバックアップを開発環境で使いたい場合は、バックアップしたワールドフォルダを `run/saves/` の下に配置してください。

例:

```sh
run/saves/MyWorld/
```

この状態で `./gradlew runClient` を起動すると、ワールド選択画面に `MyWorld` が表示されます。

通常の Minecraft で使っているワールドを直接編集すると破損時の影響が大きいため、コピーしたバックアップを `run/saves/` に置いて利用することを推奨します。

### 開発用ワールドを自動コピー&動画再生する方法

毎回 `run/saves/` に手で配置しなくてよいように、ローカル設定ファイルから自動同期できます。

1. ルートの `local.dev.properties.example` を参考に、`local.dev.properties` を作成する
2. `devWorldSourceDir` に元ワールドのパスを書く
3. `devWorldName` に `run/saves/` 側で使いたいフォルダ名を書く
4. `opVideoPath`,`edVideoPath`,`vlcLibPath`にそれぞれオープニング動画、エンディング動画、VLC.appのパスを書く（VLC.appについては後述する）
5. `./gradlew runClient` または `./gradlew runServer` を実行する

例:

```properties
devWorldSourceDir=backup/20260329
devWorldName=MyWorld
```

この設定がある場合、起動前に Gradle の `syncDevWorld` タスクが走り、`run/saves/MyWorld/` に自動で同期します。

注意:

- `local.dev.properties` は `.gitignore` 対象なので、各開発者がローカルで設定します。
- `devWorldSourceDir` はプロジェクトルートからの相対パスとして書けます。
- 同期先は `run/saves/<devWorldName>/` です。
- 元ワールドの内容が更新されたら、次回の `runClient` / `runServer` 実行時に再同期されます。

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

## MOD の導入先

`./gradlew build` を実行すると、このプロジェクトの配布用 jar は `build/libs/sdm-mod-1.0-SNAPSHOT.jar` に生成されます。

この jar を実際の Minecraft に導入する場合は、Fabric を導入済みの Minecraft の `mods` フォルダに配置してください。

- macOS: `~/Library/Application Support/minecraft/mods/`
- Windows: `%AppData%/.minecraft/mods/`
- Linux: `~/.minecraft/mods/`

開発環境で `./gradlew runClient` を使って起動する場合は、実行用ディレクトリが `run/` になるため、外部 mod は `run/mods/` に配置します。
このプロジェクト自身の mod は起動時に自動で読み込まれるため、`build/libs/sdm-mod-1.0-SNAPSHOT.jar` を `run/mods/` にコピーする必要はありません。

## アイテムの実装方法

新しいアイテムをMODに追加する手順です。ここでは `my_item`（日本語名: 魔法の石）を例に説明します。

### 概要：必要なファイル一覧

アイテム1つを追加するには、以下の**6種類のファイル**が必要です。

```
src/main/
├── java/.../sdm_mod/
│   └── ModItems.java                          ← (1) アイテム登録（Java）
└── resources/assets/sdm_mod/
    ├── items/
    │   └── my_item.json                       ← (2) モデル定義（1.21.4+で必須）
    ├── models/item/
    │   ├── my_item.json                       ← (3) 3Dモデル形状
    │   └── my_item_icon.json                  ← (4) インベントリ用アイコン（3Dの場合）
    ├── textures/item/
    │   ├── my_item.png                        ← (5) 3Dモデル用テクスチャ
    │   └── my_item_item.png                   ← (6) インベントリアイコン用テクスチャ
    └── lang/
        ├── ja_jp.json                         ← (7) 日本語名
        └── en_us.json                         ← (7) 英語名
```

> **2Dのフラットなアイテム**（学生証のような画像そのままの見た目）の場合は、(3)(5) のみで (4)(6) は不要です。

---

### Step 1: ModItems.java にアイテムを登録する

`src/main/java/jp/ac/u_tokyo/sdm/sdm_mod/ModItems.java` を開き、既存アイテムの下に1行追加します。

```java
public static final Item MY_ITEM = register(
    "my_item",
    settings -> new Item(settings)
);
```

- `MY_ITEM` はJava内の変数名（大文字スネークケース）
- `"my_item"` がゲーム内のID（小文字スネークケース）。**この名前を以降のファイルで統一して使います。**

---

### Step 2: `items/my_item.json` を作成する

`src/main/resources/assets/sdm_mod/items/my_item.json` を作成します。

**3Dモデルを使う場合（インベントリとゲーム内で別の見た目にする）:**

```json
{
  "model": {
    "type": "minecraft:select",
    "property": "minecraft:display_context",
    "cases": [
      {
        "when": ["gui", "fixed"],
        "model": {
          "type": "minecraft:model",
          "model": "sdm_mod:item/my_item_icon"
        }
      }
    ],
    "fallback": {
      "type": "minecraft:model",
      "model": "sdm_mod:item/my_item"
    }
  }
}
```

**2Dフラットアイテムの場合（シンプル）:**

```json
{
  "model": {
    "type": "minecraft:model",
    "model": "sdm_mod:item/my_item"
  }
}
```

> ここで指定した `"sdm_mod:item/my_item"` は、次のStep 3で作るファイルを指しています。

---

### Step 3: モデルJSONを作成する

#### 3Dモデルの場合

[Blockbench](https://www.blockbench.net/) で3Dモデルを作り、`File > Export > Export Block/Item Model` でエクスポートします。

出力されたJSONを `src/main/resources/assets/sdm_mod/models/item/my_item.json` に配置した後、**必ず以下の修正を行います：**

| 修正内容 | 理由 |
|---------|------|
| `"format_version": "..."` の行を削除 | Minecraftが認識しないフィールド |
| `"texture_size": [...]` の行を削除 | 残すとUV座標がずれてテクスチャが壊れる |
| テクスチャ参照に `sdm_mod:item/` を追加 | パスが合わないと紫チェック柄になる |

```json
// 修正前（Blockbench出力そのまま）
"textures": {
  "0": "my_item"
}

// 修正後
"textures": {
  "0": "sdm_mod:item/my_item"
}
```

さらに、インベントリ用アイコンのモデル `my_item_icon.json` も作成します：

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "sdm_mod:item/my_item_item"
  }
}
```

#### 2Dフラットアイテムの場合

```json
{
  "parent": "minecraft:item/generated",
  "textures": {
    "layer0": "sdm_mod:item/my_item"
  }
}
```

---

### Step 4: テクスチャ画像を配置する

PNG画像を `src/main/resources/assets/sdm_mod/textures/item/` に置きます。

| ファイル名 | 用途 |
|-----------|------|
| `my_item.png` | 3Dモデルに貼るテクスチャ（3Dの場合） |
| `my_item_item.png` | インベントリに表示するアイコン（3Dの場合） |
| `my_item.png` | 2Dアイテムの場合はこれ1枚でOK |

> **注意:** `textures/entity/` に置くと紫・黒のチェック柄（Missing Texture）になります。必ず `textures/item/` に置いてください。

---

### Step 5: 言語ファイルに名前を追加する

`src/main/resources/assets/sdm_mod/lang/ja_jp.json` と `en_us.json` の両方に追加します。

**ja_jp.json:**
```json
{
  "item.sdm_mod.my_item": "魔法の石"
}
```

**en_us.json:**
```json
{
  "item.sdm_mod.my_item": "Magic Stone"
}
```

---

### Step 6: 動作確認

```sh
./gradlew runClient
```

ゲームを起動して、クリエイティブモードのアイテム検索で `my_item` を検索し、正常に表示されることを確認します。

**紫・黒チェック柄になった場合:**

```sh
grep "my_item\|Missing" run/logs/latest.log
```

ログにエラーパスが出るので、そのパスのファイルを確認してください。ほとんどの場合、テクスチャのパス指定ミスが原因です。

---

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
- mob の自然スポーンを無効化する
- プレイヤー以外の生存エンティティを削除する
- 全プレイヤーの視線先に当たったブロック付近だけを、弱い光で照らす
- 各プレイヤーを実行主体にして `function thepa:give/revolver` を順に実行する
- 各プレイヤーを実行主体にして `function thepa:give/bullets` を順に実行する

開始地点へのテレポートは、座標確定後に追加予定です。

## ストーリー実装の配置方針

ストーリー実装は [src/main/java/jp/ac/u_tokyo/sdm/sdm_mod/story](/Users/miyoshinaoki/sdm-mod/src/main/java/jp/ac/u_tokyo/sdm/sdm_mod/story) 配下に集約し、各フェーズ固有のコードは `phase1/` から `phase6/` に分けて管理します。

各フェーズフォルダには `README.md` を置き、そのフェーズの目的、責務、開始条件、完了条件を先に整理してから実装を追加する方針にします。

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

- `guns++-5.8.7.jar`(https://modrinth.com/datapack/guns++/versions?g=1.21.6&l=fabric)

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

## VLC.app 導入
マイクラ内で動画再生をするために**VLC.app**をダウンロードする必要があります。

以下のサイトからダウンロードしてください。

https://www.videolan.org/vlc/index.ja.html



## 📜 ライセンス (License)

MIT LICENSE
