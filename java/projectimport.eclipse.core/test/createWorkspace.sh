#!/bin/bash

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

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

