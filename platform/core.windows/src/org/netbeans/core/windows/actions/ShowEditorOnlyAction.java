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
