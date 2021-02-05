package com.skydhs.czruby.commands;

import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.manager.entity.Ruby;
import com.skydhs.czruby.menu.StoreMenu;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RubyCmd implements CommandExecutor {
    private final String[] HELP = FileUtil.get().getList("Messages.ruby-cmd.help").getRaw();
    private final String INSUFFICIENT_PERMISSION = FileUtil.get().getString("Messages.insufficient-permission").asString();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length <= 0) {
            Bukkit.dispatchCommand(sender, "rubi ajuda");
            return true;
        }

        // Main argument.
        String argument = args[0].toUpperCase();

        if (StringUtils.equalsIgnoreCase(argument, "AJUDA")) {
            for (String help : HELP) {
                sender.sendMessage(help);
            }
        } else if (StringUtils.equalsIgnoreCase(argument, "LOJA")) {
            if (sender instanceof Player) {
                StoreMenu.open((Player) sender);
            } else {
                sender.sendMessage("Apenas jogadores pode executar este comando.");
            }
        } else {
            if (!sender.hasPermission("ruby.admin")) {
                sender.sendMessage(INSUFFICIENT_PERMISSION);
                return true;
            } else if (args.length < 3) {
                Bukkit.dispatchCommand(sender, "rubi ajuda");
                return true;
            }

            // Admin commands.
            Ruby target = Ruby.from(args[1]);
            long amount;

            if (target == null || !target.isOnline()) {
                sender.sendMessage(FileUtil.get().getString("Messages.invalid-target").asString());
                return true;
            }

            try {
                amount = Long.parseLong(args[2]);
            } catch (NumberFormatException ex) {
                sender.sendMessage(FileUtil.get().getString("Messages.args-need-to-be-int").replace("%args%", args[2]).asString());
                return true;
            }

            String path = null;
            switch (argument) {
                case "SET":
                    path = "set-ruby-sender:set-ruby-target";
                    target.setRubies(amount);
                    break;
                case "ADD":
                    path = "add-ruby-sender:add-ruby-target";
                    target.addRuby(amount);
                    break;
                case "TAKE":
                    path = "take-ruby-sender:take-ruby-target";
                    target.setRubies(target.getRubies() - amount);
                    break;
            }

            if (path != null) {
                String[] split = path.split(":");
                sender.sendMessage(FileUtil.get().getString("Messages.ruby-cmd." + split[0], new String[] {
                        "%target%",
                        "%rubies%"
                }, new String[] {
                        target.getPlayerName(),
                        String.valueOf(amount)
                }).asString());
                target.asPlayer().sendMessage(FileUtil.get().getString("Messages.ruby-cmd." + split[1], new String[] {
                        "%sender%",
                        "%rubies%"
                }, new String[] {
                        sender.getName(),
                        String.valueOf(amount)
                }).asString());
            }
        }

        return true;
    }
}