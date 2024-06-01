FROM openjdk:11

RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/cache/apt/archives /var/lib/apt/lists/*

RUN mkdir /tracker && \
    cd /tracker && \
    git clone https://github.com/detec14/tracker-bot.git && \
    cd tracker-bot && \
    mvn clean compile assembly:single

ENTRYPOINT ["java", "-jar", "/tracker/tracker-bot/target/tracker-bot-jar-with-dependencies.jar"]
