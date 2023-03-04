/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.test.installer;

import java.util.logging.Logger;
import java.util.*;

/**
 *

 */
public class InstallerAndUninstallerAllTest
{
  @org.junit.Test
  public void testInstaller( )
  {
    String sInstallerType = System.getProperty(
        "test.installer.bundle.name.suffix",
        "all"
      );
    //Properties pp = System.getProperties( );
    //pp.list( System.out );

      Installer I = null;

    if( sInstallerType.equals( "all" ) )
      I = new Installer( );
    else
    if( sInstallerType.equals( "javase" ) )
      I = new TestInstallerAndUninstallerJavaSE( );
    else
    if( sInstallerType.equals( "java" ) )
      I = new TestInstallerAndUninstallerJava( );
    else
    if( sInstallerType.equals( "ruby" ) )
      I = new TestInstallerAndUninstallerRuby( );
    else
    if( sInstallerType.equals( "cpp" ) )
      I = new TestInstallerAndUninstallerCPP( );
    else
    if( sInstallerType.equals( "php" ) )
      I = new TestInstallerAndUninstallerPHP( );
    else
    if( sInstallerType.equals( "javafx" ) )
      I = new TestInstallerAndUninstallerJavaFX( );

    I.testInstaller( );
  }

    public static void main(String[] args) {
        org.junit.runner.JUnitCore.runClasses(InstallerAndUninstallerAllTest.class);
    }
}

/*
Sample start:
set WORKSPACE=path_to_workspace
ant -Djavac.classpath=.:../../jemmy/external/jemmy-2.3.0.0.jar -Dtest-sys-prop.test.installer.url.prefix=http://builds/netbeans/trunk/latest_daily -Dtest-sys-prop.test.installer.bundle.name.prefix=netbeans-trunk-nightly -Dtest-sys-prop.test.use.build.number=true -Dtest-sys-prop.test.installer.bundle.name.suffix=php test


ant test
-Dtest-sys-prop.test.installer.url.prefix=http://builds/netbeans/trunk/latest_daily
-Dtest-sys-prop.test.installer.bundle.name.prefix=netbeans-trunk-nightly
-Dtest-sys-prop.test.use.build.number=true
-Dtest-sys-prop.test.installer.bundle.name.suffix=javase
-Dtest-sys-prop.test.installer.custom.path=STRING

ant test -Djavac.classpath=.:../../jemmy/external/jemmy-2.3.0.0.jar:../../nbbuild/build/public-package-jars/org-netbeans-libs-junit4.jar:../../nbbuild\netbeans\platform11\modules\ext/junit-4.5.jar -Dtest-sys-prop.test.installer.url.prefix=http://builds/netbeans/trunk/latest_daily -Dtest-sys-prop.test.installer.bundle.name.prefix=netbeans-trunk-nightly -Dtest-sys-prop.test.use.build.number=true -Dtest-sys-prop.test.installer.bundle.name.suffix=all
*/
