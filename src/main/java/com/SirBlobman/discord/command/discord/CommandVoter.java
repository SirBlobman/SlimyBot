package com.SirBlobman.discord.command.discord;

import java.util.Optional;

import com.SirBlobman.discord.constant.KnownServers;
import com.SirBlobman.discord.utility.Util;

import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.MessageAuthor;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

public class CommandVoter extends Command {
    public CommandVoter() {
        super("voter", "", "", Permission.getServerOnlyPermission(KnownServers.SIRBLOBMAN_DISCORD), "becomevoter", "registertovote");
    }

    @Override
    public void run(MessageAuthor author, ServerTextChannel channel, String[] args) {
        if(!author.isUser()) return;

        User user = author.asUser().orElse(null);
        if(user == null || user.isBot()) return;

        Server server = channel.getServer();
        giveVoterRole(server, user);
    }

    private Role getVoterRole(Server server) {
        if(server == null) return null;

        long roleID = 649094066801213451L;
        Optional<Role> orole = server.getRoleById(roleID);
        return orole.orElse(null);
    }

    private void giveVoterRole(Server server, User user) {
        if(server == null || user == null) return;

        Role role = getVoterRole(server);
        if(role == null) return;

        server.addRoleToUser(user, role, "'++voter' Command").whenComplete((success, error) -> {
            if(error != null) {
                Util.print("An error occurred while adding a voter.");
                return;
            }

            user.sendMessage("Thanks for participating in the polls for SirBlobman's Discord Server. If you believe this message was sent in error please contact SirBlobman#7235");
        });
    }
}