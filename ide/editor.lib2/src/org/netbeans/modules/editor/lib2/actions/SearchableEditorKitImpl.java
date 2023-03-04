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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultEditorKit;
import org.netbeans.lib.editor.util.ListenerList;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 * This interface should be implemented by editor kits that hold their actions
 * in a map. They may also notify
 *
 * @since 1.13
 */
public final class SearchableEditorKitImpl extends DefaultEditorKit implements SearchableEditorKit {

    // -J-Dorg.netbeans.modules.editor.lib2.actions.SearchableEditorKitImpl.level=FINEST
    private static final Logger LOG = Logger.getLogger(SearchableEditorKitImpl.class.getName());

    private final String mimeType;

    private final Map<String,Action> name2Action = new HashMap<String,Action>();

    private Action[] actions;
    
    private LookupListener actionsListener;

    private ListenerList<ChangeListener> listenerList = new ListenerList<ChangeListener>();

    SearchableEditorKitImpl(String mimeType) {
        this.mimeType = mimeType;
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("SearchableEditorKitImpl created for \"" + mimeType + "\"\n"); // NOI18N
        }
        updateActions();
    }

    public Action getAction(String actionName) {
        synchronized (name2Action) {
            return name2Action.get(actionName);
        }
    }

    private void updateActions() {
        synchronized (name2Action) {
            // Fill up the actions from layer
            Lookup.Result<Action> actionsResult = EditorActionUtilities.createActionsLookupResult(mimeType);
            Collection<? extends Action> actionColl = actionsResult.allInstances();
            actions = new Action[actionColl.size()];
            actionColl.toArray(actions);
            name2Action.clear();
            for (Action action : actions) {
                String actionName;
                if (action != null && (actionName = (String) action.getValue(Action.NAME)) != null) {
                    name2Action.put(actionName, action);
                    if (LOG.isLoggable(Level.FINER)) {
                        LOG.finer("Mime-type: \"" + mimeType + "\", registerAction(\"" + actionName + // NOI18N
                                "\", " + action + ")\n"); // NOI18N
                    }
                }
            }

            if (actionsListener == null) {
                actionsListener = new LookupListener() {
                    public void resultChanged(LookupEvent ev) {
                        updateActions();
                    }
                };
                actionsResult.addLookupListener(actionsListener);
            }
        }

        // Fire change listeners
        fireActionsChange();
    }

    @Override
    public String getContentType() {
        return mimeType;
    }

    public void addActionsChangeListener(ChangeListener listener) {
        listenerList.add(listener);
    }

    public void removeActionsChangeListener(ChangeListener listener) {
        listenerList.remove(listener);
    }

    private void fireActionsChange() {
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener listener : listenerList.getListeners()) {
            listener.stateChanged(evt);
        }
    }

}
