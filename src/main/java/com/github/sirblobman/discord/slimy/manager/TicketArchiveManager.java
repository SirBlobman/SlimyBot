package com.github.sirblobman.discord.slimy.manager;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.sirblobman.discord.slimy.DiscordBot;
import com.github.sirblobman.discord.slimy.object.ChannelRecord;
import com.github.sirblobman.discord.slimy.object.InvalidConfigurationException;
import com.github.sirblobman.discord.slimy.object.MainConfiguration;
import com.github.sirblobman.discord.slimy.object.MemberRecord;
import com.github.sirblobman.discord.slimy.object.MessageActionType;
import com.github.sirblobman.discord.slimy.object.MessageEntry;
import com.github.sirblobman.discord.slimy.object.MessageInformation;

import j2html.Config;
import j2html.TagCreator;
import j2html.tags.DomContent;
import j2html.tags.specialized.ATag;
import j2html.tags.specialized.BodyTag;
import j2html.tags.specialized.DivTag;
import j2html.tags.specialized.H1Tag;
import j2html.tags.specialized.HeadTag;
import j2html.tags.specialized.HtmlTag;
import j2html.tags.specialized.ImgTag;
import j2html.tags.specialized.SectionTag;
import j2html.tags.specialized.TimeTag;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import static j2html.TagCreator.a;
import static j2html.TagCreator.blockquote;
import static j2html.TagCreator.body;
import static j2html.TagCreator.br;
import static j2html.TagCreator.div;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.h2;
import static j2html.TagCreator.h4;
import static j2html.TagCreator.head;
import static j2html.TagCreator.img;
import static j2html.TagCreator.link;
import static j2html.TagCreator.main;
import static j2html.TagCreator.meta;
import static j2html.TagCreator.p;
import static j2html.TagCreator.script;
import static j2html.TagCreator.section;
import static j2html.TagCreator.span;
import static j2html.TagCreator.strong;
import static j2html.TagCreator.time;

public final class TicketArchiveManager extends Manager {
    public TicketArchiveManager(DiscordBot discordBot) {
        super(discordBot);
    }

    public CompletableFuture<Void> archive(TextChannel channel) {
        return CompletableFuture.runAsync(() -> {
            try {
                DiscordBot discordBot = getDiscordBot();
                MessageHistoryManager messageHistoryManager = discordBot.getMessageHistoryManager();
                messageHistoryManager.archiveChannel(channel);
                archiveInternal(channel);
            } catch (Exception ex) {
                throw new CompletionException(ex);
            }
        });
    }

    private String getTicketCreator(TextChannel channel) {
        String topic = channel.getTopic();
        if (topic == null) {
            return "Unknown";
        }

        Guild guild = channel.getGuild();
        Member member = guild.getMemberById(topic);
        if (member == null) {
            return "Unknown";
        }

        User user = member.getUser();
        return user.getAsTag();
    }

    private void archiveInternal(TextChannel channel) throws IOException, InvalidConfigurationException {
        MainConfiguration mainConfiguration = getDiscordBot().getMainConfiguration();
        String ticketHistoryChannelId = mainConfiguration.getTicketHistoryChannelId();

        Guild guild = channel.getGuild();
        TextChannel historyChannel = guild.getTextChannelById(ticketHistoryChannelId);
        if (historyChannel == null) {
            throw new InvalidConfigurationException("Invalid ticket history channel!");
        }

        Config.closeEmptyTags = true;
        HeadTag headElement = renderHead(channel);
        BodyTag bodyElement = renderBody(channel);
        HtmlTag htmlElement = TagCreator.html().attr("lang", "en").with(headElement, bodyElement);
        String document = TagCreator.document(htmlElement);

        String channelName = channel.getName();
        String timestamp = Long.toString(channel.getTimeCreated().toInstant().toEpochMilli());
        String ticketId = (channelName + "-" + timestamp);
        String ticketFileName = (ticketId + ".html");

        Path archiveFilePath = Paths.get("archive", "tickets", ticketFileName);
        if (!Files.exists(archiveFilePath)) {
            Path parentPath = archiveFilePath.getParent();
            if (parentPath != null) {
                Files.createDirectories(parentPath);
            }

            Files.createFile(archiveFilePath);
        }

        Files.writeString(archiveFilePath, document, StandardCharsets.UTF_8, StandardOpenOption.WRITE);
        String creatorTag = getTicketCreator(channel);
        MessageBuilder messageBuilder = new MessageBuilder();
        messageBuilder.append("Ticket ").append(ticketId).append(" by ").append(creatorTag);
        Message message = messageBuilder.build();

        File file = archiveFilePath.toFile();
        historyChannel.sendMessage(message).addFile(file).queue();
    }

