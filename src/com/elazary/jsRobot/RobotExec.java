package com.elazary.jsRobot;

// CraftBukkit start
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.craftbukkit.block.CraftBlockState; // CraftBukkit
import org.bukkit.craftbukkit.entity.CraftLivingEntity;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityAnimal;
import net.minecraft.server.EntityMonster;
import net.minecraft.server.EntityCreature;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;

import org.mozilla.javascript.*;
import java.io.*;

public class RobotExec extends ScriptableObject
{
  private EntityCreature entity;
  private EntityHuman player;
  private PathfinderGoalNearestAttackableTarget targetToAttack;
  boolean doAction = false; 
  boolean debug = false;
   
  RobotExec(EntityCreature entity, EntityHuman player,
      PathfinderGoalNearestAttackableTarget targetToAttack)
  {
    this.targetToAttack = targetToAttack;
    this.entity = entity;
    this.player = player;
  }

  @Override
    public String getClassName()
    {
      return "robot";
    }


	public synchronized boolean processActions()
	{
		boolean actionProcessed = false;

		if (doAction)
		{
			actionProcessed = true;
      if (debug)
				System.out.println("Proforming action: notifying");
			doAction = false;
			notify(); //Tell the robot its ok to do the action

      if (debug)
				System.out.println("Wait for action to complete");
			//Wait for the action to complete
			try {
				wait();
			} catch(InterruptedException e) {
				System.out.println("InterruptedException caught");
			}
      if (debug)
				System.out.println("Action completed");

			//Do we need this?, yield to other actions?
			try
			{
				Thread.sleep(50);  
			}catch (InterruptedException ie)
			{
				System.out.println(ie.getMessage());
			}
		}


		return actionProcessed;

	}

	public synchronized void print(String msg)
	{

		if (debug)
			System.out.println("Requesting to print: wait for action");
		doAction = true;
		//Wait untill its ok to do the action
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}

		if (debug)
			System.out.println("print Can send msg to client");
    player.c(msg);

