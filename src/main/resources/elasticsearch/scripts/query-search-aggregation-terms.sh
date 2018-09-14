#!/usr/bin/env bash

curl -i -H 'Content-Type: application/json' http://localhost:9200/books/books/_search?pretty -d '
{
   "query": {
        "match" : {
            "description" : "elasticsearch"
        }
    },
    "aggs" : {
        "publisher" : {
            "terms" : { "field" : "publisher" }
        }
    }
}'
