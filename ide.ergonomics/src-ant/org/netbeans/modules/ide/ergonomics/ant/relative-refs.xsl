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
Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="xml" indent="yes"/>
    <xsl:param name="cluster.name"/>
    <xsl:key name="unique" match="folder|file|attr" use="@path"/>

<!-- iterates through hierarchy taking only those with unique path -->

    <xsl:template match="/filesystem">
        <xsl:element name="filesystem">
            <xsl:apply-templates/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="folder|file|attr">
        <xsl:variable name="myid" select="generate-id()"/>
        <xsl:variable name="pathid" select="generate-id(key('unique', @path))"/>

        <xsl:if test="$myid = $pathid">
            <xsl:element name="{name()}">
                <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                <xsl:variable name="orig" select="."/>
                <xsl:for-each select="/descendant::folder[@path=$orig/@path]">
                    <xsl:apply-templates mode="replace-refs"/>
                    <xsl:apply-templates select="folder"/>
                </xsl:for-each>
            </xsl:element>
        </xsl:if>
    </xsl:template>

<!-- apply the mappings -->

    <!-- ignore is iterated already -->
    <xsl:template match="folder" mode="replace-refs"/>
    <xsl:template match="@path" mode="replace-refs"/>
    <xsl:template match="file" mode="replace-refs">
        <xsl:element name="file">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:if test="@url">
                <xsl:attribute name="url">
                    <xsl:call-template name="filename">
                        <xsl:with-param name="text" select="@url"/>
                    </xsl:call-template>
                </xsl:attribute>
            </xsl:if>
            <xsl:apply-templates mode="replace-refs"/>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@name='SystemFileSystem.localizingBundle']" mode="replace-refs">
        <xsl:element name="attr">
            <xsl:attribute name="name">SystemFileSystem.localizingBundle</xsl:attribute>
            <xsl:attribute name="stringvalue">org.netbeans.modules.ide.ergonomics.<xsl:value-of select="$cluster.name"/>.Bundle</xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@bundlevalue]" mode="replace-refs">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="bundlevalue">org.netbeans.modules.ide.ergonomics.<xsl:value-of select="$cluster.name"/>.Bundle#<xsl:value-of select="substring-after(@bundlevalue, '#')"/></xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="attr[@urlvalue]" mode="replace-refs">
        <xsl:choose>
            <xsl:when test="contains(@urlvalue,'javax/swing/beaninfo/')">
                <xsl:element name="attr">
                    <xsl:apply-templates select="@*" mode="replace-refs"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="attr">
                    <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
                    <xsl:attribute name="urlvalue">
                        <xsl:text>ergoloc:/org/netbeans/modules/ide/ergonomics/</xsl:text>
                        <xsl:value-of select="$cluster.name"/>
                        <xsl:text>/</xsl:text>
                        <xsl:call-template name="filename">
                            <xsl:with-param name="text" select="@urlvalue"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="attr[@name = 'iconBase' or @name='iconResource']" mode="replace-refs">
        <xsl:element name="attr">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:attribute name="stringvalue">
                <xsl:text>org/netbeans/modules/ide/ergonomics/</xsl:text>
                <xsl:value-of select="$cluster.name"/>
                <xsl:text>/</xsl:text>
                <xsl:call-template name="filename">
                    <xsl:with-param name="text" select="@stringvalue"/>
                </xsl:call-template>
            </xsl:attribute>
        </xsl:element>
    </xsl:template>
    <xsl:template match="@*|node()" mode="replace-refs">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" mode="replace-refs"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template name="filename">
        <xsl:param name="text"/>
        <xsl:variable name="after">
            <xsl:choose>
                <xsl:when test="contains($text,':/')">
                    <xsl:value-of select="substring-after($text,':/')"/>
                </xsl:when>
                <xsl:when test="contains($text,'nbresloc:')">
                    <xsl:value-of select="substring-after($text,'nbresloc:')"/>
                </xsl:when>
                <xsl:when test="contains($text,'/')">
                    <xsl:value-of select="$text"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="noslash">
            <xsl:choose>
                <xsl:when test="starts-with($after,'/')">
                    <xsl:value-of select="substring-after($text,'/')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$after"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <xsl:choose>
            <xsl:when test="$noslash">
                <xsl:value-of select="translate($noslash,'/','-')"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="translate($text,'/','-')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
</xsl:stylesheet>
