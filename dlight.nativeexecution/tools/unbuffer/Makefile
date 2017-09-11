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
CF_Linux-x86_64   = $(CF_COMMON) -DLINUX -shared -fPIC -m64
EX_Linux-x86_64   = unbuffer.so
CF_Linux-sparc_64 = $(CF_COMMON) -DLINUX -shared -fPIC -m64
EX_Linux-sparc_64 = unbuffer.so
CF_MacOSX-x86     = $(CF_COMMON) -DMAXOSX  -shared -m32
EX_MacOSX-x86     = unbuffer.dylib
CF_MacOSX-x86_64  = $(CF_COMMON) -DMAXOSX  -shared -m64
EX_MacOSX-x86_64  = unbuffer.dylib
CF_SunOS-sparc    = $(CF_COMMON) -DSOLARIS -G -fPIC -m32
EX_SunOS-sparc    = unbuffer.so
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

