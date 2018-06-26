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

]]></xsl:comment>

        <xsl:variable name="name" select="/p:project/p:configuration/j2seproject:data/j2seproject:name"/>
        <!-- Synch with build-impl.xsl: -->
        <xsl:variable name="codename" select="translate($name, ' ', '_')"/>
        <project name="{$codename}-profiler-impl">
            <xsl:attribute name="default">profile-j2ee</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

            <target name="default">
                <xsl:attribute name="depends">profile-j2ee</xsl:attribute>
                <xsl:attribute name="description">Build and profile the project.</xsl:attribute>
            </target>

	<xsl:comment>
    ======================
    INITIALIZATION SECTION
    ======================
    </xsl:comment>

            <target name="profile-init" depends="-profile-pre-init, init, -profile-post-init, -profile-init-check"/>

            <target name="-profile-pre-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>

            <target name="-profile-post-init">
                <xsl:comment> Empty placeholder for easier customization. </xsl:comment>
                <xsl:comment> You can override this target in the ../build.xml file. </xsl:comment>
            </target>
            <target name="-profile-init-check">
                <xsl:attribute name="depends">-profile-pre-init, init, -profile-post-init</xsl:attribute>
                <fail unless="profiler.info.jvm">Must set JVM to use for profiling in profiler.info.jvm</fail>
                <fail unless="profiler.info.jvmargs.agent">Must set profiler agent JVM arguments in profiler.info.jvmargs.agent</fail>
            </target>

	<xsl:comment>
    =================
    PROFILING SECTION
    =================
    </xsl:comment>

            <target name="profile-j2ee">
                <xsl:attribute name="description">Profile a J2EE project in the IDE.</xsl:attribute>
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
                    <xsl:attribute name="target">start-loadgen</xsl:attribute>
                </antcall>
            </target>
            
            <target name="start-profiled-server">
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
            
            <target name="start-profiled-server-extraargs">
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

            <target name="start-loadgen" if="profiler.loadgen.path">
                    <loadgenstart>
                        <xsl:attribute name="path">${profiler.loadgen.path}</xsl:attribute>
                    </loadgenstart>
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

  </project>
  </xsl:template>
</xsl:stylesheet>
