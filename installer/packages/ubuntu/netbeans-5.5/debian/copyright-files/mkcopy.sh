#!/bin/bash

#
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
# mkcopy.sh will construct a debian/copyright file
# usage: mkcopy.sh copyright preamble pkg-notice pkg-license \
#          upstream-notice upstream-license [third-party-notices...]
#
# BACKGROUND
# In some cases it is desirable to clearly identify the license
# for debian packaging as distinct from that of upstream (and any
# possible third party components).  This script has been created
# the purpose of generating a clean debian/copyright with the
# conventions as suggested by debian-legal:
#  http://lists.debian.org/debian-legal/2006/04/msg00251.html
#
# The inputs to this script are filenames for:
# copyright - the output copyright file to be generated
# preamble - the packaging preamble file (debianizer, upstream source)
# pkg-notice - copyright notice for the packaging
# pkg-license - license for the packaging
# upstream-notice - copyright notice for upstream
# upstream-license - license for upstream
#   it is important to note that Debian systems must *not* include the
#   full GPL, but rather a notice and reference to
#   /usr/share/common-licenses/GPL
# third-party-notices - (optional) third party license(s)

program=`basename $0`
sep="  - - - - -  "

usage()
{
  rv=$1
  cat >&2 <<-EOF
    usage: $program copyright preamble pkg-notice pkg-license upstream-notice upstream-license [third-party-notices...]
	EOF
  exit $rv
}

checkfiles() {
  for i in $*; do
    if [ ! -f $i ]; then
      echo "${program}: cannot find file: $i"
      exit 1
    fi
  done
}

savefile() {
  # save previous version
  if [ -f $1 ]; then
    mv $1 $1.1
  fi
}

generate() {
  rm -f $copyright
  cat $pkg_preamble >> $copyright
  echo " " >> $copyright

  echo "$sep copyright notice and license for Debian packaging $sep" >> $copyright
  echo " " >> $copyright
  cat $pkg_notice >> $copyright
  echo " " >> $copyright
  cat $pkg_license >> $copyright
  echo " " >> $copyright

  echo "$sep copyright notice and license for upstream $sep" >> $copyright
  echo " " >> $copyright
  cat $upstream_notice >> $copyright
  echo " " >> $copyright
  cat $upstream_license >> $copyright
  echo " " >> $copyright

  if [ "$#" -gt 0 ]; then
    echo "$sep third party copyright notice(s) and license(s) $sep" >> $copyright
  fi

  while [ "$#" -gt 0 ]; do
    echo " " >> $copyright
    cat $1 >> $copyright
    echo " " >> $copyright
    echo "$sep third party copyright notice(s) and license(s) $sep" >> $copyright
    shift
  done
}

[ "$#" -ge 6 ] || usage 1
copyright=$1
shift
checkfiles $*
pkg_preamble=$1
shift
pkg_notice=$1
shift
pkg_license=$1
shift
upstream_notice=$1
shift
upstream_license=$1
shift
# savefile $copyright
generate $*

