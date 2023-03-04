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

set -x

#Initialize basic scructure
DIRNAME=`dirname $0`
cd ${DIRNAME}
TRUNK_NIGHTLY_DIRNAME=`pwd`
export BUILD_DESC=trunk-nightly
source init.sh

ssh -p 222 $DIST_SERVER mkdir -p $DIST_SERVER_PATH/.$DATESTAMP
scp -P 222 -q -r -v $DIST/* $DIST_SERVER:$DIST_SERVER_PATH/.$DATESTAMP > $SCP_LOG 2>&1

ssh -p 222 $DIST_SERVER mv $DIST_SERVER_PATH/.$DATESTAMP $DIST_SERVER_PATH/$DATESTAMP

ssh -p 222 $DIST_SERVER rm $DIST_SERVER_PATH/latest.old
ssh -p 222 $DIST_SERVER mv $DIST_SERVER_PATH/latest $DIST_SERVER_PATH/latest.old
ssh -p 222 $DIST_SERVER ln -s $DIST_SERVER_PATH/$DATESTAMP $DIST_SERVER_PATH/latest
