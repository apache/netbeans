#!/bin/sh
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
CPUFAMILY=\${CPUFAMILY:-\`echo \${CPUTYPE} | egrep "arm" > /dev/null && echo arm\`}
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

