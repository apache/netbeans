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
import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Tries to close project as soon as it is parsed -
 * while its libraries are still being parsed
 */
public class CloseProjectWhenParsingLib extends RepositoryAccessTestBase  {

    private final static boolean verbose;
    static {
        verbose = true; // Boolean.getBoolean("test.library.access.verbose");
        if( verbose ) {
            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
            System.setProperty("cnd.repository.listener.trace", "true");
            System.setProperty("cnd.trace.close.project", "true");
	    System.setProperty("cnd.repository.workaround.nulldata", "true");
        }
    }    
    
    public CloseProjectWhenParsingLib(String testName) {
	super(testName);
    }
    
    public void testRun() throws Exception {
	
	File projectRoot = getDataFile("quote_syshdr");
	
	int count = Integer.getInteger("test.close.project.when.parsing.libs.laps", 2);
	
	final TraceModelBase traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot.getAbsolutePath());
	ModelImpl model = traceModel.getModel();
	
	for (int i = 0; i < count; i++) {
	    System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot.getAbsolutePath(), i);
	    final CsmProject project = traceModel.getProject();
	    assertNoExceptions();
	    project.waitParse();
	    Collection<CsmProject> libs = project.getLibraries();
	    // here is the key point:
	    // if we wait until libs are parsed prior than resetting the project,
	    // the assertion won't appear;
	    // if we close project without persistence cleanup, 
	    // the assertion won't appear either
	    // waitLibsParsed(project);
	    traceModel.resetProject(true);
	    waitCloseAndClear(libs);
	    assertNoExceptions();
	}
	assertNoExceptions();
    }
    
    private void waitCloseAndClear(Collection<CsmProject> libs) {
        for( CsmProject lib : libs ) {
	    lib.waitParse();
	}
        for( CsmProject lib : libs ) {
	    TraceModelBase.closeProject(lib, true);
	}
    }
    
}
