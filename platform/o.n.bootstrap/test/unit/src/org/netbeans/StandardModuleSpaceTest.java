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
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jaroslav.tulach@netbeans.org>
 */
public class StandardModuleSpaceTest extends NbTestCase {
    private File userdir;
    private File ide;
    private File platform;
    private File install;

    private File jar;
    private Module module;
    private CharSequence log;
    
    public StandardModuleSpaceTest(String testName) {
        super(testName);
    }            

    public void testClassPathWithSpaces() throws Exception {
        clearWorkDir();
        
        install = new File(getWorkDir(), "install");
        platform = new File(install, "platform");
        ide = new File(install, "ide");
        userdir = new File(getWorkDir(), "tmp");
        
        System.setProperty("netbeans.home", platform.getPath());
        System.setProperty("netbeans.dirs", ide.getPath());
        System.setProperty("netbeans.user", userdir.getPath());
        
        File tmp = new File(new File(new File(ide, "modules"), "ext"), "Sp a ce.jar");
        Map<String,String> cnt = new HashMap<String, String>();
        cnt.put("say/hello.txt", "Ahoj");
        SetupHid.createJar(tmp, cnt, Collections.<String,String>emptyMap());
        
        jar = createModule("org.use.space", ide,
            "Class-Path", "ext/Sp%20a%20ce.jar",
            "OpenIDE-Module-Public-Packages", "-",
            "OpenIDE-Module", "org.use.space"
        );
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        ModuleManager mgr = new ModuleManager(installer, ev);
        mgr.mutexPrivileged().enterWriteAccess();
        module = mgr.create(jar, null, false, false, false);
        log = Log.enable("org.netbeans", Level.WARNING);
        mgr.enable(module);
        mgr.mutexPrivileged().exitWriteAccess();
        URL u = module.getClassLoader().getResource("say/hello.txt");
        assertNotNull("Resource in space in path extension found", u);
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
