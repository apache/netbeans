#!/bin/sh -x
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
# For all tests that failed,
# copy goldens and data into build/test/unit/work/tmp2
#

cur=`pwd`
modelimpl="${MODELIMPL-${cur}}"
modelimpl_golden="${modelimpl}/test/unit/data/goldenfiles"

failed_dirs=`find ${modelimpl}/build/test/unit/work -name "*.golden" -exec dirname {} \; | sort -u`
failed_files=`find ${modelimpl}/build/test/unit/work -name "*.golden" | sort -u`

if [ -z "${failed_files}" ]; then
    echo "No failed tests found"
else
    dst="${modelimpl}/build/test/unit/etalons"
    rm -r ${dst}/* > /dev/null
    mkdir -p ${dst}
    cp -r ${failed_dirs} ${dst}

    # sed s/\.golden//g
    for d in `ls ${dst}`; do
	cd ${dst}/$d
	for gold in `ls *.golden`; do
	    orig=`echo ${gold} | sed s/\.golden//g`
	    #ls -l ${gold}
	    #ls -l ${orig}
	    echo "==================== ${gold} vs ${orig} ===================="
	    diff ${gold} ${orig}
	done
	cd - > /dev/null
    done

    cnt=`ls ${dst} | wc -l`
    echo ${cnt} directories are copied to ${dst}

    for d in `ls ${dst}`; do
	cd ${dst}/$d
	for gold in `ls *.golden`; do
            failed_name=`echo ${gold} | sed s/\.golden//g`
            orig=`find ${modelimpl_golden} -name ${failed_name}`
            if [ -z "${orig}" ]; then
                echo "ERROR: pair for ${gold} is not found"
            else 
                echo copy ${failed_name} ${orig}
                cp ${failed_name} ${orig}
            fi
	done
	cd - > /dev/null
    done
fi
