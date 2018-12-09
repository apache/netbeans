#!/bin/bash

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2007, 2016 Oracle and/or its affiliates. All rights reserved.
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
#
# Contributor(s):

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

