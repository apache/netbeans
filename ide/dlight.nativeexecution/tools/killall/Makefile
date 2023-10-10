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
OS = $(shell uname -s)
OS:sh = uname -s
ARCH = $(shell arch)
ARCH:sh = arch

ifeq (armv, $(shell expr substr `arch` 1 4))
    OS_ARCH = $(OS)-arm
else 
  ifeq (sparc64, $(ARCH))
    OS_ARCH = $(OS)-sparc
  else
    OS_ARCH = $(OS)-x86
  endif
endif

PNAME=killall

CC=gcc

SOURCES_SunOS=\
	$(ROOT_DIR)/src/solaris/pfind.c

SOURCES_Linux=\
	$(ROOT_DIR)/src/linux/pfind.c

SOURCES_Mac=\
	$(ROOT_DIR)/src/macosx/pfind.c

SOURCES_SunOS-x86=$(SOURCES_SunOS)
SOURCES_SunOS-x86_64=$(SOURCES_SunOS)
SOURCES_SunOS-sparc_64=$(SOURCES_SunOS)
SOURCES_Linux-x86=$(SOURCES_Linux)
SOURCES_Linux-arm=$(SOURCES_Linux)
SOURCES_Linux-aarch64=$(SOURCES_Linux)
SOURCES_Linux-x86_64=$(SOURCES_Linux)
SOURCES_Linux-sparc_64=$(SOURCES_Linux)
SOURCES_MacOSX-x86=$(SOURCES_Mac)
SOURCES_MacOSX-x86_64=$(SOURCES_Mac)

SOURCES=\
	$(ROOT_DIR)/src/error.c \
	$(ROOT_DIR)/src/killall.c \
	$(ROOT_DIR)/src/options.c \
	$(SOURCES_$(CONF))

SRC_DIRS=\
	$(ROOT_DIR)/src \
	$(ROOT_DIR)/src/linux \
	$(ROOT_DIR)/src/solaris \
	$(ROOT_DIR)/src/macosx

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/dist/$(CONF)
INSTALL_DIR=../../release/bin/nativeexecution/$(CONF)
LINE=sh $(ROOT_DIR)/make/line

EXEC=$(DIST_DIR)/$(PNAME)

CF_Linux-x86      = $(CF_COMMON) -DLINUX -m32
CF_Linux-arm      = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -std=c99
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32

CF_Linux-x86_64   = $(CF_COMMON) -DLINUX -m64
CF_Linux-aarch64  = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -std=c99
CF_Linux-sparc_64 = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_MacOSX-arm     = $(CF_COMMON) -DMACOSX  -target arm64-apple-macos
CF_MacOSX-x86_64  = $(CF_COMMON) -DMACOSX  -target x86_64-apple-macos
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc
LF_SunOS-x86      = -lrt
LF_SunOS-x86_64   = -lrt
LF_SunOS-sparc_64 = -lrt

CFLAGS=$(CF_$(CONF)) $(CFLAGS_EXTRA)
LDFLAGS=$(LF_$(CONF)) $(LDFLAGS_EXTRA)

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

install:
	cp $(EXEC) $(INSTALL_DIR)

