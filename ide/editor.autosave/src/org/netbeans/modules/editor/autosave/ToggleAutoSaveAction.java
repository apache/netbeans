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
package org.netbeans.modules.editor.autosave;

import java.util.prefs.PreferenceChangeListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.BooleanStateAction;

@ActionID(id = "org.netbeans.modules.editor.autosave.ToggleAutoSaveAction", category = "Edit")
@ActionRegistration(displayName = "#CTL_AutoSave", lazy = false)
@NbBundle.Messages("CTL_AutoSave=Autosave")
public final class ToggleAutoSaveAction extends BooleanStateAction {

    @StaticResource
    public static final String ICON = "org/netbeans/modules/editor/autosave/auto_save.png"; //NOI18N

    private final PreferenceChangeListener pcl = evt -> setBooleanState(AutoSaveController.prefs()
            .getBoolean(AutoSaveController.KEY_ACTIVE, AutoSaveController.KEY_ACTIVE_DEFAULT));

    @Override
    public String getName() {
        return NbBundle.getMessage(ToggleAutoSaveAction.class, "CTL_AutoSave");
    }

    @Override
    protected void initialize() {
        super.initialize();

        AutoSaveController.prefs().addPreferenceChangeListener(pcl);

        this.addPropertyChangeListener(evt -> {
            if (BooleanStateAction.PROP_BOOLEAN_STATE.equals(evt.getPropertyName())) {
                AutoSaveController.prefs().putBoolean(AutoSaveController.KEY_ACTIVE, getBooleanState());
            }
        });
        this.setBooleanState(AutoSaveController.prefs().getBoolean(AutoSaveController.KEY_ACTIVE,
                AutoSaveController.KEY_ACTIVE_DEFAULT));
    }

    @Override
    protected String iconResource() {
        return ICON;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
}
