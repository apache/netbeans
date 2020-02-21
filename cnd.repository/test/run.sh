#!/bin/bash
#
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
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

#set -x

MWS_USERDIRS="/export/home/nbuserdirs"
TIMEOUT=900
MEMORY_SET="512 384 256 192 128"
PROJECTS_SET="boost+mysql boost"
#MEMORY_SET="128 256"
#PROJECTS_SET="args"

export LOGPATH=/export/home/mwstest/${LOGNAME-generic}/`date +%y%m%d%H%M`
cp results.xsl /export/home/mwstest/${LOGNAME-generic}/

if [ ! -d ${LOGPATH} ]; then
    mkdir -p ${LOGPATH}
fi

export XMLOUTPUT=${LOGPATH}/results.xml

#TEST_SRC=/set/ide/mars/spb/testcode/ddd-3.3.11/
TEST_SRC=~/SunStudioProjects/Args1/

echo logs path: ${LOGPATH}

function sfstest() 
{
    if [ ! -n "${sfstestname}" ]; then
        echo "sfstest failed. No name provided"
        return
    fi

    echo "<test name='Disk repository ${sfstestname}'>" >> ${XMLOUTPUT}
    pushd . > /dev/null
    cd ./sfs
    run-${sfstestname}.sh $TEST_SRC --noant > ${LOGPATH}/disk-${sfstestname}.txt 2>&1
    if [ $? -eq 0 ]; then
        result="passed"
    else
        result="failed"
    fi
    echo "Disk repository ${sfstestname} test - ${result}"
    echo "<log name='Disk repository ${sfstestname} test log'>disk-${sfstestname}.txt</log>" >> ${XMLOUTPUT}
    echo "<result>${result}</result>" >> ${XMLOUTPUT}
    echo "</test>" >> ${XMLOUTPUT}
    popd > /dev/null
}

function repositorytest() {
    printf "%s" "Repository correctness test - "
    echo "<test name='Repository correctness'>" >> ${XMLOUTPUT}
    pushd . > /dev/null
    cd ./../../modelimpl/test/scripts/
    echo "<log name='Repository correctness test log'>repository-correctness.txt</log>" >> ${XMLOUTPUT}
    repository.sh $TEST_SRC > ${LOGPATH}/repository-correctness.txt
    popd > /dev/null
    echo "</test>" >> ${XMLOUTPUT}
    echo "done."
}

function mws() {
    pushd . > /dev/null
    cd ./../..
    
    for p in ${PROJECTS_SET}
    do
        MWSPROJECT=$p
        for i in ${MEMORY_SET}
        do
            MEM=$i
            runcnd
        done
    done
    popd > /dev/null
}

function runcnd() {
    if [ "true" = "${USE_REPOSITORY}" ]; then
        REPPARAMS="-J-Dcnd.repository.hardrefs=false -J-Dcnd.repository.cache.path=/export/home/cache-row7/ -J-Dcnd.repository.delete.cache.files=false"
    else
        REPPARAMS="-J-Dcnd.repository.hardrefs=true"
    fi
    printf "%s" "MWS. Project: ${MWSPROJECT}. Memory: ${MEM}Mb. Repository: ${USE_REPOSITORY} - "
    PARAMS="-J-Dcnd.close.ide.after.parse=true"
    PARAMS="${PARAMS} -J-Dcnd.close.ide.timeout=${TIMEOUT}"
    PARAMS="${PARAMS} -J-Dcnd.close.report.xml=${XMLOUTPUT}"
    RUNLINE="run.sh  --userdir ${MWS_USERDIRS}/${MWSPROJECT} -J-Xmx${MEM}M ${REPPARAMS}"

    echo "<test-mws name='Memory Working Set'>" >> ${XMLOUTPUT}
    echo "<project>${MWSPROJECT}</project>" >> ${XMLOUTPUT}
    echo "<repository>${USE_REPOSITORY}</repository>" >> ${XMLOUTPUT}
    echo "<memory>${MEM} Mb</memory>" >> ${XMLOUTPUT}
    echo "<run-line>${RUNLINE}</run-line>" >> ${XMLOUTPUT}
    #echo ${RUNLINE} 
    LOGNAME="mws-rep${USE_REPOSITORY}-${MWSPROJECT}-${MEM}.txt"
    ${RUNLINE} ${PARAMS} > ${LOGPATH}/${LOGNAME} 2>&1
    echo "<log name='mws log'>${LOGNAME}</log>" >> ${XMLOUTPUT}
    echo "</test-mws>" >> ${XMLOUTPUT}
    echo "done."
}

THEDATE=`date`
echo "<?xml version='1.0' ?>" > ${XMLOUTPUT}
echo "<?xml-stylesheet type='text/xsl' href='../results.xsl' ?>" >> ${XMLOUTPUT}
echo "<repository-tests date='${THEDATE}'>" >> ${XMLOUTPUT}

# Disk repository correctness test
sfstestname=correctness
sfstest

# Disk repository threading stress test
sfstestname=threading
sfstest

# Repository correctness test
repositorytest

# Memory working set with repository
USE_REPOSITORY=true
mws

# Memory working set withour repository
USE_REPOSITORY=false
mws

echo "</repository-tests>" >> ${XMLOUTPUT}

#cat ${XMLOUTPUT}