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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class ArchiveTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;

    private File jar;
    private Module module;
    private CharSequence log;
    
    public ArchiveTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        ide = new File(install, "ide");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        jar = createModule("org.openide.sample", platform,
            "Class-Path", "ext/non-existent.jar",
            "OpenIDE-Module-Public-Packages", "-",
            "OpenIDE-Module", "org.openide.sample"
        );
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        module = mgr.create(jar, null, false, false, false);
        log = Log.enable("org.netbeans", Level.WARNING);
        mgr.enable(module);
        mgr.mutexPrivileged().exitWriteAccess();

        Stamps.main("reset");
        
        Thread.sleep(100);

        Logger l = Logger.getLogger("org");
        l.setLevel(Level.OFF);
        l.setUseParentHandlers(false);
        JarClassLoader.initializeCache();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testWrongClassPathElement() throws Exception {
        ClassLoader l = module.getClassLoader();
        assertNotNull("Resource found", l.getResource("fake/org.openide.sample"));
        assertNull("Resource not found", l.getResource("fake/not-org.openide.sample"));
        assertNull("Resource not found", l.getResource("default.properties"));

        JarClassLoader.saveArchive();
        Stamps.getModulesJARs().waitFor(false);
        if (log.length() > 0) { // #159093
            fail("There shall be no warning even if the Class-Path file is not existent:\n" + log);
        }
    }

    private File createModule(String cnb, File cluster, String... attr) throws IOException {
        String dashes = cnb.replace('.', '-');
        
        File tmp = new File(new File(cluster, "modules"), dashes + ".jar");

        Map<String,String> attribs = new HashMap<String, String>();
        for (int i = 0; i < attr.length; i += 2) {
            attribs.put(attr[i], attr[i + 1]);
        }

        Map<String,String> files = new HashMap<String, String>();
        files.put("fake/" + cnb, cnb);

        tmp.getParentFile().mkdirs();
        SetupHid.createJar(tmp, files, attribs);

        return tmp;
    }

}
