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
                xmlns:j2seproject3="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"> 
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:param name="xjcencoding">false</xsl:param>
    <xsl:template match="/">
        
        <xsl:comment><![CDATA[
        *** GENERATED FROM jax-ws.xml - DO NOT EDIT !                             ***
        *** TO MODIFY wsimport options USE Web Service node -> Edit WS Attributes ***
        *** TO CHANGE TARGETS GENERATED TO jaxws-build.xml COPY THOSE             ***
        *** TARGETS TO ../build.xml AND MODIFY THAT FILE INSTEAD                  ***

        ]]></xsl:comment>
        
        <project>
            
            
            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>
            
            <!-- wsimport task initialization -->
            <xsl:if test="/*/*/*/jaxws:wsdl-url">
                <target name="wsimport-init" depends="init">
                    <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws"/>
                    </xsl:if>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{libs.jaxws21.classpath}}"/>
                    </taskdef>
                </target>
            </xsl:if>
            
            <!-- wsimport-client targets - one for each jaxws client -->
                <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                    <xsl:variable name="wsname" select="@name"/>
                    <xsl:variable name="package_name" select="jaxws:package-name"/>
                    <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                    <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                    <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                    <xsl:variable name="wsimportoptions" select="jaxws:wsimport-options"/>
                
                    <target name="wsimport-client-{$wsname}" depends="wsimport-init">
                        <mkdir dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                        <property name="wsdl-{$wsname}" location="xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"/>
                        <wsimport
                            sourcedestdir="${{build.generated.dir}}/jax-wsCache/{$wsname}"
                            destdir="${{build.generated.dir}}/jax-wsCache/{$wsname}"
                            wsdl="${{wsdl-{$wsname}}}"
                            catalog="{$catalog}">
							<xsl:if test="$xjcencoding='true'">
								<xsl:attribute name="encoding">
									<xsl:text>${source.encoding}</xsl:text>
                     			</xsl:attribute>
             				</xsl:if>
                            <xsl:if test="$wsimportoptions">
                                <xsl:for-each select="$wsimportoptions/jaxws:wsimport-option">
                                    <xsl:variable name="wsoptionname" select="jaxws:wsimport-option-name"/>
                                    <xsl:variable name="wsoptionvalue" select="jaxws:wsimport-option-value"/>
                                    <xsl:choose>
                                        <xsl:when test="jaxws:jaxboption">
                                            <xjcarg>
                                                <xsl:variable name="wsoption">
                                                    <xsl:value-of select="$wsoptionname"/>
                                                </xsl:variable>
                                                <xsl:attribute name="{$wsoption}">
                                                    <xsl:value-of select="$wsoptionvalue"/>
                                                </xsl:attribute>
                                            </xjcarg>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:variable name="wsoption">
                                                <xsl:value-of select="$wsoptionname"/>
                                            </xsl:variable>
                                            <xsl:attribute name="{$wsoption}">
                                                <xsl:value-of select="$wsoptionvalue"/>
                                            </xsl:attribute>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:if>
                            <xsl:if test="jaxws:binding">
                                <binding dir="xml-resources/web-service-references/{$wsname}/bindings">
                                    <xsl:attribute name="includes">
                                        <xsl:for-each select="jaxws:binding">
                                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                            <xsl:value-of select="normalize-space(jaxws:file-name)"/>
                                        </xsl:for-each>
                                    </xsl:attribute>
                                </binding>
                            </xsl:if>
                            <xsl:for-each select="jaxws:jvmarg">
                                <jvmarg>
                                    <xsl:attribute name="value">
                                        <xsl:value-of select="."/>
                                    </xsl:attribute>
                                </jvmarg>   
                            </xsl:for-each>
                            <depends file="${{wsdl-{$wsname}}}"/>
                            <produces dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                        </wsimport>
                        <copy todir="${{build.generated.sources.dir}}/jax-ws">
                            <fileset dir="${{build.generated.dir}}/jax-wsCache/{$wsname}">
                                <include name="**/*.java"/>
                            </fileset>
                        </copy>
                        <xsl:if test="jaxws:binding">
                            <copy todir="${{build.classes.dir}}">
                                <fileset dir="${{build.generated.dir}}/jax-wsCache/{$wsname}">
                                    <include name="**/*.xml"/>
                                </fileset>
                            </copy>
                        </xsl:if>
                    </target>
                    <target name="wsimport-client-clean-{$wsname}" depends="-init-project">
                        <delete dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                        <delete dir="${{build.generated.sources.dir}}/jax-ws/{$package_path}"/>
                    </target>
                </xsl:for-each>
            
            <!-- wsimport-client-generate and wsimport-client-compile targets -->
            <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <target name="wsimport-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:text>wsimport-client-</xsl:text><xsl:value-of select="@name"/>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
            </xsl:if>
            
        </project>
        
    </xsl:template>
    
</xsl:stylesheet>
