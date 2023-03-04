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

package org.netbeans.modules.project.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JFileChooser;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.project.ui.actions.TestSupport.ChangeableLookup;
import org.netbeans.spi.project.SubprojectProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Jan Lahoda
 */
public class ProjectChooserAccessoryTest extends NbTestCase {
    
    public ProjectChooserAccessoryTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    /**The cycles in project dependencies should be handled gracefully:
     */
    public void testAddSubprojects() throws Exception {
        ChangeableLookup l1 = new ChangeableLookup();
        ChangeableLookup l2 = new ChangeableLookup();
        Project p1 = new TestProject(l1);
        Project p2 = new TestProject(l2);
        
        Set<Project> subprojects1 = new HashSet<Project>();
        Set<Project> subprojects2 = new HashSet<Project>();
        
        subprojects1.add(p2);
        subprojects2.add(p1);
        
        l1.change(new SubprojectProviderImpl(subprojects1));
        l2.change(new SubprojectProviderImpl(subprojects2));
        
        List<Project> result = new ArrayList<Project>();
        //#101227
        ProjectChooserAccessory acc = new ProjectChooserAccessory(new JFileChooser(), false, true);
        acc.modelUpdater.addSubprojects(p1, result, new HashMap<Project,Set<? extends Project>>());
        
        assertTrue(new HashSet<Project>(Arrays.asList(p1, p2)).equals(new HashSet<Project>(result)));
    }
    
    private final class TestProject implements Project {
        
        private Lookup l;
        
        public TestProject(Lookup l) {
            this.l = l;
        }
        
        public FileObject getProjectDirectory() {
            throw new UnsupportedOperationException("Should not be called in this test.");
        }
        
        public Lookup getLookup() {
            return l;
        }
    }
    
    private static final class SubprojectProviderImpl implements SubprojectProvider {
        
        private Set<Project> subprojects;
        
        public SubprojectProviderImpl(Set<Project> subprojects) {
            this.subprojects = subprojects;
        }
        
        public Set<? extends Project> getSubprojects() {
            return subprojects;
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
        
    }
}
