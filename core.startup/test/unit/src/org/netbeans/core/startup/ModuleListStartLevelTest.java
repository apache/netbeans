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

import org.netbeans.SetupHid;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.netbeans.*;
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
