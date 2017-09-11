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

import org.netbeans.Module;
import org.netbeans.ModuleManager;
import java.util.*;
import org.openide.modules.*;
import java.io.*;
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
        os.write(cfg.getBytes("UTF-8"));
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
