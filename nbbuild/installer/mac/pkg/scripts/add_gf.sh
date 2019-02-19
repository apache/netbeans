#!/bin/sh -x

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
nb_dir=$1
gf_dir=$2

echo Changing netbeans.conf in $nb_dir
echo GlassFish is in $gf_dir

if [ "$nb_dir" = "" ] || [ "$gf_dir" = "" ]
then
  exit
fi
if [ -d "$nb_dir" ] && [ -d "$gf_dir" ]
then
  cd "$nb_dir" 
  cd Contents/Resources/NetBeans*/etc
  if [ -f netbeans.conf ]
  then
    echo netbeans.conf found: `pwd`/netbeans.conf
    cp netbeans.conf netbeans.conf_orig_gf
    cat netbeans.conf_orig_gf  | sed -e 's|netbeans_default_options=\"|netbeans_default_options=\"-J-Dcom.sun.aas.installRoot='$gf_dir' |' > netbeans.conf
  else
    echo No netbeans.conf in: `pwd`
  fi
fi

