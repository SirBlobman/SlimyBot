# Slimy Bot

A custom bot that is used on SirBlobman's Discord.
This bot uses the JDA library created by dv8tion.

# JDA

JDA stands for "Java Discord API". You can find out more information by clicking the link below:  
[DV8FromTheWorld/JDA](https://github.com/DV8FromTheWorld/JDA)

# Features

- Single server support.
- Configurable channels and bot owner.
- Console commands if enabled and supported by the OS.
- FAQ, Ping, Voter, User information, and Developer Information commands.
- Ticket system for support teams.
- Magic eight ball command for decision making.

## How to compile?

To compile this project, use the gradle wrapper:

### Unix

```shell
./gradlew build
```

### Windows

```shell
gradlew.bat build
```

# How to use?

To use this project, run the main jar with Java 17

```shell
java -Xms128M -Xmx128M -jar target/SlimyBot-VERSION.jar
```
