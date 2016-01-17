package pneumaticCraft.common.tileentity;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import pneumaticCraft.api.PneumaticRegistry;
import pneumaticCraft.api.heat.IHeatExchangerLogic;
import pneumaticCraft.api.item.IItemRegistry.EnumUpgrade;
import pneumaticCraft.api.recipe.IThermopneumaticProcessingPlantRecipe;
import pneumaticCraft.api.tileentity.IHeatExchanger;
import pneumaticCraft.common.block.Blockss;
import pneumaticCraft.common.network.DescSynced;
import pneumaticCraft.common.network.GuiSynced;
import pneumaticCraft.common.recipes.PneumaticRecipeRegistry;
import pneumaticCraft.lib.PneumaticValues;

public class TileEntityThermopneumaticProcessingPlant extends TileEntityPneumaticBase implements IFluidHandler,
        IHeatExchanger, IMinWorkingPressure, IRedstoneControlled, IInventory{

    @GuiSynced
    @DescSynced
    private final FluidTank inputTank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    @DescSynced
    private final FluidTank outputTank = new FluidTank(PneumaticValues.NORMAL_TANK_CAPACITY);
    @GuiSynced
    private final IHeatExchangerLogic heatExchanger = PneumaticRegistry.getInstance().getHeatRegistry().getHeatExchangerLogic();
    @GuiSynced
    public int redstoneMode;
    @GuiSynced
    private int craftingProgress;
    @GuiSynced
    public boolean hasRecipe;
    @GuiSynced
    public float requiredPressure;
    @GuiSynced
    public double requiredTemperature;
    private final ItemStack[] inventory = new ItemStack[5];
    private static final int CRAFTING_TIME = 60;

    public TileEntityThermopneumaticProcessingPlant(){
        super(5, 7, 3000, 0, 1, 2, 3);
        heatExchanger.setThermalResistance(10);

    }

    @Override
    public boolean isConnectedTo(EnumFacing dir){
        return getRotation().getOpposite() != dir && dir != EnumFacing.UP;
    }

    @Override
    public void update(){
        super.update();
        if(!worldObj.isRemote) {
            IThermopneumaticProcessingPlantRecipe recipe = getValidRecipe();
            hasRecipe = recipe != null;
            if(hasRecipe) {
                requiredPressure = recipe.getRequiredPressure(inputTank.getFluid(), inventory[4]);
                requiredTemperature = recipe.getRequiredTemperature(inputTank.getFluid(), inventory[4]);
                if(redstoneAllows() && heatExchanger.getTemperature() >= requiredTemperature && getPressure() >= getMinWorkingPressure()) {
                    craftingProgress++;
                    if(craftingProgress >= CRAFTING_TIME) {
                        outputTank.fill(recipe.getRecipeOutput(inputTank.getFluid(), inventory[4]).copy(), true);
                        recipe.useRecipeItems(inputTank.getFluid(), inventory[4]);
                        addAir(-recipe.airUsed(inputTank.getFluid(), inventory[4]));
                        heatExchanger.addHeat(-recipe.heatUsed(inputTank.getFluid(), inventory[4]));
                        if(inputTank.getFluid() != null && inputTank.getFluid().amount <= 0) inputTank.setFluid(null);
                        if(inventory[4] != null && inventory[4].stackSize <= 0) inventory[4] = null;
                        craftingProgress = 0;
                    }
                }
            } else {
                craftingProgress = 0;
                requiredTemperature = 273;
                requiredPressure = 0;
            }
            if(getUpgrades(EnumUpgrade.DISPENSER) > 0) {
                autoExportLiquid();
            }
        }
    }

    private IThermopneumaticProcessingPlantRecipe getValidRecipe(){
        for(IThermopneumaticProcessingPlantRecipe recipe : PneumaticRecipeRegistry.getInstance().thermopneumaticProcessingPlantRecipes) {
            if(recipe.isValidRecipe(inputTank.getFluid(), inventory[4])) {
                if(outputTank.getFluid() == null) {
                    return recipe;
                } else {
                    FluidStack output = recipe.getRecipeOutput(inputTank.getFluid(), inventory[4]);
                    if(output.getFluid() == outputTank.getFluid().getFluid() && output.amount <= outputTank.getCapacity() - outputTank.getFluidAmount()) {
                        return recipe;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill){
        return inputTank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain){
        return outputTank.getFluid() != null && outputTank.getFluid().isFluidEqual(resource) ? outputTank.drain(resource.amount, doDrain) : null;
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain){
        return outputTank.drain(maxDrain, doDrain);
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid){
        return true;
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid){
        return true;
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from){
        return new FluidTankInfo[]{new FluidTankInfo(inputTank), new FluidTankInfo(outputTank)};
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getInputTank(){
        return inputTank;
    }

    @SideOnly(Side.CLIENT)
    public FluidTank getOutputTank(){
        return outputTank;
    }

    @SideOnly(Side.CLIENT)
    public double getCraftingPercentage(){
        return (double)craftingProgress / CRAFTING_TIME;
    }

    @Override
    public void writeToNBT(NBTTagCompound tag){
        super.writeToNBT(tag);
        writeInventoryToNBT(tag, inventory);
        tag.setByte("redstoneMode", (byte)redstoneMode);
        tag.setInteger("craftingProgress", craftingProgress);

        NBTTagCompound tankTag = new NBTTagCompound();
        inputTank.writeToNBT(tankTag);
        tag.setTag("inputTank", tankTag);

        tankTag = new NBTTagCompound();
        outputTank.writeToNBT(tankTag);
        tag.setTag("outputTank", tankTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound tag){
        super.readFromNBT(tag);
        readInventoryFromNBT(tag, inventory);
        redstoneMode = tag.getByte("redstoneMode");
        craftingProgress = tag.getInteger("craftingProgress");
        inputTank.readFromNBT(tag.getCompoundTag("inputTank"));
        outputTank.readFromNBT(tag.getCompoundTag("outputTank"));
    }

    @Override
    public IHeatExchangerLogic getHeatExchangerLogic(EnumFacing side){
        return heatExchanger;
    }

    @Override
    public void handleGUIButtonPress(int buttonID, EntityPlayer player){
        if(buttonID == 0) {
            redstoneMode++;
            if(redstoneMode > 2) redstoneMode = 0;
        }
    }

    @Override
    public int getRedstoneMode(){
        return redstoneMode;
    }

    @Override
    public float getMinWorkingPressure(){
        return requiredPressure;
    }

    /**
     * Returns the name of the inventory.
     */
    @Override
    public String getName(){
        return Blockss.thermopneumaticProcessingPlant.getUnlocalizedName();
    }

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory(){
        return inventory.length;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int par1){
        return inventory[par1];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            if(itemStack.stackSize <= amount) {
                setInventorySlotContents(slot, null);
            } else {
                itemStack = itemStack.splitStack(amount);
                if(itemStack.stackSize == 0) {
                    setInventorySlotContents(slot, null);
                }
            }
        }
        return itemStack;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot){
        ItemStack itemStack = getStackInSlot(slot);
        if(itemStack != null) {
            setInventorySlotContents(slot, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack itemStack){
        inventory[slot] = itemStack;
        if(itemStack != null && itemStack.stackSize > getInventoryStackLimit()) {
            itemStack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public boolean isItemValidForSlot(int par1, ItemStack stack){
        return par1 == 4 || canInsertUpgrade(par1, stack);
    }

    @Override
    public boolean hasCustomName(){
        return false;
    }

    @Override
    public int getInventoryStackLimit(){
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer p_70300_1_){
        return isGuiUseableByPlayer(p_70300_1_);
    }

    @Override
    public void openInventory(EntityPlayer player){}

    @Override
    public void closeInventory(EntityPlayer player){}

    @Override
    public void clear(){
        Arrays.fill(inventory, null);
    }
}
