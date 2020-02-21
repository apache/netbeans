#!/bin/sh
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

PARAMS="-J-DuseGtk=false -J-Xmx3G"
SUSPEND="n"
CONSOLE="-J-Dnetbeans.logger.console=true"
DBGPORT=${DBGPORT-5858}
TMP_PREFIX=${TMP_PREFIX-/var/tmp}
USERDIR="--userdir ${TMP_PREFIX}/${USER}/cnd-userdir"
PARSERRORS="-J-Dparser.report.errors=true"
XREF_LOG="-J-Dorg.netbeans.modules.cnd.refactoring.plugins.level=FINE"
DEBUG="-J-agentlib:jdwp=transport=dt_socket,server=y"
PRG=$0
NB_COPY="${TMP_PREFIX}/${USER}/nb-copy"
VERBOSE=true
DRD=""

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
        PRG="$link"
    else
        PRG="`dirname "$PRG"`/$link"
    fi
done

while [ -n "$1" ]
do
    case "$1" in
        --cxx|-cxx)
                echo "use new straight parser"
                PARAMS="${PARAMS} -J-Dcnd.modelimpl.cpp.parser.new.grammar=true"
                PARAMS="${PARAMS} -J-Dcnd.modelimpl.parse.headers.with.sources=true"
                ;;
        --gdb)
                echo "Enable GDB-window and do not delete gdb-cmds logs"
                PARAMS="${PARAMS} -J-Dgdb.console.window=true -J-Dgdb.console.savelog=true"
                ;;
	--cache)
		shift
                echo "Redirecting cache to $1"
                PARAMS="${PARAMS} -J-Dcnd.repository.cache.path="$1""
                ;;
        --nopersist)
                echo "Setting persistence OFF"
                PARAMS="${PARAMS} -J-Dcnd.modelimpl.persistent=false"
                ;;
        --nb)
		shift
                echo "Using NB from $1"
                NBDIST=$1
                ;;
        --jdk)
		shift
                JAVA=$1/bin/java
                ;;
	--nocon|--noconsole)
		CONSOLE=""
		;;
	--nocopy|--nc)
		NB_COPY=""
		;;
        --cp|--copy)
		shift
                NB_COPY=$1
		if [ -r ${NB_COPY} ]; then
		    echo "The content of ${NB_COPY} will be deleted"
		    echo "Press ENTER to proceed or ^C to abort"
		    read t
		fi
                ;;
        --quiet|-q)
                echo "Disabled verbose mode"
                PARSERRORS=""
                VERBOSE=false
                XREF_LOG=""
                ;;
	--nodebug|-nodebug)
		echo "Debug mode OFF"
		DEBUG=""
		;;
	--sdebug|-sdebug)
		echo "wait to attach debugger on port ${DBGPORT}"
		SUSPEND="y"
		;;
	--debug|-debug)
		echo "debigging mode (nowait)"
		SUSPEND="n"
		;;
        --ycpu|-ycpu)
                echo "profile using YourKit Profiler with CPU sampling, save snapshots in ${HOME}/yjp_data/CPU"
                PROFILE="-J-Dosgi.compatibility.bootdelegation=true -J-agentlib:yjpagent=sampling,monitors,disablealloc,disabletracing,disablej2ee,builtinprobes=none,onexit=memory,dir=${HOME}/yjp_data/CPU"
                ;;
        --yprofile|-yprofile)
                echo "profile using YourKit Profiler, save snapshots in ${HOME}/yjp_data/IDE"
                PROFILE="-J-Dosgi.compatibility.bootdelegation=true -J-agentlib:yjpagent=dir=${HOME}/yjp_data/IDE"
		;;
        --drd|-drd)
                echo "DataRace check run (debugging is OFF)"
	        DRD="y"	
                ;;
	--ypl|-ypl)
		echo "light profile using YourKit Profiler, save snapshots in ${HOME}/yjp_data/IDE"
                PROFILE="-J-Dosgi.compatibility.bootdelegation=true -J-agentlib:yjpagent=dir=${HOME}/yjp_data/IDE,telemetryperiod=250,disabletracing,disablealloc,disablej2ee,noj2ee,disablej2ee,disableexceptiontelemetry"
                ;;
        --userdir)
		shift
                echo "setting userdir to $1"
		USERDIR="--userdir $1"
                ;;
        --nouserdir)
                echo "setting userdir to standard one"
		USERDIR=""
                ;;
	--noerr)
		echo "suppressing parser errors"
		PARSERRORS=""
		;;
	--hardrefs|--hard)
                echo "using in-memory (hard refs) repository"
		PARAMS="${PARAMS} -J-Dcnd.repository.hardrefs=true"
		;;
	--threads)
                shift
                echo "using $1 parser threads"
                PARAMS="${PARAMS} -J-Dcnd.modelimpl.parser.threads=$1"
		;;
	*)
		PARAMS="${PARAMS} $1"
		;;
    esac
    shift
