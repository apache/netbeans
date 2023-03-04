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

<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format">

<!-- template rule matching source root element -->
<xsl:template match="/">
    <html>
        <title><xsl:value-of select="document/@title"/></title>
        <head>
        </head>
        <body>
            <h1><xsl:value-of select="document/@title"/></h1>
            <p>Author:
                <a><xsl:attribute name="href">mailto:<xsl:value-of select="document/@email"/></xsl:attribute>
                    <xsl:value-of select="document/@author"/>
                </a>
            </p>
            <xsl:apply-templates select="//document/paragraph"/> 
            <hr/>
            <p><xsl:value-of select="document/footnote"/></p>
        </body> 
    </html>
 </xsl:template>

<!-- template rule matching paragraph element -->
<xsl:template match="paragraph">
    <h3><xsl:value-of select="@title"/></h3>
    <p><xsl:value-of select="."/></p>
</xsl:template>
 
 
</xsl:stylesheet> 
