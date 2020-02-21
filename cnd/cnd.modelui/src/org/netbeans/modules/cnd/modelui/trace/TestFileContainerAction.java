/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelui.trace;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.trace.CodeModelDiagnostic;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;
        
/**
 * A test action that dump file container the given project
 * 
 */
public class TestFileContainerAction extends TestProjectActionBase {

    @Override
    public String getName() {
        return NbBundle.getMessage(getClass(), "CTL_TestFileContainerAction"); //NOI18N
    }

    
    @Override
    protected void performAction(Collection<CsmProject> csmProjects) {
        if (csmProjects != null && !csmProjects.isEmpty()) {
            testFileContainer(csmProjects);
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    private void testFileContainer(Collection<CsmProject> projects) {
        for (CsmProject p : projects) {
            testFileContainer(p);
        }
    }
    
    
    private void testFileContainer(CsmProject project) {
        InputOutput io = IOProvider.getDefault().getIO("file container for " + project.getName(), false); // NOI18N
        io.select();
        final OutputWriter out = io.getOut();
        CodeModelDiagnostic.dumpFileContainer(project, out);
        for(CsmProject lib : project.getLibraries()){
            CodeModelDiagnostic.dumpFileContainer(lib, out);
        }
        out.close();
    }
}
