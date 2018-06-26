/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.groovy.support.actions;
import javax.swing.Action;
import static org.netbeans.modules.groovy.support.actions.Bundle.*;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.FileSensitiveActions;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;

/**
 * Supplies project-specific file actions (e.g. compile/run) for *.groovy files.
 *
 * @author Petr Hejl
 * @author Martin Janicek
 */
public class GroovyActionsRegistration {

    @Messages("LBL_TestProject_Action=Test")
    @ActionID(id = "org.netbeans.modules.groovy.support.GroovyProjectModule.test.project", category = "Groovy")
    @ActionRegistration(lazy = false, displayName = "#LBL_TestProject_Action")
    public static Action testProject() {
        return ProjectSensitiveActions.projectCommandAction(
                ActionProvider.COMMAND_TEST,
                LBL_TestProject_Action(),
                null);
    }

    @Messages("LBL_CompileFile_Action=Compile File")
    @ActionID(id = "org.netbeans.modules.groovy.support.GroovyProjectModule.compile", category = "Groovy")
    @ActionRegistration(lazy = false, displayName = "#LBL_CompileFile_Action")
    @ActionReference(path = "Loaders/text/x-groovy/Actions", position = 550)
    public static Action compile() {
        return FileSensitiveActions.fileCommandAction(
                ActionProvider.COMMAND_COMPILE_SINGLE,
                LBL_CompileFile_Action(),
                null);
    }

    @Messages("LBL_RunFile_Action=Run File")
    @ActionID(id = "org.netbeans.modules.groovy.support.GroovyProjectModule.run", category = "Groovy")
    @ActionRegistration(lazy = false, displayName = "#LBL_RunFile_Action")
    @ActionReferences(value = {
        @ActionReference(path = "Loaders/text/x-groovy/Actions", position = 560),
        @ActionReference(path = "Editors/text/x-groovy/Popup", position = 810, separatorBefore = 800)
    })
    public static Action run() {
        return FileSensitiveActions.fileCommandAction(
                ActionProvider.COMMAND_RUN_SINGLE,
                LBL_RunFile_Action(),
                null);
    }

    @Messages("LBL_DebugFile_Action=Debug File")
    @ActionID(id = "org.netbeans.modules.groovy.support.GroovyProjectModule.debug", category = "Groovy")
    @ActionRegistration(lazy = false, displayName = "#LBL_DebugFile_Action")
    @ActionReferences(value = {
        @ActionReference(path = "Loaders/text/x-groovy/Actions", position = 570),
        @ActionReference(path = "Editors/text/x-groovy/Popup", position = 820)
    })
    public static Action debug() {
        return FileSensitiveActions.fileCommandAction(
                ActionProvider.COMMAND_DEBUG_SINGLE,
                LBL_DebugFile_Action(),
                null);
    }

    @Messages("LBL_TestFile_Action=Test File")
    @ActionID(id = "org.netbeans.modules.groovy.support.GroovyProjectModule.test", category = "Groovy")
    @ActionRegistration(lazy = false, displayName = "#LBL_TestFile_Action")
    @ActionReferences(value = {
        @ActionReference(path = "Loaders/text/x-groovy/Actions", position = 580),
        @ActionReference(path = "Editors/text/x-groovy/Popup", position = 830)
    })
    public static Action test() {
        return FileSensitiveActions.fileCommandAction(
                ActionProvider.COMMAND_TEST_SINGLE,
                LBL_TestFile_Action(),
                null);
    }

    @Messages("LBL_DebugTestFile_Action=Debug Test File")
    @ActionID(id = "org.netbeans.modules.groovy.support.GroovyProjectModule.debug.test", category = "Groovy")
    @ActionRegistration(lazy = false, displayName = "#LBL_DebugTestFile_Action")
    @ActionReferences(value = {
        @ActionReference(path = "Loaders/text/x-groovy/Actions", position = 590),
        @ActionReference(path = "Editors/text/x-groovy/Popup", position = 840)
    })
    public static Action debugTest() {
        return FileSensitiveActions.fileCommandAction(
                ActionProvider.COMMAND_DEBUG_TEST_SINGLE,
                LBL_DebugTestFile_Action(),
                null);
    }
}
