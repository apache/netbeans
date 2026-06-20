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

BASE=`dirname $0`
cd $BASE
BASE=`pwd`

git clone https://github.com/eclipse-equinox/equinox.framework
cd equinox.framework
git checkout M20140115-0800
git show M20140115-0800:bundles/org.eclipse.osgi/core/framework/org/eclipse/osgi/framework/internal/core/Framework.java  >Framework.java
git show M20140115-0800:bundles/org.eclipse.osgi/resolver/src/org/eclipse/osgi/internal/resolver/StateBuilder.java  >StateBuilder.java
patch <../M20140115-0800.patch
if ! [ -e org.eclipse.osgi-3.9.1.v20140110-1610.jar ]; then
  wget https://repo.eclipse.org/content/repositories/releases/org/eclipse/core/org.eclipse.osgi/3.9.1.v20140110-1610/org.eclipse.osgi-3.9.1.v20140110-1610.jar
fi
mkdir -p out
javac --release 17 -cp org.eclipse.osgi-3.9.1.v20140110-1610.jar Framework.java StateBuilder.java -d out || exit 1
cd out
zip -d ../org.eclipse.osgi-3.9.1.v20140110-1610.jar META-INF/ECLIPSE_.SF META-INF/ECLIPSE_.RSA
zip -r ../org.eclipse.osgi-3.9.1.v20140110-1610.jar .

cd "$BASE"
mv equinox.framework/org.eclipse.osgi-3.9.1.v20140110-1610.jar org.eclipse.osgi_3.9.1.nb10.jar