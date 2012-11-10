package com.elazary.jsRobot;

import java.util.*;
import java.util.logging.Logger;
import java.lang.reflect.Method;

import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.GameMode;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.EventPriority;
import org.bukkit.craftbukkit.entity.CraftEntity;
import net.minecraft.server.EntitySnowman;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class jsRobot extends JavaPlugin implements Listener {

  private int currentQuestion = 0;

  private Player currentPlayer;

  public void onEnable(){
    this.saveDefaultConfig();
    getServer().getPluginManager().registerEvents(this, this);

    try{
      Class<?>[] args = new Class[3];
      args[0] = Class.class;
      args[1] = String.class;
      args[2] = int.class;

      Method a = net.minecraft.server.EntityTypes.class.getDeclaredMethod("a", args);
      a.setAccessible(true);

      a.invoke(a, Robot.class, "SnowMan", 97);
    }catch (Exception e){
      e.printStackTrace();
      this.setEnabled(false);
    }

    //Remove any lingering snowmans (should we replace with robots?)
    //These are spawned after a server restart
    for(World world : getServer().getWorlds())
    {
      for(Entity entity : world.getEntitiesByClass(LivingEntity.class))
      {
        EntityType creatureType = entity.getType();
        if (creatureType == EntityType.SNOWMAN)
        {
          net.minecraft.server.Entity mcEntity = ((CraftEntity) entity).getHandle();
          net.minecraft.server.World mcWorld = ((CraftWorld) world).getHandle();
          mcWorld.removeEntity(mcEntity); //Remove the minecraft entity
          getLogger().info("Remove " + entity + " from " + world);
        }
      }
    }
    
  }

  //Replace any snowman that a spawned by the system (like after a restart)
  @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
  public void onCreatureSpawn(CreatureSpawnEvent event)
  {
    Location location = event.getLocation();
    Entity entity = event.getEntity();
    EntityType creatureType = event.getEntityType();
    World world = location.getWorld();

    net.minecraft.server.World mcWorld = ((CraftWorld) world).getHandle();
    net.minecraft.server.Entity mcEntity = ((CraftEntity) entity).getHandle();
    getLogger().info("Spawn creature event " + creatureType + " " + EntityType.SNOWMAN);

    if (creatureType == EntityType.SNOWMAN &&
        !(mcEntity instanceof Robot)) //This is not a robot
    { 
      getLogger().info("Replace snowman");
      mcWorld.removeEntity(mcEntity); //Remove the minecraft entity

      //Add our robot entity
      Robot robot = new Robot(mcWorld);
      robot.setPosition(location.getX(), location.getY(), location.getZ());
      mcWorld.addEntity(robot, SpawnReason.CUSTOM);
      return;
    }
  }


  public void onDisable(){
  }

  public boolean onCommand(CommandSender sender, Command cmd,
      String label, String[] args){
    getLogger().info("On command " + cmd.getName());
    Player p = (Player)sender;


    Location location = p.getTargetBlock(null, 3).getLocation();
    Location location2 = p.getLocation();
    World world = location.getWorld();

    getLogger().info("Eye Location " + location.getX() + "," + location.getY() + "," + location.getZ());
    getLogger().info("Location " + location2.getX() + "," + location2.getY() + "," + location2.getZ());

    net.minecraft.server.World mcWorld = ((CraftWorld) world).getHandle();

    Robot robot = new Robot(mcWorld);
    robot.setPosition(location.getX(), location.getY()+2, location.getZ());
    mcWorld.addEntity(robot, SpawnReason.CUSTOM);

    return true;
  }

}
