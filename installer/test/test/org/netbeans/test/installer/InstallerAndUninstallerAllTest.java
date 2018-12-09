/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
