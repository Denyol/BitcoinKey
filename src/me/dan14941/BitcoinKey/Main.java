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

public class Main extends JavaPlugin implements CommandExecutor
{
	public String updatedVerion = null;

	@Override
	public void onEnable()
	{
		String spigotVersion = null;

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}

		File f = new File("plugins/lib/bitcoinj-core-0.14.3-bundled.jar");
		if(f.exists() == false && !f.isDirectory())
		{ 
			this.getLogger().info("Downloading bitcoinj library!");
			// do something
			URL dld;
			try {
				dld = new URL("https://search.maven.org/remotecontent?filepath=org/bitcoinj/bitcoinj-core/0.14.3/bitcoinj-core-0.14.3-bundled.jar");
				ReadableByteChannel rbc = Channels.newChannel(dld.openStream());
				FileOutputStream fos = new FileOutputStream("plugins/lib/bitcoinj-core-0.14.3-bundled.jar");
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos.close();
			} catch (MalformedURLException e1)
			{
				e1.printStackTrace();
			}
			catch (IOException e1) {
				e1.printStackTrace();
			}

			this.getLogger().info("Finished download!");
			this.getLogger().warning("Please reload plugins for " + this.getName() + " to work!");
			this.getPluginLoader().disablePlugin(this);
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
		/*
		String str = "";

		for (int i = 1; i <= 32; i++)
		{
			// needs to be 64 digits
			str = str + String.format("%02d", ((int)(Math.random()*100)));
		}
		str = str.trim();
		 */

		final NetworkParameters netParams;
		netParams = NetworkParameters.fromID(NetworkParameters.ID_MAINNET);

		//System.out.println(str);

		ECKey key = new ECKey();
		//key = ECKey.fromPrivate(new BigInteger(str));

		if(sender instanceof Player && sender.hasPermission("BitcoinKey"))
		{
			String tellrawPub = "tellraw " +sender.getName()+ " [\"\",{\"text\":\"Your Generated Public Key: \",\"color\":\"gold\"},{\"text\":\""+ key.getPublicKeyAsHex() +"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + key.getPublicKeyAsHex() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to Copy!\",\"color\":\"red\"}]}}}]";
			String tellrawPriv = "tellraw " +sender.getName()+ " [\"\",{\"text\":\"Your Generated Private Key: \",\"color\":\"gold\"},{\"text\":\""+ key.getPrivateKeyAsHex() +"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + key.getPrivateKeyAsHex() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to Copy!\",\"color\":\"red\"}]}}}]";
			String tellrawAdd = "tellraw " +sender.getName()+ " [\"\",{\"text\":\"Your Generated Address: \",\"color\":\"gold\"},{\"text\":\""+ key.toAddress(netParams).toString() +"\",\"color\":\"light_purple\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"" + key.toAddress(netParams).toString() + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to Copy!\",\"color\":\"red\"}]}}}]";


			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawPub);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawPriv);
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), tellrawAdd);
		}
		else if(sender instanceof ConsoleCommandSender)
		{
			sender.sendMessage(ChatColor.GOLD + "Your Generated Public Key: " + key.getPublicKeyAsHex());
			sender.sendMessage(ChatColor.GOLD + "Your Generated Private Key: " + key.getPrivateKeyAsHex());
			sender.sendMessage(ChatColor.GOLD + "Your Generated Address: " + key.toAddress(netParams).toString());
		}

		if(sender.hasPermission("BitcoinKey") == false)
		{
			sender.sendMessage(ChatColor.RED + "Sorry you don't have permission to run this command!");
		}
		return true;
	}
}
