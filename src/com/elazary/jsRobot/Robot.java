package com.elazary.jsRobot;

import java.lang.reflect.Field;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftZombie;
import org.bukkit.entity.Golem;
import org.bukkit.plugin.Plugin;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityLiving;
import net.minecraft.server.EntityGolem;
import net.minecraft.server.World;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;
import net.minecraft.server.PathfinderGoalArrowAttack;
import net.minecraft.server.PathfinderGoal;
import net.minecraft.server.NBTTagString;
import net.minecraft.server.NBTTagList;
import net.minecraft.server.ItemBookAndQuill;
import net.minecraft.server.ItemStack;
import net.minecraft.server.InventoryEnderChest;
import net.minecraft.server.TileEntity;
import net.minecraft.server.DamageSource;

// CraftBukkit start
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageEvent;
// CraftBukkit end

public class Robot extends net.minecraft.server.EntitySnowman {

  private PathfinderGoalNearestAttackableTarget targetToAttack;
  private RobotCmd robotCmd;
  private int noProgramTime;


  //@SuppressWarnings("unchecked")
  public Robot(World world) {
    super(world);
    //this.texture = "/mob/snowman.png";
    //this.a(0.4F, 1.8F);
    //this.getNavigation().a(true);
    //this.goalSelector.a(1, new PathfinderGoalArrowAttack(this, 0.25F, 1, 60));
    //targetToAttack =
    //  new PathfinderGoalNearestAttackableTarget(this, TileEntity.class, 16.0F, 0, true);
    //this.targetSelector.a(1, targetToAttack);

    this.noProgramTime =0;
    System.out.println("Starting jsRobot");

    try{
      Field goala = this.goalSelector.getClass().getDeclaredField("a");
      goala.setAccessible(true);
      ((List<PathfinderGoal>) goala.get(this.goalSelector)).clear();

      Field targeta = this.targetSelector.getClass().getDeclaredField("a");
      targeta.setAccessible(true);
      ((List<PathfinderGoal>) targeta.get(this.targetSelector)).clear();

      //this.goalSelector.a(1, new PathfinderGoalFloat(this));
      this.goalSelector.a(1, new PathfinderGoalArrowAttack(this, 0.25F, 1, 60));

      targetToAttack =
        new PathfinderGoalNearestAttackableTarget(this,
            TileEntity.class, 16.0F, 0, true);
      this.targetSelector.a(2, targetToAttack);
    }catch (Exception e){
      e.printStackTrace();
    }
  }

  @Override
  public boolean aV() {
    return true;
  }

  @Override
  public int getMaxHealth() {
    return 5;
  }
  
  @Override
    public void d() { //OnLiving Update
			//super.d();

      //System.out.println("On living update " + noProgramTime);
			if (robotCmd != null)
			{
				//Process any minecraft commands and 
				//If we are idleing for too long, then kill
				if(!robotCmd.processActions()) 
				{
          System.out.println("Did not process actions");
					EntityDamageEvent event = new EntityDamageEvent(this.getBukkitEntity(),
							EntityDamageEvent.DamageCause.MELTING, 1);
					this.world.getServer().getPluginManager().callEvent(event);

					if (!event.isCancelled()) {
						event.getEntity().setLastDamageCause(event);
						this.damageEntity(DamageSource.BURN, event.getDamage());
					}
				}
				noProgramTime = 0;
			} else {
				noProgramTime++;
			}

			//Did not program the robot in time, killing
			//This is to avoid having too many idle robots in the world
			if (noProgramTime > 2000)
			{
				EntityDamageEvent event = new EntityDamageEvent(this.getBukkitEntity(),
						EntityDamageEvent.DamageCause.MELTING, 1);
				this.world.getServer().getPluginManager().callEvent(event);

				if (!event.isCancelled()) {
					event.getEntity().setLastDamageCause(event);
					this.damageEntity(DamageSource.BURN, event.getDamage());
				}
			}
		}

  @Override
    public boolean c(EntityHuman par1EntityPlayer) //Interact with player
    {

	    ItemStack var2 = par1EntityPlayer.inventory.getItemInHand();

	    if (var2 != null && ItemBookAndQuill.a(var2.getTag()))
	    {
		    System.out.println("Book valid: Getting program");

				String funcLib = "";
				//Get any sub functions from the ender chest
				InventoryEnderChest chest = par1EntityPlayer.getEnderChest();
				if (chest != null)
				{
					ItemStack[] items = chest.getContents();
					System.out.println("Items " + items.length);
					for(int i=0; i<items.length; ++i)
					{
						ItemStack item = items[i];
						if (item != null)
						{
							if (ItemBookAndQuill.a(item.getTag()))
							{
								NBTTagList pages = (NBTTagList)item.getTag().getList("pages");
								boolean validLib = false;
								for (int j = 0; j < pages.size(); ++j)
								{
									NBTTagString page = (NBTTagString)pages.get(j);
									if (page.data != null)
									{
										String libPage = page.toString();
										if (!validLib)
										{
											//Check if this book is a library
											if (libPage.substring(0, Math.min(3, libPage.length())).equals("LIB"))
											{
											  validLib=true;
											  funcLib = funcLib + libPage.substring(3); //Remove the LIB
											}
										} else {
										  funcLib = funcLib + libPage;
										}
									}
								}
								if (validLib)
								{
									System.out.println("  Importing function from ender chest book ");
									System.out.println(funcLib);
								}

							}
						}

					}
				}
				

		    NBTTagList pages = (NBTTagList)var2.getTag().getList("pages");

		    String program = "";
		    for (int i = 0; i < pages.size(); ++i)
		    {
			    NBTTagString var3 = (NBTTagString)pages.get(i);

			    if (var3.data != null && var3.data.length() < 255)
			    {
				    program = program + var3.toString();
			    }
		    }
				program = funcLib + program;

        System.out.println(" Starting robot with " + program);
				robotCmd = new RobotCmd(this, program, par1EntityPlayer, targetToAttack);


        System.out.println(" init thread");
				//We run the parser in a seperate thread incase it goes haywire
				Thread th = new Thread(robotCmd, "robot");
        System.out.println(" start thread");
				th.start();
        System.out.println(" Done");
        return true;
	    }

	    //return super.c(par1EntityPlayer);
      return false;
    }


  @Override
    protected int getLootId() {
      return 1; //Item.SNOW_BALL.id;
    }

  @Override
    protected void dropDeathLoot(boolean flag, int i) {
      // CraftBukkit start
      //java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<org.bukkit.inventory.ItemStack>();
      //int j = this.random.nextInt(16);

      //if (j > 0) {
      //  loot.add(new org.bukkit.inventory.ItemStack(Item.SNOW_BALL.id, j));
      //}
    }

 // @Override
 //   protected Entity findTarget(){
 //     float distance = (plugin.isActive(this.world.worldData.getName()) && plugin.config.getStringList(Config.FEATURE_TARGET_DISTANCE_MOBS).contains("SKELETON")) ? plugin.config.getInt(Config.FEATURE_TARGET_DISTANCE_MULTIPLIER) * 16.0f : 16.0f;

 //     EntityHuman entityhuman = this.world.findNearbyVulnerablePlayer(this, distance);

 //     return entityhuman != null && this.l(entityhuman) ? entityhuman : null;
 //   }
  
}

