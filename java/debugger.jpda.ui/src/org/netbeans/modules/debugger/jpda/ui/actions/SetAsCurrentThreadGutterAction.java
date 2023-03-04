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

package org.netbeans.modules.debugger.jpda.ui.actions;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter.Popup;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Martin
 */
public class SetAsCurrentThreadGutterAction extends SystemAction implements ContextAwareAction  {
    
    public SetAsCurrentThreadGutterAction() {
        setEnabled(false);
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(SetAsCurrentThreadGutterAction.class, "CTL_setAsCurrentThread");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    @Override
    public void actionPerformed(ActionEvent ev) {
    }
    
    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends Lookup.Provider> annotationLookupProviders =
                actionContext.lookupAll(Lookup.Provider.class);
        List<JPDAThread> threads = new ArrayList<JPDAThread>(annotationLookupProviders.size());
        for (Lookup.Provider lp : annotationLookupProviders) {
            threads.addAll(lp.getLookup().lookupAll(JPDAThread.class));
        }
        if (threads.size() > 0) {
            return new ThreadAwareAction(threads);
        } else {
            return this;
        }
        /*
        Lookup.Provider annotationLookupProvider = actionContext.lookup(Lookup.Provider.class);
        //System.err.println("SetAsCurrentThreadGutterAction: actionContext = "+actionContext+", lookupProvider = "+annotationLookupProvider);
        if (annotationLookupProvider != null) {
            JPDAThread thread = annotationLookupProvider.getLookup().lookup(JPDAThread.class);
            //System.err.println("SetAsCurrentThreadGutterAction, lookup = "+annotationLookupProvider.getLookup()+", ALL threads = "+annotationLookupProvider.getLookup().lookupAll(JPDAThread.class));
            return new ThreadAwareAction(thread);
        } else {
            //Exceptions.printStackTrace(new IllegalStateException("expecting BreakpointAnnotation object in lookup "+actionContext));
            return this;
        }
         */
    }
    
    private static class ThreadAwareAction implements Action, Popup {
        
        private List<JPDAThread> threads;
        
        public ThreadAwareAction(List<JPDAThread> threads) {
            this.threads = threads;
        }

        private ThreadAwareAction(JPDAThread thread) {
            this.threads = Collections.singletonList(thread);
        }

        @Override
        public Object getValue(String key) {
            if (Action.NAME.equals(key)) {
                return NbBundle.getMessage(SetAsCurrentThreadGutterAction.class, "CTL_setAsCurrentThreadT", threads.get(0).getName());
            } else {
                return null;
            }
        }

        @Override
        public void putValue(String key, Object value) {}

        @Override
        public void setEnabled(boolean b) {}

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {}

        @Override
        public void actionPerformed(ActionEvent e) {
            threads.get(0).makeCurrent();
        }

        @Override
        public JMenuItem getPopupPresenter() {
            if (threads.size() == 1) {
                return new Actions.MenuItem (this, false);
            } else {
                return new MultiThreadsMenu();
            }
        }

        private class MultiThreadsMenu extends JMenuItem implements DynamicMenuContent {

            @Override
            public JComponent[] getMenuPresenters() {
                JComponent[] cs = new JComponent[threads.size()];
                for (int i = 0; i < cs.length; i++) {
                    cs[i] = new ThreadAwareAction(threads.get(i)).getPopupPresenter();
                }
                return cs;
            }

            @Override
            public JComponent[] synchMenuPresenters(JComponent[] items) {
                return items;
            }

        }
        
    }
    
}
