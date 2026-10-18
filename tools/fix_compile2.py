from pathlib import Path
import re

ROOT = Path("src/main/java")

# Base entity blocks
for path in (ROOT / "com/neofast/tech_revised/block/custom").glob("*.java"):
    if path.name == "ModBaseEntityBlock.java":
        continue
    text = path.read_text(encoding="utf-8")
    if "extends BaseEntityBlock" in text:
        text = text.replace("extends BaseEntityBlock", "extends ModBaseEntityBlock")
        path.write_text(text, encoding="utf-8")
        print("base", path.name)

# ItemInteractionResult methods that should still return InteractionResult
for path in (ROOT / "com/neofast/tech_revised/block/custom").glob("*.java"):
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace(
        "private static InteractionResult tryAutoBuildWithConfigurator",
        "private static net.minecraft.world.ItemInteractionResult tryAutoBuildWithConfigurator",
    )
    if text != original:
        path.write_text(text, encoding="utf-8")
        print("autobuild", path.name)

# Advancements
for path in ROOT.rglob("*.java"):
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace(
        "player.getServer().getAdvancements()\n                .getAdvancement(",
        "player.getServer().getAdvancements().get(",
    )
    text = text.replace("Advancement advancement =", "net.minecraft.advancements.AdvancementHolder advancement =")
    text = text.replace("import net.minecraft.advancements.Advancement;", "import net.minecraft.advancements.AdvancementHolder;")
    if text != original:
        path.write_text(text, encoding="utf-8")
        print("adv", path)

# Recipe leftovers
for path in (ROOT / "com/neofast/tech_revised/recipe").glob("*.java"):
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace("matches(Container pContainer,", "matches(ContainerInput pContainer,")
    text = text.replace("assemble(Container pContainer, RegistryAccess", "assemble(ContainerInput pContainer, net.minecraft.core.HolderLookup.Provider")
    text = text.replace("pContainer.getContainerSize()", "pContainer.size()")
    text = text.replace("fromJson(ResourceLocation pRecipeId, JsonObject", "fromJson(JsonObject")
    text = text.replace("fromNetwork(ResourceLocation pRecipeId, FriendlyByteBuf", "fromNetwork(FriendlyByteBuf")
    text = text.replace("new AlloyingRecipe(pRecipeId,", 'new AlloyingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("pBuffer.readItem()", "RecipeHelper.readItem(pBuffer)")
    text = text.replace("pBuffer.writeItem(", "RecipeHelper.writeItem(pBuffer, ")
    text = text.replace("ingredient.toNetwork(pBuffer)", "RecipeHelper.ingredientToNetwork(ingredient, pBuffer)")
    if text != original:
        path.write_text(text, encoding="utf-8")
        print("recipe", path.name)

# Smelting lookups must use SingleRecipeInput
for path in ROOT.rglob("*BlockEntity.java"):
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace(
        "getRecipeFor(RecipeType.SMELTING, new com.neofast.tech_revised.recipe.ContainerInput(inventory), level).map(net.minecraft.world.item.crafting.RecipeHolder::value)",
        "getRecipeFor(RecipeType.SMELTING, new net.minecraft.world.item.crafting.SingleRecipeInput(inventory.getItem(0)), level).map(net.minecraft.world.item.crafting.RecipeHolder::value)",
    )
    if text != original:
        path.write_text(text, encoding="utf-8")
        print("smelt", path.name)
