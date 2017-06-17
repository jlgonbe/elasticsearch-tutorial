#!/usr/bin/env bash

docker container rm -f elasticsearch1 elasticsearch2 elasticsearch3
docker image rm -f elasticsearch:5.4.0
docker volume rm -f docker_esdata1 docker_esdata2 docker_esdata3
docker network rm docker_esnet

docker container ls -a
docker image ls
docker volume ls
docker network ls