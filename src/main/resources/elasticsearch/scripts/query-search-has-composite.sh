#!/usr/bin/env bash

curl -i http://localhost:9200/catalog/books/_search?pretty -d '
{
   "size": 10,
   "_source": [ "title", "publisher" ],
   "query": {
       "bool" : {
          "must" : [
              {
                  "range" : {
                      "rating" : { "gte" : 4 }
                  }
              },
              {
                  "has_child" : {
                      "type" : "authors",
                      "query" : {
                          "term" : {
                              "last_name" : "Gormley"
                          }
                      }
                  }
              },
              {
                  "nested": {
                      "path": "categories",
                      "query" : {
                          "match": {
                              "categories.name" : "search"
                          }
                      }
                  }
              }
          ]
       }
    }
}'