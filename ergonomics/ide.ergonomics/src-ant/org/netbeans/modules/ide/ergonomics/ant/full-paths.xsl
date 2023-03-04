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
    <xsl:output method="xml" omit-xml-declaration="no"/>

    <xsl:template match="filesystem">
        <xsl:element name="filesystem">
            <xsl:call-template name="with-path">
                <xsl:with-param name="path" select="''"/>
            </xsl:call-template>
        </xsl:element>
    </xsl:template>

    <xsl:template name="with-path">
        <xsl:param name="path"/>

        <xsl:for-each select="folder|file|attr">
            <xsl:variable name="mypath"><xsl:value-of select="$path"/>/<xsl:value-of select="@name"/></xsl:variable>
            <xsl:element name="{name()}">
                <xsl:attribute name="path"><xsl:value-of select="$mypath"/></xsl:attribute>
                <xsl:apply-templates select="@*|node()"/>
                <xsl:call-template name="with-path">
                    <xsl:with-param name="path" select="$mypath"/>
                </xsl:call-template>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>

    <xsl:template match="folder|file|attr"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
