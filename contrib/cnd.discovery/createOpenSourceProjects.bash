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

XREF=""
ERROR=""
QUITE=""

function classpath() {

    local nbdist=${NBDIST-"../nbbuild/netbeans/"}
    local cnddist=${CNDDIST-"${nbdist}/cnd/"}

    CP=""

    local ide
    if [ -d "${nbdist}/ide" ]; then
	ide="${nbdist}/ide"
    else 
	if [ -d "${nbdist}/ide" ]; then
	    ide="${nbdist}/ide"
	else 
	    if [ -d "${nbdist}/ide" ]; then
		ide="${nbdist}/ide"
            else 
		    if [ -d "${nbdist}/ide" ]; then
			ide="${nbdist}/ide"
		    else 
			    if [ -d "${nbdist}/ide" ]; then
				ide="${nbdist}/ide"
			    else 
				    if [ -d "${nbdist}/ide" ]; then
					ide="${nbdist}/ide"
				    else 
					echo "Can not find ide subdirectory in Netbeans"
					return
				    fi
			    fi
		    fi
            fi
	fi
    fi

    local platform
    if [ -d "${nbdist}/platform" ]; then
	platform="${nbdist}/platform"
    else 
	if [ -d "${nbdist}/platform" ]; then
	    platform="${nbdist}/platform"
	else 
		if [ -d "${nbdist}/platform" ]; then
		    platform="${nbdist}/platform"
		else 
		    if [ -d "${nbdist}/platform" ]; then
			platform="${nbdist}/platform"
		    else
			    if [ -d "${nbdist}/platform" ]; then
				platform="${nbdist}/platform"
			    else
				echo "Can not find platform subdirectory in Netbeans"
				return
			    fi
		    fi
		fi
	fi
    fi

    CP=${CP}${path_sep}${platform}/lib/org-openide-util.jar
    CP=${CP}${path_sep}${platform}/lib/boot.jar
    CP=${CP}${path_sep}${platform}/lib/org-openide-modules.jar
    CP=${CP}${path_sep}${platform}/core/org-openide-filesystems.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-loaders.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-nodes.jar
    CP=${CP}${path_sep}${platform}/modules/org-openide-dialogs.jar
    CP=${CP}${path_sep}${platform}/modules/org-netbeans-modules-masterfs.jar
    CP=${CP}${path_sep}${platform}/modules/org-netbeans-modules-queries.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-projectapi.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-projectuiapi.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-project-ant.jar
    CP=${CP}${path_sep}${ide}/modules/org-netbeans-modules-project-libraries.jar
    CP=${CP}${path_sep}${ide}/modules/org-openidex-util.jar


    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-discovery.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-apt.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-utils.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-makeproject.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd.jar
    CP=${CP}${path_sep}${cnddist}/modules/org-netbeans-modules-cnd-api-project.jar

    if [ -z ${QUITE} ]; then
	trace_classpath ${CP}
    fi
}

function trace_classpath() {
    local paths=$@
    local ERROR=""
    for F in `echo ${paths} | awk -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	if [ ! -r ${F} ]; then
	    echo "File ${F} doesn't exist"
	    ERROR="y"
	fi
    done

    if [ -n "${ERROR}" ]; then
	echo "incorrect classpaths"
    else
	#print classpath
	echo "Using classpath:"
	for F in `echo ${paths} | awk -F${path_sep} '{ for( i=1; i<=NF; i++ ) print $i }'`; do
	    echo $F
	done
    fi
}

function params() {

    while [ -n "$1" ]
    do
	case "$1" in
	    --nb)
		    shift
		    echo "Using NB from $1"
		    NBDIST=$1
		    ;;
	    --jdk)
		    shift
		    JAVA=$1/bin/java
		    ;;
	    --cnd)
		    shift
		    echo "Using NB from $1"
		    CNDDIST=$1
		    ;;
	    *)
		    PARAMS="${PARAMS} $1"
		    ;;
	esac
	shift
    done

}

function main() {

    JAVA="${JAVA-`which java`}"
    DEFS=""
    PARAMS=""
    
    DBGPORT=${DBGPORT-5858}

    params $@

    MAIN="org.netbeans.modules.cnd.discovery.project.StandAlone"

    classpath
    if [ -z "${CP}" ]; then
	echo "Can't find some necessary jars"
	return
    fi

    if [ -z ${QUITE} ]; then
	echo "Java: " ${JAVA}
	${JAVA} -version
    fi

    set -x
#    ${JAVA} -Xmx512M -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=8888 -agentlib:yjpagent=dir=/home/as204739/yjp_data/IDE -Dorg.netbeans.modules.cnd.makeproject.api.runprofiles=true -cp ${CP} ${MAIN} ${PARAMS}
    ${JAVA} -Xmx512M -Dorg.netbeans.modules.cnd.makeproject.api.runprofiles=true -cp ${CP} ${MAIN} ${PARAMS}
}

path_sep=":"

### understand path separator
uname=`uname`
#uname_prefix=`expr substr "${uname}" 1 6`
uname_prefix=${uname:0:6}
if [ "${uname_prefix}" = "CYGWIN" ]; then
   path_sep=";"
fi

main $@


