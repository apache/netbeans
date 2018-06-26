<?xml version="1.0" encoding="UTF-8"?>
<!--
DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.

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

Contributor(s):
-->
<project default="-deploy-ant" basedir=".">
    <target name="-init-cl-deployment-env" if="deploy.ant.enabled">
        <property file="${deploy.ant.properties.file}" />
        <available file="${deploy.ant.docbase.dir}/WEB-INF/sun-web.xml" property="sun.web.present"/>
        <available file="${deploy.ant.docbase.dir}/WEB-INF/glassfish-web.xml" property="glassfish.web.present"/>
        <available file="${deploy.ant.resource.dir}" property="has.setup"/>
        <tempfile prefix="gfv3" property="gfv3.password.file" destdir="${java.io.tmpdir}"/>  <!-- do not forget to delete this! -->
        <echo message="AS_ADMIN_PASSWORD=${gfv3.password}" file="${gfv3.password.file}"/>
    </target>

    <target name="-parse-sun-web" depends="-init-cl-deployment-env" if="sun.web.present">
        <tempfile prefix="gfv3" property="temp.sun.web" destdir="${java.io.tmpdir}"/>
        <copy file="${deploy.ant.docbase.dir}/WEB-INF/sun-web.xml" tofile="${temp.sun.web}"/>
        <!-- The doctype triggers resolution which can fail -->
        <replace file="${temp.sun.web}">
            <replacetoken><![CDATA[<!DOCTYPE]]></replacetoken>
            <replacevalue><![CDATA[<!-- <!DOCTYPE]]></replacevalue>
        </replace>
        <replace file="${temp.sun.web}">
            <replacetoken><![CDATA[<sun-web-app]]></replacetoken>
            <replacevalue><![CDATA[--> <sun-web-app]]></replacevalue>
        </replace>
        <xmlproperty file="${temp.sun.web}" validate="false">
        </xmlproperty>    
        <delete file="${temp.sun.web}"/>
        <condition property="deploy.ant.client.url" value="${gfv3.url}${sun-web-app.context-root}" else="${gfv3.url}/${ant.project.name}">
            <isset property="sun-web-app.context-root"/>
        </condition>
        <condition property="deploy.context.root.argument" value="&amp;contextroot=${sun-web-app.context-root}" else="/${ant.project.name}">
            <isset property="sun-web-app.context-root"/>
        </condition>
    </target>
    <target name="-parse-glassfish-web" depends="-init-cl-deployment-env" if="glassfish.web.present">
        <tempfile prefix="gfv3" property="temp.gf.web" destdir="${java.io.tmpdir}"/>
        <copy file="${deploy.ant.docbase.dir}/WEB-INF/glassfish-web.xml" tofile="${temp.gf.web}"/>
        <!-- The doctype triggers resolution which can fail -->
        <replace file="${temp.gf.web}">
            <replacetoken><![CDATA[<!DOCTYPE]]></replacetoken>
            <replacevalue><![CDATA[<!-- <!DOCTYPE]]></replacevalue>
        </replace>
        <replace file="${temp.gf.web}">
            <replacetoken><![CDATA[<glassfish-web-app]]></replacetoken>
            <replacevalue><![CDATA[--> <glassfish-web-app]]></replacevalue>
        </replace>
        <xmlproperty file="${temp.gf.web}" validate="false">
        </xmlproperty>
        <delete file="${temp.gf.web}"/>
        <condition property="deploy.ant.client.url" value="${gfv3.url}${glassfish-web-app.context-root}" else="${gfv3.url}/${ant.project.name}">
            <isset property="glassfish-web-app.context-root"/>
        </condition>
        <condition property="deploy.context.root.argument" value="&amp;contextroot=${glassfish-web-app.context-root}" else="/${ant.project.name}">
            <isset property="glassfish-web-app.context-root"/>
        </condition>
    </target>
    <target name="-no-parse-sun-web" depends="-init-cl-deployment-env" unless="sun.web.present">
        <property name="deploy.context.root.argument" value=""/>
    </target>
    <target name="-add-resources" depends="-init-cl-deployment-env" if="has.setup">
        <tempfile prefix="gfv3" property="gfv3.resources.dir" destdir="${java.io.tmpdir}"/>
        <mkdir dir="${gfv3.resources.dir}"/>
        <mkdir dir="${gfv3.resources.dir}/META-INF"/>
        <copy todir="${gfv3.resources.dir}/META-INF">
            <fileset dir="${deploy.ant.resource.dir}"/>
        </copy>
        <jar destfile="${deploy.ant.archive}" update="true">
            <fileset dir="${gfv3.resources.dir}"/>
        </jar>
        <delete dir="${gfv3.resources.dir}"/>
    </target>
    <target name="-deploy-ant" depends="-parse-glassfish-web, -parse-sun-web, -no-parse-sun-web,-add-resources" if="deploy.ant.enabled">
        <antcall target="-deploy-without-pw"/>
        <antcall target="-deploy-with-pw"/>
    </target>

    <target name="-deploy-without-pw" unless="gfv3.password">
        <echo message="Deploying ${deploy.ant.archive}"/>
        <tempfile prefix="gfv3" property="gfv3.results.file" destdir="${java.io.tmpdir}"/>  <!-- do not forget to delete this! -->
        <property name="full.deploy.ant.archive" location="${deploy.ant.archive}"/>
        <get src="${gfv3.admin.url}/__asadmin/deploy?path=${full.deploy.ant.archive}${deploy.context.root.argument}&amp;force=true&amp;name=${ant.project.name}"
            dest="${gfv3.results.file}"/>
        <delete file="${gfv3.results.file}"/>    
    </target>
    <target name="-deploy-with-pw" if="gfv3.password">
        <echo message="Deploying ${deploy.ant.archive}"/>
        <tempfile prefix="gfv3" property="gfv3.results.file" destdir="${java.io.tmpdir}"/>  <!-- do not forget to delete this! -->
        <property name="full.deploy.ant.archive" location="${deploy.ant.archive}"/>
        <get username="${gfv3.username}" password="${gfv3.password}" src="${gfv3.admin.url}/__asadmin/deploy?path=${full.deploy.ant.archive}${deploy.context.root.argument}&amp;force=true&amp;name=${ant.project.name}"
            dest="${gfv3.results.file}"/>
        <delete file="${gfv3.results.file}"/>
    </target>
    <target name="-undeploy-ant" depends="-init-cl-deployment-env" if="deploy.ant.enabled">
        <antcall target="-undeploy-without-pw"/>
        <antcall target="-undeploy-with-pw"/>
    </target>

    <target name="-undeploy-without-pw" unless="gfv3.password">
        <echo message="Undeploying ${deploy.ant.archive}"/>
        <tempfile prefix="gfv3" property="gfv3.results.file" destdir="${java.io.tmpdir}"/>  <!-- do not forget to delete this! -->
        <get src="${gfv3.admin.url}/__asadmin/undeploy?name=${ant.project.name}"
            dest="${gfv3.results.file}"/>
        <delete file="${gfv3.results.file}"/>    
    </target>
    <target name="-undeploy-with-pw" if="gfv3.password">
        <echo message="Undeploying ${deploy.ant.archive}"/>
        <tempfile prefix="gfv3" property="gfv3.results.file" destdir="${java.io.tmpdir}"/>  <!-- do not forget to delete this! -->
        <get username="${gfv3.username}" password="${gfv3.password}" src="${gfv3.admin.url}/__asadmin/undeploy?name=${ant.project.name}"
            dest="${gfv3.results.file}"/>
        <delete file="${gfv3.results.file}"/>
    </target>
</project>
