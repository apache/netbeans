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

import org.netbeans.SetupHid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.MockEvents;
import org.netbeans.MockModuleInstaller;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Stamps;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.api.PlacesTestUtils;
import org.openide.util.test.MockLookup;

/** Do we recognize startlevel?
 * @author Jaroslav Tulach
 */
public class ModuleListStartLevelTest extends SetupHid {
    
    private static final String PREFIX = "wherever/";
    private LocalFileSystem fs;
    private MockEvents ev;
    private File ud;

    private void initModule() throws IOException {
        FileObject fo = modulesfolder.createData("com-jcraft-jsch.xml");
        File mod = new File(new File(ud, "modules"), "com-jcraft-jsch.jar");
        final HashMap<String, String> man = new HashMap<String, String>();
        man.put("Bundle-SymbolicName", "com.jcraft.jsch");
        createJar(mod, new HashMap<String, String>(), man);
        
        InputStream is = ModuleListStartLevelTest.class.getResourceAsStream("ModuleList-com-jcraft-jsch.xml");
        assertNotNull("Module definition found", is);
        final OutputStream os = fo.getOutputStream();
        FileUtil.copy(is, os);
        os.close();
        is.close();
    }

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
    
    public ModuleListStartLevelTest(String name) {
        super(name);
    }
    
    private ModuleManager mgr;
    private FileObject modulesfolder;
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        MockLookup.setInstances(new IFL());

        ud = new File(getWorkDir(), "ud");
        PlacesTestUtils.setUserDirectory(ud);

        File dir = new File(ud, "config");
        File modulesdir = new File(dir, "Modules");
        if (! modulesdir.mkdirs()) throw new IOException("Making " + modulesdir);
        fs = new LocalFileSystem();
        fs.setRootDirectory(dir);
        modulesfolder = fs.findResource("Modules");
        assertNotNull(modulesfolder);
        initModule();
        
        MockModuleInstaller installer = new MockModuleInstaller();
        ev = new MockEvents();
        mgr = new ModuleManager(installer, ev);
    }
    
    public void testParsesStartLevel() throws Exception {
        ModuleList list = new ModuleList(mgr, modulesfolder, ev);
        Set<Module> set = list.readInitial();
        
        assertEquals("One module: " + set, 1, set.size());
        Module m = set.iterator().next();
        
        assertEquals("Start level has been specified to four", 4, m.getStartLevel());
        
        Stamps.getModulesJARs().flush(0);
        Stamps.getModulesJARs().shutdown();
        
        Map<String, Map<String, Object>> cache = list.readCache();
        assertNotNull("Cache read", cache);
        Map<String, Object> module = cache.get("com.jcraft.jsch");
        assertNotNull("Module info found", module);
        Object level = module.get("startlevel");
        assertEquals("Start level is remembered", Integer.valueOf(4), level);
    }
    

}
