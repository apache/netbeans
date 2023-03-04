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
cluster_id="$1"

script_dir=`dirname "$0"`

. "$script_dir"/env.sh


cd "$NETBEANS_INSTALL_DIR"/Contents/Resources/NetBeans*/etc

if [ -z `cat netbeans.clusters | grep "^$cluster_id$"` ] ; then
	echo "$cluster_id" >> netbeans.clusters
fi