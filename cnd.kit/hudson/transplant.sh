#!/bin/sh
# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
# This script is used to transplant changes from one repository to another.
# Only changes made by listed authors are transplanted.
#
# This script was writtent to be used in hudson job.
#
# Script uses following ENV variables (could be set as hudson job parameters):
#
# - Names of authors whose changes should be transplanted
#   varname: 		AUTHORS
#   value_example: 	
#
# - Path to store revision to start searching for revisions from ...
#   varname:		REVFILE
#   value_example:	/var/hudson/hudson-transplant-cs
#
# - Path to up-to-date hudson-local repository with SOURCE repository (from which to transplant)
#   varname:		SOURCE_REPOSITORY
#   value_example:	/export/hudson/jobs/update-release701/workspace
#
# - Path to up-to-date hudson-local repository with SOURCE repository (from which to transplant)
#   varname:		SOURCE_BRANCH
#   value_example:	release701
#
#

#!/bin/sh

AWK=/bin/nawk

STATUS=0

## Functions

rollback() {
   echo "Rollback any unpushed changes ... "

   for r in `hg out -n | sed 's/^changeset.*:\([0-9a-f]*\)$/\1/p;d'`; do
      echo hg strip $r
      hg --config extensions.mq= strip -n -f $r || return 1
   done

   echo OK
   return 0
}

fail() {
  echo FAIL.
  STATUS=1
  exit 1
}

fail_rollback() {
  rollback
  fail
}

## BEGIN

if [ "${AUTHORS}" = "" ]; then
   echo AUTHORS variable is empty.
   fail
fi

touch $REVFILE

echo "----------------------------------------------------------"
echo Transplanting changes made by ${AUTHORS} in ${SOURCE_BRANCH} branch of releases repository to cnd-main repository.

# Be sure that there are no outgoing changes in current workspace
rollback || fail

cp ${REVFILE} ${REVFILE}.tmp
REV=`cat ${REVFILE}.tmp`

TEMPFILE=`mktemp`
trap "rm -f ${TEMPFILE}; rm -f ${REVFILE}.tmp; exit \${STATUS}" 1 2 15 EXIT

echo Getting revisions from ${REV} to tip ...
echo "----------------------------------------------------------"
hg log -M -b ${SOURCE_BRANCH} -r $REV:tip --template "{rev}\t{author}\n\t{desc|stringify}\n\n" --cwd "${SOURCE_REPOSITORY}" | ${AWK} -v FILTER="${AUTHORS}" '/^[0-9]/ {if ($0 ~ FILTER) {print;getline;print}}' | tee ${TEMPFILE}
echo "----------------------------------------------------------"

if [ ! -s ${TEMPFILE} ]; then
   echo No new changesets produced by any of ${AUTHORS}
   echo DONE
   exit 0
fi

for i in `cat ${TEMPFILE} | ${AWK} '/^[0-9]/{print $1}'`; do
   echo $i > ${REVFILE}.tmp
   echo "Attempt to transplant changeset:"
   hg log -b ${SOURCE_BRANCH} -r $i --template "#{rev} by {author} - {desc}\n" --cwd "${SOURCE_REPOSITORY}"
   echo hg transplant $i --log -b ${SOURCE_BRANCH} -s ${SOURCE_REPOSITORY}
   hg --config extensions.transplant= transplant --log -b ${SOURCE_BRANCH} -s ${SOURCE_REPOSITORY} $i || fail_rollback
   echo OK
   echo "----------------------------------------------------------"
done

ant -Djava.awt.headless=true clean build-nozip || fail_rollback

echo "----------------------------------------------------------"
echo Pushing changes...
echo "----------------------------------------------------------"
cat ${TEMPFILE}
echo "----------------------------------------------------------"
echo hg push
hg push || fail_rollback

LAST_REV=`cat ${REVFILE}.tmp`
NEW_REV=`expr ${LAST_REV} + 1`
echo ${NEW_REV} > ${REVFILE}


