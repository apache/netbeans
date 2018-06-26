/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
