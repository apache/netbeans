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

PATH=/usr/bin:/bin:/usr/sbin:/sbin:/usr/local/bin
HOSTNAME=`uname -n`
OS=`uname -s`
CPUTYPE=`uname -p`
BITNESS=32

LS=/bin/ls
OSFAMILY=
DATETIME=`date -u +'%Y-%m-%d %H:%M:%S'`

if [ "${OS}" = "SunOS" ]; then
   BITNESS=`isainfo -b`
   OSFAMILY="SUNOS"
   OSNAME="SunOS"
   OSBUILD=`head -1 /etc/release | sed -e "s/^ *//"`
   CPUNUM=`/usr/sbin/psrinfo -v | grep "^Status of" | wc -l | sed 's/^ *//'`
else
   if [ "${OS}" = "Darwin" ]; then
      sysctl hw.cpu64bit_capable | grep -q "1$"
      if [ $? -eq 0 ]; then
         BITNESS=64
      fi
   else
      uname -a | egrep "x86_64|WOW64|sparc64|aarch64" >/dev/null
      if [ $? -eq 0 ]; then
         BITNESS=64
      fi
   fi

   if [ -f "/etc/sun-release" ]; then
      OSNAME="${OS}-JDS"
      OSBUILD=`head -1 /etc/sun-release`
   elif [ -f /etc/SuSE-release ]; then
      OSNAME="${OS}-SuSE"
      OSBUILD=`cat /etc/SuSE-release | tr "\n" " "`;
   elif [ -f /etc/redhat-release ]; then
      OSNAME="${OS}-Redhat"
      OSBUILD=`head -1 /etc/redhat-release`
   elif [ -f /etc/gentoo-release ]; then
      OSNAME="${OS}-Gentoo"
      OSBUILD=`head -1 /etc/gentoo-release`
   elif [ -f /etc/lsb-release ]; then
      OSNAME="${OS}-"`cat /etc/lsb-release | grep DISTRIB_ID | sed 's/.*=//'`
      OSBUILD=`cat /etc/lsb-release | grep DISTRIB_DESCRIPTION | sed 's/.*=//' | sed 's/"//g'`
   fi
fi

OSFAMILY=${OSFAMILY:-`echo ${OS} | grep _NT- >/dev/null && echo WINDOWS`}
OSFAMILY=${OSFAMILY:-`test "$OS" = "Darwin" && echo MACOSX`}
OSFAMILY=${OSFAMILY:-`test "$OS" = "Linux" && echo LINUX`}
OSFAMILY=${OSFAMILY:-${OS}}

CPUFAMILY=`(echo ${CPUTYPE} | egrep "^i|x86_64|athlon|Intel" >/dev/null && echo x86) || echo ${CPUTYPE}`
if [ "${CPUFAMILY}" != "x86" -a "${CPUFAMILY}" != "sparc" -a "${CPUFAMILY}" != "sparc64" ]; then
   CPUTYPE=`uname -m`
fi
CPUFAMILY=`(echo ${CPUTYPE} | egrep "^i|x86_64|athlon|Intel" >/dev/null && echo x86) || echo ${CPUTYPE}`
if [ "${CPUFAMILY}" = "sparc64" ]; then
   CPUFAMILY="sparc"
fi
# New check if ARM64 then return ARM so Java code will stop returning “UNKNOWN”
if [ "${CPUFAMILY}" = "arm64" ]; then
   CPUFAMILY="arm"
fi

USERDIRBASE=${HOME}

if [ "${OSFAMILY}" = "LINUX" ]; then
   if [ "${CPUFAMILY}" = "sparc" ]; then
     CPUNUM=`cat /proc/cpuinfo | grep 'ncpus active' | sed 's/[^:]*.[ ]*//'`
   else
     CPUNUM=`cat /proc/cpuinfo | grep processor | wc -l | sed 's/^ *//'`
   fi
elif [ "${OSFAMILY}" = "WINDOWS" ]; then
   CPUNUM=$NUMBER_OF_PROCESSORS
   OSNAME=`uname`
   USERDIRBASE=${USERPROFILE}
