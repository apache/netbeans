#!/bin/bash

#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU General Public
# License Version 2 only ("GPL") or the Common Development and Distribution
# License("CDDL") (collectively, the "License"). You may not use this file except in
# compliance with the License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
# License for the specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header Notice in
# each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
# designates this particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that accompanied this code.
# If applicable, add the following below the License Header, with the fields enclosed
# by brackets [] replaced by your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
# 
# Contributor(s):
# 
# The Original Software is NetBeans. The Initial Developer of the Original Software
# is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
# Rights Reserved.
# 
# If you wish your version of this file to be governed by only the CDDL or only the
# GPL Version 2, indicate your decision by adding "[Contributor] elects to include
# this software in this distribution under the [CDDL or GPL Version 2] license." If
# you do not indicate a single choice of license, a recipient has the option to
# distribute your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above. However, if you
# add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
# option applies only if the new code is made subject to such option by the copyright
# holder.
# 

set -x -e

if [ -z "$1" ] || [ -z "$2" ] ; then
    echo "usage: $0 zipdir basename"
    echo ""
    echo "zipdir is the dir which contains the zip distros, e.g. nbbuild/dist"
    echo "basename is the distro filename prefix, e.g. netbeans-hudson-trunk-2464"
    echo "zipdir should contain <basename>.zip, <basename>-java.zip, <basename>-ruby.zip,..."
    echo Requires GLASSFISH_LOCATION, TOMCAT_LOCATION, OPENESB_LOCATION, JBICORE_LOCATION to be set. 
    exit 1
fi

if [ -n "$3" ]; then
   INSTRUMENT_SH=$3
fi

zipdir=$1
basename=$2

progdir=`dirname $0`
cd $progdir
progdir=`pwd`

dmgname=$basename
# Remove build number from DMG name
#dmgname=`echo "$dmgname" | sed "s/-[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]//g"`

instrument_build() {
   DIR=$1
   $INSTRUMENT_SH $DIR $progdir/../../emma/emma_filter $progdir/../../emma/emma.jar
#   mkdir $DIR/emma-lib
   chmod a+w $DIR/emma-lib
   #cp $progdir/../../emma/emma_filter $DIR/emma-lib/netbeans_coverage.ec
   cp $progdir/../../emma/emma.jar $DIR/emma-lib/
   sed -i -e "s/^netbeans_default_options=/netbeans_default_options=\"--cp:p $\{NETBEANS_HOME\}\/emma-lib\/emma.jar -J-Demma.coverage.file=\$\{NETBEANS_HOME\}\/emma-lib\/netbeans_coverage.ec -J-Dnetbeans.security.nocheck=true/" $DIR/etc/netbeans.conf
}


buildnum=""`find "$zipdir" -name '*[0-9].zip'`
buildnum="`expr $buildnum : '.*-\(.*\)\..*'`" 
installdir="NetBeans 6.1 Dev $buildnum"

ant -f $progdir/build.xml distclean

# build GF package.  The GF dir image must already exists as $progdir/glassfish/glassfish
# ie after running java -Xmx256m -jar glassfish-installer.jar but before running ant -f setup.xml

#ant -f $progdir/glassfish/build.xml distclean build-pkg

# full download

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename.zip
# remove uml, it has serious performance problem on Mac
rm -rf $progdir/build/netbeans/uml*
# remove mobility, there is no WTK on Mac
rm -rf $progdir/build/netbeans/mobility*
# copy over GlassFish.pkg
#mkdir -p $progdir build/pkg
#rsync -a $progdir/glassfish/build/pkg/ $progdir/build/pkg/
#instrument
if [ -n "$INSTRUMENT_SH" ]; then
    instrument_build $progdir/build/netbeans
fi

# build dmg
ant -f $progdir/build.xml -Ddmgname=_$dmgname-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="$installdir" build-dmg -Dglassfish_location="$GLASSFISH_LOCATION" -Dtomcat_location="$TOMCAT_LOCATION" -Dopenesb_location="$OPENESB_LOCATION" -Djbicore_location="$JBICORE_LOCATION" -Dnetbeans_license_file="$progdir/licenses/NetBeans_6_Beta_1_Global_License.txt"

# javaee

ant -f $progdir/build.xml clean
mkdir $progdir/build
unzip -d $progdir/build $zipdir/$basename-javaee.zip
# copy over GlassFish.pkg
#mkdir -p $progdir build/pkg
#rsync -a $progdir/glassfish/build/pkg/ $progdir/build/pkg/
if [ -n "$INSTRUMENT_SH" ]; then
    instrument_build $progdir/build/netbeans
fi
ant -f $progdir/build.xml -Ddmgname=_$dmgname-javaee-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="$installdir" build-dmg  -Dglassfish_location="$GLASSFISH_LOCATION" -Dtomcat_location="$TOMCAT_LOCATION" -Dnetbeans_license_file="$progdir/licenses/NetBeans_6_Beta_1_WebAndJavaEE.txt"


# all others

for pkg in javase ruby cpp php html; do
    dmg_postfix=$pkg
    license_file=pkg/license.txt
    if [ $pkg = cpp ]
    then
      license_file="licenses/NetBeans_6_Beta_1_CC++.txt"
    else
      if [ $pkg = javase ]
      then
        license_file="licenses/NetBeans_6_Beta_1_Java_SE.txt"
      else
        if [ $pkg = ruby ]
        then
          license_file="licenses/NetBeans_6_Beta_1_Ruby.txt"
        fi
      fi
    fi
    ant -f $progdir/build.xml clean
    mkdir $progdir/build
    unzip -d $progdir/build $zipdir/$basename-$pkg.zip

    if [ -n "$INSTRUMENT_SH" ]; then
	instrument_build $progdir/build/netbeans
    fi
    ant -f $progdir/build.xml -Ddmgname=_$dmgname-$dmg_postfix-macosx.dmg -Dnb.dir=$progdir/build/netbeans -Dnetbeans.appname="$installdir" build-dmg -Dnetbeans_license_file="$progdir/$license_file"
done

