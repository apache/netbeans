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

function resolve_symlink() {
    # get passed file
    local toResolve="$1"
    while [ -h "$toResolve" ]; do
        ls=`ls -ld "$toResolve"`
        link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
        if expr "$link" : '^/' 2> /dev/null >/dev/null; then
            # this is absolute path
            toResolve="$link"
        else
            # this is relative path
            toResolve="`dirname "$toResolve"`/$link"
        fi
    done
    echo $toResolve
}

function absolute_dir() {
    # get passed directory
    local inDir="$1"
    # remember current dir
    local oldDir=`pwd`
    # get the resolved dir
    cd $inDir
    local resolvedDir=`pwd`
    # restore dir
    cd $oldDir
    echo $resolvedDir
}

function sys_includes() {
    local lang="$1"
    # extract predefined system include directories
    local sys_incl=`g++ -E -v -x ${lang} /dev/null  2>&1 | awk '\
		BEGIN { cnt=0; inside = 0; } \
		/#include "..." search starts here:/ { inside = 1; } \
		/#include <...> search starts here:/ { inside = 1; } \
		/End of search list/ { inside = 0; } \
		/^[^#].*/ { if( inside ) print $1 }'`
    echo ${sys_incl}
}

function header_files() {
    local folder="${1}"
    local direct_headers=`find ${folder} -type f | grep -v "/bits/"`
    local headers="$direct_headers"
    echo ${headers}
}

function symlinks_in_dir() {
    local folder="${1}"
    local sym_links=`find ${folder} -type l -a ! -name "*.h"`
    local out_dirs=""
    for link in $sym_links; do
        local resolved_link=`resolve_symlink $link`
        if [ -d $resolved_link ]; then
            resolved_link=`absolute_dir $resolved_link`
            out_dirs=`echo $out_dirs $resolved_link`
        fi
    done
    echo $out_dirs
}

function add_headers() {
    local incl_dir=$1
    local out_file=$2
    echo adding headers of directory $incl_dir
    local headers=`header_files ${incl_dir}`
    for header in $headers; do
        echo "#include <${header}>" >> $out_file
    done
}

function create_include_file_for_language() {
    local lang="${1-c++}"
    local out_file="${2-out_file.cpp}"
    local sys_includes=`sys_includes ${lang}`
    echo "/* this is generated file of all system includes for --${lang}-- */" > $out_file
 
    for incl_dir in $sys_includes; do
        echo "/* headers from ${incl_dir} */" >> $out_file
        incl_dir=`resolve_symlink $incl_dir`
        echo "/* include directory was resoved into ${incl_dir} */" >> $out_file
        add_headers "${incl_dir}" "$out_file"
        local link_dirs=`symlinks_in_dir $incl_dir`
        if [ -n "$link_dirs" ]; then
            echo symlinked content of $incl_dir is "$link_dirs"
            for link_dir in $link_dirs; do
                echo "/* symlinked include directory was resoved into ${link_dir} */" >> $out_file      
                add_headers "${link_dir}" "$out_file"
            done
        fi
    done
}

echo C++ includes
out_cpp_file="${1-out_file.cpp}"
create_include_file_for_language c++ $out_cpp_file

#echo C includes
#out_c_file="${2-out_file.c}"
#create_include_file_for_language c $out_c_file
