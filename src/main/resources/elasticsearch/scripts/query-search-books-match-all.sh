#!/usr/bin/env bash

curl -i -H 'Content-Type: application/json' http://localhost:9200/books/books/_search?pretty -d '
{
    "size": 10,
    "query": {
        "match_all" : {
        }
    }
}'