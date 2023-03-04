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
                xmlns:j2se="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:j2seproject="http://www.netbeans.org/ns/j2se-project/1"
                xmlns:projdeps="http://www.netbeans.org/ns/ant-project-references/1"
                exclude-result-prefixes="xalan p j2se projdeps">
<!-- XXX should use namespaces for NB in-VM tasks from ant/browsetask and profilerjpda/ant (Ant 1.6.1 and higher only) -->
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

    <xsl:comment><![CDATA[
*** GENERATED FROM project.xml - DO NOT EDIT  ***
***         EDIT ../build.xml INSTEAD         ***

For the purpose of easier reading the script
is divided into following sections:

  - initialization
  - profiling
  - applet profiling

]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/j2seproject:data/j2seproject:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-profiler-impl">
            <xsl:attribute name="default">profile</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

            <target name="default">
                <xsl:attribute name="depends">profile</xsl:attribute>
                <xsl:attribute name="description">Build and profile the project.</xsl:attribute>
            </target>

    <xsl:comment>
    ======================
    INITIALIZATION SECTION
    ======================
    </xsl:comment>

            <target name="profile-init" depends="-profile-pre-init, init, -profile-post-init, -profile-init-macrodef-profile, -profile-init-check"/>

            <target name="-profile-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-profile-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-profile-init-macrodef-profile">
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
                        <java fork="true" classname="@{{classname}}" dir="${{profiler.info.dir}}" jvm="${{profiler.info.jvm}}">
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
                <fail unless="profiler.info.jvm">Must set JVM to use for profiling in profiler.info.jvm</fail>
                <fail unless="profiler.info.jvmargs.agent">Must set profiler agent JVM arguments in profiler.info.jvmargs.agent</fail>
            </target>

    <xsl:comment>
    =================
    PROFILING SECTION
    =================
    </xsl:comment>

            <target name="profile">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">profile-init,compile</xsl:attribute>
                <xsl:attribute name="description">Profile a project in the IDE.</xsl:attribute>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.classpath}}"/>
                    </classpath>
                </nbprofiledirect>
                <profile/>
            </target>

            <target name="profile-single">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">profile-init,compile-single</xsl:attribute>
                <xsl:attribute name="description">Profile a selected class in the IDE.</xsl:attribute>
                <fail unless="profile.class">Must select one file in the IDE or set profile.class</fail>
                <nbprofiledirect>
                    <classpath>
                        <path path="${{run.classpath}}"/>
                    </classpath>
                </nbprofiledirect>
              <profile classname="${{profile.class}}"/>
            </target>

    <xsl:comment>
    =========================
    APPLET PROFILING  SECTION
    =========================
    </xsl:comment>

            <target name="profile-applet">
                <xsl:attribute name="if">netbeans.home</xsl:attribute>
                <xsl:attribute name="depends">profile-init,compile-single</xsl:attribute>
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

    <xsl:comment>
    =========================
    TESTS PROFILING  SECTION
    =========================
    </xsl:comment>

          <target name="profile-test-single">
              <xsl:attribute name="if">netbeans.home</xsl:attribute>
              <xsl:attribute name="depends">profile-init,compile-test-single</xsl:attribute>
              <nbprofiledirect>
                  <classpath>
                      <path path="${{run.test.classpath}}"/>
                  </classpath>
              </nbprofiledirect>

              <junit showoutput="true" fork="true" dir="${{profiler.info.dir}}"  jvm="${{profiler.info.jvm}}" failureproperty="tests.failed" errorproperty="tests.failed">
                  <env key="${{profiler.info.pathvar}}" path="${{profiler.info.agentpath}}:${{profiler.current.path}}"/>
                  <jvmarg value="${{profiler.info.jvmargs.agent}}" />
                  <jvmarg line="${{profiler.info.jvmargs}}"/>
                  <test name="${{profile.class}}"/>
                  <classpath>
                      <path path="${{run.test.classpath}}"/>
                  </classpath>
                  <syspropertyset>
                      <propertyref prefix="test-sys-prop."/>
                      <mapper type="glob" from="test-sys-prop.*" to="*"/>
                  </syspropertyset>
                  <formatter type="brief" usefile="false"/>
                  <formatter type="xml"/>
              </junit>
          </target>

  </project>
  </xsl:template>
</xsl:stylesheet>
