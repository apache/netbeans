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
