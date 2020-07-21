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

#
# resolve symlinks
#

PRG=$0

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname "$PRG"`/$link"
    fi
done

progdir=`dirname "$PRG"`
APPNAME=`basename "$PRG"`
case "`uname`" in
    Darwin*)
        # set default userdir and cachedir on Mac OS X
        DEFAULT_USERDIR_ROOT="${HOME}/Library/Application Support/${APPNAME}"
        DEFAULT_CACHEDIR_ROOT=${HOME}/Library/Caches/${APPNAME}
        ;;
    *) 
        # set default userdir and cachedir on unix systems
        DEFAULT_USERDIR_ROOT=${HOME}/.${APPNAME}
        DEFAULT_CACHEDIR_ROOT=${HOME}/.cache/${APPNAME}
        ;;
esac

if [ -f "$progdir/../etc/$APPNAME".conf ] ; then
    . "$progdir/../etc/$APPNAME".conf
fi

# XXX does not correctly deal with spaces in non-userdir params
args=""

case "`uname`" in
    Darwin*)
        if [ ! -z "$default_mac_userdir" ]; then
          userdir="${default_mac_userdir}"
        else
          userdir="${default_userdir}"
        fi
        ;;
    *)
        userdir="${default_userdir}"
        ;;
esac
while [ $# -gt 0 ] ; do
    case "$1" in
        --userdir) shift; if [ $# -gt 0 ] ; then userdir="$1"; fi
            ;;
        *) args="$args \"$1\""
            ;;
    esac
    shift
done

cachedir="${default_cachedir}"

if [ -f "${userdir}/etc/$APPNAME".conf ] ; then
    . "${userdir}/etc/$APPNAME".conf
fi

if [ -n "$jdkhome" -a \! -d "$jdkhome" -a -d "$progdir/../$jdkhome" ]; then
    # #74333: permit jdkhome to be defined as relative to app dir
    jdkhome="$progdir/../$jdkhome"
fi

readClusters() {
  if [ -x /usr/ucb/echo ]; then
    echo=/usr/ucb/echo
  else
    echo=echo
  fi
  while read X; do
    if [ "$X" \!= "" ]; then
      $echo "$progdir/../$X"
    fi
  done
}

absolutize_paths() {
    while read path; do
        if [ -d "$path" ]; then
            (cd "$path" 2>/dev/null && pwd)
        else
            echo "$path"
        fi
    done
}

clusters=`(cat "$progdir/../etc/$APPNAME".clusters; echo) | readClusters | absolutize_paths | tr '\012' ':'`

if [ ! -z "$extra_clusters" ] ; then
    clusters="$clusters:$extra_clusters"
fi

nbexec=`echo "$progdir"/../platform*/lib/nbexec`

case "`uname`" in
    Darwin*)
        eval exec sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            -J-Xdock:name='"$APPNAME"' \
            '"-J-Xdock:icon=$progdir/../../$APPNAME.icns"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            --cachedir '"${cachedir}"' \
            ${default_options} \
            "$args"
        ;;
    *)  
       sh=sh
       # #73162: Ubuntu uses the ancient Bourne shell, which does not implement trap well.
       if [ -x /bin/bash ]
       then
           sh=/bin/bash
       fi
       eval exec $sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            --cachedir '"${cachedir}"' \
            ${default_options} \
            "$args"
       exit 1
        ;;
esac
