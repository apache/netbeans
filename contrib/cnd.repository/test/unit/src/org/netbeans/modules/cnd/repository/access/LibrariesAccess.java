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
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Tests accessing libraries
 */
public class LibrariesAccess extends RepositoryAccessTestBase {
    
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
    
    public LibrariesAccess(String testName) {
	super(testName);
    }
    
    public void testRun() throws Exception {
	
	File projectRoot = getDataFile("quote_syshdr");
	
	int count = Integer.getInteger("test.library.access.laps", 1000);
	
	final TraceModelBase traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot.getAbsolutePath());
	ModelImpl model = traceModel.getModel();
	
	for (int i = 0; i < count; i++) {
	    System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot.getAbsolutePath(), i);
	    final CsmProject project = traceModel.getProject();
	    int cnt = 0;
	    while( ! project.isStable(null) ) {
		accessLibraries(project);
		assertNoExceptions();
		cnt++;
	    }
	    assertNoExceptions();
	    System.err.printf("\twhile parsing, libraries were accessed %d times\n", cnt);
	    project.waitParse();
	    cnt = 1000;
	    for (int j = 0; j < cnt; j++) {
		accessLibraries(project);
		assertNoExceptions();
	    }
	    System.err.printf("\tafter parsing, libraries were accessed %d times\n", cnt);
	    waitLibsParsed(project);
	    traceModel.resetProject(i < count/2);
	    assertNoExceptions();
	}
	assertNoExceptions();
    }
    
    
    private void accessLibraries(CsmProject project) throws Exception {
        for( CsmProject lib : project.getLibraries() ) {
	    assertNotNull(lib);
        }
    }
        
}
