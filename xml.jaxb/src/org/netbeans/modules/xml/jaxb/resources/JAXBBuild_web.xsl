<?xml version="1.0" encoding="UTF-8" ?>
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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
<xsl:stylesheet xmlns:s="http://xml.netbeans.org/schema/JAXBWizConfig" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes" xmlns:xalan="http://xml.apache.org/xslt"  xalan:indent-amount="4"/>
    <xsl:template match="/">
        <xsl:comment>
            *** GENERATED FROM xml_binding_cfg.xml - DO NOT EDIT  ***
            *** Configure thru JAXB Wizard.                       ***
        </xsl:comment>
        <xsl:element name="project">
            <xsl:attribute name="name"><xsl:value-of select="s:schemas/@projectName"/>_jaxb</xsl:attribute>
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">.</xsl:attribute>
            <xsl:element name="target">
                <xsl:attribute name="name">xjc-typedef-target</xsl:attribute>
                <xsl:attribute name="depends">-init-project</xsl:attribute>
                <typedef classname="com.sun.tools.xjc.XJCTask" name="xjc">
                    <classpath path="${{jaxbwiz.xjcdef.classpath}}"/>
                </typedef>
            </xsl:element>
            <xsl:element name="target">
                <xsl:attribute name="name">jaxb-clean-code-generation</xsl:attribute>
                <xsl:attribute name="depends">clean,jaxb-code-generation</xsl:attribute>
            </xsl:element>
            <xsl:element name="target">
                <xsl:attribute name="name">jaxb-code-generation</xsl:attribute>
                <xsl:attribute name="depends">xjc-typedef-target,-do-init,-init-macrodef-javac</xsl:attribute>
                <mkdir dir="${{build.generated.sources.dir}}/jaxb"/>
                <mkdir dir="build/generated/jaxbCache"/>
                <xsl:apply-templates select="s:schemas/s:schema"/>
                <xsl:comment>*** Web project javac macro does not support sourcepath attribute, so do not pass "sourcepath=${src.dir}"</xsl:comment>
            </xsl:element>
        </xsl:element>
    </xsl:template>
    <xsl:template match="s:schema">
        <xsl:element name="mkdir">
            <xsl:attribute name="dir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>
        </xsl:element>

        <xsl:element name="xjc">
            <xsl:if test="string-length(@package) &gt; 0">
                <xsl:attribute name="package"><xsl:value-of select="./@package"/></xsl:attribute>
            </xsl:if>
            <xsl:attribute name="destdir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>

            <xsl:choose>
                <xsl:when test="count(s:catalog) &gt; 0">
                    <xsl:for-each select="s:catalog">
                        <xsl:choose>
                            <xsl:when test="string-length(./@location) &gt; 0">
                                <xsl:apply-templates select="."/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:attribute name="catalog"><xsl:text>catalog.xml</xsl:text></xsl:attribute>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="catalog"><xsl:text>catalog.xml</xsl:text></xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:element name="classpath">
                <xsl:element name="pathelement">
                    <xsl:attribute name="location">${src.dir}</xsl:attribute>
                </xsl:element>
                <xsl:element name="pathelement">
                    <xsl:attribute name="path">${jaxbwiz.xjcrun.classpath}</xsl:attribute>
                </xsl:element>
            </xsl:element>
            <xsl:element name="arg">
                <xsl:attribute name="value"><xsl:value-of select="./@type"/></xsl:attribute>
            </xsl:element>
            <xsl:for-each select="s:xjc-options/s:xjc-option">
                <xsl:if test="./@value='true'">
                    <xsl:apply-templates select="."/>
                </xsl:if>
            </xsl:for-each>

            <xsl:apply-templates select="s:schema-sources/s:schema-source"/>

            <xsl:apply-templates select="s:bindings"/>

            <xsl:element name="depends">
                <xsl:attribute name="file"><xsl:value-of select="s:schema-sources/s:schema-source/@location"/></xsl:attribute>
            </xsl:element>

            <xsl:element name="produces">
                <xsl:attribute name="dir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>
            </xsl:element>
        </xsl:element>

        <xsl:element name="copy">
            <xsl:attribute name="todir">${build.generated.sources.dir}/jaxb</xsl:attribute>
            <xsl:element name="fileset">
                <xsl:attribute name="dir">build/generated/jaxbCache/<xsl:value-of select="./@name"/></xsl:attribute>
            </xsl:element>
        </xsl:element>

    </xsl:template>
    <xsl:template match="s:schema-source">
        <xsl:element name="schema">
            <xsl:attribute name="file"><xsl:value-of select="./@location"/></xsl:attribute>
        </xsl:element>
    </xsl:template>

    <xsl:template match="s:bindings">
        <xsl:if test="count(s:binding) &gt; 0">
            <xsl:for-each select="s:binding">
                <xsl:element name="binding">
                    <xsl:attribute name="file"><xsl:value-of select="./@location"/></xsl:attribute>
                </xsl:element>
            </xsl:for-each>
        </xsl:if>
    </xsl:template>

    <xsl:template match="s:xjc-option">
        <xsl:element name="arg">
            <xsl:attribute name="value"><xsl:value-of select="./@name"/></xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="s:catalog">
            <xsl:attribute name="catalog"><xsl:value-of select="./@location"/></xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
