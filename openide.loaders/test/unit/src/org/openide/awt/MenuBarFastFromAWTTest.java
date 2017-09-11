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
import org.netbeans.junit.NbTestCase;
import org.openide.actions.OpenAction;
import org.openide.loaders.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Test whether menu can be quickly obtained from event dispatch thread.
 *
 * @author Jaroslav Tulach
 */
public class MenuBarFastFromAWTTest extends NbTestCase {
    private DataFolder df;
    private MenuBar mb;
    
    private int add;
    private int remove;
    
    public MenuBarFastFromAWTTest(String testName) {
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
        df = DataFolder.findFolder(fo);
        
        FileObject fileMenu = df.getPrimaryFile().createFolder("File");
        DataFolder fileM = DataFolder.findFolder(fileMenu);
        InstanceDataObject.create(fileM, null, OpenAction.class);
        
        mb = new MenuBar(df);
        mb.waitFinished();
    }

    @Override
    protected void tearDown() throws Exception {
    }
    
    public void testInAWT() throws Exception {
        EventQueue.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                doTestInAwt();
            }
        });
    }

    final void doTestInAwt() {
        assertEquals("One menu", 1, mb.getMenuCount());
        JMenu m = mb.getMenu(0);
        assertEquals("named file", "File", m.getText());

        long before = System.currentTimeMillis();
        MenuBarTest.simulateExpansionOfMenu(m);
        Component[] arr = m.getMenuComponents();
        assertEquals("One menu item", 1, arr.length);
        long after = System.currentTimeMillis();
        
        long took = after - before;
        if (took > 5000) {
            fail("Too long time to compute menu items (" + took + " ms), probably time out somewhere!");
        }
    }
}
