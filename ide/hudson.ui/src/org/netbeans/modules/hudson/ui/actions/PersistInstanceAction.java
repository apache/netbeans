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

import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.openide.util.ContextAwareAction;
import java.awt.event.ActionEvent;
import java.util.Collections;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.api.Utilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import static org.netbeans.modules.hudson.ui.actions.Bundle.*;

@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.PersistInstanceAction")
@ActionRegistration(displayName="#LBL_Persist_Instance", iconInMenu=false, lazy=false)
@ActionReference(path=HudsonInstance.ACTION_PATH, position=700)
@Messages("LBL_Persist_Instance=&Persist")
// XXX cannot just use List<HudsonInstanceImpl> ctor: must be disabled when context menu created, so must be eager
public class PersistInstanceAction extends AbstractAction implements ContextAwareAction {

    public PersistInstanceAction() {
        this(Collections.<HudsonInstance>emptyList());
    }

    public @Override Action createContextAwareInstance(Lookup actionContext) {
        return new PersistInstanceAction(actionContext.lookupAll(HudsonInstance.class));
    }

    private final Collection<? extends HudsonInstance> instances;

    private PersistInstanceAction(Collection<? extends HudsonInstance> instances) {
        super(LBL_Persist_Instance());
        this.instances = instances;
        for (HudsonInstance instance : instances) {
            if (instance.isPersisted()) {
                setEnabled(false);
                break;
            }
        }
    }

    public @Override void actionPerformed(ActionEvent e) {
        for (HudsonInstance instance : instances) {
            Utilities.persistInstance(instance);
        }
    }

}
