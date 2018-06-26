/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
