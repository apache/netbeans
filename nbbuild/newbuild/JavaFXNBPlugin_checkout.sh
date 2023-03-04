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

# This script is purposed
#to getting all needed sources
# to start Java FX Netbeans Plugin builds


#Check if there is the main repo 
cd $WORKSPACE
if [ ! -d main ] ;
then 
hg clone  http://hg.netbeans.org/main
else
	cd $WORKSPACE/main
	if [ -d .hg ] 
       	then 
		hg pull http://hg.netbeans.org/main
		hg update -C
	else
		cd $WORKSPACE
		hg clone  http://hg.netbeans.org/main
	fi
fi

#Check if there is the repo main/contrib
cd $WORKSPACE/main

if [ ! -d contrib ] ;
then 
hg clone  http://hg.netbeans.org/main/contrib
else
	cd $WORKSPACE/main/contrib
	if [ -d .hg ] 
       	then 
		hg pull http://hg.netbeans.org/main/contrib
		hg update -C
	else
		cd $WORKSPACE
		hg clone  http://hg.netbeans.org/maincontrib
	fi
fi
