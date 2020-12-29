#!/bin/zsh
# Script which updates the preindexed files
# See INDICES.txt for more info. 
#
# In advance, create a Python project. Then configure the $INDEXING_PROJECT below
# to the full path to this project. It will then be used for indexing purposes.
#
# To try debugging this, set up the properties in GsfModuleInstaller.java's restored method, for example like this:
# System.setProperty("gsf.preindexing", "true");
# System.setProperty("netbeans.full.hack=true", "true");
# System.setProperty("python.interpreter", "/Users/user/dev/python/install/bin/python");
# System.setProperty("gsf.preindexing.projectpath", "/Users/user/NetBeansProjects/PythonPreindexProject");
#


#
# Configure the following parameters:
#
NBHGHOME=~/netbeans/hg/main-silver
NATIVEPYTHONHOME=/usr
VMFLAGS=-J-Xmx1024m
INDEXING_PROJECT=/Users/user/NetBeansProjects/NewPythonProject

# You probably don't want to change these:
NB=$NBHGHOME/nbbuild/netbeans/bin/netbeans
# Location of a Python interpreter which contains lots of gems
NATIVEPYTHON=$NATIVEPYTHONHOME/bin/python
SCRATCHFILE=/tmp/native.zip
USERDIR=/tmp/preindexing
TMP_BINARIES=/tmp/binaries-list

#############################################################################################
# No user-configurable parts beyond this point...

ar="$1"
if test "$ar" = "" ; then
  ar="both"
fi

#export PATH=$NATIVEPYTHONHOME/bin:$PATH
CLUSTERS=$NBHGHOME/nbbuild/netbeans
PYTHON=$CLUSTERS/python
GSF=$CLUSTERS/gsf
unset GEM_HOME

if test ! -f $CLUSTERS/extra/modules/org-netbeans-modules-gsf-tools.jar ; then
  echo "You should build contrib/gsf.tools first, which will automate the indexing process within the IDE when this script is run."
  exit 0
fi

find $CLUSTERS -name "netbeans-index*.zip" -exec rm {} \;
rm -rf $PYTHON/preindexed/lib
rm -rf $GSF/preindexed-javascript/lib

if test "$ar" = "local" -o  "$ar" = "both" ; then

rm -rf $USERDIR
$NB $VMFLAGS -J-Dgsf.preindexing=true -J-Dpython.computeindex -J-Dgsf.preindexing.projectpath=$INDEXING_PROJECT -J-Dnetbeans.full.hack=true --userdir $USERDIR

# Pack preindexed.zip
#cd $CLUSTERS
cd $PYTHON
rm -f preindexed-python.zip
find . -name "netbeans-index-*php*.zip" -exec rm {} \;
find . -name "netbeans-index-*groovy*.zip" -exec rm {} \;
find . -name "netbeans-index-*ruby*.zip" -exec rm {} \;
# Include empty JavaScript so IDE doesn't have to regenerate it,
# until GSF supports looking only for one language for boot classpaths
#find . -name "netbeans-index-*javascript*.zip" -exec rm {} \;
zip -r preindexed-python.zip `find . -name "netbeans-index-*"`
mv preindexed-python.zip $NBHGHOME/python.editor/external/preindexed.zip
rm -f preindexed-python.zip

cd $PYTHON
#rm $NBHGHOME/python.editor/external/pythonstubs-2_6_1.egg
#zip -r $NBHGHOME/python.editor/external/pythonstubs-2_6_1.egg pythonstubs

fi

# NATIVE
if test "$ar" = "native" -o  "$ar" = "both" ; then

#find $NATIVEPYTHONHOME . -name "netbeans-index*.zip" -exec rm {} \;
#rm -rf $USERDIR
#$NB $VMFLAGS -J-Dgsf.preindexing=true -J-Dpython.computeindex -J-Dgsf.preindexing.projectpath=$INDEXING_PROJECT -J-Dnetbeans.full.hack=true --userdir $USERDIR -J-Dpython.interpreter=$NATIVEPYTHON
#
## Go to the native installation:
## Ruby
#cd $NATIVEPYTHONHOME
#rm -f $SCRATCHFILE
#zip -r $SCRATCHFILE `find . -name "netbeans-index-*python*.zip"` 
#cd $PYTHON
#rm -rf preindexed
#mkdir preindexed
#cd preindexed
#unzip $SCRATCHFILE
#cd ..
#rm -f $NBHGHOME/python.editor/external/preindexed-native.zip
#find . -name "netbeans-index-*php*.zip" -exec rm {} \;
#find . -name "netbeans-index-*groovy*.zip" -exec rm {} \;
#find . -name "netbeans-index-*ruby*.zip" -exec rm {} \;
#find . -name "netbeans-index-*javascript*.zip" -exec rm {} \;
#zip -r $NBHGHOME/python.editor/external/preindexed-native.zip preindexed/
#

cd $NATIVEPYTHONHOME
rm -f $SCRATCHFILE
echo "**************"
echo "Indexing complete. There should be no output after this:"

fi




# Update binaries-list -- manual upload is still necessary!
# Python
rm -f $TMP_BINARIES
cat $NBHGHOME/python.editor/external/binaries-list | sed '/preindexed.zip/d' | sed '/pythonstubs-2_6_1.egg/d' | sed '/preindexed-native.zip/d' > $TMP_BINARIES
echo `openssl dgst -sha1 $NBHGHOME/python.editor/external/pythonstubs-2_6_1.egg | awk '{ print toupper($2) }'` pythonstubs-2_6_1.egg >> $TMP_BINARIES
echo `openssl dgst -sha1 $NBHGHOME/python.editor/external/preindexed.zip | awk '{ print toupper($2) }'` preindexed.zip >> $TMP_BINARIES
#echo `openssl dgst -sha1 $NBHGHOME/python.editor/external/preindexed-native.zip | awk '{ print toupper($2) }'` preindexed-native.zip >> $TMP_BINARIES
mv $TMP_BINARIES $NBHGHOME/python.editor/external/binaries-list
