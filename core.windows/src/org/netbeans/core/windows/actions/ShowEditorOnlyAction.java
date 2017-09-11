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
package org.netbeans.core.windows.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.EditorOnlyDisplayer;
import org.netbeans.core.windows.TopComponentTracker;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 * An action to toggle distraction-free editing mode when only the editor component
 * is showing and everything else is hidden (other windows, toolbars, status bar etc).
 * 
 * @author S. Aubrecht
 */
public class ShowEditorOnlyAction extends AbstractAction implements PropertyChangeListener, Runnable, DynamicMenuContent {

    private JCheckBoxMenuItem [] menuItems;

    private ShowEditorOnlyAction() {
        super( NbBundle.getMessage( ShowEditorOnlyAction.class, "CTL_ShowOnlyEditor") );

        addPropertyChangeListener( new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if( Action.ACCELERATOR_KEY.equals(evt.getPropertyName()) ) {
                    synchronized( ShowEditorOnlyAction.this ) {
                        menuItems = null;
                        createItems();
                    }
                }
            }
        });

        TopComponent.getRegistry().addPropertyChangeListener(
            WeakListeners.propertyChange(this, TopComponent.getRegistry()));
        // #126355 - may be called outside dispatch thread
        if (EventQueue.isDispatchThread()) {
            updateState();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    public static Action create() {
        return new ShowEditorOnlyAction();
    }

    @Override
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();
        return menuItems;
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return menuItems;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        EditorOnlyDisplayer eod = EditorOnlyDisplayer.getInstance();
        eod.setActive( !eod.isActive() );
    }


    private void createItems() {
        synchronized( this ) {
            if (menuItems == null) {
                menuItems = new JCheckBoxMenuItem[1];
                menuItems[0] = new JCheckBoxMenuItem(this);
                menuItems[0].setIcon(null);
                Mnemonics.setLocalizedText(menuItems[0], NbBundle.getMessage( ShowEditorOnlyAction.class, "CTL_ShowOnlyEditor"));
            }
        }
    }

    private void updateState() {
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }

    @Override
    public void run() {
        boolean isDocumentActive = false;
        TopComponent tc = TopComponent.getRegistry().getActivated();
        if (tc != null) {
            isDocumentActive = TopComponentTracker.getDefault().isEditorTopComponent( tc );
        }
        synchronized( this ) {
            createItems();
            menuItems[0].setSelected( EditorOnlyDisplayer.getInstance().isActive() );
            menuItems[0].setEnabled( isDocumentActive );
        }
    }

    @Override
    public void propertyChange( PropertyChangeEvent evt ) {
        if(TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
            updateState();
        }
    }
}
