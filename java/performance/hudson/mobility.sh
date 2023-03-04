#!/bin/bash -x

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

if test ! -e /space/hudsonserver/master 
then
if [ -n "$mobility_enabled" ]
then

rm -rf "$WORKSPACE"/mobility
#ugly workaround
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

    
ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilitySetupTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityDialogsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=1 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

ant test-qa-functional -Dsuite.dir=test -Dtest.includes=**/MeasureMobilityActionsTest* -Dnetbeans.dest.dir=$netbeans_dest -Dnetbeans.keyring.no.master=true -Drepeat=7 -Dcom.sun.management.jmxremote.port=3333 -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -DBrokenReferencesSupport.suppressBrokenRefAlert=true -Dorg.netbeans.editor.linewrap=true
rm -f "$netbeans_dest"/mobility/config/Modules/org-netbeans-modules-mobility-snippets.xml

cp -R build/test/qa-functional/work/ "$WORKSPACE"/mobility
cp -R build/test/qa-functional/results "$WORKSPACE"/mobility
touch "$WORKSPACE"/mobility/userdir0
touch "$WORKSPACE"/mobility/tmpdir
rm -rf "$WORKSPACE"/mobility/userdir0
rm -rf "$WORKSPACE"/mobility/tmpdir
fi
fi
