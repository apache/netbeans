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
            <property file="${{user.properties.file}}"/>
            <nbmproject2:property name="harness.dir" value="nbplatform.${{nbplatform.active}}.harness.dir"/>
            <nbmproject2:property name="netbeans.dest.dir" value="nbplatform.${{nbplatform.active}}.netbeans.dest.dir"/>
            <fail message="You must define 'nbplatform.${{nbplatform.active}}.harness.dir'">
                <condition>
                    <not>
                        <available file="${{harness.dir}}" type="dir"/>
                    </not>
                </condition>
            </fail>
            <import file="${{harness.dir}}/build.xml"/>
        </project>
    </xsl:template>
</xsl:stylesheet>
