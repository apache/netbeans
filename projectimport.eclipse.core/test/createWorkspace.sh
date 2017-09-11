#!/bin/bash

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
#
# Portions Copyrighted 2012 Sun Microsystems, Inc.
#
# Script: createWorkspace.sh
#
# Convenient script for generating workspace with metadata we needs for tests.
#
# Usage:   ./createWorkspace.sh -w workspace -p "projects_separated_by_:"
# Example: ./createWorkspace.sh -w ~/workspace-3.0 -p "p1:p2:p3"
#
# cDate: 2004/12/14
# mDate: 2006/01/05
#
# Author: martin.krauskopf (martin.krauskopf at sun.com)
# Editor: VIM - Vi IMproved 6.3 (2004 June 7, compiled Jun 26 2004 15:03:59)

# ==================================================================== #
# =================== Don't need to touch following ================== #
# ==================================================================== #

function processParams()
{
  while [ $# != 0 ]; do
    case "$1" in
      --workspace|-w)
        # workspace
        WORKSPACE="$2"
        shift
        ;;
      --projects|-p)
        # projects
        PROJECTS="`echo $2 | sed 's/:/ /g'`"
        echo "Projects: \"$PROJECTS\""
        shift
        ;;
      *)
        echo -e "\nERROR: \"$1\": invalid argument"
        exit 1
        ;;
    esac
    shift
  done
}

function fail {
  echo "SCRIPT_FAILED: $1"
  exit 2
}

# ==================================================================== #

processParams $@

if [ -z "$WORKSPACE" -o -z "$PROJECTS" ]; then
  echo "Usage: `basename $0` --workspace|-w <workspace> --projects|-p <project>..."
  exit 3
fi

[ -d "$WORKSPACE" ] || fail "$WORKSPACE must exist"
WORKSPACE_PLUGINS=".metadata/.plugins"
[ -d "$WORKSPACE/$WORKSPACE_PLUGINS" ] || fail "$WORKSPACE/$WORKSPACE_PLUGINS must exist"

WORKSPACE_DUMP="$PWD/`dirname $0`/`basename $WORKSPACE`"
mkdir "$WORKSPACE_DUMP" || fail "$WORKSPACE_DUMP cannot be created"

cd "$WORKSPACE"
cp -a --parent "$WORKSPACE_PLUGINS/org.eclipse.core.runtime/.settings/org.eclipse.jdt.launching.prefs" \
    "$WORKSPACE_PLUGINS/org.eclipse.core.runtime/.settings/org.eclipse.jdt.core.prefs" \
    "$WORKSPACE_PLUGINS/org.eclipse.core.resources/.projects" \
    $PROJECTS \
    "$WORKSPACE_DUMP" || fail "Copying failed"

echo "INFO: \"$WORKSPACE_DUMP\" was created on the base of \"$WORKSPACE\""

