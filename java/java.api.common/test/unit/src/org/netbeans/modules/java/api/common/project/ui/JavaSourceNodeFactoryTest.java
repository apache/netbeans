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
package org.netbeans.modules.java.api.common.project.ui;

import java.io.File;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.ui.support.NodeList;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tomas Zezula
 */
public class JavaSourceNodeFactoryTest extends NbTestCase {
    
    private Project prj;
    
    public JavaSourceNodeFactoryTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        final FileObject projectDir = FileUtil.createFolder(
                new File(getWorkDir(),"project"));  //NOI18N
        prj = new MockProject(projectDir);
    }    

    public void testListeners() {
        final JavaSourceNodeFactory f = new JavaSourceNodeFactory();
        final NodeList<?> l = f.createNodes(prj);
        final ChangeListener l1 = new CL();
        final ChangeListener l2 = new CL();
        
        //Paired add and remove
        l.addChangeListener(l1);
        l.removeChangeListener(l1);
        
        
        //Paired add and remove of 2 listeners
        l.addChangeListener(l1);
        l.addChangeListener(l2);
        l.removeChangeListener(l1);
        l.removeChangeListener(l2);
        
        //Unpaired remove
        l.removeChangeListener(l1);
        
    }
    
    
    private static final class MockProject implements Project {
        
        private final FileObject projectDir;
        
        MockProject(final FileObject projectDir) {
            assert projectDir != null;
            this.projectDir = projectDir;
        }
        

        @Override
        public FileObject getProjectDirectory() {
            return projectDir;
        }

        @Override
        public Lookup getLookup() {
            return Lookups.fixed(this);
        }        
    }
    
    private static class CL implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
        }
    }
}
