/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.search;

//import org.netbeans.modules.search.project.SearchScopeNodeSelection;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.ActionMap;
import org.openide.actions.FindAction;
import org.openide.actions.ReplaceAction;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Mutex;
import org.openide.util.SharedClassObject;
import org.openide.util.WeakSet;
import org.openide.util.actions.CallbackSystemAction;
import org.openide.util.actions.SystemAction;
import org.openide.windows.TopComponent;

/**
 * Generic template for FindActionManager and ReplaceActionManger. It provides
 * fallback shortcuts for some windows.
 *
 * @author jhavlin
 *
 * @param <A> New action type, e.g. FindInFilesAction
 * @param <S> Original action type, e.g. FindAction
 * @param <L> Lookup sensitive type, e.g.g FindInFilesAction.LookupSensitive
 */
public abstract class ActionManager<A extends SystemAction, S extends CallbackSystemAction>
        implements PropertyChangeListener, Runnable {

    protected static final Logger LOG =
            Logger.getLogger(ActionManager.class.getName());
    /**
     * Search perfomer.
     */
    protected final A action;
    /**
     * holds set of windows for which their ActionMap was modified
     */
    private final Set<TopComponent> activatedOnWindows =
            new WeakSet<>(8);
    /**
     *
     */
    private Object actionMapKey;
    /**
     * Holds class {@code SearchScopeNodeSelection.LookupSensitive}. See Bug
     * #183434.
     */
    //private Class<SearchScopeNodeSelection> ssnslsClass;
    /**
     * Holds e.g. class {@code FindInFilesAction.LookupSensitive}. See Bug
     * #183434.
     */
    private Class<S> origSysActionCls;

    /**
     * Constructor.
     */
    protected ActionManager(Class<A> actionCls, Class<S> origSysActionCls) {
        this.origSysActionCls = origSysActionCls;
        action = SharedClassObject.findObject(actionCls, true);
    }

    /**
     * Initialization, called from Installer.
     */
    void init() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
        Mutex.EVENT.writeAccess(this);

        // Fix of the Bug #183434 - caching of the classes to avoid their 
        // loading during execution of the action
        //ssnslsClass = SearchScopeNodeSelection.class;
    }

    @Override
    public void run() {
        someoneActivated();
    }

    private void someoneActivated() {
        TopComponent win = TopComponent.getRegistry().getActivated();
        if (LOG.isLoggable(Level.FINER)) {
            String winId;
            if (win == null) {
                winId = "<null>";
            } else {
                String winName = win.getDisplayName();
                if (winName == null) {
                    winName = win.getHtmlDisplayName();
                }
                if (winName == null) {
                    winName = win.getName();
                }
                if (winName != null) {
                    winName = '"' + winName + '"';
                } else {
                    winName = "<noname>";
                }
                winId = winName + '(' + win.getClass().getName() + ')';
            }
            LOG.log(Level.FINER, "someoneActivated ({0})", winId);
        }

        if ((win == null) || (win instanceof CloneableEditorSupport.Pane)) {
            return;
        }

        Object key = getActionMapKey();
        ActionMap actionMap = win.getActionMap();

        if ((actionMap.get(key) == null) && activatedOnWindows.add(win)) {
            Action ls = getAction();
            actionMap.put(key, ls);
            win.putClientProperty(getMappedActionKey(),
                    new WeakReference<>(ls));
        }
    }

    /**
     * Implements
     * <code>PropertyChangeListener</code>. Be interested in current_nodes
     * property change.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if (TopComponent.Registry.PROP_ACTIVATED.equals(propName)) {
            someoneActivated();
        }
    }

    private Object getActionMapKey() {
        if (actionMapKey == null) {
            S systemAction =
                    SharedClassObject.findObject(origSysActionCls, true);
            assert systemAction != null;

            actionMapKey = systemAction.getActionMapKey();
        }
        return actionMapKey;
    }

    /**
     * Get key for JComponent property under which the reference to the
     * underlying action will be stored.
     */
    public abstract String getMappedActionKey();

    protected abstract Action getAction();

    /**
     * Manages <em>FindAction</em> - provides fallback shortcut Ctrl-Shift-F.
     *
     * @author Petr Kuzel
     * @author Marian Petras
     * @see org.openide.actions.FindAction
     * @see org.openide.windows.TopComponent.Registry
     */
    static final class FindActionManager
            extends ActionManager<FindInFilesAction.Selection, FindAction> {

        private static final String MAPPED_FIND_ACTION =
                FindActionManager.class.getName()
                + " - FindActionImpl";                                  //NOI18N
        private static FindActionManager instance = null;

        private FindActionManager() {
            super(FindInFilesAction.Selection.class, FindAction.class);
        }

        @Override
        public String getMappedActionKey() {
            return FindActionManager.MAPPED_FIND_ACTION;
        }

        @Override
        protected Action getAction() {
            return action;
        }

        static FindActionManager getInstance() {
            LOG.finer("getInstance()");                                 //NOI18N
            if (instance == null) {
                instance = new FindActionManager();
            }
            return instance;
        }
    }

    /**
     * Manages <em>ReplaceAction</em> - provides fallback shortcut Ctrl-Shift-H.
     *
     * @author Petr Kuzel
     * @author Marian Petras
     * @see org.openide.actions.ReplaceAction
     * @see org.openide.windows.TopComponent.Registry
     */
    static final class ReplaceActionManager
            extends ActionManager<ReplaceInFilesAction.Selection, ReplaceAction> {

        private static final String MAPPED_FIND_ACTION =
                ReplaceActionManager.class.getName()
                + " - ReplActionImpl";                                  //NOI18N
        private static ReplaceActionManager instance = null;

        private ReplaceActionManager() {
            super(ReplaceInFilesAction.Selection.class, ReplaceAction.class);
        }

        @Override
        public String getMappedActionKey() {
            return ReplaceActionManager.MAPPED_FIND_ACTION;
        }

        @Override
        protected Action getAction() {
            return action;
        }

        static ReplaceActionManager getInstance() {
            LOG.finer("getInstance()");                                 //NOI18N
            if (instance == null) {
                instance = new ReplaceActionManager();
            }
            return instance;
        }
    }
}
