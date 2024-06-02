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
Be aware that the bot uses an external service to render the STFC galaxy maps (http://stfc-map.iapns.com:3130). If the service is not available, it will only display the system names to be checked.

```
˜# docker run -dti --name bot \
    -v <path-to-config>:/tracker/tracker-bot/assets/config.json \
    tracker-bot:latest
```
