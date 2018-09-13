#!/usr/bin/env bash

echo "authors"
http http://localhost:9200/authors/_settings
http http://localhost:9200/authors/_mapping

echo "books"
http http://localhost:9200/books/_settings
http http://localhost:9200/books/_mapping
