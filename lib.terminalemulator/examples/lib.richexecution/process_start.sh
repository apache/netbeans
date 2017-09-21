#! /bin/sh

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

# See process_start.cat for spec and usage information.

PTY=

echo PID $$

#
# process options
#
while [ "$1" != "" ]
do
    case $1 in
    -pty)   
        shift
        if [ "$1" = "" ]
        then
            echo "ERROR missing pty after -pty"
            exit -1
        fi
        PTY=$1
        ;;
    -*)
        echo "ERROR unrecognized option '"$1"'"
        exit -1
        ;;
    *)
        break
        ;;
    esac
    shift
done

if [ "$1" = "" ]
then
    echo "ERROR missing executable"
    exit -1
fi

if [ "$PTY" = "" ]
then
    echo "ERROR -pty required"
    exit -1
fi

# echo PTY=$PTY
# echo '$*'=$*

exec $* 0<> $PTY 1<> $PTY 2<> $PTY

echo "ERROR exec failed"
exit -1
