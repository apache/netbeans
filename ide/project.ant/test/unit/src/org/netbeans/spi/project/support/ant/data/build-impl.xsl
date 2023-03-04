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
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:test="urn:test:shared"
                exclude-result-prefixes="xalan p test">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
    
<xsl:variable name="name" select="/p:project/p:configuration/test:data/test:name"/>
<xsl:variable name="displayname" select="/p:project/p:configuration/test:data/test:display-name"/>
<project name="{$name}.impl" default="all" basedir="..">

    <description><xsl:value-of select="$displayname"/></description>

    <target name="all"/>
    
    <xsl:if test="/p:project/p:configuration/test:data/test:shared-stuff">
        <target name="x" description="shared-stuff was defined"/>
    </xsl:if>

</project>

    </xsl:template>
    
</xsl:stylesheet>
