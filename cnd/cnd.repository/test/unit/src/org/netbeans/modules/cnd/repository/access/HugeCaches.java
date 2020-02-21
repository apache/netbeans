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
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * A test that reproduces the situation described in the IZ #124767
 * http://www.netbeans.org/issues/show_bug.cgi?id=124767
 */
public class HugeCaches extends RepositoryAccessTestBase {

    static {
	System.setProperty("cnd.repository.trace.defragm", "true");
	System.setProperty("cnd.repository.queue.maintenance", "10");
    }
    
    public HugeCaches(String testName) {
	super(testName);
    }
    
    public void testRun() throws Exception {
	
	File projectRoot1 = getDataFile("quote_nosyshdr");
	File projectRoot2 = getDataFile("../org");
	
	int count = Integer.getInteger("huge.caches.laps", 1000);
	
	final TraceModelBase traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot1.getAbsolutePath(), projectRoot2.getAbsolutePath());
	ModelImpl model = traceModel.getModel();
	
	for (int i = 0; i < count; i++) {
	    System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot1.getAbsolutePath(), i);
	    final CsmProject project = traceModel.getProject();
	    project.waitParse();
	    sleep(2000); // (i < 2 ? 2000 : 4000); // 12000);
	    if( i > 0 &&  i % 20 == 0 ) {
		System.err.printf("\n\nSleeping...\n");
		sleep(15000);
		System.err.printf("\nAwoke\n\n");
	    }
	    
	    invalidateProjectFiles(project);
	    //traceModel.resetProject(i < count/2);
	    assertNoExceptions();
	}
	assertNoExceptions();
    }
    
    private void invalidateProjectFiles(CsmProject project) {
	for(CsmFile file : project.getAllFiles() ) {
	    FileImpl impl = (FileImpl) file;
	    impl.markReparseNeeded(false);
	    try {
		file.scheduleParsing(false);
		//sleep(500);
	    } catch ( InterruptedException e ) {}
	}
    }
    
}
