package com.skydhs.czruby.commands;

import com.skydhs.czruby.FileUtil;
import com.skydhs.czruby.manager.entity.Ruby;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FragmentCmd implements CommandExecutor  {
    private final String[] HELP = FileUtil.get().getList("Messages.fragment-cmd.help").getRaw();
    private final String INSUFFICIENT_PERMISSION = FileUtil.get().getString("Messages.insufficient-permission").asString();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length <= 0) {
            Bukkit.dispatchCommand(sender, "rubifrag ajuda");
            return true;
        }

        // Main argument.
        String argument = args[0].toUpperCase();

        if (StringUtils.equalsIgnoreCase(argument, "AJUDA")) {
            for (String help : HELP) {
                sender.sendMessage(help);
            }
        } else {
            if (!sender.hasPermission("ruby.admin")) {
                sender.sendMessage(INSUFFICIENT_PERMISSION);
                return true;
            } else if (args.length < 3) {
                Bukkit.dispatchCommand(sender, "rubifrag ajuda");
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
                    path = "set-fragment-sender:set-fragment-target";
                    target.setFragments(amount);
                    break;
                case "ADD":
                    path = "add-fragment-sender:add-fragment-target";
                    target.addFragment(amount);
                    break;
                case "TAKE":
                    path = "take-fragment-sender:take-fragment-target";
                    target.setFragments(target.getFragments() - amount);
                    break;
            }

            if (path != null) {
                String[] split = path.split(":");
                sender.sendMessage(FileUtil.get().getString("Messages.ruby-cmd." + split[0], new String[] {
                        "%target%",
                        "%fragments%"
                }, new String[] {
                        target.getPlayerName(),
                        String.valueOf(amount)
                }).asString());
                target.asPlayer().sendMessage(FileUtil.get().getString("Messages.ruby-cmd." + split[1], new String[] {
                        "%sender%",
                        "%fragments%"
                }, new String[] {
                        sender.getName(),
                        String.valueOf(amount)
                }).asString());
            }
        }

        return true;
    }
}