elif [ "${OSFAMILY}" = "MACOSX" ]; then
   CPUNUM=`hostinfo | awk '/processor.*logical/{print $1}'`
   OSNAME="MacOSX"
   OSBUILD=`hostinfo | sed -n '/kernel version/{n;p;}' | sed 's/[	 ]*\([^:]*\).*/\1/'`
elif [ "${OSFAMILY}" = "FreeBSD" ]; then
   CPUNUM=`sysctl hw.ncpu | awk '{print $2}'`
   OSNAME=`sysctl -n  kern.ostype`
   OSBUILD=`sysctl -n kern.osrelease`
fi

wx_fail() {
    tmp="${1}/wx_test"
    touch ${tmp} 2> /dev/null
    if [ $? -eq 0 ]; then
        chmod u+x ${tmp} 2> /dev/null
        if [ -x ${tmp} ]; then
            rm ${tmp} 2> /dev/null
            return 1
        fi
        rm ${tmp} 2> /dev/null
    fi

    return 0
}

USER=${USER:-`logname 2>/dev/null`}
USER=${USER:-${USERNAME}}
USER_D=`echo ${USER} | sed "s/[\\/]/_/g"`
TMPBASE=${TMPBASE:-/var/tmp}

SUFFIX=0
TMPDIRBASE=${TMPBASE}/dlight_${USER_D}

if wx_fail ${TMPBASE}; then
    if wx_fail ${TMPDIRBASE}; then
        TMPBASE=/tmp
        TMPDIRBASE=${TMPBASE}/dlight_${USER_D}
    fi
fi

mkdir -p ${TMPDIRBASE}
while [ ${SUFFIX} -lt 5 ]; do
    if wx_fail ${TMPDIRBASE}; then
        echo "Warning: TMPDIRBASE is not writable: ${TMPDIRBASE}">&2
        SUFFIX=`expr 1 + ${SUFFIX}`
        TMPDIRBASE=${TMPBASE}/dlight_${USER_D}_${SUFFIX}
        /bin/mkdir -p ${TMPDIRBASE} 2>/dev/null
    else
        break
    fi
done

if wx_fail ${TMPDIRBASE}; then
    :
else
    SUFFIX=0
    TMPBASE=${TMPDIRBASE}
    TMPDIRBASE=${TMPBASE}/${NB_KEY}
    mkdir -p ${TMPDIRBASE}
    while [ ${SUFFIX} -lt 5 ]; do
        if wx_fail ${TMPDIRBASE}; then
            echo "Warning: TMPDIRBASE is not writable: ${TMPDIRBASE}">&2
            SUFFIX=`expr 1 + ${SUFFIX}`
            TMPDIRBASE=${TMPBASE}/${NB_KEY}_${SUFFIX}
            /bin/mkdir -p ${TMPDIRBASE} 2>/dev/null
        else
            break
        fi
    done
fi

if wx_fail ${TMPDIRBASE}; then
    TMPDIRBASE=${TMPBASE}
fi

if wx_fail ${TMPDIRBASE}; then
    echo "Error: TMPDIRBASE is not writable: ${TMPDIRBASE}">&2
fi

ENVFILE="${TMPDIRBASE}/env"

ID=`LC_MESSAGES=C /usr/bin/id`

echo BITNESS=${BITNESS}
echo CPUFAMILY=${CPUFAMILY}
echo CPUNUM=${CPUNUM}
echo CPUTYPE=${CPUTYPE}
echo HOSTNAME=${HOSTNAME}
echo OSNAME=${OSNAME}
echo OSBUILD=${OSBUILD}
echo OSFAMILY=${OSFAMILY}
echo USER=${USER}
echo SH=${SHELL}
echo USERDIRBASE=${USERDIRBASE}
echo TMPDIRBASE=${TMPDIRBASE}
echo DATETIME=${DATETIME}
echo ENVFILE=${ENVFILE}
echo ID=${ID}
exit 0
