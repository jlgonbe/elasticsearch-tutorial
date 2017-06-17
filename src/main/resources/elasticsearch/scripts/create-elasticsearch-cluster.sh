#!/usr/bin/env bash
echo "deprecated! use docker/docker-compose.yml"

readonly ELASTICSEARCH_VERSION=5.4.0
readonly TRANSPORT_HOST=0.0.0.0
readonly CLUSTER_NAME=es-catalog

docker run -d --name elasticsearch1 -p 9200:9200 -p 9300:9300 elasticsearch:5.4.0 -E cluster.name=es-catalog -E node.name=elasticsearch1 -E transport.host=0.0.0.0 -E bootstrap.memory_lock=true -E "ES_JAVA_OPTS=-Xms512m -Xmx512m"
# sleep 300
docker run -d --name elasticsearch2 --link=elasticsearch1 elasticsearch:5.4.0 -E cluster.name=es-catalog -E node.name=elasticsearch2 -E transport.host=0.0.0.0 -E bootstrap.memory_lock=true -E "ES_JAVA_OPTS=-Xms512m -Xmx512m" -E discovery.zen.ping.unicast.hosts=elasticsearch1
# sleep 300
docker run -d --name elasticsearch3 --link=elasticsearch1 elasticsearch:5.4.0 -E cluster.name=es-catalog -E node.name=elasticsearch3 -E transport.host=0.0.0.0 -E bootstrap.memory_lock=true -E "ES_JAVA_OPTS=-Xms512m -Xmx512m" -E discovery.zen.ping.unicast.hosts=elasticsearch1

docker container ls -a