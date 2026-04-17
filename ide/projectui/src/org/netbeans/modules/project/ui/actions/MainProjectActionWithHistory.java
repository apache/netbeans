/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 *
 * @author mkleint
 */
public class MainProjectActionWithHistory extends MainProjectAction implements Presenter.Toolbar, PopupMenuListener {

//    public MainProjectActionWithHistory(ProjectActionPerformer performer, String name, Icon icon) {
//        this(null, performer, name, icon, null);
//    }
    private final String command;

    public MainProjectActionWithHistory(String command, String name, Icon icon) {
        this(command, null, name, icon, null);
    }

    private MainProjectActionWithHistory(String command, ProjectActionPerformer performer, String name, Icon icon, Lookup lookup) {
        super(command, performer, name, icon, lookup);
        this.command = command;
    }
    
     @Override
    public Component getToolbarPresenter() {
       
            JPopupMenu menu = new JPopupMenu();
            JButton button = DropDownButtonFactory.createDropDownButton(
                    new ImageIcon(new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB)), menu);
            final JMenuItem item = new JMenuItem(org.openide.awt.Actions.cutAmpersand((String) getValue("menuText")));
            item.setEnabled(isEnabled());

            addPropertyChangeListener(evt -> {
                String prop = evt.getPropertyName();
                if (prop == null) {
                    return;
                }
                switch (prop) {
                    case "enabled" -> item.setEnabled((Boolean) evt.getNewValue());
                    case "menuText" -> item.setText(org.openide.awt.Actions.cutAmpersand((String) evt.getNewValue()));
                }
            });

            menu.add(item);
            item.addActionListener(MainProjectActionWithHistory.this::actionPerformed);
           
            org.openide.awt.Actions.connect(button, this);
            menu.addPopupMenuListener(this);
            return button;
        
    }
    
     
// PopupMenuListener ........................................................

    @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JPopupMenu menu = (JPopupMenu) e.getSource();
        for (Component c : menu.getComponents()) {
            if (c instanceof JComponent && ((JComponent)c).getClientProperty("aaa") != null) {
                menu.remove(c);
            }
        }
        List<BuildExecutionSupport.ActionItem> list = ((BuildExecutionSupportImpl) BuildExecutionSupportImpl.getInstance()).getHistoryFor(command);
        if (!list.isEmpty()) {
            JSeparator sep = new JSeparator();
            sep.putClientProperty("aaa", "aaa");
            menu.add(sep);
            for (final BuildExecutionSupport.ActionItem bai : list) {
                JMenuItem item = new JMenuItem(bai.getDisplayName());
                item.putClientProperty("aaa", "aaa");
                menu.add(item);
                item.addActionListener(evt -> 
                    RequestProcessor.getDefault().post(bai::repeatExecution)
                );
            }
        }
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
    }     
}
