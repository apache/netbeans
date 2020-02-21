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

ifeq (Darwin,$(OS))
  OS = MacOSX
endif

ifeq (armv, $(shell expr substr `arch` 1 4))
    OS_ARCH = $(OS)-arm
else 
  ifeq (sun4, $(ARCH))
    OS_ARCH = $(OS)-sparc
  else 
    ifeq (sparc64, $(ARCH))
      OS_ARCH = $(OS)-sparc
    else
      OS_ARCH = $(OS)-x86
    endif
  endif
endif

ifdef 64BITS
    CONF=$(OS_ARCH)_64
else
    CONF=$(OS_ARCH)
endif


PNAME=fs_server

CC_COMMON=gcc

CC_SOLARIS=cc

CC_Linux-x86      = $(CC_COMMON)
CC_Linux-x86_64   = $(CC_COMMON)
CC_Linux-arm      = $(CC_COMMON)
CC_Linux-sparc    = $(CC_COMMON)
CC_MacOSX-x86     = clang 
CC_MacOSX-x86_64  = clang 
CC_SunOS-sparc    = $(CC_SOLARIS)
CC_SunOS-sparc_64 = $(CC_SOLARIS)
CC_SunOS-x86      = $(CC_SOLARIS)
CC_SunOS-x86_64   = $(CC_SOLARIS)
CC_Windows-x86    = $(CC_COMMON)
CC_Windows-x86_64 = $(CC_COMMON)
CC_FreeBSD-x86    = $(CC_COMMON)
CC_FreeBSD-x86_64 = $(CC_COMMON)
CC=$(CC_$(CONF))

ifdef PARFAIT
   CC=parfait-gcc
endif

ifdef PREVISE
    PREVISE_FLAG = -xprevise=yes 
    CODEAN_TARGET = codean
else
    PREVISE_FLAG = 
    CODEAN_TARGET = 
endif

ifdef ASSERTIONS
    NDEBUG_FLAG =
else
    NDEBUG_FLAG = -DNDEBUG
endif

SOURCES=\
	$(ROOT_DIR)/src/fs_server.c \
	$(ROOT_DIR)/src/queue.c \
	$(ROOT_DIR)/src/blocking_queue.c \
	$(ROOT_DIR)/src/util.c \
	$(ROOT_DIR)/src/dirtab.c \
	$(ROOT_DIR)/src/settings.c \
	$(ROOT_DIR)/src/array.c

SRC_DIRS=\
	$(ROOT_DIR)/src

MKDIR=mkdir -p
OBJ_DIR=$(ROOT_DIR)/build/$(CONF)
DIST_DIR=$(ROOT_DIR)/../../release/bin/$(CONF)
INSTALL_DIR=../../../nbbuild/netbeans/dlight/bin$(CONF)
LINE=sh $(ROOT_DIR)/make/line

EXEC=$(DIST_DIR)/$(PNAME)


#CF_COMMON         = --std=c99 -s -O2
CF_COMMON         = --std=c99 -g -Wall -Werror -D_REENTRANT ${NDEBUG_FLAG}
CF_SOLARIS         = -std=c99 -g -errwarn -D_REENTRANT ${NDEBUG_FLAG}

CF_Linux-arm      = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700
CF_Linux-x86      = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m32
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_Linux-sparc    = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_MacOSX-x86     = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m32
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -D_DARWIN_C_SOURCE -m64
CF_SunOS-sparc    = $(CF_SOLARIS) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_SunOS-sparc_64 = $(CF_SOLARIS) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_SunOS-x86      = $(CF_SOLARIS) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_SunOS-x86_64   = $(CF_SOLARIS) -DSOLARIS -D__EXTENSIONS__ -D_POSIX_PTHREAD_SEMANTICS -m64
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32
CF_FreeBSD-x86    = $(CF_COMMON) -D_GNU_SOURCE -m32
CF_FreeBSD-x86_64 = $(CF_COMMON) -D_GNU_SOURCE -m64

LF_Linux-x86      = $(LF_COMMON)
LF_Linux-x86_64   = $(LF_COMMON)
LF_Linux-arm      = $(LF_COMMON) -static

CFLAGS=$(CF_$(CONF)) ${PREVISE_FLAG}
LDFLAGS=$(LF_$(CONF)) ${PREVISE_FLAG}

ifdef PREVISE
  STRIP = @echo "Not stripping" 
else
  STRIP = strip $(EXEC)
endif

all: $(EXEC) strip summarise ${CODEAN_TARGET}

all-debug: $(EXEC) summarise

summarise:
	@$(LINE) =
	@(cd `dirname $(EXEC)`; echo "`pwd`/`basename $(EXEC)`"); echo
	@#echo $(EXEC); echo
	@ls -la $(EXEC) | sed "s#$(EXEC)##"
	@FILE=`(ls /bin/file || ls /usr/bin/file) 2>/dev/null`; \
	   if [ -n "$${FILE}" ]; then $${FILE} ${EXEC} | sed "s#$(EXEC):.##"; fi
	@$(LINE) =

strip:
	$(STRIP) 

codean:
	@(echo; echo "========== $(EXEC) static analysis =========="; echo)
	@codean -s $(EXEC)
	@(echo; echo HTML results are:)
	@(cd ${DIST}; ls $(EXEC).static.html)

parfait: all
	@echo Processing parfait output. This can take a while...
	@parfait --enable=all $(OBJ_DIRS)/*.bc

include $(ROOT_DIR)/make/Makefile.$(MAKEFILE)

$(EXEC): $(DIST_DIR) $(OBJS)
	@$(LINE) -
	$(LINK.c) $(LDFLAGS) -o $@ $(OBJS) -lpthread

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

