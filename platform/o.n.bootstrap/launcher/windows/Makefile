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

#  Main targets can be executed directly, and they are:
#  
#     build                    build a specific configuration
#     clean                    remove built files from a configuration
#     clobber                  remove all built files
#     all                      build all configurations
#     help                     print help mesage
#  
#  Targets .build-impl, .clean-impl, .clobber-impl, .all-impl, and
#  .help-impl are implemented in nbproject/makefile-impl.mk.
#
# NOCDDL

# Environment 
MKDIR=mkdir
CP=cp
CCADMIN=CCadmin
RANLIB=ranlib



# build
build: .build-post-$(CONF)

.build-pre:
# Add your pre 'build' code here...

.build-post-nbexec: .build-impl nbexecexe.cpp nbexecloader.h utilsfuncs.cpp nbexec_exe.rc
	windres.exe -Ocoff nbexec_exe.rc nbexec_exe.res
	g++ -s -mno-cygwin -Wl,--nxcompat -Wl,--dynamicbase -Wl,--no-seh -DNBEXEC_DLL=\"nbexec.dll\" nbexecexe.cpp utilsfuncs.cpp nbexec_exe.res -o nbexec.exe
	cp nbexec.exe ../../../nbbuild/netbeans/platform/lib/
	cp nbexec.dll ../../../nbbuild/netbeans/platform/lib/
	
.build-post-nbexec64: .build-impl nbexecexe.cpp nbexecloader.h utilsfuncs.cpp nbexec_exe.rc
	x86_64-w64-mingw32-windres.exe -Ocoff nbexec_exe.rc nbexec_exe64.res 
	x86_64-w64-mingw32-g++.exe -m64 -s -mno-cygwin -Wl,--nxcompat -Wl,--dynamicbase -DNBEXEC_DLL=\"nbexec64.dll\" -static-libgcc -static-libstdc++ nbexecexe.cpp utilsfuncs.cpp nbexec_exe64.res -o nbexec64.exe 
	cp nbexec64.exe ../../../nbbuild/netbeans/platform/lib/
	cp nbexec64.dll ../../../nbbuild/netbeans/platform/lib/



# clean
clean: .clean-post-$(CONF)

.clean-pre:
# Add your pre 'clean' code here...

.clean-post-nbexec: .clean-impl
	rm -f nbexec_exe32.res nbexec32.exe
	
.clean-post-nbexec64: .clean-impl
	rm -f nbexec_exe64.res nbexec64.exe



# clobber
clobber: .clobber-post

.clobber-pre:
# Add your pre 'clobber' code here...

.clobber-post: .clobber-impl
# Add your post 'clobber' code here...



# all
all: .all-post

.all-pre:
# Add your pre 'all' code here...

.all-post: .all-impl
# Add your post 'all' code here...



# help
help: .help-post

.help-pre:
# Add your pre 'help' code here...

.help-post: .help-impl
# Add your post 'help' code here...



# include project implementation makefile
include nbproject/Makefile-impl.mk
