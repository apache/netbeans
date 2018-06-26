<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.

Oracle and Java are registered trademarks of Oracle and/or its affiliates.
Other names may be trademarks of their respective owners.


The contents of this file are subject to the terms of either the GNU
General Public License Version 2 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://www.netbeans.org/cddl-gplv2.html
or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License file at
nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
particular file as subject to the "Classpath" exception as provided
by Oracle in the GPL Version 2 section of the License file that
accompanied this code. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

Contributor(s):

The Original Software is NetBeans. The Initial Developer of the Original
Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
Microsystems, Inc. All Rights Reserved.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
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
