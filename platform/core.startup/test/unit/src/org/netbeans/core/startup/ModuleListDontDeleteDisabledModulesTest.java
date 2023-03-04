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

package org.netbeans.core.startup;

import org.netbeans.Module;
import org.netbeans.ModuleManager;
import java.util.*;
import org.openide.modules.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.SetupHid;
import org.openide.filesystems.*;
import org.openide.util.test.MockLookup;

/** 
 * @author 
 */
public class ModuleListDontDeleteDisabledModulesTest extends SetupHid {
    
    private File ud;
    
    private static final String PREFIX = "wherever/";
    
    private final class IFL extends InstalledFileLocator {
        public IFL() {}
        public File locate(String relativePath, String codeNameBase, boolean localized) {
            if (relativePath.startsWith(PREFIX)) {
                File f = new File(jars, relativePath.substring(PREFIX.length()).replace('/', File.separatorChar));
                if (f.exists()) {
                    return f;
                }
            }
            return null;
        }
    }
    
    public ModuleListDontDeleteDisabledModulesTest(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    private org.netbeans.core.startup.ModuleList list;
    private FileObject modulesfolder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new IFL());

        ud = new File(getWorkDir(), "ud");
        ud.mkdirs();
        System.setProperty("netbeans.user", ud.getPath());
        
        MockModuleInstaller installer = new MockModuleInstaller();
        MockEvents ev = new MockEvents();
        mgr = new ModuleManager(installer, ev);
        File dir = getWorkDir();
        File modulesdir = new File(dir, "Modules");
        if (! modulesdir.mkdir()) throw new IOException("Making " + modulesdir);
        modulesfolder = FileUtil.createFolder(FileUtil.getConfigRoot(), "Modules");
        assertNotNull(modulesfolder);
        list = new ModuleList(mgr, modulesfolder, ev);
    }
    
    public void testIsMissingDisabledModuleIgnoredOrDeleted() throws Exception {
        File file = new File(new File(new File(ud, "config"), "Modules"), "org-foo.xml");
        file.getParentFile().mkdirs();
        FileOutputStream os = new FileOutputStream(file);
        String cfg = "<?xml version='1.0' encoding='UTF-8'?>\n" +
                "<!DOCTYPE module PUBLIC '-//NetBeans//DTD Module Status 1.0//EN' 'http://www.netbeans.org/dtds/module-status-1_0.dtd'>\n" +
                "<module name='org.foo'>\n" +
                "   <param name='autoload'>false</param>\n" +
                "   <param name='eager'>false</param>\n" +
                "   <param name='enabled'>false</param>\n" +
                "   <param name='jar'>modules/org-foo.jar</param>\n" +
                "   <param name='reloadable'>false</param>\n" +
                "   <param name='specversion'>1.0</param>\n" +
                "</module>\n" +
                "\n";
        os.write(cfg.getBytes(StandardCharsets.UTF_8));
        os.close();
        modulesfolder.refresh();

        FileObject configFo = FileUtil.getConfigFile("Modules/org-foo.xml");
        assertNotNull("Config file exists", configFo);

        mgr.mutexPrivileged().enterWriteAccess();
        try {
            assertEquals(Collections.emptySet(), list.readInitial());
            assertEquals(Collections.emptySet(), mgr.getModules());
            list.trigger(Collections.<Module>emptySet());
            assertEquals(Collections.emptySet(), mgr.getModules());
        } finally {
            mgr.mutexPrivileged().exitWriteAccess();
        }

        assertTrue("Old config file is still OK", configFo.isValid());
        FileObject configFo2 = FileUtil.getConfigFile("Modules/org-foo.xml");
        assertNotNull("Config file exists", configFo2);
    }
}
