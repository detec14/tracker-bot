HOW-TO
======

This repository uses Docker to build a container to the application.
Run the following commands to build:

```
˜# cd .docker
˜# docker build -t tracker-bot .
```

In order to use Discord API, you need a token. Request yours [here](https://discordapp.com/developers/applications/).
Provide the following permissions:
- Send messages
- Embed links
- Attach files
- Add reactions

Create a file called `config.json` containing the token for the bot.

```
{
    "bot-token" : "<bot-token>"
}
```

Now start the container.
Provide two volume mounts: the `config.json` file and a directory where the map rendering service stores the images (make sure permissions are properly set).

```
˜# docker run -dti --name bot \
    -v <path-to-config>:/tracker/tracker-bot/assets/config.json \
    -v <path-to-renderer-output>:/maps \
    tracker-bot:latest
```
