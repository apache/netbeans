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
                xmlns:webproject1="http://www.netbeans.org/ns/web-project/1"
                xmlns:webproject2="http://www.netbeans.org/ns/web-project/2"
                xmlns:webproject3="http://www.netbeans.org/ns/web-project/3"
                exclude-result-prefixes="xalan p webproject1 webproject2 webproject3">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:variable name="name" select="/p:project/p:configuration/webproject3:data/webproject3:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project>
            <target name="-groovy-init-macrodef-javac">
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
                    <attribute>
                        <xsl:attribute name="name">processorpath</xsl:attribute>
                        <xsl:attribute name="default">${javac.processorpath}</xsl:attribute>
                    </attribute>
                    <attribute>
                        <xsl:attribute name="name">apgeneratedsrcdir</xsl:attribute>
                        <xsl:attribute name="default">${build.generated.sources.dir}/ap-source-output</xsl:attribute>
                    </attribute>
                    <element>
                        <xsl:attribute name="name">customize</xsl:attribute>
                        <xsl:attribute name="optional">true</xsl:attribute>
                    </element>
                    <sequential>
                        <taskdef>
                            <xsl:attribute name="name">groovyc</xsl:attribute>
                            <xsl:attribute name="classpath">${javac.classpath}:${j2ee.platform.classpath}</xsl:attribute>
                            <xsl:attribute name="classname">org.codehaus.groovy.ant.Groovyc</xsl:attribute>
                        </taskdef>
                        <property name="empty.dir" location="${{build.dir}}/empty"/>
                        <mkdir dir="${{empty.dir}}"/>
                        <groovyc>
                            <xsl:attribute name="javahome">${platform.home}</xsl:attribute>
                            <xsl:attribute name="srcdir">@{srcdir}</xsl:attribute>
                            <xsl:attribute name="sourcepath">@{sourcepath}</xsl:attribute>
                            <xsl:attribute name="destdir">@{destdir}</xsl:attribute>
                            <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                            <xsl:attribute name="excludes">@{excludes}</xsl:attribute>
                            <xsl:attribute name="includeAntRuntime">false</xsl:attribute>
                            <xsl:attribute name="fork">true</xsl:attribute>
                            <src>
                                <dirset dir="@{{gensrcdir}}" erroronmissingdir="false">
                                    <include name="*"/>
                                </dirset>
                            </src>
                            <classpath>
                                <path path="@{{classpath}}"/>
                            </classpath>
                            <javac>
                                <xsl:attribute name="debug">@{debug}</xsl:attribute>
                                <xsl:attribute name="deprecation">${javac.deprecation}</xsl:attribute>
                                <xsl:attribute name="encoding">${source.encoding}</xsl:attribute>
                                <xsl:if test ="not(/p:project/p:configuration/webproject3:data/webproject3:explicit-platform/@explicit-source-supported ='false')">
                                    <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                    <xsl:attribute name="target">${javac.target}</xsl:attribute>
                                </xsl:if>
                                <xsl:if test="/p:project/p:configuration/webproject3:data/webproject3:explicit-platform">
                                    <xsl:attribute name="fork">yes</xsl:attribute>
                                    <xsl:attribute name="executable">${platform.javac}</xsl:attribute>
                                    <xsl:attribute name="tempdir">${java.io.tmpdir}</xsl:attribute>
                                </xsl:if>

                                <compilerarg line="${{javac.compilerargs}} ${{javac.compilerargs.jaxws}}"/>
                                <customize/>
                            </javac>
                        </groovyc>
                    </sequential>
                </macrodef>
                <macrodef>
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
                <macrodef>
                    <xsl:attribute name="name">force-recompile</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/web-project/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">destdir</xsl:attribute>
                        <xsl:attribute name="default">${build.classes.dir}</xsl:attribute>
                    </attribute>
                    <sequential>
                        <fail unless="javac.includes">Must set javac.includes</fail>
                        <pathconvert>
                            <xsl:attribute name="property">javac.includes.binary</xsl:attribute>
                            <xsl:attribute name="pathsep">,</xsl:attribute>
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
                        <delete>
                            <files includes="${{javac.includes.binary}}"/>
                        </delete>
                    </sequential>
                </macrodef>
            </target>

            <!--                    -->
            <!--    Test project    -->
            <!--                    -->
            <target depends="init,compile-test,-pre-test-run" if="have.tests" name="-do-test-run-with-groovy">
                <webproject2:test testincludes=""/>
            </target>
            <target depends="init,compile-test,-pre-test-run,-do-test-run-with-groovy" if="have.tests" name="-post-test-run-with-groovy">
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            <target depends="init,compile-test,-pre-test-run,-do-test-run-with-groovy,test-report,-post-test-run-with-groovy,-test-browse" description="Run unit tests." name="test-with-groovy"/>

            <!--                                        -->
            <!--    Single groovy file test runner      -->
            <!--                                        -->
            <target depends="init,compile-test-single,-pre-test-run-single" if="have.tests" name="-do-test-run-single-groovy">
                <fail unless="test.binarytestincludes">Must select some files in the IDE or set test.includes</fail>
                <webproject2:test testincludes=""/>
            </target>
            <target depends="init,compile-test-single,-pre-test-run-single,-do-test-run-single-groovy" if="have.tests" name="-post-test-run-single-groovy">
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            <target depends="init,compile-test-single,-pre-test-run-single,-do-test-run-single-groovy,-post-test-run-single-groovy" description="Run single unit test." name="test-single-groovy"/>

            <!--                                             -->
            <!--    Single groovy file debug test runner     -->
            <!--                                             -->
            <target depends="init,compile-test-single,-pre-test-run-single,-debug-start-debugger-test" name="-do-test-debug-single-groovy">
                <fail unless="test.binarytestincludes">Must select some files in the IDE or set test.binarytestincludes</fail>
                <webproject2:test-debug testincludes="" />
            </target>
            <target depends="init,compile-test-single,-pre-test-run-single,-do-test-debug-single-groovy" if="have.tests" name="-post-test-debug-single-groovy">
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            <target depends="init,compile-test-single,-pre-test-run-single,-debug-start-debugger-test,-do-test-debug-single-groovy,-post-test-debug-single-groovy" name="debug-single-groovy"/>

        </project>
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