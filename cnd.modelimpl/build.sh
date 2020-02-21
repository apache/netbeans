#!/bin/bash
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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

function main() {

	local clean="n"
	local all="n"
	local cluster="cnd"
	local quiet="n"

	if [ -d "../nbbuild" ]; then
		nbbuild="../nbbuild"
	else
		if [ -d "nbbuild" ]; then
			nbbuild="nbbuild"
		else
			if [ -d ${NBBUILD} ]; then
				nbbuild="${NBBUILD}"
			else
				echo "Can not find nbbuild directory"
				return
			fi
		fi
	fi
	
	while [ -n "$1" ]
	do
		case "$1" in
			-h|--help|-\?)
				usage
				return
				;;
			-c|--clean)
				clean="y"
				;;
			-a|--all)
				all="y"
				;;
			-q|--quiet)
				quiet="y"
				;;
			*)
				cluster="$1"
				;;
		esac
		shift
	done
	
	local cluster_config="-Dcluster.config=${cluster}"
	local rebuild_cluster="-Drebuild.cluster.name=nb.cluster.${cluster}"
	
	# if we need to build BIG ide, check that user.build.properties does NOT contain cluster specification
	if [ ${all} == "y" ]; then
		cluster_config=""
		rebuild_cluster=""
		local ubp="${nbbuild}/user.build.properties"
		if [ ${clean} == "y" ]; then
			local search_string="^cluster.config"
		else
			local search_string="^rebuild.cluster.name"
		fi
		egrep "${search_string}" ${ubp}
		if [ -r ${ubp} ]; then
			local tmp
			tmp=`egrep "${search_string}" ${ubp}`
			rc=$?
			if [ ${rc} == 0 ]; then
				echo "Can not build BIG IDE: your ${ubp} specifies cluster:"
				echo "${tmp}"
				return
			fi
		fi
		clean="y"
		echo ""; echo "========== Building BIG IDE =========="; echo ""; 
		sleep 4 # allow user pressing ^C before we clean :)
	else
		egrep "^nb\.cluster\.${cluster}=" ${nbbuild}/cluster.properties > /dev/null
		rc=$?
		if [ ${rc} != 0 ]; then
			echo "Wrong cluster: ${cluster}"
			return
		fi
	fi

	local log="/tmp/${USER}-netbeans-build.log"
	if [ ${clean} == "y" ]; then
		echo ""; echo "========== Cleaning and building cluster ${cluster} =========="; echo ""; 
		sleep 2 # allow user pressing ^C before we clean :)		
		if [ ${quiet} == "y" ]; then
			echo "Quiet mode: redirecting output to ${log}"
			ant -Dadd.junit=true -f ${nbbuild}/build.xml ${cluster_config} clean build-nozip add-junit 2>&1 > ${log}
		else
			ant -Dadd.junit=true -f ${nbbuild}/build.xml ${cluster_config} clean build-nozip add-junit
		fi
	else
		echo ""; echo "========== Rebuilding cluster ${cluster} =========="; echo ""; 
		for D in `ls -1d ${nbbuild}/../cnd* ${nbbuild}/../dlight*`; do rm -rf  $D/build/* 2> /dev/null; done
		if [ ${quiet} == "y" ]; then
			echo "Quiet mode: redirecting output to ${log}"
			ant -f ${nbbuild}/build.xml ${rebuild_cluster} rebuild-cluster 2>&1 > ${log}
		else
			ant -f ${nbbuild}/build.xml ${rebuild_cluster} rebuild-cluster
		fi
	fi
	if [ ${quiet} == "y" ]; then 
		echo ""
		echo "Here is the bottom of the build log (${log})"; 
		tail -16 ${log}
		echo "See full log in ${log}"; 
	fi
}

function usage() {
	echo ""
	echo "Usage:"
	echo ""
	echo "build.sh [-c] -a"
	echo "	to build BIG IDE"
	echo "or"
	echo "build.sh [-c] cluster"
	echo "	to build particular cluster"
	echo ""
	echo "	-c means clean build-nozip, otherwise rebuild-cluster"
	echo ""
}

main $@
