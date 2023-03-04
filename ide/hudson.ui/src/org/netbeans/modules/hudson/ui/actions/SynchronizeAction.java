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

package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.hudson.api.HudsonInstance;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.SynchronizeAction")
@ActionRegistration(displayName="#LBL_SynchronizeAction", iconInMenu=false, lazy=false)
@ActionReference(path=HudsonInstance.ACTION_PATH, position=500)
@Messages("LBL_SynchronizeAction=&Synchronize")
public class SynchronizeAction extends AbstractAction implements ContextAwareAction {

    private final Collection<? extends HudsonInstance> instances;

    public SynchronizeAction() {
        this(Collections.<HudsonInstance>emptySet());
    }

    @Override public Action createContextAwareInstance(Lookup actionContext) {
        return new SynchronizeAction(actionContext.lookupAll(HudsonInstance.class));
    }

    @Messages("LBL_SynchronizeAction_disconnected=Connect")
    private SynchronizeAction(Collection<? extends HudsonInstance> instances) {
        this.instances = instances;
        boolean allForbidden = true;
        boolean allDisconnected = true;
        for (HudsonInstance instance : instances) {
            if (!instance.isForbidden()) {
                allForbidden = false;
            }
            if (instance.isConnected()) {
                allDisconnected = false;
            }
        }
        if (allForbidden) {
            // LogInAction would do the same thing, so confusing to show this as well.
            setEnabled(false);
            putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        } else if (allDisconnected) {
            putValue(NAME, LBL_SynchronizeAction_disconnected());
        } else {
            putValue(NAME, LBL_SynchronizeAction());
        }
    }
    
    public @Override void actionPerformed(ActionEvent e) {
        for (HudsonInstance instance : instances) {
            instance.synchronize(true);
        }
    }

}
