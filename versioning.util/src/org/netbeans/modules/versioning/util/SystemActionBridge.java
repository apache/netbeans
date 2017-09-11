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
