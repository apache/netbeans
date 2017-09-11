/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.project.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.MenuElement;
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

            addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    String propName = evt.getPropertyName();
                    if ("enabled".equals(propName)) {
                        item.setEnabled((Boolean) evt.getNewValue());
                    } else if ("menuText".equals(propName)) {
                        item.setText(org.openide.awt.Actions.cutAmpersand((String) evt.getNewValue()));
                    }
                }
            });

            menu.add(item);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MainProjectActionWithHistory.this.actionPerformed(e);
                }
            });
           
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
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        RequestProcessor.getDefault().post(new Runnable() {
                            @Override
                            public void run() {
                                bai.repeatExecution();
                            }
                        });
                    }
                });
            }
        }
    }

    @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    }

    @Override public void popupMenuCanceled(PopupMenuEvent e) {
    }     
}
