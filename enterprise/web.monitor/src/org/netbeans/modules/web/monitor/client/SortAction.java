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
