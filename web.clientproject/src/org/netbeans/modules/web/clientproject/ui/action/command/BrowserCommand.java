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
package org.netbeans.modules.web.clientproject.ui.action.command;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.spi.platform.ClientProjectEnhancedBrowserImplementation;
import org.netbeans.modules.web.clientproject.util.FileUtilities;
import org.netbeans.spi.project.ActionProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

// unfortunately, this class represents Cordova as well :/
public class BrowserCommand extends Command {

    private final String commandId;


    public BrowserCommand(ClientSideProject project, String commandId) {
        super(project);
        assert commandId != null;
        this.commandId = commandId;
    }

    @Override
    public String getCommandId() {
        return commandId;
    }

    @Override
    boolean isActionEnabledInternal(Lookup context) {
        if (project.isJsLibrary()) {
            return false;
        }
        if (!project.isRunBrowser()) {
            return false;
        }
        if (isFileCommand()
                && isJsFileCommand(context)) {
            return false;
        }
        ActionProvider actionProvider = getBrowserActionProvider();
        if (actionProvider != null
                && isSupportedAction(getCommandId(), actionProvider)) {
            return actionProvider.isActionEnabled(getCommandId(), context);
        }
        return false;
    }

    @Override
    void invokeActionInternal(final Lookup context) {
        if (project.isJsLibrary()) {
            return;
        }
        if (!project.isRunBrowser()) {
            return;
        }
        if (isFileCommand()
                && isJsFileCommand(context)) {
            return;
        }
        project.logBrowserUsage();
        final ActionProvider actionProvider = getBrowserActionProvider();
        if (actionProvider != null) {
            assert isSupportedAction(getCommandId(), actionProvider) : getCommandId() + " :: " + actionProvider;
            if (actionProvider.isActionEnabled(commandId, context)) {
                runInEventThread(new Runnable() {
                    @Override
                    public void run() {
                        actionProvider.invokeAction(getCommandId(), context);
                    }
                });
            }
        }
    }

    public List<String> getSupportedActions() {
        ActionProvider actionProvider = getBrowserActionProvider();
        if (actionProvider == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(actionProvider.getSupportedActions());
    }

    @CheckForNull
    private ActionProvider getBrowserActionProvider() {
        ClientProjectEnhancedBrowserImplementation browserImplementation = project.getEnhancedBrowserImpl();
        if (browserImplementation != null) {
            return browserImplementation.getActionProvider();
        }
        return null;
    }

    private boolean isFileCommand() {
        switch (commandId) {
            case ActionProvider.COMMAND_RUN_SINGLE:
            case ActionProvider.COMMAND_DEBUG_SINGLE:
                return true;
            default:
                return false;
        }
    }

    private boolean isJsFileCommand(Lookup context) {
        FileObject fo = context.lookup(FileObject.class);
        return fo != null
                && FileUtilities.isJavaScriptFile(fo);
    }

}
