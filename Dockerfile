FROM ubuntu:latest
LABEL authors="birun"

ENTRYPOINT ["top", "-b"]