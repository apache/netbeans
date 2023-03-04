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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.util.ContextAwareAction;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Andrei Badea
 */
public abstract class SQLExecutionBaseAction extends AbstractAction implements ContextAwareAction, HelpCtx.Provider {

    public SQLExecutionBaseAction() {
        initialize();
        
        // allow subclasses to set the name for the "master" action
        if (getValue(Action.NAME) == null) {
            putValue(Action.NAME, getDisplayName(null));
        }
        String iconBase = getIconBase();
        if (iconBase != null) {
            putValue("iconBase", iconBase);
        }
    }
    
    protected void initialize() {
        // allows subclasses to e.g. set noIconInMenu
    }

    protected abstract String getDisplayName(SQLExecution sqlExecution);

    protected String getIconBase() {
        return null;
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    protected boolean enable(SQLExecution sqlExecution) {
        return !sqlExecution.isExecuting();
    }

    protected abstract void actionPerformed(SQLExecution sqlExecution);

    public void actionPerformed(ActionEvent e) {
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new ContextAwareDelegate(this, actionContext);
    }

    public static void notifyNoDatabaseConnection() {
        String message = NbBundle.getMessage(SQLExecutionBaseAction.class, "LBL_NoDatabaseConnection");
        NotifyDescriptor desc = new NotifyDescriptor.Message(message, NotifyDescriptor.WARNING_MESSAGE);
        DialogDisplayer.getDefault().notify(desc);
    }

    public static DatabaseConnection selectDatabaseConnection() {
        return SelectConnectionPanel.selectConnection(false);
    }

    static class ContextAwareDelegate extends AbstractAction implements Presenter.Toolbar, HelpCtx.Provider {

        private final SQLExecutionBaseAction parent;
        private final Lookup.Result<SQLExecution> result;

        private SQLExecution sqlExecution;
        private PropertyChangeListener listener;

        public ContextAwareDelegate(SQLExecutionBaseAction parent, Lookup actionContext) {
            this.parent = parent;

            result = actionContext.lookup(new Lookup.Template<SQLExecution>(SQLExecution.class));
            result.addLookupListener(new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    ContextAwareDelegate.this.resultChanged();
                }
            });
            resultChanged();
        }

        protected synchronized void setSQLExecution(SQLExecution sqlExecution) {
            this.sqlExecution = sqlExecution;
        }

        protected synchronized SQLExecution getSQLExecution() {
            return sqlExecution;
        }

        private synchronized void resultChanged() {
            if (sqlExecution != null) {
                sqlExecution.removePropertyChangeListener(listener);
            }

            Iterator<? extends SQLExecution> iterator = result.allInstances().iterator();
            if (iterator.hasNext()) {
                setSQLExecution((SQLExecution)iterator.next());
                listener = new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        propertyChanged(evt.getPropertyName());
                    }
                };
                sqlExecution.addPropertyChangeListener(listener);
                propertyChanged(null);

                if (iterator.hasNext()) {
                    Logger.getLogger("global").log(Level.WARNING, "Multiple SQLExecution instances in the action context. Will only use the first one."); // NOI18N
                }
            } else {
                setSQLExecution(null);
                listener = null;
                propertyChanged(null);
            }
        }

        private void propertyChanged(String propertyName) {
            if (propertyName == null || SQLExecution.PROP_EXECUTING.equals(propertyName)) {
                Mutex.EVENT.readAccess(new Runnable() {
                    public void run() {
                        boolean enabled = false;
                        SQLExecution sqlExecution = getSQLExecution();
                        if (sqlExecution != null) {
                            enabled = parent.enable(sqlExecution);
                        }
                        String name = parent.getDisplayName(sqlExecution);

                        setEnabled(enabled);
                        putValue(Action.NAME, name);
                    }
                });
            }
        }

        public void actionPerformed(ActionEvent e) {
            SQLExecution sqlExec = getSQLExecution();
            if (sqlExec != null) {
                parent.actionPerformed(sqlExec);
            }
        }

        @Override
        public Object getValue(String key) {
            Object value = super.getValue(key);
            if (value == null) {
                value = parent.getValue(key);
            }
            return value;
        }

        public HelpCtx getHelpCtx() {
            return parent.getHelpCtx();
        }

        public Component getToolbarPresenter() {
            JButton button = new JButton();
            Actions.connect(button, this);
            return button;
        }
    }
}
