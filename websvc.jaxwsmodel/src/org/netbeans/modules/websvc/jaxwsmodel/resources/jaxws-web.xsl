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
                xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
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

            <!-- START: Invoke wsgen if web service is not JSR 109 and not from wsdl-->
            <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="count(/jaxws:jax-ws/jaxws:services/jaxws:service[not(jaxws:wsdl-url)]) > 0">
                    <target name="wsgen-init" depends="init, -do-compile">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws/resources/"/>
                        <mkdir dir="${{build.classes.dir}}"/>
                        <mkdir dir="${{build.classes.dir}}/META-INF"/>
                        <property name="j2ee.platform.wsgen.classpath" value="${{libs.jaxws21.classpath}}"/>
                        <taskdef name="wsgen" classname="com.sun.tools.ws.ant.WsGen">
                            <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                        </taskdef>
                    </target>
                </xsl:if>
                <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                    <xsl:if test="not(jaxws:wsdl-url)">
                        <xsl:variable name="wsname" select="@name"/>
                        <xsl:variable name="seiclass" select="jaxws:implementation-class"/>
                        <target name="wsgen-{$wsname}" depends="wsgen-init">
                            <copy todir="${{build.classes.dir}}/META-INF">
                                <fileset dir="${{webinf.dir}}" includes="wsit-{$seiclass}.xml"/>
                            </copy>
                            <wsgen 
                            	sourcedestdir="${{build.generated.sources.dir}}/jax-ws"
                            	resourcedestdir="${{build.generated.sources.dir}}/jax-ws/resources/"
                                destdir="${{build.generated.sources.dir}}/jax-ws"
                                 verbose="true"
                                 keep="true"
                                 genwsdl="true"
                                 sei="{$seiclass}">
                                <xsl:if test="$jaxwsversion='jaxws21lib'">
                                	<xsl:attribute name="xendorsed">
                                		<xsl:text>true</xsl:text>
                                	</xsl:attribute>
                                 </xsl:if>
                                        <classpath path="${{java.home}}/../lib/tools.jar:${{build.classes.dir}}:${{j2ee.platform.wsgen.classpath}}:${{javac.classpath}}"/>
                             </wsgen>
                        </target>
                    </xsl:if>
                </xsl:for-each>
            </xsl:if>
            <!-- END: Invoke wsgen if web service is not JSR 109 -->

            <xsl:comment>
                ===================
                JAX-WS WSIMPORT SECTION
                ===================
            </xsl:comment>
            
            <!-- wsimport task initialization -->
            <xsl:if test="/*/*/*/jaxws:wsdl-url">
                <xsl:variable name="isJSR109">
                    <xsl:value-of select="/jaxws:jax-ws/jaxws:jsr109"/>
                </xsl:variable>
                <target name="wsimport-init" depends="init">
		    <fail if="${{wsimport.bad.version}}" message="WsImport ant task defined in the classpath ${{j2ee.platform.wsimport.classpath}} has a serious bug. See http://wiki.netbeans.org/MetroIssue for details."/>
                    <xsl:if test="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws"/>
                    </xsl:if>
                    <xsl:if test="/jaxws:jax-ws/jaxws:services/jaxws:service/jaxws:wsdl-url">
                        <mkdir dir="${{build.generated.sources.dir}}/jax-ws"/>
                    </xsl:if>
                    <property name="j2ee.platform.wsimport.classpath" value="${{libs.jaxws21.classpath}}"/>
                    <taskdef name="wsimport" classname="com.sun.tools.ws.ant.WsImport">
                        <classpath path="${{java.home}}/../lib/tools.jar:${{j2ee.platform.wsimport.classpath}}:${{javac.classpath}}"/>
                    </taskdef>
                    <condition property="conf-dir" value="${{conf.dir}}/" else="">
                        <isset property="conf.dir"/>
                    </condition>
                </target>
            </xsl:if>
            <!-- END: wsimport task initialization -->

            <!-- wsimport target for client -->
            <xsl:for-each select="/jaxws:jax-ws/jaxws:clients/jaxws:client">
                <xsl:variable name="wsname" select="@name"/>
                <xsl:variable name="package_name" select="jaxws:package-name"/>
                <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                <target name="wsimport-client-{$wsname}" depends="wsimport-init">
                    <mkdir dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                    <xsl:variable name="isService_var" select="false()"/>
                    <xsl:call-template name="invokeWsimport">
                        <xsl:with-param name="isService" select="$isService_var"/>
                        <xsl:with-param name="wsName" select="$wsname" />
                        <xsl:with-param name="wsdlUrl" select="$wsdl_url"/>
                        <xsl:with-param name="Catalog" select="$catalog"/>
                        <xsl:with-param name="wsimportoptions" select="jaxws:wsimport-options"/>
                    </xsl:call-template>
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
                    <delete dir="${{build.generated.sources.dir}}/jax-ws/{$package_path}"/>
                    <delete dir="${{build.generated.dir}}/jax-wsCache/{$wsname}"/>
                </target>
            </xsl:for-each>
            <!-- END: wsimport target for client -->

            <!-- wsimport target for service -->
            <xsl:for-each select="/jaxws:jax-ws/jaxws:services/jaxws:service">
                <xsl:if test="jaxws:wsdl-url">
                    <xsl:variable name="wsname" select="@name"/>
                    <xsl:variable name="package_name" select="jaxws:package-name"/>
                    <xsl:variable name="wsdl_url" select="jaxws:local-wsdl-file"/>
                    <xsl:variable name="package_path" select = "translate($package_name,'.','/')"/>
                    <xsl:variable name="service_name" select = "jaxws:service-name"/>
                    <xsl:variable name="catalog" select = "jaxws:catalog-file"/>
                    <target name="wsimport-service-{$wsname}" depends="wsimport-init">
                        <mkdir dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"/>
                        <xsl:variable name="isService_var" select="true()"/>
                        <xsl:call-template name="invokeWsimport">
                            <xsl:with-param name="isService" select="$isService_var"/>
                            <xsl:with-param name="wsName" select="$wsname" />
                            <xsl:with-param name="wsdlUrl" select="$wsdl_url"/>
                            <xsl:with-param name="Catalog" select="$catalog"/>
                            <xsl:with-param name="wsimportoptions" select="jaxws:wsimport-options"/>
                        </xsl:call-template>
                        <copy todir="${{build.generated.sources.dir}}/jax-ws">
                            <fileset dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}">
                                <include name="**/*.java"/>
                            </fileset>
                        </copy>
                        <copy todir="${{build.web.dir}}/WEB-INF/wsdl/{$wsname}">
                            <fileset dir="${{basedir}}/${{conf-dir}}xml-resources/web-services/{$wsname}/wsdl/" />
                        </copy>
                    </target>
                    <target name="wsimport-service-clean-{$wsname}" depends="-init-project">
                        <delete dir="${{build.generated.sources.dir}}/jax-ws/{$package_path}"/>
                        <delete dir="${{build.generated.dir}}/jax-wsCache/service/{$wsname}"/>
                    </target>
                </xsl:if>
            </xsl:for-each>
            <!-- wsimport target for service -->

            <!-- wsimport-client-generate, wsimport-service-generate targets -->
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
            <!-- END: wsimport-client-generate, wsimport-service-generate targets -->
        </project>
    </xsl:template>

    <!-- invokeWsimport template -->
    <xsl:template name="invokeWsimport">
        <xsl:param name="isService" />
        <xsl:param name="wsName" />
        <xsl:param name="wsdlUrl"/>
        <xsl:param name="Catalog"/>
        <xsl:param name="wsimportoptions"/>
        <wsimport>
            <xsl:variable name="cacheDir">
                <xsl:choose>
                    <xsl:when test="$isService">
                        <xsl:text>service/</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text></xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="wsDir">
                <xsl:choose>
                    <xsl:when test="$isService">
                        <xsl:text>web-services</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>web-service-references</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <!-- setting wsimport attributes -->
            <xsl:attribute name="sourcedestdir">${build.generated.dir}/jax-wsCache/<xsl:value-of select="$cacheDir"/><xsl:value-of select="$wsName"/></xsl:attribute>
            <xsl:attribute name="destdir">${build.generated.dir}/jax-wsCache/<xsl:value-of select="$cacheDir"/><xsl:value-of select="$wsName"/></xsl:attribute>
            <xsl:attribute name="wsdl">${basedir}/${conf-dir}xml-resources/<xsl:value-of select="$wsDir"/>/<xsl:value-of select="$wsName"/>/wsdl/<xsl:value-of select="$wsdlUrl"/></xsl:attribute>
            <xsl:attribute name="catalog"><xsl:value-of select="$Catalog" /></xsl:attribute>
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
                <xsl:variable name="is_xnocompile" select="$wsimportoptions/jaxws:wsimport-option/jaxws:wsimport-option-name='xnocompile'"/>
                <xsl:if test="$is_xnocompile">
                    <depends>
                        <xsl:attribute name="file">${basedir}/${conf-dir}xml-resources/<xsl:value-of select="$wsDir"/>/<xsl:value-of select="$wsName"/>/wsdl/<xsl:value-of select="$wsdlUrl"/></xsl:attribute>
                    </depends>
                    <produces>
                        <xsl:attribute name="dir">${build.generated.dir}/jax-wsCache/<xsl:value-of select="$cacheDir"/><xsl:value-of select="$wsName"/></xsl:attribute>
                    </produces>
                </xsl:if>
            </xsl:if>
            <xsl:if test="jaxws:binding">
                <binding>
                    <xsl:attribute name="dir">${conf-dir}xml-resources/<xsl:value-of select="$wsDir"/>/<xsl:value-of select="$wsName"/>/bindings</xsl:attribute>
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
        </wsimport>
    </xsl:template>
    <!-- END: invokeWsimport template -->

</xsl:stylesheet>
