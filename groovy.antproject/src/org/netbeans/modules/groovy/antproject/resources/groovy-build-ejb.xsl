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
                xmlns:p="http://www.netbeans.org/ns/project/1"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:ejbjarproject1="http://www.netbeans.org/ns/j2ee-ejbjarproject/1"
                xmlns:ejbjarproject2="http://www.netbeans.org/ns/j2ee-ejbjarproject/2"
                xmlns:ejbjarproject3="http://www.netbeans.org/ns/j2ee-ejbjarproject/3"
                exclude-result-prefixes="xalan p ejbjarproject1 ejbjarproject2 ejbjarproject3">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
    <xsl:template match="/">

        <xsl:variable name="name" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:name"/>
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project>
            <target name="-groovy-init-macrodef-javac">
                <macrodef>
                    <xsl:attribute name="name">javac</xsl:attribute>
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
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
                                <xsl:if test ="not(/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform/@explicit-source-supported ='false')">
                                    <xsl:attribute name="source">${javac.source}</xsl:attribute>
                                    <xsl:attribute name="target">${javac.target}</xsl:attribute>
                                </xsl:if>
                                <xsl:if test="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:explicit-platform">
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/2</xsl:attribute>
                    <attribute>
                        <xsl:attribute name="name">srcdir</xsl:attribute>
                        <xsl:attribute name="default">
                            <xsl:call-template name="createPath">
                                <xsl:with-param name="roots" select="/p:project/p:configuration/ejbjarproject3:data/ejbjarproject3:source-roots"/>
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
                    <xsl:attribute name="uri">http://www.netbeans.org/ns/j2ee-ejbjarproject/2</xsl:attribute>
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
                <ejbjarproject2:test testincludes=""/>
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
                <ejbjarproject2:test testincludes=""/>
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
                <ejbjarproject2:test-debug testincludes="" />
            </target>
            <target depends="init,compile-test-single,-pre-test-run-single,-do-test-debug-single-groovy" if="have.tests" name="-post-test-debug-single-groovy">
                <fail if="tests.failed" unless="ignore.failing.tests">Some tests failed; see details above.</fail>
            </target>
            <target depends="init,compile-test-single,-pre-test-run-single,-debug-start-debugger-test,-do-test-debug-single-groovy,-post-test-debug-single-groovy" name="debug-single-groovy"/>

        </project>
    </xsl:template>

    <xsl:template name="createPath">
        <xsl:param name="roots"/>
        <xsl:for-each select="$roots/ejbjarproject3:root">
            <xsl:if test="position() != 1">
                <xsl:text>:</xsl:text>
            </xsl:if>
            <xsl:text>${</xsl:text>
            <xsl:value-of select="@id"/>
            <xsl:text>}</xsl:text>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>