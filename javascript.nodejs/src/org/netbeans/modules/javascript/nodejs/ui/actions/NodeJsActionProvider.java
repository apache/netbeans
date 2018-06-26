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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public final class NodeJsActionProvider implements ActionProvider {

    private static final RequestProcessor RP = new RequestProcessor(NodeJsActionProvider.class);

    private final Project project;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private final List<String> supportedActions;


    public NodeJsActionProvider(Project project) {
        assert project != null;
        this.project = project;
        fillCommands();
        supportedActions = new ArrayList<>(commands.keySet());
    }

    private void fillCommands() {
        Command[] allCommands = new Command[] {
            new RunProjectCommand(project),
            new RunFileCommand(project),
            new DebugProjectCommand(project),
            new DebugFileCommand(project),
        };
        for (Command command : allCommands) {
            Command old = commands.put(command.getCommandId(), command);
            assert old == null : "Command already set for " + command.getCommandId();
        }
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions.toArray(new String[supportedActions.size()]);
    }

    @Override
    public void invokeAction(String command, final Lookup context) {
        final Command runCommand = commands.get(command);
        assert runCommand != null : command;
        RP.post(new Runnable() {
            @Override
            public void run() {
                runCommand.run(context);
            }
        });
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) {
        Command runCommand = commands.get(command);
        if (runCommand == null) {
            return false;
        }
        return runCommand.isEnabled(context);
    }

}