    private HeadTag renderHead(TextChannel channel) {
        String channelName = channel.getName();
        String timestamp = Long.toString(channel.getTimeCreated().toInstant().toEpochMilli());
        String ticketId = (channelName + "-" + timestamp);

        return head(
                meta().attr("charset", "UTF-8"),
                meta().attr("http-equiv", "X-UA-Compatible").attr("content", "IE=edge"),
                meta().attr("name", "viewport")
                        .attr("content", "width=device-width, initial-scale=1.0"),
                meta().attr("name", "author").attr("content", "Olivo"),
                link().withRel("stylesheet").withType("text/css")
                        .withHref("https://www.sirblobman.xyz/style/ticket.min.css"),
                script().withSrc("https://www.sirblobman.xyz/script/discord-markdown.min.js")
        ).withTitle("Ticket " + ticketId);
    }

    private BodyTag renderBody(TextChannel channel) {
        String channelName = channel.getName();
        String timestamp = Long.toString(channel.getTimeCreated().toInstant().toEpochMilli());
        String ticketId = (channelName + "-" + timestamp);
        String creatorTag = getTicketCreator(channel);

        return body(
                main(
                        section(
                                h1("Ticket " + ticketId),
                                h2("Created by " + creatorTag)
                        ).withId("title"),
                        getMessagesSection(channel)
                ),
                script().withSrc("https://www.sirblobman.xyz/script/discord-convert.js")
        );
    }

    private SectionTag getMessagesSection(TextChannel channel) {
        SectionTag section = section();
        DiscordBot discordBot = getDiscordBot();
        MessageHistoryManager messageHistoryManager = discordBot.getMessageHistoryManager();

        Map<String, MessageInformation> messageContentMap = new LinkedHashMap<>();
        List<MessageEntry> messageHistoryList = messageHistoryManager.getMessageHistory(channel);
        for (MessageEntry entry : messageHistoryList) {
            MessageActionType actionType = entry.getActionType();
            String messageId = entry.getMessageId();

            if (actionType == MessageActionType.DELETE) {
                messageContentMap.remove(messageId);
                continue;
            }

            if (actionType == MessageActionType.EDIT || actionType == MessageActionType.CREATE) {
                Optional<String> contentOptional = entry.getNewContentRaw();
                String contentRaw = contentOptional.orElse("");

                MessageInformation messageInformation = messageContentMap.getOrDefault(messageId,
                        new MessageInformation(messageId, entry.getMemberId().orElse(null),
                                contentRaw, entry.getTimestamp()));
                messageInformation.setContentRaw(contentRaw);
                messageContentMap.putIfAbsent(messageId, messageInformation);
            }
        }

        Set<Entry<String, MessageInformation>> entrySet = messageContentMap.entrySet();
        for (Entry<String, MessageInformation> entry : entrySet) {
            MessageInformation messageInformation = entry.getValue();
            DivTag divTag = createDivTag(messageInformation, channel.getGuild());
            section = section.with(divTag);
        }

        return section.withId("messages");
    }

