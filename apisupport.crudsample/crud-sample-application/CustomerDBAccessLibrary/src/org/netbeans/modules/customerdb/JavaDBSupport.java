/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.customerdb;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.BaseUtilities;

public class JavaDBSupport {
    public static final String JAVADB_HOME = "javadb.home";
    public static final String JAVADB_PROPERTIES_HOME = "javadb.properties.home";
    public static void ensureStartedDB() {
        try {
            startDB();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void startDB() throws IOException {
            String java = null;
            File javaExecuble = new File(System.getProperty("java.home"), "/bin/java" + (BaseUtilities.isWindows() ? ".exe" : "")); // NOI18N
            if (javaExecuble != null && javaExecuble.exists()) {
                if (javaExecuble.canExecute()) {
                    java = javaExecuble.getAbsolutePath();
                }
            }
            if (java == null) {
                // XXX: MessageBox
            }

            if (getDerbyInstallation() == null) {
                // XXX: MessageBox
            }

            // java -Dderby.system.home="<userdir/derby>" -classpath
            //     "<DERBY_INSTALL>/lib/derby.jar:<DERBY_INSTALL>/lib/derbytools.jar:<DERBY_INSTALL>/lib/derbynet.jar"
            //     org.apache.derby.drda.NetworkServerControl start
            NbProcessDescriptor desc = new NbProcessDescriptor(
              java,
              "-Dderby.system.home=\"" + getDerbySystemHome() + "\" " +
              "-classpath \"" + getNetworkServerClasspath() + "\"" +
              " org.apache.derby.drda.NetworkServerControl start"
            );
            Logger.getLogger(JavaDBSupport.class.getName()).log(Level.FINE, "Running {0} {1}", new String[] {desc.getProcessName(), desc.getArguments()});
            Process process = desc.exec(null, getEnvironment(), true, getDerbyInstallation());
            if (process == null) {
                Logger.getLogger(JavaDBSupport.class.getName()).log(Level.FINE, "Process {0} is running.", new Object[] {process});
            } else {
                Logger.getLogger(JavaDBSupport.class.getName()).log(Level.FINE, "Process didn't start.");
            }
    }

    private static String getDerbySystemHome() {
        String home = null;
        home = System.getProperty(JAVADB_PROPERTIES_HOME);
        if (home == null || home.length() == 0) {
            // path to the default place of Java DB server in NB IDE
            home = System.getProperty("user.home") + "/.netbeans-derby/";
        }
        return home;
    }

    private static String getNetworkServerClasspath() {
        File f = getDerbyInstallation();
        if (f == null) {
            throw new IllegalStateException("No JavaDB installation found.");
        }
        return
            new File(f, "lib/derby.jar").getAbsolutePath() + File.pathSeparator +
            new File(f, "lib/derbytools.jar").getAbsolutePath() + File.pathSeparator +
            new File(f, "lib/derbynet.jar").getAbsolutePath();
    }

    private static File getDerbyInstallation() {
        File f = null;
        String javaDBHome = System.getProperty(JAVADB_HOME);
        if (javaDBHome == null) {
            javaDBHome = NbBundle.getMessage(JavaDBSupport.class, JAVADB_HOME);
            if (javaDBHome != null) {
                f = new File(javaDBHome);
            } else {
                String javaHome = System.getProperty("java.home");
                // path to JavaDB in JDK6
                f = new File(javaHome + "/../db/");
            }
        } else {
            f = new File(javaDBHome);
        }
        return f != null && f.exists() ? f : null;
    }

    private static String[] getEnvironment() {
        String location = getDerbyInstallation().getAbsolutePath();
        if (location.equals("")) { // NOI18N
            return null;
        }
        return new String[] { "DERBY_INSTALL=" + location }; // NOI18N
    }

}
