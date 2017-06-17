#!/usr/bin/env bash

curl -i http://localhost:9200/catalog/books/_search?pretty -d '
{
   "size": 10,
   "_source": [ "title" ],
   "query": {
       "has_child" : {
            "type" : "authors",
            "inner_hits" : {
                "size": 5
            },
            "query" : {
                "term" : {
                    "last_name" : "Gormley"
                }
            }
        }
    }
}'