    private DivTag createDivTag(MessageInformation information, Guild guild) {
        DiscordBot discordBot = getDiscordBot();
        DatabaseManager databaseManager = discordBot.getDatabaseManager();
        Optional<MemberRecord> optionalMember = information.getMember(databaseManager);

        MemberRecord member = optionalMember.orElse(null);
        String rawContent = information.getContentRaw();
        long timestamp = information.getTimestamp();

        ImgTag imgTag = getImgTag(member).withClass("author-icon");
        DivTag messageTag = getDivTag(guild, member, rawContent, timestamp);
        return div(imgTag, messageTag);
    }

    private ImgTag getImgTag(@Nullable MemberRecord member) {
        String unknownUserIconPNG = ("https://www.sirblobman.xyz/slimy_bot/images/discord_unknown_user.png");
        if (member == null) {
            return img().withSrc(unknownUserIconPNG).withAlt("Avatar for an unknown user.");
        }

        String avatarUrl = member.avatar_url();
        String altString = ("Avatar for user " + member.tag());
        return img().withSrc(avatarUrl).withOnerror(unknownUserIconPNG).withAlt(altString);
    }

    private DivTag getDivTag(Guild guild, @Nullable MemberRecord member, String message, long timestamp) {
        DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss.SSS zzz");
        String timestampDateString = dateFormat.format(new Date(timestamp));
        String timestampString = Long.toString(timestamp);

        String userTag = (member == null ? "Unknown User" : member.tag());
        TimeTag timeTag = time(timestampDateString).withDatetime(timestampString);
        H1Tag h1Tag = h1(userTag).with(timeTag);

        String replaced = replaceMentions(guild, message);
        DomContent fixedMessage = fixLineBreaks(replaced);
        return div(h1Tag, fixedMessage);
    }

    private String replaceMentions(Guild guild, String contentRaw) {
        contentRaw = replaceRoleMentions(guild, contentRaw);
        contentRaw = replaceMemberMentions(guild, contentRaw);
        contentRaw = replaceChannelMentions(guild, contentRaw);
        return contentRaw;
    }

    private String replaceMemberMentions(Guild guild, String content) {
        content = replaceMemberMentions2(guild, content);
        Pattern memberPattern = Pattern.compile("<@(\\d*)>");

        Matcher matcher = memberPattern.matcher(content);
        return matcher.replaceAll(result -> {
            String memberId = result.group(1);
            Member memberById = guild.getMemberById(memberId);
            if (memberById != null) {
                User memberUser = memberById.getUser();
                String tagName = memberUser.getAsTag();
                return Matcher.quoteReplacement("@" + tagName);
            }

            DatabaseManager databaseManager = getDiscordBot().getDatabaseManager();
            MemberRecord memberRecord = databaseManager.getKnownMemberById(memberId);
            if (memberRecord != null) {
                String tagName = memberRecord.tag();
                return Matcher.quoteReplacement("@" + tagName);
            }

            String group = result.group();
            return Matcher.quoteReplacement(group);
        });
    }

    private String replaceMemberMentions2(Guild guild, String content) {
        Pattern memberPattern = Pattern.compile("<@!(\\d*)>");
        Matcher matcher = memberPattern.matcher(content);

        return matcher.replaceAll(result -> {
            String memberId = result.group(1);
            Member memberById = guild.getMemberById(memberId);
            if (memberById != null) {
                User memberUser = memberById.getUser();
                String tagName = memberUser.getAsTag();
                return Matcher.quoteReplacement("@" + tagName);
            }

            DatabaseManager databaseManager = getDiscordBot().getDatabaseManager();
            MemberRecord memberRecord = databaseManager.getKnownMemberById(memberId);
            if (memberRecord != null) {
                String tagName = memberRecord.tag();
                return Matcher.quoteReplacement("@" + tagName);
            }

            String group = result.group();
            return Matcher.quoteReplacement(group);
        });
    }

