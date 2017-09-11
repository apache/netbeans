/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.settings;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        os.write("javax.swing.JButton\n".getBytes("UTF-8"));
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
