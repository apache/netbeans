/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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