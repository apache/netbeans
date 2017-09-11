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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.core.startup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Permission;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import org.netbeans.CLIHandler;
import org.netbeans.junit.NbTestCase;


/** Make sure the CLIHandler can be in modules and really work.
 * @author Jaroslav Tulach
 */
public class CLILookupHelpTest extends NbTestCase {
    File home, cluster2, user;
    
    public CLILookupHelpTest(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        clearWorkDir();

        File p = new File(getWorkDir(), "par");
        home = new File(p, "cluster1");
        cluster2 = new File(p, "cluster2");
        user = new File(getWorkDir(), "testuserdir");
        
        home.mkdirs();
        cluster2.mkdirs();
        user.mkdirs();
        
        System.setProperty("netbeans.home", home.toString());
        System.setProperty("netbeans.dirs", cluster2.toString());
        
        System.setSecurityManager(new NoExit());
    }
    

    protected void tearDown() throws Exception {
        NoExit.disable = true;
    }
    
    public void testModuleInAClusterCanBeFound() throws Exception {
        createJAR(home, "test-module-one", One.class);
        createJAR(cluster2, "test-module-two", Two.class);
        createJAR(user, "test-module-user", User.class);

        try {
            org.netbeans.Main.main(new String[] { "--help", "--userdir", user.toString() });
            fail("At the end this shall throw security exception");
        } catch (SecurityException ex) {
            assertEquals("Exit code shall be two", "2", ex.getMessage());
        }
        
        assertEquals("Usage one", 1, One.usageCnt); assertEquals("CLI", 0, One.cliCnt);
        assertEquals("Usage two", 1, Two.usageCnt); assertEquals("CLI", 0, Two.cliCnt);
        assertEquals("Usage user", 1, User.usageCnt); assertEquals("CLI", 0, User.cliCnt);
    }

    static File createJAR(File cluster, String moduleName, Class metaInfHandler) 
    throws IOException {
        File xml = new File(new File(new File(cluster, "config"), "Modules"), moduleName + ".xml");
        File jar = new File(new File(cluster, "modules"), moduleName + ".jar");
        
        xml.getParentFile().mkdirs();
        jar.getParentFile().mkdirs();
        
        
        Manifest mf = new Manifest();
        mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        mf.getMainAttributes().putValue("OpenIDE-Module", moduleName.replace('-', '.'));
        mf.getMainAttributes().putValue("OpenIDE-Module-Public-Packages", "-");
        
        JarOutputStream os = new JarOutputStream(new FileOutputStream(jar), mf);
        if (metaInfHandler != null) {
            os.putNextEntry(new JarEntry("META-INF/services/org.netbeans.CLIHandler"));
            os.write(metaInfHandler.getName().getBytes());
        }
        os.close();
        
        FileWriter w = new FileWriter(xml);
        w.write(            
"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<!DOCTYPE module PUBLIC \"-//NetBeans//DTD Module Status 1.0//EN\"\n" +
"                        \"http://www.netbeans.org/dtds/module-status-1_0.dtd\">\n" +
"<module name=\"" + moduleName.replace('-', '.') + "\">\n" +
"    <param name=\"autoload\">false</param>\n" +
"    <param name=\"eager\">false</param>\n" +
"    <param name=\"enabled\">true</param>\n" +
"    <param name=\"jar\">modules/" + moduleName + ".jar</param>\n" +
"    <param name=\"release\">2</param>\n" +
"    <param name=\"reloadable\">false</param>\n" +
"    <param name=\"specversion\">3.4.0.1</param>\n" +
"</module>\n");
        w.close();
        
        return jar;
    }

    
    public static final class One extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public One() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    public static final class Two extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public Two() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    public static final class User extends CLIHandler {
        public static int cliCnt;
        public static int usageCnt;
        
        public User() {
            super(WHEN_EXTRA);
        }

        protected int cli(CLIHandler.Args args) {
            cliCnt++;
            return 0;
        }

        protected void usage(PrintWriter w) {
            usageCnt++;
        }
    }
    
    
    private static final class NoExit extends SecurityManager {
        public static boolean disable;
        
        public void checkExit(int status) {
            if (!disable) {
                throw new SecurityException(String.valueOf(status));
            }
        }

        public void checkPermission(Permission perm) {
            
        }

        public void checkPermission(Permission perm, Object context) {
            
        }
        
    }
}
