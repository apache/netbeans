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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
