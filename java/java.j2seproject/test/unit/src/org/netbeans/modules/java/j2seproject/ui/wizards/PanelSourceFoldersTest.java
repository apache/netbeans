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

package org.netbeans.modules.java.j2seproject.ui.wizards;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.api.common.project.ui.wizards.FolderList;


/**
 *
 * @author  tom
 */
public class PanelSourceFoldersTest extends NbTestCase {

    public PanelSourceFoldersTest (java.lang.String testName) {
        super(testName);
    }

    public void testCheckValidity () throws Exception {

        File root = getWorkDir();
        File projectDir = new File (root, "project");
        File test = new File (root,  "tests");
        test.mkdir();
        File src = new File (root, "src");
        src.mkdir();
        File badSrcDir = new File (root, "badSrc");
        File badSrcDir2 = new File (test, "src");
        badSrcDir2.mkdir();
        File badProjectDir = new File (root, "badPrjDir");
        badProjectDir.mkdir();
        badProjectDir.setReadOnly();
        
        assertNotNull("Empty name", PanelProjectLocationExtSrc.checkValidity ("",projectDir.getAbsolutePath(),"build.xml", true));
        assertNotNull("Read Only WorkDir", PanelProjectLocationExtSrc.checkValidity ("",badProjectDir.getAbsolutePath(),"build.xml", true));
        assertNotNull("Non Existent Sources", PanelSourceFolders.checkValidity (projectDir, new File[] {badSrcDir} , new File[] {test}));
        assertFalse("Sources == Tests",  FolderList.isValidRoot (src, new File[] {src},projectDir));
        assertFalse("Tests under Sources", FolderList.isValidRoot (new File (src, "Tests"),new File[] {src},projectDir));
        assertFalse("Sources under Tests", FolderList.isValidRoot (badSrcDir2, new File[] {test},projectDir));
        assertNull ("Valid data", PanelSourceFolders.checkValidity (projectDir, new File[]{src}, new File[]{test}));
    }
}
