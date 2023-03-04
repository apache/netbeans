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
package org.netbeans.modules.php.composer.ui.actions;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.php.composer.files.ComposerJson;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.php.composer.ui.actions.ComposerScriptsAction", category = "Project")
@ActionRegistration(displayName = "#ComposerScriptsAction.name", lazy = false)
@ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 1051, separatorAfter = 1052)
@NbBundle.Messages("ComposerScriptsAction.name=Composer Scripts")
public class ComposerScriptsAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private static final long serialVersionUID = -5103436278063173440L;

    @NullAllowed
    final Project project;
    final List<String> scripts = new CopyOnWriteArrayList<>();

    public ComposerScriptsAction() {
        this(null, null);
    }

    public ComposerScriptsAction(Project project, Collection<String> scripts) {
        this.project = project;
        if (scripts != null) {
            this.scripts.addAll(scripts);
        }
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        // hide this action from Tools > Keymap
        putValue(Action.NAME, ""); // NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        Project contextProject = context.lookup(Project.class);
        if (contextProject == null) {
            return this;
        }
        PhpModule phpModule = PhpModule.Factory.lookupPhpModule(context);
        if (phpModule == null) {
            return this;
        }
        ComposerJson composerJson = new ComposerJson(phpModule.getProjectDirectory());
        Set<String> allScripts = composerJson.getScripts();
        if (allScripts.isEmpty()) {
            return this;
        }
        List<String> orderedScripts = new ArrayList<>(allScripts);
        Collections.sort(orderedScripts);
        return new ComposerScriptsAction(contextProject, orderedScripts);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (project == null) {
            return new Actions.MenuItem(this, false);
        }
        return createScriptsMenu();
    }

    private JMenuItem createScriptsMenu() {
        assert project != null;
        assert !scripts.isEmpty();
        JMenu menu = new JMenu(Bundle.ComposerScriptsAction_name());
        for (final String command : scripts) {
            RunScriptAction scriptAction = new RunScriptAction(command);
            menu.add(scriptAction);
        }
        return menu;
    }

    //~ Inner classes
    private static class RunScriptAction extends BaseComposerAction {

        private final String script;

        public RunScriptAction(String script) {
            this.script = script;
            putValue("noIconInMenu", true); // NOI18N
            putValue(NAME, script);
            putValue(SHORT_DESCRIPTION, script);
            putValue("menuText", script); // NOI18N
        }

        @Override
        protected String getName() {
            return script;
        }

        @Override
        protected void runCommand(PhpModule phpModule) throws InvalidPhpExecutableException {
            Composer.getDefault().runScript(phpModule, script);
        }

    }

}
