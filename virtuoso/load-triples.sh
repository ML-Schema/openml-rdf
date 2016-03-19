#!/bin/bash
cp -r ../rdf /usr/local/Cellar/virtuoso/7.1.0/share/virtuoso/vad/
isql -input load-triples.sql