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
