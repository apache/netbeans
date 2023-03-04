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

package org.netbeans.core.multiview;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingUtilities;
import org.netbeans.core.api.multiview.MultiViewHandler;
import org.netbeans.core.api.multiview.MultiViewPerspective;
import org.netbeans.core.api.multiview.MultiViews;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Action to show in main menu to switch active view.
 * @author mkleint
 */
public class EditorsAction extends AbstractAction 
implements Presenter.Menu, Presenter.Popup {
                                    
    public EditorsAction() {
        super(NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction"));
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
        assert false;// no operation
    }
    
    @Override
    public JMenuItem getMenuPresenter() {
        JMenu menu = new UpdatingMenu();
        String label = NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction");
        Mnemonics.setLocalizedText(menu, label);
        return menu;
    }
    @Override
    public JMenuItem getPopupPresenter() {
        JMenu menu = new UpdatingMenu();
        String label = NbBundle.getMessage(EditorsAction.class, "CTL_EditorsAction");
        Actions.setMenuText(menu, label, false);
        return menu;
    }
    
    private static final class UpdatingMenu extends JMenu implements DynamicMenuContent {
        
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }
        
        public JComponent[] getMenuPresenters() {
            assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT";
            removeAll();
            final TopComponent tc = WindowManager.getDefault().getRegistry().getActivated();
            if (tc != null) {
                setEnabled(true);
                MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                if (handler != null) {
                    ButtonGroup group = new ButtonGroup();
                    MultiViewPerspective[] pers = handler.getPerspectives();
                    final String [] names = new String [pers.length];
                    for (int i = 0; i < pers.length; i++) {
                        MultiViewPerspective thisPers = pers[i];

                        JRadioButtonMenuItem item = new JRadioButtonMenuItem();
                        names[i] = thisPers.getDisplayName();
                        Mnemonics.setLocalizedText(item, thisPers.getDisplayName());
                        item.setActionCommand(thisPers.getDisplayName());
                        item.addActionListener(new ActionListener() {
                            public void actionPerformed(ActionEvent event) {
                                MultiViewHandler handler = MultiViews.findMultiViewHandler(tc);
                                if (handler == null) {
                                    return;
                                }
                                MultiViewPerspective thisPers = null;
                                MultiViewPerspective[] pers = handler.getPerspectives();
                                assert pers.length == names.length : "Arrays must have the same length";
                                for (int i = 0; i < pers.length; i++) {
                                    if (event.getActionCommand().equals(names[i])) {
                                        thisPers = pers[i];
                                        break;
                                    }
                                }
                                if (thisPers != null) {
                                    handler.requestActive(thisPers);
                                }
                            }
                        });
                        if (thisPers.getDisplayName().equals(handler.getSelectedPerspective().getDisplayName())) {
                            item.setSelected(true);
                        }
			boolean isSplitDescription = false;
			MultiViewDescription desc = Accessor.DEFAULT.extractDescription(thisPers);
			if (desc instanceof ContextAwareDescription) {
			    isSplitDescription = ((ContextAwareDescription) desc).isSplitDescription();
			}
			if (!isSplitDescription) {
			    group.add(item);
			    add(item);
			}
                    }
                } else { // handler == null
                    //No reason to enable action on any TC because now it was enabled even for Welcome page
                    setEnabled(false);
                    /*JRadioButtonMenuItem but = new JRadioButtonMenuItem();
                    Mnemonics.setLocalizedText(but, NbBundle.getMessage(EditorsAction.class, "EditorsAction.source"));
                    but.setSelected(true);
                    add(but);*/
                }
            } else { // tc == null
                setEnabled(false);
            }
            return new JComponent[] {this};
        }
        
    }
    
}
