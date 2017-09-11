#!/bin/sh
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

PROG=`basename "$0"`
USAGE="usage: ${PROG} [-h host] [-b bitness] [options to gmake]"

BITNESS_PARAM=no

while getopts h:b: opt; do
  case $opt in
    h) RHOST=$OPTARG
       ;;
    b) BITNESS_PARAM=$OPTARG
       ;;
  esac
done

shift `expr $OPTIND - 1`

if [ "$RHOST" = "" ]; then
   PREFIX=""
else 
   PREFIX="ssh $RHOST"
fi

LHOST=`uname -n`
PWD=`pwd`

$PREFIX sh -s << EOF
OS=\`uname -s\`
OSFAMILY=
CPUTYPE=\`uname -p\`
BITNESS=32

if [ "\${CPUTYPE}" = "unknown" ]; then
   CPUTYPE=\`uname -m\`
fi

if [ "\${OS}" = "SunOS" ]; then
   BITNESS=\`isainfo -b\`
   OSFAMILY="SunOS"
else
   uname -a | egrep "x86_64|WOW64" >/dev/null
   if [ \$? -eq 0 ]; then
      BITNESS=64
   fi
fi

OSFAMILY=\${OSFAMILY:-\`echo \${OS} | grep _NT- >/dev/null && echo Windows\`} # CYGWIN_NT-10.0
OSFAMILY=\${OSFAMILY:-\`test "\$OS" = "Darwin" && echo MacOSX\`}
OSFAMILY=\${OSFAMILY:-\`test "\$OS" = "Linux" && echo Linux\`}
OSFAMILY=\${OSFAMILY:-\${OS}}

CPUFAMILY=\`echo \${CPUTYPE} | egrep "^i|x86_64|athlon|Intel" >/dev/null && echo x86\`
CPUFAMILY=\${CPUFAMILY:-\`echo \${CPUTYPE} | egrep "armv" > /dev/null && echo arm\`}
CPUFAMILY=\${CPUFAMILY:-\`echo \${CPUTYPE} | egrep "sparc" > /dev/null && echo sparc\`} # sparc or sparc64
CPUFAMILY=\${CPUFAMILY:-\${CPUTYPE}}

if [ "$BITNESS_PARAM" != "no" ]; then
   BITNESS="$BITNESS_PARAM"
fi

PLATFORM=\${OSFAMILY}-\${CPUFAMILY}

if [ "\${BITNESS}" = "64" ]; then
   PLATFORM=\${PLATFORM}_64
fi

echo Platform: \${PLATFORM}
uname -a

MAKE=\`which gmake || which make\`
cd /net/$LHOST/$PWD 2> /dev/null
\$MAKE CONF=\${PLATFORM} OSFAMILY=\${OSFAMILY} $@

EOF

