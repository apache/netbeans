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

file=$NBCND_RC
prompt="[Enter] "
pgm=true

if [ -x /bin/uname ]
then
    sysname=`/bin/uname -s`
elif [ -x /usr/bin/uname ]
then
    sysname=`/usr/bin/uname -s`
else
    sysname=`uname -s`
fi

if [ -x /bin/echo ]
then
    ECHO=/bin/echo
elif [ -x /usr/bin/echo ]
then
    ECHO=/usr/bin/echo
else
    ECHO=echo
fi

if [ "$sysname" = "Darwin" -o "$sysname" = "SunOS" ]
then
    NOPT=
    ENDOPT="\c"
else
    NOPT="-n"
    ENDOPT=
fi

while [ -n "$1" ]
do
    case "$1" in
    -p)
        prompt="$2"
        shift
        ;;

    -f)
        file="$2"
        shift
        ;;

    /*|./*|[a-zA-Z]:/*)
        pgm="$1"
        shift
        break;
        ;;

    *)
        pgm="./$1"
        shift
        break
        ;;
    esac
    shift
done

"$pgm" "$@"
rc=$?

if [ -n "$file" ]
then
    $ECHO $rc > "$file"
fi

$ECHO $NOPT "$prompt${ENDOPT}"
read a
exit $rc
