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

package org.netbeans.modules.apisupport.project.ui.wizard.common;

import javax.swing.KeyStroke;
import org.netbeans.junit.NbTestCase;

/**
 * @author Martin Krauskopf
 */
public class WizardUtilsTest extends NbTestCase {
    
    public WizardUtilsTest(String testName) {
        super(testName);
    }
    
    /* XXX rewrite to use mock data
    public void testCreateLayerPresenterComboModel() throws Exception {
        Project project = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        Map<String,Object> excludes = new HashMap<String,Object>();
        excludes.put("template", true);
        excludes.put("simple", false);
        String sfsRoot = "Templates";
        ComboBoxModel allModel = UIUtil.createLayerPresenterComboModel(project, sfsRoot);
        ComboBoxModel excludedModel = UIUtil.createLayerPresenterComboModel(project, sfsRoot, excludes);
        assertTrue("UIUtil.createLayerPresenterComboModel() doesn't work.", allModel.getSize() >= excludedModel.getSize());
    }
     */
    
    public void testKeyToLogicalString() throws Exception {
        assertKeyLogicalString("X", "pressed X");
        assertKeyLogicalString("D-X", "ctrl pressed X");
        assertKeyLogicalString("DO-X", "ctrl alt pressed X");
        assertKeyLogicalString("DS-X", "shift ctrl pressed X");
        assertKeyLogicalString("OS-X", "shift alt pressed X");
        assertKeyLogicalString("DOS-X", "shift ctrl alt pressed X");
        assertKeyLogicalString("ENTER", "pressed ENTER");
    }
    
    private void assertKeyLogicalString(String expected, String swingKeyStroke) {
        assertEquals(swingKeyStroke + " corresponding to " + expected, expected, WizardUtils.keyToLogicalString(KeyStroke.getKeyStroke(swingKeyStroke)));
    }

    /* XXX rewrite to not rely on nb.org:
    public void testLayerItemPresenterCompareTo() throws Exception {
        TestBase.initializeBuildProperties(getWorkDir(), getDataDir());
        NbModuleProject project = TestBase.generateStandaloneModule(getWorkDir(), "module");
        FileSystem fs = LayerUtils.getEffectiveSystemFilesystem(project);
        FileObject root = fs.getRoot().getFileObject("Templates/Project/APISupport");
        FileObject module = root.getFileObject("emptyModule");
        FileObject suite = root.getFileObject("emptySuite");
        FileObject library = root.getFileObject("libraryModule");
        LayerItemPresenter moduleLIP = new LayerItemPresenter(module, root);
        LayerItemPresenter moduleLIP1 = new LayerItemPresenter(module, root);
        LayerItemPresenter suiteLIP = new LayerItemPresenter(suite, root);
        LayerItemPresenter libraryLIP = new LayerItemPresenter(library, root);
        assertTrue("'Module Project' < 'Module Suite Project'", moduleLIP.compareTo(suiteLIP) < 0);
        assertTrue("'Module Project' == 'Module Project'", moduleLIP.compareTo(moduleLIP1) == 0);
        assertTrue("'Library Wrapper Module Project < 'Module Project'", libraryLIP.compareTo(moduleLIP) < 0);
        assertTrue("'Library Wrapper Module Project < 'Module Suite Project'", libraryLIP.compareTo(suiteLIP) < 0);
    }
     */
    
    public void testIsValidSFSFolderName() throws Exception {
        assertTrue(WizardUtils.isValidSFSPath("a"));
        assertTrue(WizardUtils.isValidSFSPath("a/b/c"));
        assertTrue(WizardUtils.isValidSFSPath("a/b/c_c/"));
        assertTrue(WizardUtils.isValidSFSPath("/a/b/c_c"));
        assertTrue(WizardUtils.isValidSFSPath("a/1a/b/c/1d_d/"));
        assertTrue(WizardUtils.isValidSFSPath("_a/b/c_"));
        assertFalse(WizardUtils.isValidSFSPath("a/b/c/dd+"));
        assertFalse(WizardUtils.isValidSFSPath("a+b"));
        assertFalse(WizardUtils.isValidSFSPath(""));
        assertFalse(WizardUtils.isValidSFSPath(" "));
    }

}
