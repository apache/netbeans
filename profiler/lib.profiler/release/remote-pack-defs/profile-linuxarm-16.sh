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

# This script expects JAVA_HOME to point to the correct JDK 6.0+ installation
# In case you need to customize it, please uncomment and modify the following lines

# JAVA_HOME=/opt/java/jdk1.6.0_02
# export JAVA_HOME

# Determine the location of the profile script as an absolute directory
ORIG_DIR=`pwd`
PROG_NAME=`type $0 | awk '{print $3}'`
INSTALL_DIR=`dirname $PROG_NAME`
cd $INSTALL_DIR
INSTALL_DIR=`pwd`
cd $ORIG_DIR

$JAVA_HOME/bin/java -agentpath:$INSTALL_DIR/../lib/deployed/jdk16/linux-arm/libprofilerinterface.so=$INSTALL_DIR/../lib/,5140 $@
