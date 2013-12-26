package net.castegaming.plugins.FPSCaste.listener;

import net.castegaming.plugins.FPSCaste.FPSCaste;
import net.castegaming.plugins.FPSCaste.FPSPlayer;
import net.castegaming.plugins.FPSCaste.playerclass.weapons.WeaponContainer;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {
	
	FPSCaste plugin = null;
	
	public DeathListener(FPSCaste plugin){
		this.plugin = plugin;
	}
	
	/**
	 * Defines what to do when a player dies
	 * @param e PlayerDeathEvent e
	 */
	@EventHandler 
	public void playerDeathEvent(PlayerDeathEvent e){
		Player death = e.getEntity();
		String playername = death.getName();
		FPSPlayer player = FPSCaste.getFPSPlayer(playername);
		
        if(player.isIngame()){	
        	player.getMatch().setTime(2400);
        	FPSPlayer killer = null;
	        WeaponContainer weapon = null;
	        String message = null;
	        boolean headshot = false;
	        
	        EntityDamageEvent lastDamageCause = e.getEntity().getLastDamageCause();
	        
	        e.setDroppedExp(0);
	        e.setDeathMessage(null);
			
			if (e.getDrops() != null){
				e.getDrops().clear();
				//clear drops untill i created class pickup system
			}
			
	        if (lastDamageCause.getCause() == DamageCause.ENTITY_ATTACK) {
	            EntityDamageByEntityEvent nEvent = (EntityDamageByEntityEvent) lastDamageCause;

	            if (nEvent.getDamager() instanceof Player) {
	            	//if damaged by a player, it means direct hit, aka a knife
	            	if (death.getInventory().first(1) != -1){
		        		killer = FPSCaste.getFPSPlayer(death.getInventory().getItem(death.getInventory().first(1)).getItemMeta().getLore().get(0));
		        		weapon = WeaponContainer.getWeapon(death.getMetadata("FPSexplosion").get(0).toString().split(":")[1]);
		        	} else {
		                killer = FPSCaste.getFPSPlayer(((Player) nEvent.getDamager()).getName());
		                weapon = WeaponContainer.getWeapon("Knife");
		        	}
	            } else {
	            	//not killed by a player entity?
	            }
	        } else if (lastDamageCause.getCause() == DamageCause.ENTITY_EXPLOSION){
	        	killer = FPSCaste.getFPSPlayer(death.getMetadata("FPSexplosion").get(0).toString().split(":")[0]);
	        	weapon = WeaponContainer.getWeapon(death.getMetadata("FPSexplosion").get(0).toString().split(":")[1]);
	        } else if (lastDamageCause.getCause() == DamageCause.FALL){
	        	weapon = null;
	        	killer = player;
	        	message = playername + " has fell to his death";
	        } else if (lastDamageCause.getCause() == DamageCause.PROJECTILE){
            	if (death.getKiller() != null){
            		EntityDamageByEntityEvent nEvent = (EntityDamageByEntityEvent) lastDamageCause;
            		
            		if (nEvent.getDamager() instanceof Arrow){
            			Arrow arrow = (Arrow) nEvent.getDamager();
            			
            			killer = FPSCaste.getFPSPlayer( (death.getKiller()).getName());
                		weapon = WeaponContainer.getWeapon(arrow.getMetadata("FPSweapon").get(0).asString());
						
						double y = arrow.getLocation().getY();
				        double shotY = e.getEntity().getLocation().getY();
				        headshot = y - shotY > 1.35d;
            		}
            	} else {
            	}
	        } else if (lastDamageCause.getCause() == DamageCause.SUICIDE){
            	killer = player;
            	weapon = null;
                message = playername + " has suicided for no reason :/";
	        }
	        
	        if (!(weapon == null)){
	        	if (message == null){
				    if (!death.getName().equals(killer.getName())){
				    	message = killer.getName() + " -[";
				    	if (headshot) message += "☹";
				    	message += weapon.getName();
				    	if (headshot) message += "☹";
				    	message += "]- " + death.getName();
				    } else {
				        message = death.getName() + " has committed suicide";
				    }
		        }
	        }
			    
		    player.getMatch().broadcastTeamBad(message, player.getTeam());
			    
			if (!killer.equals(player)) {
				killer.addKill(playername);
			} else {
				killer.getMatch().handleKill(killer.getName(), playername);
			}
		    player.addDeath(killer.getName());
        } 
	}
}