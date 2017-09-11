 # DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 #
 # Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 #
 # Portions Copyrighted 2012 Sun Microsystems, Inc.

set -x

DIRNAME=`dirname $0`
cd ${DIRNAME}
source init.sh

cd  ${DIST}/zip

nbsrc_file=`ls netbeans*platform-src.zip`
zip_file=`basename $nbsrc_file -platform-src.zip`.zip

cd  $NB_ALL

cd nbbuild
cp ${DIST}/zip/${zip_file} .
unzip -oq ${zip_file}
cd ..

#Build napackaged NBMs for stable UC - IDE + UC-only
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f nbbuild/build.xml build-nbms -Dcluster.config=stableuc -Duse.pack200=false -Dbase.nbm.target.dir=${DIST}/uc-unpackaged -Dkeystore=$KEYSTORE -Dstorepass=$STOREPASS -Dbuild.compiler.debuglevel=source,lines
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build unpackaged all stable UC NBMs"
    exit $ERROR_CODE;
fi

cd nbbuild
Build catalog for unpackaged NBMs
ant -Dbuildnum=$BUILDNUM -Dbuildnumber=$BUILDNUMBER -f build.xml generate-uc-catalog -Dnbms.location=${DIST}/uc-unpackaged -Dcatalog.file=${DIST}/uc-unpackaged/catalog.xml
ERROR_CODE=$?

if [ $ERROR_CODE != 0 ]; then
    echo "ERROR: $ERROR_CODE - Can't build stable UC catalog for unpackaged NBMs"
    exit $ERROR_CODE;
fi
cd ..

