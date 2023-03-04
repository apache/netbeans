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

package org.netbeans.modules.project.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.openide.awt.DynamicMenuContent;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;

/** Action sensitive to current project
 * @author Petr Hrebejk
 */
abstract class LookupSensitiveAction extends BasicAction implements Runnable, LookupListener, Presenter.Popup, Presenter.Menu {
    static final Logger UILOG = Logger.getLogger("org.netbeans.ui.actions"); // NOI18N
    private static final Logger LOG = Logger.getLogger(LookupSensitiveAction.class.getName());
    protected static final RequestProcessor RP = new RequestProcessor(LookupSensitiveAction.class);

    private Lookup lookup;
    private Class<?>[] watch;    
    private boolean needsRefresh = true;
    
    private final Object RESULTS_LOCK = new Object();
    private boolean initialized = false;
    private Lookup.Result results[];

    private boolean refreshing = false;

    /**
     * Constructor for global actions. E.g. actions in main menu which
     * listen to the global context.
     */
    public LookupSensitiveAction(Icon icon, Lookup lookup, Class[] watch ) {
        super( null, icon );
        if (lookup == null) {
            lookup = LastActivatedWindowLookup.INSTANCE;
        }
        this.lookup = lookup;
        this.watch = watch;
    }

    /** Called when there may be a need for initialization.
     *
     * @return true if subclasses shall initialize themselves
     */
    protected boolean init () { 
        synchronized (RESULTS_LOCK) {//synchronized == issue 215335
        if (initialized) {
            return false;
        }
        this.results = new Lookup.Result[watch.length];
        // Needs to listen on changes in results
        for ( int i = 0; i < watch.length; i++ ) {
            results[i] = lookup.lookupResult(watch[i]);
            results[i].allItems();
            LookupListener resultListener = WeakListeners.create(LookupListener.class, this, results[i]);
            results[i].addLookupListener( resultListener );
        }
        initialized = true;
        return true;
        }
    }

    /** Needs to override getValue in order to force refresh
     */
    public @Override Object getValue( String key ) {
        init ();
        if ( needsRefresh ) {
            doRefresh(true);
        }
        return super.getValue( key );
    }

    /** Needs to override isEnabled in order to force refresh
     */
    public @Override boolean isEnabled() {
        init ();
        if ( needsRefresh ) {
            doRefresh(true);
        }
        return super.isEnabled();
    }

    @Override
    public final void actionPerformed( ActionEvent e ) {
        init ();

        if (UILOG.isLoggable(Level.FINE)) {
            LogRecord r;
            boolean isKey;
            if (e.getSource() instanceof JMenuItem) {
                isKey = false;
            } else if (e.getSource() instanceof JButton) {
                isKey = false;
            } else {
                isKey = true;
            }

            if (!isKey) {
                r = new LogRecord(Level.FINE, "UI_ACTION_BUTTON_PRESS"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(LookupSensitiveAction.class));
                r.setParameters(new Object[] {
                    e.getSource(),
                    e.getSource().getClass().getName(),
                    this,
                    getClass().getName(),
                    getValue(NAME)
                });
                r.setLoggerName(UILOG.getName());
                UILOG.log(r);
            }
        }

        actionPerformed( lookup );
    }

    protected final Lookup getLookup() {
        return lookup;
    }

    private void doRefresh(boolean immediate) {
        if (refreshing) {
            return;
        }
        refreshing = true;
        try {
            if (LOG.isLoggable(Level.FINER)) {
                LogRecord r = new LogRecord(Level.FINER, "LOG_ACTION_REFRESH"); // NOI18N
                r.setResourceBundle(NbBundle.getBundle(LookupSensitiveAction.class));
                r.setParameters(new Object[]{
                    getClass(),
                    lookup
                });
                r.setLoggerName(LOG.getName());
                LOG.log(r);
            }
            refresh(lookup, immediate);
        } finally {
            refreshing = false;
        }
        needsRefresh = false;
    }

    // Abstract methods --------------------------------------------------------

    /** Called when the action is performed
     */
    protected abstract void actionPerformed( Lookup context );

    /** Place where to change properties (enablement/name) when
     *  the set of current projects changes.
     */
    protected abstract void refresh(Lookup context, boolean immediate);

    // Implementation of LookupListener ----------------------------------------

    @Override
    public void resultChanged( LookupEvent e ) {
        if ( refreshing ) {
            return;
        }
        else if ( getPropertyChangeListeners().length == 0 ) {
            needsRefresh = true;
        }
        else {
            Mutex.EVENT.readAccess(this);
        }
    }

    @Override
    public void run() {
        doRefresh(false);
    }

    // Implementation of Presenter.Menu and Presenter.Popup --------------------
    
    @Override
    public JMenuItem getMenuPresenter () {
        return new DynamicMenuItem(this, false);
    }
    
    @Override
    public JMenuItem getPopupPresenter () {
        return new DynamicMenuItem(this, true);
    }

    private class DynamicMenuItem extends JMenuItem implements DynamicMenuContent {
        
        private AbstractAction action;
        private boolean popup;
        
        public DynamicMenuItem(AbstractAction action, boolean popup) {
            this.action = action;
            this.popup = popup;
            org.openide.awt.Actions.connect(this, action, popup);
        }
        
        @Override
        public JComponent[] getMenuPresenters() {
            JMenuItem menuPresenter = new JMenuItem();
            org.openide.awt.Actions.connect(menuPresenter, action, popup);
            return new JComponent [] { menuPresenter };
        }
        
        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return getMenuPresenters();
        }
        
    }
    
    /**
     * #120721: do not want to use Utilities.actionsGlobalContext since that does not survive focus change,
     * and we would like to mimic the selection tracking behavior of Hacks.keepCurrentProjectNameUpdated.
     */
    static final class LastActivatedWindowLookup extends ProxyLookup implements PropertyChangeListener {

        static final Lookup INSTANCE = new LastActivatedWindowLookup();

        private final TopComponent.Registry reg = TopComponent.getRegistry();

        LastActivatedWindowLookup() {
            reg.addPropertyChangeListener(this);
            updateLookups();
        }

        private void updateLookups() {
            Node[] nodes = reg.getActivatedNodes();
            Lookup[] delegates = new Lookup[nodes.length];
            for (int i = 0; i < nodes.length; i++) {
                delegates[i] = nodes[i].getLookup();
            }
            setLookups(delegates);
        }

        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                updateLookups();
            }
        }

    }

}
