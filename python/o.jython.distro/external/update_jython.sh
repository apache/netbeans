#!/bin/sh

version=2.7.0
repository=https://bitbucket.org/jython/jython
target=jython-${version}.zip
dist=jython-${version}
location=jython

hg clone --rev v${version} $repository $location
cd $location

# Note - need both ant calls
ant
ant jar-complete

rm -f ../$target
mv dist $dist
curl https://pypi.python.org/packages/source/s/setuptools/setuptools-15.0.zip -O
unzip setuptools-15.0.zip
cd setuptools-15.0
../$dist/bin/jython setup.py install
cd ..
zip -r ../$target $dist/Lib $dist/bin/ $dist/jython.jar $dist/registry 
cd ..
echo "Updating binaries-list"
echo `openssl dgst -sha1 $target | awk '{ print toupper($2) }'` $target > binaries-list
echo "Cleaning up"
rm -rf "$location"
echo "Done."
