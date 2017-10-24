<a href="https://www.elastic.co" target="_blank"><img src="https://static-www.elastic.co/assets/blt45b0886c90beceee/logo-elastic.svg" width=300/></a> 
# elasticsearch tutorial

## prerequisites:
- install <a href="https://docs.docker.com/engine/installation/" target="_blank">Docker</a> & <a href="https://docs.docker.com/compose/install/" target="_blank">Docker Compose</a>
- install <a href="https://httpie.org/" target="_blank">httpie</a> :: scripts for checking elasticsearch status use it

(linux)
- modify vm.max_map_count in /etc/sysctl.conf:
```sudo vi /etc/sysctl.conf```
- add at the end 'vm.max_map_count=262144'
or
- for a runtime change:
```sudo sysctl -w vm.max_map_count=262144```

## technologies:
- docker && <a href="https://docs.docker.com/compose/reference/overview/" target="_blank">docker-compose</a>
- elasticsearch stack (elasticsearch, logstash, kibana) + plugin elasticsearch head
  - elasticsearch version :: 5.6.3
- spring boot
- junit

## elasticsearch stack:
- Start cluster `docker-compose up -d`.
- Stop cluster `docker-compose down`. Data volumes will persist, so it’s possible to start the cluster again with the same data.
- Destroy cluster and the data volumes `docker-compose down -v`.

## elasticsearch APIs:
- transport :: used in exposed endpoints
- rest :: tested in [ElasticSearchRestApiTest](https://github.com/jgb11/elasticsearch-tutorial/blob/feature/Readme_improve/src/test/java/jgb/elasticsearch/main/ElasticsearchRestApiTest.java)

## endpoints
- **GET** _/elastic/health_ :: check elasticsearch health
- _/elastic/index_ :: everything related with index management
  - **GET** _/{indexName}/exists_ :: check if {indexName} exists
  - **POST** _/catalog_ :: create index catalog with this mappings:
```json
{ 
  "settings": {
    "index" : {
      "number_of_shards" : 5, 
      "number_of_replicas" : 2 
    }
  },
  "mappings": {
    "books": {
      "_source" : {
        "enabled": true
      },
      "properties": {
        "title": { "type": "text" },
        "categories" : {
          "type": "nested",
          "properties" : {
            "name": { "type": "text" }
          }
        },
        "publisher": { "type": "keyword" },
        "description": { "type": "text" },
        "published_date": { "type": "date" },
        "isbn": { "type": "keyword" },
        "rating": { "type": "byte" }
       }
   },
   "authors": {
     "properties": {
       "first_name": { "type": "keyword" },
       "last_name": { "type": "keyword" }
     },
     "_parent": {
        "type": "books"
      }
    }
  }
}
```

- _/elastic/books_ :: everything related with books
  - **GET** _/count_ :: count existing books
  - **GET** _/count/rating/{rating}_ :: count books with indicated {rating}
  - **POST** _/demo_ :: create demo book in a single operation
- _/elastic/authors_ :: everything related with authors
  - **POST** _/demo_ :: create demo authors with bulk API
  
## scripts
Inside folder [resources/elasticsearch/scripts](https://github.com/jgb11/elasticsearch-tutorial/tree/feature/Readme_improve/src/main/resources/elasticsearch) there are utilities scripts to:
- check elasticsearch status
- create sample indexes
- perform some example queries 