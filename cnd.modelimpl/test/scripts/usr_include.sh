#!/bin/bash

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.

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
