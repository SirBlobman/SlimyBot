# Slimy Bot

A custom bot that is used on [SirBlobman's Discord](https://discord.gg/XMq2agT).

## JDA

This bot uses the JDA library. JDA stands for "Java Discord API".
You can find out more information by checking out their repository:
[discord-jda/JDA](https://github.com/discord-jda/JDA)

## Features

- Single server support.
- Configurable channels and bot owner.
- Console commands if enabled and supported by the OS.
- FAQ, Ping, Voter, User information, and Developer Information commands.
- Ticket system for support teams.
- Magic eight-ball command for decision-making.

## Requirements

- Java 21
- Internet connection to login and receive data from Discord.
- MariaDB database running v10.11 or higher.

## Developers

To compile this project, use the gradle wrapper:

### Unix

```shell
./gradlew build
```

### Windows

```bat
gradlew.bat build
```

## Usage

To use this project, run the main jar with Java 17

```shell
java -Xmx256M -jar target/SlimyBot-3.0.0.jar
```

The first time you run the bot, you will receive an error.
You need to configure the files and your Discord bot token, then run the start command again.
