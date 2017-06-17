#!/usr/bin/env bash

curl -i http://localhost:9200/catalog/books/_delete_by_query?pretty -d '
{
   "query": {
      "bool": {
          "must": [
              { "range" : { "rating" : { "lt" : 3 } } }
          ],
          "filter": [
             { "term" :  { "publisher" : "Manning" } }
          ]
      }
   }
}'