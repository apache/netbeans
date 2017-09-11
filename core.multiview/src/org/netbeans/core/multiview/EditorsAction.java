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
