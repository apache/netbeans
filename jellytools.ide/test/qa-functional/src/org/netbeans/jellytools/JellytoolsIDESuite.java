/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
