<?xml version="1.0" encoding="UTF-8"?>
<!--
  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

  Copyright 1997-2015 Oracle and/or its affiliates. All rights reserved.

  Oracle and Java are registered trademarks of Oracle and/or its affiliates.
  Other names may be trademarks of their respective owners.

  The contents of this file are subject to the terms of either the GNU General Public
  License Version 2 only ("GPL") or the Common Development and Distribution
  License("CDDL") (collectively, the "License"). You may not use this file except in
  compliance with the License. You can obtain a copy of the License at
  http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
  License for the specific language governing permissions and limitations under the
  License.  When distributing the software, include this License Header Notice in
  each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
  designates this particular file as subject to the "Classpath" exception as provided
  by Oracle in the GPL Version 2 section of the License file that accompanied this code.
  If applicable, add the following below the License Header, with the fields enclosed
  by brackets [] replaced by your own identifying information:
  "Portions Copyrighted [year] [name of copyright owner]"
  
  Contributor(s):
  
  The Original Software is NetBeans. The Initial Developer of the Original Software
  is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
  Rights Reserved.
  
  If you wish your version of this file to be governed by only the CDDL or only the
  GPL Version 2, indicate your decision by adding "[Contributor] elects to include
  this software in this distribution under the [CDDL or GPL Version 2] license." If
  you do not indicate a single choice of license, a recipient has the option to
  distribute your version of this file under either the CDDL, the GPL Version 2 or
  to extend the choice of license to its licensees as provided above. However, if you
  add GPL Version 2 code and therefore, elected the GPL Version 2 license, then the
  option applies only if the new code is made subject to such option by the copyright
  holder.
-->

<project name="Mac Installer Properties" basedir="." >
   
    <property name="translatedfiles.src" value="${basedir}/../../../src"/>
        
    <property name="install.dir" value="/Applications/NetBeans"/>
    
    <!-- Base IDE properties   -->       
    <property name="baseide.version" value="Dev"/>
    <property name="baseide.id" value="Dev"/>
    <property name="appname" value="NetBeans Dev ${buildnumber}"/> 
    <property name="mpkg.name_nb" value="NetBeans Dev ${buildnumber}"/> 
    <property name="app.name" value="${install.dir}/${appname}.app"/>
    <property name="nbClusterDir" value="nb"/>      
    <property name="nb.check.build.number" value="0"/>

    <!-- Unique ID in db/receipts for Development builds -->
    <property name="nb.id" value="${baseide.id}-${buildnumber}"/>
    <!-- Unique ID in db/receipts for release build -->
    <!--<property name="nb.id" value="${baseide.id}"/>-->

    <property name="appversion" value="Development Version"/>
    <property name="nb.display.version.long"  value="Development Version ${buildnumber}"/>
    <property name="nb.display.version.short" value="Dev"/>

    <!-- Tomcat properties   -->    
    <property name="tomcat.version" value="8.0.27"/>
    <property name="tomcat.id" value="8.0.27"/>
    <property name="tomcat.install.dir" value="${install.dir}/apache-tomcat-${tomcat.version}"/>
    <property name="tomcat_location" value="${binary_cache_host}/tomcat/apache-tomcat-${tomcat.version}.zip"/> 
            
    <!-- GlassFish 4 properties   -->   
    <property name="glassfish.build.type"      value=""/>
    <property name="glassfish.location.prefix" value="${gf_builds_host}/java/re/glassfish/4.1.1/promoted"/>
    
    <loadresource property="glassfish.build.number">
          <url url="${glassfish.location.prefix}/latest/archive/release"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <!-- replaceregex pattern="(.*)glassfish-4.1.1-b([0-9a-z]+)\.zip(.*)" replace="\2" flags="g"/ -->
              <replaceregex pattern="(.*)glassfish-4.1.1-a.zip(.*)" replace="\2" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    
    <property name="glassfish.display.version" value="4.1.1"/>
    <property name="glassfish.version"      value="b${glassfish.build.number}"/>
    <property name="glassfish.id"           value="${glassfish.display.version}"/>
    <property name="glassfish.install.dir"  value="${install.dir}/glassfish-4.1.1"/>
    <!-- property name="glassfish_location"     value="${glassfish.location.prefix}/${glassfish.build.type}/${glassfish.version}/archive/bundles/glassfish-4.1.1-${glassfish.version}.zip"/ -->
    <property name="glassfish_location"     value="${glassfish.location.prefix}/${glassfish.build.type}/latest/archive/release/glassfish-4.1.1-a.zip"/>
    <property name="glassfish.subdir"       value="glassfish4"/>
    
    <property name="dmg.prefix.name" value="${prefix}-${buildnumber}"/>                         

    <!-- Nested JRE Properties-->        
    <property name="jre.builds.path" value="${jdk_builds_host}/${jre_builds_path}/latest/bundles/macosx-x64"/><!-- Change latest to fcs/b{proper buildnumber} -->
    <!-- e.g. 1.8.0 - jre-8u31-macosx-x64.tar.gz -->
    <loadresource property="jre.version.number">
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
    </condition>
    
    <!--condition property="jre.folder.name" value="jre1.${jre.version.number}.0.jre" else="jre1.${jre.version.number}.0_${jre.update.number}.jre">
        <equals arg1="${jre.update.number}" arg2="0"/>
    </condition-->       

    <!-- JDK Properties-->    
    <condition property="jdk_builds_path" value="${jdk7_builds_path}" else="${jdk8_builds_path}">
        <equals arg1="${build.jdk7}" arg2="1"/>
    </condition>
    
    <property name="jdk.builds.path" value="${jdk_builds_host}/${jdk_builds_path}/latest/bundles/macosx-x64"/>
    <!-- e.g. 1.7.0_55 - jdk-7u55-fcs-bin-b07-macosx-x64-04_feb_2014.dmg -->
    <!-- e.g. 1.8.0 - jdk-8-fcs-bin-b129-macosx-x64-06_feb_2014.dmg -->
    <loadresource property="jdk.version.number">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)([u]?)([0-9]*)(-[a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\2" flags="g"/>
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
    
    <loadresource property="jdk.update.number.opt">
          <url url="${jdk.builds.path}"/>
          <filterchain>
            <striplinebreaks/>
            <tokenfilter>
              <replaceregex pattern="(.*)jdk-([0-9]+)([u]?)([0-9]*)(-[a-z]+)-bin-b(([0-9]+)+)-(.*)" replace="\4" flags="g"/>
            </tokenfilter>
          </filterchain>
    </loadresource>
    <condition property="jdk.update.number" value="${jdk.update.number.opt}" else="0">
        <equals arg1="${is.update}" arg2="u"/>
    </condition>
    
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
    </loadresource>
    
    <echo message="JDK Metadata: Version: ${jdk.version.number} Update: ${jdk.update.number} Build: ${jdk.build.number} Build type: ${jdk.build.type}" />
    
    <property name="mpkg.prefix_nb_jdk" value=" with JDK"/> 
    <property name="jdk.bundle.files.suffix" value="nb-dev"/>
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
    <condition property="jdk_bits_location" value="${jdk_builds_host}/${jdk_builds_path}/all/b${jdk.build.number}/bundles/macosx-x64/jdk-${jdk.version.number}-${jdk.ea.text}macosx-x64.dmg"
                                           else="${jdk_builds_host}/${jdk_builds_path}/all/b${jdk.build.number}/bundles/macosx-x64/jdk-${jdk.version.number}u${jdk.update.number}-${jdk.ea.text}macosx-x64.dmg">
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
