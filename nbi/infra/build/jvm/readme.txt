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

Helper util for creating bundled JVM.
Should be run on each required platform.
To specify x64 target, environment variable TARGET_PLATFORM can be set to solaris-sparcv9 or solaris/linux/windows-amd64.
Another option - just run Ant on x64 JVM if it is possible.
build-private.sh file is missing but contains only one line with java builds host (JVM_BUILDS_HOST=...).

Notes: UnzipSFX Version 5.51 (22 May 2004) on Linux doesn`t work with symlinks so v.5.50 is used.
