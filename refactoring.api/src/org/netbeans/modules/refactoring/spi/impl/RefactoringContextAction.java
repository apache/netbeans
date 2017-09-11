/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.refactoring.spi.impl;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.actions.Presenter;

/**
 * It is UI placeholder for language specific refactoring actions
 *
 * @author Jan Pokorsky
 * @see RefactoringContextActionsProvider
 */
public final class RefactoringContextAction extends AbstractAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup {

    private final Lookup context;

    public RefactoringContextAction() {
        this(null);
    }

    public RefactoringContextAction(Lookup context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported."); // NOI18N
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new RefactoringContextAction(actionContext);
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return new InlineMenu(context, false);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        return new InlineMenu(context, true);
    }

    private static final class InlineMenu extends JMenuItem implements DynamicMenuContent {

        private static final JComponent[] EMPTY_CONTENT = new JComponent[0];
        private Lookup context;
        private final boolean popup;

        public InlineMenu(Lookup context, boolean popup) {
            this.context = context;
            this.popup = popup;
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return createMenuItems();
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            JComponent[] comps = new JComponent[1];
            for (JComponent item : items) {
                if (item instanceof Actions.MenuItem) {
                    comps[0] = item;
                    // update menu items to reflect Action.isEnabled
                    ((Actions.MenuItem) item).synchMenuPresenters(comps);
                } else if(item instanceof JMenu) {
                    JMenu jMenu = (JMenu) item;
                    for (Component subItem : jMenu.getMenuComponents()) {
                        if (subItem instanceof Actions.MenuItem) {
                            comps[0] = (JComponent) subItem;
                            // update menu items to reflect Action.isEnabled
                            ((Actions.MenuItem) subItem).synchMenuPresenters(comps);
                        }
                    }
                }
            }
            // returns most up-to date items
            return createMenuItems();
        }

        /** Creates items when actually needed. */
        private JComponent[] createMenuItems() {
            resolveContext();
            MimePath mpath = resolveMIMEType();

            RefactoringContextActionsProvider actionProvider = mpath != null
                    ? MimeLookup.getLookup(mpath).lookup(RefactoringContextActionsProvider.class)
                    : null;

            if (actionProvider != null) {
                return actionProvider.getMenuItems(popup, context);
            } else {
                return EMPTY_CONTENT;
            }

        }

        private void resolveContext() {
            if (context == null) {
                context = Utilities.actionsGlobalContext();
            }
        }

        private MimePath resolveMIMEType() {
            MimePath mpath = context.lookup(MimePath.class);
            if (mpath != null) {
                return mpath;
            }

            FileObject fobj = context.lookup(FileObject.class);
            if (fobj != null) {
                return MimePath.parse(fobj.getMIMEType());
            }
            
            DataObject dobj = context.lookup(DataObject.class);
            return dobj == null ? null : MimePath.parse(dobj.getPrimaryFile().getMIMEType());
        }

    }

}
