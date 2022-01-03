#!/bin/bash
# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

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
