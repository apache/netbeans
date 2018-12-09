#!/bin/sh -x
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.
#
# Contributor(s):


verifyClassName=
verifyClassPath=
doVerify=0

#it slows down building
#if [ -n "$2" ] && [ -n "$3" ] ; then
#   verifyClassName="$2"
#   verifyClassPath="$3"
#   doVerify=1
#fi

javaPath="/Library/Java/JavaVirtualMachines/1.6.0_65-b14-462.jdk/Contents/Home"
unpackCommand="$javaPath/bin/unpack200"
javaCommand="$javaPath/bin/java"
packCommand="$javaPath/bin/pack200"

verify(){
  filenamePacked="$1"
  filenameSource="$2"
  tmpFile="$2.tmp"
  $unpackCommand "$1" "$tmpFile"
  result=1
  if [ 0 -eq $? ] ; then
	$javaCommand -cp "$verifyClassPath" "$verifyClassName" "$tmpFile" >/dev/null
	result=$?
  fi

  if [ -f "$tmpFile" ] ; then
      rm "$tmpFile"
  fi

  return $result
}

for f in `find $1 -name "*.jar"`
do
  bn=`basename $f`
  if  [ "$bn" != "jhall.jar" ] && [ "$bn" != "derby.jar" ] && [ "$bn" != "derbyclient.jar" ]
  then
    if [ ! -z "$dont_pack_anything" ] && [ "$dont_pack_anything" == "y" ] ; then
        echo "Skipping packing of $f"
        continue
    fi
    if [ ! -z "$dont_pack_localization_jars" ] && [ "$dont_pack_localization_jars" == "y" ]; then
        if [ ! -z `echo $f | grep "/locale/"` ]; then
            echo "Skipping packing of localization jars: $f"
            continue
        fi
    fi
    if [ -f "$f.pack" ] || [ -f "$f.pack.gz" ] ; then 
        echo "Packed file $f.pack(.gz) exists, skipping packing of the original file $f"
        continue
    fi
    if [ -f `echo $f | sed 's/.jar/.jad/'` ] ; then
        echo "Jar Descriptor (.jad) exists, skipping packing of the original file $f"
        continue
    fi
    if [ 2 -eq `unzip -l "$f" 2>/dev/null | grep "META-INF/" | sed "s/.*META-INF\///g" | grep "\.SF\|\.RSA\|\.DSA"| wc -l` ] ; then
        echo "Jar file $f is signed, skipping packing"
        continue
    fi

    echo Packing $f
    $packCommand -J-Xmx256m -g $f.pack $f
    if [ 0 -eq $? ] ; then
        res=0
        if [ 1 -eq $doVerify ] ; then
	    verify $f.pack $f
            res=$?
	fi

        if [ 0 -eq $res ] ; then
            chmod `stat -f %Lp $f` $f.pack && touch -r $f $f.pack
            rm $f
        else
            echo Error verification packed jar : $f
	    rm $f.pack
        fi
    else
	if [ -f $f.pack ] ; then
	    echo Error packing jar : $f
	    rm $f.pack
	fi
    fi
  fi
done

