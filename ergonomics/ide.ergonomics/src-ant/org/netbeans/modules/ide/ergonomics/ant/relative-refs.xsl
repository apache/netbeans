<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.

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
