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
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.actions.Presenter;

/**
 *
 * @author mkleint
 */
public class DynaMenuModelTest extends TestCase {

    public DynaMenuModelTest(String testName) {
        super(testName);
    }

    /**
     * Test of loadSubmenu method, of class org.openide.awt.DynaMenuModel.
     */
    public void testLoadSubmenu() {
        System.out.println("loadSubmenu");
        
        List<Object> cInstances = new ArrayList<Object>();
        cInstances.add(new Act1());
        cInstances.add(new Act2());
        JMenu m = new JMenu();
        DynaMenuModel instance = new DynaMenuModel();
        
        instance.loadSubmenu(cInstances, m, false, Collections.<Object,FileObject>emptyMap());
        Component[] comps = m.getPopupMenu().getComponents();
        assertEquals("0", ((JMenuItem)comps[0]).getText());
        assertEquals("1", ((JMenuItem)comps[1]).getText());
        assertEquals("2", ((JMenuItem)comps[2]).getText());
        
    }

    /**
     * Test of checkSubmenu method, of class org.openide.awt.DynaMenuModel.
     */
    public void testCheckSubmenu() {
        List<Object> cInstances = new ArrayList<Object>();
        cInstances.add(new Act1());
        cInstances.add(new Act2());
        JMenu m = new JMenu();
        DynaMenuModel instance = new DynaMenuModel();
        
        instance.loadSubmenu(cInstances, m, false, Collections.<Object,FileObject>emptyMap());
        instance.checkSubmenu(m);
        
        Component[] comps = m.getPopupMenu().getComponents();
        assertEquals("0", ((JMenuItem)comps[0]).getText());
        assertEquals("1x", ((JMenuItem)comps[1]).getText());
        assertEquals("2x", ((JMenuItem)comps[2]).getText());
        
    }
    
    
    
    public  void testSeparators() {
        JPopupMenu menu = new JPopupMenu();
        menu.add(new JMenu());
        menu.add(new JSeparator());
        menu.add(new DynaMenuModel.InvisibleMenuItem());
        menu.add(new JSeparator());
        menu.add(new JMenuItem());
        DynaMenuModel.checkSeparators(menu.getComponents(), menu);
        Component[] menus = menu.getComponents();
        assertTrue(menus[1].isVisible());
        assertFalse(menus[3].isVisible());
        
        menu = new JPopupMenu();
        menu.add(new JMenu());
        menu.add(new JSeparator());
        menu.add(new DynaMenuModel.InvisibleMenuItem());
        menu.add(new JMenuItem());
        menu.add(new DynaMenuModel.InvisibleMenuItem());
        menu.add(new JSeparator());
        menu.add(new JSeparator());
        menu.add(new JMenuItem());
        DynaMenuModel.checkSeparators(menu.getComponents(), menu);
        Component[] menus2 = menu.getComponents();
        assertTrue(menus2[1].isVisible());
        assertTrue(menus2[5].isVisible());
        assertFalse(menus2[6].isVisible());
        
        
        
    }
    
    private class Act1 extends AbstractAction implements Presenter.Menu {
        public void actionPerformed(ActionEvent actionEvent) {
        }
        
        public JMenuItem getMenuPresenter() {
            return new JMenuItem("0");
        }
    }
    
    private class Act2 extends AbstractAction implements Presenter.Menu {
        public void actionPerformed(ActionEvent actionEvent) {
        }
        
        public JMenuItem getMenuPresenter() {
            return new Dyna();
        }
    }    
    
    private class Dyna extends JMenuItem implements DynamicMenuContent {
        private JMenuItem itm1;
        private JMenuItem itm2;
        public JComponent[] getMenuPresenters() {
            itm1 = new JMenuItem();
            itm1.setText("1");
            itm2 = new JMenuItem();
            itm2.setText("2");
            return new JComponent[] {
                itm1,
                itm2
            };
        }
    
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            ((JMenuItem)items[0]).setText("1x");
            ((JMenuItem)items[1]).setText("2x");
            return items;
        }
    }    
    
}
