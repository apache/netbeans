# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
#
# Oracle and Java are registered trademarks of Oracle and/or its affiliates.
# Other names may be trademarks of their respective owners.
#
# The contents of this file are subject to the terms of either the GNU
# General Public License Version 2 only ("GPL") or the Common
# Development and Distribution License("CDDL") (collectively, the
# "License"). You may not use this file except in compliance with the
# License. You can obtain a copy of the License at
# http://www.netbeans.org/cddl-gplv2.html
# or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
# specific language governing permissions and limitations under the
# License.  When distributing the software, include this License Header
# Notice in each file and include the License file at
# nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
# particular file as subject to the "Classpath" exception as provided
# by Oracle in the GPL Version 2 section of the License file that
# accompanied this code. If applicable, add the following below the
# License Header, with the fields enclosed by brackets [] replaced by
# your own identifying information:
# "Portions Copyrighted [year] [name of copyright owner]"
#
# Contributor(s):
#
# The Original Software is NetBeans. The Initial Developer of the Original
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
# Microsystems, Inc. All Rights Reserved.
#
# If you wish your version of this file to be governed by only the CDDL
# or only the GPL Version 2, indicate your decision by adding
# "[Contributor] elects to include this software in this distribution
# under the [CDDL or GPL Version 2] license." If you do not indicate a
# single choice of license, a recipient has the option to distribute
# your version of this file under either the CDDL, the GPL Version 2 or
# to extend the choice of license to its licensees as provided above.
# However, if you add GPL Version 2 code and therefore, elected the GPL
# Version 2 license, then the option applies only if the new code is
# made subject to such option by the copyright holder.

LINKER-Linux := gcc
LINKER-SunOS := cc
LINKER-Mac_OS_X := gcc

CC-Linux := gcc
CC-SunOS := cc
CC-Mac_OS_X := gcc
	
ifdef PREVISE
    PREVISE_FLAG = -xprevise=yes 
    CODEAN_TARGET = codean
else
    PREVISE_FLAG = 
    CODEAN_TARGET = 
endif

DEBUG := -g

CFLAGS-COMMON := -c ${DEBUG} -fPIC -D_GNU_SOURCE
CFLAGS-Linux := ${CFLAGS-COMMON} -Wreturn-type -Wimplicit -Werror -std=c99 ${PREVISE_FLAG}
CFLAGS-SunOS := ${CFLAGS-COMMON} -xc99 -errwarn ${PREVISE_FLAG}
CFLAGS-Mac_OS_X := ${CFLAGS-COMMON}

LDFLAGS-LIB-Linux := -shared -ldl -lrt
LDFLAGS-LIB-SunOS := -shared -ldl -lrt -lsocket
LDFLAGS-LIB-Mac_OS_X := -G -ldl -lrt

LDFLAGS-APP-Linux := -lrt -lnsl -lresolv
LDFLAGS-APP-SunOS := -lrt -lnsl -lsocket -lresolv
LDFLAGS-APP-Mac_OS_X := -lrt -lnsl -lsocket -lresolv

ifdef 64BITS
    FLAG32_64 := -m64
else
    FLAG32_64 := -m32
endif

# The following sets UNAME_S to a platform dependent string. Since
# this gets evaluated each use, its assigned to "OSNAME" and that
# is the actual value used in the rest of the Makefile.
UNAME_S := $(shell uname -s)
UNAME_P := $(shell uname -p)
UNAME_M := $(shell uname -m)

OSNAME = $(UNAME_S)
ifeq ($(findstring _NT-,$(UNAME_S)), _NT-)  # catches Cygwin and MinGW
	OSNAME = Windows
endif
ifeq (Darwin,$(UNAME_S))
	OSNAME = Mac_OS_X
endif

TRACE := 0
CC := ${CC-${OSNAME}}
LINKER := ${LINKER-${OSNAME}}
CFLAGS := ${CFLAGS-${OSNAME}} ${FLAG32_64} -DTRACE=${TRACE} ${EXTRA_CFLAGS}
LDFLAGS_LIB := ${LDFLAGS-LIB-${OSNAME}} ${FLAG32_64} ${DEBUG} ${PREVISE_FLAG}
LDFLAGS_APP := ${LDFLAGS-APP-${OSNAME}} ${FLAG32_64} ${DEBUG} ${PREVISE_FLAG}

ifdef 64BITS
    PLATFORM_64 := _64
    TEST_ENV_PARAM := 64
else
    PLATFORM_64 :=
    TEST_ENV_PARAM := 
endif

# map different values of `uname -p` and/or `uname -m` into standard postfixes
TMP-i386   :=  x86
TMP-i686   :=  x86
TMP-i86pc  :=  x86
TMP-x86_64 :=  x86
TMP-sparc  :=  sparc
TMP-sparc64 :=  sparc
TMP-sun4u  :=  sparc

ifeq ($(UNAME_P), unknown)  # for my Ubuntu 10.4 on Intel I5 it says "unknown" :(
	UNAME_P := i386
endif

PLATFORM = ${TMP-${UNAME_P}}${PLATFORM_64}
ifeq (${PLATFORM}, )
    PLATFORM = ${TMP-${UNAME_M}}${PLATFORM_64}
