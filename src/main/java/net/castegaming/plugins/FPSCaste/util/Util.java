package net.castegaming.plugins.FPSCaste.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

public class Util {

	/**
	 * 
	 * @param l
	 * @param range
	 * @return
	 */
	public static Location randomInRange(Location l, double range) {
		Random r = new Random();
		double x = r.nextDouble() * (2 * range) - range;
		double z = r.nextDouble() * (2 * range) - range;
		return l.clone().add(x, 0, z);
	}

	/**
	 * @param s
	 *            world:x:y:z:yaw:pitch
	 * @return A location
	 */
	public static Location StringToLoc(String s) {
		String[] splits = s.split(":");
		int len = 5;

		if (splits.length < len)
			return null;
		double[] doubles = new double[len];

		try {
			for (int i = 1; i < len; i++) {
				doubles[i - 1] = Double.parseDouble(splits[i]);
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return new Location(Bukkit.getServer().getWorld(splits[0]), doubles[0],
				doubles[1], doubles[2], (float) doubles[3], (float) doubles[4]);
	}

	/**
	 * Returns a formatted string for the location
	 * 
	 * @param l
	 *            The location
	 * @return a string with syntax world:x:y:z
	 */
	public static String toLocationString(Location l) {
		return l.getWorld().getName() + ":" + l.getX() + ":" + l.getY() + ":"
				+ l.getZ() + ":" + l.getYaw() + ":" + l.getPitch();
	}

	/**
	 * @param s
	 *            world:x:y:z
	 * @return A location
	 */
	public static Location toBlockLoc(String s) {
		String[] splits = s.split(":");
		if (splits.length < 3)
			return null;
		int[] ints = new int[3];

		try {
			for (int i = 1; i < 4; i++) {
				ints[i - 1] = Integer.parseInt(splits[i]);
			}
		} catch (NumberFormatException e) {
			return null;
		}
		return new Location(Bukkit.getServer().getWorld(splits[0]), ints[0],
				ints[1], ints[2]);
	}

	/**
	 * Returns a formatted string for the location
	 * 
	 * @param l
	 *            The location
	 * @return a string with syntax world:x:y:z
	 */
	public static String toBlockString(Location l) {
		return l.getWorld().getName() + ":" + l.getBlockX() + ":"
				+ l.getBlockY() + ":" + l.getBlockZ();
	}

	/**
	 * Transforms name:damage into an {@link ItemStack}
	 * 
	 * @param material
	 *            the name
	 * @return
	 */
	public static ItemStack transformItem(String material) {
		String[] itemnames = material.split(":");
		Material mat = Material.getMaterial(itemnames[0]);

		ItemStack i = new ItemStack(mat);
		if (itemnames.length > 1) {
			i.setDurability((short) Parse.parseInt(itemnames[1], 0));
		}
		return i;
	}

	public static List<Block> blocksInRange(Location location, int maxrange) {
		Location l;
		List<Block> blocks = new LinkedList<Block>();

		for (int x = -(maxrange); x <= maxrange; x++) {
			for (int y = -(maxrange); y <= maxrange; y++) {
				for (int z = -(maxrange); z <= maxrange; z++) {
					l = location.clone().add(x, y, z);
					if (l.distanceSquared(location) > maxrange * maxrange)
						continue;
					blocks.add(l.getBlock());
				}
			}
		}
		return blocks;
	}

	public static boolean isEqual(Location l1, Location l2) {
		if (!l1.getWorld().getName().equals(l2.getWorld().getName()))
			return false;

		double xfrom = l1.getX();
		double zfrom = l1.getZ();

		double xto = l2.getX();
		double zto = l2.getZ();

		return (xfrom == xto && zfrom == zto);
	}

	public static Location emptySpace(Location l) {
		if (l != null) {
			Location locabove = l;
			locabove.add(0, 1, 0);
			if (locabove.getBlock().getType().equals(Material.AIR)) {
				locabove = emptySpace(locabove);
			}
			return locabove;
		} else {
			return null;
		}
	}

	public static Location blockBelow(Location l) {
		if (l != null) {
			Location locbelow = l;
			locbelow.add(0, -1, 0);
			if (locbelow.getBlock().getType().equals(Material.AIR)) {
				locbelow = blockBelow(locbelow);
			}
			return locbelow;
		} else {
			return null;
		}
	}

	private static Method nmsBlockGetter;
	private static Field durabilityField;

	/**
	 * @param b
	 * @return
	 */
	public static float getBlockResistance(Material mat) {
		if (nmsBlockGetter == null) {
			try {
				Class<?> magicNumberClass = Class.forName(Bukkit.getServer()
						.getClass().getPackage().getName()
						+ ".util.CraftMagicNumbers");
				nmsBlockGetter = magicNumberClass.getMethod("getBlock",
						Material.class);
				durabilityField = nmsBlockGetter.getReturnType()
						.getDeclaredField("durability");
				durabilityField.setAccessible(true);
			} catch (Exception e){
				return -1;
			}
		}

		try {
			return durabilityField.getFloat(nmsBlockGetter.invoke(null, mat));
		} catch (Exception e){
			return -1;
		}
	}
}
