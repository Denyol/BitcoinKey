package me.dan14941.BitcoinKey;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements CommandExecutor
{
	public String updatedVerion = null;
	public Main main = null;

	@Override
	public void onEnable()
	{
		String spigotVersion = null;
		main = this;

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

		File dir = new File("plugins/lib");
		if(dir.exists() == false)
		{
			dir.mkdir();
		}
		
		File f = new File("plugins/lib/bitcoinj-core-0.14.3-bundled.jar");
		if(f.exists() == false && !f.isDirectory())
		{ 
			this.getLogger().info("Downloading bitcoinj library!");
			
			new BukkitRunnable() {
	            public void run()
	            {
	            	URL dld;
	    			try {
	    				dld = new URL("https://search.maven.org/remotecontent?filepath=org/bitcoinj/bitcoinj-core/0.14.3/bitcoinj-core-0.14.3-bundled.jar");
	    				ReadableByteChannel rbc = Channels.newChannel(dld.openStream());
	    				FileOutputStream fos = new FileOutputStream("plugins/lib/tmp.jar");
	    				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
	    				fos.close();
	    				
	    				File tmpFile = new File("plugins/lib/tmp.jar");
	    				File newFile = new File("plugins/lib/bitcoinj-core-0.14.3-bundled.jar");
	    				tmpFile.renameTo(newFile);
	    			} catch (MalformedURLException e1)
	    			{
	    				e1.printStackTrace();
	    			}
	    			catch (IOException e1) {
	    				e1.printStackTrace();
	    			}
	    			
	    			new BukkitRunnable()
	    			{
	    				public void run()
	    				{
	    					main.getLogger().info("Finished download!");
	    					main.getLogger().warning("Please reload plugins for " + main.getName() + " to work!");
	    					main.getPluginLoader().disablePlugin(main);
	    				}
	    			}.runTask(main);
	            }
	        }.runTaskAsynchronously(this);
	        
			return;
		}

		if(f.exists() == false && !f.isDirectory())
		{ 
			this.getLogger().severe("Could not find the bitcoinj library!");
			this.getLogger().severe("Disabling plugin!");
			this.getPluginLoader().disablePlugin(this);
			return;
		}

		this.getCommand("BitcoinKey").setExecutor(this);

		String resource = "25280";

		getLogger().info("Checking for a new version!");
		try {
			HttpURLConnection con = (HttpURLConnection) new URL(
					"http://www.spigotmc.org/api/general.php").openConnection();
			con.setDoOutput(true);
			con.setRequestMethod("POST");
			con.getOutputStream()
			.write(("key=98BE0FE67F88AB82B4C197FAF1DC3B69206EFDCC4D3B80FC83A00037510B99B4&resource=" + resource)
					.getBytes("UTF-8"));
			String version = new BufferedReader(new InputStreamReader(
					con.getInputStream())).readLine();
			if (version.length() <= 20) {
				spigotVersion = version;
			}
		} catch (Exception ex) {
			getLogger().info("Failed to check for a update on spigot.");
		}

		if(this.getDescription().getVersion().equalsIgnoreCase(spigotVersion) == false)
			getLogger().info("A new version was found on spigotmc!");
		else
			getLogger().info("No new version was found.");

		updatedVerion = spigotVersion;

		if(this.getDescription().getVersion().equalsIgnoreCase(updatedVerion) == false && updatedVerion != null)
			getServer().getScheduler().runTaskLater(this, new Runnable()
			{
				public void run()
				{
					getServer().getConsoleSender().sendMessage(ChatColor.RED + "[BitcoinKey] There is an update available! Please download version " + updatedVerion + "!");
				}
			}, 45);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		File depend = new File("plugins/lib/bitcoinj-core-0.14.3-bundled.jar");
		if(depend.exists() == false && !depend.isDirectory())
		{ 
			if(sender instanceof Player)
			{
				sender.sendMessage(ChatColor.RED + "Something went wrong! Please ask a staff member to check the console logs!");
				return true;
			}
			else
			{
				sender.sendMessage(ChatColor.DARK_RED + "[" + this.getName() + "] Please ensure there is a 'lib/bitcoinJ...' file inside the plugin folder then do /reload.");
				return true;
			}
		}
		
		final NetworkParameters netParams;
		netParams = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);

		ECKey key = new ECKey();
		//key = ECKey.fromPrivate(new BigInteger(str));

		if(sender instanceof Player && sender.hasPermission("BitcoinKey.generate"))
		{
			String commandStart = "tellraw " + sender.getName();
			String tellrawPub = commandStart+ " [\"\",{\"text\":\"Your Generated Public Key: \",\"color\":\"gold\"},{\"text\":\""+ key.getPublicKeyAsHex() +"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + key.getPublicKeyAsHex() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to Copy!\",\"color\":\"red\"}]}}}]";
			String tellrawPriv = commandStart+ " [\"\",{\"text\":\"Your Generated Private Key: \",\"color\":\"gold\"},{\"text\":\""+ key.getPrivateKeyAsHex() +"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + key.getPrivateKeyAsHex() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to Copy!\",\"color\":\"red\"}]}}}]";
			String tellrawAdd = commandStart+ " [\"\",{\"text\":\"Your Generated Address: \",\"color\":\"gold\"},{\"text\":\""+ key.toAddress(netParams).toString() +"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + key.toAddress(netParams).toString() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to Copy!\",\"color\":\"red\"}]}}}]";
			String tellrawBlockchainLink = commandStart+ " [\"\",{\"text\":\"Blockchain Address Link: \",\"color\":\"gold\"},{\"text\":\"https://blockchain.info/address/"+key.toAddress(netParams).toString()+"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"https://blockchain.info/address/" + key.toAddress(netParams).toString() +"\"}}]";

			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawPub);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawPriv);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawAdd);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawBlockchainLink);
		}
		else if(sender instanceof ConsoleCommandSender)
		{
			sender.sendMessage(ChatColor.GOLD + "Your Generated Public Key: " + key.getPublicKeyAsHex());
			sender.sendMessage(ChatColor.GOLD + "Your Generated Private Key: " + key.getPrivateKeyAsHex());
			sender.sendMessage(ChatColor.GOLD + "Your Generated Address: " + key.toAddress(netParams).toString());
			sender.sendMessage(ChatColor.GOLD + "Blockchain Address Link: https://blockchain.info/address/" + key.toAddress(netParams).toString());
		}

		if(sender.hasPermission("BitcoinKey") == false)
		{
			sender.sendMessage(ChatColor.RED + "Sorry you don't have permission to run this command!");
		}
		return true;
	}
}
