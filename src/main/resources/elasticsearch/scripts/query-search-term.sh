#!/usr/bin/env bash

curl -i http://localhost:9200/catalog/books/_search?pretty -d '
{
   "size": 10,
   "_source": [ "title" ],
   "query": {
        "term" : {
            "publisher" : "Manning"
        }
    }
}' 