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

<project name="Mac Installer Properties" basedir="." >
   
    <property name="translatedfiles.src" value="${basedir}/../../../src"/>
        
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->       
    <property name="nbide.version" value="${versionnumber}"/>
    <property name="nbide.id" value="${nbide.version}"/>
    <property name="appname" value="Apache NetBeans ${nbide.version}"/> 
    <property name="mpkg.name_nb" value="${appname}"/> 
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb"/>      
    <property name="nb.check.build.number" value="0"/>

    <!-- Unique ID in db/receipts for Development builds -->
    <!--<property name="nb.id" value="${nbide.id}-${buildnumber}"/>-->
    <!-- Unique ID in db/receipts for release build -->
    <property name="nb.id" value="${nbide.id}"/>

    <property name="appversion" value="${nbide.version}"/>
    <property name="nb.display.version.long"  value="${nbide.version}"/>
    <property name="nb.display.version.short" value="${nbide.version}"/>

    <!-- Tomcat properties   -->    
    <property name="tomcat.version" value="8.0.27"/>
    <property name="tomcat.id" value="8.0.27"/>
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-${tomcat.version}"/>
    <property name="tomcat_location" value="${binary_cache_host}/tomcat/apache-tomcat-${tomcat.version}.zip"/> 
            
    <!-- GlassFish 4 properties   -->   
    <property name="glassfish.build.type"      value=""/>
    <property name="glassfish.location.prefix" value="${gf_builds_host}/java/re/glassfish/4.1.1/promoted"/>
    
    <!--loadresource property="glassfish.build.number">
          <url url="${glassfish.location.prefix}/latest/archive/release"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)glassfish-4.1.1-a.zip(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <property name="glassfish.display.version" value="4.1.1"/>
    <property name="glassfish.version"      value="b${glassfish.build.number}"/>
    <property name="glassfish.id"           value="${glassfish.display.version}"/>
    <property name="glassfish.install.dir"  value="${install.dir}/glassfish-4.1.1"/>
    <property name="glassfish_location"     value="${glassfish.location.prefix}/${glassfish.build.type}/latest/archive/release/glassfish-4.1.1-a.zip"/>
    <property name="glassfish.subdir"       value="glassfish4"/-->
    
    <property name="dmg.prefix.name" value="${prefix}"/>                         

    <!-- Nested JRE Properties-->        
    <property name="jre.builds.path" value="${jre_builds_host}/${jre_builds_path}/latest/bundles/macosx-x64"/><!-- Change latest to fcs/b{proper buildnumber} -->
    <!-- e.g. 1.8.0 - jre-8u31-macosx-x64.tar.gz -->
    <!--loadresource property="jre.version.number">
          <url url="${jre.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jre-([0-9]+)([u]?)([0-9]*)-(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="jre.is.update">
          <url url="${jre.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jre-([0-9]+)([u]?)([0-9]*)-(.*)" replace="\3" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="jre.update.number.opt">
          <url url="${jre.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jre-([0-9]+)([u]?)([0-9]*)-(.*)" replace="\4" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    <condition property="jre.update.number" value="${jre.update.number.opt}" else="0">
        <equals arg1="${jre.is.update}" arg2="u"/>
    </condition>
    
    <echo message="Nested JRE Metadata: Version: ${jre.version.number} Update: ${jre.update.number}" />
    
    <condition property="jre.archiv.filename" value="jre-${jre.version.number}-macosx-x64.tar.gz" else="jre-${jre.version.number}u${jre.update.number}-macosx-x64.tar.gz">
        <equals arg1="${jre.update.number}" arg2="0"/>
    </condition>
    
    <condition property="jre.version.string.long" value="1.${jre.version.number}.0" else="1.${jre.version.number}.0_${jre.update.number}">
        <equals arg1="${jre.update.number}" arg2="0"/>
    </condition-->
    
    <!--condition property="jre.folder.name" value="jre1.${jre.version.number}.0.jre" else="jre1.${jre.version.number}.0_${jre.update.number}.jre">
        <equals arg1="${jre.update.number}" arg2="0"/>
    </condition-->       

    <!-- JDK Properties-->    
    <condition property="jdk_builds_path" value="${jdk7_builds_path}" else="${jdk11_builds_path}">
        <equals arg1="${build.jdk7}" arg2="1"/>
    </condition>
    
    <property name="jdk.builds.path" value="${jdk_builds_host}/${jdk_builds_path}/osx-x64"/>
    <!-- e.g. 1.7.0_55 - jdk-7u55-fcs-bin-b07-macosx-x64-04_feb_2014.dmg -->
    <!-- e.g. 1.8.0 - jdk-8-fcs-bin-b129-macosx-x64-06_feb_2014.dmg -->

    <!--e.g. jdk-11.0.1_osx-x64_bin.dmg -->
    <!--loadresource property="jdk.version.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([1-9][0-9]*).0.([1-9])([0-9]*)(_[a-z]+)-x64_bin(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="is.update">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)([u]?)([0-9]*)(-[a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\3" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="jdk.update.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([1-9][0-9]*).0.([1-9])([0-9]*)(_[a-z]+)-x64_bin(.*)" replace="\3" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <loadresource property="jdk.build.type">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)([u]?)([0-9]*)(-[a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\5" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    <condition property="jdk.ea.text" value="ea-" else="">
        <equals arg1="${jdk.build.type}" arg2="ea"/>
    </condition>
    
    
    <loadresource property="jdk.build.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)([u]?)([0-9]*)(-[a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\6" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource-->
    
    <echo message="JDK Metadata: Version: ${jdk.version.number} Update: ${jdk.update.number}"/><!-- Build: ${jdk.build.number} Build type: ${jdk.build.type}" /-->
    
    <property name="mpkg.prefix_nb_jdk" value=" with JDK"/> 
    <property name="jdk.bundle.files.suffix" value="nb-10_0"/>
    <property name="output.jdk.dir" value="jdk/"/>
    <condition property="jdk.bundle.files.prefix" value="jdk-${jdk.version.number}" else="jdk-${jdk.version.number}u${jdk.update.number}">
        <equals arg1="${jdk.update.number}" arg2="0"/>
    </condition>
    <condition property="mpkg.version_jdk" value=" ${jdk.version.number}" else=" ${jdk.version.number} Update ${jdk.update.number}">
        <equals arg1="${jdk.update.number}" arg2="0"/>
    </condition>
    <condition property="default.jdk.home" value="/Library/Java/JavaVirtualMachines/jdk1.${jdk.version.number}.0.jdk/Contents/Home"
                                           else="/Library/Java/JavaVirtualMachines/jdk1.${jdk.version.number}.0_${jdk.update.number}.jdk/Contents/Home">
        <equals arg1="${jdk.update.number}" arg2="0"/>
    </condition>
    <condition property="jdk_bits_location" value="${jdk_builds_host}/${jdk_builds_path}/osx-x64/jdk-${jdk.version.number}-${jdk.ea.text}macosx-x64.dmg"
                                           else="${jdk_builds_host}/${jdk_builds_path}/osx-x64/jdk-${jdk.version.number}.0.${jdk.update.number}_osx-x64_bin.dmg">
        <equals arg1="${jdk.update.number}" arg2="0"/>
    </condition>
    <condition property="jdk.update.number.long" value="0${jdk.update.number}" else="${jdk.update.number}">
        <length string="${jdk.update.number}" length="1"/>  
    </condition>
    <condition property="jdk.package.name" value="JDK\ ${jdk.version.number}"
                                           else="JDK\ ${jdk.version.number}\ Update\ ${jdk.update.number.long}">
        <equals arg1="${jdk.update.number}" arg2="0"/>
    </condition>

</project>
