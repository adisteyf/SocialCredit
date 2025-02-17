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
    private final Map<String, Map<String, Boolean>> hasVoted = new HashMap<>();
    public int def_val = 10;

    @Override
    public void onEnable() {
        this.getCommand("sc").setExecutor(this);
        this.getCommand("+rep").setExecutor(this);
        this.getCommand("-rep").setExecutor(this);

        load();
    }

    public void load() {
        this.reloadConfig();

        for (String key : getConfig().getConfigurationSection("rating").getKeys(false)) {
            socialCredits.put(key, getConfig().getInt("rating." + key));
        }

        for (String voter : getConfig().getConfigurationSection("votes").getKeys(false)) {
            if (getConfig().contains("votes." + voter)) {
                for (String targetPlayer : getConfig().getConfigurationSection("votes." + voter).getKeys(false)) {
                    boolean votedForRep = getConfig().getBoolean("votes." + voter + "." + targetPlayer);

                    hasVoted.putIfAbsent(voter, new HashMap<>());
                    hasVoted.get(voter).put(targetPlayer, votedForRep);
                }
            }
        }

        def_val = getConfig().getInt("def_val");
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

    public boolean minusrepCommandChecker(String[] args, CommandSender sender, Command cmd) {
        if (!cmd.getName().equalsIgnoreCase("-rep") || args.length != 1) return false;

        if (sender instanceof Player) {
            String targetPlayer = args[0];
            String voter = sender.getName();

            if (hasVoted.containsKey(voter) && hasVoted.get(voter).containsKey(targetPlayer) && !hasVoted.get(voter).get(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "Вы уже проголосовали за " + targetPlayer + ". Вы можете проголосовать только за +rep.");
                return true;
            }

            socialCredits.put(targetPlayer, socialCredits.getOrDefault(targetPlayer, def_val) - 1);
            if (hasVoted.containsKey(voter) && hasVoted.get(voter).containsKey(targetPlayer) && hasVoted.get(voter).get(targetPlayer)) {
                socialCredits.put(targetPlayer, socialCredits.getOrDefault(targetPlayer, def_val) - 1);
            }

            int scs = socialCredits.get(targetPlayer);
            sender.sendMessage(ChatColor.YELLOW + "У игрока " + ChatColor.RESET + targetPlayer + ChatColor.YELLOW + " теперь " + ChatColor.RESET + getDeclension(scs) + ChatColor.RESET);

            hasVoted.putIfAbsent(voter, new HashMap<>());
            hasVoted.get(voter).put(targetPlayer, false); // false для -rep

            getConfig().set("votes." + voter + "." + targetPlayer, false);
            saveConfig();
        }

        return true;
    }

    public boolean plusrepCommandChecker(String[] args, CommandSender sender, Command cmd) {
        if (!cmd.getName().equalsIgnoreCase("+rep") || args.length != 1) return false;

        if (sender instanceof Player) {
            String targetPlayer = args[0];
            String voter = sender.getName();

            if (hasVoted.containsKey(voter) && hasVoted.get(voter).containsKey(targetPlayer) && hasVoted.get(voter).get(targetPlayer)) {
                sender.sendMessage(ChatColor.RED + "Вы уже проголосовали за " + targetPlayer + ". Вы можете проголосовать только за -rep.");
                return true;
            }

            // Уменьшаем социальные кредиты
            socialCredits.put(targetPlayer, socialCredits.getOrDefault(targetPlayer, def_val) + 1);
            if (hasVoted.containsKey(voter) && hasVoted.get(voter).containsKey(targetPlayer) && !hasVoted.get(voter).get(targetPlayer)) {
                socialCredits.put(targetPlayer, socialCredits.getOrDefault(targetPlayer, def_val) + 1);
            }

            int scs = socialCredits.get(targetPlayer);
            sender.sendMessage(ChatColor.YELLOW + "У игрока " + ChatColor.RESET + targetPlayer + ChatColor.YELLOW + " теперь " + ChatColor.RESET + getDeclension(scs) + ChatColor.RESET);

            hasVoted.putIfAbsent(voter, new HashMap<>());
            hasVoted.get(voter).put(targetPlayer, true); // true для +rep

            getConfig().set("votes." + voter + "." + targetPlayer, true);
            saveConfig();
        }

        return true;
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
