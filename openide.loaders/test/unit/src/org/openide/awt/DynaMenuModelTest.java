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
