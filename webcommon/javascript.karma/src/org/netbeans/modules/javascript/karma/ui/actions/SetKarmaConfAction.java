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
package org.netbeans.modules.javascript.karma.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

@NbBundle.Messages("SetKarmaConfAction.name=Set Karma Configuration")
@ActionID(id = "org.netbeans.modules.javascript.karma.ui.actions.SetKarmaConfAction", category = "Unit Tests")
@ActionRegistration(displayName = "#SetKarmaConfAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/javascript/Popup", position = 907),
    @ActionReference(path = "Loaders/text/javascript/Actions", position = 157),
})
public final class SetKarmaConfAction extends AbstractAction implements ContextAwareAction {

    @NullAllowed
    private final Project project;
    @NullAllowed
    private final FileObject karmaConfJs;


    public SetKarmaConfAction() {
        this(null, null);
    }

    public SetKarmaConfAction(Project project, FileObject karmaConfJs) {
        this.project = project;
        this.karmaConfJs = karmaConfJs;
        setEnabled(
                karmaConfJs != null
                && karmaConfJs.getName().startsWith("karma")
                && karmaConfJs.getName().endsWith(".conf")
        );
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        putValue(Action.NAME, Bundle.SetKarmaConfAction_name());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert project != null;
        assert karmaConfJs != null;
        KarmaPreferences.setConfig(project, FileUtil.toFile(karmaConfJs).getAbsolutePath());
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            DataObject dataObject = context.lookup(DataObject.class);
            if (dataObject != null) {
                file = dataObject.getPrimaryFile();
            }
        }
        if (file == null) {
            return this;
        }
        Project owner = FileOwnerQuery.getOwner(file);
        if (owner == null) {
            return this;
        }
        if (!KarmaPreferences.isEnabled(owner)) {
            return this;
        }
        return new SetKarmaConfAction(owner, file);
    }

}
