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
