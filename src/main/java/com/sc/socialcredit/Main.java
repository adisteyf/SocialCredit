package com.sc.socialcredit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends JavaPlugin implements CommandExecutor {

    private final Map<String, Integer> socialCredits = new HashMap<>();

    @Override
    public void onEnable() {
        this.getCommand("sc").setExecutor(this);
        this.getCommand("+rep").setExecutor(this);
        this.getCommand("-rep").setExecutor(this);

        socialCredits.put("Adisteyf", 42);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (scCommandChecker(args, sender, command))       return false;
        if (plusrepCommandChecker(args, sender, command))  return false;
        if (minusrepCommandChecker(args, sender, command)) return false;

        return true;
    }

    public boolean scCommandChecker(String[] args, CommandSender sender, Command cmd) {
        if (!cmd.getName().equalsIgnoreCase("sc")) return false;

        if (args.length == 0) {
            showTopPlayers(sender);
            return true;
        }

        if (args.length == 1) {
            int scs = socialCredits.get(args[0]);
            sender.sendMessage(ChatColor.GOLD+ "Рейтинг игрока "+ChatColor.RESET + args[0] + ChatColor.GOLD+": "+ChatColor.RESET + getDeclension(scs));
            return true;
        }

        return false;
    }

    public static String getDeclension(int count) {
        String word;
        int lastDigit = count % 10;
        int lastTwoDigits = count % 100;

        if (lastDigit == 1 && lastTwoDigits != 11) {
            word = "кредит";
        } else if (lastDigit >= 2 && lastDigit <= 4 && (lastTwoDigits < 12 || lastTwoDigits > 14)) {
            word = "кредита";
        } else {
            word = "кредитов";
        }

        return count + " " + word;
    }

    public boolean plusrepCommandChecker(String[] args, CommandSender sender, Command cmd) {
        if (!cmd.getName().equalsIgnoreCase("+rep") || args.length != 1) return false;

        if (sender instanceof Player) {
            String targetPlayer = args[0];
            socialCredits.put(targetPlayer, socialCredits.getOrDefault(targetPlayer, 0) +1);
            int scs = socialCredits.get(args[0]);
            sender.sendMessage(ChatColor.YELLOW+ "У игрока " +ChatColor.RESET + args[0] + ChatColor.YELLOW+ " теперь " +ChatColor.RESET+ getDeclension(scs) +ChatColor.RESET);
        }

        return false;
    }

    public boolean minusrepCommandChecker(String[] args, CommandSender sender, Command cmd) {
        if (!cmd.getName().equalsIgnoreCase("-rep") || args.length != 1) return false;

        if (sender instanceof Player) {
            String targetPlayer = args[0];
            socialCredits.put(targetPlayer, socialCredits.getOrDefault(targetPlayer, 0) -1);
            int scs = socialCredits.get(args[0]);
            sender.sendMessage(ChatColor.YELLOW+ "У игрока " +ChatColor.RESET + args[0] + ChatColor.YELLOW+ " теперь " +ChatColor.RESET+ getDeclension(scs) +ChatColor.RESET);
        }

        return false;
    }

    private void showTopPlayers(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "Топ 10 игроков по социальным кредитам:");

        List<Map.Entry<String, Integer>> topPlayers = socialCredits.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());


        for (Map.Entry<String, Integer> entry : topPlayers) {
            sender.sendMessage(ChatColor.YELLOW + entry.getKey() + ": " + getDeclension(entry.getValue()));
        }
    }
}
