<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:nbmproject2="http://www.netbeans.org/ns/nb-module-project/2"
                xmlns:nbmproject3="http://www.netbeans.org/ns/nb-module-project/3"
                exclude-result-prefixes="xalan p nbmproject2 nbmproject3">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***
]]></xsl:comment>
        <xsl:variable name="codenamebase" select="/p:project/p:configuration/nbmproject2:data/nbmproject2:code-name-base |
                                                  /p:project/p:configuration/nbmproject3:data/nbmproject3:code-name-base"/>
        <project name="{$codenamebase}-impl">
            <xsl:attribute name="basedir">..</xsl:attribute>
            <fail message="Please build using Ant 1.7.1 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.7.1"/>
                    </not>
                </condition>
            </fail>
            <xsl:choose>
                <xsl:when test="/p:project/p:configuration/nbmproject2:data/nbmproject2:suite-component |
                                /p:project/p:configuration/nbmproject3:data/nbmproject3:suite-component">
                    <property file="nbproject/private/suite-private.properties"/>
                    <property file="nbproject/suite.properties"/>
                    <fail unless="suite.dir">You must set 'suite.dir' to point to your containing module suite</fail>
                    <property file="${{suite.dir}}/nbproject/private/platform-private.properties"/>
                    <property file="${{suite.dir}}/nbproject/platform.properties"/>
                </xsl:when>
                <xsl:when test="/p:project/p:configuration/nbmproject2:data/nbmproject2:standalone |
                                /p:project/p:configuration/nbmproject3:data/nbmproject3:standalone">
                    <property file="nbproject/private/platform-private.properties"/>
                    <property file="nbproject/platform.properties"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:message terminate="yes">
                        Cannot generate build-impl.xml for a netbeans.org module!
                    </xsl:message>
                </xsl:otherwise>
            </xsl:choose>
            <macrodef name="property" uri="http://www.netbeans.org/ns/nb-module-project/2">
                <attribute name="name"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{name}}" value="${{@{{value}}}}"/>
                </sequential>
            </macrodef>
            <macrodef name="evalprops" uri="http://www.netbeans.org/ns/nb-module-project/2">
                <attribute name="property"/>
                <attribute name="value"/>
                <sequential>
                    <property name="@{{property}}" value="@{{value}}"/>
                </sequential>
            </macrodef>
            <property file="${{user.properties.file}}"/>
            <nbmproject2:property name="harness.dir" value="nbplatform.${{nbplatform.active}}.harness.dir"/>
            <nbmproject2:property name="nbplatform.active.dir" value="nbplatform.${{nbplatform.active}}.netbeans.dest.dir"/>
            <nbmproject2:evalprops property="cluster.path.evaluated" value="${{cluster.path}}"/>
            <fail message="Path to 'platform' cluster missing in $${{cluster.path}} property or using corrupt Netbeans Platform (missing harness).">
                <condition>
                    <not>
                        <contains string="${{cluster.path.evaluated}}" substring="platform"/>
                    </not>
                </condition>
            </fail>
            <import file="${{harness.dir}}/build.xml"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
