/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
