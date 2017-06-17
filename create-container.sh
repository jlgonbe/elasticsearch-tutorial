mvn clean package docker:build

docker run -d -p 8086:8086 \
--name elastic_tutorial \
--net elasticstack_esnet \
jlgonzalezbeltran/elasticsearch-test