#!/bin/sh

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