		if (debug)
			System.out.println("print Notify that the action is done");
		notify(); //action is done
		if (debug)
			System.out.println("Print Action done");
	}

	public synchronized void giveExp(int exp)
	{

		doAction = true;
		//Wait untill its ok to do the action
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}

		if (debug)
			System.out.println("print Can send msg to client");
    player.giveExp(exp);

		if (debug)
			System.out.println("print Notify that the action is done");
		notify(); //action is done
		if (debug)
			System.out.println("Print Action done");
	}

	public synchronized int getExpLevel()
	{

		doAction = true;
		//Wait untill its ok to do the action
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}

		if (debug)
			System.out.println("print Can send msg to client");
    int exp = player.expLevel;

		if (debug)
			System.out.println("print Notify that the action is done");
		notify(); //action is done
		if (debug)
			System.out.println("Print Action done");

		return exp;
	}



	public synchronized void move(int x, int y, int z, boolean goThrough, boolean absolute )
	{

		doAction = true;
		//Wait untill its ok to do the action
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}

		if (debug)
			System.out.println("Trying moving Robot to " + x + "," + y + "," + z); 
		int dx = x;
		int dy = y;
		int dz = z;

		if (!absolute)
		{
			dx = dx + (int)Math.floor(entity.locX);
			dy = dy + (int)Math.floor(entity.locY);
			dz = dz + (int)Math.floor(entity.locZ);
		}

		if (debug)
			System.out.println("Trying ABS moving Robot to " + dx + "," + dy + "," + dz); 
		if (!goThrough)
		{
			int blockId = entity.world.getTypeId(dx,dy, dz);
			if (debug)
				System.out.println("Block ID " + blockId);
			if (blockId == 0) // || !Block.byId[blockId].material.isSolid())
				entity.setPosition(dx+0.5,dy,dz+0.5);

		} else {
			entity.setPosition(dx+0.5,dy,dz+0.5);
		}
		notify(); //action is done
	}

  

	public synchronized void rotate(int yaw, int pitch)
	{

		doAction = true;
		//Wait untill its ok to do the action
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}

		if (debug)
			System.out.println("Rotating robot to " + yaw + " " + pitch);
		//double dx = entity.locX;
		//double dy = entity.locY;
		//double dz = entity.locZ;

		//entity.setPositionRotation(dx, dy, dz, yaw, pitch); //add 0.5 to move the robot so it will rotate
		//entity.setPosition(dx-0.5,dy,dz);
		//entity.setPosition(dx+0.5,dy,dz);

		entity.b(yaw, pitch); 
		notify(); //action is done
	}

	public synchronized void giveItem(int item, int quantity, boolean allowBlock)
	{

		if (quantity <= 0)
			quantity = 1;
		if (quantity > 16) quantity = 16;

		//Invalid items
    if (item < 256 && !allowBlock) return;
		if (item > 388 && item < 2256) return;
		if (item > 2266) return;

		doAction = true;
		//Wait untill its ok to do the action
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}

		try {
			CraftLivingEntity craftEntity = (CraftLivingEntity) entity.getBukkitEntity();
			org.bukkit.World world = craftEntity.getWorld();
			org.bukkit.inventory.ItemStack itemStack = new org.bukkit.inventory.ItemStack(item, quantity);
			world.dropItemNaturally(craftEntity.getLocation(), itemStack );
		} catch (Exception e)
    {
			System.out.println("Can not give item " + item);
			notify(); //action is done	
		}

		notify(); //action is done
	}


  /*
  public synchronized void tryMove(int x, int y, int z, double speed, boolean absolute)
  {

    doAction = true;
    //Wait untill its ok to do the action
    try {
      wait();
    } catch(InterruptedException e) {
      System.out.println("InterruptedException caught");
    }

    if (speed > 0)
    {
      System.out.println("Trying moving Robot to " + x + "," + y + "," + z + "Speed: " + speed);


      double dx = x;
      double dy = y;
      double dz = z;

      if (!absolute)
      {
        dx = dx + MathHelper.floor_double(entity.locX) + 0.5;
        dy = dy + MathHelper.floor_double(entity.locY);
        dz = dz + MathHelper.floor_double(entity.locZ) + 0.5;
      }
      entity.getNavigator().tryMoveToXYZ(dx, dy, dz, (float)speed);
    }

    notify(); //action is done

  }
	*/

	public void sleep(int ms)
	{
		try
		{
			Thread.sleep(ms);  
		}catch (InterruptedException ie)
		{
			System.out.println(ie.getMessage());
		}
	}

	public void setDebug(boolean d)
	{
		this.debug = d;
	}

	public synchronized double getPosX()
	{
		doAction = true;
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		}
		double pos = entity.locX;
		notify();

		return pos;
	}

	public synchronized double getPosY()
	{
		doAction = true;
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		} 

		double pos = entity.locY;
		notify();

		return pos;
	}

	public synchronized double getPosZ()
	{
		doAction = true;
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		} 
		double pos =  entity.locZ;
		notify();

		return pos;
	}
 
  public synchronized boolean noPath()
  {
    doAction = true;
    try {
      wait();
    } catch(InterruptedException e) {
      System.out.println("InterruptedException caught");
    }

    boolean ret = true; // = entity.getNavigator().noPath(); //TODO
    notify();
    return ret;
  }
 
	public synchronized boolean setBlock(int block, int x, int y, int z, boolean absolute)
	{
		if (block < 0)
			return false;

		//Dont give protection ores
		if (block == 16 ||
			  block == 21 ||
				block == 56)
			return false;

		//Only allow setting the blocks around the robot
		if (!absolute)
		{
			if (x > 1 || x < -1 ||
					y > 1 || y < -1 ||
					z > 1 || z < -1 
				 )
				return false;
		}


		doAction = true;
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		} 

		int dx = x;
		int dy = y;
		int dz = z;

		if (!absolute)
		{
			dx = dx + (int)Math.floor(entity.locX);
			dy = dy + (int)Math.floor(entity.locY);
			dz = dz + (int)Math.floor(entity.locZ);
		}

		CraftBlockState blockState = CraftBlockState.getBlockState(entity.world, dx, dy, dz); // CraftBukkit

		boolean ret = entity.world.setTypeId(dx, dy, dz, block);
		// CraftBukkit start - Hoes - blockface -1 for 'SELF'
		org.bukkit.event.block.BlockPlaceEvent event =
			org.bukkit.craftbukkit.event.CraftEventFactory.callBlockPlaceEvent(entity.world, player, blockState, dx, dy, dz);

		if (event.isCancelled() || !event.canBuild()) {
			event.getBlockPlaced().setTypeId(blockState.getTypeId()); //Revert to old block state
			ret = false;
		}
	
		if (debug)
			System.out.println("Set block " + block + " at: " + dx + "," + dy + "," + dz);
		//boolean ret = entity.world.setTypeId(dx, dy, dz, block);
		notify();

		return ret;
	}

	public synchronized int getBlock(int x, int y, int z, boolean absolute)
	{
		//Only allow setting the blocks around the robot
		if (!absolute)
		{
			if (x > 1 || x < -1 ||
					y > 1 || y < -1 ||
					z > 1 || z < -1 
				 )
				return -1;
		}


		doAction = true;
		try {
			wait();
		} catch(InterruptedException e) {
			System.out.println("InterruptedException caught");
		} 

		int dx = x;
		int dy = y;
		int dz = z;

		if (!absolute)
		{
			dx = dx + (int)Math.floor(entity.locX);
			dy = dy + (int)Math.floor(entity.locY);
			dz = dz + (int)Math.floor(entity.locZ);
		}

		int id = entity.world.getTypeId(dx,dy,dz);
    if (debug)
			System.out.println("Block " + id + " at: " + dx + "," + dy + "," + dz);
		notify();

		return id;
	}

  public synchronized void attack(String target)
  {
    doAction = true;
    try {
      wait();
    } catch(InterruptedException e) {
      System.out.println("InterruptedException caught");
    }

   // if (target.startsWith("monster"))
   //   targetToAttack.b = EntityMonster.class;
   // else if (target.startsWith("player"))
   // {
   //   System.out.println("Attacking players");
   //   targetToAttack.b = EntityHuman.class;
   // } 
   // else if (target.startsWith("animal"))
   // {
   //   System.out.println("Attacking Animal");
   //   targetToAttack.b = EntityAnimal.class;
   // } 
    

    notify();

  }
	
}
    
