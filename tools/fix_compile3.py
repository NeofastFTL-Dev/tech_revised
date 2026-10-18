from pathlib import Path

root = Path("src/main/java")
for path in root.rglob("*.java"):
    if "net/minecraftforge" in str(path).replace("\\", "/"):
        if "items/SlotItemHandler" in str(path).replace("\\", "/"):
            continue
    text = path.read_text(encoding="utf-8")
    original = text
    text = text.replace("ItemStack.isSameItemSameTags(", "ItemStack.isSameItemSameComponents(")
    text = text.replace("buffer.readFluidStack()", "com.neofast.tech_revised.recipe.RecipeHelper.readFluidStack(buffer)")
    text = text.replace("buffer.writeFluidStack(", "com.neofast.tech_revised.recipe.RecipeHelper.writeFluidStack(buffer, ")
    text = text.replace("CreativeModeTab.builder()", "net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup.builder()")
    text = text.replace("new AlloyingRecipe(pRecipeId,", 'new AlloyingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new CokeOvenRecipe(recipeId,", 'new CokeOvenRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new MeltingRecipe(recipeId,", 'new MeltingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new DrillingPlatformRecipe(recipeId,", 'new DrillingPlatformRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new OxygenConverterRecipe(recipeId,", 'new OxygenConverterRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new ItemFluidToItemRecipe(recipeId,", 'new ItemFluidToItemRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new FluidToItemRecipe(recipeId,", 'new FluidToItemRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new ArgonOxygenDecarburizationConverterRecipe(recipeId,", 'new ArgonOxygenDecarburizationConverterRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new PcbRecipe(recipeId,", 'new PcbRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new LaminationRecipe(recipeId,", 'new LaminationRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new GenericIndustrialRecipe(recipeId,", 'new GenericIndustrialRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new CrusherRecipe(recipeId,", 'new CrusherRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new ElectricArcFurnaceRecipe(recipeId,", 'new ElectricArcFurnaceRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace("new BatchingRecipe(recipeId,", 'new BatchingRecipe(net.minecraft.resources.ResourceLocation.parse("tech_revised:recipe"),')
    text = text.replace(
        "stack.hasCraftingRemainingItem() ? stack.getCraftingRemainingItem() : ItemStack.EMPTY",
        "stack.getItem() instanceof net.minecraft.world.item.BucketItem ? new ItemStack(net.minecraft.world.item.Items.BUCKET) : ItemStack.EMPTY",
    )
    text = text.replace(".hasCraftingRemainingItem()", ".getItem() instanceof net.minecraft.world.item.BucketItem")
    text = text.replace(".getCraftingRemainingItem()", ".getItem() instanceof net.minecraft.world.item.BucketItem ? new ItemStack(net.minecraft.world.item.Items.BUCKET) : ItemStack.EMPTY")
    if text != original:
        path.write_text(text, encoding="utf-8")
        print("updated", path)
