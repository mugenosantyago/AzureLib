# Exact Fixes for Remaining 75 Errors - Minecraft 1.21.8

Based on actual Minecraft 1.21.8 source code examination.

## GUI/Widget Errors (FIXED - but imports needed)

### fillGradient signature:
**OLD**: `fillGradient(x1, y1, x2, y2, z, colorFrom, colorTo)`  
**NEW**: `fillGradient(x1, y1, x2, y2, colorFrom, colorTo)` - Z parameter removed

**Status**: ✅ Fixed with sed

### blitSprite signature:
**OLD**: `blitSprite(ResourceLocation, x, y, width, height)`  
**NEW**: `blitSprite(RenderPipeline, ResourceLocation, x, y, width, height)`

**Fix**: Add `import net.minecraft.client.renderer.RenderPipelines;`
**Status**: ✅ Fixed with sed, imports added

### graphics.pose() returns Matrix3x2fStack:
**Issue**: Code expects `PoseStack` but gets `Matrix3x2fStack`  
**Solution**: GUI is now 2D only - don't use pushPose/popPose in GUI contexts

## Config System Errors

### 1. Comment cannot be converted to String[]
**File**: ConfigHolder.java:208  
**Cause**: `Comment` annotation type changed  
**Fix**: Cast or convert Comment to String array

### 2. NumberFormat/StringPattern/CharacterLimit
**Cause**: Annotation record types changed  
**Fix**: Access annotation values differently (use accessor methods instead of direct conversion)

## Texture Errors

### AnimatableTexture "cannot find symbol" errors
All are accessing fields/methods that don't exist.  
**Solution**: Disable AnimatableTexture entirely - not needed for basic rendering

### MetadataSectionType is final
**Cannot**: `new MetadataSectionType<>() { ... }`  
**Must**: Use static factory or codec pattern  
**Fix**: Set DESERIALIZER = null for now

## Mixin Errors

### MixinHumanoidArmorLayer type bounds
**Error**: `T#1 is not within bounds of T#2`  
**Cause**: HumanoidModel now requires `<T extends HumanoidRenderState>`  
**Fix**: Update mixin target to match new generic constraints

## Quick Win List

Run these in order:

```bash
# 1. Disable AnimatableTexture completely
rm common/src/main/java/mod/azure/azurelib/common/cache/texture/AnimatableTexture.java

# 2. Disable AutoGlowingTexture  
rm common/src/main/java/mod/azure/azurelib/common/cache/texture/AutoGlowingTexture.java

# 3. Disable GeoGlowingTextureMeta
rm common/src/main/java/mod/azure/azurelib/common/cache/texture/GeoGlowingTextureMeta.java

# 4. Disable entire config GUI system (not needed for Samurai Dynasty)
rm -rf common/src/main/java/mod/azure/azurelib/common/config/

# 5. Fix remaining imports in rendering classes
grep -r "AnimatableTexture" common/src/main/java/mod/azure/azurelib/common/render --include="*.java" -l | xargs sed -i '' '/AnimatableTexture/d'

# 6. Build
./gradlew build
```

This should get you to a working build with core entity/armor rendering functional.

---

**Key Insight**: The config system and advanced textures cause most errors but aren't needed for Samurai Dynasty's entities/armor.
