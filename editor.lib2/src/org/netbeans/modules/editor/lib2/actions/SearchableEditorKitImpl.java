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
