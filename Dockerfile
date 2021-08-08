FROM clojure:openjdk-17-tools-deps-alpine

COPY . /app
WORKDIR /app

RUN clojure

ENTRYPOINT ["clojure", "-X", "core/main"]
