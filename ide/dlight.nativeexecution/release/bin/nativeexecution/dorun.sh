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

PATH=/bin:/usr/bin
PROG=`basename "$0"`
USAGE="usage: ${PROG} [-p prompt] -x execScript"
PROMPT=NO
STATUS=-1

fail() {
  echo $@ >&2
  exit 1
}

doExit() {
  echo ${STATUS} > "${SHFILE}.res"
  exit ${STATUS}
}

[ $# -lt 1 ] && fail $USAGE

while getopts p:x: opt; do
  case $opt in
    x) SHFILE=$OPTARG
       ;;
    p) PROMPT=$OPTARG
       ;;
  esac
done

shift `expr $OPTIND - 1`

trap "doExit" 1 2 15 EXIT

sh "${SHFILE}"
STATUS=$?

echo ${STATUS} > "${SHFILE}.res"

if [ "${PROMPT}" != "NO" ]; then
  echo "${PROMPT}"
  read X
else
  sleep 1
fi

