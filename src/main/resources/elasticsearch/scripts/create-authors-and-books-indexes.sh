#!/usr/bin/env bash

http PUT http://localhost:9200/authors < ../authors-index.json
http PUT http://localhost:9200/books < ../books-index.json
