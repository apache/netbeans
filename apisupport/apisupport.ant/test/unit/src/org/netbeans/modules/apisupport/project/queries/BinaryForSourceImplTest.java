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
package org.netbeans.modules.apisupport.project.queries;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class BinaryForSourceImplTest extends TestBase {

    public BinaryForSourceImplTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }    
    
    public void testFindSourceRootForCompiledClasses() throws Exception {
        final NbModuleProject prj = generateStandaloneModule(getWorkDir(),"testproject");   //NOI18N
        
        doTestFindBinaryRootForSources(prj.getProjectDirectoryFile(), "src", "build/classes");   //NOI18N
        doTestFindBinaryRootForSources(prj.getProjectDirectoryFile(), "build/classes-generated/", "build/classes");  //NOI18N
        doTestFindBinaryRootForSources(prj.getProjectDirectoryFile(), "test/unit/src", "build/test/unit/classes");   //NOI18N
        doTestFindBinaryRootForSources(prj.getProjectDirectoryFile(), "build/test/unit/classes-generated/", "build/test/unit/classes");  //NOI18N
        
    }

    private void doTestFindBinaryRootForSources(
            @NonNull final File projectFolder,
            @NonNull final String srcPath,
            @NonNull final String classesPath) throws Exception {
        final File classesF = file(projectFolder, classesPath);
        final File srcF = file(projectFolder, srcPath);
        assertEquals("right binary root for " + srcPath,    //NOI18N
            Collections.singletonList(Utilities.toURI(classesF).toURL()),
            Arrays.asList(BinaryForSourceQuery.findBinaryRoots(Utilities.toURI(srcF).toURL()).getRoots()));
    }
}
