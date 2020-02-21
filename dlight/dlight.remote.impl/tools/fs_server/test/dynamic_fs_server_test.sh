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


################################################################################
# To run dynamic tests:
# 1) build fs_server on Solaris x86 with debug (Debug_Solaris-x86 configuration)
# 2) instrument it, for example, via
#   discover fs_server -H /tmp/fs_server_errors.html
# 3) cd to the directory this file resides in
# 4) bash ./dynamic_fs_server_test.sh
# You are done! See errors in /tmp/fs_server_errors.html
################################################################################

function increment_idx() {
    req_idx=`expr $req_idx + 1`
}

#
# Parameters:
# 1) directory to iterate
# 2) output file
# 3) command
function make_requests() {

    # directory to iterate recursively
    R=$1
    if [ ! -d $R ]; then
      echo $R is not a directory
      exit 2
    fi
    R=`(cd $R; pwd)`

    # output file
    O=$2

    # fs_server command
    C=$3

    for D in `find $R -type d`; do increment_idx; echo "${C} $req_idx ${#D} $D"  >> $O; done
}

function req_sleep() {
    # make it sleep for a while to allow threads complete their job
    # nothe that it is request reader thread that sleeps
    increment_idx
    echo "P ${req_idx} 1 3" >> ${req_file}
}

# Possible types of requests:
# l - FS_REQ_LS
# r - FS_REQ_RECURSIVE_LS
# S - FS_REQ_STAT
# s - FS_REQ_LSTAT
# C - FS_REQ_COPY
# m - FS_REQ_MOVE
# q - FS_REQ_QUIT
# P - FS_REQ_SLEEP
# W - FS_REQ_ADD_WATCH
# w - FS_REQ_REMOVE_WATCH
# R - FS_REQ_REFRESH
# d - FS_REQ_DELETE
# D - FS_REQ_DELETE_ON_DISCONNECT
# i - FS_REQ_SERVER_INFO
# o - FS_REQ_OPTION                                                                                                                                                                                                                             
# ? - FS_REQ_HELP 

basedir=`dirname $0`

thead_count=16

echo "changing current directory to ${basedir}"
cd ${basedir}

req_file="/tmp/dynamic_fs_server_test.req"
rsp_file="/tmp/dynamic_fs_server_test.rsp"
echo "preparing requests and writing them into ${req_file}"
rm -rf ${req_file}
req_idx=0

# first wrong requests (for some reason while put in the end they didn't gave an error)
echo "%" >> ${req_file} # wrong type of request
echo "s 0 100 asdasdasd" >> ${req_file} # real path length is less
echo "s 0 1 asdasdasd" >> ${req_file} # real path length is more
echo "m 0 0 asdasdasd1 100 asdasdasd2" >> ${req_file} # real path2 length is less
echo "m 0 0 asdasdasd1 1 asdasdasd2" >> ${req_file} # real path2 length is more
echo "m 0 0 qwe" >> ${req_file} # path 2 is absent
req_sleep

make_requests ../../.. ${req_file} l

req_sleep

make_requests ../.. ${req_file} r
make_requests ../.. ${req_file} s
make_requests ../.. ${req_file} S

#
# now make requests for copy, move and delete
#

tmpdir=`mktemp -d`
echo "temporary dir: ${tmpdir}"
src=${tmpdir}/src
copy_dst=${tmpdir}/copy_dst
move_dst=${tmpdir}/move_dst
mkdir ${src}
cp -r ../../* ${src}/

increment_idx
echo "C ${req_idx} 0 ${src} 0 ${copy_dst}" >> ${req_file}

req_sleep

increment_idx
echo "m ${req_idx} 0 ${src} 0 ${move_dst}" >> ${req_file}

# the previous one was erroneous since the fs_server can only move plain files, not directories
# let's now add correct one
increment_idx
plain_file=${tmpdir}/plain_file
echo "123" > ${plain_file}
echo "m ${req_idx} 0 ${plain_file} 0 ${move_dst}" >> ${req_file}

delete_on_exut_file=/tmp/delete_on_exut_file #it should not be in/
echo "123" > ${delete_on_exut_file}
echo "D 0 0 ${delete_on_exut_file}" >> ${req_file}

req_sleep # always sleep before removal to allow others finish their job
increment_idx
echo "d ${req_idx} 0 ${copy_dst}" >> ${req_file}
increment_idx
echo "d ${req_idx} 0 ${src}" >> ${req_file}

req_sleep # always sleep before removal to allow others finish their job
increment_idx
echo "d ${req_idx} 0 ${tmpdir}" >> ${req_file}

req_sleep # sleep after removal as well

increment_idx
make_requests ../../.. ${req_file} r

echo "i 0" >> ${req_file}
echo "o 0 0 access=fast" >> ${req_file}
echo "o 0 0 access=full" >> ${req_file}

# and now again wrong requests
echo "%" >> ${req_file} # wrong type of request
echo "s 0 100 asdasdasd" >> ${req_file} # real path length is less
echo "s 0 1 asdasdasd" >> ${req_file} # real path length is more
echo "m 0 0 asdasdasd1 100 asdasdasd2" >> ${req_file} # real path2 length is less
echo "m 0 0 asdasdasd1 1 asdasdasd2" >> ${req_file} # real path2 length is more
echo "m 0 0 qwe" >> ${req_file} # path 2 is absent

req_sleep # sleep before exit to allow threads finish their work
echo "q" >> ${req_file}

arc=`arch`
if [ "${arc}" = "sun4" ]; then
    arc="sparc"
else
    if [ "${arc}" = "x86_64" -o "${arc}" = "i86pc" ]; then
        arc="x86"
    else
        echo "Architecture ${arc} is not supported for this test"
        exit 4
    fi
fi

os=`uname -s`

echo "----- temporary dir ${tmpdir} content before starting fs_server:"
find ${tmpdir} -ls
echo "----- requests:"
cat ${req_file}

cache_dir=/tmp/fs_server_test_cache_${USER}
echo "----- launching fs_server and feeding it with commands from ${req_file}"
echo "fs_server cache dir is ${cache_dir}"

cat ${req_file} | ../../../release/bin/${os}-${arc}/fs_server -t ${thead_count} -p -l -s -d ${cache_dir} # > ${rsp_file}

if [ -f ${delete_on_exut_file} ]; then
    echo "Warning: file ${delete_on_exut_file} was not deleted on exit!"
else 
    echo "Bravo! file ${delete_on_exut_file} has been deleted on exit!"
fi

#just in case remove temp directory
if [ -d ${tmpdir} ]; then
    echo "Something went wrong: fs_server should have already removed ${tmpdir}"
    echo "Don't worry, we'll now remove it."
    rm -rf ${tmpdir}
    echo "Removed"
fi
