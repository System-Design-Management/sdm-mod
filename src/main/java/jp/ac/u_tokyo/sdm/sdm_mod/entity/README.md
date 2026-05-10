# カスタムモブの追加手順

警察官（`PoliceOfficerEntity`）や少年（`BoyEntity`）と同じ構成で新しいモブを追加する手順。

## 追加が必要なファイル一覧

| ファイル | 役割 |
|---------|------|
| `entity/XxxEntity.java` | エンティティの動作定義 |
| `client/render/entity/XxxEntityRenderer.java` | 見た目のレンダラー（クライアント専用） |
| `ModEntities.java` | エンティティタイプの登録 |
| `SdmModClient.java` | レンダラーの登録 |
| `textures/entity/xxx.png` | スキンテクスチャ（64x64px） |

---

## ステップ 1: エンティティクラスを作る

`src/main/java/.../entity/XxxEntity.java` を作成する。
`PoliceOfficerEntity.java` をコピーして名前を書き換えるのが最も簡単。

```java
public class XxxEntity extends ZombieEntity {
    public XxxEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super(entityType, world);
        setAiDisabled(true);   // AI無効（動かない）
        setPersistent();        // リスポーンしない
        setCanPickUpLoot(false);
        setBaby(false);
        setSilent(true);
    }

    public static DefaultAttributeContainer.Builder createXxxAttributes() {
        return ZombieEntity.createZombieAttributes();
    }

    @Override
    protected void initGoals() { }  // 目標AI無し

    @Override
    public boolean canPickupItem(ItemStack stack) { return false; }
}
```

---

## ステップ 2: レンダラークラスを作る

`src/client/java/.../client/render/entity/XxxEntityRenderer.java` を作成する。

```java
public class XxxEntityRenderer
    extends LivingEntityRenderer<XxxEntity, PlayerEntityRenderState, PlayerEntityModel> {

    private static final Identifier TEXTURE = Identifier.of(SdmMod.MOD_ID, "textures/entity/xxx.png");

    public XxxEntityRenderer(EntityRendererFactory.Context context) {
        // 第2引数: false = 通常体型、true = スリム体型
        super(context, new PlayerEntityModel(context.getPart(EntityModelLayers.PLAYER), false), 0.5f);
        this.addFeature(new HeldItemFeatureRenderer<>(this));  // アイテムを手に持たせる場合
    }

    @Override
    public PlayerEntityRenderState createRenderState() {
        return new PlayerEntityRenderState();
    }

    @Override
    public void updateRenderState(XxxEntity entity, PlayerEntityRenderState state, float tickDelta) {
        super.updateRenderState(entity, state, tickDelta);
        ArmedEntityRenderState.updateRenderState(entity, state, this.itemModelResolver);
        state.mainArm = Arm.RIGHT;
    }

    @Override
    public Identifier getTexture(PlayerEntityRenderState state) {
        return TEXTURE;
    }
}
```

### 倒れた姿勢にしたい場合（警察官のように）

`setupTransforms` をオーバーライドして X 軸 90度回転を追加する:

```java
@Override
protected void setupTransforms(PlayerEntityRenderState state, MatrixStack matrices, float bodyYaw, float baseHeight) {
    super.setupTransforms(state, matrices, bodyYaw, baseHeight);
    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90.0f));
}
```

---

## ステップ 3: ModEntities.java に登録する

`src/main/java/.../ModEntities.java` に追加:

```java
// フィールド宣言
public static final EntityType<XxxEntity> XXX = register(
    "xxx",  // エンティティID（スポーンコマンドで使う名前）
    EntityType.Builder
        .create(XxxEntity::new, SpawnGroup.MONSTER)
        .dimensions(0.6f, 1.95f)  // 幅, 高さ（単位: ブロック）
        .maxTrackingRange(8)
);

// initialize() 内
FabricDefaultAttributeRegistry.register(XXX, XxxEntity.createXxxAttributes());
```

---

## ステップ 4: SdmModClient.java にレンダラーを登録する

`src/client/java/.../client/SdmModClient.java` の `onInitializeClient()` に追加:

```java
EntityRendererRegistry.register(ModEntities.XXX, XxxEntityRenderer::new);
```

---

## ステップ 5: テクスチャを配置する

`src/main/resources/assets/sdm_mod/textures/entity/xxx.png` に **64x64px** の PNG を配置する。

Minecraft のプレイヤースキンと同じレイアウト（Steve フォーマット）。
スリム体型（細腕）にする場合は Alex フォーマットを使い、レンダラーの第2引数を `true` にする。

---

## スポーンさせるには

ゲーム内コマンド:
```
/summon sdm_mod:xxx
```

コードからスポーンさせる場合は `StoryPoliceOfficerService.java` を参考にする。
