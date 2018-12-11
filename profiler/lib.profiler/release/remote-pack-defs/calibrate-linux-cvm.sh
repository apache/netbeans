#!/bin/sh

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

# This script expects CVM_HOME to point to the correct CVM installation
# In case you need to customize it, please uncomment and modify the following lines

# CVM_HOME=/opt/cvm
# export CVM_HOME

OLD_PWD=`pwd`
cd `dirname $0`
INSTALL_DIR=`pwd`
cd $OLD_PWD
unset OLD_PWD

$CVM_HOME/bin/cvm -Djava.library.path=$CVM_HOME/lib:$INSTALL_DIR/../lib/deployed/cvm/linux  -classpath $INSTALL_DIR/../lib/jfluid-server.jar:$INSTALL_DIR/../lib/jfluid-server-cvm.jar org.netbeans.lib.profiler.server.ProfilerCalibrator
