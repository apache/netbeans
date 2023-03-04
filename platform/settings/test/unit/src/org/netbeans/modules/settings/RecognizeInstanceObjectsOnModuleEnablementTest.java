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
package org.netbeans.modules.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import junit.framework.Test;
import org.netbeans.DuplicateException;
import org.netbeans.InvalidException;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class RecognizeInstanceObjectsOnModuleEnablementTest extends NbTestCase {
    private File f;

    public RecognizeInstanceObjectsOnModuleEnablementTest(String name) {
        super(name);
    }
    
    public static Test suite() {
        return NbModuleSuite.createConfiguration(RecognizeInstanceObjectsOnModuleEnablementTest.class).
            gui(false).honorAutoloadEager(true).suite();
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        f = new File(getWorkDir(), "m.jar");
        
        Manifest man = new Manifest();
        Attributes attr = man.getMainAttributes();
        attr.putValue("OpenIDE-Module", "m.test");
        attr.putValue("OpenIDE-Module-Public-Packages", "-");
        attr.putValue("Manifest-Version", "1.0");
        JarOutputStream os = new JarOutputStream(new FileOutputStream(f), man);
        os.putNextEntry(new JarEntry("META-INF/namedservices/ui/javax.swing.JComponent"));
        os.write("javax.swing.JButton\n".getBytes(StandardCharsets.UTF_8));
        os.closeEntry();
        os.close();
        
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "ui/ch/my/javax-swing-JPanel.instance");
    }
    
    public void testEnableModuleWithEntry() throws Exception {
        ModuleSystem ms = Main.getModuleSystem();
        final ModuleManager man = ms.getManager();
        
        CharSequence log = Log.enable("org.openide.loaders.FolderInstance.ui.ch.my", Level.FINE);
        
        final Lookup.Result<JComponent> res = Lookups.forPath("ui").lookupResult(JComponent.class);
        assertTrue("No component registered yet", allWithoutPanel(res).isEmpty());
        class L implements LookupListener {
            int cnt;
            @Override
            public void resultChanged(LookupEvent ev) {
                allWithoutPanel(res);
                cnt++;
            }
        }
        L l = new L();
        res.addLookupListener(l);
        
        doEnableModuleWithEntry(man, res);
        
        assertTrue("Listener notified about change: " + l.cnt, l.cnt >= 1);
        
        int first = log.toString().indexOf("new org.openide.loaders.FolderLookup");
        int none = log.toString().indexOf("new org.openide.loaders.FolderLookup", first + 1);
        
        assertTrue("One instance created: " + first, first >= 0);
        assertEquals("No other instance created: " + none + "\n" + log, -1, none);
    }

    private void doEnableModuleWithEntry(final ModuleManager man, final Lookup.Result<JComponent> res) throws IllegalArgumentException, IOException, DuplicateException {
        Module m = enableModule(man);
        assertTrue("Module m.test has been enabled", m.isEnabled());

        final Collection<? extends JComponent> all = allWithoutPanel(res);
        assertEquals("One component registered now", 1, all.size());
        JComponent c = all.iterator().next();
        
        assertNotNull("Instance really found", c);
        assertEquals("It is button", JButton.class, c.getClass());
    }
    
    private static <T> Collection<T> allWithoutPanel(Lookup.Result<T> res) {
        Collection<T> arr = new ArrayList<T>();
        boolean found = false;
        for (T t : res.allInstances()) {
            if (t instanceof JPanel) {
                found = true;
            } else {
                arr.add(t);
            }
        }
        assertTrue("Panel found in " + res.allInstances(), found);
        return arr;
    }

    private Module enableModule(final ModuleManager man) throws InvalidException, DuplicateException, IllegalArgumentException, IOException {
        Module m = man.create(f, null, false, false, false);
        man.mutexPrivileged().enterWriteAccess();
        try {
            man.enable(m);
        } finally {
            man.mutexPrivileged().exitWriteAccess();
        }
        return m;
    }
}
