/*
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
