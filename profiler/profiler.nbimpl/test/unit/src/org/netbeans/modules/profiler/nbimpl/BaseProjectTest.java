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
package org.netbeans.modules.profiler.nbimpl;

import java.io.File;
import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jaroslav Bachorik
 */
public class BaseProjectTest extends NbTestCase {
    private Project p;
    
    static {
        // for setting the default lookup to TestUtil's one
        setLookup(new Object[0]);
        
    }
    
    public static void setLookup(Object[] instances) {
        TestUtilities.setLookup(instances);
    }

    public BaseProjectTest(String name) {
        super(name);
    }
    
    public void testDummy() {
        // just dummy test to make junit happy
    }

    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtilities.setCacheFolder(getWorkDir());
        System.setProperty("netbeans.user", new File(getWorkDir(), "ud").
                getAbsolutePath()); // NOI18N
        FileObject projectPath = FileUtil.toFileObject(FileUtil.normalizeFile(
                new File(getDataDir(), "JavaApp")));
        
        p = ProjectManager.getDefault().findProject(projectPath);
        
        OpenProjects.getDefault().open(new Project[]{p}, false);
        
        IndexingManager.getDefault().refreshIndexAndWait( 
                projectPath.getFileObject("src").getURL(), null);
        IndexingManager.getDefault().refreshAllIndices("java");
        
        while(IndexingManager.getDefault().isIndexing());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        try {
            clearWorkDir();
        }
        catch( IOException e ){
            
        }
    }
    
    
    protected Project getProject() {
        return p;
    }
    
    
}