    private String replaceRoleMentions(Guild guild, String content) {
        Pattern rolePattern = Pattern.compile("<@&(\\d*)>");
        Matcher matcher = rolePattern.matcher(content);

        return matcher.replaceAll(result -> {
            String roleId = result.group(1);
            Role roleById = guild.getRoleById(roleId);
            if (roleById != null) {
                String roleName = roleById.getName();
                return Matcher.quoteReplacement("@" + roleName);
            }

            String group = result.group();
            return Matcher.quoteReplacement(group);
        });
    }

    private String replaceChannelMentions(Guild guild, String content) {
        Pattern channelPattern = Pattern.compile("<#(\\d*)>");
        Matcher matcher = channelPattern.matcher(content);

        return matcher.replaceAll(result -> {
            String channelId = result.group(1);
            GuildChannel guildChannel = guild.getGuildChannelById(channelId);
            if (guildChannel != null) {
                String channelName = guildChannel.getName();
                return Matcher.quoteReplacement("#" + channelName);
            }

            DatabaseManager databaseManager = getDiscordBot().getDatabaseManager();
            ChannelRecord channelRecord = databaseManager.getKnownChannelById(channelId);
            if (channelRecord != null) {
                String channelName = channelRecord.name();
                return Matcher.quoteReplacement("#" + channelName);
            }

            return result.group();
        });
    }

    private DomContent fixLineBreaks(String content) {
        DivTag div = div();

        String[] split = content.split("\n\n");
        for (int i = 0; i < split.length; i++) {
            if (i != 0) {
                div = div.with(br());
            }

            String line = split[i];
            String[] secondSplit = line.split("\n");
            for (String line2 : secondSplit) {
                if (line2.startsWith("> ")) {
                    String quotedText = line2.substring(2);
                    DomContent quoteTag = div(blockquote(quotedText).withClass("markdown"));
                    div = div.with(quoteTag);
                } else if (line2.startsWith("[Embed: ") && line2.endsWith("]")) {
                    int line2Length = line2.length();
                    String embedJson = line2.substring("[Embed: ".length(), line2Length - 1);
                    DomContent embed = parseEmbed(embedJson);
                    div = div.with(embed);
                } else if (line2.startsWith("[Attachment: ") && line2.endsWith("]")) {
                    int line2Length = line2.length();
                    String attachmentJson = line2.substring("[Attachment: ".length(), line2Length - 1);
                    DomContent attachment = parseAttachment(attachmentJson);
                    div = div.with(attachment);
                } else {
                    DivTag markdownDiv = div(line2).withClass("markdown");
                    div = div.with(markdownDiv);
                }
            }
        }

        return div;
    }

