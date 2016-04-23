FROM adzerk/boot-clj

EXPOSE 8080

WORKDIR /app
COPY . /app

ENTRYPOINT ["/usr/bin/boot", "run", "wait"]
