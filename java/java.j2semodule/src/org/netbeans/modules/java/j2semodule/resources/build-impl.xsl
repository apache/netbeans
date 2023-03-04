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
                xmlns:j2semodularproject1="http://www.netbeans.org/ns/j2se-modular-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                xmlns:projdeps2="http://www.netbeans.org/ns/ant-project-references/2"
                xmlns:libs="http://www.netbeans.org/ns/ant-project-libraries/1"
                xmlns:if="ant:if"
                xmlns:unless="ant:unless"
                exclude-result-prefixes="xalan p projdeps projdeps2 j2semodularproject1 libs">
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
        
        <xsl:variable name="name" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-impl">
            <xsl:attribute name="default">default</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>
            
            <fail message="Please build using Ant 1.9.7 or higher.">
                <condition>
                    <not>
                        <antversion atleast="1.9.7"/>
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
                <property name="default.javac.source" value="9"/>
                <property name="default.javac.target" value="9"/>
            </target>
            
            <target name="-init-pre-project">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user</xsl:attribute>
                <property file="nbproject/configs/${{config}}.properties"/>
                <property file="nbproject/project.properties"/>
                <property name="netbeans.modular.tasks.version" value="1" />
                <property name="netbeans.modular.tasks.dir" location="${{build.dir}}/tasks/${{netbeans.modular.tasks.version}}" />
            </target>

            <target depends="-init-pre-project" name="-check-netbeans-tasks">
                <condition property="netbeans.tasks.compiled">
                    <available file="${{netbeans.modular.tasks.dir}}/out/netbeans/ModuleInfoSelector.class" />
                </condition>
            </target>
            <target depends="-init-pre-project,-check-netbeans-tasks" name="-init-compile-netbeans-tasks" unless="netbeans.tasks.compiled">
                <echo file="${{netbeans.modular.tasks.dir}}/src/netbeans/CoalesceKeyvalue.java">
