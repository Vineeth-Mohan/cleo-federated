#!/bin/sh

if [ $# -eq 0 ]; then
    echo "Usage:"
    echo "  $0 [xml file] ..."
    echo "Example:"
    echo "  $0 dat/nasdaq-company-list.xml"
    exit 1;
fi
echo $1
    curl -v -X POST -H "Content-type: application/xml" -H "Accept: application/xml" http://localhost:8080/cleo-primer/rest/elements/$1 --data-binary @$2

