package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.item.IItemRegistry.EnumUpgrade;
import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import pneumaticCraft.common.item.Itemss;

public class UserSetSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "Constant";
    }

    @Override
    public Set<Item> getRequiredUpgrades(){
        Set<Item> upgrades = new HashSet<Item>();
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.DISPENSER));
        return upgrades;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 1;
    }

    @Override
    public boolean needsTextBox(){
        return true;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add("gui.universalSensor.desc.userSetSensor");
        return text;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText){
        try {
            return Math.min(15, Math.max(0, Integer.parseInt(textBoxText)));
        } catch(Exception e) {
            return 0;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }

}