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

package org.openide.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.prefs.Preferences;
import javax.swing.Action;
import javax.swing.JMenuItem;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class FileSystemActionTest extends NbTestCase {

    public FileSystemActionTest(String s) {
        super(s);
    }

    /**
     * Injection of actions into Filesystems is now a special behaviour of NB 
     * platform - implemented in master filesystem.
     * 
     * @return 
     */
    public static Test suite() {
        return NbModuleSuite.create(FileSystemActionTest.class, null, null);
    }
    
    public void testManualRefreshPreference() throws IOException {
        Preferences pref = NbPreferences.root().node("org/openide/actions/FileSystemRefreshAction");
        assertFalse("Not set", pref.getBoolean("manual", false));

        FileObject fo = FileUtil.toFileObject(getWorkDir());
        Lookup lkp = Lookups.singleton(DataFolder.findFolder(fo).getNodeDelegate());

        FileSystemAction fsa = FileSystemAction.get(FileSystemAction.class);
        Action a = fsa.createContextAwareInstance(lkp);

        assertEquals("Menu presenter ", true, a instanceof Presenter.Menu);

        Presenter.Menu pm = (Presenter.Menu)a;
        DynamicMenuContent submenu = (DynamicMenuContent)pm.getMenuPresenter();
        assertEquals("No submenu", 0, submenu.getMenuPresenters().length);

        pref.putBoolean("manual", true);

        DynamicMenuContent submenu2 = (DynamicMenuContent)pm.getMenuPresenter();
        assertEquals("One action", 1, submenu2.getMenuPresenters().length);
    }
    
    public void testCreateMenu() throws Exception {
        TestFS fs = new TestFS();
        FileObject fo = fs.getRoot();
     
        // create menu for a lookup containg a node
        Lookup lkp = Lookups.singleton(DataFolder.findFolder(fo).getNodeDelegate());
        Method m = FileSystemAction.class.getDeclaredMethod("createMenu",
                Boolean.TYPE, Lookup.class);
        m.setAccessible(true);
        JMenuItem[] item = (JMenuItem[])m.invoke(null, true, lkp);
        assertTrue(item.length > 0);
        
        // create menu for a lookup containg a DataObject
        lkp = Lookups.singleton(DataFolder.findFolder(fo));
        item = (JMenuItem[])m.invoke(null, true, lkp);
        assertTrue(item.length > 0);
}
    
    private static class TestFS extends LocalFileSystem {
        public SystemAction[] getActions() {
            return new SystemAction[] {
                SystemAction.get(TestFSAction.class),
            }; 
        }
        public static class TestFSAction extends SystemAction implements Presenter.Menu, Presenter.Popup {
            @Override public String getName() { return ""; }
            @Override public HelpCtx getHelpCtx() { return null; }
            @Override public void actionPerformed(ActionEvent ev) { }
            @Override public JMenuItem getMenuPresenter() { return new JMenuItem(); }
            @Override public JMenuItem getPopupPresenter() { return new JMenuItem(); }
        }
    }
}