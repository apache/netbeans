#!/bin/sh
#
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
#
nb_dir=$1
tc_dir=$2

echo Changing netbeans.conf in $nb_dir
echo Tomcat is in $tc_dir

if [ "$nb_dir" = "" ] || [ "$tc_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$tc_dir" ]
then
  cd "$nb_dir"
  cd Contents/Resources/NetBeans*/
  curdir=`pwd`
  dirname=`dirname "$0"`
  jdk_home=`"$dirname"/get_current_jdk.sh`
  "$jdk_home"/bin/java -cp \
                           platform/core/core.jar:platform/core/core-base.jar:platform/lib/boot.jar:platform/lib/org-openide-modules.jar:platform/core/org-openide-filesystems.jar:platform/lib/org-openide-util.jar:platform/lib/org-openide-util-lookup.jar:platform/lib/org-openide-util-ui.jar:enterprise/modules/org-netbeans-modules-j2eeapis.jar:enterprise/modules/org-netbeans-modules-j2eeserver.jar:enterprise/modules/org-netbeans-modules-tomcat5.jar \
                           \
                           org.netbeans.modules.tomcat5.registration.AutomaticRegistration \
                           --add \
                           "$curdir/nb" \
                           "$tc_dir"
  val=$?

  if [ $val -eq 0 ] ; then
     echo "Tomcat installed at $tc_dir integrated with NetBeans installed at $nb_dir"
  else
     echo "Tomcat installed at $tc_dir was not integrated with NetBeans installed at $nb_dir, error code is $val"
  fi
fi

