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

package org.openide.awt;

import java.awt.Component;
import java.awt.EventQueue;
import java.util.logging.Level;
import javax.swing.JMenu;
import javax.swing.JSeparator;
import javax.swing.UIDefaults;
import javax.swing.plaf.metal.MetalLookAndFeel;
import org.netbeans.junit.NbTestCase;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MenuBarSeparatorInAWTTest extends NbTestCase {
    private DataFolder df;
    private MenuBar mb;

    static {
        System.setProperty("swing.defaultlaf", MyLaF.class.getName());
    }
    
    public MenuBarSeparatorInAWTTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }
    
    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.createFolder(
            FileUtil.getConfigRoot(),
            "Folder" + getName()
        );
        final FileObject file = fo.createFolder("File");
        file.createData("org-openide-actions-CutAction.instance").setAttribute("position", 100);
        file.createData("javax-swing-JSeparator.instance").setAttribute("position", 200);
        file.createData("org-openide-actions-CopyAction.instance").setAttribute("position", 300);
        df = DataFolder.findFolder(fo);
    }

    public void testJSeperatorCannotBeCreatedOutsideOfAWT() throws Throwable {
        mb = new MenuBar(df);
        mb.waitFinished();

        assertEquals("Laf created", 1, MyLaF.cnt);

        class R implements Runnable {
            Component[] arr;

            @Override
            public void run() {
                JMenu m = mb.getMenu(0);
                assertNotNull("One menu found", m);
                m.setPopupMenuVisible(true);
                arr = m.getMenuComponents();
            }
        }
        R run = new R();
        EventQueue.invokeAndWait(run);

        if (MyLaF.wrong != null) {
            throw MyLaF.wrong;
        }

        assertEquals("Three component", 3, run.arr.length);
        assertTrue("2nd is JSeparator", run.arr[1] instanceof JSeparator);

        try {
            new JSeparator();
            fail( "JSeparator must be created in EDT.");
        } catch( AssertionError e ) {
            //expected
        }
}




    public static final class MyLaF extends MetalLookAndFeel {
        static int cnt;
        static Throwable wrong;

        public MyLaF() {
            cnt++;
        }

        @Override
        public String getName() {
            return "MyLaf";
        }

        private void assertAWT() {
            assertTrue("Is AWT dispatch thread", EventQueue.isDispatchThread());
        }

        @Override
        public UIDefaults getDefaults() {
            final UIDefaults del = super.getDefaults();
            return new UIDefaults() {
                @Override
                public Object get(Object key) {
                    if( "SeparatorUI".equals( key ) )
                        assertAWT();
                    return del.get(key);
                }
            };
        }



        @Override
        public String getID() {
            assertAWT();
            return super.getID();
        }

        @Override
        public String getDescription() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isNativeLookAndFeel() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isSupportedLookAndFeel() {
            return true;
        }
    }
}
