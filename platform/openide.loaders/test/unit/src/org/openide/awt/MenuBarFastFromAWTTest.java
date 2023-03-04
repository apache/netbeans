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
