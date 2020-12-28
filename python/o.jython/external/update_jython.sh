#!/bin/sh

repository=https://bitbucket.org/jython/jython

# Name of jar file we're creating from the jython.jar
target=jython-parser.jar

# Temp build location
location=tmp

echo "fetching sources"
rm -rf "$location"
hg clone "$repository" "$location"
cd "$location"

# Note - need both ant calls
echo "Building jar"
ant
ant jar-complete

cd ..
mv "$location"/dist/jython.jar "$target"

echo "Updating binaries-list"
echo $(openssl dgst -sha1 "$target" | awk '{ print toupper($2) }') "$target" > binaries-list

echo "Cleaning up"
rm -rf "$location"

echo "Done."