done

if [ -z "${NBDIST}" ]; then
	CND=`dirname "$PRG"`
	cd ${CND}
	CND=`pwd`
	cd $OLDPWD
	NBDIST="${CND}/../nbbuild/netbeans"
fi

if [ -z "${NBDIST}" ]; then
	echo "Please specify NBDIST environment variable; it should point to Netbeans installation"
        exit 1;
else
	if [ ! -r "${NBDIST}/bin/netbeans" ]; then
		echo "NBDIST environment variable should point to Netbeans installation"
                exit 1;
	fi
fi

if [ -n "$DRD" ]; then
    DRDDIR="${HOME}/devarea/drd" 
    PROFILE="-J-Dosgi.compatibility.bootdelegation=true -J-javaagent:${DRDDIR}/latest/drd_agent.jar -J-Ddrd.settings.file=${DRDDIR}/drd.properties -J-Ddrd.config.dir=${DRDDIR}/config"
    DEBUG=""
fi

if [ -n "${DEBUG}" ]; then
    DEBUG="${DEBUG},suspend=${SUSPEND},address=${DBGPORT}"
fi

DEFS=""
DEFS="${DEFS} ${CONSOLE}"
DEFS="${DEFS} ${PARSERRORS}"
DEFS="${DEFS} -J-Dcnd.modelimpl.timing=true"
DEFS="${DEFS} -J-Dcnd.modelimpl.timing.per.file.flat=${VERBOSE}"
DEFS="${DEFS} -J-Dparser.report.include.failures=${VERBOSE}"
DEFS="${DEFS} -J-Dsun.java2d.pmoffscreen=false"
DEFS="${DEFS} -J-Dtest.xref.action=true"
DEFS="${DEFS} -J-Dcnd.classview.sys-includes=true"
DEFS="${DEFS} -J-Dcnd.callgraph.showgraph=true"
DEFS="${DEFS} -J-Dcnd.trace.includes=true"
DEFS="${DEFS} -J-Dcnd.standalone.trace=${VERBOSE}"
DEFS="${DEFS} -J-Dcnd.refactoring.extra=true"
DEFS="${DEFS} -J-Dcnd.trace.multiple.visible=${VERBOSE}"
DEFS="${DEFS} -J-Dcnd.trace.csm.cache=${VERBOSE}"
DEFS="${DEFS} -J-Dcnd.semantic.line.limit=-1"
##DEFS="${DEFS} -J-Dcnd.parser.queue.trace=true"
##DEFS="${DEFS} -J-Dcnd.modelimpl.parser.threads=2"
##DEFS="${DEFS} -J-Dcnd.modelimpl.no.reparse.include=true"

#to solve the issue with breakpoints hit in popup dialogs which hangs debugger and system
DEFS="${DEFS} -J-Dsun.awt.disablegrab=true"

if [ -z "${NB_COPY}" ]; then
    echo "Using original NB location: ${NBDIST}"
else
    echo "Copying NB from ${NBDIST} to ${NB_COPY}..."
    mkdir -p ${NB_COPY} > /dev/null
    rm -rf ${NB_COPY}/* > /dev/null
    cp -r ${NBDIST}/* ${NB_COPY}
    echo "Launching NB from ${NB_COPY}"
    NBDIST="${NB_COPY}"
fi

"${NBDIST}/bin/netbeans" -J-ea -J-server -J-DSUNW_NO_UPDATE_NOTIFY=true ${XREF_LOG} ${USERDIR} ${DEBUG} ${PROFILE} ${DEFS} ${PARAMS}
