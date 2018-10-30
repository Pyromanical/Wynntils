/*
 *  * Copyright © Wynntils - 2018.
 */

package cf.wynntils.modules.core.events;

import cf.wynntils.Reference;
import cf.wynntils.core.events.custom.WynnWorldJoinEvent;
import cf.wynntils.core.framework.instances.PlayerInfo;
import cf.wynntils.core.framework.interfaces.Listener;
import cf.wynntils.core.framework.interfaces.annotations.EventHandler;
import cf.wynntils.modules.core.instances.PacketFilter;
import cf.wynntils.webapi.WebManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Arrays;
import java.util.HashSet;

public class ServerEvents implements Listener {

    @EventHandler
    public void joinServer(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        e.getManager().channel().pipeline().addBefore("fml:packet_handler", Reference.MOD_ID + ":packet_filter", new PacketFilter());

        WebManager.checkForUpdates();
    }

    boolean waitingForFriendList = false;
    @EventHandler
    public void joinWorldEvent(WynnWorldJoinEvent e) {
        Minecraft.getMinecraft().player.sendChatMessage("/friends list");

        waitingForFriendList = true;
    }

    @EventHandler
    public void chatMessage(ClientChatReceivedEvent e) {
        if(e.isCanceled() || e.getType() != 1) {
            return;
        }
        if(e.getMessage().getUnformattedText().startsWith(Minecraft.getMinecraft().player.getName() + "'")) {
            String[] splited = e.getMessage().getUnformattedText().split(":");

            String[] friends;
            if(splited[1].contains(",")) {
                friends = splited[1].substring(1).split(", ");
            }else{ friends = new String[] {splited[1].substring(1)}; }

            PlayerInfo.getPlayerInfo().setFriendList(new HashSet<>(Arrays.asList(friends)));

            if(waitingForFriendList) e.setCanceled(true);
            waitingForFriendList = false;
        }
    }

    @EventHandler
    public void addFriend(ClientChatEvent e) {
        if(e.getMessage().startsWith("/friend add ")) {
            PlayerInfo.getPlayerInfo().getFriendList().add(e.getMessage().replace("/friend add ", ""));
        }else if(e.getMessage().startsWith("/friend remove ")) {
            PlayerInfo.getPlayerInfo().getFriendList().remove(e.getMessage().replace("/friend remove ", ""));
        }
    }

}
