#!/usr/bin/env bash

curl -i -H 'Content-Type: application/json' http://localhost:9200/books/books/_delete_by_query?pretty -d '
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