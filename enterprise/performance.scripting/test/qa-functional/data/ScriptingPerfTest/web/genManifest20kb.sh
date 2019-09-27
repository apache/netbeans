#!/bin/bash

file="manifest20kb.mf"

echo "Manifest-Version: 1.0" > $file
# write while the file size is less than 20kb
x=0
while [ `ls -s $file|cut -d' ' -f1` -lt 41 ]
do
    x=$((x+1))
    cat >> $file << EOF
#Copy - $x
OpenIDE-Module-$x: org.netbeans.modules.web.core.syntax/1
OpenIDE-Module-Implementation-Version-$x: 1
OpenIDE-Module-Localizing-Bundle-$x: org/netbeans/modules/web/core/syntax/Bundle.properties
OpenIDE-Module-Install-$x: org/netbeans/modules/web/core/syntax/settings/RestoreSettings.class
OpenIDE-Module-Layer-$x: org/netbeans/modules/web/core/syntax/resources/layer.xml

EOF

done

