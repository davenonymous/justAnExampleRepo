package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.item.IItemRegistry.EnumUpgrade;
import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import pneumaticCraft.common.item.Itemss;

public class WorldTicktimeSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "World/Tick time (lag)";
    }

    @Override
    public Set<Item> getRequiredUpgrades(){
        Set<Item> upgrades = new HashSet<Item>();
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.DISPENSER));
        return upgrades;
    }

    @Override
    public boolean needsTextBox(){
        return true;
    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add(EnumChatFormatting.BLACK + "Emits a redstone level dependant on the time used by the server to update the world this Universal Sensor is in. This time is calculated in the same way as Forge's /tps command. With the textbox you can select a resolution as follows:");
        text.add(EnumChatFormatting.RED + "Strength = Ticktime(mS) * TextboxValue");
        text.add(EnumChatFormatting.GREEN + "Example:  Ticktime = 20mS ; Textbox text = '0.5'");
        text.add(EnumChatFormatting.GREEN + "Strength = 20 * 0.5 = 10");
        return text;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 40;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText){
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        double worldTickTime = mean(server.worldTickTimes.get(world.provider.getDimensionId())) * 1.0E-6D;
        try {
            int redstoneStrength = (int)(worldTickTime * Double.parseDouble(textBoxText));
            return Math.min(15, redstoneStrength);
        } catch(Exception e) {
            return 0;
        }
    }

    private static long mean(long[] values){
        long sum = 0l;
        for(long v : values) {
            sum += v;
        }

        return sum / values.length;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawAdditionalInfo(FontRenderer fontRenderer){}

    @Override
    public Rectangle needsSlot(){
        return null;
    }
}
