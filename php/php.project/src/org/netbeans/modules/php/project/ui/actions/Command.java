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
package org.netbeans.modules.php.project.ui.actions;

import java.util.logging.Logger;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.PhpProjectValidator;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.actions.support.CommandUtils;
import org.netbeans.modules.php.project.ui.actions.support.ConfigAction;
import org.netbeans.modules.php.spi.testing.PhpTestingProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * @author Radek Matous, Tomas Mysik
 */
public abstract class Command {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    private final PhpProject project;

    public Command(PhpProject project) {
        assert project != null;
        this.project = project;
    }

    public abstract String getCommandId();

    public abstract boolean isActionEnabledInternal(Lookup context);

    public abstract void invokeActionInternal(Lookup context);

    public final boolean isActionEnabled(Lookup context) {
        if (PhpProjectValidator.isFatallyBroken(project)) {
            // will be handled in invokeAction(), see below
            return true;
        }
        return isActionEnabledInternal(context);
    }

    public final void invokeAction(Lookup context) {
        if (!validateInvokeAction(context)) {
            return;
        }
        invokeActionInternal(context);
    }

    protected boolean validateInvokeAction(Lookup context) {
        if (PhpProjectValidator.isFatallyBroken(project)) {
            UiUtils.warnBrokenProject(project.getPhpModule());
            return false;
        }
        return true;
    }

    public boolean asyncCallRequired() {
        return true;
    }

    public boolean saveRequired() {
        return true;
    }

    public boolean isFileSensitive() {
        return false;
    }

    public final PhpProject getProject() {
        return project;
    }

    protected ConfigAction getConfigAction() {
        return ConfigAction.get(ConfigAction.convert(ProjectPropertiesSupport.getRunAs(project)), project);
    }

    protected boolean isTestFile(FileObject fileObj) {
        // #156939
        if (fileObj == null) {
            return false;
        }
        // #188770
        PhpModule phpModule = project.getPhpModule();
        for (PhpTestingProvider provider : project.getTestingProviders()) {
            if (provider.isTestFile(phpModule, fileObj)) {
                return true;
            }
        }
        return CommandUtils.isUnderTests(project, fileObj, false);
    }

    protected boolean isSeleniumFile(FileObject fileObj) {
        // #156939
        if (fileObj == null) {
            return false;
        }
        return CommandUtils.isUnderSelenium(project, fileObj, false);
    }
}