endif

testit :sh= uname -s

DIST_BASE=../release/bin
BUILD_BASE="./build"

DIST=${DIST_BASE}/$(OSNAME)-$(PLATFORM)
BUILD=${BUILD_BASE}/$(OSNAME)-$(PLATFORM)

INSTALLDIR=../../nbbuild/netbeans/cnd/bin

all: build_impl install ${CODEAN_TARGET}

install: build_impl
	mkdir -p ${INSTALLDIR}
	cp -r ${DIST} ${INSTALLDIR}

build_impl: check init ${DIST}/rfs_preload.so ${DIST}/rfs_controller ${DIST}/rfs_test_env summarise

codean:
	@(echo; echo "========== rfs_preload.so static analysis =========="; echo)
	@codean -s ${DIST}/rfs_preload.so
	@(echo; echo "========== rfs_controller static analysis =========="; echo)
	@codean -s ${DIST}/rfs_controller
	@(echo; echo HTML results are:)
	@(cd ${DIST}; ls `pwd`/rfs_preload.so.static.html)
	@(cd ${DIST}; ls `pwd`/rfs_controller.static.html)
	
#object files
${BUILD}/%.o : %.c
	${CC} ${CFLAGS} -o $@ $<

# rfs_test_env can do with implicit function declarations
${BUILD}/rfs_test_env.o : rfs_test_env.c
	${CC} ${CFLAGS} -o $@ $<

PRELOAD_OBJFILES := ${BUILD}/rfs_preload.o ${BUILD}/rfs_util.o ${BUILD}/rfs_protocol.o ${BUILD}/rfs_preload_socks.o
CONTROLLER_OBJFILES := ${BUILD}/rfs_controller.o ${BUILD}/rfs_util.o ${BUILD}/rfs_protocol.o ${BUILD}/rfs_filedata.o
TEST_ENV_OBJFILES := ${BUILD}/rfs_test_env.o
	
ifdef PREVISE
    STRIP = @echo "Not stripping" 
else
    STRIP = strip
endif

#library:
${DIST}/rfs_preload.so: ${PRELOAD_OBJFILES}
	${LINKER} ${LDFLAGS_LIB} -o ${DIST}/rfs_preload.so ${PRELOAD_OBJFILES}
	${STRIP} ${DIST}/rfs_preload.so

#appliction:
${DIST}/rfs_controller: ${CONTROLLER_OBJFILES}
	${LINKER} ${LDFLAGS_APP} -o ${DIST}/rfs_controller ${CONTROLLER_OBJFILES}
	${STRIP} ${DIST}/rfs_controller

#test environment:
${DIST}/rfs_test_env: ${TEST_ENV_OBJFILES}
	${LINKER} ${LDFLAGS_APP} -o ${DIST}/rfs_test_env ${TEST_ENV_OBJFILES}
	${STRIP} ${DIST}/rfs_test_env

summarise:
	@echo; (cd ${DIST}; echo "Build results are in the directory"; pwd; file rfs_controller rfs_preload.so rfs_test_env); echo
	@echo; echo "----------------------------------------"; echo

clean:
	rm -rf ${BUILD} ${DIST}

cleanall:
	rm -rf ${BUILD_BASE} ${DIST_BASE}

init:
	@mkdir -p ${BUILD} ${DIST}
	@echo "Using the following compiler: `which $(CC)`"

check:
	@if [ -n "$(testit)" ]; \
	then \
		echo "Error: You must use GNU make"; \
		exit 1; \
	fi

test_env: all
	sh rfs_test_env.sh

test_filedata: test_filedata_build
	${BUILD}/test_rfs_filedata

test_filedata_build: all
	${CC} ${CFLAGS} -DTRACE=1 -o ${BUILD}/test_rfs_filedata.o test/test_rfs_filedata.c
	${LINKER} ${LDFLAGS_APP} -o ${BUILD}/test_rfs_filedata ${BUILD}/rfs_filedata.o ${BUILD}/rfs_util.o ${BUILD}/test_rfs_filedata.o

test_pkg: test_pkg_build
	${BUILD}/test_pkg_client

test_pkg_build: all
	${CC} ${CFLAGS} -o ${BUILD}/test_pkg_client.o test/test_pkg_client.c
	${LINKER} ${LDFLAGS_APP} -lpthread -o ${BUILD}/test_pkg_client ${BUILD}/test_pkg_client.o ${BUILD}/rfs_util.o ${BUILD}/rfs_protocol.o ${BUILD}/rfs_preload_socks.o

test_platform:
	@echo "UNAME_S = [${UNAME_S}]"
	@echo "UNAME_M = [${UNAME_M}]"
	@echo "UNAME_P = [${UNAME_P}]"
	@echo "OSNAME = [${OSNAME}]"
	@echo "PLATFORM = [${PLATFORM}]"
	@echo "CFLAGS = [${CFLAGS}]"
	@echo "LDFLAGS_LIB = [${LDFLAGS_LIB}]"
	@echo "LDFLAGS_APP = [${LDFLAGS_APP}]"
	@echo "${APP_NAME} = [${APP_PATH}]"
