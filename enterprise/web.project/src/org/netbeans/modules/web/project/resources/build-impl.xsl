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
<!--
XXX should not have changed /1 to /2 for URI of *all* macrodefs; only the ones
that actually changed semantically as a result of supporting multiple compilation
units. E.g. <webproject1:property/> did not change at all, whereas
<webproject1:javac/> did. Need to only update URIs where necessary; otherwise we
cause gratuitous incompatibilities for people overriding macrodef targets. Also
we will need to have an upgrade guide that enumerates all build script incompatibilities
introduced by support for multiple source roots. -jglick
-->
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:webproject1="http://www.netbeans.org/ns/web-project/1"
                xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
                xmlns:webproject3="http://www.netbeans.org/ns/web-project/3"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                exclude-result-prefixes="xalan p projdeps projdeps2 libs">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    
    <xsl:template match="/">
        
        <xsl:comment><![CDATA[
        *** GENERATED FROM project.xml - DO NOT EDIT  ***
        ***         EDIT ../build.xml INSTEAD         ***

        For the purpose of easier reading the script
        is divided into following sections:
        - initialization
        - compilation
        - dist
        - execution
        - debugging
        - javadoc
        - test compilation
        - test execution
        - test debugging
        - cleanup

        ]]></xsl:comment>
        
        <xsl:variable name="name" select="/p:project/p:configuration/webproject3:data/webproject3:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            <import file="ant-deploy.xml" />

            <fail message="Please build using Ant 1.7.1 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.7.1"/>
                    </not>
                </condition>
            </fail>

            <target name="default">
                <xsl:attribute name="depends">dist,javadoc</xsl:attribute>
                <xsl:attribute name="description">Build whole project.</xsl:attribute>
            </target>
            
            <xsl:comment>
                INITIALIZATION SECTION
            </xsl:comment>
            
            <target name="-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-init-private">
                <xsl:attribute name="depends">-pre-init</xsl:attribute>
                <property file="nbproject/private/private.properties"/>
            </target>
            
            <xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">
                <target name="-pre-init-libraries">
                    <property name="libraries.path">
                        <xsl:attribute name="location"><xsl:value-of select="/p:project/p:configuration/libs:libraries/libs:definitions"/></xsl:attribute>
                    </property>
                    <dirname property="libraries.dir.nativedirsep" file="${{libraries.path}}"/>
                    <!-- Do not want \ on Windows, since it would act as an escape char: -->
                    <pathconvert property="libraries.dir" dirsep="/">
                        <path path="${{libraries.dir.nativedirsep}}"/>
                    </pathconvert>
                    <basename property="libraries.basename" file="${{libraries.path}}" suffix=".properties"/>
                    <available property="private.properties.available" file="${{libraries.dir}}/${{libraries.basename}}-private.properties"/>
                </target>
                <target name="-init-private-libraries" depends="-pre-init-libraries" if="private.properties.available">
                    <loadproperties srcfile="${{libraries.dir}}/${{libraries.basename}}-private.properties" encoding="ISO-8859-1">
                        <filterchain>
                            <replacestring from="$${{base}}" to="${{libraries.dir}}"/>
                            <escapeunicode/>
                        </filterchain>
                    </loadproperties>
                </target>
                <target name="-init-libraries" depends="-pre-init,-init-private,-init-private-libraries">
                    <loadproperties srcfile="${{libraries.path}}" encoding="ISO-8859-1">
                        <filterchain>
                            <replacestring from="$${{base}}" to="${{libraries.dir}}"/>
                            <escapeunicode/>
                        </filterchain>
                    </loadproperties>
                </target>
            </xsl:if>
            
            <target name="-init-user">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if></xsl:attribute>
                <property file="${{user.properties.file}}"/>
                <xsl:comment> The two properties below are usually overridden </xsl:comment>
                <xsl:comment> by the active platform. Just a fallback. </xsl:comment>
                <property name="default.javac.source" value="1.4"/>
                <property name="default.javac.target" value="1.4"/>
            </target>
            
            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user</xsl:attribute>
                <property file="nbproject/project.properties"/>
            </target>
            
            <target name="-do-ear-init">
                <xsl:attribute name="depends">-pre-init,-init-private,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
            </target>
            
            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-init-macrodef-property</xsl:attribute>
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                    <webproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                    <webproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                    <webproject1:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                    <webproject1:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                    <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                        <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                    </condition>
                    <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                    <webproject1:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                    <condition property="platform.java" value="${{platform.home}}/bin/java">
                        <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                    </condition>
                    <property name="platform.java" value="${{platform.java.tmp}}"/>
                    <webproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
                    <condition property="platform.javadoc" value="${{platform.home}}/bin/javadoc">
                        <equals arg1="${{platform.javadoc.tmp}}" arg2="$${{platforms.${{platform.active}}.javadoc}}"/>
                    </condition>
                    <property name="platform.javadoc" value="${{platform.javadoc.tmp}}"/>
                    <fail unless="platform.home">Must set platform.home</fail>
                    <fail unless="platform.bootcp">Must set platform.bootcp</fail>
                    <fail unless="platform.java">Must set platform.java</fail>
                    <fail unless="platform.javac">Must set platform.javac</fail>
  <fail if="platform.invalid">
 The J2SE Platform is not correctly set up.
 Your active platform is: ${platform.active}, but the corresponding property "platforms.${platform.active}.home" is not found in the project's properties files. 
 Either open the project in the IDE and setup the Platform with the same name or add it manually.
 For example like this:
     ant -Duser.properties.file=&lt;path_to_property_file&gt; jar (where you put the property "platforms.${platform.active}.home" in a .properties file)
  or ant -Dplatforms.${platform.active}.home=&lt;path_to_JDK_home&gt; jar (where no properties file is used) 
  </fail>
                </xsl:if>
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:use-manifest">
                    <fail unless="manifest.file">Must set manifest.file</fail>
                </xsl:if>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <condition property="no.javadoc.preview">
                    <isfalse value="${{javadoc.preview}}"/>
                </condition>
                <property name="javac.compilerargs" value=""/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <condition property="no.dist.ear.dir">
                    <not>
                        <isset property="dist.ear.dir"/>
                    </not>
                </condition>
                <property name="build.web.excludes" value="${{build.classes.excludes}}"/>
                <condition property="do.compile.jsps">
                    <istrue value="${{compile.jsps}}"/>
                </condition>
                <condition property="do.debug.server">
                    <or>
                        <not><isset property="debug.server"/></not>
                        <istrue value="${{debug.server}}"/>
                        <and>
                            <not><istrue value="${{debug.server}}"/></not>
                            <not><istrue value="${{debug.client}}"/></not>
                        </and>
                    </or>
                </condition>
                <condition property="do.debug.client">
                    <istrue value="${{debug.client}}"/>
                </condition>
                <condition property="do.display.browser">
                    <istrue value="${{display.browser}}"/>
                </condition>
                <condition property="do.display.browser.debug.old">
                    <and>
                        <isset property="do.display.browser"/>
                        <not><isset property="do.debug.client"/></not>
                        <not><isset property="browser.context"/></not>
                    </and>
                </condition>
                <condition property="do.display.browser.debug">
                    <and>
                        <isset property="do.display.browser"/>
                        <not><isset property="do.debug.client"/></not>
                        <isset property="browser.context"/>
                    </and>
                </condition>
                <available file="${{conf.dir}}/MANIFEST.MF" property="has.custom.manifest"/>
                <available file="${{persistence.xml.dir}}/persistence.xml" property="has.persistence.xml"/>

                <condition property="do.war.package.with.custom.manifest">
                    <isset property="has.custom.manifest"/>
                </condition>
                <condition property="do.war.package.without.custom.manifest">
                    <not>
                        <isset property="has.custom.manifest"/>
                    </not>
                </condition>
                <!--
                    #97118
                    used to determine the build strategy for run
                -->
                <condition property="do.tmp.war.package.with.custom.manifest">
                    <and>
                        <isset property="has.custom.manifest"/>
                        <or>
                            <isfalse value="${{directory.deployment.supported}}"/>
                            <isset property="dist.ear.dir"/>
                        </or>
                    </and>
                </condition>
                <condition property="do.tmp.war.package.without.custom.manifest">
                    <and>
                        <not>
                            <isset property="has.custom.manifest"/>
                        </not>
                        <or>
                            <isfalse value="${{directory.deployment.supported}}"/>
                            <isset property="dist.ear.dir"/>
                        </or>
                    </and>
                </condition>
                <condition property="do.tmp.war.package">
                    <or>
                        <isfalse value="${{directory.deployment.supported}}"/>
                        <isset property="dist.ear.dir"/>
                    </or>
                </condition>
                
                <property value="${{build.web.dir}}/META-INF" name="build.meta.inf.dir"/>
                
                <condition property="application.args.param" value="${{application.args}}" else="">
                    <and>
                        <isset property="application.args"/>
                        <not>
                            <equals arg1="${{application.args}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <property name="source.encoding" value="${{file.encoding}}"/>
                <condition property="javadoc.encoding.used" value="${{javadoc.encoding}}">
                    <and>
                        <isset property="javadoc.encoding"/>
                        <not>
                            <equals arg1="${{javadoc.encoding}}" arg2=""/>
                        </not>
                    </and>
                </condition>
                <property name="javadoc.encoding.used" value="${{source.encoding}}"/>
                <property name="includes" value="**"/>
                <property name="excludes" value=""/>
                <property name="runmain.jvmargs" value=""/>
                <path id="endorsed.classpath.path" path="${{endorsed.classpath}}"/>
                <condition property="endorsed.classpath.cmd.line.arg" value="-Xbootclasspath/p:'${{toString:endorsed.classpath.path}}'" else="">
                    <and>
                        <isset property="endorsed.classpath"/>
                        <length length="0" string="${{endorsed.classpath}}" when="greater"/>
                    </and>
                </condition>
                <xsl:if test="not(/p:project/p:configuration/webproject3:data/webproject3:explicit-platform)">
                <condition property="jdkBug6558476" else="false"> <!-- Force fork even on default platform http://bugs.sun.com/view_bug.do?bug_id=6558476 on JDK 1.5 and 1.6 on Windows -->
                    <and>
                        <matches string="${{java.specification.version}}" pattern="1\.[56]"/>
                        <not>
                            <os family="unix"/>
                        </not>
                    </and>
                </condition>
                <property name="javac.fork" value="${{jdkBug6558476}}"/>
                </xsl:if>
                <condition property="junit.available">
                    <or>
                        <available classname="org.junit.Test" classpath="${{run.test.classpath}}"/>
                        <available classname="junit.framework.Test" classpath="${{run.test.classpath}}"/>
                    </or>
                </condition>
                <condition property="testng.available">
                    <available classname="org.testng.annotations.Test" classpath="${{run.test.classpath}}"/>
                </condition>
                <condition property="junit+testng.available">
                    <and>
                        <istrue value="${{junit.available}}"/>
                        <istrue value="${{testng.available}}"/>
                    </and>
                </condition>
                <condition property="testng.mode" value="mixed" else="testng">
                    <istrue value="${{junit+testng.available}}"/>
                </condition>
                <condition property="testng.debug.mode" value="-mixed" else="">
                    <istrue value="${{junit+testng.available}}"/>
                </condition>
            </target>
            
            <!-- COS feature - used in run-deploy -->
            <!-- compiler use deploy.on.save flag to fire changes -->
            <target name="-init-cos">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="unless">deploy.on.save</xsl:attribute>
                <condition>
                    <xsl:attribute name="property">deploy.on.save</xsl:attribute>
                    <xsl:attribute name="value">true</xsl:attribute>
                    <or>
                        <istrue value="${{j2ee.deploy.on.save}}"/>
                        <istrue value="${{j2ee.compile.on.save}}"/>
                    </or>
                </condition>            
            </target>
            
            <target name="-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-init-check">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init</xsl:attribute>
                <!-- XXX XSLT 2.0 would make it possible to use a for-each here -->
                <!-- Note that if the properties were defined in project.xml that would be easy -->
                <!-- But required props should be defined by the AntBasedProjectType, not stored in each project -->
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="build.web.dir">Must set build.web.dir</fail>
                <fail unless="build.generated.dir">Must set build.generated.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.war">Must set dist.war</fail>
                <condition property="missing.j2ee.server.home">
                    <and>
                        <matches pattern="j2ee.server.home" string="${{j2ee.platform.classpath}}"/>
                        <not>
                            <isset property="j2ee.server.home"/>
                        </not>
                    </and>
                </condition>
                <fail if="missing.j2ee.server.home">
