# AzureLib Port to Minecraft 1.21.8 - Current Status

## ✅ Configuration Complete

All configuration files have been successfully updated to target Minecraft 1.21.8:

### Updated Files
- ✅ `gradle.properties` - All version numbers updated
- ✅ `gradle/wrapper/gradle-wrapper.properties` - Gradle 8.8 → 8.12
- ✅ `common/build.gradle` - Dependencies updated
- ✅ `neo/build.gradle` - Dependencies updated
- ✅ `fabric/build.gradle` - Dependencies updated
- ✅ `changelog.md` - v3.2.0 release notes added
- ✅ `common/src/main/resources/azurelib.accesswidener` - Incompatible entries commented out

### Version Numbers (Verified from Maven)
```properties
minecraft_version = 1.21.8
neo_version = 21.8.52  # Latest from NeoForge Maven
fabric_version = 0.136.1+1.21.8  # Latest from Fabric Maven
fabric_loom_version = 1.10-SNAPSHOT
gradle = 8.12
```

## ⚠️ Code Changes Required

The build now fails due to **API changes in Minecraft 1.21.8**. These require code modifications:

### 1. Access Widener Changes
**File:** `common/src/main/resources/azurelib.accesswidener`

The following entries were commented out (removed/changed in 1.21.8):
- `Entity.getLeashOffset()` - Method removed
- `AgeableListModel` fields (scaleHead, babyYHeadOffset, etc.) - Fields removed
- `PlayerModel.ear` - Field removed  
- `HumanoidArmorLayer.renderModel()` - Signature changed
- `RenderStateShard` inner classes - Restructured
- `RenderType.create()` methods - Signature changed

### 2. RenderSystem API Changes
**Files affected:** Multiple rendering classes

```java
// OLD (1.21.1)
RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
RenderSystem.enableBlend();
RenderSystem.enableDepthTest();

// NEW (1.21.8)
// These methods have been removed or changed
// Need to find replacement APIs
```

### 3. PoseStack API Changes
**Files affected:** GUI and rendering classes

```java
// OLD (1.21.1)
graphics.pose().pushPose();

// NEW (1.21.8)
// Method signature or return type changed
// Matrix3x2fStack doesn't have pushPose()
```

### 4. PlayerModel Generic Type Removed
**Files affected:** `AzItemArmRenderUtil.java`

```java
// OLD (1.21.1)
PlayerModel<?> playerEntityModel

// NEW (1.21.8)
PlayerModel playerEntityModel  // No longer generic
```

### 5. AreaEffectCloud API Changes
**File:** `CommonUtils.java`

```java
// OLD (1.21.1)
areaEffectCloudEntity.setParticle(particle);

// NEW (1.21.8)
// Method removed or renamed
```

### 6. Shoulder Surfing Compatibility
**File:** `ShoulderSurfingCompat.java`

- Temporarily disabled (dependency not available for 1.21.8)
- Returns default alpha value (1.0F)
- Can be re-enabled when Shoulder Surfing updates

## 📊 Build Errors Summary

Total compilation errors: **100+**

### Error Categories:
1. **RenderSystem methods removed** (~20 errors)
2. **PoseStack API changes** (~15 errors)
3. **PlayerModel generic type** (~10 errors)
4. **Access widener validation** (resolved)
5. **Entity/Model API changes** (~10 errors)
6. **Other API changes** (~45+ errors)

## 🔧 What Needs to Be Done

### High Priority (Blocking Build)
1. **Update RenderSystem calls** - Find replacement for removed methods
2. **Fix PoseStack usage** - Adapt to new Matrix stack API
3. **Remove PlayerModel generics** - Update all usages
4. **Fix AreaEffectCloud** - Find replacement for setParticle()
5. **Review all rendering code** - Many render-related APIs changed

### Medium Priority (Features)
6. **Update access widener** - Find new method signatures for commented entries
7. **Test armor rendering** - HumanoidArmorLayer changes may affect armor
8. **Test entity rendering** - Entity-related changes need verification

### Low Priority (Optional)
9. **Re-enable Shoulder Surfing** - When dependency becomes available
10. **Optimize for 1.21.8** - Take advantage of new APIs

## 📝 Recommended Approach

### Option 1: Complete the Port (Recommended)
1. Search Minecraft 1.21.8 source/mappings for replacement APIs
2. Update all affected code files (~50-100 files estimated)
3. Test thoroughly
4. Update documentation

**Estimated effort:** 8-16 hours of development work

### Option 2: Wait for Community
1. Monitor other mods porting to 1.21.8
2. See how they handle the API changes
3. Apply similar solutions

**Estimated wait:** Days to weeks

### Option 3: Incremental Port
1. Fix critical errors first (RenderSystem, PoseStack)
2. Get a minimal build working
3. Fix remaining issues incrementally

## 🔍 Useful Resources

- **NeoForge 1.21.8 Changelog**: https://neoforged.net/news/
- **Fabric API Changes**: Check Fabric GitHub for 1.21.8 changes
- **Minecraft Mappings**: Use Parchment/MojMap to find new method names
- **Other Mods**: Check how JEI, Sodium, etc. handled the port

## 📦 Files Modified So Far

### Configuration (Complete)
- `gradle.properties`
- `gradle/wrapper/gradle-wrapper.properties`
- `common/build.gradle`
- `neo/build.gradle`
- `fabric/build.gradle`
- `changelog.md`

### Code (Partial)
- `common/src/main/resources/azurelib.accesswidener` - Commented out incompatible entries
- `common/src/main/java/mod/azure/azurelib/common/render/armor/compat/ShoulderSurfingCompat.java` - Disabled Shoulder Surfing integration

### Code (Needs Modification - Not Started)
- ~50-100 Java files with API incompatibilities

## 💡 Next Steps

1. **Decide on approach** (complete port vs wait vs incremental)
2. **If porting**: Start with RenderSystem and PoseStack fixes
3. **Create mapping document** of old API → new API
4. **Apply fixes systematically**
5. **Test after each major fix**

---

**Status**: Configuration ✅ | Code ⚠️ (Needs Work)  
**Build**: ❌ Failing (100+ compilation errors)  
**Estimated Completion**: 8-16 hours of focused development  
**Date**: January 17, 2026
