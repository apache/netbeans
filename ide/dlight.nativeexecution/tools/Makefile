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

INSTALLDIR = /dev/null

testit :sh= uname -s

#CFLAGS-Windows_XP := -mno-cygwin
CFLAGS-Linux := -fPIC -Wl,--hash-style=sysv
CFLAGS-SunOS := -fPIC
CFLAGS-Mac_OS_X := -fPIC
LDFLAGS-SunOS := -lposix4

PLATFORM_DIR = $(CONF)

CF_Linux-x86	    = -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m32
CF_Linux-x86_64	    = -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_Linux-sparc_64   = -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_Linux-arm	    = -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -std=c99
CF_Linux-aarch64    = -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -std=c99
CF_MacOSX-arm	    = -DMAXOSX  -D_DARWIN_C_SOURCE -target arm64-apple-macos
CF_MacOSX-x86_64    = -DMAXOSX  -D_DARWIN_C_SOURCE -target x86_64-apple-macos
CF_SunOS-sparc_64   = -DSOLARIS -D__EXTENSIONS__ -m64
CF_SunOS-x86	    = -DSOLARIS -m32
CF_SunOS-x86_64	    = -DSOLARIS -m64
CF_Windows-x86	    = -DWINDOWS -m32
CF_Windows-x86_64   = -DWINDOWS -m32 # ?

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc

CFLAGS := $(CF_$(CONF)) $(CFLAGS_EXTRA)
LDFLAGS := $(LF_$(CONF)) $(LDFLAGS_EXTRA)

CC=gcc

CFLAGS += $(CFLAGS-$(OSFAMILY))
LDFLAGS += $(LDFLAGS-$(OSFAMILY))

DIST=../release/bin/nativeexecution
BUILD=./build

APP_PATH=${DIST}/${PLATFORM_DIR}

all: init privp process_start pty_open sigqueue stat

privp: init
	${CC} ${CFLAGS} -o ${APP_PATH}/$@ $@.c

process_start: init
	${CC} ${CFLAGS} -o ${APP_PATH}/$@ $@.c

pty_open: init
	${CC} ${CFLAGS} -o ${APP_PATH}/$@ $@.c

sigqueue: init
	${CC} ${CFLAGS} ${LDFLAGS} -o ${APP_PATH}/$@ $@.c

stat: init
	${CC} ${CFLAGS} -o ${APP_PATH}/$@ $@.c

clean:
	rm -rf ${APP_PATH} ${DIST}/${PLATFORM_DIR}

cleanall:
	rm -rf ${BUILD} ${DIST}

init: check
	@mkdir -p ${APP_PATH} ${DIST}/${PLATFORM_DIR}

check:
	@if [ -n "$(testit)" ]; \
	then \
		echo "Error: You must use GNU make"; \
		exit 1; \
	fi
	
	@if [ "${PLATFORM_DIR}" = "" ]; \
	then \
		echo "PLATFORM_DIR undefined"; \
		exit 1; \
	fi

