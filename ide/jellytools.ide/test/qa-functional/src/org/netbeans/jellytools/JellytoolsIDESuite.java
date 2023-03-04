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
package org.netbeans.jellytools;

import junit.framework.Test;
import org.netbeans.jellytools.actions.FindInFilesActionTest;
import org.netbeans.jellytools.modules.db.actions.DbActionsTest;
import org.netbeans.jellytools.modules.db.nodes.DatabasesNodeTest;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperatorTest;
import org.netbeans.jellytools.modules.debugger.actions.BreakpointsWindowActionTest;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsActionTest;
import org.netbeans.jellytools.modules.editor.CompletionJListOperatorTest;

/**
 * Run all tests in the same instance of the IDE.
 *
 * @author Jiri Skrivanek
 */
public class JellytoolsIDESuite {

    public static Test suite() {
        return JellyTestCase.emptyConfiguration().
                addTest(DocumentsDialogOperatorTest.class, DocumentsDialogOperatorTest.tests).
                addTest(EditorOperatorTest.class, EditorOperatorTest.tests).
                addTest(EditorWindowOperatorTest.class, EditorWindowOperatorTest.tests).
                addTest(FilesTabOperatorTest.class, FilesTabOperatorTest.tests).
                addTest(FindInFilesOperatorTest.class).
                addTest(NavigatorOperatorTest.class).
                addTest(ProjectsTabOperatorTest.class, ProjectsTabOperatorTest.tests).
                addTest(RuntimeTabOperatorTest.class, RuntimeTabOperatorTest.tests).
                addTest(SearchResultsOperatorTest.class, SearchResultsOperatorTest.tests).
                addTest(FindInFilesActionTest.class, FindInFilesActionTest.tests).
                addTest(DbActionsTest.class).
                addTest(DatabasesNodeTest.class).
                addTest(BreakpointsWindowOperatorTest.class).
                addTest(BreakpointsWindowActionTest.class).
                addTest(DeleteAllBreakpointsActionTest.class).
                addTest(CompletionJListOperatorTest.class, CompletionJListOperatorTest.tests).
                suite();
    }
}
