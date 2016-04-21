FROM pandeiro/lein:latest

COPY . /app

RUN lein deps 



