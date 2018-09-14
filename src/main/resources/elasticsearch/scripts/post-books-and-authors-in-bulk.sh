#!/usr/bin/env bash

http POST http://localhost:9200/_bulk < ../books-and-authors-bulk.json
