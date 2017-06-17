#!/usr/bin/env bash

curl -i http://localhost:9200/catalog/books/_search?pretty -d '
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

curl -i http://localhost:9200/catalog/books/_search?pretty -d '
{
  "aggs" : {
      "authors": {
        "children": {
          "type" : "authors"
        },
        "aggs": {
          "top-authors": {
            "terms": {
            "script" : {
              "inline": "doc['first_name'].value + ' ' + doc['last_name'].value",
              "lang": "painless"
            },
            "size": 10
          }
        }
      }
    }
  }
}'