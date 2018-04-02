package speeddev.info.PrisonMines;

import java.io.File;
import java.util.HashMap;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class PrisonMines extends JavaPlugin
implements Listener
{
	
	String prefix = "§9[PrisonMines] ";
HashMap<Block, Material> block = new HashMap<Block, Material>();
HashMap<Block, Integer> timer = new HashMap<Block, Integer>();

public void onEnable()
{
  PluginManager pm = getServer().getPluginManager();
  pm.registerEvents(this, this);
  File config = new File(getDataFolder() + File.separator + "config.yml");
  if (!config.exists())
  {
    getLogger().info("Generating config.yml");
    getConfig().addDefault("COAL_ORE", Integer.valueOf(10));
    getConfig().addDefault("IRON_ORE", Integer.valueOf(10));
    getConfig().addDefault("GOLD_ORE", Integer.valueOf(10));
    getConfig().addDefault("DIAMOND_ORE", Integer.valueOf(10));
    getConfig().addDefault("EMERALD_ORE", Integer.valueOf(10));
    getConfig().addDefault("LAPIS_ORE", Integer.valueOf(10));
    getConfig().addDefault("GLOWING_REDSTONE_ORE", Integer.valueOf(10));
    getConfig().options().copyDefaults(true);
    saveConfig();
    
    getConfig().options().copyDefaults(true);
    saveConfig();
  }
  BukkitRunnable task = new BukkitRunnable()
  {
    public void run()
    {
      for (Block b : PrisonMines.this.timer.keySet())
      {
        int newtime = ((Integer)PrisonMines.this.timer.get(b)).intValue() + 1;
        PrisonMines.this.timer.put(b, Integer.valueOf(newtime));
        if (newtime >= PrisonMines.this.getTime(b)) {
          if (b.getChunk().isLoaded())
          {
            b.setType((Material)PrisonMines.this.block.get(b));
            PrisonMines.this.block.remove(b);
            PrisonMines.this.timer.remove(b);
          }
        }
      }
    }
  };
  task.runTaskTimer(this, 0L, 20L);
}

public void onDisable()
{
  for (Block b : this.block.keySet()) {
    b.setType((Material)this.block.get(b));
  }
}

public int getTime(Block b)
{
  int time = getConfig().getInt(((Material)this.block.get(b)).toString());
  return time;
}

@EventHandler(ignoreCancelled=false)
public void onMine(final BlockBreakEvent event)
{
  if (event.getPlayer().getGameMode() == GameMode.CREATIVE) {
    return;
  }
  if (event.isCancelled()) {
    return;
  }
  if (!getConfig().contains(event.getBlock().getType().toString())) {
    return;
  }
  this.block.put(event.getBlock(), event.getBlock().getType());
  this.timer.put(event.getBlock(), Integer.valueOf(0));
  getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable()
  {
    public void run()
    {
      if (event.isCancelled()) {
        return;
      }
      event.getBlock().setType(Material.BEDROCK);
    }
  }, 2L);
}

@EventHandler
public void onClick(PlayerInteractEvent event)
{
  if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
    return;
  }
  Block block = event.getClickedBlock();
  if (block.getType() != Material.BEDROCK) {
    return;
  }
  if (!this.timer.containsKey(block)) {
    return;
  }
  int left = getTime(block) - ((Integer)this.timer.get(block)).intValue();
  event.getPlayer().sendMessage(prefix + ChatColor.RED + "This block will respawn in " + left + " seconds!");
}

}
