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
package org.netbeans.modules.javascript.nodejs.ui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.javascript.nodejs.ui.actions.NpmRunScriptAction", category = "Build")
@ActionRegistration(displayName = "#NpmRunScriptAction.name", lazy = false)
@ActionReferences({
    // #250300
    //@ActionReference(path = "Editors/text/package+x-json/Popup", position = 907),
    @ActionReference(path = "Loaders/text/package+x-json/Actions", position = 157),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 171),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 111),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 651),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 751),
})
@NbBundle.Messages("NpmRunScriptAction.name=npm Scripts")
public class NpmRunScriptAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    private static final String PRE_PREFIX = "pre"; // NOI18N
    private static final String POST_PREFIX = "post"; // NOI18N
    private static final String INSTALL_COMMAND = "install"; // NOI18N

    static final RequestProcessor RP = new RequestProcessor(NpmRunScriptAction.class);

    final Project project;
    final List<String> scripts = new CopyOnWriteArrayList<>();


    public NpmRunScriptAction() {
        this(null, null);
    }

    public NpmRunScriptAction(Project project, Collection<String> scripts) {
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
        Pair<Project, PackageJson> data = NodeJsUtils.getProjectAndPackageJson(context);
        Project contextProject = data.first();
        if (contextProject == null) {
            return this;
        }
        PackageJson packageJson = data.second();
        if (packageJson == null) {
            return this;
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> allScripts = packageJson.getContentValue(Map.class, PackageJson.FIELD_SCRIPTS);
        if (allScripts == null
                || allScripts.isEmpty()) {
            return this;
        }
        List<String> allScriptsList = sanitizeScripts(allScripts.keySet());
        if (allScriptsList.isEmpty()) {
            return this;
        }
        return new NpmRunScriptAction(contextProject, allScriptsList);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (project == null) {
            return new Actions.MenuItem(this, false);
        }
        return createScriptsMenu();
    }

    private List<String> sanitizeScripts(Collection<String> allScripts) {
        Set<String> allCommands = new HashSet<>();
        for (String script : allScripts) {
            String command;
            if (script.startsWith(PRE_PREFIX)) {
                command = script.substring(PRE_PREFIX.length());
            } else if (script.startsWith(POST_PREFIX)) {
                command = script.substring(POST_PREFIX.length());
            } else {
                command = script;
            }
            if (INSTALL_COMMAND.equals(command)) {
                // we have special action for it
                continue;
            }
            if (StringUtilities.hasText(command)) {
                allCommands.add(command);
            }
        }
        List<String> commands = new ArrayList<>(allCommands);
        Collections.sort(commands);
        return commands;
    }

    private JMenuItem createScriptsMenu() {
        assert project != null;
        assert !scripts.isEmpty();
        JMenu menu = new JMenu(Bundle.NpmRunScriptAction_name());
        for (final String command : scripts) {
            JMenuItem menuItem = new JMenuItem(command);
            menuItem.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    final NpmExecutable npm = NpmExecutable.getDefault(project, true);
                    if (npm == null) {
                        return;
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            NodeJsUtils.logUsageNpmRunScript(command);
                            npm.runScript(command);
                        }
                    });
                }
            });
            menu.add(menuItem);
        }
        return menu;
    }

}
