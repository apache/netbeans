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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.PrintStream;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider.NativeProjectImpl;
import org.openide.filesystems.FileObject;

/**
 * 
 */
public class ModelImplTest extends ModelImplBaseTestCase {

    public ModelImplTest(String testName) {
        super(testName);
    }
    
    public void testModelProvider() {
        CsmModel csmModel = CsmModelAccessor.getModel();
        assertNotNull("Null model", csmModel);
        assertTrue("Unknown model provider " + csmModel.getClass().getName(), csmModel instanceof ModelImpl);
    }
    
    public static void dumpProjectContainers(PrintStream printStream, ProjectBase project, boolean dumpFiles) {
        ProjectBase.dumpProjectContainers(printStream, project, dumpFiles);
    }

    public static void fireFileAdded(final CsmProject project, FileObject sourceFileObject) {
        assertNotNull(project);
        Object platform = project.getPlatformProject();
        if (platform instanceof NativeProjectProvider.NativeProjectImpl) {
            NativeProjectProvider.NativeProjectImpl nativeProject = (NativeProjectImpl) platform;
            nativeProject.fireFileAdded(sourceFileObject);
        }
        if (project instanceof ProjectBase) {
            ((ProjectBase)project).onFileObjectExternalCreate(sourceFileObject);
        }
    }

    public static void fireFileChanged(final CsmProject project, FileObject sourceFileObject) {
        assertNotNull(project);
        Object platform = project.getPlatformProject();
        if (platform instanceof NativeProjectProvider.NativeProjectImpl) {
            NativeProjectProvider.NativeProjectImpl nativeProject = (NativeProjectImpl) platform;
            nativeProject.fireFileChanged(sourceFileObject);
        } else {
            assertTrue("can not send fireFileChanged using project " + platform, false);
        }
    }

    public static void fireFileChanged(CsmFile file) {
        FileObject fileObject = file.getFileObject();
        assertNotNull("no file object for " + file, fileObject);
        CsmProject project = file.getProject();
        fireFileChanged(project, fileObject);
    }
}
