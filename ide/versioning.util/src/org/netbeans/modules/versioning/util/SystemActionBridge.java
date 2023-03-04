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

package org.netbeans.modules.versioning.util;

import java.util.MissingResourceException;
import org.openide.util.actions.SystemAction;
import org.openide.util.Lookup;
import org.openide.util.ContextAwareAction;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;

/**
 * Converts NetBeans {@link SystemAction} to Swing's {@link Action}.
 *
 * @author Maros Sandor
 */
public class SystemActionBridge extends AbstractAction {

    /** UI logger to notify about invocation of an action */
    private static Logger UILOG = Logger.getLogger("org.netbeans.ui.SystemActionBridge"); // NOI18N
        
    private Action action;
    private Action delegateAction;

    public static SystemActionBridge createAction(Action action, String name, Lookup context, String actionPathPrefix) {
        Action delegateAction = action;
        if (context != null && action instanceof ContextAwareAction) {
            action = ((ContextAwareAction) action).createContextAwareInstance(context);
        }
        return new SystemActionBridge(action, delegateAction, name, actionPathPrefix);
    }
    
    public static SystemActionBridge createAction(Action action, String name, Lookup context) {
        return createAction(action, name, context, null);
    }

    private SystemActionBridge(Action action, Action delegateAction, String name, String actionPathPrefix) {
        super(name, null);
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
        this.action = action;
        this.delegateAction = delegateAction;
        if(actionPathPrefix != null) {
            Utils.setAcceleratorBindings(actionPathPrefix, this);
        }
    }

    public SystemActionBridge(Action action, String name, String actionPathPrefix) {
        this(action, action, name, actionPathPrefix);
    }
    
    public SystemActionBridge(Action action, String name) {
        this(action, action, name, null);
    }

    public void actionPerformed(ActionEvent e) {
        log();
        action.actionPerformed(e);
    }

    public boolean isEnabled() {
        return action.isEnabled();
    }

    Action getDelegate() {
        return action;
    }
    
    private void log() throws MissingResourceException {
        LogRecord rec = new LogRecord(Level.FINER, "UI_ACTION_BUTTON_PRESS"); // NOI18N
        rec.setParameters(new Object[]{"", "", delegateAction, delegateAction.getClass().getName(), action.getValue(Action.NAME)});
        rec.setResourceBundle(NbBundle.getBundle(Actions.class));
        rec.setResourceBundleName(Actions.class.getPackage().getName() + ".Bundle"); // NOI18N
        rec.setLoggerName(UILOG.getName());
        UILOG.log(rec);
    }
}
