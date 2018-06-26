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

import java.io.File;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable;
import org.netbeans.modules.javascript.nodejs.preferences.NodeJsPreferencesValidator;
import org.netbeans.modules.javascript.nodejs.util.RunInfo;
import org.netbeans.modules.web.common.api.ValidationResult;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;

final class DebugFileCommand extends Command {

    public DebugFileCommand(Project project) {
        super(project);
    }

    @Override
    public String getCommandId() {
        return ActionProvider.COMMAND_DEBUG_SINGLE;
    }

    @Override
    public boolean isEnabled(Lookup context) {
        return lookupJavaScriptFile(context) != null;
    }

    @Override
    ValidationResult validateRunInfo(RunInfo runInfo) {
        return new NodeJsPreferencesValidator()
                .validateDebugPort(runInfo.getDebugPort())
                .getResult();
    }

    @Override
    void runInternal(Lookup context) {
        File file = lookupJavaScriptFile(context);
        assert file != null;
        NodeExecutable node = getNode();
        if (node == null) {
            return;
        }
        RunInfo runInfo = getRunInfo();
        if (runInfo == null) {
            return;
        }
        node.debug(runInfo.getDebugPort(), file, null);
    }

}