    private DomContent parseEmbed(String embedJson) {
        try {
            JSONTokener tokener = new JSONTokener(embedJson);
            JSONObject jsonObject = new JSONObject(tokener);
            DivTag embedDiv = div().withClass("embed");

            int color = 0;
            if (jsonObject.has("color")) {
                color = jsonObject.getInt("color");
            }

            String hex = String.format(Locale.US, "#%06X", color);
            embedDiv = embedDiv.with(div().withStyle("background-color: " + hex + ";").withClass("embed-color"));

            if (jsonObject.has("author")) {
                JSONObject authorObject = jsonObject.getJSONObject("author");
                if (authorObject.has("icon_url")) {
                    String iconUrl = authorObject.getString("icon_url");
                    embedDiv = embedDiv.with(img().withSrc(iconUrl).withClass("embed-image-author"));
                }

                if (authorObject.has("name")) {
                    String name = authorObject.getString("name");
                    if (authorObject.has("url")) {
                        String url = authorObject.getString("url");
                        embedDiv = embedDiv.with(a(strong(name)).withHref(url));
                    } else {
                        embedDiv = embedDiv.with(strong(name));
                    }
                }
            }

            if (jsonObject.has("thumbnail")) {
                JSONObject imageObject = jsonObject.getJSONObject("thumbnail");
                if (imageObject.has("url")) {
                    String url = jsonObject.getString("url");
                    embedDiv = embedDiv.with(img().withSrc(url).withClass("embed-image-thumbnail"));
                }
            }

            if (jsonObject.has("title")) {
                String title = jsonObject.getString("title");
                DomContent titleContent = h4(title).withClass("embed-title");

                if (jsonObject.has("url")) {
                    String url = jsonObject.getString("url");
                    ATag link = a().withHref(url).with(titleContent);
                    embedDiv = embedDiv.with(link);
                } else {
                    embedDiv = embedDiv.with(titleContent);
                }
            }

            if (jsonObject.has("description")) {
                String description = jsonObject.getString("description");
                embedDiv = embedDiv.with(p(description).withClass("embed-description"));
            }

            if (jsonObject.has("image")) {
                JSONObject imageObject = jsonObject.getJSONObject("image");
                if (imageObject.has("url")) {
                    String url = jsonObject.getString("url");
                    embedDiv = embedDiv.with(img().withSrc(url).withClass("embed-image"));
                }
            }

            if (jsonObject.has("fields")) {
                JSONArray fields = jsonObject.getJSONArray("fields");
                embedDiv = appendFields(embedDiv, fields);
            }

            if (jsonObject.has("footer")) {
                DivTag footerDiv = div().withClass("embed-footer");
                JSONObject footerObject = jsonObject.getJSONObject("footer");

                if (footerObject.has("icon_url")) {
                    String iconUrl = footerObject.getString("icon_url");
                    footerDiv = footerDiv.with(img().withSrc(iconUrl).withClass("embed-footer-icon"));
                }

                if (footerObject.has("text")) {
                    String text = footerObject.getString("text");
                    footerDiv = footerDiv.withText(" " + text);
                }

                embedDiv = embedDiv.with(footerDiv);
            }

            if (jsonObject.has("timestamp")) {
                String timestamp = jsonObject.getString("timestamp");
                embedDiv = embedDiv.with(time(timestamp).withDatetime(timestamp));
            }

            return embedDiv;
        } catch (JSONException ex) {
            return div("Embed Error: " + ex.getMessage());
        }
    }

    private DivTag appendFields(DivTag embedDiv, JSONArray fields) {
        DivTag fieldsTag = div().withClass("embed-fields");

        int fieldsLength = fields.length();
        for (int i = 0; i < fieldsLength; i++) {
            try {
                JSONObject jsonObject = fields.getJSONObject(i);
                boolean inline = (jsonObject.has("inline") && jsonObject.getBoolean("inline"));
                String name = jsonObject.getString("name");
                String text = jsonObject.getString("text");

                DivTag fieldTag;
                if (inline) {
                    fieldTag = div().withClass("embed-field-inline");
                } else {
                    fieldTag = div().withClass("embed-field");
                }

                fieldTag = fieldTag.with(strong(name), br(), span(text));
                fieldsTag = fieldsTag.with(fieldTag);
            } catch (JSONException ignored) {
            }
        }

        embedDiv = embedDiv.with(fieldsTag);
        return embedDiv;
    }

    private DomContent parseAttachment(String attachmentJson) {
        try {
            JSONTokener tokener = new JSONTokener(attachmentJson);
            JSONObject jsonObject = new JSONObject(tokener);
            DivTag attachmentDiv = div().withClass("attachment");

            String fileName = jsonObject.getString("file_name");
            String attachmentURL = jsonObject.getString("attachment_url");

            attachmentDiv = attachmentDiv.with(a().with(strong("Attachment: ")).withText(fileName)
                    .withHref(attachmentURL));
            return attachmentDiv;
        } catch (JSONException ex) {
            return div("Attachment Error: " + ex.getMessage());
        }
    }
}
