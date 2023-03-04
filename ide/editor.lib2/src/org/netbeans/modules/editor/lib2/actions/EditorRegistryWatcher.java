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
package org.netbeans.modules.editor.lib2.actions;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.editor.lib2.WeakReferenceStableList;

/**
 *
 * @author Miloslav Metelka
 */
public class EditorRegistryWatcher implements PropertyChangeListener {
    
    private static final EditorRegistryWatcher INSTANCE = new EditorRegistryWatcher();
    
    // -J-Dorg.netbeans.modules.editor.lib2.actions.EditorRegistryWatcher.level=FINE
    private static final Logger LOG = Logger.getLogger(EditorRegistryWatcher.class.getName());
    
    public static EditorRegistryWatcher get() {
        return INSTANCE;
    }
    
    private WeakReferenceStableList<PresenterUpdater> presenterUpdaters =
            new WeakReferenceStableList<PresenterUpdater>();
    
    private Reference<JTextComponent> activeTextComponentRef;

    private EditorRegistryWatcher() {
        EditorRegistry.addPropertyChangeListener(this);
        activeTextComponentRef = new WeakReference<JTextComponent>(EditorRegistry.focusedComponent());
    }

    public void registerPresenterUpdater(PresenterUpdater updater) {
        presenterUpdaters.add(updater);
        JTextComponent activeTextComponent = activeTextComponentRef.get();
        if (activeTextComponent != null) {
            EditorKit kit = activeTextComponent.getUI().getEditorKit(activeTextComponent);
            if (kit != null) {
                updater.setActiveAction(EditorActionUtilities.getAction(kit, updater.getActionName()));
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (EditorRegistry.FOCUS_LOST_PROPERTY.equals(propName)) {
            // For subsequent focus-gained it would be ideal to schedule a timer
            // that would possibly directly change to a new component.
//            for (PresenterUpdater updater : presenterUpdaters.getList()) {
//                updater.setActiveComponent(null);
//            }
        } else if (EditorRegistry.FOCUS_GAINED_PROPERTY.equals(propName)) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("EditorRegistryWatcher: EditorRegistry.FOCUS_GAINED\n");
            }
            updateActiveActionInPresenters((JTextComponent) evt.getNewValue());
        }
    }
    
    private void updateActiveActionInPresenters(JTextComponent c) {
        if (c == activeTextComponentRef.get()) {
            return;
        }
        activeTextComponentRef = new WeakReference<JTextComponent>(c);
        EditorKit kit = (c != null) ? c.getUI().getEditorKit(c) : null;
        SearchableEditorKit searchableKit = (kit != null) ? EditorActionUtilities.getSearchableKit(kit) : null;
        for (PresenterUpdater updater : presenterUpdaters.getList()) {
            Action a = (searchableKit != null) ? searchableKit.getAction(updater.getActionName()) : null;
            updater.setActiveAction(a);
        }
    }

    public void notifyActiveTopComponentChanged(Component activeTopComponent) {
        if (activeTopComponent != null) {
            JTextComponent activeTextComponent = activeTextComponentRef.get();
            if (activeTextComponent != null) {
                if (!SwingUtilities.isDescendingFrom(activeTextComponent, activeTopComponent)) {
                    // A top component was focused that does not contain focused text component
                    // so notify that there's in fact no active text component
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine("EditorRegistryWatcher: TopComponent without active JTextComponent\n");
                    }
                    updateActiveActionInPresenters(null);
                }
            }
        }
    }

}
