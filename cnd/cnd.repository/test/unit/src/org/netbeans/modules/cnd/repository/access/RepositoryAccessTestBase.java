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

package org.netbeans.modules.cnd.repository.access;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Common ancestor for tests that just access repository in different combinations,
 * and check that no exceptions ocurred.
 * These tests use "official" repository access, 
 * so they don't need to reside in org.netbeans.modules.cnd.repository.impl
 * 
 */
public class RepositoryAccessTestBase  extends ModelImplBaseTestCase {

    public RepositoryAccessTestBase(String testName) {
	super(testName);
    }

    @Override
    protected File getTestCaseDataDir() {
	String dataPath = convertToModelImplDataDir("repository");
        String filePath = "common";
        return Manager.normalizeFile(new File(dataPath, filePath));
    }

    protected String getBriefClassName() {
	String name = getClass().getName();
	int pos = name.lastIndexOf('.');
	return (pos < 0) ? name : name.substring(pos+1);
    }
    
    protected void waitLibsParsed(CsmProject project) {
	for( CsmProject lib : project.getLibraries() ) {
	    lib.waitParse();
	}
    }
    
    protected static ProjectBase createExtraProject(TraceModelBase traceModel, File projectRoot, String name) throws IOException {
	return createExtraProject(traceModel, Collections.singletonList(projectRoot), name);
    }
    
    protected static ProjectBase createExtraProject(TraceModelBase traceModel, List<File> files, String name) throws IOException {
	NativeProject nativeProject = NativeProjectProvider.createProject(name, files, 
                Collections.<String>emptyList(), Collections.<String>emptyList(), 
		Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), 
                Collections.<String>emptyList(), Collections.<String>emptyList(), Collections.<String>emptyList(), true);
	ProjectBase result = traceModel.getModel().addProject(nativeProject, name, true); // NOI18N
	return result;
    }
    
}
