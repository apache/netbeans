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
    xmlns:jaxws="http://www.netbeans.org/ns/jax-ws/1"
    exclude-result-prefixes="xalan p projdeps">
    <xsl:output method="xml" indent="yes" encoding="UTF-8" xalan:indent-amount="4"/>
   
    <xsl:template match="/">
        <project>
            <xsl:attribute name="default">-post-run-deploy</xsl:attribute>
            <xsl:attribute name="basedir">..</xsl:attribute>

            <target name="-wsit-init">
                <property file="nbproject/private/private.properties"/>
                <condition property="user.properties.file" value="${{netbeans.user}}/build.properties">
                    <not>
                        <isset property="user.properties.file"/>
                    </not>
                </condition>
                <property file="${{deploy.ant.properties.file}}"/>
                <!-- GFv2 -->
                <condition property="appserver.root" value="${{sjsas.root}}">
                    <isset property="sjsas.root"/>
                </condition>
                <condition property="appserver.password" value="${{sjsas.password}}">
                    <isset property="sjsas.password"/>
                </condition>
                <!-- GFv3 -->
                <condition property="appserver.root" value="${{gfv3.root}}">
                    <isset property="gfv3.root"/>
                </condition>
                <condition property="appserver.password" value="${{gfv3.password}}">
                    <isset property="gfv3.password"/>
                </condition>

                <!-- fallback -->
                <condition property="appserver.password" value="changeit">
                    <not><isset property="appserver.password"/></not>
                </condition>
                <fail unless="user.properties.file">Must set user properties file</fail>
                <fail unless="appserver.root">Must set Sun app server root</fail>
                <fail unless="appserver.password">Must set Sun app server password</fail>
            </target>

            <target name="-create-wsit-prop" unless="do.not.create.wsit.prop">  
                <echo file="nbproject/wsit.properties" message="AS_ADMIN_USERPASSWORD=${{appserver.password}}"/>
            </target>

            <target name="-delete-create-wsit-file" if="user.created">
                <delete file="nbproject/wsit.createuser"/>
            </target>

            <target name="create-user" unless="user.exists">  
                <exec timeout="10000" outputproperty="creation.out" executable="${{appserver.root}}/bin/asadmin" failonerror="false" failifexecutionfails="false" osfamily="unix">
                    <arg value="create-file-user"/>
                    <arg value="--passwordfile"/>
                    <arg value="nbproject/wsit.properties"/>
                    <arg value="wsitUser"/>
                </exec>
                <exec timeout="10000" outputproperty="creation.out" executable="${{appserver.root}}/bin/asadmin" failonerror="false" failifexecutionfails="false" osfamily="mac">
                    <arg value="create-file-user"/>
                    <arg value="--passwordfile"/>
                    <arg value="nbproject/wsit.properties"/>
                    <arg value="wsitUser"/>
                </exec>
                <exec timeout="10000" outputproperty="creation.out" executable="${{appserver.root}}/bin/asadmin.bat" failonerror="false" failifexecutionfails="false" osfamily="windows">
                    <arg value="create-file-user"/>
                    <arg value="--passwordfile"/>
                    <arg value="nbproject/wsit.properties"/>
                    <arg value="wsitUser"/>
                </exec>
                <condition property="user.created">
                    <and>
                        <contains string="${{creation.out}}" substring="create-file-user"/>
                        <contains string="${{creation.out}}" substring="success"/>
                    </and>
                </condition>
                <antcall target="-delete-create-wsit-file"/>
            </target>

            <target name="-do-create-user" if="do-create-user">
                <available property="do.not.create.wsit.prop" file="nbproject/wsit.properties"/>
                <antcall target="-create-wsit-prop"/>

                <exec timeout="10000" outputproperty="as.users" executable="${{appserver.root}}/bin/asadmin" failonerror="false" failifexecutionfails="false" osfamily="unix">
                    <arg value="list-file-users"/>
                </exec>
                <exec timeout="10000" outputproperty="as.users" executable="${{appserver.root}}/bin/asadmin" failonerror="false" failifexecutionfails="false" osfamily="mac">
                    <arg value="list-file-users"/>
                </exec>
                <exec timeout="10000" outputproperty="as.users" executable="${{appserver.root}}/bin/asadmin.bat" failonerror="false" failifexecutionfails="false" osfamily="windows">
                    <arg value="list-file-users"/>
                </exec>

                <condition property="user.exists">
                    <contains string="${{as.users}}" substring="wsitUser"/>
                </condition>

                <antcall target="create-user"/>
            </target>

            <target name="-post-run-deploy" depends="-wsit-init">
                <available property="do-create-user" file="nbproject/wsit.createuser"/>
                <antcall target="-do-create-user"/>        
            </target>

        </project>
    </xsl:template>
</xsl:stylesheet>
