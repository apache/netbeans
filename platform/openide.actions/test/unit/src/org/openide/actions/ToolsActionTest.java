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

import java.io.IOException;
import java.util.Arrays;
import javax.swing.JComponent;
import org.openide.awt.DynamicMenuContent;
import javax.swing.Action;
import java.util.List;
import javax.swing.JMenuItem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileObject;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.actions.ActionPerformer;
import static org.junit.Assert.*;
import org.openide.cookies.SaveCookie;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class ToolsActionTest extends NbTestCase implements ActionPerformer, SaveCookie {

    public ToolsActionTest(String n) {
        super(n);
    }

    @Override
    protected void setUp() throws Exception {
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        fo.delete();
    }

    public void testActionReadFromLayer () throws Exception {
        CopyAction copy = CopyAction.get(CopyAction.class);
        CutAction cut = CutAction.get(CutAction.class);
        PasteAction paste = PasteAction.get(PasteAction.class);

        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        assertNotNull("ToolActions folder found", fo);

        fo.createFolder("Cat1").createData("org-openide-actions-CutAction.instance").setAttribute("position", 100);
        fo.getFileObject("Cat1").createData("org-openide-actions-PasteAction.instance").setAttribute("position", 200);
        fo.createFolder("Cat2").createData("org-openide-actions-CopyAction.instance");

        List<Action> ToolActions = ToolsAction.getToolActions();
        assertEquals("Four actions: " + ToolActions, 4, ToolActions.size());
        assertEquals("Cut first", cut, ToolActions.get(0));
        assertEquals("Paste snd", paste, ToolActions.get(1));
        assertEquals("Separator in middle", null, ToolActions.get(2));
        assertEquals("Copy last", copy, ToolActions.get(3));

        ToolsAction ta = ToolsAction.get(ToolsAction.class);
        JMenuItem mp = ta.getMenuPresenter();
        DynamicMenuContent mc = null;
        if (mp instanceof DynamicMenuContent) {
            mc = (DynamicMenuContent) mp;
        } else {
            fail("Shall be instance of DynamicMenuContent: " + mp);
        }

        JComponent[] arr = mc.getMenuPresenters();
        assertEquals("None " + Arrays.asList(arr), 0, arr.length);
        CutAction.get(CutAction.class).setActionPerformer(this);

        arr = mc.getMenuPresenters();
        assertEquals("One " + Arrays.asList(arr), 1, arr.length);
    }

    public void testIsPopupVisible() throws Exception {
        ToolsAction ta = ToolsAction.get(ToolsAction.class);

        Lookup lkp = Lookups.singleton(this);
        FileObject fo = FileUtil.createFolder(FileUtil.getConfigRoot(), "UI/ToolActions");
        assertNotNull("ToolActions folder found", fo);

        fo.createFolder("Cat1").createData("org-openide-actions-SaveAction.instance").setAttribute("position", 100);

        Action a = ta.createContextAwareInstance(lkp);
        assertTrue("It is menu presenter", a instanceof Presenter.Popup);
        Presenter.Popup pp = (Presenter.Popup)a;
        JMenuItem item = pp.getPopupPresenter();

        assertTrue("Item is enabled", item.isEnabled());
        DynamicMenuContent dmc = (DynamicMenuContent)item;
        assertEquals("One presenter to delegte to", 1, dmc.getMenuPresenters().length);
    }


    @Override
    public void performAction(SystemAction action) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void save() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
