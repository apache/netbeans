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
    <xsl:output method="xml"/>

    <!-- print out <api /> dependencies on all needed netbeans subprojects -->
    <xsl:template match="/" >
        <p>
            These modules are required in project.xml:
            <xsl:apply-templates select="//dependency" mode="comment" />
            <ul>
                <xsl:apply-templates select="//dependency" />
            </ul>
        </p>
    </xsl:template>

    <xsl:template match="dependency" >
        <li><api>
            <xsl:attribute name="type">import</xsl:attribute>
            <xsl:attribute name="group">java</xsl:attribute>
            <xsl:attribute name="category">
                <xsl:choose>
                    <xsl:when test="api-category" >
                        <xsl:value-of select="api-category/text()"/>
                    </xsl:when>
                    <xsl:otherwise>private</xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="name">
                <xsl:choose>
                    <xsl:when test="api-name" >
                        <xsl:apply-templates select="api-name/text()"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:apply-templates select="code-name-base/text()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <!-- Removed, since there is no guarantee Javadoc for that module is published:
            <xsl:attribute name="url">
                <xsl:text>@</xsl:text>
                <xsl:value-of select="translate(code-name-base/text(),'.','-')"/>
                <xsl:text>@/overview-summary.html</xsl:text>
            </xsl:attribute>
            -->

            <xsl:if test="compile-dependency">
                The module is needed for compilation. 
            </xsl:if>
            <xsl:if test="run-dependency">
                The module is used during runtime. 
                <xsl:if test="run-dependency/specification-version">
                    Specification version 
                    <xsl:value-of select="run-dependency/specification-version/node()" />
                    is required.
                </xsl:if>
            </xsl:if>
        </api></li>
    </xsl:template>
    
    <xsl:template match="dependency" mode="comment" >
        <xsl:comment>
        <xsl:text>&lt;li&gt;&lt;api type='import' group='java' category='</xsl:text>
        <xsl:choose>
            <xsl:when test="api-category" >
                <xsl:value-of select="api-category/text()"/>
            </xsl:when>
            <xsl:otherwise>private</xsl:otherwise>
        </xsl:choose>
        <xsl:text>' name='</xsl:text>
        <xsl:choose>
            <xsl:when test="api-name" >
                <xsl:apply-templates select="api-name/text()"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:apply-templates select="code-name-base/text()"/>
            </xsl:otherwise>
        </xsl:choose>
        <xsl:text>'&gt;</xsl:text>
        <!-- Removed, since there is no guarantee Javadoc for that module is published:
             url='@</xsl:text>
             <xsl:value-of select="translate(code-name-base/text(),'.','-')"/>
             <xsl:text>@/overview-summary.html'
        -->
        <xsl:if test="compile-dependency">
            The module is needed for compilation. 
        </xsl:if>
        <xsl:if test="run-dependency">
            The module is used during runtime. 
            <xsl:if test="run-dependency/specification-version">
                Specification version 
                <xsl:value-of select="run-dependency/specification-version/node()" />
                is required.
            </xsl:if>
        </xsl:if>
        <xsl:text>&lt;/api&gt;
&lt;/li&gt;            
</xsl:text>
        </xsl:comment>
    </xsl:template>
</xsl:stylesheet> 

