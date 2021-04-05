<?xml version="1.0" encoding="UTF-8"?>
<!--

    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

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
                xmlns:j2seproject1="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:j2seproject2="http://www.netbeans.org/ns/j2se-project/2"
                xmlns:j2seproject3="http://www.netbeans.org/ns/j2se-project/3"
                xmlns:jaxrpc="http://www.netbeans.org/ns/j2se-project/jax-rpc"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                xmlns:if="ant:if"
                xmlns:unless="ant:unless"
                exclude-result-prefixes="xalan p projdeps projdeps2 j2seproject2 libs">
    <!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and debuggerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">
        
        <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:

  - initialization
  - compilation
  - jar
  - execution
  - debugging
  - javadoc
  - test compilation
  - test execution
  - test debugging
  - applet
  - cleanup

        ]]></xsl:comment>
        
        <xsl:variable name="name" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <fail message="Please build using Ant 1.8.0 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.8.0"/>
                    </not>
                </condition>
            </fail>

            <target name="default">
                <xsl:attribute name="depends">test,jar,javadoc</xsl:attribute>
                <xsl:attribute name="description">Build and test whole project.</xsl:attribute>
            </target>
            
            <xsl:comment> 
                ======================
                INITIALIZATION SECTION 
                ======================
            </xsl:comment>
            
            <target name="-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-init-private">
                <xsl:attribute name="depends">-pre-init</xsl:attribute>
                <property file="nbproject/private/config.properties"/>
                <property file="nbproject/private/configs/${{config}}.properties"/>
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
                <property name="default.javac.source" value="1.6"/>
                <property name="default.javac.target" value="1.6"/>
            </target>
            
            <target name="-init-project">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user</xsl:attribute>
                <property file="nbproject/configs/${{config}}.properties"/>
                <property file="nbproject/project.properties"/>
            </target>

            <target name="-init-modules-supported">
                <condition property="modules.supported.internal" value="true">
                    <not>
                        <matches string="${{javac.source}}" pattern="1\.[0-8](\..*)?"/>
                    </not>
                </condition>
            </target>
            <target name="-init-macrodef-modulename" depends="-init-modules-supported" if="modules.supported.internal">
                <macrodef name="modulename" uri="http://www.netbeans.org/ns/j2se-project/3">
                    <attribute name="property"/>
                    <attribute name="sourcepath"/>
                    <sequential>
                        <loadresource property="@{{property}}" quiet="true">
                            <javaresource classpath="@{{sourcepath}}" name="module-info.java" parentFirst="false"/>
                            <filterchain>
                                <stripjavacomments/>
                                <linecontainsregexp>
                                    <regexp pattern="module .* \{{"/>
                                </linecontainsregexp>
                                <tokenfilter>
                                    <linetokenizer/>
                                    <replaceregex flags="s" pattern="(\s*module\s+)(\S*)(\s*\{{.*)" replace="\2"/>
                                </tokenfilter>
                                <striplinebreaks/>
                            </filterchain>
                        </loadresource>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-source-module-properties" depends="-init-modules-supported,-init-macrodef-modulename" if="modules.supported.internal">
                <fail message="Java 9 support requires Ant 1.10.0 or higher.">
                    <condition>
                        <not>
                            <antversion atleast="1.10.0"/>
                        </not>
                    </condition>
                </fail>
                <j2seproject3:modulename property="module.name">
                    <xsl:attribute name="sourcepath">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                    </xsl:attribute>
                </j2seproject3:modulename>
                <condition property="named.module.internal">
                    <and>
                        <isset property="module.name"/>
                        <length length="0" string="${{module.name}}" when="greater"/>
                    </and>
                </condition>
                <condition property="unnamed.module.internal">
                    <not>
                        <isset property="named.module.internal"/>
                    </not>
                </condition>
                <property name="javac.modulepath" value=""/>
                <property name="run.modulepath" value="${{javac.modulepath}}"/>
                <property name="module.build.classes.dir" value="${{build.classes.dir}}"/>
                <property name="debug.modulepath" value="${{run.modulepath}}"/>
                <property name="javac.upgrademodulepath" value=""/>
                <property name="run.upgrademodulepath" value="${{javac.upgrademodulepath}}"/>
                <condition property="javac.systemmodulepath.cmd.line.arg" value="--system '${{javac.systemmodulepath}}'" else="">
                    <and>
                        <isset property="javac.systemmodulepath"/>
                        <length string="${{javac.systemmodulepath}}" when="greater" length="0"/>
                    </and>
                </condition>
                <property name="dist.jlink.dir" value="${{dist.dir}}/jlink"/>
                <property name="dist.jlink.output" value="${{dist.jlink.dir}}/${{application.title}}"/>
                <property name="module.name" value=""/>
            </target>
            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-init-macrodef-property,-init-modules-supported</xsl:attribute>

                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                        <j2seproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                        <j2seproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                        <j2seproject1:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                        <j2seproject1:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                        <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                            <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                        </condition>
                        <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                        <j2seproject1:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                        <condition property="platform.java" value="${{platform.home}}/bin/java">
                            <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                        </condition>
                        <property name="platform.java" value="${{platform.java.tmp}}"/>
                        <j2seproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
                        <condition property="platform.javadoc" value="${{platform.home}}/bin/javadoc">
                            <equals arg1="${{platform.javadoc.tmp}}" arg2="$${{platforms.${{platform.active}}.javadoc}}"/>
                        </condition>
                        <property name="platform.javadoc" value="${{platform.javadoc.tmp}}"/>
                        <condition property="platform.invalid" value="true">
                            <or>
                                <contains string="${{platform.javac}}" substring="$${{platforms."/>
                                <contains string="${{platform.java}}" substring="$${{platforms."/>
                                <contains string="${{platform.javadoc}}" substring="$${{platforms."/>
                            </or>
                        </condition>
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
                    </xsl:when>
                    <xsl:otherwise>
                        <property name="platform.java" value="${{java.home}}/bin/java"/>
                    </xsl:otherwise>
                </xsl:choose>
                <available file="${{manifest.file}}" property="manifest.available"/>
                <condition property="splashscreen.available">
                    <and>
                        <not>
                            <equals arg1="${{application.splash}}" arg2="" trim="true"/>
                        </not>
                        <available file="${{application.splash}}"/>
                    </and>
                </condition>                
                <condition property="main.class.available">
                    <and>
                        <isset property="main.class"/>
                        <not>
                            <equals arg1="${{main.class}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
                <condition property="profile.available">
                    <and>
                        <isset property="javac.profile"/>
                        <length length="0" string="${{javac.profile}}" when="greater"/>
                        <not>
                            <matches string="${{javac.source}}" pattern="1\.[0-7](\..*)?"/>
                        </not>
                    </and>
                </condition>
                <condition property="do.archive">
                    <or>
                        <not>
                            <istrue value="${{jar.archive.disabled}}"/>  <!-- Disables archive creation when archiving is overriden by an extension -->
                        </not>
                        <istrue value="${{not.archive.disabled}}" />
                    </or>
                </condition>
                <condition property="do.archive+manifest.available">
                    <and>
                        <isset property="manifest.available"/>
                        <istrue value="${{do.archive}}"/>
                    </and>
                </condition>
                <condition property="do.archive+main.class.available">
                    <and>
                        <isset property="main.class.available"/>
                        <istrue value="${{do.archive}}"/>
                    </and>
                </condition>
                <condition property="do.archive+splashscreen.available">
                    <and>
                        <isset property="splashscreen.available"/>
                        <istrue value="${{do.archive}}"/>
                    </and>
                </condition>
                <condition property="do.archive+profile.available">
                    <and>
                        <isset property="profile.available"/>
                        <istrue value="${{do.archive}}"/>
                    </and>
                </condition>
                                
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="netbeans.home+have.tests">
                    <and>
                        <isset property="netbeans.home"/>
                        <isset property="have.tests"/>
                    </and>
                </condition>
                <condition property="no.javadoc.preview">
                    <and>
                        <isset property="javadoc.preview"/>
                        <isfalse value="${{javadoc.preview}}"/>
                    </and>
                </condition>
                <property name="run.jvmargs" value=""/>
                <property name="run.jvmargs.ide" value=""/>
                <property name="javac.compilerargs" value=""/>
                <property name="work.dir" value="${{basedir}}"/>
                <condition property="no.deps">
                    <and>
                        <istrue value="${{no.dependencies}}"/>
                    </and>
                </condition>
                <property name="javac.debug" value="true"/>
                <property name="javadoc.preview" value="true"/>
                <property name="application.args" value=""/>
                <property name="source.encoding" value="${{file.encoding}}"/>
                <property name="runtime.encoding" value="${{source.encoding}}"/>
                <property name="manifest.encoding" value="${{source.encoding}}"/>
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
                <property name="do.depend" value="false"/>
                <condition property="do.depend.true">
                    <istrue value="${{do.depend}}"/>
                </condition>
                <path id="endorsed.classpath.path" path="${{endorsed.classpath}}"/>
                <condition property="endorsed.classpath.cmd.line.arg" value="-Xbootclasspath/p:'${{toString:endorsed.classpath.path}}'" else="">
                    <and>
                        <isset property="endorsed.classpath"/>
                        <not>
                            <equals arg1="${{endorsed.classpath}}" arg2="" trim="true"/>
                        </not>                
                    </and>
                </condition>
                <condition property="javac.profile.cmd.line.arg" value="-profile ${{javac.profile}}" else="">
                    <isset property="profile.available"/>
                </condition>
                <xsl:if test="not(/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform)">
                    <condition property="jdkBug6558476" else="false"> <!-- Force fork even on default platform http://bugs.sun.com/view_bug.do?bug_id=6558476 on JDK 1.5 and 1.6 on Windows -->
                        <and>
                            <matches string="${{java.specification.version}}" pattern="1\.[56]"/>
                            <not>
                                <os family="unix"/>
                            </not>
                        </and>
                    </condition>
                    <condition property="javac.fork" else="false">
                        <or>
                            <istrue value="${{jdkBug6558476}}"/>
                            <istrue value="${{javac.external.vm}}"/>
                        </or>
                    </condition>
                </xsl:if>
                <property name="jar.index" value="false"/>
                <property name="jar.index.metainf" value="${{jar.index}}"/>
                <property name="copylibs.rebase" value="true"/>
                <available file="${{meta.inf.dir}}/persistence.xml" property="has.persistence.xml"/>
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
                <property name="java.failonerror" value="true"/>
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
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.classes.dir">Must set build.classes.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.classes.dir">Must set build.test.classes.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>
                <fail unless="dist.jar">Must set dist.jar</fail>
            </target>
            
            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
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

            <target name="-init-macrodef-javac-with-module" depends="-init-ap-cmdline-properties,-init-source-module-properties" if="modules.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">upgrademodulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.upgrademodulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">processorpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processorpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">processormodulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processormodulepath}</xsl:attribute>
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
                        <xsl:attribute name="name">sourcepath</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
                        <xsl:attribute name="unless:set">named.module.internal</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">sourcepath</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                        <xsl:attribute name="if:set">named.module.internal</xsl:attribute>
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
                        <condition property="warn.excludes.internal">
                            <and>
                                <isset property="named.module.internal"/>
                                <length string="@{{excludes}}" when="greater" length="0" trim="true"/>
                            </and>
                        </condition>
                        <echo level="warning" message="The javac excludes are not supported in the JDK 9 Named Module." if:set="warn.excludes.internal"/>
                        <property name="empty.dir" location="${{build.dir}}/empty"/><!-- #157692 -->
                        <mkdir dir="${{empty.dir}}"/>
                        <mkdir dir="@{{apgeneratedsrcdir}}"/>
                        <condition property="processormodulepath.set">
                            <resourcecount when="greater" count="0">
                                <path>
                                    <pathelement path="@{{processormodulepath}}"/>
                                </path>
                            </resourcecount>
                        </condition>
                        <javac>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="sourcepath">@{sourcepath}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                    <xsl:attribute name="fork">yes</xsl:attribute>
                                    <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="fork">${javac.fork}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:attribute name="includeantruntime">false</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <modulepath>
                                <path path="@{{modulepath}}"/>
                            </modulepath>
                            <upgrademodulepath>
                                <path path="@{{upgrademodulepath}}"/>
                            </upgrademodulepath>
                            <compilerarg line="${{javac.systemmodulepath.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.profile.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <compilerarg value="--processor-module-path" if:set="processormodulepath.set"/>
                            <compilerarg path="@{{processormodulepath}}" if:set="processormodulepath.set"/>
                            <compilerarg value="-processorpath" unless:set="processormodulepath.set"/>
                            <compilerarg path="@{{processorpath}}:${{empty.dir}}" unless:set="processormodulepath.set"/>
                            <compilerarg line="${{ap.processors.internal}}" />
                            <compilerarg line="${{annotation.processing.processor.options}}" />
                            <compilerarg value="-s" />
                            <compilerarg path="@{{apgeneratedsrcdir}}" />
                            <compilerarg line="${{ap.proc.none.internal}}" />
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-macrodef-javac-with-processors" depends="-init-ap-cmdline-properties,-init-source-module-properties" if="ap.supported.internal" unless="modules.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">upgrademodulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.upgrademodulepath}</xsl:attribute>
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
                        <xsl:attribute name="name">sourcepath</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
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
                            <xsl:attribute name="sourcepath">@{sourcepath}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                    <xsl:attribute name="fork">yes</xsl:attribute>
                                    <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="fork">${javac.fork}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                                </xsl:otherwise>
                            </xsl:choose>
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
                            <compilerarg line="${{javac.profile.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <compilerarg value="-processorpath" />
                            <compilerarg path="@{{processorpath}}:${{empty.dir}}" />
                            <compilerarg line="${{ap.processors.internal}}" />
                            <compilerarg line="${{annotation.processing.processor.options}}" />
                            <compilerarg value="-s" />
                            <compilerarg path="@{{apgeneratedsrcdir}}" />
                            <compilerarg line="${{ap.proc.none.internal}}" />
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-macrodef-javac-without-processors" depends="-init-ap-cmdline-properties,-init-source-module-properties" unless="ap.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">upgrademodulepath</xsl:attribute>
                        <xsl:attribute name="default">${javac.upgrademodulepath}</xsl:attribute>
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
                        <xsl:attribute name="name">sourcepath</xsl:attribute>
                        <xsl:attribute name="default">${empty.dir}</xsl:attribute>
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
                            <xsl:attribute name="sourcepath">@{sourcepath}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform/@explicit-source-supported ='false')">                            
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>                            
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                    <xsl:attribute name="fork">yes</xsl:attribute>
                                    <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:attribute name="fork">${javac.fork}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute> <!-- XXX cf. #51482, Ant #29391 -->
                                </xsl:otherwise>
                            </xsl:choose>
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
                            <compilerarg line="${{javac.profile.cmd.line.arg}}"/>
                            <compilerarg line="${{javac.compilerargs}}"/>
                            <customize/>
                        </javac>
                    </sequential>
                </macrodef>
            </target>
            <target name="-init-macrodef-javac" depends="-init-macrodef-javac-with-module,-init-macrodef-javac-with-processors,-init-macrodef-javac-without-processors">
                <macrodef> <!-- #36033, #85707 -->
                    <xsl:attribute name="name">depend</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.classpath}</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <delete>
                            <fileset file="${{javac.includesfile.binary}}"/>  <!-- deleteonexit keeps the file during IDE run -->
                        </delete>
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

            <target name="-init-macrodef-junit-prototype-with-module" depends="-init-modules-supported" if="modules.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">junit-prototype</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customizePrototype</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="junit.forkmode" value="perTest"/>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="forkmode">${junit.forkmode}</xsl:attribute>
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${build.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <classpath>
                                <path path="${{run.test.classpath}}"/>
                            </classpath>
                            <modulepath>
                                <path path="${{run.test.modulepath}}"/>
                            </modulepath>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg value="-ea"/>
                            <jvmarg line="${{run.test.jvmargs}}"/>
                            <customizePrototype/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-prototype-without-module" depends="-init-modules-supported" unless="modules.supported.internal">
                <macrodef>
                    <xsl:attribute name="name">junit-prototype</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">includes</xsl:attribute>
                        <xsl:attribute name="default">${includes}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">excludes</xsl:attribute>
                        <xsl:attribute name="default">${excludes}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customizePrototype</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property name="junit.forkmode" value="perTest"/>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="forkmode">${junit.forkmode}</xsl:attribute>
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${build.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <syspropertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper type="glob" from="test-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <classpath>
                                <path path="${{run.test.classpath}}"/>
                            </classpath>
                            <formatter type="brief" usefile="false"/>
                            <formatter type="xml"/>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg value="-ea"/>
                            <customizePrototype/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-single" depends="-init-test-properties,-init-macrodef-junit-prototype-with-module,-init-macrodef-junit-prototype-without-module" if="${{nb.junit.single}}" unless="${{nb.junit.batch}}">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:junit-prototype>
                            <customizePrototype>
                                <test todir="${{build.test.results.dir}}" name="@{{testincludes}}" methods="@{{testmethods}}"/>
                                <customize/>
                            </customizePrototype>
                        </j2seproject3:junit-prototype>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-batch" depends="-init-test-properties,-init-macrodef-junit-prototype-with-module,-init-macrodef-junit-prototype-without-module" if="${{nb.junit.batch}}" unless="${{nb.junit.single}}">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:junit-prototype>
                            <customizePrototype>
                                <batchtest todir="${{build.test.results.dir}}">
                                    <xsl:call-template name="createFilesets">
                                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                                        <xsl:with-param name="includes">@{includes}</xsl:with-param>
                                        <xsl:with-param name="includes2">@{testincludes}</xsl:with-param>
                                        <xsl:with-param name="excludes">@{excludes}</xsl:with-param>
                                    </xsl:call-template>
                                    <fileset dir="${{build.test.classes.dir}}" excludes="@{{excludes}},${{excludes}},${{test.binaryexcludes}}" includes="${{test.binaryincludes}}">
                                        <filename name="${{test.binarytestincludes}}"/>
                                    </fileset>
                                </batchtest>
                                <customize/>
                            </customizePrototype>
                        </j2seproject3:junit-prototype>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit" if="${{junit.available}}" depends="-init-macrodef-junit-init,-init-macrodef-junit-single, -init-macrodef-junit-batch"/>

            <target name="-init-macrodef-testng" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
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
                            <xsl:attribute name="workingDir">${work.dir}</xsl:attribute> <!-- #47474: match <java> --> 
                            <xsl:attribute name="failureProperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="methods">${testng.methods.arg}</xsl:attribute>
                            <xsl:attribute name="outputdir">${build.test.results.dir}</xsl:attribute>
                            <xsl:attribute name="suitename"><xsl:value-of select="$codename"/></xsl:attribute>
                            <xsl:attribute name="testname">TestNG tests</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <xmlfileset dir="${{build.test.classes.dir}}" includes="@{{testincludes}}"/>
                            <propertyset>
                                <propertyref prefix="test-sys-prop."/>
                                <mapper from="test-sys-prop.*" to="*" type="glob"/>
                            </propertyset>
                            <classpath>
                                <path path="${{run.test.classpath}}"/>
                            </classpath>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <customize/>
                        </testng>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-impl">
                <macrodef>
                    <xsl:attribute name="name">test-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:junit includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </j2seproject3:junit>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-testng-impl" depends="-init-macrodef-testng" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:testng includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </j2seproject3:testng>
                    </sequential>
                </macrodef>
            </target>
                        
            <target name="-init-macrodef-test" depends="-init-macrodef-test-impl,-init-macrodef-junit-impl,-init-macrodef-testng-impl">
                <macrodef>
                    <xsl:attribute name="name">test</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:test-impl includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize>
                                <jvmarg line="${{run.jvmargs}}"/>
                                <jvmarg line="${{run.jvmargs.ide}}"/>
                            </customize>
                        </j2seproject3:test-impl>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-debug-impl" depends="-init-macrodef-junit" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <xsl:attribute name="name">customizeDebuggee</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <j2seproject3:junit includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize>
                                <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                                <customizeDebuggee/>
                            </customize>
                        </j2seproject3:junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-debug" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:debug classname="org.testng.TestNG" classpath="${{debug.test.classpath}}">
                            <customizeDebuggee>
                                <customize2/>
                                <jvmarg value="-ea"/>
                                <arg line="${{testng.debug.mode}}"/>
                                <arg line="-d ${{build.test.results.dir}}"/>
                                <arg line="-listener org.testng.reporters.VerboseReporter"/>
                                <arg line="${{testng.cmd.args}}"/>
                            </customizeDebuggee>
                        </j2seproject3:debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-debug-impl" depends="-init-macrodef-testng-debug" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng-debug-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:testng-debug testClass="@{{testClass}}" testMethod="@{{testMethod}}">
                            <customize2/>
                        </j2seproject3:testng-debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-debug-junit" depends="-init-macrodef-junit-debug-impl" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:test-debug-impl includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customizeDebuggee>
                                <jvmarg line="${{run.jvmargs}}"/>
                                <jvmarg line="${{run.jvmargs.ide}}"/>
                            </customizeDebuggee>
                        </j2seproject3:test-debug-impl>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-test-debug-testng" depends="-init-macrodef-testng-debug-impl" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
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
                        <j2seproject3:testng-debug-impl testClass="@{{testClass}}" testMethod="@{{testMethod}}">
                            <customize2>
                                <syspropertyset>
                                    <propertyref prefix="test-sys-prop."/>
                                    <mapper from="test-sys-prop.*" to="*" type="glob"/>
                                </syspropertyset>
                            </customize2>
                        </j2seproject3:testng-debug-impl>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-test-debug" depends="-init-macrodef-test-debug-junit,-init-macrodef-test-debug-testng"/>
            
            <xsl:comment>
                pre NB7.2 profiling section; consider it deprecated
            </xsl:comment>
            <target name="profile-init" depends="-profile-pre-init, init, -profile-post-init, -profile-init-macrodef-profile, -profile-init-check">
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

            <target name="-profile-init-macrodef-profile">
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <macrodef>
                  <xsl:attribute name="name">resolve</xsl:attribute>
                  <attribute>
                      <xsl:attribute name="name">name</xsl:attribute>
                  </attribute>
                  <attribute>
                      <xsl:attribute name="name">value</xsl:attribute>
                  </attribute>
                  <sequential>
                      <property name="@{{name}}" value="${{env.@{{value}}}}"/>
                  </sequential>
                </macrodef>

                <macrodef>
                    <xsl:attribute name="name">profile</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <element>
                      <xsl:attribute name="name">customize</xsl:attribute>
                      <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property environment="env"/>
                        <resolve name="profiler.current.path" value="${{profiler.info.pathvar}}"/>
                        <java fork="true" classname="@{{classname}}" dir="${{profiler.info.dir}}" jvm="${{profiler.info.jvm}}" failonerror="${{java.failonerror}}">
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg value="${{profiler.info.jvmargs.agent}}"/>
                            <jvmarg line="${{profiler.info.jvmargs}}"/>
                            <env key="${{profiler.info.pathvar}}" path="${{profiler.info.agentpath}}:${{profiler.current.path}}"/>
                            <arg line="${{application.args}}"/>
                            <classpath>
                                <path path="${{run.classpath}}"/>
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

            <target name="-profile-init-check">
                <xsl:attribute name="depends">-profile-pre-init, init, -profile-post-init, -profile-init-macrodef-profile</xsl:attribute>
                <xsl:attribute name="if">profiler.info.jvmargs.agent</xsl:attribute>
                <fail unless="profiler.info.jvm">Must set JVM to use for profiling in profiler.info.jvm</fail>
                <fail unless="profiler.info.jvmargs.agent">Must set profiler agent JVM arguments in profiler.info.jvmargs.agent</fail>
            </target>
            <xsl:comment>
                end of pre NB7.2 profiling section
            </xsl:comment>

            <target name="-init-macrodef-nbjpda" depends="-init-debug-args">
                <macrodef>
                    <xsl:attribute name="name">nbjpdastart</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">name</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${debug.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">stopclassname</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdastart transport="${{debug-transport}}" addressproperty="jpda.address" name="@{{name}}" stopclassname="@{{stopclassname}}">
                            <modulepath>
                                <path path="@{{modulepath}}"/>
                            </modulepath>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">modulename</xsl:attribute>
                        <xsl:attribute name="default">${module.name}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${debug.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${debug.classpath}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customizeDebuggee</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <j2seproject1:java modulename="@{{modulename}}" classname="@{{classname}}" modulepath="@{{modulepath}}" classpath="@{{classpath}}">
                            <customize>
                                <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                                <customizeDebuggee/>
                            </customize>
                        </j2seproject1:java>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-java-with-module" if="named.module.internal" depends="-init-source-module-properties">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">modulename</xsl:attribute>
                        <xsl:attribute name="default">${module.name}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${run.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">upgrademodulepath</xsl:attribute>
                        <xsl:attribute name="default">${run.upgrademodulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${run.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">jvm</xsl:attribute>
                        <xsl:attribute name="default">jvm</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" module="@{{modulename}}" classname="@{{classname}}" failonerror="${{java.failonerror}}">
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <modulepath>
                                <pathelement path="@{{modulepath}}"/>
                                <pathelement location="${{module.build.classes.dir}}"/>
                            </modulepath>
                            <upgrademodulepath>
                                <path path="@{{upgrademodulepath}}"/>
                            </upgrademodulepath>
                            <jvmarg value="-Dfile.encoding=${{runtime.encoding}}"/>
                            <redirector inputencoding="${{runtime.encoding}}" outputencoding="${{runtime.encoding}}" errorencoding="${{runtime.encoding}}"/>
                            <jvmarg line="${{run.jvmargs}}"/>
                            <jvmarg line="${{run.jvmargs.ide}}"/>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-java-with-unnamed-module" if="unnamed.module.internal" depends="-init-source-module-properties">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">modulename</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default">${run.modulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">upgrademodulepath</xsl:attribute>
                        <xsl:attribute name="default">${run.upgrademodulepath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${run.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">jvm</xsl:attribute>
                        <xsl:attribute name="default">jvm</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}" failonerror="${{java.failonerror}}">
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <modulepath>
                                <path path="@{{modulepath}}"/>
                            </modulepath>
                            <upgrademodulepath>
                                <path path="@{{upgrademodulepath}}"/>
                            </upgrademodulepath>
                            <jvmarg value="-Dfile.encoding=${{runtime.encoding}}"/>
                            <redirector inputencoding="${{runtime.encoding}}" outputencoding="${{runtime.encoding}}" errorencoding="${{runtime.encoding}}"/>
                            <jvmarg line="${{run.jvmargs}}"/>
                            <jvmarg line="${{run.jvmargs.ide}}"/>
                            <syspropertyset>
                                <propertyref prefix="run-sys-prop."/>
                                <mapper type="glob" from="run-sys-prop.*" to="*"/>
                            </syspropertyset>
                            <customize/>
                        </java>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-java-without-module" unless="modules.supported.internal" depends="-init-source-module-properties">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">modulename</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classname</xsl:attribute>
                        <xsl:attribute name="default">${main.class}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">modulepath</xsl:attribute>
                        <xsl:attribute name="default"></xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">classpath</xsl:attribute>
                        <xsl:attribute name="default">${run.classpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">jvm</xsl:attribute>
                        <xsl:attribute name="default">jvm</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <java fork="true" classname="@{{classname}}" failonerror="${{java.failonerror}}">
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <xsl:attribute name="jvm">${platform.java}</xsl:attribute>
                            </xsl:if>
                            <jvmarg line="${{endorsed.classpath.cmd.line.arg}}"/>
                            <jvmarg value="-Dfile.encoding=${{runtime.encoding}}"/>
                            <redirector inputencoding="${{runtime.encoding}}" outputencoding="${{runtime.encoding}}" errorencoding="${{runtime.encoding}}"/>
                            <jvmarg line="${{run.jvmargs}}"/>
                            <jvmarg line="${{run.jvmargs.ide}}"/>
                            <classpath>
                                <path path="@{{classpath}}"/>
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

            <target name="-init-macrodef-java" depends="-init-macrodef-java-with-module, -init-macrodef-java-with-unnamed-module, -init-macrodef-java-without-module"/>

            <target name="-init-macrodef-copylibs">
                <macrodef>
                    <xsl:attribute name="name">copylibs</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/3</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">manifest</xsl:attribute>
                        <xsl:attribute name="default">${manifest.file}</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <property location="${{build.classes.dir}}" name="build.classes.dir.resolved"/>
                        <pathconvert property="run.classpath.without.build.classes.dir">
                            <path path="${{run.classpath}}"/>
                            <map from="${{build.classes.dir.resolved}}" to=""/>
                        </pathconvert>
                        <pathconvert pathsep=" " property="jar.classpath">
                            <path path="${{run.classpath.without.build.classes.dir}}"/>
                            <chainedmapper>
                                <flattenmapper/>
                                <filtermapper>
                                    <replacestring from=" " to="%20"/>
                                </filtermapper>
                                <globmapper from="*" to="lib/*"/>
                            </chainedmapper>
                        </pathconvert>
                        <taskdef classname="org.netbeans.modules.java.j2seproject.copylibstask.CopyLibs" classpath="${{libs.CopyLibs.classpath}}" name="copylibs"/>
                        <copylibs rebase="${{copylibs.rebase}}" compress="${{jar.compress}}" jarfile="${{dist.jar}}" manifest="@{{manifest}}" manifestencoding="UTF-8" runtimeclasspath="${{run.classpath.without.build.classes.dir}}" index="${{jar.index}}" indexMetaInf="${{jar.index.metainf}}" excludeFromCopy="${{copylibs.excludes}}">
                            <fileset dir="${{build.classes.dir}}" excludes="${{dist.archive.excludes}}"/>
                            <manifest>
                                <attribute name="Class-Path" value="${{jar.classpath}}"/>
                                <customize/>
                            </manifest>
                        </copylibs>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-presetdef-jar">
                <presetdef>
                    <xsl:attribute name="name">jar</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-project/1</xsl:attribute>
                    <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" index="${{jar.index}}" manifestencoding="UTF-8">
                        <j2seproject1:fileset dir="${{build.classes.dir}}" excludes="${{dist.archive.excludes}}"/>
                        <!-- XXX should have a property serving as the excludes list -->
                    </jar>
                </presetdef>
            </target>

            <target name="-init-ap-cmdline-properties">
                <property name="annotation.processing.enabled" value="true" />
                <property name="annotation.processing.processors.list" value="" />
                <property name="annotation.processing.processor.options" value="" />
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

            <target name="init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac,-init-macrodef-test,-init-macrodef-test-debug,-init-macrodef-nbjpda,-init-macrodef-debug,-init-macrodef-java,-init-presetdef-jar,-init-ap-cmdline</xsl:attribute>
            </target>
            
            <xsl:comment>
                ===================
                COMPILATION SECTION
                ===================
            </xsl:comment>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="kind" select="'jar'"/>
                <xsl:with-param name="type" select="'jar'"/>
            </xsl:call-template>
            
            <xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                <target name="wscompile-init" depends="init">
                    <taskdef name="wscompile" classname="com.sun.xml.rpc.tools.ant.Wscompile"
                             classpath="${{wscompile.classpath}}"/>
                    <taskdef name="wsclientuptodate" classname="org.netbeans.modules.websvc.jaxrpc.ant.WsClientUpToDate"
                             classpath="${{wsclientuptodate.classpath}}"/>
                    
                    <mkdir dir="${{build.classes.dir}}"/>
                    <mkdir dir="${{build.generated.sources.dir}}/jax-rpc"/>
                    
                    <xsl:for-each select="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                        <xsl:variable name="wsclientname">
                            <xsl:value-of select="jaxrpc:web-service-client-name"/>
                        </xsl:variable>
                        
                        <wsclientuptodate property="wscompile.client.{$wsclientname}.notrequired"
                                          sourcewsdl="${{meta.inf.dir}}/wsdl/{$wsclientname}.wsdl"
                                          targetdir="${{build.generated.sources.dir}}/jax-rpc"/>
                    </xsl:for-each>
                </target>
            </xsl:if>
            
            <xsl:for-each select="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                <xsl:variable name="wsclientname">
                    <xsl:value-of select="jaxrpc:web-service-client-name"/>
                </xsl:variable>
                <xsl:variable name="useimport">
                    <xsl:choose>
                        <xsl:when test="jaxrpc:web-service-stub-type">
                            <xsl:value-of select="jaxrpc:web-service-stub-type='jsr-109_client'"/>
                        </xsl:when>
                        <xsl:otherwise>true</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:variable name="useclient">
                    <xsl:choose>
                        <xsl:when test="jaxrpc:web-service-stub-type">
                            <xsl:value-of select="jaxrpc:web-service-stub-type='jaxrpc_static_client'"/>
                        </xsl:when>
                        <xsl:otherwise>false</xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                
                <target name="{$wsclientname}-client-wscompile" depends="wscompile-init" unless="wscompile.client.{$wsclientname}.notrequired">
                    <property name="config_target" location="${{meta.inf.dir}}/wsdl"/>
                    <copy file="${{meta.inf.dir}}/wsdl/{$wsclientname}-config.xml"
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
                        base="${{build.generated.sources.dir}}/jax-rpc"
                        sourceBase="${{build.generated.sources.dir}}/jax-rpc"
                        classpath="${{wscompile.classpath}}:${{javac.classpath}}"
                        mapping="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-mapping.xml"
                        httpproxy="${{wscompile.client.{$wsclientname}.proxy}}"
                        config="${{build.generated.sources.dir}}/jax-rpc/wsdl/{$wsclientname}-config.xml">
                    </wscompile>
                </target>
            </xsl:for-each>
            
            <xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                <target name="web-service-client-generate">
                    <xsl:attribute name="depends">
                        <xsl:for-each select="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">
                            <xsl:if test="position()!=1"><xsl:text>, </xsl:text></xsl:if>
                            <xsl:variable name="wsname2">
                                <xsl:value-of select="jaxrpc:web-service-client-name"/>
                            </xsl:variable>
                            <xsl:value-of select="jaxrpc:web-service-client-name"/><xsl:text>-client-wscompile</xsl:text>
                        </xsl:for-each>
                    </xsl:attribute>
                </target>
            </xsl:if>
            
            <target name="-verify-automatic-build">
                <xsl:attribute name="depends">init,-check-automatic-build,-clean-after-automatic-build</xsl:attribute>
            </target>
            
            <target name="-check-automatic-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <available file="${{build.classes.dir}}/.netbeans_automatic_build" property="netbeans.automatic.build"/>
            </target>
            
            <target name="-clean-after-automatic-build" depends="init" if="netbeans.automatic.build">
                <antcall target="clean">                    
                    <param name="no.dependencies" value="true"/>
                </antcall>
            </target>
            
            <target name="-pre-pre-compile">
                <xsl:attribute name="depends">init,deps-jar<xsl:if test="/p:project/p:configuration/jaxrpc:web-service-clients/jaxrpc:web-service-client">,web-service-client-generate</xsl:if></xsl:attribute>
                <mkdir dir="${{build.classes.dir}}"/>
            </target>
            
            <target name="-pre-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-compile-depend" if="do.depend.true">
                <pathconvert property="build.generated.subdirs">
                    <dirset dir="${{build.generated.sources.dir}}" erroronmissingdir="false">
                        <include name="*"/>
                    </dirset>
                </pathconvert>
                <j2seproject3:depend>
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        </xsl:call-template>
                        <xsl:text>:${build.generated.subdirs}</xsl:text>
                    </xsl:attribute>
                </j2seproject3:depend>
            </target>
            <target name="-do-compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile, -copy-persistence-xml,-compile-depend</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <j2seproject3:javac gensrcdir="${{build.generated.sources.dir}}"/>
                <copy todir="${{build.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        <!-- XXX should perhaps use ${includes} and ${excludes} -->
                        <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    </xsl:call-template>
                </copy>
            </target>

            <target name="-copy-persistence-xml" if="has.persistence.xml"><!-- see eclipselink issue https://bugs.eclipse.org/bugs/show_bug.cgi?id=302450, need to copy persistence.xml before build -->
                <mkdir dir="${{build.classes.dir}}/META-INF"/>
                <copy todir="${{build.classes.dir}}/META-INF">
                    <fileset dir="${{meta.inf.dir}}" includes="persistence.xml orm.xml"/>
                </copy>
            </target>
            
            <target name="-post-compile">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile">
                <xsl:attribute name="depends">init,deps-jar,-verify-automatic-build,-pre-pre-compile,-pre-compile,-do-compile,-post-compile</xsl:attribute>
                <xsl:attribute name="description">Compile project.</xsl:attribute>
            </target>
            
            <target name="-pre-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-do-compile-single">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <j2seproject3:force-recompile/>
                <xsl:element name="j2seproject3:javac">
                    <xsl:attribute name="includes">${javac.includes}, module-info.java</xsl:attribute>
                    <xsl:attribute name="excludes"/>
                    <xsl:attribute name="sourcepath"> <!-- #115918 -->
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="gensrcdir">${build.generated.sources.dir}</xsl:attribute>
                </xsl:element>
            </target>
            
            <target name="-post-compile-single">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="compile-single">
                <xsl:attribute name="depends">init,deps-jar,-verify-automatic-build,-pre-pre-compile,-pre-compile-single,-do-compile-single,-post-compile-single</xsl:attribute>
            </target>
            
            <xsl:comment>
                ====================
                JAR BUILDING SECTION
                ====================
            </xsl:comment>
            
            <target name="-pre-pre-jar">
                <xsl:attribute name="depends">init</xsl:attribute>
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
            </target>
            
            <target name="-pre-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name ="-check-module-main-class" depends="init,compile">
                <pathconvert property="main.class.file">
                    <string value="${{main.class}}"/>
                    <unpackagemapper from="*" to="*.class"/>
                </pathconvert>
                <condition property="do.module.main.class">
                    <and>
                        <isset property="main.class.available"/>
                        <available file="${{build.classes.dir}}/module-info.class"/>
                        <available file="${{build.classes.dir}}/${{main.class.file}}"/>
                        <isset property="libs.CopyLibs.classpath"/>
                        <available classpath="${{libs.CopyLibs.classpath}}" classname="org.netbeans.modules.java.j2seproject.moduletask.ModuleMainClass"/>
                    </and>
                </condition>
            </target>
            <target name="-set-module-main-class" depends="-check-module-main-class" if="do.module.main.class">
                <taskdef classname="org.netbeans.modules.java.j2seproject.moduletask.ModuleMainClass" classpath="${{libs.CopyLibs.classpath}}" name="modulemainclass"/>
                <modulemainclass mainclass="${{main.class}}" moduleinfo="${{build.classes.dir}}/module-info.class" failonerror="false"/>
            </target>

            <target name="-do-jar-create-manifest">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <xsl:attribute name="unless">manifest.available</xsl:attribute>
                <tempfile destdir="${{build.dir}}" deleteonexit="true" property="tmp.manifest.file"/>
                <touch file="${{tmp.manifest.file}}" verbose="false"/>
            </target>

            <target name="-do-jar-copy-manifest">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">do.archive+manifest.available</xsl:attribute>
                <tempfile destdir="${{build.dir}}" deleteonexit="true" property="tmp.manifest.file"/>
                <copy file="${{manifest.file}}" tofile="${{tmp.manifest.file}}" encoding="${{manifest.encoding}}" outputencoding="UTF-8"/>
            </target>

            <target name="-do-jar-set-mainclass">
                <xsl:attribute name="depends">init,-do-jar-create-manifest,-do-jar-copy-manifest</xsl:attribute>
                <xsl:attribute name="if">do.archive+main.class.available</xsl:attribute>
                <manifest file="${{tmp.manifest.file}}" mode="update" encoding="UTF-8">
                    <attribute name="Main-Class" value="${{main.class}}"/>
                </manifest>
            </target>

            <target name="-do-jar-set-profile">
                <xsl:attribute name="depends">init,-do-jar-create-manifest,-do-jar-copy-manifest</xsl:attribute>
                <xsl:attribute name="if">do.archive+profile.available</xsl:attribute>
                <manifest file="${{tmp.manifest.file}}" mode="update" encoding="UTF-8">
                    <attribute name="Profile" value="${{javac.profile}}"/>
                </manifest>
            </target>

            <target name="-do-jar-set-splashscreen">
                <xsl:attribute name="depends">init,-do-jar-create-manifest,-do-jar-copy-manifest</xsl:attribute>
                <xsl:attribute name="if">do.archive+splashscreen.available</xsl:attribute>
                <basename file="${{application.splash}}" property="splashscreen.basename"/>
                <mkdir dir="${{build.classes.dir}}/META-INF"/>
                <copy failonerror="false" file="${{application.splash}}" todir="${{build.classes.dir}}/META-INF"/>
                <manifest file="${{tmp.manifest.file}}" mode="update" encoding="UTF-8">
                    <attribute name="SplashScreen-Image" value="META-INF/${{splashscreen.basename}}"/>
                </manifest>
            </target>
            
            <target name="-check-do-mkdist">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <condition property="do.mkdist">
                    <and>
                        <isset property="do.archive"/>
                        <isset property="libs.CopyLibs.classpath"/>
                        <not>
                            <istrue value="${{mkdist.disabled}}"/>
                        </not>
                        <not>
                            <available file="${{build.classes.dir}}/module-info.class"/>
                        </not>
                    </and>
                </condition>
            </target>

            <target name="-do-jar-copylibs">
                <xsl:attribute name="depends">init,-init-macrodef-copylibs,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-set-mainclass,-do-jar-set-profile,-do-jar-set-splashscreen,-check-do-mkdist</xsl:attribute>
                <xsl:attribute name="if">do.mkdist</xsl:attribute>
                <j2seproject3:copylibs manifest="${{tmp.manifest.file}}"/>
                <echo level="info">To run this application from the command line without Ant, try:</echo>
                <property name="dist.jar.resolved" location="${{dist.jar}}"/>
                <echo level="info"><xsl:choose>
                        <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">${platform.java}</xsl:when>
                        <xsl:otherwise>java</xsl:otherwise>
                </xsl:choose> -jar "${dist.jar.resolved}"</echo>
            </target>

            <target name="-do-jar-jar">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-set-mainclass,-do-jar-set-profile,-do-jar-set-splashscreen,-check-do-mkdist</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <xsl:attribute name="unless">do.mkdist</xsl:attribute>
                <j2seproject1:jar manifest="${{tmp.manifest.file}}"/>
                <property location="${{build.classes.dir}}" name="build.classes.dir.resolved"/>
                <property location="${{dist.jar}}" name="dist.jar.resolved"/>
                <condition property="jar.usage.message.class.path.replacement" value="" else="${{dist.jar.resolved}}">
                    <isset property="named.module.internal"/>
                </condition>
                <pathconvert property="run.classpath.with.dist.jar">
                    <path path="${{run.classpath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to="${{jar.usage.message.class.path.replacement}}"/>
                </pathconvert>
                <pathconvert property="run.modulepath.with.dist.jar">
                    <path location="${{dist.jar.resolved}}"/>
                    <path path="${{run.modulepath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to="${{dist.jar.resolved}}"/>
                </pathconvert>
                <condition property="jar.usage.message.run.modulepath.with.dist.jar" value="${{run.modulepath.with.dist.jar}}" else="${{run.modulepath}}">
                    <isset property="named.module.internal"/>
                </condition>
                <condition property="jar.usage.message.module.path" value=" -p ${{jar.usage.message.run.modulepath.with.dist.jar}}" else="">
                    <and>
                        <isset property="modules.supported.internal"/>
                        <length length="0" string="${{jar.usage.message.run.modulepath.with.dist.jar}}" when="greater"/>
                    </and>
                </condition>
                <condition property="jar.usage.message.class.path" value=" -cp ${{run.classpath.with.dist.jar}}" else="">
                    <length length="0" string="${{run.classpath.with.dist.jar}}" when="greater"/>
                </condition>
                <condition property="jar.usage.message.main.class.class.selector" value="" else="/${{main.class}}">
                    <isset property="do.module.main.class"/>
                </condition>
                <condition property="jar.usage.message.main.class" value=" -m ${{module.name}}${{jar.usage.message.main.class.class.selector}}" else=" ${{main.class}}">
                    <isset property="named.module.internal"/>
                </condition>
                <condition property="jar.usage.message" else="" value="To run this application from the command line without Ant, try:${{line.separator}}${{platform.java}}${{jar.usage.message.module.path}}${{jar.usage.message.class.path}}${{jar.usage.message.main.class}}">
                    <isset property="main.class.available"/>
                </condition>
                <condition property="jar.usage.level" else="debug" value="info">
                    <isset property="main.class.available"/>
                </condition>
                <echo level="${{jar.usage.level}}" message="${{jar.usage.message}}"/>
            </target>

            <target name="-do-jar-delete-manifest" >
                <xsl:attribute name="depends">-do-jar-copylibs</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <delete>
                    <fileset file="${{tmp.manifest.file}}"/>
                </delete>
            </target>


            <target name="-do-jar-without-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-set-mainclass,-do-jar-set-profile,-do-jar-set-splashscreen,-do-jar-jar,-do-jar-delete-manifest</xsl:attribute>
            </target>
            <target name="-do-jar-with-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-create-manifest,-do-jar-copy-manifest,-do-jar-set-mainclass,-do-jar-set-profile,-do-jar-set-splashscreen,-do-jar-copylibs,-do-jar-delete-manifest</xsl:attribute>
            </target>
           
            <target name="-post-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-set-module-main-class,-do-jar-without-libraries,-do-jar-with-libraries,-post-jar</xsl:attribute>
            </target>
            
            <target name="jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-do-jar,-post-jar,deploy</xsl:attribute>
                <xsl:attribute name="description">Build JAR.</xsl:attribute>
            </target>

            <xsl:comment>
                =================
                DEPLOY SECTION
                =================
            </xsl:comment>
            <target name="-pre-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            <target name="-check-jlink" depends="init">
                <condition property="do.jlink.internal">
                    <and>
                        <istrue value="${{do.jlink}}"/>
                        <isset property="do.archive"/>
                        <isset property="named.module.internal"/>
                    </and>
                </condition>
            </target>
            <target name="-do-deploy" depends="init,-do-jar,-post-jar,-pre-deploy,-check-jlink" if="do.jlink.internal">
                <delete dir="${{dist.jlink.dir}}" quiet="true" failonerror="false"/>
                <property name="jlink.launcher.name" value="${{application.title}}"/>
                <condition property="jlink.add.modules" value="${{module.name}},${{jlink.additionalmodules}}" else="${{module.name}}">
                    <and>
                        <isset property="jlink.additionalmodules"/>
                        <length string="${{jlink.additionalmodules}}" when="greater" length="0"/>
                    </and>
                </condition>
                <condition property="jlink.do.strip.internal">
                    <and>
                        <isset property="jlink.strip"/>
                        <istrue value="${{jlink.strip}}"/>
                    </and>
                </condition>
                <condition property="jlink.do.additionalparam.internal">
                    <and>
                        <isset property="jlink.additionalparam"/>
                        <length string="${{jlink.additionalparam}}" when="greater" length="0"/>
                    </and>
                </condition>
                <condition property="jlink.do.launcher.internal">
                    <and>
                        <istrue value="${{jlink.launcher}}"/>
                        <isset property="main.class.available"/>
                    </and>
                </condition>
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                        <property name="platform.jlink" value="${{platform.home}}/bin/jlink"/>
                        <property name="jlink.systemmodules.internal" value="${{platform.home}}/jmods"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <property name="platform.jlink" value="${{jdk.home}}/bin/jlink"/>
                        <property name="jlink.systemmodules.internal" value="${{jdk.home}}/jmods"/>
                    </xsl:otherwise>
                </xsl:choose>
                <exec executable="${{platform.jlink}}">
                    <arg value="--module-path"/>
                    <arg path="${{jlink.systemmodules.internal}}:${{run.modulepath}}:${{dist.jar}}"/>
                    <arg value="--add-modules"/>
                    <arg value="${{jlink.add.modules}}"/>
                    <arg value="--strip-debug" if:set="jlink.do.strip.internal"/>
                    <arg value="--launcher" if:set="jlink.do.launcher.internal"/>
                    <arg value="${{jlink.launcher.name}}=${{module.name}}/${{main.class}}" if:set="jlink.do.launcher.internal"/>
                    <arg line="${{jlink.additionalparam}}" if:set="jlink.do.additionalparam.internal"/>
                    <arg value="--output"/>
                    <arg value="${{dist.jlink.output}}"/>
                </exec>
            </target>
            <target name="-post-deploy">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            <target name="deploy" depends="-do-jar,-post-jar,-pre-deploy,-do-deploy,-post-deploy"/>

            <xsl:comment>
                =================
                EXECUTION SECTION
                =================
            </xsl:comment>
            
            <target name="run">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <xsl:attribute name="description">Run a main class.</xsl:attribute>
                <j2seproject1:java>
                    <customize>
                        <arg line="${{application.args}}"/>
                    </customize>
                </j2seproject1:java>
            </target>
            
            <target name="-do-not-recompile">
                <property name="javac.includes.binary" value=""/> <!-- #116230 hack -->
            </target>
            <target name="run-single">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <j2seproject1:java classname="${{run.class}}"/>
            </target>

            <target name="run-test-with-main">
                <xsl:attribute name="depends">init,compile-test-single</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <j2seproject1:java classname="${{run.class}}" classpath="${{run.test.classpath}}"/>
            </target>

            <xsl:comment>
                =================
                DEBUGGING SECTION
                =================
            </xsl:comment>
            
            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2seproject1:nbjpdastart name="${{debug.class}}"/>
            </target>

            <target name="-debug-start-debugger-main-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2seproject1:nbjpdastart name="${{debug.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="-debug-start-debuggee">
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <j2seproject3:debug>
                    <customizeDebuggee>
                        <arg line="${{application.args}}"/>
                    </customizeDebuggee>
                </j2seproject3:debug>
            </target>
            
            <target name="debug">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger,-debug-start-debuggee</xsl:attribute>
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
            </target>
            
            <target name="-debug-start-debugger-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <j2seproject1:nbjpdastart stopclassname="${{main.class}}"/>
            </target>
            
            <target name="debug-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger-stepinto,-debug-start-debuggee</xsl:attribute>
            </target>
            
            <target name="-debug-start-debuggee-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <j2seproject3:debug classname="${{debug.class}}"/>
            </target>
            
            <target name="debug-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-start-debugger,-debug-start-debuggee-single</xsl:attribute>
            </target>

            <target name="-debug-start-debuggee-main-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <j2seproject3:debug classname="${{debug.class}}" classpath="${{debug.test.classpath}}"/>
            </target>

            <target name="debug-test-with-main">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-debug-start-debugger-main-test,-debug-start-debuggee-main-test</xsl:attribute>
            </target>
            
            <target name="-pre-debug-fix">
                <xsl:attribute name="depends">init</xsl:attribute>
                <fail unless="fix.includes">Must set fix.includes</fail>
                <property name="javac.includes" value="${{fix.includes}}.java"/>
            </target>
            
            <target name="-do-debug-fix">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-single</xsl:attribute>
                <j2seproject1:nbjpdareload/>
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
                pre NB7.2 profiler integration
            </xsl:comment>
            <target depends="profile-init,compile" description="Profile a project in the IDE." if="profiler.info.jvmargs.agent" name="-profile-pre72">
                <fail unless="netbeans.home">This target only works when run from inside the NetBeans IDE.</fail>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.classpath}}"/>
                    </classpath>
                </nbprofiledirect>
                <profile/>
            </target>
            <target depends="profile-init,compile-single" description="Profile a selected class in the IDE." if="profiler.info.jvmargs.agent" name="-profile-single-pre72">
                <fail unless="profile.class">Must select one file in the IDE or set profile.class</fail>
                <fail unless="netbeans.home">This target only works when run from inside the NetBeans IDE.</fail>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.classpath}}"/>
                    </classpath>
                </nbprofiledirect>
                <profile classname="${{profile.class}}"/>
            </target>
            <target depends="profile-init,compile-single" if="profiler.info.jvmargs.agent" name="-profile-applet-pre72">
                <fail unless="netbeans.home">This target only works when run from inside the NetBeans IDE.</fail>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.classpath}}"/>
                    </classpath>
                </nbprofiledirect>
                <profile classname="sun.applet.AppletViewer">
                    <customize>
                        <arg value="${{applet.url}}"/>
                    </customize>
                </profile>
            </target>
            <target depends="-init-macrodef-junit,profile-init,compile-test-single" if="profiler.info.jvmargs.agent" name="-profile-test-single-pre72">
                <fail unless="netbeans.home">This target only works when run from inside the NetBeans IDE.</fail>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.test.classpath}}"/>
                    </classpath>
                </nbprofiledirect>
                <j2seproject3:junit includes="${{includes}}" excludes="${{excludes}}" testincludes="${{profile.class}}" testmethods="">
                    <customize>
                        <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                        <env key="${{profiler.info.pathvar}}" path="${{profiler.info.agentpath}}:${{profiler.current.path}}"/>
                        <jvmarg value="${{profiler.info.jvmargs.agent}}"/>
                        <jvmarg line="${{profiler.info.jvmargs}}"/>
                        <classpath>
                            <path path="${{run.test.classpath}}"/>
                        </classpath>
                    </customize>
                </j2seproject3:junit>
            </target>
            <xsl:comment>
                end of pre NB72 profiling section
            </xsl:comment>
            
            <target name="-profile-check" if="netbeans.home">
                <condition property="profiler.configured">
                    <or>
                        <contains string="${{run.jvmargs.ide}}" substring="-agentpath:" casesensitive="true"/>
                        <contains string="${{run.jvmargs.ide}}" substring="-javaagent:" casesensitive="true"/>
                    </or>
                </condition>
            </target>
            
            <target name="profile" depends="-profile-check,-profile-pre72" description="Profile a project in the IDE." if="profiler.configured" unless="profiler.info.jvmargs.agent">
                <startprofiler/>
                <antcall target="run"/>
            </target>

            <target name="profile-single" depends="-profile-check,-profile-single-pre72" description="Profile a selected class in the IDE." if="profiler.configured" unless="profiler.info.jvmargs.agent">
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <startprofiler/>
                <antcall target="run-single"/>
            </target>

            <target name="profile-test-single" depends="-profile-test-single-pre72" description="Profile a selected test in the IDE."/>

            <target name="profile-test" depends="-profile-check" description="Profile a selected test in the IDE." if="profiler.configured" unless="profiler.info.jvmargs">
                <fail unless="test.includes">Must select some files in the IDE or set test.includes</fail>
                <startprofiler/>
                <antcall target="test-single"/>
            </target>

            <target name="profile-test-with-main" depends="-profile-check" description="Profile a selected class in the IDE." if="profiler.configured">
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <startprofiler/>
                <antcall target="run-test-with-main"/>
            </target>
            
            <target name="profile-applet" depends="-profile-check,-profile-applet-pre72" if="profiler.configured" unless="profiler.info.jvmargs.agent">
                <fail unless="applet.url">Must select one file in the IDE or set applet.url</fail>
                <startprofiler/>
                <antcall target="run-applet"/>
            </target>
            
            <xsl:comment>
                ===============
                JAVADOC SECTION
                ===============
            </xsl:comment>
            
            <target name="-javadoc-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <mkdir dir="${{dist.javadoc.dir}}"/>
                <condition property="javadoc.endorsed.classpath.cmd.line.arg" value="-J${{endorsed.classpath.cmd.line.arg}}" else="">
                    <and>
                        <isset property="endorsed.classpath.cmd.line.arg"/>
                        <not>
                            <equals arg1="${{endorsed.classpath.cmd.line.arg}}" arg2=""/>
                        </not>
                    </and>
                </condition>
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                        <exec failonerror="false" executable="${{platform.java}}" outputproperty="platform.version.output">
                            <arg value="-version"/>
                        </exec>
                        <condition property="bug5101868workaround" value="*.java" else="">
                            <matches pattern="1\.[56](\..*)?" string="${{platform.version.output}}" multiline="true"/>
                        </condition>
                    </xsl:when>
                    <xsl:otherwise>
                        <condition property="bug5101868workaround" value="*.java" else="">
                            <matches pattern="1\.[56](\..*)?" string="${{java.version}}"/>
                        </condition>
                    </xsl:otherwise>
                </xsl:choose>
                <condition property="javadoc.html5.cmd.line.arg" value="-html5" else="">
                    <and>
                        <isset property="javadoc.html5"/>
                        <xsl:choose>
                            <xsl:when test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                                <available file="${{platform.home}}${{file.separator}}lib${{file.separator}}jrt-fs.jar"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <available file="${{jdk.home}}${{file.separator}}lib${{file.separator}}jrt-fs.jar"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </and>
                </condition>
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
                    <xsl:attribute name="additionalparam">-J-Dfile.encoding=${file.encoding} ${javadoc.additionalparam}</xsl:attribute>
                    <xsl:attribute name="failonerror">true</xsl:attribute> <!-- #47325 -->
                    <xsl:attribute name="useexternalfile">true</xsl:attribute> <!-- #57375, requires Ant >=1.6.5 -->
                    <xsl:attribute name="encoding">${javadoc.encoding.used}</xsl:attribute>
                    <xsl:attribute name="docencoding">UTF-8</xsl:attribute>
                    <xsl:attribute name="charset">UTF-8</xsl:attribute>
                    <xsl:if test="/p:project/p:configuration/j2seproject3:data/j2seproject3:explicit-platform">
                        <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    </xsl:if>
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                    <!-- Does not work with includes/excludes:
                    <sourcepath>
                        <xsl:call-template name="createPathElements">
                            <xsl:with-param name="locations" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        </xsl:call-template>
                    </sourcepath>
                    -->
                    <!-- Does not work with includes/excludes either, and duplicates class names in index:
                    <xsl:call-template name="createPackagesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                    </xsl:call-template>
                    -->
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        <xsl:with-param name="excludes">${bug5101868workaround}</xsl:with-param>
                        <xsl:with-param name="includes2">**/*.java</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/*.java"/>
                        <exclude name="*.java"/>
                    </fileset>
                    <arg line="${{javadoc.endorsed.classpath.cmd.line.arg}}"/>
                    <arg line="${{javadoc.html5.cmd.line.arg}}"/>
                </javadoc>
                <copy todir="${{dist.javadoc.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:source-roots"/>
                        <xsl:with-param name="includes2">**/doc-files/**</xsl:with-param>
                    </xsl:call-template>
                    <fileset>
                        <xsl:attribute name="dir">${build.generated.sources.dir}</xsl:attribute>
                        <xsl:attribute name="erroronmissingdir">false</xsl:attribute>
                        <include name="**/doc-files/**"/>
                    </fileset>
                </copy>

            </target>
            
            <target name="-javadoc-browse">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="unless">no.javadoc.preview</xsl:attribute>
                <xsl:attribute name="depends">init,-javadoc-build</xsl:attribute>
                <nbbrowse file="${{dist.javadoc.dir}}/index.html"/>
            </target>
            
            <target name="javadoc">
                <xsl:attribute name="depends">init,-javadoc-build,-javadoc-browse</xsl:attribute>
                <xsl:attribute name="description">Build Javadoc.</xsl:attribute>
            </target>
            
            <xsl:comment>
                =========================
                TEST COMPILATION SECTION
                =========================
            </xsl:comment>
            
            <target name="-pre-pre-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile</xsl:attribute>
                <mkdir dir="${{build.test.classes.dir}}"/>
            </target>
            
            <target name="-pre-compile-test">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-test-javac-module-properties-with-module" depends="-init-source-module-properties" if="named.module.internal">
                <j2seproject3:modulename property="test.module.name">
                    <xsl:attribute name="sourcepath">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                            </xsl:call-template>
                    </xsl:attribute>
                </j2seproject3:modulename>
                <condition>
                    <xsl:attribute name="property">javac.test.sourcepath</xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="else">${empty.dir}</xsl:attribute>
                    <and>
                        <isset property="test.module.name"/>
                        <length length="0" string="${{test.module.name}}" when="greater"/>
                    </and>
                </condition>
                <condition>
                    <xsl:attribute name="property">javac.test.compilerargs</xsl:attribute>
                    <xsl:attribute name="value">--add-reads ${test.module.name}=ALL-UNNAMED</xsl:attribute>
                    <xsl:attribute name="else">--patch-module ${module.name}=<xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template> --add-reads ${module.name}=ALL-UNNAMED</xsl:attribute>
                    <and>
                        <isset property="test.module.name"/>
                        <length length="0" string="${{test.module.name}}" when="greater"/>
                    </and>
                </condition>
            </target>
            <target name="-init-test-run-module-properties" depends="-init-source-module-properties" if="named.module.internal">
                <condition property ="run.test.addexport.source.module.internal" value="${{test.module.name}}" else="${{module.name}}">
                    <and>
                        <isset property="test.module.name"/>
                        <length length="0" string="${{test.module.name}}" when="greater"/>
                    </and>
                </condition>
                <fileset id="run.test.packages.internal" dir="${{build.test.classes.dir}}" includes="**/*.class"/>
                <property name="build.test.classes.dir.abs.internal" location="${{build.test.classes.dir}}"/>
                <pathconvert refid="run.test.packages.internal" property="run.test.addexports.internal" pathsep=" ">
                    <chainedmapper>
                        <regexpmapper from="^(.*)\Q${{file.separator}}\E.*\.class$$" to="\1"/>
                        <filtermapper>
                            <uniqfilter/>
                            <replacestring from="${{build.test.classes.dir.abs.internal}}" to=""/>
                        </filtermapper>
                        <cutdirsmapper dirs="1"/>
                        <packagemapper from="*" to="--add-exports ${{run.test.addexport.source.module.internal}}/*=ALL-UNNAMED"/>
                    </chainedmapper>
                </pathconvert>
                <condition property ="run.test.jvmargs" value="--add-modules ${{test.module.name}} --add-reads ${{test.module.name}}=ALL-UNNAMED ${{run.test.addexports.internal}}" else="--patch-module ${{module.name}}=${{build.test.classes.dir}} --add-modules ${{module.name}} --add-reads ${{module.name}}=ALL-UNNAMED ${{run.test.addexports.internal}}">
                    <and>
                        <isset property="test.module.name"/>
                        <length length="0" string="${{test.module.name}}" when="greater"/>
                    </and>
                </condition>
            </target>
            <target name="-init-test-module-properties-without-module" depends="-init-source-module-properties" unless="named.module.internal">
                <property name="javac.test.sourcepath" value="${{empty.dir}}"/>
                <property name="javac.test.compilerargs" value=""/>
                <property name="run.test.jvmargs" value=""/>
            </target>
            <target name="-init-test-module-properties" depends="-init-test-javac-module-properties-with-module,-init-test-module-properties-without-module"/>

            <target name="-compile-test-depend" if="do.depend.true">
                <xsl:element name="j2seproject3:depend">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                </xsl:element>
            </target>
            <target name="-do-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,deps-jar,compile,-init-test-module-properties,-pre-pre-compile-test,-pre-compile-test,-compile-test-depend</xsl:attribute>
                <xsl:element name="j2seproject3:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                    <xsl:attribute name="processorpath">${javac.test.processorpath}</xsl:attribute>
                    <xsl:attribute name="modulepath">${javac.test.modulepath}</xsl:attribute>
                    <xsl:attribute name="apgeneratedsrcdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="sourcepath">${javac.test.sourcepath}</xsl:attribute>
                    <customize>
                        <compilerarg line="${{javac.test.compilerargs}}"/>
                    </customize>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
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
                <xsl:attribute name="depends">init,deps-jar,compile,-init-test-module-properties,-pre-pre-compile-test,-pre-compile-test-single</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <xsl:element name="j2seproject3:force-recompile">
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                </xsl:element>
                <xsl:element name="j2seproject3:javac">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="sourcepath">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="debug">true</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                    <xsl:attribute name="modulepath">${javac.test.modulepath}</xsl:attribute>
                    <xsl:attribute name="includes">${javac.includes}, module-info.java</xsl:attribute>
                    <xsl:attribute name="excludes"/>
                    <xsl:attribute name="processorpath">${javac.test.processorpath}</xsl:attribute>
                    <xsl:attribute name="apgeneratedsrcdir">${build.test.classes.dir}</xsl:attribute>
                    <customize>
                        <compilerarg line="${{javac.test.compilerargs}}"/>
                    </customize>
                </xsl:element>
                <copy todir="${{build.test.classes.dir}}">
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2seproject3:data/j2seproject3:test-roots"/>
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
                =======================
                TEST EXECUTION SECTION
                =======================
            </xsl:comment>
            
            <target name="-pre-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init</xsl:attribute>
                <mkdir dir="${{build.test.results.dir}}"/>
            </target>
            
            <target name="-do-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-init-test-run-module-properties,-pre-test-run</xsl:attribute>
                <j2seproject3:test testincludes="**/*Test.java" includes="${{includes}}"/>
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
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-pre-test-run-single</xsl:attribute>
                <fail unless="test.includes">Must select some files in the IDE or set test.includes</fail>
                <j2seproject3:test includes="${{test.includes}}" excludes="" testincludes="${{test.includes}}" />
            </target>
            
            <target name="-post-test-run-single">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            
            <target name="test-single">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-pre-test-run-single,-do-test-run-single,-post-test-run-single</xsl:attribute>
                <xsl:attribute name="description">Run single unit test.</xsl:attribute>
            </target>
            
            <target name="-do-test-run-single-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select some files in the IDE or set test.class</fail>
                <fail unless="test.method">Must select some method in the IDE or set test.method</fail>
                <j2seproject3:test includes="${{javac.includes}}" excludes="" testincludes="${{test.class}}" testmethods="${{test.method}}"/>
            </target>
            
            <target name="-post-test-run-single-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single,-do-test-run-single-method</xsl:attribute>
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>

            <target name="test-single-method">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-pre-test-run-single,-do-test-run-single-method,-post-test-run-single-method</xsl:attribute>
                <xsl:attribute name="description">Run single unit test.</xsl:attribute>
            </target>
            
            <xsl:comment>
                =======================
                TEST DEBUGGING SECTION
                =======================
            </xsl:comment>
            
            <target name="-debug-start-debuggee-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <j2seproject3:test-debug includes="${{javac.includes}}" excludes="" testincludes="${{javac.includes}}" testClass="${{test.class}}"/>
            </target>
            
            <target name="-debug-start-debuggee-test-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <fail unless="test.method">Must select some method in the IDE or set test.method</fail>
                <j2seproject3:test-debug includes="${{javac.includes}}" excludes="" testincludes="${{test.class}}" testmethods="${{test.method}}" testClass="${{test.class}}" testMethod="${{test.method}}"/>
            </target>

            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <j2seproject1:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>
            
            <target name="debug-test-method">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-debug-start-debugger-test,-debug-start-debuggee-test-method</xsl:attribute>
            </target>
            
            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <j2seproject1:nbjpdareload dir="${{build.test.classes.dir}}"/>
            </target>
            
            <target name="debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,-do-debug-fix-test</xsl:attribute>
            </target>
            
            
            <xsl:comment>
                =========================
                APPLET EXECUTION SECTION
                =========================
            </xsl:comment>
            
            <target name="run-applet">
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="applet.url">Must select one file in the IDE or set applet.url</fail>
                <j2seproject1:java classname="sun.applet.AppletViewer">
                    <customize>
                        <arg value="${{applet.url}}"/>
                    </customize>
                </j2seproject1:java>
            </target>
            
            <xsl:comment>
                =========================
                APPLET DEBUGGING  SECTION
                =========================
            </xsl:comment>
            
            <target name="-debug-start-debuggee-applet">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single</xsl:attribute>
                <fail unless="applet.url">Must select one file in the IDE or set applet.url</fail>
                <j2seproject3:debug classname="sun.applet.AppletViewer">
                    <customizeDebuggee>
                        <arg value="${{applet.url}}"/>
                    </customizeDebuggee>
                </j2seproject3:debug>
            </target>
            
            <target name="debug-applet">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-start-debugger,-debug-start-debuggee-applet</xsl:attribute>
            </target>
            
            <xsl:comment>
                ===============
                CLEANUP SECTION
                ===============
            </xsl:comment>
            
            <xsl:call-template name="deps.target">
                <xsl:with-param name="kind" select="'clean'"/>
            </xsl:call-template>
            
            <target name="-do-clean">
                <xsl:attribute name="depends">init</xsl:attribute>
                <delete dir="${{build.dir}}"/>
                <delete dir="${{dist.jlink.output}}"/> <!-- Jlink artefact has symlinks inside -->
                <delete dir="${{dist.dir}}" followsymlinks="false" includeemptydirs="true"/> <!-- see issue 176851 -->
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
            </target>
            
            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,-do-clean,-post-clean</xsl:attribute>
                <xsl:attribute name="description">Clean build products.</xsl:attribute>
            </target>

            <target name="-check-call-dep">
                <property file="${{call.built.properties}}" prefix="already.built."/>
                <condition property="should.call.dep">
                    <and>
                    <not>
                        <isset property="already.built.${{call.subproject}}"/>
                    </not>
                        <available file="${{call.script}}"/>
                    </and>
                </condition>
                <!--<echo message="I am {$codename}; should.call.dep=${{should.call.dep}} due to already.built.${{call.subproject}}"/><echoproperties prefix="already.built."/>-->
            </target>
            <target name="-maybe-call-dep" depends="-check-call-dep" if="should.call.dep">
                <ant target="${{call.target}}" antfile="${{call.script}}" inheritall="false">
                    <propertyset>
                        <propertyref prefix="transfer."/>
                        <mapper type="glob" from="transfer.*" to="*"/>
                    </propertyset>
                </ant>
            </target>
            
        </project>
        
    </xsl:template>
    
    <!---
    Generic template to build subdependencies of a certain type.
    Feel free to copy into other modules.
    @param kind required end of name of target to generate
    @param type artifact-type from project.xml to filter on; optional, if not specified, uses
                all references, and looks for clean targets rather than build targets
    @return an Ant target which builds (or cleans) all known subprojects
    -->
    <xsl:template name="deps.target">
        <xsl:param name="kind"/>
        <xsl:param name="type"/>
        <target name="-deps-{$kind}-init" unless="built-{$kind}.properties">
            <property name="built-{$kind}.properties" location="${{build.dir}}/built-{$kind}.properties"/>
            <delete file="${{built-{$kind}.properties}}" quiet="true"/>
        </target>
        <target name="-warn-already-built-{$kind}" if="already.built.{$kind}.${{basedir}}">
            <echo level="warn" message="Cycle detected: {/p:project/p:configuration/j2seproject3:data/j2seproject3:name} was already built"/>
        </target>
        <target name="deps-{$kind}" depends="init,-deps-{$kind}-init">
            <xsl:attribute name="unless">no.deps</xsl:attribute>

            <mkdir dir="${{build.dir}}"/>
            <touch file="${{built-{$kind}.properties}}" verbose="false"/>
            <property file="${{built-{$kind}.properties}}" prefix="already.built.{$kind}."/>
            <!--<echo message="from deps-{$kind} of {/p:project/p:configuration/j2seproject3:data/j2seproject3:name}:"/><echoproperties prefix="already.built.{$kind}."/>-->
            <antcall target="-warn-already-built-{$kind}"/>
            <propertyfile file="${{built-{$kind}.properties}}">
                <entry key="${{basedir}}" value=""/>
            </propertyfile>
            
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
                <xsl:choose>
                    <xsl:when test="projdeps2:properties">
                        <antcall target="-maybe-call-dep">
                            <param name="call.built.properties" value="${{built-{$kind}.properties}}"/>
                            <param name="call.subproject" location="${{project.{$subproj}}}"/>
                            <param name="call.script" location="{$script}"/>
                            <param name="call.target" value="{$subtarget}"/>
                            <param name="transfer.built-{$kind}.properties" value="${{built-{$kind}.properties}}"/>
                            <param name="transfer.not.archive.disabled" value="true"/>
                            <param name="transfer.do.jlink" value="false"/>
                            <xsl:for-each select="projdeps2:properties/projdeps2:property">
                                <param name="transfer.{@name}" value="{.}"/>
                            </xsl:for-each>
                        </antcall>
                    </xsl:when>
                    <xsl:otherwise> <!-- XXX maybe just fold into former? projdeps2:properties/projdeps2:property select nothing? -->
                        <antcall target="-maybe-call-dep">
                            <param name="call.built.properties" value="${{built-{$kind}.properties}}"/>
                            <param name="call.subproject" location="${{project.{$subproj}}}"/>
                            <param name="call.script" location="{$script}"/>
                            <param name="call.target" value="{$subtarget}"/>
                            <param name="transfer.built-{$kind}.properties" value="${{built-{$kind}.properties}}"/>
                            <param name="transfer.not.archive.disabled" value="true"/>
                            <param name="transfer.do.jlink" value="false"/>
                        </antcall>
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
                <antcall target="-maybe-call-dep">
                    <param name="call.built.properties" value="${{built-{$kind}.properties}}"/>
                    <param name="call.subproject" location="${{project.{$subproj}}}"/>
                    <param name="call.script" location="${{project.{$subproj}}}/{$script}"/>
                    <param name="call.target" value="{$subtarget}"/>
                    <param name="transfer.built-{$kind}.properties" value="${{built-{$kind}.properties}}"/>
                    <param name="transfer.not.archive.disabled" value="true"/>
                    <param name="transfer.do.jlink" value="false"/>
                </antcall>
            </xsl:for-each>
            
        </target>
    </xsl:template>
    
    <xsl:template name="createRootAvailableTest">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/j2seproject3:root">
                    <xsl:element name="available">
                        <xsl:attribute name="file"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
                    </xsl:element>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2seproject3:root">
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
        <xsl:param name="condition"/>
        <xsl:for-each select="$roots/j2seproject3:root">
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
                    <xsl:copy-of select="$condition"/>
                </xsl:if>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPackagesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/j2seproject3:root">
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
        <xsl:for-each select="$locations/j2seproject3:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2seproject3:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>						
    </xsl:template>
</xsl:stylesheet>
