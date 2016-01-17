package pneumaticCraft.common.sensor.pollSensors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import org.lwjgl.util.Rectangle;

import pneumaticCraft.api.item.IItemRegistry.EnumUpgrade;
import pneumaticCraft.api.universalSensor.IPollSensorSetting;
import pneumaticCraft.common.item.Itemss;

public class PlayerHealthSensor implements IPollSensorSetting{

    @Override
    public String getSensorPath(){
        return "Player/Player Health";
    }

    @Override
    public Set<Item> getRequiredUpgrades(){
        Set<Item> upgrades = new HashSet<Item>();
        upgrades.add(Itemss.upgrades.get(EnumUpgrade.ENTITY_TRACKER));
        return upgrades;
    }

    @Override
    public boolean needsTextBox(){
        return true;
    }

    @Override
    public void drawAdditionalInfo(FontRenderer fontRenderer){

    }

    @Override
    public List<String> getDescription(){
        List<String> text = new ArrayList<String>();
        text.add("gui.universalSensor.desc.playerHealth");
        return text;
    }

    @Override
    public Rectangle needsSlot(){
        return null;
    }

    @Override
    public int getPollFrequency(TileEntity te){
        return 10;
    }

    @Override
    public int getRedstoneValue(World world, BlockPos pos, int sensorRange, String textBoxText){
        EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().getPlayerByUsername(textBoxText);
        if(player != null) {
            return (int)(15 * player.getHealth() / player.getMaxHealth());
        } else {
            return 0;
        }
    }

}
