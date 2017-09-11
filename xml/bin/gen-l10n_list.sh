#!/bin/sh

#
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
# Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
#

#set -x

#
# It is used for generating xml/*/l10n.list files.
#
# Run it from xml/.. directory.
#

# reset old list
L10N_LIST="xml/l10n.list"
echo -n > $L10N_LIST

for module in api catalog core css tax text-edit tools tree-edit xsl schema; do
    MODULE_HOME="xml/${module}"
    MODULE_L10N_LIST="${MODULE_HOME}/l10n.list"
    MODULE_L10N_LIST_TEMP="${MODULE_L10N_LIST}.temp"

    rm -f ${MODULE_L10N_LIST_TEMP}

    ## src
    for sub in / /compat/ /lib/ /lib/compat/; do
        MODULE_SRC=xml/${module}${sub}src
        if [ -e $MODULE_SRC ]; then
            find $MODULE_SRC -name "Bundle.properties" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.html" | grep -v "_ja.html" | grep -v "package.html" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.template" | grep -v "_ja.template" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "mf-layer.xml" | grep -v "mf-layer_ja.xml" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.xml" | grep -v "_ja.xml" | grep "/templates/" >> ${MODULE_L10N_LIST_TEMP}
            find $MODULE_SRC -name "*.xsl" | grep -v "_ja.xsl" | grep "/templates/" >> ${MODULE_L10N_LIST_TEMP}
        fi
    done
    
    ## javahelp
    MODULE_JAVAHELP=$MODULE_HOME/javahelp
    if [ -e $MODULE_JAVAHELP ]; then
        for ext in html gif xml hs jhm; do
            echo "$MODULE_JAVAHELP/org/netbeans/modules/xml/$module/docs/**/*.$ext" >> ${MODULE_L10N_LIST_TEMP}
        done
    fi

    ## sort
    cat ${MODULE_L10N_LIST_TEMP} | sort > ${MODULE_L10N_LIST}
    rm ${MODULE_L10N_LIST_TEMP}

#    find xml/${module} -name "Bundle.properties" | sort > ${MODULE_L10N_LIST}
#    find xml/${module} -name "*.html" | grep -v "_ja.html" | grep -v "package.html" | sort >> ${MODULE_L10N_LIST}

    cat ${MODULE_L10N_LIST} >> $L10N_LIST
done
