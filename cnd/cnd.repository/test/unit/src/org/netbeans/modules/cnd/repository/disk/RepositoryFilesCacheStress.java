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

package org.netbeans.modules.cnd.repository.disk;

import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import java.io.File;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;
import org.netbeans.modules.cnd.repository.access.RepositoryAccessTestBase;
import org.openide.util.Exceptions;


/**
 *
 */
public class RepositoryFilesCacheStress extends RepositoryAccessTestBase {

    static {
        //System.setProperty("cnd.repository.queue.ticking", "true");
	//System.setProperty("cnd.repository.file.stat", "1");
	//System.setProperty("cnd.repository.mf.stat", "true");
        //System.setProperty("caches.stress.laps", "5");
    }
    
    public RepositoryFilesCacheStress(String testName) {
        super(testName);
    }

    private boolean runOtherThread;
    private int closeCnt;
    
    public void testClosures() throws Exception {
	
	File projectRoot1 = getDataFile("quote_nosyshdr");
	File projectRoot2 = getDataFile("../org");
	
	int count = Integer.getInteger("caches.stress.laps", 1000);
	
	final TraceModelBase traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot1.getAbsolutePath(), projectRoot2.getAbsolutePath());
	//ModelImpl model = traceModel.getModel();
        
        final File tmpSrcFile = File.createTempFile("TempFile", ".cpp", getWorkDir());
        writeFile(tmpSrcFile, "int foo();");
        runOtherThread = true;
        closeCnt = 0;
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                while( runOtherThread ) {
                    try {
                        createAndCloseExtraProject(traceModel, tmpSrcFile);
                    } catch (IOException ex) {
                        registerException(ex);
                    }
                    closeCnt++;
                    sleep(100);
                }
            }
        };
        new Thread(r).start();
	
        
	for (int i = 0; i < count; i++) {
	    System.err.printf("%s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot1.getAbsolutePath(), i);
	    final CsmProject project = traceModel.getProject();
	    project.waitParse();
	    invalidateProjectFiles(project);
	    //traceModel.resetProject(i < count/2);
	    assertNoExceptions();
	}
        runOtherThread = false;
	assertNoExceptions();
        System.err.printf("\n\nDone. Main project was parsed %d times. Extra project was closed %d times\n", count, closeCnt);
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

    private void createAndCloseExtraProject(TraceModelBase traceModel, File tmpSrcFile) throws IOException {
        ProjectBase project = createExtraProject(traceModel, tmpSrcFile, "DummyProject2");
        project.waitParse();
        traceModel.getModel().closeProjectBase(project);
    }
            
}
