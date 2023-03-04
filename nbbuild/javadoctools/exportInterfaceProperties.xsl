<?xml version="1.0" encoding="UTF-8" ?>
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
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" />
    <xsl:param name="code.name.base" />


    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="//api[@type='export' and @group='java' and @category='official']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='official']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='stable']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='stable']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='devel']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='devel']" />
                </xsl:call-template>
            </xsl:when>
            <xsl:when test="//api[@type='export' and @group='java' and @category='friend']" >
                <xsl:call-template name="print-properties" >
                    <xsl:with-param name="api" select="//api[@type='export' and @group='java' and @category='friend']" />
                </xsl:call-template>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="print-properties" >
        <xsl:param name="api" />
        
        <xsl:text>arch.</xsl:text>
        <xsl:value-of select="translate($code.name.base,'-','.')"/>
        <xsl:text>.name=</xsl:text>
        <xsl:value-of select="$api/@name"/>
        <xsl:text>
</xsl:text>

        <xsl:text>arch.</xsl:text>
        <xsl:value-of select="translate($code.name.base,'-','.')"/>
        <xsl:text>.category=</xsl:text>
        <xsl:value-of select="$api/@category"/>
        <xsl:text>
</xsl:text>
    </xsl:template>
</xsl:stylesheet> 
