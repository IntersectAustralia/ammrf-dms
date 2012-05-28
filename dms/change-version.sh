#!/bin/bash
FROM=$1
TO=$2
if [ -z "$FROM" -o -z "$TO" ]; then
   echo "usage $0" '<from> <to>'
   echo "e.g. \$ $0 0.7.0 0.8.0"
   echo "note: execute in parent folder"
   exit 1
fi
echo find . -name pom.xml -exec grep -l "$FROM" {} \; -exec sed -i -e "s/$FROM/$TO/g" {} \; -print
find . -name pom.xml -exec grep -q "$FROM" {} \; -exec sed -i -e "s/$FROM/$TO/g" {} \; -print
