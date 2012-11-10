package com.elazary.jsRobot;

// CraftBukkit start
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import net.minecraft.server.Entity;
import net.minecraft.server.EntityHuman;
import net.minecraft.server.EntityAnimal;
import net.minecraft.server.EntityCreature;
import net.minecraft.server.PathfinderGoalNearestAttackableTarget;

import org.mozilla.javascript.*;
import java.io.*;
import java.util.regex.*;

class RobotCmd implements Runnable {

  private String program;
  private RobotExec robot;
  private EntityHuman player;
  private EntityCreature entity;
  private PathfinderGoalNearestAttackableTarget targetToAttack;
  private int idleTime; 
  

	//Thread runner;
	public RobotCmd() {
	}

	public RobotCmd(EntityCreature entity, String program, EntityHuman player,
      PathfinderGoalNearestAttackableTarget targetToAttack) {

    this.program = program;
    this.entity = entity;
    this.player = player;
    this.targetToAttack = targetToAttack;
    this.idleTime = 0;
		System.out.println("Programming robot with: ");
    System.out.println(program);
    robot = new RobotExec(entity, player, targetToAttack);
	}

	public boolean processActions()
	{
		//If we processed an action then reset our idle time
		if (robot.processActions())
			idleTime = 0;
		else
			idleTime++;

		if (idleTime > 10000)
			return false;
		else
			return true;
	}
	
	public void run() {

    System.out.println(Thread.currentThread());
    System.out.println("Running robot program \n");

		//Add the loop routine
		Pattern loop = Pattern.compile("repeat\\s*\\(\\s*([0-9]+)\\s*\\)");
		Matcher action = loop.matcher(program);
		StringBuffer sb = new StringBuffer(program.length());
		while (action.find()) {
			String cnt = action.group(1);
			action.appendReplacement(sb, Matcher.quoteReplacement("for(var index=0; index < " + cnt + "; index++)"));
		}
		action.appendTail(sb);
		System.out.println(sb.toString());
		program = sb.toString();

    Context cx = Context.enter();
    try {

      try {
        // Initialize the standard objects (Object, Function, etc.)
        // This must be done before scripts can be executed. Returns
        // a scope object that we use in later calls.
        Scriptable scope = cx.initStandardObjects();

				//Remove imported java functions
				//cx is the Context instance you're using to run scripts
				cx.setClassShutter(new ClassShutter() {
						public boolean visibleToScripts(String className) {                   
						System.out.println("Class: " + className);
							
							if(className.startsWith("adapter") ||
								 className.startsWith("java.io.PrintStream") )
								return true;
							return false;
						}
				});


        String[] names = {"print", "move", "rotate", "noPath", "setBlock", "giveItem",
										      "getPosX", "getPosY", "getPosZ", "setDebug", "giveExp", "getExpLevel",
													"attack", "sleep", "getBlock" }; 
        robot.defineFunctionProperties(names, RobotExec.class,
            ScriptableObject.DONTENUM);
        ScriptableObject.putProperty(scope, "robot", robot);

        Object wrappedOut = Context.javaToJS(System.out, scope);
        ScriptableObject.putProperty(scope, "out", wrappedOut);

				//Load the library
				FileReader in = null;
				String filename = "library.js";
				try {
					in = new FileReader(filename);
				}
				catch (FileNotFoundException ex) {
					System.out.println("Couldn't open file \"" + filename + "\".");
					return;
				}
				if (in != null)
				{

					try {
						// Here we evalute the entire contents of the file as
						// a script. Text is printed only if the print() function
						// is called.
						cx.evaluateReader(scope, in, filename, 1, null);
					}
					catch (WrappedException we) {
						System.err.println(we.getWrappedException().toString());
						we.printStackTrace();
					}
					catch (EvaluatorException ee) {
						System.err.println("js: " + ee.getMessage());
					}
					catch (JavaScriptException jse) {
						System.err.println("js: " + jse.getMessage());
					}
					catch (IOException ioe) {
						System.err.println(ioe.toString());
					}
					finally {
						try {
							in.close();
						}
						catch (IOException ioe) {
							System.err.println(ioe.toString());
						}
					}
				}

        // Now evaluate the string we've colected.
        Object result = cx.evaluateString(scope, program, "<program>", 1, null);

        // Convert the result to a string and print it.
        player.c("Program Excepted"); 
      }
			catch (WrappedException we) {
				player.c(we.getWrappedException().toString());
				//we.printStackTrace();
			}
			catch (EvaluatorException ee) {
				player.c(ee.toString());
			}
			catch (JavaScriptException jse) {
				player.c(jse.toString());
			}
			catch (Exception ex)
			{
				player.c(ex.toString());
				//System.err.println(ex.toString()); 
			}


    }
     finally {
      // Exit from the context.
      Context.exit();
    }
  }

}

