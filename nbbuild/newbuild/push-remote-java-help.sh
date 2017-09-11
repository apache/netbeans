#!/bin/sh

# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 2013, 2016 Oracle and/or its affiliates. All rights reserved.
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

hg pull -b $push_branch
hg up $push_branch -C
rm -rf usersguide/javahelp

# mkdir -p usersguide/javahelp/org/netbeans/modules/usersguide
# cp -r -v $pull_path/org/netbeans/modules/usersguide/* usersguide/javahelp/org/netbeans/modules/usersguide/
mkdir -p usersguide/javahelp

cp -r -v $pull_path/* usersguide/javahelp/

hg add usersguide/javahelp
hg st usersguide/javahelp

DELETED_FILES_COUNT=`hg st -d usersguide/javahelp | wc -l`
echo DELETED_FILES_COUNT: $DELETED_FILES_COUNT
if [ "$DELETED_FILES_COUNT" -gt 0 ]; then
    hg st -d usersguide/javahelp | cut -c 3- | xargs hg rm -A
fi

CHANGED_FILES_COUNT=`hg st -mar usersguide/javahelp | wc -l`
echo CHANGED_FILES_COUNT: $CHANGED_FILES_COUNT
if [ "$CHANGED_FILES_COUNT" -gt 0 ]; then
    echo $CHANGED_FILES_COUNT changes found, let start the build and push.
    ant clean build || exit 2
    echo Build succeed.

    # update to latest tip
    hg pull -u

    # encode windows-like line-endings to unix-like
    hg ci -m "new help files" -u "$commit_username" --config 'extensions.win32text=' --config 'encode.**=cleverencode:'

    # check count of really modified files for push
    OUT_COUNT=`hg parent -v --template 'files: {files}'| grep '^files:' | wc -w`
    echo OUT_COUNT: $OUT_COUNT
    if [ "$OUT_COUNT" -gt 1 ]; then
        echo "There are $OUT_COUNT outgoing changes, start pushing..."
        hg push -b $push_branch -f $push_url
        HG_RESULT=$?
        if [ $HG_RESULT == 0 ]; then
            echo Push succeed.
        else
            echo "Hg push failed: $HG_RESULT, rolling back the commit"
            hg rollback
            hg up -C
            exit $HG_RESULT;
        fi
    else
        echo "There are no outgoing changes, rolling back the commit"
        hg rollback
        hg up -C
    fi
else
    echo No changes found, no reason to push.
    hg up -C
fi
