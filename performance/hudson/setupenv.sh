#!/bin/bash -x

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2011, 2016 Oracle and/or its affiliates. All rights reserved.
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

if test ! -e /space/hudsonserver/master 
then

cd $performance/j2se

buildnum=`cat "$reposdir"/build.number`
str1="<property name=\"perftestrun.buildnumber\" value=\"$buildnum\"/>"
str2="<property name=\"env.BUILD_NUMBER\" value=\"`echo $BUILD_NUMBER`\" />"
str3="<property name=\"env.JOB_NAME\" value=\"`echo $JOB_NAME`\" />"
export str="$str1 $str2 $str3"


ant test-unit -Dsuite.dir=test -Dtest.includes=**/MeasureJ2SEStartupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dperformance.testutilities.dist.jar=$perfjar -Dnetbeans.keyring.no.master=true -Drepeat=1 -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.performance.exec.dir=$execdir -Dnbplatform.default.harness.dir=$platdefharness

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/j2se/build/test/unit/results/TEST-org.netbeans.performance.j2se.MeasureJ2SEStartupTest.xml > tmp.xml && mv tmp.xml "$performance"/j2se/build/test/unit/results/TEST-org.netbeans.performance.j2se.MeasureJ2SEStartupTest.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/j2se/build/test/unit/results/TEST-org.netbeans.performance.j2se.MeasureJ2SEStartupTest.xml

touch  $performance/j2se/build/test/unit/work/userdir0
touch  $performance/j2se/build/test/unit/work/tmpdir
rm -rf  $performance/j2se/build/test/unit/work/o.n.p.j.s*
rm -rf  $performance/j2se/build/test/unit/work/userdir0
rm -rf  $performance/j2se/build/test/unit/work/tmpdir
cp -R build/test/unit/work/ "$WORKSPACE"/startup/
cp -R build/test/unit/results "$WORKSPACE"/startup/

cd "$performance"

# performance project
cd "$performance"

ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/Enable*Test* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/Enable*Test* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true
ant test-unit -Dsuite.dir=test -Dtest.includes=**/fod/Enable*Test* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -Dorg.netbeans.editor.linewrap=true

buildnum=`cat "$reposdir"/build.number`

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablementSpeedBase.xml

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableJavaTest.xml

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableCNDTest.xml 

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnablePHPTest.xml 

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.fod.EnableEnterpriseTest.xml 

cp -R build/test/unit/work/ "$WORKSPACE"/fod
cp -R build/test/unit/results "$WORKSPACE"/fod
rm -rf "$WORKSPACE"/fod/userdir0
rm -rf "$WORKSPACE"/fod/tmpdir


cd "$performance"

ant test-unit -Dsuite.dir=test -Dtest.includes=**/MeasureScanningTest* -Dnetbeans.dest.dir=$netbeans_dest -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dnetbeans.keyring.no.master=true -DSuspendSupport.disabled=true-Drepeat=3 -Dorg.netbeans.editor.linewrap=true

awk -v str="$str" '{print} NR == 4 {printf (str);}'  "$performance"/build/test/unit/results/TEST-org.netbeans.performance.scanning.MeasureScanningTest.xml > tmp.xml && mv tmp.xml "$performance"/build/test/unit/results/TEST-org.netbeans.performance.scanning.MeasureScanningTest.xml
sed -i "s/\(<property name=\"buildnumber\" value=\"\).*\(\"\)/\1$buildnum\2/g" $performance/build/test/unit/results/TEST-org.netbeans.performance.scanning.MeasureScanningTest.xml

touch $performance/build/test/unit/work/tmpdir
touch "$performance"/build/test/unit/classes
touch "$performance"/build/test/unit/classes-generated
touch "$performance"/build/test/unit/data
rm -rf "$performance"/build/test/unit/work/o.n.p.s*
rm -rf "$performance"/build/test/unit/work/tmpdir
rm -rf "$performance"/build/test/unit/classes
rm -rf "$performance"/build/test/unit/classes-generated
rm -rf "$performance"/build/test/unit/data
rm -f "$performance"/tmp.xml

cp -R build/test/unit/work/ "$WORKSPACE"/scanning
cp -R build/test/unit/results "$WORKSPACE"/scanning

fi
