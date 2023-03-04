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
                xmlns:ejbjarproject2="http://www.netbeans.org/ns/j2ee-ejbjarproject/2"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"> 
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:param name="jaxwsversion">jaxws21lib</xsl:param>
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
                JAX-WS WSGEN SECTION
                ===================
            </xsl:comment>
            
            <!-- WS from java - support for WSDL generation -->
            <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="count(/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]) > 0">
                    <target name="wsgen-init" depends="init">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws/resources/"/>
                        <property name="j2ee.platform.wsgen.classpath" value="${{libs.jaxws21.classpath}}"/>
                        <taskdef name="wsgen" classname="com.sun.tools.ws.ant.WsGen">
                            <classpath path="${{j2ee.platform.wsgen.classpath}}"/>
                        </taskdef>
                    </target>
                    <target name="wsgen-compile">
                        <xsl:attribute name="depends">
                            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]">
                                <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                                <xsl:variable name="wsname2">
                                    <xsl:value-of select="@name"/>
                                </xsl:variable>
                                <xsl:text>wsgen-</xsl:text><xsl:value-of select="@name"/>
                            </xsl:for-each>
                        </xsl:attribute>
                        <ejbjarproject2:javac srcdir="${{build.generated.sources.dir}}/jax-ws" classpath="${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}" destdir="${{classes.dir}}"/>
                    </target>
                </xsl:if>
            </xsl:if>
            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="not(jaxws:wsdl-url)">
                    <xsl:variable name="wsname" select="@name"/>
                    
                    <xsl:variable name="seiclass" select="jaxws:implementation-class"/>
                    <target name="wsgen-{$wsname}" depends="wsgen-init, -do-compile">
                        <xsl:choose>
                            <xsl:when test="$jaxwsversion='jaxws21lib'">
                                <wsgen
                                    destdir="${{build.generated.sources.dir}}/jax-ws"
                                    sourcedestdir="${{build.generated.sources.dir}}/jax-ws"
                                    resourcedestdir="${{build.generated.sources.dir}}/jax-ws/resources/"
                                    verbose="true"
                                    xendorsed="true"
                                    keep="false"
                                    genwsdl="true"
                                    sei="{$seiclass}">
                                    <classpath path="${{java.home}}/../lib/tools.jar:${{classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                </wsgen>
                            </xsl:when>
                            <xsl:otherwise>
                                <wsgen
                                    destdir="${{build.generated.sources.dir}}/jax-ws"
                                    sourcedestdir="${{build.generated.sources.dir}}/jax-ws"
                                    resourcedestdir="${{build.generated.sources.dir}}/jax-ws/resources/"
                                    verbose="true"
                                    keep="false"
                                    genwsdl="true"
                                    sei="{$seiclass}">
                                    <classpath path="${{java.home}}/../lib/tools.jar:${{classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                                </wsgen>
                            </xsl:otherwise>
                        </xsl:choose>
                    </target>
                </xsl:if>
            </xsl:for-each>
            <!-- END WS from Java -->            
            
            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>
            
            <!-- wsimport task initialization -->
            <xsl:if test="/*/*/*/jaxws:wsdl-url">
                <target name="wsimport-init" depends="init">
   		    <fail if="${{wsimport.bad.version}}" message="WsImport ant task defined in the classpath ${{j2ee.platform.wsimport.classpath}} has a serious bug. See http://wiki.netbeans.org/MetroIssue for details."/>
                    <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws"/>
                    </xsl:if>
                    <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service/jaxws:wsdl-url">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws"/>
                    </xsl:if>
                    <mkdir dir="${{classes.dir}}"/>
                    <property name="j2ee.platform.wsimport.classpath" value="${{libs.jaxws21.classpath}}"/>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{j2ee.platform.wsimport.classpath}}"/>
                    </taskdef>
                </target>
            </xsl:if>
            <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <xsl:variable name="wsname" select="@name"/>
                <xsl:variable name="package_name" select="jaxws:package-name"/>
                <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                <xsl:variable name="wsimportoptions" select="jaxws:wsimport-options"/>
                <xsl:variable name="is_xnocompile" select="$wsimportoptions/jaxws:wsimport-option/jaxws:wsimport-option-name='xnocompile'"/>
                <target name="wsimport-client-{$wsname}" depends="wsimport-init">
                    <mkdir dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                    <property name="wsdl-{$wsname}" location="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/wsdl/{$wsdl_url}"/>
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
                            <binding dir="${{meta.inf}}/xml-resources/web-service-references/{$wsname}/bindings">
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
                        <xsl:if test="$is_xnocompile">
                            <depends file="${{wsdl-{$wsname}}}"/>
                            <produces dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                        </xsl:if>
                    </wsimport>
                    <copy todir="${{build.generated.sources.dir}}/jax-ws">
                        <fileset dir="${{build.generated.dir}}/jax-wsCache/{$wsname}">
                            <include name="**/*.java"/>
                        </fileset>
                    </copy>
                    <xsl:if test="jaxws:binding">
                        <copy todir="${{classes.dir}}">
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
            
            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="jaxws:wsdl-url">
                    <xsl:variable name="wsname" select="@name"/>
                    <xsl:variable name="package_name" select="jaxws:package-name"/>
                    <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                    <xsl:variable name="service_name" select="jaxws:service-name"/>
                    <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                    <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                    <xsl:variable name="wsimportoptions" select="jaxws:wsimport-options"/>
                    <xsl:variable name="is_xnocompile" select="$wsimportoptions/jaxws:wsimport-option/jaxws:wsimport-option-name='xnocompile'"/>
                    <target name="wsimport-service-{$wsname}" depends="wsimport-init">
                        <mkdir dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"/>
                        <property name="service-wsdl-{$wsname}" location="${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/{$wsdl_url}"/>
                        <wsimport
                            sourcedestdir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"
                            destdir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"
                            wsdl="${{service-wsdl-{$wsname}}}"
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
                                <binding dir="${{meta.inf}}/xml-resources/web-services/{$wsname}/bindings">
                                    <xsl:attribute name="includes">
                                        <xsl:for-each select="jaxws:binding">
                                            <xsl:if test="position()!=1"><xsl:text>;</xsl:text></xsl:if>
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
                            <xsl:if test="$is_xnocompile">
                                <depends file="${{wsdl-{$wsname}}}"/>
                                <produces dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"/>
                            </xsl:if>
                        </wsimport>
                        <copy todir="${{build.generated.sources.dir}}/jax-ws">
                            <fileset dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}">
                                <include name="**/*.java"/>
                            </fileset>
                        </copy>
                        <property name="targetLocation-{$wsname}" location="${{meta.inf}}/wsdl/{$wsname}"/>
                        <property name="sourceLocation-{$wsname}" location="${{meta.inf}}/xml-resources/web-services/{$wsname}/wsdl/"/>
                        <copy todir="${{targetLocation-{$wsname}}}">
                            <fileset dir="${{sourceLocation-{$wsname}}}" />
                        </copy> 
                    </target>
                    <target name="wsimport-service-clean-{$wsname}" depends="-init-project">
                        <delete dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"/>
                        <delete dir="${{build.generated.sources.dir}}/jax-ws/{$package_path}"/>
                    </target>
                </xsl:if>
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
            
            <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service/jaxws:wsdl-url">
                <target name="wsimport-service-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:if test="jaxws:wsdl-url">
                                <xsl:text>wsimport-service-</xsl:text><xsl:value-of select="@name"/>
                            </xsl:if>
                            <xsl:if test="not(jaxws:wsdl-url)">
                                <xsl:text>wsimport-init</xsl:text>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
            </xsl:if>
            
        </project>
        
    </xsl:template>
    
</xsl:stylesheet>