<![CDATA[
package netbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class CoalesceKeyvalue extends Task {
    private String property;

    public void setProperty(String property) {
        this.property = property;
    }

    private String value;

    public void setValue(String value) {
        this.value = value;
    }

    private String valueSep;

    public void setValueSep(String valueSep) {
        this.valueSep = valueSep;
    }

    private String entrySep;

    public void setEntrySep(String entrySep) {
        this.entrySep = entrySep;
    }

    private String multiSep;

    public void setMultiSep(String multiSep) {
        this.multiSep = multiSep;
    }

    private String outSep;

    public void setOutSep(String outSep) {
        this.outSep = outSep;
    }

    @Override
    public void execute() throws BuildException {
        List<String> result = new ArrayList<>();
        Map<String, List<String>> module2Paths = new HashMap<>();

        for (String entry : value.split(Pattern.quote(entrySep))) {
            String[] keyValue = entry.split(Pattern.quote(valueSep), 2);
            if (keyValue.length == 1) {
                result.add(keyValue[0]);
            } else {
                module2Paths.computeIfAbsent(keyValue[0], s -> new ArrayList<>())
                            .add(keyValue[1].trim());
            }
        }
        module2Paths.entrySet()
                    .stream()
                    .forEach(e -> result.add(e.getKey() + valueSep + e.getValue().stream().collect(Collectors.joining(multiSep))));
        getProject().setProperty(property, result.stream().collect(Collectors.joining(" " + entrySep)));
    }

}
]]>
                </echo>
                <echo file="${{netbeans.modular.tasks.dir}}/src/netbeans/ModsourceRegexp.java">
            <![CDATA[
package netbeans;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public class ModsourceRegexp extends Task {
    private String property;

    public void setProperty(String property) {
        this.property = property;
    }

    private String filePattern;

    public void setFilePattern(String filePattern) {
        this.filePattern = filePattern;
    }

    private String modsource;

    public void setModsource(String modsource) {
        this.modsource = modsource;
    }

    private List<String> expandGroup(String grp) {
        List<String> exp = new ArrayList<>();
        String item = "";
        int depth = 0;

        for (int i = 0; i < grp.length(); i++) {
            char c = grp.charAt(i);
            switch (c) {
                case '{':
                    if (depth++ == 0) {
                        continue;
                    }
                    break;
                case '}':
                    if (--depth == 0) {
                        exp.add(item);
                        continue;
                    }
                    break;
                case ',':
                    if (depth == 1) {
                        exp.add(item);
                        item = "";
                        continue;
                    }
                default:
                    break;
            }
            item = item + c;
        }
        return exp;
    }

    private List<String> pathVariants(String spec) {
        return pathVariants(spec, new ArrayList<>());
    }

    private List<String> pathVariants(String spec, List<String> res) {
        int start  = spec.indexOf('{');
        if (start == -1) {
            res.add(spec);
            return res;
        }
        int depth = 1;
        int end;
        for (end = start + 1; end < spec.length() && depth > 0; end++) {
            char c = spec.charAt(end);
            switch (c) {
                case '{': depth++; break;
                case '}': depth--; break;
            }
        }
        String prefix = spec.substring(0, start);
        String suffix = spec.substring(end);
        expandGroup(spec.substring(start, end)).stream().forEach(item -> {
            pathVariants(prefix + item + suffix, res);
        });
        return res;
    }

    private String toRegexp2(String spec, String filepattern, String separator) {
        List<String> prefixes = new ArrayList<>();
        List<String> suffixes = new ArrayList<>();
        pathVariants(spec).forEach(item -> {
            suffixes.add(item);
        });
        String tail = "";
        String separatorString = separator;
        if ("\\".equals(separatorString)) {
            separatorString = "\\\\";
        }
        if (filepattern != null && !Objects.equals(filepattern, tail)) {
            tail = separatorString + filepattern;
        }
        return "([^" + separatorString +"]+)\\Q" + separator + "\\E(" + suffixes.stream().collect(Collectors.joining("|")) + ")" + tail;
    }

    @Override
    public void execute() throws BuildException {
        getProject().setProperty(property, toRegexp2(modsource, filePattern, getProject().getProperty("file.separator")));
    }

}
]]>
                </echo>
                <echo file="${{netbeans.modular.tasks.dir}}/src/netbeans/ModuleInfoSelector.java">
            <![CDATA[
package netbeans;

import java.io.File;
import java.util.Arrays;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.types.selectors.BaseExtendSelector;

public class ModuleInfoSelector extends BaseExtendSelector {

    @Override
    public boolean isSelected(File basedir, String filename, File file) throws BuildException {
        String extension = Arrays.stream(getParameters())
                                 .filter(p -> "extension".equals(p.getName()))
                                 .map(p -> p.getValue())
                                 .findAny()
                                 .get();
        return !new File(file, "module-info." + extension).exists();
    }

}
]]>
                </echo>
                <mkdir dir="${{netbeans.modular.tasks.dir}}/out" />
                <javac destdir="${{netbeans.modular.tasks.dir}}/out" srcdir="${{netbeans.modular.tasks.dir}}/src" classpath="${{ant.core.lib}}" />
            </target>
            <target depends="-init-pre-project,-init-compile-netbeans-tasks" name="-init-project">
                <taskdef name="coalesce_keyvalue" uri="http://www.netbeans.org/ns/j2se-modular-project/1" classname="netbeans.CoalesceKeyvalue" classpath="${{netbeans.modular.tasks.dir}}/out" />
                <taskdef name="modsource_regexp" uri="http://www.netbeans.org/ns/j2se-modular-project/1" classname="netbeans.ModsourceRegexp" classpath="${{netbeans.modular.tasks.dir}}/out" />
            </target>

            <target name="-init-source-module-properties">
                <property name="javac.modulepath" value=""/>
                <property name="run.modulepath" value="${{javac.modulepath}}:${{build.modules.dir}}"/>
                <property name="debug.modulepath" value="${{run.modulepath}}"/>
                <property name="javac.upgrademodulepath" value=""/>
                <property name="run.upgrademodulepath" value="${{javac.upgrademodulepath}}"/>
                <condition property="javac.systemmodulepath.cmd.line.arg" value="-system '${{javac.systemmodulepath}}'" else="">
                    <and>
                        <isset property="javac.systemmodulepath"/>
                        <length string="${{javac.systemmodulepath}}" when="greater" length="0"/>
                    </and>
                </condition>
                <property name="dist.jlink.dir" value="${{dist.dir}}/jlink"/>
                <property name="dist.jlink.output" value="${{dist.jlink.dir}}/${{application.title}}"/>
            </target>
            <target name="-do-init">
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-init-macrodef-property</xsl:attribute>

                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
                        <j2semodularproject1:property name="platform.home" value="platforms.${{platform.active}}.home"/>
                        <j2semodularproject1:property name="platform.bootcp" value="platforms.${{platform.active}}.bootclasspath"/>
                        <j2semodularproject1:property name="platform.compiler" value="platforms.${{platform.active}}.compile"/>
                        <j2semodularproject1:property name="platform.javac.tmp" value="platforms.${{platform.active}}.javac"/>
                        <condition property="platform.javac" value="${{platform.home}}/bin/javac">
                            <equals arg1="${{platform.javac.tmp}}" arg2="$${{platforms.${{platform.active}}.javac}}"/>
                        </condition>
                        <property name="platform.javac" value="${{platform.javac.tmp}}"/>
                        <j2semodularproject1:property name="platform.java.tmp" value="platforms.${{platform.active}}.java"/>
                        <condition property="platform.java" value="${{platform.home}}/bin/java">
                            <equals arg1="${{platform.java.tmp}}" arg2="$${{platforms.${{platform.active}}.java}}"/>
                        </condition>
                        <property name="platform.java" value="${{platform.java.tmp}}"/>
                        <j2semodularproject1:property name="platform.javadoc.tmp" value="platforms.${{platform.active}}.javadoc"/>
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

                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableSet">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                    <xsl:with-param name="propName">have.tests</xsl:with-param>
                    <xsl:with-param name="suffix">patchset</xsl:with-param>
                    <xsl:with-param name="filter">
                        <custom classname="netbeans.ModuleInfoSelector" classpath="${{netbeans.modular.tasks.dir}}/out">
                            <param name="extension" value="java" />
                        </custom>
                    </xsl:with-param>
                </xsl:call-template>
                <xsl:call-template name="createRootAvailableTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                    <xsl:with-param name="propName">have.sources</xsl:with-param>
                </xsl:call-template>
                <condition property="main.class.available">
                    <and>
                        <isset property="main.class"/>
                        <not>
                            <equals arg1="${{main.class}}" arg2="" trim="true"/>
                        </not>
                    </and>
                </condition>
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
                <condition property="do.archive">
                    <or>
                        <not>
                            <istrue value="${{jar.archive.disabled}}"/>
                        </not>
                        <istrue value="${{not.archive.disabled}}"/>
                    </or>
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
                <xsl:if test="not(/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform)">
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
                <condition property="main.class.check.available">
                    <and>
                        <isset property="libs.CopyLibs.classpath"/>
                        <available classname="org.netbeans.modules.java.j2seproject.moduletask.ModuleMainClass" classpath="${{libs.CopyLibs.classpath}}"/>
                    </and>
                </condition>
                <property name="jar.index" value="false"/>
                <property name="jar.index.metainf" value="${{jar.index}}"/>
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
                
                <macrodef name="for-paths" xmlns:if="ant:if" xmlns:unless="ant:unless" uri="http://www.netbeans.org/ns/j2se-modular-project/1">
                    <attribute name="paths"/>
                    <attribute name="separator" default="${{path.separator}}"/>
                    <element name="call" implicit="yes"/>
                    <sequential>
                        <local name="entry"/>
                        <local name="tail"/>
                        <local name="moreElements"/>
                        <loadresource quiet="true" property="entry" unless:blank="@{{paths}}">
                            <concat>@{paths}</concat>
                            <filterchain>
                                <replaceregex pattern="([^@{{separator}}]*)\Q@{{separator}}\E.*" replace="\1" />
                            </filterchain>
                        </loadresource>
                
                        <sequential if:set="entry" >
                            <call/>
                        </sequential>                
                        <condition property="moreElements" value="true" else="false">
                            <contains string="@{{paths}}" substring="@{{separator}}"/>
                        </condition>
                        <loadresource quiet="true" property="tail" if:true="${{moreElements}}">
                            <concat>@{paths}</concat>
                            <filterchain>
                                <replaceregex pattern="[^@{{separator}}]*\Q@{{separator}}\E(.*)" replace="\1" />
                            </filterchain>
                        </loadresource>

                        <j2semodularproject1:for-paths paths="${{tail}}" if:true="${{moreElements}}">
                            <call />
                        </j2semodularproject1:for-paths>
                    </sequential>
                </macrodef>
                <property name="modules.supported.internal" value="true"/>
                <condition property="file.separator.string" value="\${{file.separator}}" else="${{file.separator}}">
                    <equals arg1="${{file.separator}}" arg2="\"/>
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
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                </xsl:call-template>
                <xsl:call-template name="createSourcePathValidityTest">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                </xsl:call-template>
                <fail unless="build.dir">Must set build.dir</fail>
                <fail unless="dist.dir">Must set dist.dir</fail>
                <fail unless="build.modules.dir">Must set build.modules.dir</fail>
                <fail unless="dist.javadoc.dir">Must set dist.javadoc.dir</fail>
                <fail unless="build.test.modules.dir">Must set build.test.modules.dir</fail>
                <fail unless="build.test.results.dir">Must set build.test.results.dir</fail>
                <fail unless="build.classes.excludes">Must set build.classes.excludes</fail>

                <fail message="Java 9 support requires Ant 1.10.0 or higher.">
                    <condition>
                        <not>
                            <antversion atleast="1.10.0"/>
                        </not>
                    </condition>
                </fail>
            </target>
            
            <target name="-init-macrodef-property">
                <macrodef>
                    <xsl:attribute name="name">property</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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

            <target name="-init-macrodef-javac" depends="-init-ap-cmdline-properties,-init-source-module-properties">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.modules.dir}</xsl:attribute>
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
                        <xsl:attribute name="name">modulesourcepath</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createModulePath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                            </xsl:call-template>
                        </xsl:attribute>
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
                        <condition property="processormodulepath.set">
                            <resourcecount when="greater" count="0">
                                <path>
                                    <pathelement path="@{{processormodulepath}}"/>
                                </path>
                            </resourcecount>
                        </condition>
                        <javac>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="debug">@{debug}</xsl:attribute>
                            <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:if test ="not(/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform/@explicit-source-supported ='false')">
                                <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                <xsl:attribute name="target">${javac.target}</xsl:attribute>
                            </xsl:if>
                            <xsl:attribute name="includes">@{includes}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:choose>
                                <xsl:when test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <modulepath>
                                <path path="@{{modulepath}}"/>
                            </modulepath>
                            <modulesourcepath>
                                <path path="@{{modulesourcepath}}"/>
                            </modulesourcepath>
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
            <target name="-init-macrodef-javac-depend" depends="-init-macrodef-javac">
                <macrodef> <!-- #36033, #85707 -->
                    <xsl:attribute name="name">depend</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.modules.dir}</xsl:attribute>
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

            <target name="-init-macrodef-junit-prototype">
                <macrodef>
                    <xsl:attribute name="name">junit-prototype</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <property name="empty.dir" location="${{build.dir}}/empty"/>
                        <property name="junit.forkmode" value="perTest"/>
                        <junit>
                            <xsl:attribute name="showoutput">true</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <xsl:attribute name="forkmode">${junit.forkmode}</xsl:attribute>
                            <xsl:attribute name="dir">${work.dir}</xsl:attribute> <!-- #47474: match <java> -->
                            <xsl:attribute name="failureproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="errorproperty">tests.failed</xsl:attribute>
                            <xsl:attribute name="tempdir">${build.dir}</xsl:attribute>
                            <xsl:if test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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
                            <jvmarg value="--module-path"/>
                            <jvmarg path="${{run.modulepath}}${{path.separator}}${{run.test.modulepath}}${{path.separator}}${{empty.dir}}"/>
                            <jvmarg line="${{run.test.jvmargs}}"/>
                            <customizePrototype/>
                        </junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-single" depends="-init-test-properties,-init-macrodef-junit-prototype" if="${{nb.junit.single}}" unless="${{nb.junit.batch}}">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:junit-prototype>
                            <customizePrototype>
                                <test todir="${{build.test.results.dir}}" name="@{{testincludes}}" methods="@{{testmethods}}"/>
                                <customize/>
                            </customizePrototype>
                        </j2semodularproject1:junit-prototype>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-batch" depends="-init-test-properties,-init-macrodef-junit-prototype" if="${{nb.junit.batch}}" unless="${{nb.junit.single}}">
                <macrodef>
                    <xsl:attribute name="name">junit</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:junit-prototype>
                            <customizePrototype>
                                <batchtest todir="${{build.test.results.dir}}">
                                    <xsl:call-template name="createMappedResources">
                                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                                        <xsl:with-param name="includes">**/@{includes}</xsl:with-param>
                                        <xsl:with-param name="includes2">**/@{testincludes}</xsl:with-param>
                                        <xsl:with-param name="excludes">@{excludes}</xsl:with-param>
                                        <xsl:with-param name="setprefix">have.tests</xsl:with-param>
                                    </xsl:call-template>
                                    <fileset dir="${{build.test.modules.dir}}" excludes="@{{excludes}},${{excludes}},${{test.binaryexcludes}}" includes="${{test.binaryincludes}}">
                                        <filename name="${{test.binarytestincludes}}"/>
                                    </fileset>
                                </batchtest>
                                <customize/>
                            </customizePrototype>
                        </j2semodularproject1:junit-prototype>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit" if="${{junit.available}}" depends="-init-macrodef-junit-init,-init-macrodef-junit-single, -init-macrodef-junit-batch"/>

            <target name="-init-macrodef-testng" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                                <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
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
                            <xsl:if test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:junit includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </j2semodularproject1:junit>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-testng-impl" depends="-init-macrodef-testng" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:testng includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize/>
                        </j2semodularproject1:testng>
                    </sequential>
                </macrodef>
            </target>
                        
            <target name="-init-macrodef-test" depends="-init-macrodef-test-impl,-init-macrodef-junit-impl,-init-macrodef-testng-impl">
                <macrodef>
                    <xsl:attribute name="name">test</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:test-impl includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize>
                                <jvmarg line="${{run.jvmargs}}"/>
                                <jvmarg line="${{run.jvmargs.ide}}"/>
                            </customize>
                        </j2semodularproject1:test-impl>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-junit-debug-impl" depends="-init-macrodef-junit" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:junit includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customize>
                                <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                                <customizeDebuggee/>
                            </customize>
                        </j2semodularproject1:junit>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-debug" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:debug classname="org.testng.TestNG" classpath="${{debug.test.classpath}}">
                            <customizeDebuggee>
                                <customize2/>
                                <jvmarg value="-ea"/>
                                <arg line="${{testng.debug.mode}}"/>
                                <arg line="-d ${{build.test.results.dir}}"/>
                                <arg line="-listener org.testng.reporters.VerboseReporter"/>
                                <arg line="${{testng.cmd.args}}"/>
                            </customizeDebuggee>
                        </j2semodularproject1:debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-testng-debug-impl" depends="-init-macrodef-testng-debug" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">testng-debug-impl</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:testng-debug testClass="@{{testClass}}" testMethod="@{{testMethod}}">
                            <customize2/>
                        </j2semodularproject1:testng-debug>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-test-debug-junit" depends="-init-macrodef-junit-debug-impl" if="${{junit.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:test-debug-impl includes="@{{includes}}" excludes="@{{excludes}}" testincludes="@{{testincludes}}" testmethods="@{{testmethods}}">
                            <customizeDebuggee>
                                <jvmarg line="${{run.jvmargs}}"/>
                                <jvmarg line="${{run.jvmargs.ide}}"/>
                            </customizeDebuggee>
                        </j2semodularproject1:test-debug-impl>
                    </sequential>
                </macrodef>
            </target>
            
            <target name="-init-macrodef-test-debug-testng" depends="-init-macrodef-testng-debug-impl" if="${{testng.available}}">
                <macrodef>
                    <xsl:attribute name="name">test-debug</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:testng-debug-impl testClass="@{{testClass}}" testMethod="@{{testMethod}}">
                            <customize2>
                                <syspropertyset>
                                    <propertyref prefix="test-sys-prop."/>
                                    <mapper from="test-sys-prop.*" to="*" type="glob"/>
                                </syspropertyset>
                            </customize2>
                        </j2semodularproject1:testng-debug-impl>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
                                <bootclasspath>
                                    <path path="${{platform.bootcp}}"/>
                                </bootclasspath>
                            </xsl:if>
                        </nbjpdastart>
                    </sequential>
                </macrodef>
                <macrodef>
                    <xsl:attribute name="name">nbjpdareload</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">dir</xsl:attribute>
                        <xsl:attribute name="default">${debug.modules.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <nbjpdareload>
                            <fileset includes="${{fix.classes}}" dir="@{{dir}}" >
                                <include name="*/${{fix.includes}}*.class"/>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                        <j2semodularproject1:java modulename="@{{modulename}}" classname="@{{classname}}" modulepath="@{{modulepath}}" classpath="@{{classpath}}">
                            <customize>
                                <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                                <customizeDebuggee/>
                            </customize>
                        </j2semodularproject1:java>
                    </sequential>
                </macrodef>
            </target>

            <target name="-init-macrodef-java" depends="-init-source-module-properties">
                <macrodef>
                    <xsl:attribute name="name">java</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
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
                            <xsl:if test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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

            <target name="-init-presetdef-jar">
                <presetdef>
                    <xsl:attribute name="name">jar</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2se-modular-project/1</xsl:attribute>
                    <jar jarfile="${{dist.jar}}" compress="${{jar.compress}}" index="${{jar.index}}" manifestencoding="UTF-8">
                        <j2semodularproject1:fileset dir="${{build.classes.dir}}" excludes="${{dist.archive.excludes}}"/>
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
            </target>
            <target name="-init-ap-cmdline-supported" depends="-init-ap-cmdline-properties">
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
                <xsl:attribute name="depends">-pre-init,-init-private<xsl:if test="/p:project/p:configuration/libs:libraries/libs:definitions">,-init-libraries</xsl:if>,-init-user,-init-project,-do-init,-post-init,-init-check,-init-macrodef-property,-init-macrodef-javac-depend,-init-macrodef-test,-init-macrodef-test-debug,-init-macrodef-nbjpda,-init-macrodef-debug,-init-macrodef-java,-init-presetdef-jar,-init-ap-cmdline</xsl:attribute>
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
                        
            <target name="-verify-automatic-build">
                <xsl:attribute name="depends">init,-check-automatic-build,-clean-after-automatic-build</xsl:attribute>
            </target>
            
            <target name="-check-automatic-build">
                <xsl:attribute name="depends">init</xsl:attribute>
                <available file="${{build.modules.dir}}/.netbeans_automatic_build" property="netbeans.automatic.build"/>
            </target>
            
            <target name="-clean-after-automatic-build" depends="init" if="netbeans.automatic.build">
                <antcall target="clean">                    
                    <param name="no.dependencies" value="true"/>
                </antcall>
            </target>
            
            <target name="-pre-pre-compile">
                <mkdir dir="${{build.modules.dir}}"/>
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
                <j2semodularproject1:depend>
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                        </xsl:call-template>
                        <xsl:text>:${build.generated.subdirs}</xsl:text>
                    </xsl:attribute>
                </j2semodularproject1:depend>
            </target>
            <target name="-do-compile">
                <xsl:attribute name="depends">init,deps-jar,-pre-pre-compile,-pre-compile,-compile-depend</xsl:attribute>
                <xsl:attribute name="if">have.sources</xsl:attribute>
                <j2semodularproject1:javac gensrcdir="${{build.generated.sources.dir}}"/>
                <xsl:call-template name="copyResources">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                    <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    <xsl:with-param name="todir">${build.modules.dir}</xsl:with-param>
                </xsl:call-template>
            </target>

            <target name="-copy-persistence-xml" if="has.persistence.xml"><!-- see eclipselink issue https://bugs.eclipse.org/bugs/show_bug.cgi?id=302450, need to copy persistence.xml before build -->
                <fail message="XXX: Not supported on MM projects"/>
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
                <xsl:attribute name="depends">init,deps-jar</xsl:attribute>
                <fail unless="javac.includes">Must select some files in the IDE or set javac.includes</fail>
                <j2semodularproject1:force-recompile/>
                <xsl:element name="j2semodularproject1:javac">
                    <xsl:attribute name="includes">${javac.includes}, module-info.java</xsl:attribute>
                    <xsl:attribute name="excludes"/>
                    <!--
                    <xsl:attribute name="sourcepath"> <!- - #115918 - ->
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    -->
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
            
            <target depends="init,compile" name="-check-module-main-class">
                <condition property="do.module.main.class">
                    <and>
                        <available file="${{module.dir}}/module-info.class"/>
                        <isset property="main.class.check.available"/>
                    </and>
                </condition>
            </target>
            <target name="-pre-pre-jar" depends="init">
                <dirname property="dist.jar.dir" file="${{dist.jar}}"/>
                <mkdir dir="${{dist.jar.dir}}"/>
            </target>
            
            <target name="-pre-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

    
            <target name="-pre-single-jar">
                <!-- Empty placeholder for easier customization. -->
                <!-- You can override this target in the ../build.xml file. -->
            </target>

            <target name="-make-single-jar" depends="-pre-single-jar" if="module.jar.filename">
                <jar compress="${{jar.compress}}" 
                     destfile="${{dist.dir}}/${{module.jar.filename}}" manifestencoding="UTF-8"
                    basedir="${{module.dir}}" excludes="${{dist.archive.excludes}}"/>
            </target>
            
            <target name="-do-jar-jar">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-main-module-check-condition</xsl:attribute>
                <xsl:attribute name="if">do.archive</xsl:attribute>
                <xsl:attribute name="unless">do.mkdist</xsl:attribute>
                <property name="build.modules.dir.resolved" location="${{build.modules.dir}}"/>
                <dirset dir="${{build.modules.dir.resolved}}" id="do.jar.dirs" includes="*"/>
                <pathconvert property="do.jar.dir.list" refid="do.jar.dirs">
                    <identitymapper/>
                </pathconvert>
                <j2semodularproject1:for-paths paths="${{do.jar.dir.list}}">
                    <local name="module.jar.filename"/>
                    <local name="module.jar.name.tmp"/>
            
                    <basename property="module.jar.name.tmp" file="${{entry}}"/>
                    <property name="module.jar.filename" value="${{module.jar.name.tmp}}.jar"/>
                    <antcall target="-make-single-jar" inheritRefs="true">
                        <param name="module.jar.filename" value="${{module.jar.filename}}"/>
                        <param name="module.dir" location="${{entry}}"/>
                    </antcall>
                </j2semodularproject1:for-paths>
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
                <property location="${{build.classes.dir}}" name="build.classes.dir.resolved"/>
                <property location="${{dist.jar}}" name="dist.jar.resolved"/>
                <pathconvert property="run.classpath.with.dist.jar">
                    <path path="${{run.classpath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to="${{dist.jar.resolved}}"/>
                </pathconvert>
                <pathconvert property="run.modulepath.with.dist.jar">
                    <path path="${{run.modulepath}}"/>
                    <map from="${{build.classes.dir.resolved}}" to="${{dist.jar.resolved}}"/>
                </pathconvert>
                <condition property="jar.usage.message.module.path" value=" --module-path ${{run.modulepath.with.dist.jar}}" else="">
                    <and>
                        <isset property="modules.supported.internal"/>
                        <length length="0" string="${{run.modulepath.with.dist.jar}}" when="greater"/>
                    </and>
                </condition>
                <condition property="jar.usage.message.class.path" value=" -cp ${{run.classpath.with.dist.jar}}" else="">
                    <length length="0" string="${{run.classpath.with.dist.jar}}" when="greater"/>
                </condition>
                <condition property="jar.usage.message.main.class" value=" -m ${{module.name}}/${{main.class}}" else=" ${{main.class}}">
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

            <target name="-do-jar-without-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar,-do-jar-jar</xsl:attribute>
            </target>
            <target name="-do-jar-with-libraries">
                <xsl:attribute name="depends">init,compile,-pre-pre-jar,-pre-jar</xsl:attribute>
            </target>
           
            <target name="-post-jar">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-do-jar">
                <xsl:attribute name="depends">init,compile,-pre-jar,-do-jar-without-libraries,-do-jar-with-libraries,-post-jar</xsl:attribute>
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
                    </and>
                </condition>
            </target>
            <target name="-do-deploy" depends="init,-do-jar,-post-jar,-pre-deploy,-check-jlink,-main-module-set" if="do.jlink.internal">
                <delete dir="${{dist.jlink.dir}}" quiet="true" failonerror="false"/>
                <property name="jlink.launcher.name" value="${{application.title}}"/>
                <pathconvert property="jlink.modulelist.internal" pathsep=",">
                    <fileset dir="${{dist.dir}}" includes="*.jar"/>
                    <mapper>
                    <chainedmapper>
                      <flattenmapper/>
                      <globmapper from="*.jar" to="*"/>
                    </chainedmapper>
                    </mapper>
                </pathconvert>
                <condition property="jlink.add.modules" value="${{jlink.modulelist.internal}},${{jlink.additionalmodules}}" else="${{jlink.modulelist.internal}}">
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
                        <isset property="module.name"/>
                        <length string="${{module.name}}" when="greater" length="0"/>
                        <isset property="main.class.available"/>
                    </and>
                </condition>
                <xsl:choose>
                    <xsl:when test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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
                    <arg path="${{jlink.systemmodules.internal}}:${{run.modulepath}}:${{dist.dir}}"/>
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
            
            <target name="-check-main-class">
                <fail unless="main.class">No main class specified</fail>
            </target>
            <target depends="init,compile,-check-main-class,-main-module-check" description="Run a main class." name="run">
                <property name="main.class.relativepath" refid="main.class.relativepath"></property>
                <pathconvert pathsep="," property="src.dir.list" refid="have.sources.set"></pathconvert>
                <j2semodularproject1:modsource_regexp filePattern="(.*$)" modsource="${{src.dir.path}}" property="run.src.dir.path.regexp"/>
                <j2semodularproject1:java>
                    <customize>
                        <arg line="${{application.args}}"></arg>
                    </customize>
                </j2semodularproject1:java>
            </target>
            <target name="-main-module-set" unless="module.name">
                <condition property="check.class.name" value="${{run.class}}" else="${{main.class}}">
                    <isset property="run.class"/>
                </condition>
                <condition property="run.modules.dir" value="${{build.modules.dir}}">
                    <not>
                        <isset property="run.modules.dir"/>
                    </not>
                </condition>
                <resources id="main.class.relativepath">
                    <mappedresources>
                        <string value="${{check.class.name}}"/>
                        <unpackagemapper from="*" to="*.class" />
                    </mappedresources>
                </resources>
                <property name="run.modules.dir.location" location="${{run.modules.dir}}"/>
                <pathconvert property="module.name">
                    <fileset dir="${{run.modules.dir}}" includes="**/${{toString:main.class.relativepath}}"/>
                    <regexpmapper from="\Q${{run.modules.dir.location}}${{file.separator}}\E([^${{file.separator.string}}]+)\Q${{file.separator}}\E.*\.class" to="\1"/>
                </pathconvert>
            </target>
            <target name="-main-module-check" depends="-main-module-set">
                <fail message="Could not determine module of the main class and module.name is not set">
                    <condition>
                        <or>
                            <not>
                                <isset property="module.name"/>
                            </not>
                            <length string="${{module.name}}" when="equal" length="0"/>
                        </or>
                    </condition>
                </fail>
            </target>
            <target name="-main-module-check-condition" depends="-main-module-set" if="main.class.available">
                <fail message="Could not determine module of the main class and module.name is not set">
                    <condition>
                        <or>
                            <not>
                                <isset property="module.name"/>
                            </not>
                            <length string="${{module.name}}" when="equal" length="0"/>
                        </or>
                    </condition>
                </fail>
            </target>
            <target name="-do-not-recompile">
                <property name="javac.includes.binary" value=""/> <!-- #116230 hack -->
            </target>
            <target name="run-single">
                <xsl:attribute name="depends">init,compile-single,-main-module-check</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <j2semodularproject1:java classname="${{run.class}}"/>
            </target>

            <target name="run-test-with-main">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-main-module-check</xsl:attribute>
                <fail unless="run.class">Must select one file in the IDE or set run.class</fail>
                <j2semodularproject1:java classname="${{run.class}}" classpath="${{run.test.classpath}}" modulepath="${{run.test.modulepath}}">
                    <customize>
                        <jvmarg line="${{run.test.jvmargs}}"/>
                    </customize>
                </j2semodularproject1:java>
            </target>

            <xsl:comment>
                =================
                DEBUGGING SECTION
                =================
            </xsl:comment>
            
            <target name="-debug-init">
                <condition property="run.class" value="${{debug.class}}" else="${{main.class}}">
                    <isset property="debug.class"/>
                </condition>
                <fail message="debug.class or main.class property is not set" unless="run.class"/>
            </target>
            <target name="-debug-start-debugger">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-debug-init,-main-module-check</xsl:attribute>
                <j2semodularproject1:nbjpdastart name="${{debug.class}}"/>
            </target>

            <target name="-debug-start-debugger-main-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-debug-init,-main-module-check</xsl:attribute>
                <j2semodularproject1:nbjpdastart name="${{debug.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="-debug-start-debuggee">
                <xsl:attribute name="depends">init,compile,-debug-init,-main-module-check</xsl:attribute>
                <j2semodularproject1:debug classname="${{run.class}}">
                    <customizeDebuggee>
                        <arg line="${{application.args}}"/>
                    </customizeDebuggee>
                </j2semodularproject1:debug>
            </target>
            
            <target name="debug">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-init,-main-module-check,-debug-start-debugger,-debug-start-debuggee</xsl:attribute>
                <xsl:attribute name="description">Debug project in IDE.</xsl:attribute>
            </target>
            
            <target name="-debug-start-debugger-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-debug-init,-main-module-check</xsl:attribute>
                <j2semodularproject1:nbjpdastart stopclassname="${{debug.class}}"/>
            </target>
            
            <target name="debug-stepinto">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile,-debug-start-debugger-stepinto,-debug-start-debuggee</xsl:attribute>
            </target>
            
            <target name="-debug-start-debuggee-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-init,-main-module-check</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <j2semodularproject1:debug classname="${{debug.class}}"/>
            </target>
            
            <target name="debug-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-single,-debug-start-debugger,-debug-start-debuggee-single</xsl:attribute>
            </target>

            <target name="-debug-start-debuggee-main-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-debug-init,-main-module-check</xsl:attribute>
                <fail unless="debug.class">Must select one file in the IDE or set debug.class</fail>
                <j2semodularproject1:debug classname="${{debug.class}}" classpath="${{debug.test.classpath}}"/>
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
                 <property name="debug.modules.dir" location="${{build.modules.dir}}"/>
                <j2semodularproject1:nbjpdareload/>
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
                <j2semodularproject1:junit includes="${{includes}}" excludes="${{excludes}}" testincludes="${{profile.class}}" testmethods="">
                    <customize>
                        <jvmarg value="-agentlib:jdwp=transport=${{debug-transport}},address=${{jpda.address}}"/>
                        <env key="${{profiler.info.pathvar}}" path="${{profiler.info.agentpath}}:${{profiler.current.path}}"/>
                        <jvmarg value="${{profiler.info.jvmargs.agent}}"/>
                        <jvmarg line="${{profiler.info.jvmargs}}"/>
                        <classpath>
                            <path path="${{run.test.classpath}}"/>
                        </classpath>
                    </customize>
                </j2semodularproject1:junit>
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
                    <xsl:when test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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
                            <xsl:when test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
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
                    <xsl:if test="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:explicit-platform">
                        <xsl:attribute name="executable">${platform.javadoc}</xsl:attribute>
                    </xsl:if>                                                        
                    <classpath>
                        <path path="${{javac.classpath}}"/>
                    </classpath>
                    <!-- Does not work with includes/excludes:
                    <sourcepath>
                        <xsl:call-template name="createPathElements">
                            <xsl:with-param name="locations" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                        </xsl:call-template>
                    </sourcepath>
                    -->
                    <!-- Does not work with includes/excludes either, and duplicates class names in index:
                    <xsl:call-template name="createPackagesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
                    </xsl:call-template>
                    -->
                    <xsl:call-template name="createFilesets">
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
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
                        <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:source-roots"/>
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
                <mkdir dir="${{build.test.modules.dir}}"/>
            </target>
            
            <target name="-pre-compile-test">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-init-test-run-module-properties" depends="-init-source-module-properties">
                <fileset id="run.test.packages.internal" dir="${{build.test.modules.dir}}" includes="**/*.class"/>
                <property name="build.test.modules.dir.abs.internal" location="${{build.test.modules.dir}}"/>
                <pathconvert refid="run.test.packages.internal" property="run.test.addexports.internal" pathsep=" ">
                    <chainedmapper>
                        <filtermapper>
                            <replacestring from="${{build.test.modules.dir.abs.internal}}${{file.separator}}" to=""/>
                        </filtermapper>
                        <regexpmapper from="^([^${{file.separator.string}}]*)\Q${{file.separator}}\E(.*)\Q${{file.separator}}\E.*\.class$$" to="\1${{path.separator}}\2"/>
                        <filtermapper>
                            <uniqfilter/>
                            <replacestring from="${{file.separator}}" to="."/>
                        </filtermapper>
                        <regexpmapper from="([^${{file.separator.string}}]+)${{path.separator}}(.*)" to="--add-exports \1/\2=ALL-UNNAMED"/>
                    </chainedmapper>
                </pathconvert>
                <property name="build.test.modules.location" location="${{build.test.modules.dir}}"/>
                <pathconvert property="run.test.addmodules.list" pathsep=",">
                    <map from="${{build.test.modules.location}}${{file.separator}}" to=""/>
                    <dirset dir="${{build.test.modules.dir}}" includes="*"/>
                    <chainedmapper>
                        <filtermapper>
                            <uniqfilter/>
                        </filtermapper>
                    </chainedmapper>
                </pathconvert>
                <pathconvert property="run.test.patchmodules.list" pathsep=" ">
                    <dirset dir="${{build.test.modules.dir}}" includes="*">
                        <custom classname="netbeans.ModuleInfoSelector" classpath="${{netbeans.modular.tasks.dir}}/out">
                            <param name="extension" value="class" />
                        </custom>
                    </dirset>
                    <chainedmapper>
                        <filtermapper>
                            <uniqfilter/>
                        </filtermapper>
                        <regexpmapper from=".*\Q${{file.separator}}\E([^${{file.separator.string}}]+)$" to="--patch-module \1=\0"/>
                    </chainedmapper>
                </pathconvert>
                <j2semodularproject1:coalesce_keyvalue property="run.test.patchmodules" value="${{run.test.patchmodules.list}}" valueSep="=" multiSep="${{path.separator}}" entrySep="--patch-module "/>
                <condition property="run.test.addmodules.internal" value="--add-modules ${{run.test.addmodules.list}}" else="">
                    <isset property="run.test.addmodules.list"/>
                </condition>
                <pathconvert property="run.test.addreads.internal" pathsep=" ">
                    <map from="${{build.test.modules.location}}" to=""/>
                    <dirset dir="${{build.test.modules.dir}}" includes="*"/>
                    <chainedmapper>
                        <regexpmapper from="^\Q${{build.test.modules.location}}${{file.separator}}\E(.*)" to="\1"/>
                        <regexpmapper from="(.*)" to="--add-reads \1=ALL-UNNAMED"/>
                        <filtermapper>
                            <uniqfilter/>
                        </filtermapper>
                    </chainedmapper>
                </pathconvert>
                <property name="run.test.jvmargs" value="${{run.test.addmodules.internal}} ${{run.test.addreads.internal}} ${{run.test.addexports.internal}} ${{run.test.patchmodules}}"/>
            </target>
            <target name="-init-test-javac-module-properties" depends="-init-source-module-properties">
                <pathconvert pathsep=" " property="compile.test.patchmodule.internal" refid="have.tests.patchset">
                    <regexpmapper from="(.*\Q${{file.separator}}\E)([^${{file.separator.string}}]+)\Q${{file.separator}}\E(.*)$$" to="--patch-module \2=\1\2${{file.separator.string}}\3"/>
                </pathconvert>
                <pathconvert property="compile.test.addreads" pathsep=" ">
                    <union refid="have.tests.set"/>
                    <chainedmapper>
                        <firstmatchmapper>
                            <regexpmapper from="${{have.tests.test.src.dir.regexp}}" to="\1"/>
                        </firstmatchmapper>
                        <regexpmapper from="(.*)" to="--add-reads \1=ALL-UNNAMED"/>
                        <filtermapper>
                            <uniqfilter/>
                        </filtermapper>
                    </chainedmapper>
                </pathconvert>
                <j2semodularproject1:coalesce_keyvalue property="compile.test.patchmodules" value="${{compile.test.patchmodule.internal}}" valueSep="=" multiSep="${{path.separator}}" entrySep="--patch-module "/>
                <property name="javac.test.moduleargs" value="${{compile.test.patchmodules}} ${{compile.test.addreads}}"/>
            </target>
            <target name="-init-test-module-properties" depends="-init-test-javac-module-properties">
                <property name="test.module.build.location" location="${{build.modules.dir}}"/>
                <xsl:element name="property">
                    <xsl:attribute name="name">
                        <xsl:text>test.source.modulepath</xsl:text>
                    </xsl:attribute>
                    <xsl:attribute name="value">
                        <xsl:call-template name="createModulePath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                </xsl:element>
                <property name="test.compile.modulepath" value="${{javac.test.modulepath}}:${{build.modules.dir}}"/>
                <macrodef name="test-javac" uri="http://www.netbeans.org/ns/j2se-modular-project/1">
                    <attribute name="includes" default="${{includes}}"/>
                    <attribute name="excludes" default="${{excludes}}"/>
                    <element name="additionalargs" implicit="true" optional="true" />
                    <sequential>
                        <j2semodularproject1:javac destdir="${{build.test.modules.dir}}" debug="true" classpath="${{javac.test.classpath}}" processorpath="${{javac.test.processorpath}}" 
                                                   modulepath="${{test.compile.modulepath}}" apgeneratedsrcdir="${{build.test.modules.dir}}" modulesourcepath="${{test.source.modulepath}}"
                                                   includes="@{{includes}}" excludes="@{{excludes}}">
                            <customize>
                                <compilerarg line="${{javac.test.moduleargs}}"/>
                                <additionalargs/>
                            </customize>
                        </j2semodularproject1:javac>
                    </sequential>
                </macrodef>

            </target>

            <target name="-compile-test-depend" if="do.depend.true">
                <xsl:element name="j2semodularproject1:depend">
                    <xsl:attribute name="srcdir">
                        <xsl:call-template name="createPath">
                            <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                        </xsl:call-template>
                    </xsl:attribute>
                    <xsl:attribute name="destdir">${build.test.classes.dir}</xsl:attribute>
                    <xsl:attribute name="classpath">${javac.test.classpath}</xsl:attribute>
                </xsl:element>
            </target>
            <target name="-do-compile-test">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,deps-jar,compile,-init-test-module-properties,-pre-pre-compile-test,-pre-compile-test,-compile-test-depend</xsl:attribute>
                <j2semodularproject1:test-javac/>
                <xsl:call-template name="copyResources">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                    <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    <xsl:with-param name="todir">${build.test.modules.dir}</xsl:with-param>
                </xsl:call-template>
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
                <xsl:element name="j2semodularproject1:force-recompile">
                    <xsl:attribute name="destdir">${build.test.modules.dir}</xsl:attribute>
                </xsl:element>
                <j2semodularproject1:test-javac includes="${{javac.includes}}"/>
                <xsl:call-template name="copyResources">
                    <xsl:with-param name="roots" select="/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:test-roots"/>
                    <xsl:with-param name="excludes">${build.classes.excludes}</xsl:with-param>
                    <xsl:with-param name="todir">${build.test.modules.dir}</xsl:with-param>
                </xsl:call-template>
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
            <target name="-init-test-run">
                <property name="run.modules.dir" value="${{build.test.modules.dir}}"/>
            </target>
            <target name="-do-test-run">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test,-init-test-run-module-properties,-pre-test-run</xsl:attribute>
                <j2semodularproject1:test testincludes="**/*Test.java" includes="${{includes}}"/>
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
                <j2semodularproject1:test includes="${{test.includes}}" excludes="" testincludes="${{test.includes}}" />
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
                <j2semodularproject1:test includes="${{javac.includes}}" excludes="" testincludes="${{test.class}}" testmethods="${{test.method}}"/>
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
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <j2semodularproject1:test-debug includes="${{javac.includes}}" excludes="" testincludes="${{javac.includes}}" testClass="${{test.class}}"/>
            </target>
            
            <target name="-debug-start-debuggee-test-method">
                <xsl:attribute name="if">have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-pre-test-run-single</xsl:attribute>
                <fail unless="test.class">Must select one file in the IDE or set test.class</fail>
                <fail unless="test.method">Must select some method in the IDE or set test.method</fail>
                <j2semodularproject1:test-debug includes="${{javac.includes}}" excludes="" testincludes="${{test.class}}" testmethods="${{test.method}}" testClass="${{test.class}}" testMethod="${{test.method}}"/>
            </target>

            <target name="-debug-start-debugger-test">
                <xsl:attribute name="if">netbeans.home+have.tests</xsl:attribute>
                <xsl:attribute name="depends">init,compile-test</xsl:attribute>
                <j2semodularproject1:nbjpdastart name="${{test.class}}" classpath="${{debug.test.classpath}}"/>
            </target>
            
            <target name="debug-test">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-debug-start-debugger-test,-debug-start-debuggee-test</xsl:attribute>
            </target>
            
            <target name="debug-test-method">
                <xsl:attribute name="depends">init,compile-test-single,-init-test-run-module-properties,-debug-start-debugger-test,-debug-start-debuggee-test-method</xsl:attribute>
            </target>
            <target name="debug-single-method" depends="debug-test-method" />
            
            <target name="-do-debug-fix-test">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">init,-pre-debug-fix,compile-test-single</xsl:attribute>
                <property name="debug.modules.dir" value="${{build.test.modules.dir}}"/>
                <j2semodularproject1:nbjpdareload/>
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
                <fail message="Applets are no longer supported by JDK 9"/>
            </target>
            
            <xsl:comment>
                =========================
                APPLET DEBUGGING  SECTION
                =========================
            </xsl:comment>
            
            <target name="-debug-start-debuggee-applet">
                <fail message="Applets are no longer supported by JDK 9"/>
            </target>
            
            <target name="debug-applet">
                <fail message="Applets are no longer supported by JDK 9"/>
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
                <delete dir="${{dist.jlink.output}}"/>
                <delete dir="${{dist.dir}}" followsymlinks="false" includeemptydirs="true"/> <!-- see issue 176851 -->
                <!-- XXX explicitly delete all build.* and dist.* dirs in case they are not subdirs -->
            </target>
            
            <target name="-post-clean">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            
            <target name="-recompile-netbeans-tasks-after-clean">
                <antcall target="-init-compile-netbeans-tasks" inheritall="false"/>
            </target>

            <target name="clean">
                <xsl:attribute name="depends">init,deps-clean,-do-clean,-recompile-netbeans-tasks-after-clean,-post-clean</xsl:attribute>
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
            <echo level="warn" message="Cycle detected: {/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:name} was already built"/>
        </target>
        <target name="deps-{$kind}" depends="init,-deps-{$kind}-init">
            <xsl:attribute name="unless">no.deps</xsl:attribute>

            <mkdir dir="${{build.dir}}"/>
            <touch file="${{built-{$kind}.properties}}" verbose="false"/>
            <property file="${{built-{$kind}.properties}}" prefix="already.built.{$kind}."/>
            <!--<echo message="from deps-{$kind} of {/p:project/p:configuration/j2semodularproject1:data/j2semodularproject1:name}:"/><echoproperties prefix="already.built.{$kind}."/>-->
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
        <xsl:call-template name="createRootAvailableSet">
            <xsl:with-param name="roots" select="$roots"/>
            <xsl:with-param name="propName" select="$propName"/>
            <xsl:with-param name="suffix" select="'set'"/>
        </xsl:call-template>
        <!--
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <j2semodularproject1:modsource_regexp property="{$propName}.{@id}.regexp" modsource="${{{@id}.path}}"/>
            <dirset dir="${{basedir}}/${{{@id}}}" includes="*/*" id="{$propName}.{@id}.set">
                <filename regex="${{{$propName}.{@id}.regexp}}"/>
            </dirset>
        </xsl:for-each>
        <union id="{$propName}.set">
            <xsl:for-each select="$roots/j2semodularproject1:root">
                <dirset refid="{$propName}.{@id}.set"/>
            </xsl:for-each>
        </union>
        -->
        <xsl:element name="condition">
            <xsl:attribute name="property"><xsl:value-of select="$propName"/></xsl:attribute>
            <or>
                <xsl:for-each select="$roots/j2semodularproject1:root">
                    <resourcecount when="greater" count="0">
                        <union refid="{$propName}.set"/>
                    </resourcecount>
                </xsl:for-each>
            </or>
        </xsl:element>
    </xsl:template>
    
    <xsl:template name="createRootAvailableSet">
        <xsl:param name="roots"/>
        <xsl:param name="propName"/>
        <xsl:param name="suffix" select="'set'"/>
        <xsl:param name="filter"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <j2semodularproject1:modsource_regexp property="{$propName}.{@id}.regexp" modsource="${{{@id}.path}}"/>
            <dirset dir="${{basedir}}/${{{@id}}}" includes="*/*" id="{$propName}.{@id}.{$suffix}">
                <filename regex="${{{$propName}.{@id}.regexp}}"/>
                <xsl:if test="$filter">
                    <xsl:copy-of select="$filter"/>
                </xsl:if>
            </dirset>
        </xsl:for-each>
        <union id="{$propName}.{$suffix}">
            <xsl:for-each select="$roots/j2semodularproject1:root">
                <dirset refid="{$propName}.{@id}.{$suffix}"/>
            </xsl:for-each>
        </union>
    </xsl:template>
    
    <xsl:template name="createSourcePathValidityTest">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <xsl:element name="fail">
                <xsl:attribute name="unless"><xsl:value-of select="@id"/></xsl:attribute>
                <xsl:text>Must set </xsl:text><xsl:value-of select="@id"/>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <!-- Each file root has its own pattern for 'source' files, and must be copied
         with root-specific regexpmapper -->
    <xsl:template name="copyResources">
        <xsl:param name="todir"/>
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:param name="condition"/>
        <xsl:param name="regexp"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <j2semodularproject1:modsource_regexp property="{@id}.path.regexp" modsource="${{{@id}.path}}" filePattern="(.*$)"/>
            <echo message="Copying resources from ${{{@id}}}"/>
            <copy todir="{$todir}">
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
                <regexpmapper from="${{{@id}.path.regexp}}" to="\1/\3"/>
            </copy>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createOneFileSet">
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:param name="condition"/>
        
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
    </xsl:template>
    
    <xsl:template name="createFilesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:param name="condition"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <xsl:call-template name="createOneFileSet">
                <xsl:with-param name="includes" select="$includes"/>
                <xsl:with-param name="includes2" select="$includes2"/>
                <xsl:with-param name="excludes" select="$excludes"/>
                <xsl:with-param name="condition" select="$condition"/>
            </xsl:call-template>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="createMappedResources">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="includes2"/>
        <xsl:param name="excludes"/>
        <xsl:param name="condition"/>
        <xsl:param name="setprefix"/>
        <mappedresources>
            <union>
                <xsl:for-each select="$roots/j2semodularproject1:root">
                    <xsl:call-template name="createOneFileSet">
                        <xsl:with-param name="includes" select="$includes"/>
                        <xsl:with-param name="includes2" select="$includes2"/>
                        <xsl:with-param name="excludes" select="$excludes"/>
                        <xsl:with-param name="condition">
                            <xsl:copy-of select="$condition"/>
                            <filename regex="${{{$setprefix}.{@id}.regexp}}"/>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:for-each>
            </union>
            <xsl:for-each select="$roots/j2semodularproject1:root">
                <regexpmapper from="${{{$setprefix}.{@id}.regexp}}\Q${{file.separator}}\E(.*)$" to="\3"/>
            </xsl:for-each>
        </mappedresources>
    </xsl:template>
    
    <xsl:template name="createPackagesets">
        <xsl:param name="roots"/>
        <xsl:param name="includes" select="'${includes}'"/>
        <xsl:param name="excludes"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
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
        <xsl:for-each select="$locations/j2semodularproject1:root">
            <xsl:element name="pathelement">
                <xsl:attribute name="location"><xsl:text>${</xsl:text><xsl:value-of select="@id"/><xsl:text>}</xsl:text></xsl:attribute>
            </xsl:element>
        </xsl:for-each>
    </xsl:template>
    
    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>						
    </xsl:template>

    <xsl:template name="createModulePath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/j2semodularproject1:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}/*/</xsl:text>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>.path}</xsl:text>
        </xsl:for-each>						
    </xsl:template>
</xsl:stylesheet>
