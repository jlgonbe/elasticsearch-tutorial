[<img src="https://static-www.elastic.co/assets/blt45b0886c90beceee/logo-elastic.svg" style="width: 200px;"/>](https://www.elastic.co) 
# elasticsearch tutorial

## prerequisites:
- install [Docker](https://docs.docker.com/engine/installation/) & [Docker Compose](https://docs.docker.com/compose/install/)
- install [httpie](https://httpie.org/) :: scripts for checking elasticsearch status use it

## technologies:
- docker && [docker-compose](https://docs.docker.com/compose/reference/overview/ "docker-compose CLI")
- elasticsearch stack (elasticsearch, logstash, kibana) + plugin elasticsearch head
  - elasticsearch version :: 5.4.1
- spring boot
- junit

## elasticsearch stack:
- Start cluster `docker-compose up -d`.
- Stop cluster `docker-compose down`. Data volumes will persist, so it’s possible to start the cluster again with the same data.
- Destroy cluster and the data volumes `docker-compose down -v`.

## elasticsearch APIs:
- transport :: used in exposed ``_````_``endpoints
- rest :: tested in ElasticSearchRestApiTest

## endpoints
- **GET** _/elastic/health_ :: check elasticsearch health
- _/elastic/index_ :: everything related with index management
  - **GET** _/{indexName}/exists_ :: check if {indexName} exists
  - **POST** _/catalog_ :: create index catalog with this configuration:
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
- /elastic/authors :: everything related with authors
  - **POST** _/demo_ :: create demo authors with bulk API