# DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
#
# Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
#
# Contributor(s):

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
SOURCES_SunOS-sparc=$(SOURCES_SunOS)
SOURCES_SunOS-sparc_64=$(SOURCES_SunOS)
SOURCES_Linux-x86=$(SOURCES_Linux)
SOURCES_Linux-arm=$(SOURCES_Linux)
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
CF_MacOSX-x86     = $(CF_COMMON) -DMACOSX  -D_DARWIN_C_SOURCE -m32
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_SunOS-x86      = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m32
CF_Windows-x86    = $(CF_COMMON) -DWINDOWS -m32

CF_Linux-x86_64   = $(CF_COMMON) -DLINUX -m64
CF_Linux-sparc_64 = $(CF_COMMON) -DLINUX   -D_GNU_SOURCE -D_XOPEN_SOURCE=700 -m64
CF_MacOSX-x86_64  = $(CF_COMMON) -DMACOSX  -m64
CF_SunOS-sparc_64 = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_SunOS-x86_64   = $(CF_COMMON) -DSOLARIS -D__EXTENSIONS__ -m64
CF_Windows-x86_64 = $(CF_COMMON) -DWINDOWS -m32

LF_Windows-x86    = --static-libgcc
LF_Windows-x86_64 = --static-libgcc
LF_SunOS-x86      = -lrt
LF_SunOS-x86_64   = -lrt
LF_SunOS-sparc    = -lrt
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

