import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.security.auth.login.LoginException;

//This is DiscordBot. The bot is run on a Raspberry Pi so performance is crucial; mainly the bot cannot use too much memory

public class Bot extends ListenerAdapter {
    //Audio manager that is used to manage audio players
    AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    public Bot() {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(playerManager);
        playerManager.setPlayerCleanupThreshold(10000); //Set the cleanup threshold to 10000ms or 10 seconds.
        AudioPlayer player = playerManager.createPlayer();
    }

    public static  void main(String[] args) throws LoginException, InterruptedException {
        String token = "NjY1NDA1MTk0MTMzMDQ1MjU5.XhqORw.t2jDYQrpok227hFE1LuV-guqSes"; //this is the bot token. if the code is made public remove this line and use a input buffer instead or else Discord will shut off the bot.
        System.out.println(token); //print the token. this meant for testing purposes.
        JDA bot = new JDABuilder(token).build();
        bot.awaitReady();
        bot.addEventListener(new Bot());
    }

    /**
     * This method is called whenever a message is sent in a text channel in a server/guild that the bot is a part of.
     * The method will check what the message is and respond accordingly. Ie. if the message was a valid command for the bot
     * then the bot will respond or else the bot will ignore.
     * @param event this represents the message and hold information such as the contents, author and channel of the message.
     */
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String msg = event.getMessage().getContentDisplay();
        //checking if there is a command and what it is.
        if (!event.getAuthor().isBot()) {
            if (msg.equalsIgnoreCase("!seal"))
                seal(event);
            else if (msg.equalsIgnoreCase("!help"))
                help(event);
            else if (msg.equalsIgnoreCase("!gamer"))
                gamer(event);
            else if (msg.equalsIgnoreCase("!nut"))
                nut(event);
            else if (msg.equalsIgnoreCase("!ayaya"))
                ayaya(event);
            else if (msg.equalsIgnoreCase("!stop"))
                stop(event);
            else if (msg.equalsIgnoreCase("!seno"))
                seno(event);
            else if (msg.equalsIgnoreCase("!bocchi") || msg.equalsIgnoreCase("!tomodachininaritai"))
                bocchi(event);
            else if (msg.equalsIgnoreCase("!@"))
                at(event);
            else if (msg.matches("^(!)(?:https?:\\/\\/)?(?:www\\.)?(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=))((\\w|-){11})?$"))
                loadAndPlay(msg.substring(1), event);
        }
    }

    /**
     * This method is called whenever a user has left the channel
     * @param event contains information such as which user left and the channel that the user left from.
     */
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        checkLeave(event);
    }

    /**
     * Sends a custom emote sequence from the KillerB's Meme Emporium Server to the text channel from which the command was evoked.
     * This method will only work on KillerB's Meme Emporium server.
     * @param event contains information about the command that was sent
     */
    private void bocchi(MessageReceivedEvent event) {
        sendMessage(event, "<:Bocchi1:638528475069939725><:Bocchi2:638528488726462498>\n<:Bocchi3:638528499187187712><:Bocchi4:638528508888481817>");
    }

    /**
     * Sends the Navy Seal copypasta to the text channel from which the command was evoked.
     * Will work on all servers the bot is a part of.
     * @param event contains information about the command that was sent
     */
    private void seal(MessageReceivedEvent event) {
        event.getChannel().sendMessage("What the fuck did you just fucking say about me, you little bitch? I'll have you know I graduated top of my class in the Navy Seals, and I've been involved in numerous secret raids on Al-Quaeda, and I have over 300 confirmed kills. I am trained in gorilla warfare and I'm the top sniper in the entire US armed forces. You are nothing to me but just another target. I will wipe you the fuck out with precision the likes of which has never been seen before on this Earth, mark my fucking words. You think you can get away with saying that shit to me over the Internet? Think again, fucker. As we speak I am contacting my secret network of spies across the USA and your IP is being traced right now so you better prepare for the storm, maggot. The storm that wipes out the pathetic little thing you call your life. You're fucking dead, kid. I can be anywhere, anytime, and I can kill you in over seven hundred ways, and that's just with my bare hands. Not only am I extensively trained in unarmed combat, but I have access to the entire arsenal of the United States Marine Corps and I will use it to its full extent to wipe your miserable ass off the face of the continent, you little shit. If only you could have known what unholy retribution your little \"clever\" comment was about to bring down upon you, maybe you would have held your fucking tongue. But you couldn't, you didn't, and now you're paying the price, you goddamn idiot. I will shit fury all over you and you will drown in it. You're fucking dead, kiddo.").queue();
    }

    /**
     * Sends a text-to-speech message to the server form which the command was evoked.
     * The message consists of a random number of "@" between 10 and 20.
     * @param event contains infromation abouut the command that was sent
     */
    private void at(MessageReceivedEvent event) {
        int times = (int)(Math.random() * 10.0 + 10.0);
        StringBuilder msg = new StringBuilder();
        for (int i = 0; i < times; ++i)
            msg.append("@");
        MessageBuilder msgSend = new MessageBuilder(msg);
        msgSend.setTTS(true);
        event.getChannel().sendMessage(msgSend.build()).queue();
    }

    /**
     * Sends a list of the bots command to the text channel that evoked the command.
     * @param event contains information about the bot.
     */
    private void help(MessageReceivedEvent event) {
        event.getChannel().sendMessage("!seal: copypasta \n " +
                "!gamer: mentions others depending on who you are \n" +
                "more to be added.")
                .queue();
    }

    private void gamer(MessageReceivedEvent event) {
        User author = event.getAuthor();
        String name = author.getAsTag();
        switch(name) {
            case "Thedomesticfish#9450":
                sendMessage(event, "Gaymer Time <@171091041905147915><@159513805368459266>");
                return;
            case "Fak#6598":
                sendMessage(event, "Gaymer Time <@224300400126328834><@159513805368459266>");
                return;
            case "killerb#0444":
                sendMessage(event, "Gaymer Time <@224300400126328834><@171091041905147915>");
                return;
            default:
                sendMessage(event, "This command isn't made for you. Fuck off.");
        }
    }

    private void nut (MessageReceivedEvent event) {
        sendMessage(event, ":regional_indicator_n: :regional_indicator_u: :regional_indicator_t:");
    }

    private void sendMessage(MessageReceivedEvent event, String msg) {
        event.getChannel().sendMessage(msg).queue();
    }

    private void checkLeave(GuildVoiceLeaveEvent event) {
        VoiceChannel channel = event.getChannelLeft();
        Guild guild = event.getGuild();
        AudioManager manager = guild.getAudioManager();
        if (channel.getMembers().size() == 1 && channel.getIdLong() == manager.getConnectedChannel().getIdLong()) {
            if (manager.isConnected())
                manager.closeAudioConnection();
        }
    }

    private void ayaya(MessageReceivedEvent event) {
        loadAndPlay("https://www.youtube.com/watch?v=D0q0QeQbw9U", event);
    }

    private void stop(MessageReceivedEvent event) {
        VoiceChannel channel = event.getMember().getVoiceState().getChannel();
        if (channel == null) {
            sendMessage(event, "You are not currently in a channel");
            return;
        }

        Guild guild = event.getGuild();
        AudioManager manager = guild.getAudioManager();
        if (manager.isConnected())
            manager.closeAudioConnection();
        else
            sendMessage(event, "The bot isn't in the voice channel retard.");
    }

    private void seno(MessageReceivedEvent event){
        loadAndPlay("https://www.youtube.com/watch?v=oyuHmYSt2iA",  event);
    }

    private void loadAndPlay(String trackUrl, MessageReceivedEvent event) {
            VoiceChannel channel = event.getMember().getVoiceState().getChannel();
            AudioPlayer player = playerManager.createPlayer();
            if (channel == null) {
                sendMessage(event, "You are not currently in a channel");
                return;
            }
            Guild guild = event.getGuild();
            playerManager.loadItem(trackUrl, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    player.playTrack(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    //ignore for now since we only play one song
                }

                @Override
                public void noMatches() {
                    sendMessage(event, "invalid url");
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    sendMessage(event, "Song load failed");
                }
            });
            AudioManager manager = guild.getAudioManager();
            manager.setSendingHandler(new AudioPlayerSendHandler(player));
            if (!manager.isConnected())
                manager.openAudioConnection(channel);
        }
}
