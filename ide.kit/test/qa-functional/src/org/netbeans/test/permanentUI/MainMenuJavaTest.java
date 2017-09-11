/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012-2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.permanentUI;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.test.permanentUI.utils.ProjectContext;

/**
 *
 * @author Marian.Mirilovic@oracle.com
 */
public class MainMenuJavaTest extends MainMenuTestCase {

    public MainMenuJavaTest(String name) {
        super(name);
    }

    public static Test suite() {
        return MainMenuJavaTest.emptyConfiguration().
                // here you test main-menu bar
                addTest(MainMenuJavaTest.class, "testFileMenu").
                addTest(MainMenuJavaTest.class, "testRefactorMenu").
                addTest(MainMenuJavaTest.class, "testDebugMenu").
                addTest(MainMenuJavaTest.class, "testRunMenu").
                addTest(MainMenuJavaTest.class, "testToolsMenu").
                addTest(MainMenuJavaTest.class, "testProfileMenu").
                addTest(MainMenuJavaTest.class, "testView_CodeFoldsSubMenu").
                clusters(".*").enableModules(".*").
                suite();
    }

    @Override
    public void initialize() throws IOException {
        openFile("SampleProject", org.netbeans.jellytools.Bundle.getString("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir") + TREE_SEPARATOR + "sample1", "SampleClass1.java");
    }
    
    @Override
    public ProjectContext getContext() {
        return ProjectContext.JAVA;
    }
    
    @Override
    protected void tearDown() throws Exception {}
    

    public void testFileMenu() {
        oneMenuTest("File");
    }

    public void testRefactorMenu() {
        oneMenuTest("Refactor");
    }

    public void testDebugMenu() {
        oneMenuTest("Debug");
    }

    public void testRunMenu() {
        oneMenuTest("Run");
    }

    public void testToolsMenu() {
        oneMenuTest("Tools");
    }

    public void testProfileMenu() {
        oneMenuTest("Profile");
    }

    public void testView_CodeFoldsSubMenu() {
        oneSubMenuTest("View|Code Folds", true);
    }


}