The Java EE server classpath is not correctly set up - server home directory is missing.
Either open the project in the IDE and assign the server or setup the server classpath manually.
For example like this:
   ant -Dj2ee.server.home=&lt;app_server_installation_directory&gt;
                </fail>
                <fail unless="j2ee.platform.classpath">
The Java EE server classpath is not correctly set up. Your active server type is ${j2ee.server.type}.
Either open the project in the IDE and assign the server or setup the server classpath manually.
For example like this:
   ant -Duser.properties.file=&lt;path_to_property_file&gt; (where you put the property "j2ee.platform.classpath" in a .properties file)
or ant -Dj2ee.platform.classpath=&lt;server_classpath&gt; (where no properties file is used)
                </fail>                
            </target>
            
            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">value</xsl:attribute>
                    </attribute>
                    <sequential>
                        <property name="@{{name}}" value="${{@{{value}}}}"/>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-javac-with-processors" depends="-init-ap-cmdline-properties" if="ap.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">processorpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processorpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">apgeneratedsrcdir</xsl:attribute>
                        <xsl:attribute name="default">${build.generated.sources.dir}/ap-source-output</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">gensrcdir</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="empty.dir" location="${{build.dir}}/empty"/><!-- #157692 -->
                        <mkdir dir="${{empty.dir}}"/>
                        <mkdir dir="@{{apgeneratedsrcdir}}"/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <!-- XXX #137060 likely needs to be fixed here -->
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/webproject3:data/webproject3:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:attribute name="fork">${javac.fork}</xsl:attribute> <!-- Force fork even on default platform http://bugs.sun.com/view_bug.do?bug_id=6558476 -->
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                            </xsl:if>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                                     <compilerarg value="-processorpath" />
                            <compilerarg path="@{{processorpath}}:${{empty.dir}}" />
                            <compilerarg line="${{ap.processors.internal}}" />
                            <compilerarg value="-s" />
                            <compilerarg path="@{{apgeneratedsrcdir}}" />
                            <compilerarg line="${{ap.proc.none.internal}}" />
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
           <target name="-init-macrodef-javac-without-processors" depends="-init-ap-cmdline-properties" unless="ap.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">processorpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processorpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">apgeneratedsrcdir</xsl:attribute>
                        <xsl:attribute name="default">${build.generated.sources.dir}/ap-source-output</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">debug</xsl:attribute>
                        <xsl:attribute name="default">${javac.debug}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">gensrcdir</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="empty.dir" location="${{build.dir}}/empty"/><!-- #157692 -->
                        <mkdir dir="${{empty.dir}}"/>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <!-- XXX #137060 likely needs to be fixed here -->
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/webproject3:data/webproject3:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="fork">yes</xsl:attribute>
                                <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                            </xsl:if>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <compilerarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-macrodef-javac" depends="-init-macrodef-javac-with-processors,-init-macrodef-javac-without-processors">
                <macrodef> <!-- #36033, #85707 -->
                    <xsl:attribute name="name">depend</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <depend>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="cache">${build.dir}/depcache</xsl:attribute>
                            <xsl:attribute name="includes">${includes}</xsl:attribute>
                            <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                        </depend>
                    </sequential>
                </macrodef>
                <macrodef> <!-- #85707 -->
                    <xsl:attribute name="name">force-recompile</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <fail unless="javac.includes">Must set javac.includes</fail>
                        <!-- XXX one little flaw in this weird trick: does not work on folders. -->
                        <pathconvert>
                            <xsl:attribute name="property">javac.includes.binary</xsl:attribute>
                            <xsl:attribute name="pathsep">${line.separator}</xsl:attribute>
                            <path>
                                <filelist>
                                    <xsl:attribute name="dir">@{destdir}</xsl:attribute>
                                    <xsl:attribute name="files">${javac.includes}</xsl:attribute>
                                </filelist>
                            </path>
                            <globmapper>
                                <xsl:attribute name="from">*.java</xsl:attribute>
                                <xsl:attribute name="to">*.class</xsl:attribute>
                            </globmapper>
                        </pathconvert>
                        <tempfile property="javac.includesfile.binary" deleteonexit="true"/>
                        <echo message="${{javac.includes.binary}}" file="${{javac.includesfile.binary}}"/>
                        <delete>
                            <files includesfile="${{javac.includesfile.binary}}"/>
                        </delete>
                        <delete file="${{javac.includesfile.binary}}"/> <!-- deleteonexit keeps the file during IDE run -->
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-junit-init" if="${{junit.available}}">
                <condition property="nb.junit.batch" value="true" else="false">
                    <and>
                        <istrue value="${{junit.available}}"/>
                        <not>
                            <isset property="test.method"/>
                        </not>
                    </and>
                </condition>
                <condition property="nb.junit.single" value="true" else="false">
                    <and>
                        <istrue value="${{junit.available}}"/>
                        <isset property="test.method"/>
                    </and>
                </condition>
            </target>

            <target name="-init-test-properties">
                <property>
                    <xsl:attribute name="name">test.binaryincludes</xsl:attribute>
                    <xsl:attribute name="value">&lt;nothing&gt;</xsl:attribute>
                </property>
                <property>
                    <xsl:attribute name="name">test.binarytestincludes</xsl:attribute>
                    <xsl:attribute name="value"></xsl:attribute>
                </property>
                <property>
                    <xsl:attribute name="name">test.binaryexcludes</xsl:attribute>
                    <xsl:attribute name="value"></xsl:attribute>
                </property>
            </target>

            <target name="-init-macrodef-junit-single" if="${{nb.junit.single}}" unless="${{nb.junit.batch}}">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <test todir="${{build.test.results.dir}}" name="@{{testincludes}}" methods="@{{testmethods}}"/>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg value="-ea"/>
                            <customize/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-batch" if="${{nb.junit.batch}}" unless="${{nb.junit.single}}" depends="-init-test-properties">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="run.jvmargs.ide" value=""/>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${build.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                                    <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                    <xsl:with-param name="includes2">@{testincludes}</xsl:with-param>
                                    <xsl:with-param name="excludes">@{excludes}</xsl:with-param>
                                </xsl:call-template>
                                <fileset dir="${{build.test.classes.dir}}" excludes="@{{excludes}},${{excludes}},${{test.binaryexcludes}}" includes="${{test.binaryincludes}}">
                                    <filename name="${{test.binarytestincludes}}"/>
                                </fileset>
                            </batchtest>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg value="-ea"/>
                            <jvmarg line="${{run.jvmargs.ide}}"/>
                            <customize/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit" if="${{junit.available}}" depends="-init-macrodef-junit-init,-init-macrodef-junit-single, -init-macrodef-junit-batch"/>

            <target name="-init-macrodef-testng" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <condition property="testng.methods.arg" value="@{{testincludes}}.@{{testmethods}}" else="">
                            <isset property="test.method"/>
                        </condition>
                        <union id="test.set">
                            <xsl:call-template name="createFilesets">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                                <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                <xsl:with-param name="includes2">@{testincludes}</xsl:with-param>
                                <xsl:with-param name="excludes">@{excludes},**/*.xml</xsl:with-param>
                            </xsl:call-template>
                        </union>
                        <taskdef name="testng" classname="org.testng.TestNGAntTask" classpath="${{run.test.classpath}}"/>
                        <testng>
                            <xsl:attribute name="listeners">org.testng.reporters.VerboseReporter</xsl:attribute>
                            <xsl:attribute name="mode">${testng.mode}</xsl:attribute>
                            <xsl:attribute name="classfilesetref">test.set</xsl:attribute>
                            <xsl:attribute name="workingDir">${basedir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureProperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="methods">${testng.methods.arg}</xsl:attribute>
                            <xsl:attribute name="outputdir">${build.test.results.dir}</xsl:attribute>
                            <xsl:attribute name="suitename"><xsl:value-of select="$codename"/></xsl:attribute>
                            <xsl:attribute name="testname">TestNG tests</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <xmlfileset dir="${{build.test.classes.dir}}" includes="@{{testincludes}}"/>
                            <propertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper from="test-sys-prop.*" to="*" type="glob"/>
                            </propertyset>
                            <customize/>
                        </testng>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-impl">
                <macrodef>
                    <xsl:attribute name="name">test-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                        <xsl:attribute name="implicit">true</xsl:attribute>
                    </element>
                    <sequential>
                        <echo>No tests executed.</echo>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-impl" depends="-init-macrodef-junit" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                        <xsl:attribute name="implicit">true</xsl:attribute>
                    </element>
                    <sequential>
                        <webproject2:junit includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </webproject2:junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-impl" depends="-init-macrodef-testng" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                        <xsl:attribute name="implicit">true</xsl:attribute>
                    </element>
                    <sequential>
                        <webproject2:testng includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </webproject2:testng>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test" depends="-init-macrodef-test-impl,-init-macrodef-junit-impl,-init-macrodef-testng-impl">
                <macrodef>
                    <xsl:attribute name="name">test</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <sequential>
                        <webproject2:test-impl includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize>
                                <classpath>
                                    <path path="${{run.test.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.platform.embeddableejb.classpath}}"/>
                                </classpath>
                                <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                                <jvmarg line="${{runmain.jvmargs}}"/>
                            </customize>
                        </webproject2:test-impl>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-debug" if="${{junit.available}}" unless="${{nb.junit.batch}}">
                <macrodef>
                    <xsl:attribute name="name">junit-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <xsl:element name="element">
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </xsl:element>
                    <sequential>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <test todir="${{build.test.results.dir}}" name="@{{testincludes}}" methods="@{{testmethods}}"/>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg value="-ea"/>
                            <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                            <customize/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-debug-batch" if="${{nb.junit.batch}}" depends="-init-test-properties">
                <macrodef>
                    <xsl:attribute name="name">junit-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="run.jvmargs.ide" value=""/>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="dir">${basedir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${build.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <batchtest todir="${{build.test.results.dir}}">
                                <xsl:call-template name="createFilesets">
                                    <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                                    <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                    <xsl:with-param name="includes2">@{testincludes}</xsl:with-param>
                                    <xsl:with-param name="excludes">@{excludes}</xsl:with-param>
                                </xsl:call-template>
                                <fileset dir="${{build.test.classes.dir}}" excludes="@{{excludes}},${{excludes}},${{test.binaryexcludes}}" includes="${{test.binaryincludes}}">
                                    <filename name="${{test.binarytestincludes}}"/>
                                </fileset>
                            </batchtest>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg value="-ea"/>
                            <jvmarg line="${{run.jvmargs.ide}}"/>
                            <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                            <customize/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-debug-impl" depends="-init-macrodef-junit-debug,-init-macrodef-junit-debug-batch" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                        <xsl:attribute name="implicit">true</xsl:attribute>
                    </element>
                    <sequential>
                        <webproject2:junit-debug includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </webproject2:junit-debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-debug" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">testClass</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testMethod</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <xsl:element name="element">
                        <xsl:attribute name="name">customize2</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </xsl:element>
                    <sequential>
                        <condition property="test.class.or.method" value="-methods @{{testClass}}.@{{testMethod}}" else="-testclass @{{testClass}}">
                            <isset property="test.method"/>
                        </condition>
                        <condition property="testng.cmd.args" value="@{{testClass}}" else="-suitename {$codename} -testname @{{testClass}} ${{test.class.or.method}}">
                            <matches string="@{{testClass}}" pattern=".*\.xml"/>
                        </condition>
                        <delete dir="${{build.test.results.dir}}" quiet="true"/>
                        <mkdir dir="${{build.test.results.dir}}"/>
                        <webproject1:debug args="${{testng.cmd.args}}" classname="org.testng.TestNG" classpath="${{debug.test.classpath}}:${{j2ee.platform.embeddableejb.classpath}}">
                            <customize>
                                <customize2/>
                                <jvmarg value="-ea"/>
                                <arg line="${{testng.debug.mode}}"/>
                                <arg line="-d ${{build.test.results.dir}}"/>
                                <arg line="-listener org.testng.reporters.VerboseReporter"/>
                            </customize>
                        </webproject1:debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-debug-impl" depends="-init-macrodef-testng-debug" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng-debug-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">testClass</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testMethod</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <xsl:element name="element">
                        <xsl:attribute name="name">customize2</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                        <xsl:attribute name="implicit">true</xsl:attribute>
                    </xsl:element>
                    <sequential>
                        <webproject2:testng-debug testClass="@{{testClass}}" testMethod="@{{testMethod}}">
                            <customize2/>
                        </webproject2:testng-debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-debug-junit" depends="-init-macrodef-junit-debug-impl" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testClass</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testMethod</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <sequential>
                        <webproject2:test-debug-impl includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize>
                                <classpath>
                                    <path path="${{run.test.classpath}}:${{j2ee.platform.classpath}}:${{j2ee.platform.embeddableejb.classpath}}"/>
                                </classpath>
                                <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                                <jvmarg line="${{runmain.jvmargs}}"/>
                            </customize>
                        </webproject2:test-debug-impl>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-debug-testng" depends="-init-macrodef-testng-debug-impl" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testincludes</xsl:attribute>
                        <xsl:attribute name="default">**</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testmethods</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testClass</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">testMethod</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <sequential>
                        <webproject2:testng-debug-impl testClass="@{{testClass}}" testMethod="@{{testMethod}}">
                            <customize2>
                                <syspropertyset>
                                    <propertyref prefix="test-sys-prop."/>
                                    <mapper from="test-sys-prop.*" to="*" type="glob"/>
                                </syspropertyset>
                            </customize2>
                        </webproject2:testng-debug-impl>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-debug" depends="-init-macrodef-test-debug-junit,-init-macrodef-test-debug-testng"/>
            
            <target name="-init-macrodef-java">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg line="${{runmain.jvmargs}}"/>
                            <!--
                                #113297, #118187
                                for the moment debug.classpath equals run.classpath
                                XXX: introduce run.classpath and use it for debug.classpath
                             -->
                            <classpath>
                                <path path="@{{classpath}}:${{j2ee.platform.classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-nbjsdebug">
                <macrodef>
                    <xsl:attribute name="name">nbjsdebugstart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">webUrl</xsl:attribute>
                        <xsl:attribute name="default">${client.url}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjsdebugstart webUrl="@{{webUrl}}" urlPart="${{client.urlPart}}"/>
                    </sequential>
                </macrodef>                
            </target>
            
            <target name="-init-macrodef-nbjpda" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdastart transport="${{debug-transport}}" addressproperty="jpda.address" name="@{{name}}">
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">dir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdareload>
                            <fileset includes="${{fix.classes}}" dir="@{{dir}}" >
                                <include name="${{fix.includes}}*.class"/>
                            </fileset>
                        </nbjpdareload>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdaappreloaded</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <sequential>
                        <nbjpdaappreloaded />
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-debug-args">
                <condition property="debug-transport-by-os" value="dt_shmem" else="dt_socket">
                    <os family="windows"/>
                </condition>
                <condition property="debug-transport" value="${{debug.transport}}" else="${{debug-transport-by-os}}">
                    <isset property="debug.transport"/>
                </condition>
            </target>
            
            <target name="-init-macrodef-debug" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">args</xsl:attribute>
                        <xsl:attribute name="default">${application.args.param}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}">
                            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                            <jvmarg line="${{runmain.jvmargs}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <arg line="@{{args}}"/>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-taskdefs">
                <fail unless="libs.CopyLibs.classpath">
The libs.CopyLibs.classpath property is not set up.
This property must point to 
org-netbeans-modules-java-j2seproject-copylibstask.jar file which is part
of NetBeans IDE installation and is usually located at 
&lt;netbeans_installation&gt;/java&lt;version&gt;/ant/extra folder.
Either open the project in the IDE and make sure CopyLibs library
exists or setup the property manually. For example like this:
 ant -Dlibs.CopyLibs.classpath=a/path/to/org-netbeans-modules-java-j2seproject-copylibstask.jar
                </fail>
                <taskdef resource="org/netbeans/modules/java/j2seproject/copylibstask/antlib.xml" classpath="${{libs.CopyLibs.classpath}}"/>
            </target>
            
            <target name="-init-ap-cmdline-properties">
                <property name="annotation.processing.enabled" value="true" />
                <property name="annotation.processing.processors.list" value="" />
                <property name="annotation.processing.run.all.processors" value="true" />
                <property name="javac.processorpath" value="${{javac.classpath}}" />
                <property name="javac.test.processorpath" value="${{javac.test.classpath}}"/>
                <condition property="ap.supported.internal" value="true">
                    <not>
                        <matches string="${{javac.source}}" pattern="1\.[0-5](\..*)?" />
                    </not>
                </condition>
            </target>
            <target name="-init-ap-cmdline-supported" depends="-init-ap-cmdline-properties" if="ap.supported.internal">
                <condition property="ap.processors.internal" value="-processor ${{annotation.processing.processors.list}}" else="">
                    <isfalse value="${{annotation.processing.run.all.processors}}" />
                </condition>
                <condition property="ap.proc.none.internal" value="-proc:none" else="">
                    <isfalse value="${{annotation.processing.enabled}}" />
                </condition>
            </target>
            <target name="-init-ap-cmdline" depends="-init-ap-cmdline-properties,-init-ap-cmdline-supported">
                <property name="ap.cmd.line.internal" value=""/>
            </target>

            <xsl:comment>
                pre NB7.2 profiling section; consider it deprecated
            </xsl:comment>
            <target name="profile-init" depends="-profile-pre-init, init, -profile-post-init, -profile-init-check">
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
            </target>

            <target name="-profile-pre-init">
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-profile-post-init">
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            <target name="-profile-init-check">
                <xsl:attribute name="depends">-profile-pre-init, init, -profile-post-init</xsl:attribute>
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <fail unless="profiler.info.jvm">Must set JVM to use for profiling in profiler.info.jvm</fail>
                <fail unless="profiler.info.jvmargs.agent">Must set profiler agent JVM arguments in profiler.info.jvmargs.agent</fail>
            </target>
            <xsl:comment>
                end of pre NB7.2 profiling section
            </xsl:comment>

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-test,-init-macrodef-test-debug,-init-macrodef-java,-init-macrodef-nbjpda,-init-macrodef-nbjsdebug,-init-macrodef-debug,-init-taskdefs,-init-ap-cmdline</xsl:attribute>
            </target>
            
            <xsl:comment>
                COMPILATION SECTION
            </xsl:comment>
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-module-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-ear-jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
                <xsl:with-param name="ear" select="'true'"/>
            </xsl:call-template>
            
            <target name="deps-jar">
                <xsl:attribute name="depends">init, deps-module-jar, deps-ear-jar</xsl:attribute>
                <xsl:attribute name="unless">no.deps</xsl:attribute>
            </target>
  
            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service|/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                <target name="wscompile-init" depends="init">
                    <taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile"
                             classpath="${{wscompile.classpath}}"/>
                    <taskdef name="wsclientuptodate" classname="org.netbeans.modules.websvc.jaxrpc.ant.WsClientUpToDate"
                             classpath="${{wsclientuptodate.classpath}}"/>
                    <mkdir dir="${{build.web.dir}}/WEB-INF/wsdl"/>
                    <mkdir dir="${{webinf.dir}}/wsdl"/>
                    <mkdir dir="${{build.classes.dir}}"/>
                    <mkdir dir="${{build.generated.sources.dir}}/jax-rpc"/>
                    <mkdir dir="${{build.generated.dir}}/jax-rpc-binaries"/>
                    
                    <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="webproject3:web-service-client-name"/>
                        </xsl:variable>
                        
                        <wsclientuptodate property="wscompile.client.{$wsclientname}.notrequired"
                                          sourcewsdl="${{webinf.dir}}/wsdl/{$wsclientname}.wsdl"
                                          targetdir="${{build.generated.sources.dir}}/jax-rpc"/>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                <target name="fromwsdl-noop"/>
            </xsl:if>
            
            <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                <xsl:variable name="wsname">
                    <xsl:value-of select="webproject3:web-service-name"/>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="webproject3:from-wsdl">
                        <target name="{$wsname}_wscompile" depends="init, wscompile-init">
                            <wscompile import="true"
                                       config="${{{$wsname}.config.name}}"
                                       features="${{wscompile.service.{$wsname}.features}}"
                                       mapping="${{webinf.dir}}/${{{$wsname}.mapping}}"
                                       classpath="${{wscompile.classpath}}:${{javac.classpath}}"
                                       nonClassDir="${{build.web.dir}}/WEB-INF/wsdl"
                                       verbose="true"
                                       xPrintStackTrace="true"
                                       base="${{build.generated.dir}}/jax-rpc-binaries"
                                       sourceBase="${{src.dir}}"
                                       keep="true"
                                       fork="true"/>
                        </target>
                    </xsl:when>
                    <xsl:otherwise>
                        <target name="{$wsname}_wscompile" depends="wscompile-init">
                            <wscompile
                                define="true"
                                fork="true"
                                keep="true"
                                base="${{build.generated.dir}}/jax-rpc-binaries"
                                xPrintStackTrace="true"
                                verbose="true"
                                nonClassDir="${{build.web.dir}}/WEB-INF/wsdl"
                                classpath="${{wscompile.classpath}}:${{build.classes.dir}}:${{javac.classpath}}"
                                mapping="${{build.web.dir}}/WEB-INF/${{{$wsname}.mapping}}"
                                config="${{{$wsname}.config.name}}"
                                features="${{wscompile.service.{$wsname}.features}}"
                                sourceBase="${{build.generated.sources.dir}}/jax-rpc">
                            </wscompile>
                        </target>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            
            <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="webproject3:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="webproject3:web-service-stub-type">
                            <xsl:value-of select="webproject3:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="webproject3:web-service-stub-type">
                            <xsl:value-of select="webproject3:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                
                <target name="{$wsclientname}-client-wscompile" depends="wscompile-init" unless="wscompile.client.{$wsclientname}.notrequired">
                    <property name="config_target" location="${{webinf.dir}}/wsdl"/>
                    <copy file="${{webinf.dir}}/wsdl/{$wsclientname}-config.xml"
                          tofile="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-config.xml" filtering="on" encoding="UTF-8">
                        <filterset>
                            <!-- replace token with reference to WSDL file in source tree, not build tree, since the
                            the file probably has not have been copied to the build tree yet. -->
                            <filter token="CONFIG_ABSOLUTE_PATH" value="${{config_target}}"/>
                        </filterset>
                    </copy>
                    <wscompile
                        verbose="${{wscompile.client.{$wsclientname}.verbose}}"
                        debug="${{wscompile.client.{$wsclientname}.debug}}"
                        xPrintStackTrace="${{wscompile.client.{$wsclientname}.xPrintStackTrace}}"
                        xSerializable="${{wscompile.client.{$wsclientname}.xSerializable}}"
                        optimize="${{wscompile.client.{$wsclientname}.optimize}}"
                        fork="true" keep="true"
                        client="{$useclient}" import="{$useimport}"
                        features="${{wscompile.client.{$wsclientname}.features}}"
                        base="${{build.generated.dir}}/jax-rpc-binaries"
                        sourceBase="${{build.generated.sources.dir}}/jax-rpc"
                        classpath="${{wscompile.classpath}}:${{javac.classpath}}"
                        mapping="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-mapping.xml"
                        httpproxy="${{wscompile.client.{$wsclientname}.proxy}}"
                        config="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>
            
            <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                <target name="web-service-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="webproject3:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="webproject3:web-service-client-name"/><xsl:text>-client-wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                    <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="webproject3:web-service-client-name"/>
                        </xsl:variable>
                        <copy file="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-mapping.xml"
                              tofile="${{build.web.dir}}/WEB-INF/{$wsclientname}-mapping.xml"/>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar<xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">,web-service-client-generate</xsl:if>
                </xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>
            
            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-copy-webdir">
                <copy todir="${{build.web.dir}}">
                    <fileset excludes="${{build.web.excludes}},${{excludes}}" includes="${{includes}}" dir="${{web.docbase.dir}}">
                        <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                            <xsl:attribute name="excludes">WEB-INF/classes/** WEB-INF/web.xml WEB/sun-web.xml</xsl:attribute>
                        </xsl:if>
                    </fileset>
                </copy>
                <copy todir="${{build.web.dir}}/WEB-INF">
                    <fileset excludes="${{build.web.excludes}}" dir="${{webinf.dir}}">
                        <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                            <xsl:attribute name="excludes">classes/** web.xml sun-web.xml</xsl:attribute>
                        </xsl:if>
                    </fileset>
                </copy>
                
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                    <xsl:comment>For web services, refresh web.xml and sun-web.xml</xsl:comment>
                    <copy todir="${{build.web.dir}}" overwrite="true">
                        <fileset includes="WEB-INF/web.xml WEB-INF/sun-web.xml" dir="${{web.docbase.dir}}"/>
                    </copy>
                    <copy todir="${{build.web.dir}}/WEB-INF" overwrite="true">
                        <fileset includes="web.xml sun-web.xml" dir="${{webinf.dir}}"/>
                    </copy>
                </xsl:if>
            </target>

            <target name="-do-compile">
                <xsl:attribute name="depends">init, deps-jar, -pre-pre-compile, -pre-compile, -copy-manifest, -copy-persistence-xml, -copy-webdir, library-inclusion-in-archive,library-inclusion-in-manifest</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                
                <webproject2:javac destdir="${{build.classes.dir}}" gensrcdir="${{build.generated.sources.dir}}"/>
                
                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-copy-manifest" if="has.custom.manifest">
                <mkdir dir="${{build.meta.inf.dir}}"/>
                <copy todir="${{build.meta.inf.dir}}">
                    <fileset dir="${{conf.dir}}" includes="MANIFEST.MF"/>
                </copy>
            </target>
            
            <target name="-copy-persistence-xml" if="has.persistence.xml">
                <mkdir dir="${{build.web.dir}}/WEB-INF/classes/META-INF"/>
                <copy todir="${{build.web.dir}}/WEB-INF/classes/META-INF">
                    <fileset dir="${{persistence.xml.dir}}" includes="persistence.xml orm.xml"/>
                </copy>
            </target>
            
            <target name="-post-compile">
                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text>
                            </xsl:if>
                            <xsl:choose>
                                <xsl:when test="not(webproject3:from-wsdl)">
                                    <xsl:value-of select="webproject3:web-service-name"/><xsl:text>_wscompile</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>fromwsdl-noop</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                        
                        
                    </xsl:attribute>
                </xsl:if>
                
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>
            
            <target name="-pre-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <webproject2:javac includes="${{javac.includes}}" excludes="" gensrcdir="${{build.generated.sources.dir}}"/>
                
                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile-single,-do-compile-single,-post-compile-single</xsl:attribute>
            </target>
            
            <property name="jspc.schemas" value="/resources/schemas/"/><!-- #192308 -->
            <property name="jspc.dtds" value="/resources/dtds/"/><!-- #192308 -->
            
            <target name="compile-jsps">
                <xsl:attribute name="depends">compile</xsl:attribute>
                <xsl:attribute name="if">do.compile.jsps</xsl:attribute>
                <xsl:attribute name="description">Test compile JSP pages to expose compilation errors.</xsl:attribute>
                
                <mkdir dir="${{build.generated.dir}}/src"/>
                <java classname="org.netbeans.modules.web.project.ant.JspC"
                      fork="true"
                      failonerror="true"
                >
                    <arg value="-uriroot"/>
                    <arg file="${{basedir}}/${{build.web.dir}}"/>
                    <arg value="-d"/>
                    <arg file="${{basedir}}/${{build.generated.dir}}/src"/>
                    <arg value="-die1"/>
                    <arg value="-schemas ${{jspc.schemas}}"/>
                    <arg value="-dtds ${{jspc.dtds}}"/>
                    <arg value="-compilerSourceVM ${{javac.source}}"/>
                    <arg value="-compilerTargetVM ${{javac.target}}"/>
                    <arg value="-javaEncoding ${{source.encoding}}"/> <!-- #72175 -->
                    <arg value="-sysClasspath ${{libs.jsp-compilation-syscp.classpath}}"/><!-- #192308 -->
                    <classpath path="${{java.home}}/../lib/tools.jar:${{libs.jsp-compiler.classpath}}:${{libs.jsp-compilation.classpath}}"/>
                </java>
                <mkdir dir="${{build.generated.dir}}/classes"/>
                <webproject2:javac
                    srcdir="${{build.generated.dir}}/src"
                    destdir="${{build.generated.dir}}/classes"
                    classpath="${{build.classes.dir}}:${{libs.jsp-compilation.classpath}}:${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
                
            </target>
            
            <target name="-do-compile-single-jsp">
                <xsl:attribute name="depends">compile</xsl:attribute>
                <xsl:attribute name="if">jsp.includes</xsl:attribute>
                <fail unless="javac.jsp.includes">Must select some files in the IDE or set javac.jsp.includes</fail>
                
                <mkdir dir="${{build.generated.dir}}/src"/>
                <java classname="org.netbeans.modules.web.project.ant.JspCSingle"
                      fork="true"
                      failonerror="true"
                >
                    <arg value="-uriroot"/>
                    <arg file="${{basedir}}/${{build.web.dir}}"/>
                    <arg value="-d"/>
                    <arg file="${{basedir}}/${{build.generated.dir}}/src"/>
                    <arg value="-die1"/>
                    <arg value="-schemas ${{jspc.schemas}}"/>
                    <arg value="-dtds ${{jspc.dtds}}"/>
                    <arg value="-sysClasspath ${{libs.jsp-compilation-syscp.classpath}}"/><!-- #192308 -->
                    <arg value="-jspc.files"/>
                    <arg path="${{jsp.includes}}"/>
                    <arg value="-compilerSourceVM ${{javac.source}}"/>
                    <arg value="-compilerTargetVM ${{javac.target}}"/>
                    <arg value="-javaEncoding ${{source.encoding}}"/> <!-- #72175 -->
                    <classpath path="${{java.home}}/../lib/tools.jar:${{libs.jsp-compiler.classpath}}:${{libs.jsp-compilation.classpath}}"/>
                </java>
                <mkdir dir="${{build.generated.dir}}/classes"/>
                <webproject2:javac
                    srcdir="${{build.generated.dir}}/src"
                    destdir="${{build.generated.dir}}/classes"
                    classpath="${{build.classes.dir}}:${{libs.jsp-compilation.classpath}}:${{javac.classpath}}:${{j2ee.platform.classpath}}">
                    <customize>
                        <patternset includes="${{javac.jsp.includes}}"/>
                    </customize>
                </webproject2:javac>
                <!--
                <webproject:javac xmlns:webproject="http://www.netbeans.org/ns/web-project/1">
                <xsl:with-param name="srcdir" select="'${{build.generated.dir}}/src'"/>
                <xsl:with-param name="destdir" select="'${{build.generated.dir}}/classes'"/>
                <xsl:with-param name="classpath" select="'${{javac.classpath}}:${{j2ee.platform.classpath}}:${{build.classes.dir}}'"/>
                <xsl:with-param name="classpath" select="'${{javac.classpath}}:${{j2ee.platform.classpath}}:${{build.classes.dir}}:${{jspc.classpath}}'"/>
                </webproject:javac>
                -->
            </target>
            
            <target name="compile-single-jsp">
                <fail unless="jsp.includes">Must select a file in the IDE or set jsp.includes</fail>
                <antcall target="-do-compile-single-jsp"/>
            </target>
            
            <xsl:comment>
                DIST BUILDING SECTION
            </xsl:comment>
            
            <target name="-pre-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <!-- "dist" -->
            <target name="-do-dist-without-manifest" if="do.war.package.without.custom.manifest">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <xsl:call-template name="distWithoutCustomManifest"/>
            </target>
            <target name="-do-dist-with-manifest" if="do.war.package.with.custom.manifest">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <xsl:call-template name="distWithCustomManifest"/>
            </target>
            
            <!--
                #97118
                target to do war build, if the project is not going to be
                directory deployed. used by run-deploy as part of 'run'
            -->
            <target name="-do-tmp-dist-without-manifest" if="do.tmp.war.package.without.custom.manifest">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <xsl:call-template name="distWithoutCustomManifest"/>
            </target>
            <target name="-do-tmp-dist-with-manifest" if="do.tmp.war.package.with.custom.manifest">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist</xsl:attribute>
                <xsl:call-template name="distWithCustomManifest"/>
            </target>

            <target name="do-dist">
                <xsl:attribute name="depends">init,compile,compile-jsps,-pre-dist,-do-dist-with-manifest,-do-dist-without-manifest</xsl:attribute>
            </target>
            
            <target name="library-inclusion-in-manifest" depends="init">
                <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
                <!-- copy libraries into ear  -->
                <xsl:for-each select="//webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                    <copyfiles iftldtodir="${{build.web.dir}}/WEB-INF">
                        <xsl:attribute name="todir">${dist.ear.dir}</xsl:attribute>
                        <xsl:if test="//webproject3:web-module-libraries/webproject3:library[@dirs]">
                            <xsl:if test="(@dirs = 200)">
                                <xsl:attribute name="todir">${dist.ear.dir}/lib</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="(@dirs = 300)">
                                <xsl:attribute name="todir"><xsl:value-of select="concat('${build.web.dir}/',webproject3:path-in-war)"/></xsl:attribute>
                            </xsl:if>
                        </xsl:if>
                       <xsl:attribute name="files"><xsl:value-of select="webproject3:file"/></xsl:attribute>
<!--                       <xsl:attribute name="manifestproperty">
                           <xsl:value-of select="concat('manifest.', substring-before(substring-after(webproject3:file,'{'),'}'), '')"/>
                       </xsl:attribute> -->
                    </copyfiles>
                </xsl:for-each>
                <!-- copy additional content into web module -->
                <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-module-additional-libraries/webproject3:library[webproject3:path-in-war]">
                    <copyfiles>
                       <xsl:attribute name="todir"><xsl:value-of select="concat('${build.web.dir}/',webproject3:path-in-war)"/></xsl:attribute>
                       <xsl:attribute name="files"><xsl:value-of select="webproject3:file"/></xsl:attribute>
                    </copyfiles>
                </xsl:for-each>
                
                <mkdir dir="${{build.web.dir}}/META-INF"/>
                <manifest file="${{build.web.dir}}/META-INF/MANIFEST.MF" mode="update"/>
<!--                <manifest file="${{build.web.dir}}/META-INF/MANIFEST.MF" mode="update">
                    <xsl:if test="//webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                        <attribute>
                            <xsl:attribute name="name">Class-Path</xsl:attribute>
                            <xsl:attribute name="value">
                                <xsl:for-each select="//webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                                    <xsl:value-of select="concat('${manifest.', substring-before(substring-after(webproject3:file,'{'),'}'), '} ')"/>
                                </xsl:for-each>
                            </xsl:attribute>
                        </attribute>
                    </xsl:if>
                </manifest> -->
            </target>
            
            <target name="library-inclusion-in-archive" depends="init">
                <xsl:attribute name="unless">dist.ear.dir</xsl:attribute>
                <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-module-libraries/webproject3:library[webproject3:path-in-war]">
                    <copyfiles>
                       <xsl:attribute name="todir"><xsl:value-of select="concat('${build.web.dir}/',webproject3:path-in-war)"/></xsl:attribute>
                       <xsl:attribute name="files"><xsl:value-of select="webproject3:file"/></xsl:attribute>
                    </copyfiles>
                </xsl:for-each>
                
                <xsl:for-each select="/p:project/p:configuration/webproject3:data/webproject3:web-module-additional-libraries/webproject3:library[webproject3:path-in-war]">
                    <copyfiles>
                       <xsl:attribute name="todir"><xsl:value-of select="concat('${build.web.dir}/',webproject3:path-in-war)"/></xsl:attribute>
                       <xsl:attribute name="files"><xsl:value-of select="webproject3:file"/></xsl:attribute>
                    </copyfiles>
                </xsl:for-each>
            </target>

            <target depends="init" name="-clean-webinf-lib" if="dist.ear.dir">
                <!-- this may need to be optimized in future -->
                <delete dir="${{build.web.dir}}/WEB-INF/lib"/>
            </target>

            <target name="do-ear-dist">
                <xsl:attribute name="depends">init,-clean-webinf-lib,compile,compile-jsps,-pre-dist,library-inclusion-in-manifest</xsl:attribute>
                <xsl:attribute name="if">do.tmp.war.package</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.ear.war}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
                <jar jarfile="${{dist.ear.war}}" compress="${{jar.compress}}" manifest="${{build.web.dir}}/META-INF/MANIFEST.MF">
                    <fileset dir="${{build.web.dir}}" excludes="WEB-INF/classes/.netbeans_*,${{dist.archive.excludes}}"/>
                </jar>
            </target>
            
            <target name="-post-dist">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="dist">
                <xsl:attribute name="depends">init,compile,-pre-dist,do-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (WAR).</xsl:attribute>
            </target>
            
            <target name="dist-ear">
                <xsl:attribute name="depends">init,-clean-webinf-lib,-init-cos,compile,-pre-dist,do-ear-dist,-post-dist</xsl:attribute>
                <xsl:attribute name="description">Build distribution (WAR) to be packaged into an EAR.</xsl:attribute>
            </target>
            
            <xsl:comment>
                EXECUTION SECTION
            </xsl:comment>
            
            <target name="run">
                <xsl:attribute name="depends">run-deploy,run-display-browser</xsl:attribute>
                <xsl:attribute name="description">Deploy to server and show in browser.</xsl:attribute>
            </target>
            
            <target name="-pre-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-post-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-pre-nbmodule-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> This target can be overriden by NetBeans modules. Don't override it directly, use -pre-run-deploy task instead. </xsl:comment>
            </target>
            
            <target name="-post-nbmodule-run-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> This target can be overriden by NetBeans modules. Don't override it directly, use -post-run-deploy task instead. </xsl:comment>
            </target>
            
            <target name="-run-deploy-am">
                <xsl:comment> Task to deploy to the Access Manager runtime. </xsl:comment>
            </target>
            
            <target name="run-deploy">
                <xsl:attribute name="depends">init,-init-cos,compile,compile-jsps,-do-compile-single-jsp,-pre-dist,-do-tmp-dist-with-manifest,-do-tmp-dist-without-manifest,-pre-run-deploy,-pre-nbmodule-run-deploy,-run-deploy-nb,-init-deploy-ant,-deploy-ant,-run-deploy-am,-post-nbmodule-run-deploy,-post-run-deploy,-do-update-breakpoints</xsl:attribute>   
            </target>
            
            <target name="-run-deploy-nb" if="netbeans.home">
                <nbdeploy debugmode="false" clientUrlPart="${{client.urlPart}}" forceRedeploy="${{forceRedeploy}}"/>
            </target>
            
            <target name="-init-deploy-ant" unless="netbeans.home">
                <property name="deploy.ant.archive" value="${{dist.war}}"/>
                <property name="deploy.ant.docbase.dir" value="${{web.docbase.dir}}"/>
                <property name="deploy.ant.resource.dir" value="${{resource.dir}}"/>
                <property name="deploy.ant.enabled" value="true"/>
            </target>
            
            <target name="run-undeploy">
                <xsl:attribute name="depends">dist,-run-undeploy-nb,-init-deploy-ant,-undeploy-ant</xsl:attribute>
            </target>
            
            <target name="-run-undeploy-nb" if="netbeans.home">
                <fail message="Undeploy is not supported from within the IDE"/>
            </target>
            
            <target name="verify">
                <xsl:attribute name="depends">init,-pre-dist,dist,-post-dist</xsl:attribute>
                <nbverify file="${{dist.war}}"/>
            </target>
            
            <target name="run-display-browser">
                <xsl:attribute name="depends">run-deploy,-init-display-browser,-display-browser-nb-old,-display-browser-nb,-display-browser-cl</xsl:attribute>
            </target>
            
            <target name="-init-display-browser" if="do.display.browser">
                <condition property="do.display.browser.nb.old">
                    <and>
                        <isset property="netbeans.home"/>
                        <not>
                            <isset property="browser.context"/>
                        </not>
                    </and>
                </condition>
                <condition property="do.display.browser.nb">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="browser.context"/>
                    </and>
                </condition>
                <condition property="do.display.browser.cl">
                    <isset property="deploy.ant.enabled"/>
                </condition>
            </target>
            
            <target name="-display-browser-nb-old" if="do.display.browser.nb.old">
                <nbbrowse url="${{client.url}}"/>
            </target>

            <target name="-display-browser-nb" if="do.display.browser.nb">
                <nbbrowse url="${{client.url}}" context="${{browser.context}}" urlPath="${{client.urlPart}}"/>
            </target>
            
            <target name="-get-browser" if="do.display.browser.cl" unless="browser">
                <condition property="browser" value="rundll32">
                    <os family="windows"/>
                </condition>
                <condition property="browser.args" value="url.dll,FileProtocolHandler" else="">
                    <os family="windows"/>
                </condition>
                <condition property="browser" value="/usr/bin/open">
                    <os family="mac"/>
                </condition>
                <property environment="env"/>
                <condition property="browser" value="${{env.BROWSER}}">
                    <isset property="env.BROWSER"/>
                </condition>
                <condition property="browser" value="/usr/bin/firefox">
                    <available file="/usr/bin/firefox"/>
                </condition>
                <condition property="browser" value="/usr/local/firefox/firefox">
                    <available file="/usr/local/firefox/firefox"/>
                </condition>
                <condition property="browser" value="/usr/bin/mozilla">
                    <available file="/usr/bin/mozilla"/>
                </condition>
                <condition property="browser" value="/usr/local/mozilla/mozilla">
                    <available file="/usr/local/mozilla/mozilla"/>
                </condition>
                <condition property="browser" value="/usr/sfw/lib/firefox/firefox">
                    <available file="/usr/sfw/lib/firefox/firefox"/>
                </condition>
                <condition property="browser" value="/opt/csw/bin/firefox">
                    <available file="/opt/csw/bin/firefox"/>
                </condition>
                <condition property="browser" value="/usr/sfw/lib/mozilla/mozilla">
                    <available file="/usr/sfw/lib/mozilla/mozilla"/>
                </condition>
                <condition property="browser" value="/opt/csw/bin/mozilla">
                    <available file="/opt/csw/bin/mozilla"/>
                </condition>
            </target>
            
            <target name="-display-browser-cl" depends="-get-browser" if="do.display.browser.cl">
                <fail unless="browser">
                    Browser not found, cannot launch the deployed application. Try to set the BROWSER environment variable.
                </fail>
                <property name="browse.url" value="${{deploy.ant.client.url}}${{client.urlPart}}"/>
                <echo>Launching ${browse.url}</echo>
                <exec executable="${{browser}}" spawn="true">
                    <arg line="${{browser.args}} ${{browse.url}}"/>
                </exec>
            </target>
            
            <target name="run-main">
                <xsl:attribute name="depends">init,-init-cos,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <webproject1:java classname="${{run.class}}"/>
            </target>
            <target name="run-test-with-main">
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <webproject1:java classname="${{run.class}}" classpath="${{run.test.classpath}}"/>
            </target>
            
            <target name="-do-update-breakpoints">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <webproject1:nbjpdaappreloaded/>
            </target>            
            <xsl:comment>
                DEBUGGING SECTION
            </xsl:comment>
            
            <target name="debug">
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
                <xsl:attribute name ="depends">init,-init-cos,compile,compile-jsps,-do-compile-single-jsp,-pre-dist,-do-tmp-dist-with-manifest,-do-tmp-dist-without-manifest</xsl:attribute>
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <nbstartserver debugmode="true"/>
                <antcall target="connect-debugger"/>
                <nbdeploy debugmode="true" clientUrlPart="${{client.urlPart}}" forceRedeploy="true" />
                <antcall target="debug-display-browser-old"/>
                <antcall target="debug-display-browser"/>
                <antcall target="connect-client-debugger"/>
            </target>
            
            <target name="connect-debugger" if="do.debug.server" unless="is.debugged">
                <condition>
                    <xsl:attribute name="property">listeningcp</xsl:attribute>
                    <xsl:attribute name="value">sourcepath</xsl:attribute>
                    <istrue value="${{j2ee.compile.on.save}}"/>
                </condition>
                <nbjpdaconnect name="${{name}}" host="${{jpda.host}}" address="${{jpda.address}}" transport="${{jpda.transport}}" listeningcp="${{listeningcp}}">
                    <xsl:choose>
                        <xsl:when test="/p:project/p:configuration/webproject3:data/webproject3:web-services/webproject3:web-service|/p:project/p:configuration/webproject3:data/webproject3:web-service-clients/webproject3:web-service-client">
                            <classpath>
                                <path path="${{debug.classpath}}:${{j2ee.platform.classpath}}:${{ws.debug.classpaths}}"/>
                            </classpath>
                            <sourcepath>
                                <path path="${{web.docbase.dir}}:${{ws.web.docbase.dirs}}"/>
                            </sourcepath>
                        </xsl:when>
                        <xsl:otherwise>
                            <classpath>
                                <path path="${{debug.classpath}}:${{j2ee.platform.classpath}}"/>
                            </classpath>
                            <sourcepath>
                                <path path="${{web.docbase.dir}}"/>
                            </sourcepath>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                        <bootclasspath>
                            <path path="${{platform.bootcp}}"/>
                        </bootclasspath>
                    </xsl:if>
                </nbjpdaconnect>
            </target>
            
            <target name="debug-display-browser-old" if="do.display.browser.debug.old">
                <nbbrowse url="${{client.url}}"/>
            </target>

            <target name="debug-display-browser" if="do.display.browser.debug">
                <nbbrowse url="${{client.url}}" context="${{browser.context}}" urlPath="${{client.urlPart}}"/>
            </target>
            
            <target name="connect-client-debugger" if="do.debug.client">
                <webproject1:nbjsdebugstart webUrl="${{client.url}}"/>
            </target>

            <target name="-debug-start-debuggee-main-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <webproject1:debug classname="${{debug.class}}" classpath="${{debug.test.classpath}}"/>
            </target>

            <target name="debug-test-with-main">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-debug-start-debugger-main-test,-debug-start-debuggee-main-test</xsl:attribute>
            </target>

            <target name="debug-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,compile-jsps,-do-compile-single-jsp,debug</xsl:attribute>
            </target>
            <target name="-debug-start-debugger-main-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <webproject1:nbjpdastart name="${{debug.class}}" classpath="${{debug.test.classpath}}"/>
             </target>
            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <webproject1:nbjpdastart name="${{debug.class}}"/>
            </target>
            
            <target name="-debug-start-debuggee-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <webproject1:debug classname="${{debug.class}}"/>
            </target>
            
            <target name="debug-single-main">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-start-debugger,-debug-start-debuggee-single</xsl:attribute>
            </target>
            
            <target name="-pre-debug-fix">
                <xsl:attribute name="depends">init</xsl:attribute>
                <fail unless="fix.includes">Must set fix.includes</fail>
                <property name="javac.includes" value="${{fix.includes}}.java"/>
            </target>
            
            <target name="-do-debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-single</xsl:attribute>
                <webproject1:nbjpdareload/>
            </target>
            
            <target name="debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix</xsl:attribute>
            </target>

            <xsl:comment>
            =================
            PROFILING SECTION
            =================
            </xsl:comment>

            <xsl:comment>
                pre NB7.2 profiling section; consider it deprecated
            </xsl:comment>
            <target name="-profile-pre72">
                <xsl:attribute name="description">Profile a J2EE project in the IDE.</xsl:attribute>
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <condition>
                    <xsl:attribute name="property">profiler.startserver.target</xsl:attribute>
                    <xsl:attribute name="value">start-profiled-server-extraargs</xsl:attribute>
                    <xsl:attribute name="else">start-profiled-server</xsl:attribute>
                    <isset>
                        <xsl:attribute name="property">profiler.info.jvmargs.extra</xsl:attribute>
                    </isset>
                </condition>
                <antcall>
                    <xsl:attribute name="target">${profiler.startserver.target}</xsl:attribute>
                </antcall>
                <antcall>
                    <xsl:attribute name="target">run</xsl:attribute>
                </antcall>
                <antcall>
                    <xsl:attribute name="target">-profile-start-loadgen</xsl:attribute>
                </antcall>
            </target>

            <target name="start-profiled-server" if="profiler.info.jvmargs.agent">
                <nbstartprofiledserver>
                    <xsl:attribute name="forceRestart">${profiler.j2ee.serverForceRestart}</xsl:attribute>
                    <xsl:attribute name="startupTimeout">${profiler.j2ee.serverStartupTimeout}</xsl:attribute>
                    <xsl:attribute name="javaPlatform">${profiler.info.javaPlatform}</xsl:attribute>
                    <jvmarg>
                        <xsl:attribute name="value">${profiler.info.jvmargs.agent}</xsl:attribute>
                    </jvmarg>
                    <jvmarg>
                        <xsl:attribute name="value">${profiler.j2ee.agentID}</xsl:attribute>
                    </jvmarg>
                </nbstartprofiledserver>
            </target>

            <target name="start-profiled-server-extraargs" if="profiler.info.jvmargs.agent">
                <nbstartprofiledserver>
                    <xsl:attribute name="forceRestart">${profiler.j2ee.serverForceRestart}</xsl:attribute>
                    <xsl:attribute name="startupTimeout">${profiler.j2ee.serverStartupTimeout}</xsl:attribute>
                    <xsl:attribute name="javaPlatform">${profiler.info.javaPlatform}</xsl:attribute>
                    <jvmarg>
                        <xsl:attribute name="value">${profiler.info.jvmargs.extra}</xsl:attribute>
                    </jvmarg>
                    <jvmarg>
                        <xsl:attribute name="value">${profiler.info.jvmargs.agent}</xsl:attribute>
                    </jvmarg>
                    <jvmarg>
                        <xsl:attribute name="value">${profiler.j2ee.agentID}</xsl:attribute>
                    </jvmarg>
                </nbstartprofiledserver>
            </target>
            
            <target name="-profile-test-single-pre72">
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <xsl:attribute name="depends">profile-init,compile-test-single</xsl:attribute>
                <fail unless="netbeans.home">This target only works when run from inside the NetBeans IDE.</fail>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.test.classpath}}"/>
                        <path path="${{j2ee.platform.classpath}}"/>
                    </classpath>
                </nbprofiledirect>

                <junit showoutput="true" fork="true" dir="${{profiler.info.dir}}"  jvm="${{profiler.info.jvm}}" failureproperty="tests.failed" errorproperty="tests.failed">
                    <env key="${{profiler.info.pathvar}}" path="${{profiler.info.agentpath}}:${{profiler.current.path}}"/>
                    <jvmarg value="${{profiler.info.jvmargs.agent}}" />
                    <jvmarg line="${{profiler.info.jvmargs}}"/>
                    <test name="${{profile.class}}"/>
                    <classpath>
                        <path path="${{run.test.classpath}}"/>
                        <path path="${{j2ee.platform.classpath}}"/>
                    </classpath>
                    <syspropertyset>
                        <propertyref prefix="test-sys-prop."/>
                        <mapper type="glob" from="test-sys-prop.*" to="*"/>
                    </syspropertyset>
                    <formatter type="brief" usefile="false"/>
                    <formatter type="xml"/>
                </junit>
            </target>

            <target name="-profile-check" if="netbeans.home">
                <condition property="profiler.configured">
                    <or>
                        <contains string="${{run.jvmargs.ide}}" substring="-agentpath:" casesensitive="true"/>
                        <contains string="${{run.jvmargs.ide}}" substring="-javaagent:" casesensitive="true"/>
                    </or>
                </condition>
            </target>
            
            <target name="-do-profile" depends="init,-init-cos,compile,compile-jsps,-do-compile-single-jsp,-pre-dist,-do-tmp-dist-with-manifest,-do-tmp-dist-without-manifest">
                <startprofiler/>
                <nbstartserver profilemode="true"/>
                
                <nbdeploy profilemode="true" clientUrlPart="${{client.urlPart}}" forceRedeploy="true" />
                <antcall>
                    <xsl:attribute name="target">debug-display-browser-old</xsl:attribute>
                </antcall>
                <antcall>
                    <xsl:attribute name="target">debug-display-browser</xsl:attribute>
                </antcall>
                <antcall>
                    <xsl:attribute name="target">-profile-start-loadgen</xsl:attribute>
                </antcall>
            </target>

            <target name="profile" depends="-profile-check,-profile-pre72" if="profiler.configured" unless="profiler.info.jvmargs.agent">
                <xsl:attribute name="description">Profile a J2EE project in the IDE.</xsl:attribute>
                
                <antcall>
                    <xsl:attribute name="target">-do-profile</xsl:attribute>
                </antcall>
            </target>
            
            <target name="profile-test-single" depends="-profile-test-single-pre72"/>
            
            <target name="profile-test" depends="-profile-check" if="profiler.configured" unless="profiler.info.jvmargs.agent">
                <startprofiler/>
                <antcall target="test-single"/>
            </target>

            <target name="-profile-start-loadgen" if="profiler.loadgen.path">
                <loadgenstart>
                    <xsl:attribute name="path">${profiler.loadgen.path}</xsl:attribute>
                </loadgenstart>
            </target>

            <xsl:comment>
                JAVADOC SECTION
            </xsl:comment>
            
            <target name="javadoc-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <mkdir dir="${{dist.javadoc.dir}}"/>
                <!-- XXX do an up-to-date check first -->
                <javadoc>
                    <xsl:attribute name="destdir">${dist.javadoc.dir}</xsl:attribute>
                    <xsl:attribute name="source">${javac.source}</xsl:attribute>
                    <xsl:attribute name="notree">${javadoc.notree}</xsl:attribute>
                    <xsl:attribute name="use">${javadoc.use}</xsl:attribute>
                    <xsl:attribute name="nonavbar">${javadoc.nonavbar}</xsl:attribute>
                    <xsl:attribute name="noindex">${javadoc.noindex}</xsl:attribute>
                    <xsl:attribute name="splitindex">${javadoc.splitindex}</xsl:attribute>
                    <xsl:attribute name="author">${javadoc.author}</xsl:attribute>
                    <xsl:attribute name="version">${javadoc.version}</xsl:attribute>
                    <xsl:attribute name="windowtitle">${javadoc.windowtitle}</xsl:attribute>
                    <xsl:attribute name="private">${javadoc.private}</xsl:attribute>
                    <xsl:attribute name="additionalparam">${javadoc.additionalparam}</xsl:attribute>
                    <xsl:attribute name="failonerror">true</xsl:attribute> <!-- #47325 -->
                    <xsl:attribute name="useexternalfile">true</xsl:attribute> <!-- #57375, requires Ant >=1.6.5 -->
                    <xsl:attribute name="encoding">${javadoc.encoding.used}</xsl:attribute>
                    <xsl:attribute name="docencoding">UTF-8</xsl:attribute>
                    <xsl:attribute name="charset">UTF-8</xsl:attribute>
                    <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                        <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    </xsl:if>
                    <classpath>
                        <path path="${{javac.classpath}}:${{j2ee.platform.classpath}}"/>
                    </classpath>
                    <!-- Does not work with includes/excludes:
                    <sourcepath>
                        <xsl:call-template name="createPathElements">
                            <xsl:with-param name="locations" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                        </xsl:call-template>
                    </sourcepath>
                    <xsl:call-template name="createPackagesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                    </xsl:call-template>
                    -->
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                        <xsl:with-param name="includes2">**/*.java</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/*.java"/>
                    </fileset>
                </javadoc>
                <copy todir="${{dist.javadoc.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:source-roots"/>
                        <xsl:with-param name="includes2">**/doc-files/**</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/doc-files/**"/>
                    </fileset>
                </copy>

            </target>
            
            <target name="javadoc-browse">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">no.javadoc.preview</xsl:attribute>
                <xsl:attribute name="depends">init,javadoc-build</xsl:attribute>
                <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
            </target>
            
            <target name="javadoc">
                <xsl:attribute name="depends">init,javadoc-build,javadoc-browse</xsl:attribute>
                <xsl:attribute name="description">Build Javadoc.</xsl:attribute>
            </target>
            
            <xsl:comment>
                
                TEST COMPILATION SECTION
            </xsl:comment>
            
            <target name="-pre-pre-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <mkdir dir="${{build.test.classes.dir}}"/>
                <property name="j2ee.platform.embeddableejb.classpath" value=""/>
            </target>
            
            <target name="-pre-compile-test">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test</xsl:attribute>
                <xsl:element name="webproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}:${j2ee.platform.classpath}:${j2ee.platform.embeddableejb.classpath}</xsl:attribute>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile-test">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-test">
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test,-do-compile-test,-post-compile-test</xsl:attribute>
            </target>
            
            <target name="-pre-compile-test-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-test-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test-single</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <xsl:element name="webproject2:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}:${j2ee.platform.classpath}:${j2ee.platform.embeddableejb.classpath}</xsl:attribute>
                    <xsl:attribute name="includes">${javac.includes}</xsl:attribute>
                    <xsl:attribute name="excludes"/>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/webproject3:data/webproject3:test-roots"/>
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>
            
            <target name="-post-compile-test-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-test-single">
                <xsl:attribute name="depends">init,compile,-pre-pre-compile-test,-pre-compile-test-single,-do-compile-test-single,-post-compile-test-single</xsl:attribute>
            </target>
            
            <xsl:comment>
                
                TEST EXECUTION SECTION
            </xsl:comment>
            
            <target name="-pre-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.test.results.dir}}"/>
            </target>
            
            <target name="-do-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-pre-test-run</xsl:attribute>
                <webproject2:test testincludes="**/*Test.java" includes="${{includes}}"/>
            </target>
            
            <target name="-post-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-pre-test-run,-do-test-run</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-report">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <!-- TBD
                <junitreport todir="${{build.test.results.dir}}">
                <fileset dir="${{build.test.results.dir}}">
                <include name="TEST-*.xml"/>
                </fileset>
                <report format="noframes" todir="${{build.test.results.dir}}"/>
                </junitreport>
                -->
            </target>
            
            <target name="-test-browse">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <!-- TBD
                <nbbrowse file="${{build.test.results.dir}}/junit-noframes.html"/>
                -->
            </target>
            
            <target name="test">
                <xsl:attribute name="depends">init,compile-test,-pre-test-run,-do-test-run,test-report,-post-test-run,-test-browse</xsl:attribute>
                <xsl:attribute name="description">Run unit tests.</xsl:attribute>
            </target>
            
            <target name="-pre-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.test.results.dir}}"/>
            </target>
            
            <target name="-do-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.includes">Must select some files in the IDE or set test.includes</fail>
                <webproject2:test includes="${{test.includes}}" excludes="" testincludes="${{test.includes}}" />
            </target>
            
            <target name="-post-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-single">
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single,-post-test-run-single</xsl:attribute>
                <xsl:attribute name="description">Run single unit test.</xsl:attribute>
            </target>
            
            <target name="-do-test-run-single-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select some files in the IDE or set test.class</fail>
                <fail unless="test.method">Must select some method in the IDE or set test.method</fail>
                <webproject2:test includes="${{javac.includes}}" excludes="" testincludes="${{test.class}}" testmethods="${{test.method}}"/>
            </target>

            <target name="-post-test-run-single-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single-method</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>

            <target name="test-single-method">
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single-method,-post-test-run-single-method</xsl:attribute>
                <xsl:attribute name="description">Run single unit test.</xsl:attribute>
            </target>

            <xsl:comment>
                
                TEST DEBUGGING SECTION
            </xsl:comment>
            
            <target name="-debug-start-debuggee-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <webproject2:test-debug includes="${{javac.includes}}" excludes="" testincludes="${{javac.includes}}" testClass="${{test.class}}"/>
            </target>

            <target name="-debug-start-debuggee-test-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <fail unless="test.method">Must select some method in the IDE or set test.method</fail>
                <webproject2:test-debug includes="${{javac.includes}}" excludes="" testincludes="${{test.class}}" testmethods="${{test.method}}" testClass="${{test.class}}" testMethod="${{test.method}}"/>
            </target>

            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <webproject1:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>
            
            <target name="debug-test-method">
                <xsl:attribute name="depends">init,compile-test-single,-debug-start-debugger-test,-debug-start-debuggee-test-method</xsl:attribute>
            </target>
            <target name="debug-single-method" depends="debug-test-method" />

            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <webproject1:nbjpdareload dir="${{build.test.classes.dir}}"/>
            </target>
            
            <target name="debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix-test</xsl:attribute>
            </target>

            <xsl:comment>
                
                CLEANUP SECTION
            </xsl:comment>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="targetname" select="'deps-clean'"/>
            </xsl:call-template>
            
            <target name="do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                
                <condition value="${{build.web.dir}}" property="build.dir.to.clean">
                    <isset property="dist.ear.dir"/>
                </condition>
                <property value="${{build.web.dir}}" name="build.dir.to.clean"/>
                
                <delete includeEmptyDirs="true" quiet="true">
                    <fileset dir="${{build.dir.to.clean}}/WEB-INF/lib"/>
                </delete>
                <delete dir="${{build.dir}}"/>
                <available file="${{build.dir.to.clean}}/WEB-INF/lib" type="dir" property="status.clean-failed"/>
                <delete dir="${{dist.dir}}"/>
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
                <!--
                <delete dir="${{build.generated.dir}}"/>
                <delete dir="${{build.web.dir}}"/>
                -->
            </target>                      
            
            <target name="check-clean">
                <xsl:attribute name="depends">do-clean</xsl:attribute>
                <xsl:attribute name="if">status.clean-failed</xsl:attribute>
                <!--
                When undeploy is implemented it should be optional:
                <xsl:attribute name="unless">clean.check.skip</xsl:attribute>
                -->
                <echo message="Warning: unable to delete some files in ${{build.web.dir}}/WEB-INF/lib - they are probably locked by the J2EE server. " />
                <echo level="info" message="To delete all files undeploy the module from Server Registry in Runtime tab and then use Clean again."/>
                <!--
                Here comes the undeploy code when supported by nbdeploy task:
                <nbdeploy undeploy="true" clientUrlPart="${client.urlPart}"/>
                And then another attempt to delete:
                <delete dir="${{build.web.dir}}/WEB-INF/lib"/>
                -->
            </target>
            
            <target name="undeploy-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                
                <nbundeploy failOnError="false" startServer="false"/>
            </target>
            
            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="clean">
                <xsl:attribute name="depends">init,undeploy-clean,deps-clean,do-clean,check-clean,-post-clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>
            
            <target name="clean-ear">
                <xsl:attribute name="depends">clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>
            
        </project>
        
        <!-- TBD items:

        Could pass <propertyset> to run, debug, etc. under Ant 1.6,
        optionally, by doing e.g.

        <propertyset>
        <propertyref prefix="sysprop."/>
        <mapper type="glob" from="sysprop.*" to="*"/>
        </propertyset>

        Now user can add to e.g. project.properties e.g.:
        sysprop.org.netbeans.modules.javahelp=0
        to simulate
        -Dorg.netbeans.modules.javahelp=0

        -->
        
    </xsl:template>
    
    <!--
    operations for building of War file
    xslt templates instead of antcall, see # 115640
    -->
    <xsl:template name="distWithoutCustomManifest">
        <dirname property="dist.jar.dir" file="${{dist.war}}"/>
        <mkdir dir="${{dist.jar.dir}}"/>
        <jar jarfile="${{dist.war}}" compress="${{jar.compress}}">
            <fileset dir="${{build.web.dir}}" excludes="WEB-INF/classes/.netbeans_*,${{dist.archive.excludes}}"/>
        </jar>
    </xsl:template>
    <xsl:template name="distWithCustomManifest">
        <dirname property="dist.jar.dir" file="${{dist.war}}"/>
        <mkdir dir="${{dist.jar.dir}}"/>
        <jar manifest="${{build.meta.inf.dir}}/MANIFEST.MF" jarfile="${{dist.war}}" compress="${{jar.compress}}">
            <fileset dir="${{build.web.dir}}" excludes="WEB-INF/classes/.netbeans_*,${{dist.archive.excludes}}"/>
        </jar>
    </xsl:template>
    
    
    <!---
    Generic template to build subdependencies of a certain type.
    Feel free to copy into other modules.
    @param targetname required name of target to generate
    @param type artifact-type from project.xml to filter on; optional, if not specified, uses
    all references, and looks for clean targets rather than build targets
    @return an Ant target which builds (or cleans) all known subprojects
    -->
    <xsl:template name="deps.target">
        <xsl:param name="targetname"/>
        <xsl:param name="type"/>
        <xsl:param name="ear"/>
        <target name="{$targetname}">
            <xsl:attribute name="depends">init</xsl:attribute>
            
            <xsl:choose>
                <xsl:when test="$type">
                    <xsl:choose>
                        <xsl:when test="$ear">
                            <xsl:attribute name="if">dist.ear.dir</xsl:attribute>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="if">no.dist.ear.dir</xsl:attribute>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:when>
            </xsl:choose>
            <xsl:attribute name="unless">no.deps</xsl:attribute>
            
            <xsl:variable name="references2" select="/p:project/p:configuration/projdeps2:references"/>
            <xsl:for-each select="$references2/projdeps2:reference[not($type) or projdeps2:artifact-type = $type]">
                <xsl:variable name="subproj" select="projdeps2:foreign-project"/>
                <xsl:variable name="subtarget">
                    <xsl:choose>
                        <xsl:when test="$type">
                            <xsl:value-of select="projdeps2:target"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="projdeps2:clean-target"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="script" select="projdeps2:script"/>
                <!-- Distinguish build of a dependent project as standalone module or as a part of an ear -->
                <xsl:choose>
                    <xsl:when test="$ear">
                        <xsl:choose>
                            <!-- call standart target if the artifact type is jar (java libraries) -->
                            <xsl:when test="$subtarget = 'jar'">
                                <xsl:choose>
                                    <xsl:when test="projdeps2:properties">
                                        <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                                            <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                                <property name="{@name}" value="{.}"/>
                                            </xsl:for-each>
                                            <property name="deploy.on.save" value="false"/>
                                        </ant>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                                            <property name="deploy.on.save" value="false"/>
                                        </ant>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="projdeps2:properties">
                                        <ant target="dist-ear" inheritall="false" antfile="{$script}">
                                            <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                                <property name="{@name}" value="{.}"/>
                                            </xsl:for-each>
                                            <property name="dist.ear.dir" location="${{build.dir}}"/>
                                        </ant>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <ant target="dist-ear" inheritall="false" antfile="{$script}">
                                            <property name="dist.ear.dir" location="${{build.dir}}"/>
                                        </ant>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="projdeps2:properties">
                                <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                                    <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                        <property name="{@name}" value="{.}"/>
                                    </xsl:for-each>
                                    <xsl:choose>
                                        <xsl:when test="$subtarget = 'jar'">
                                            <property name="deploy.on.save" value="false"/>
                                        </xsl:when>
                                    </xsl:choose>
                                </ant>
                            </xsl:when>
                            <xsl:otherwise>
                                <ant target="{$subtarget}" inheritall="false" antfile="{$script}">
                                    <xsl:choose>
                                        <xsl:when test="$subtarget = 'jar'">
                                            <property name="deploy.on.save" value="false"/>
                                        </xsl:when>
                                    </xsl:choose>
                                </ant>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
            
            <xsl:variable name="references" select="/p:project/p:configuration/projdeps:references"/>
            <xsl:for-each select="$references/projdeps:reference[not($type) or projdeps:artifact-type = $type]">
                <xsl:variable name="subproj" select="projdeps:foreign-project"/>
                <xsl:variable name="subtarget">
                    <xsl:choose>
                        <xsl:when test="$type">
                            <xsl:value-of select="projdeps:target"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="projdeps:clean-target"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="script" select="projdeps:script"/>
                <!-- Distinguish build of a dependent project as standalone module or as a part of an ear -->
                <xsl:choose>
                    <xsl:when test="$ear">
                        <xsl:choose>
                            <!-- call standart target if the artifact type is jar (java libraries) -->
                            <xsl:when test="$subtarget = 'jar'">
                                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">
                                    <property name="deploy.on.save" value="false"/>
                                </ant>
                            </xsl:when>
                            <xsl:otherwise>
                                <ant target="dist-ear" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">
                                    <property name="dist.ear.dir" location="${{build.dir}}"/>
                                </ant>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:choose>
                            <xsl:when test="$subtarget = 'jar'">
                                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}">
                                    <property name="deploy.on.save" value="false"/>
                                </ant>
                            </xsl:when>
                            <xsl:otherwise>
                                <ant target="{$subtarget}" inheritall="false" antfile="${{project.{$subproj}}}/{$script}"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>
                
            </xsl:for-each>
        </target>
    </xsl:template>
    
    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/webproject3:root">
                    <xsl:element name="available">
                        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/webproject3:root">
            <xsl:element name="fail">
                <xsl:attribute name="unless"><xsl:value-of select="@id"/></xsl:attribute>
                <xsl:text>Must set </xsl:text><xsl:value-of select="@id"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createFilesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/webproject3:root">
            <xsl:element name="fileset">
                <xsl:attribute name="dir"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                <xsl:attribute name="includes"><xsl:value-of select="$includes"/></xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes"><xsl:value-of select="$excludes"/>,${excludes}</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="$includes2">
                    <filename name="{$includes2}"/>
                </xsl:if>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPackagesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/webproject3:root">
            <xsl:element name="packageset">
                <xsl:attribute name="dir"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                <xsl:attribute name="includes"><xsl:value-of select="$includes"/></xsl:attribute>
                <xsl:choose>
                    <xsl:when test="$excludes">
                        <xsl:attribute name="excludes"><xsl:value-of select="$excludes"/>,${excludes}</xsl:attribute>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:attribute name="excludes">${excludes}</xsl:attribute>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>        
    
    <xsl:template name="createPathElements">
        <xsl:param name="locations"/>
        <xsl:for-each select="$locations/webproject3:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/webproject3:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>
    </xsl:template>
    
</xsl:stylesheet>
