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

package org.netbeans.modules.java.freeform.ui;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.ant.freeform.TestBase;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;

public class SourceFoldersPanelTest extends TestBase {
    
    public SourceFoldersPanelTest(String name) {
        super(name);
    }
    
    public void testGetDefaultLabel() throws Exception {
        char sep = File.separatorChar;
        assertEquals("foo", SourceFoldersPanel.getDefaultLabel("foo", false));
        assertEquals("foo", SourceFoldersPanel.getDefaultLabel("foo", true));
        assertEquals("foo" + sep + "bar", SourceFoldersPanel.getDefaultLabel("foo/bar", false));
        assertEquals("foo", SourceFoldersPanel.getDefaultLabel("${project.dir}/foo", false));
        assertEquals(sep + "else" + sep + "where", SourceFoldersPanel.getDefaultLabel("/else/where", false));
        // #54428:
        assertEquals("Source Packages", SourceFoldersPanel.getDefaultLabel(".", false));
        assertEquals("Test Packages", SourceFoldersPanel.getDefaultLabel(".", true));
        assertEquals("Source Packages", SourceFoldersPanel.getDefaultLabel("${project.dir}", false));
        assertEquals("Test Packages", SourceFoldersPanel.getDefaultLabel("${project.dir}", true));
    }
    
    public void testProcessRoots() throws Exception {
        //new freeform:
        File baseFolder = new File(egdir, "freeforminside/FreeForm");
        File projectFolder = new File(egdir, "freeforminside/FreeForm");
        PropertyEvaluator evaluator = new PlainPropertyEvaluator(new EditableProperties());
        ProjectModel model = null;
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should accept free location", 0, SourceFoldersPanel.processRoots(model, new File[] {new File(baseFolder, "src")}, false, true).size());
        
        //tests for #58490:
        File upperProject = new File(egdir, "freeforminside");
        
        Project upper = ProjectManager.getDefault().findProject(FileUtil.toFileObject(upperProject));
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should accept location under the newly created project", 0, SourceFoldersPanel.processRoots(model, new File[] {new File(baseFolder, "src")}, false, true).size());
        
        baseFolder = new File(egdir, "simple3");
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should accept location under the newly created project", 0, SourceFoldersPanel.processRoots(model, new File[] {new File(baseFolder, "src")}, false, true).size());
        
        baseFolder = new File(egdir, "freeforminside/FreeForm");
        projectFolder = new File(egdir, "simple3");
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should accept location under the newly created project", 0, SourceFoldersPanel.processRoots(model, new File[] {new File(baseFolder, "src")}, false, true).size());
        
        //invalid (owned by other project) sources are reported:
        baseFolder = new File(egdir, "freeforminside/FreeForm");
        projectFolder = new File(egdir, "freeforminside/FreeForm");
        
        File invalidFile1 = new File(egdir, "freeforminside/src");
        File invalidFile2 = new File(egdir, "simple3/src");
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should  reject invalid location", Collections.singleton(invalidFile1),
                SourceFoldersPanel.processRoots(model, new File[] {invalidFile1, invalidFile2}, false, true));
        
        FileOwnerQuery.markExternalOwner(FileUtil.toFileObject(invalidFile2), upper, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should reject invalid location", new HashSet<File>(Arrays.asList(invalidFile1, invalidFile2)),
                SourceFoldersPanel.processRoots(model, new File[] {invalidFile1, invalidFile2}, false, true));
        
        //test isTests option:
        File tests1 = new File(egdir, "tests1");
        File tests2 = new File(egdir, "tests2");
        File src1 = new File(egdir, "src1");
        File src2 = new File(egdir, "src2");
        
        model = ProjectModel.createEmptyModel(baseFolder, projectFolder, evaluator);
        
        assertEquals("should accept free location", 0, SourceFoldersPanel.processRoots(model, new File[] {tests1}, true, true).size());
        assertEquals("should accept free location", 0, SourceFoldersPanel.processRoots(model, new File[] {src1}, false, true).size());
        assertEquals("should reject reregistration as test", Collections.singleton(src1),
                SourceFoldersPanel.processRoots(model, new File[] {tests1, src1, tests2}, true, true));
        assertEquals("should reject reregistration as src", Collections.singleton(tests1),
                SourceFoldersPanel.processRoots(model, new File[] {tests1, src1, src2}, false, true));
    }

    protected void setUp() throws Exception {

        super.setUp();
    }

    private static class PlainPropertyEvaluator implements PropertyEvaluator {
        
        private EditableProperties properties;
        
        PlainPropertyEvaluator( EditableProperties properties ) {            
            this.properties = properties;            
        }
        
        
        public String getProperty(String prop) {            
            return properties.getProperty( prop );            
        }

        public String evaluate(String text) {
            return text;
        }

        public void removePropertyChangeListener(java.beans.PropertyChangeListener listener) {
            // NOP
        }

        public void addPropertyChangeListener(java.beans.PropertyChangeListener listener) {
            // NOP
        }

        public Map<String,String> getProperties() {
            return properties;
        }
        
    }
}
