#!/usr/bin/env bash

echo "authors"
http POST http://localhost:9200/authors/_open

echo "books"
http POST http://localhost:9200/books/_open
