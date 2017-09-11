/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
