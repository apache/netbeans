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
package org.netbeans.core.windows.view.ui;

import java.awt.*;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.core.windows.IDEInitializer;
import org.netbeans.junit.*;
import org.openide.awt.ToolbarPool;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;


/**
 * Tests whether the system properties <code>
 * netbeans.winsys.menu_bar.path, netbeans.winsys.status_line.path
 * </code> and <code>netbeans.winsys.no_toolbars</code> really do
 * something.
 * 
 * @author David Strupl
 */
@RandomlyFails // the tests fail randomly from time to time
public class CustomMenuBarTest extends NbTestCase {

    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(CustomMenuBarTest.class);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.winsys.menu_bar.path", "LookAndFeel/MenuBar.instance");
        System.setProperty("netbeans.winsys.status_line.path", "LookAndFeel/StatusLine.instance");
        System.setProperty("netbeans.winsys.no_toolbars", "true");
    }

    @Override
    protected void tearDown() throws Exception {
        System.getProperties().remove("netbeans.winsys.menu_bar.path");
        System.getProperties().remove("netbeans.winsys.status_line.path");
        System.getProperties().remove("netbeans.winsys.no_toolbars");
    }

    private static JMenuBar myMenuBar;
    private static JComponent myStatusLine;

    /** Creates a new instance of SFSTest */
    public CustomMenuBarTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }


    public void testAlternativeMenuBar() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);

        IDEInitializer.addLayers(new String[]{"org/netbeans/core/windows/resources/layer-CustomMenuBarTest.xml"});

        //Verify that test layer was added to default filesystem
        assertNotNull(FileUtil.getConfigFile("LookAndFeel/MenuBar.instance"));

        MainWindow mw = MainWindow.getInstance();
        mw.initializeComponents();
        assertEquals(mw.getJMenuBar(), createMenuBar());
        IDEInitializer.removeLayers();
    }

    public void testAlternativeStatusLine() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);

        IDEInitializer.addLayers(new String[]{"org/netbeans/core/windows/resources/layer-CustomMenuBarTest.xml"});

        //Verify that test layer was added to default filesystem
        assertNotNull(FileUtil.getConfigFile("LookAndFeel/StatusLine.instance"));
        MainWindow mw = MainWindow.getInstance();
        mw.initializeComponents();
        assertTrue(findComponent(mw.getFrame(), createStatusLine()));

        IDEInitializer.removeLayers();
    }

    public void testNoToolbar() throws Exception {
        MainWindow mw = MainWindow.getInstance();
        mw.initializeComponents();
        ToolbarPool tp = ToolbarPool.getDefault();
        assertTrue(!findComponent(mw.getFrame(), tp));
    }

    private static boolean findComponent(Container cont, Component comp) {
        if (cont == null || comp == null) {
            return false;
        }
        if (cont.equals(comp)) {
            return true;
        }
        Component[] children = cont.getComponents();
        for (int i = 0; i < children.length; i++) {
            if (children[i].equals(comp)) {
                return true;
            }
            if (children[i] instanceof Container) {
                if (findComponent((Container) children[i], comp)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static class MyMenuBar extends JMenuBar {
    }

    private static class MyStatus extends JComponent {
    }

    private static JMenuBar createMenuBar() {
        if (myMenuBar == null) {
            myMenuBar = new MyMenuBar();
        }
        return myMenuBar;
    }

    private static JComponent createStatusLine() {
        if (myStatusLine == null) {
            myStatusLine = new MyStatus();
        }
        return myStatusLine;
    }
}
