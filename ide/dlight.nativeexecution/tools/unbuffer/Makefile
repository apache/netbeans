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

MAKEFILE = $(shell echo gmake)
MAKEFILE:sh = echo make
ROOT_DIR = $(shell pwd)
ROOT_DIR:sh = pwd

CC=gcc

SOURCES=\
	$(ROOT_DIR)/src/unbuffer.c

SRC_DIRS=\
	$(ROOT_DIR)/src

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)
INSTALL_DIR=../../release/bin/nativeexecution/$(CONF)
LINE=sh $(ROOT_DIR)/make/line

CF_Linux-x86      = $(CF_COMMON) -DLINUX -shared -fPIC -m32
EX_Linux-x86      = unbuffer.so
CF_Linux-arm      = $(CF_COMMON) -DLINUX -shared -fPIC
EX_Linux-arm      = unbuffer.so
CF_Linux-aarch64  = $(CF_COMMON) -DLINUX -shared -fPIC
EX_Linux-aarch64  = unbuffer.so
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX -shared -fPIC -m64
EX_Linux-x86_64   = unbuffer.so
CF_Linux-sparc_64 = $(CF_COMMON) -DLINUX -shared -fPIC -m64
EX_Linux-sparc_64 = unbuffer.so
CF_MacOSX-arm     = $(CF_COMMON) -DMAXOSX  -shared -target arm64-apple-macos
EX_MacOSX-arm     = unbuffer.dylib
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -shared -target x86_64-apple-macos
EX_MacOSX-x86_64  = unbuffer.dylib
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -G -fPIC -m64
EX_SunOS-sparc_64 = unbuffer.so
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -G -fPIC -m32
EX_SunOS-x86      = unbuffer.so
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -G -fPIC -m64
EX_SunOS-x86_64   = unbuffer.so
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -shared -m32
EX_Windows-x86    = unbuffer.dll
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -shared -m32
EX_Windows-x86_64 = unbuffer.dll

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc

CFLAGS=$(CF_$(CONF)) $(CFLAGS_EXTRA)
LDFLAGS=$(LF_$(CONF)) $(LDFLAGS_EXTRA)

PNAME=$(EX_$(CONF))

EXEC=$(DIST_DIR)/$(PNAME)

all: $(EXEC)
	@$(LINE) =
	@echo $(EXEC); echo
	@ls -la $(EXEC) | sed "s#$(EXEC)##"
	@FILE=`(ls /bin/file || ls /usr/bin/file) 2>/dev/null`; \
	   if [ -n "$${FILE}" ]; then $${FILE} ${EXEC} | sed "s#$(EXEC):.##"; fi
	@$(LINE) =

include $(ROOT_DIR)/make/Makefile.$(MAKEFILE)

$(EXEC): $(DIST_DIR) $(OBJS)
	@$(LINE) -
	$(LINK.c) -o $@ $(OBJS)

$(OBJ_DIRS) $(DIST_DIR):
	$(MKDIR) $@

clean: clean_deps
	$(RM) -r $(OBJ_DIR) $(DIST_DIR)

clean-all:
	$(RM) -r $(ROOT_DIR)/build
	$(RM) -r $(ROOT_DIR)/dist
	$(RM) .make.state*

install:
	cp $(EXEC) $(INSTALL_DIR)

