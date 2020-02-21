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
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImpl;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 *
 */
public class ChangingPropertiesTestCase extends RepositoryAccessTestBase {

    private final static boolean verbose;
    static {
        verbose = true; // Boolean.getBoolean("test.library.changing.props.verbose");
        if( verbose ) {
            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
            System.setProperty("cnd.repository.listener.trace", "true");
            System.setProperty("cnd.trace.close.project", "true");
	    //System.setProperty("cnd.repository.workaround.nulldata", "true");
        }
    }    

    public ChangingPropertiesTestCase(String testName) {
	super(testName);
    }
    
    public void testRun() throws Exception {
	
	File projectRoot = getDataFile("quote_syshdr");
	
	int count = Integer.getInteger("test.library.changing.props.laps", 1000);
	
	final TraceModelBase traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(true);
	traceModel.processArguments(projectRoot.getAbsolutePath());
	ModelImpl model = traceModel.getModel();
	ModelSupport.instance().setModel(model);
	final CsmProject project = traceModel.getProject();
	
	System.err.printf("Waiting parse...\n");
	project.waitParse();
	final NativeProject nativeProject = (NativeProject) project.getPlatformProject();
	
	// a simple timing
	System.err.printf("Calculating parse time\n");
	long parseTime = System.currentTimeMillis();
	NativeProjectProvider.fireAllFilesChanged(nativeProject);
	sleep(500); // otherwise
	project.waitParse();
	parseTime = System.currentTimeMillis() - parseTime;
	System.err.printf("Parse time is %d ms\n", parseTime);
	
	for (int i = 0; i < count; i++) {
	    System.err.printf("########## %s: processing project %s. Pass %d \n", getBriefClassName(), projectRoot.getAbsolutePath(), i);
	    NativeProjectProvider.fireAllFilesChanged(nativeProject);
	    long timeout = (long) (Math.random() * parseTime);
	    System.err.printf("Sleeping %d ms\n", timeout);
	    sleep(timeout);
	    assertNoExceptions();
	}
	assertNoExceptions();
    }
}
