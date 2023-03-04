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
import javax.swing.JComponent;
import org.openide.awt.DynamicMenuContent;
import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ToolsActionSlowTest extends NbTestCase {

    public ToolsActionSlowTest(String n) {
        super(n);
    }

    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        fo.delete();
    }

    
    public void testInlineIsNotBlocked() throws Exception {
        ToolsAction ta = ToolsAction.get(ToolsAction.class);

        Lookup lkp = Lookups.singleton(this);
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        assertNotNull("ToolActions folder found", fo);

        fo.createFolder("Cat3").createData(BlockingAction.class.getName().replace('.', '-') + ".instance").setAttribute("position", 100);

        Action a = ta.createContextAwareInstance(lkp);
        assertTrue("It is menu presenter", a instanceof Presenter.Menu);
        Presenter.Menu mp = (Presenter.Menu)a;
        JMenuItem item = mp.getMenuPresenter();

        assertTrue("Item is enabled", item.isEnabled());
        DynamicMenuContent dmc = (DynamicMenuContent)item;
        final JComponent[] arr = dmc.getMenuPresenters();
        assertEquals("One presenter to delegte to", 1, arr.length);
        assertFalse("Disabled", arr[0].isEnabled());
    }

    public static final class BlockingAction extends AbstractAction {
        private static boolean proceed;
        
        public BlockingAction() {
            synchronized (BlockingAction.class) {
                while (!proceed) {
                    try {
                        BlockingAction.class.wait();
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
        }
        
    }
}
