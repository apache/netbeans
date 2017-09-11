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
