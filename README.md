# elasticsearch tutorial

install httpie :: scripts for checking elasticsearch status use it

technologies:
- docker && docker-compose
- spring boot
- elasticsearch stack (elasticsearch, logstash, kibana) + plugin elasticsearch head
- junit

elasticsearch stack:
- To start the cluster ```docker-compose up -d```.
- To stop the cluster ```docker-compose down```. Data volumes will persist, so it’s possible to start the cluster again with the same data.
- To destroy the cluster and the data volumes just type ```docker-compose down -v```.

elasticsearch version :: 5.4.1

elasticsearch APIs:
- transport
- rest
