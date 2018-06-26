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

/**
 * SortAction.java
 *
 * Created on June 23, 2004, 4:07 PM
 *
 * @author  Stepan Herold
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.netbeans.modules.web.monitor.client.Controller.CompAlpha;
import org.netbeans.modules.web.monitor.client.Controller.CompTime;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;



public class SortAction extends NodeAction {
    // radio button menu items
    private transient JMenuItem descSortMenuItem, ascSortMenuItem, alphSortMenuItem;        
    
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    public JMenuItem getPopupPresenter() {
        JMenu menu = new JMenu(NbBundle.getMessage(MonitorAction.class, "MON_Sort_by"));

        TransactionView transView = TransactionView.getInstance();
        descSortMenuItem = createItem(
            NbBundle.getMessage(MonitorAction.class, "MON_Sort_desc"), 
            transView.isDescButtonSelected());        
        ascSortMenuItem = createItem(
            NbBundle.getMessage(MonitorAction.class, "MON_Sort_asc"), 
            transView.isAscButtonSelected());
        alphSortMenuItem = createItem(
            NbBundle.getMessage(MonitorAction.class, "MON_Sort_alph"), 
            transView.isAlphButtonSelected());
        
        ActionListener listener = new RadioMenuItemActioListener();
        descSortMenuItem.addActionListener(listener);
        ascSortMenuItem.addActionListener(listener);
        alphSortMenuItem.addActionListener(listener);
        
        menu.add(descSortMenuItem);
        menu.add(ascSortMenuItem);    
        menu.add(alphSortMenuItem);
        
        return menu;
    }

    private JMenuItem createItem(String dispName, boolean selected) {
        JMenuItem item = new JRadioButtonMenuItem();
        item.setText(dispName);
        item.setSelected(selected);
        return item;
    }     
    
    public String getName() {
        return NbBundle.getMessage(MonitorAction.class, "MON_Sort_by");
    }
    
    protected void performAction(Node[] activatedNodes) {
    }
    
    class RadioMenuItemActioListener implements ActionListener {        
        public void actionPerformed(ActionEvent e) {
            Controller controller = MonitorAction.getController();
            TransactionView transView = TransactionView.getInstance();
            Object source = e.getSource();            
            if (source == descSortMenuItem) {
                if (!transView.isDescButtonSelected()) {
                    transView.toggleTaskbarButtons(false, true, false);                
                    controller.setComparator(controller.new CompTime(true));
                }
             } else if (source == ascSortMenuItem) {
                 if (!transView.isAscButtonSelected()) {
                    transView.toggleTaskbarButtons(true, false, false);
                    controller.setComparator(controller.new CompTime(false));
                 }
             } else if (source == alphSortMenuItem) {
                 if (!transView.isAlphButtonSelected()) {
                    transView.toggleTaskbarButtons(false, false, true);
                    controller.setComparator(controller.new CompAlpha());
                 }
             }                       
        }        
    }    
}
