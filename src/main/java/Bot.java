
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

//This is DiscordBot. The bot is run on a Raspberry Pi so performance is crucial; mainly the bot cannot use too much memory.

public class Bot extends ListenerAdapter {
    //Audio manager that is used to manage audio players
    private AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    //Used for identifying the YouTube URL.
    private static final boolean start = true; //there is a timestamp that we want to start from
    private static final boolean none = false; //there is not a timestamp to start from
    private final String clientID;
    private JsonObject emotes;

    private Bot(String clientID) {
        playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(playerManager);
        playerManager.setPlayerCleanupThreshold(10000); //Set the cleanup threshold to 10000ms or 10 seconds.
        AudioPlayer player = playerManager.createPlayer();
        this.clientID = clientID;
        initializeEmotes();
    }

    private void initializeEmotes() {
        try(FileReader emoteFile = new FileReader("/home/pi/Bot/DiscordBot/src/main/java/emotes.json")) {
            emotes = JsonParser.parseReader(emoteFile).getAsJsonObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static  void main(String[] args) throws LoginException, InterruptedException {
        Scanner tokenScanner = new Scanner(System.in);
        System.out.println("Enter token");
        String token = tokenScanner.nextLine();
        System.out.println("Reddit Token"); //print the token. this meant for testing purposes.
        String cliendID = tokenScanner.nextLine();
        JDA bot = new JDABuilder(token).build();
        bot.awaitReady();
        bot.addEventListener(new Bot(cliendID));
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
            if (msg.contains("!seal"))
                seal(event);
            else if (msg.contains("!help"))
                help(event);
            else if (msg.contains("!gamer"))
                gamer(event);
            else if (msg.contains("!nut"))
                nut(event);
            else if (msg.contains("!ayaya"))
                ayaya(event);
            else if (msg.contains("!stop"))
                stop(event);
            else if (msg.contains("!seno"))
                seno(event);
            else if (msg.contains("!bocchi") || msg.contains("!tomodachininaritai"))
                bocchi(event);
            else if (msg.contains("!@"))
                at(event);
            //checking YouTube links without timestamps.
            else if (msg.matches("^(!)(?:https?:\\/\\/)?(?:www\\.)?(?:youtu\\.be\\/|youtube\\.com\\/(?:embed\\/|v\\/|watch\\?v=|watch\\?.+&v=))((\\w|-){11})?$"))
                loadAndPlay(msg.substring(1), event, none);
            //checking youtube links with timestamps.
            else if (msg.matches("^(!)(?:https?:\\/\\/)?(?:www\\.)?(youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=|\\&v=)([^#\\&\\?]*)(?:(\\?t|&start)=(\\d+))$"))
                loadAndPlay(msg.substring(1), event, start);
            else if (msg.contains("/spit"))
                spit(event);
            else if (msg.contains("!hot"))
                hot(event);
            else if (msg.contains("!4head"))
                fourHead(event);
            else if (msg.contains("!jebaited"))
                jebaited(event);
            else if (msg.contains("!fat"))
                fat(event);
            else if (msg.contains("!add")) {
                if (!msg.substring(0,4).equals("!add")) {
                    sendMessage(event, "Invalid format");
                    return;
                }
                String restOfString = msg.substring(5); //Look for all the words past !add.
                String[] emoteInfo = checkAddRemove(event, restOfString, true);
                if (emoteInfo != null)
                    addEmote(event, emoteInfo[0], emoteInfo[1]);
            }
            else if (msg.contains("!remove")) {
                if (!msg.substring(0,7).equals("!remove")) {
                    sendMessage(event, "Invalid format");
                    return;
                }
                String[] emoteInfo = checkAddRemove(event, msg.substring(8), false);
                if (emoteInfo != null)
                    removeEmote(event, emoteInfo[0]);
            }
            else if (msg.equals("!print"))
                sendMessage(event, emotes.toString());
            else if (msg.charAt(0) == '!')
                sendEmote(event, msg.substring(1));
        }
    }

    /**
     * This method is called whenever a user has left the channel
     * @param event contains information such as which user left and the channel that the user left from.
     */
    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        try {
            checkLeave(event);
        } catch (NullPointerException ignored) {
            //ignore since checkLeave only generates a null pointer when the bot isn't connected to a channel when a user leaves.
        }
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
        event.getChannel().sendMessage("!gamer, !seal, !bocchi, !nut, !stop, !ayaya, !seno, /spit, !hot, !4head, !jebaited, !fat, !add emoteName emoteID").queue();
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
                sendMessage(event, "This command isn't made for you. Screw off");
        }
    }

    private void nut (MessageReceivedEvent event) {
        sendMessage(event, ":regional_indicator_n: :regional_indicator_u: :regional_indicator_t:");
    }

    /**
     * Sends a message to the specified channel
     * @param event the channel to send the message to. Is always the channel from which to command came from.
     * @param msg the message to be sent in the Discord API format.
     */
    private void sendMessage(MessageReceivedEvent event, String msg) {
        event.getChannel().sendMessage(msg).queue();
    }

    /**
     * Called when someone leaves a voice channel. If the bot is in the same channel as the one that was left then the bot will leave iff the channel is empty
     * except for the bot
     * @param event contains information of what channel was left.
     */
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
        loadAndPlay("https://www.youtube.com/watch?v=D0q0QeQbw9U", event, none);
    }

    /**
     * Tells the bot to leave the voice channel. Will only work if the user that fires the command is in the same voice channel as the bot.
     * The bot will indicate to the user if it is not currently in a voice channel.
     * @param event contains information as to what where the message was sent from.
     */
    private void stop(MessageReceivedEvent event) {
         try {
             VoiceChannel channel = event.getMember().getVoiceState().getChannel();
             Guild guild = event.getGuild();
             AudioManager manager = guild.getAudioManager();
             if (manager.isConnected())
                 manager.closeAudioConnection();
             else
                 sendMessage(event, "The bot isn't in the voice channel idiot.");
         } catch (NullPointerException userNotInVoiceChannel) {
            sendMessage(event, "You are not currently in a channel idiot");
        }
    }

    private void seno(MessageReceivedEvent event){
        loadAndPlay("https://www.youtube.com/watch?v=oyuHmYSt2iA",  event, none);
    }

    /**
     * Loads a audio file from a source. Only works with YouTube right now
     * @param trackUrl the url of the YouTube video
     * @param event contains information as to where the request came from
     * @param type whether or not there is a predetermined timestamp from the url.
     */
    private void loadAndPlay(String trackUrl, MessageReceivedEvent event, boolean type) {
        try {
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
                    if (type == start) {
                        String timeS = "";
                        int indexOfTime = trackUrl.indexOf("?t=");
                        if (indexOfTime != -1) {
                            timeS = trackUrl.substring(indexOfTime + 3);
                        } else {
                            timeS = trackUrl.substring(indexOfTime + 6);
                        }
                        try {
                            int startTime = Integer.parseInt(timeS);
                            track.setPosition(startTime * 1000);
                            player.playTrack(track);
                        } catch (NumberFormatException e) {
                            sendMessage(event, "invalid timestamp");
                        }
                    }
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
                    sendMessage(event, "load failed");
                }
            });
            AudioManager manager = guild.getAudioManager();
            manager.setSendingHandler(new AudioPlayerSendHandler(player));
            if (!manager.isConnected())
                manager.openAudioConnection(channel);
        } catch (NullPointerException userNotInVoiceChannel) {
            sendMessage(event, "You are not currently in a voice channel idiot");
        }
    }

    private void spit(MessageReceivedEvent event) {
        loadAndPlay("https://youtu.be/hNXkLB_ewc8?t=5", event, start);
    }

    private void hot(MessageReceivedEvent event) {
        try {
            RedditBot reddit = new RedditBot(clientID);
            sendMessage(event, reddit.hot());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void fourHead(MessageReceivedEvent event){
        event.getChannel().sendFile(new File("/home/pi/Bot/DiscordBot/src/main/java/4head.png")).queue();
    }

    private void jebaited(MessageReceivedEvent event){
        event.getChannel().sendFile(new File("/home/pi/Bot/DiscordBot/src/main/java/jebaited.png")).queue();
    }

    private void fat(MessageReceivedEvent event) {
        event.getChannel().sendFile(new File("/home/pi/Bot/DiscordBot/src/main/java/fat.jpg")).queue();
    }

    private void sendEmote (MessageReceivedEvent event, String emote) {
        initializeEmotes(); //Temporary solution until I can figure out how to send the emote id over discord without it being transformed automatically
        if (emotes.has(emote))
            sendMessage(event, emotes.get(emote).getAsString());
        else
            sendMessage(event, "This emote doesn't exist");
    }

    private void addEmote (MessageReceivedEvent event, String emoteName, String emoteID) {
        if (emotes.has(emoteName))
            sendMessage(event, "This emote is already associated with another emote. Remove first then add again");
        else {
            emotes.addProperty(emoteName, emoteID);
            Gson pretty = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(pretty.toJson(emotes));
            overwriteJSON(event);
        }
    }

    private void removeEmote(MessageReceivedEvent event, String emoteName) {
        if (!emotes.has(emoteName))
            sendMessage(event, "This is not recognized as an added emote");
        else {
            emotes.remove(emoteName);
            overwriteJSON(event);
            Gson pretty = new GsonBuilder().setPrettyPrinting().create();
            System.out.println(pretty.toJson(emotes));
        }
    }

    private void overwriteJSON(MessageReceivedEvent event) {
        Gson pretty = new GsonBuilder().setPrettyPrinting().create();
        try{
            File json = new File("/home/pi/Bot/DiscordBot/src/main/java/emotes.json");
            FileWriter newJSONFile = new FileWriter(json, false);
            String prettyPrinted = pretty.toJson(emotes);
            newJSONFile.write(prettyPrinted);
        } catch (IOException e) {
            e.printStackTrace();
            sendMessage(event, "Couldn't add/remove emote to server. Emote will work but be lost on Bot restarts if this was an add command");
        }
    }

    private String[] checkAddRemove(MessageReceivedEvent event, String restOfString, boolean add) {
        String[] emoteInfo = restOfString.split(" ");
        if ((emoteInfo.length != 2 && add) || (emoteInfo.length != 1 && !add)) {
            sendMessage(event, "Command is in wrong format");
            return null;
        }
        return emoteInfo;
    }
}
