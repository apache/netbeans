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

package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.io.File;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Set;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmTracer;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceXRef;
import org.netbeans.modules.cnd.support.Interrupter;

/**
 *
 */
public class ReferenceRepositoryImplTestCase extends TraceModelTestBase {
    
    public ReferenceRepositoryImplTestCase(String testName) {
        super(testName);
    }
    
    public void testCpuClassRefs() throws Exception {
        performTest("cpu.h", 47, 9);
    }
    ////////////////////////////////////////////////////////////////////////////
    // general staff
    
    @Override 
    protected File getTestCaseDataDir() {
        File dataDir = super.getDataDir();
        String filePath = "common/quote_nosyshdr";
        return Manager.normalizeFile(new File(dataDir, filePath));
    }
    
    @Override
    protected void postSetUp() throws Exception {
        super.postSetUp();
        log("postSetUp preparing project.");
        initParsedProject();
        log("postSetUp finished preparing project.");
        log("Test "+getName()+  "started");         
    }    
    
    protected void doTest(File testFile, PrintStream streamOut, PrintStream streamErr, Object ... params) throws Exception {
        FileImpl fileImpl = getFileImpl(testFile);
        assertNotNull("csm file not found for " + testFile.getAbsolutePath(), fileImpl);
        int line = (Integer) params[0];
        int column = (Integer) params[1];
        boolean inProject = (Boolean)params[2];
        @SuppressWarnings("unchecked")
        Set<CsmReferenceKind> kinds = (Set<CsmReferenceKind>) params[3];
        int offset = fileImpl.getOffset(line, column);
        CsmReference tgtRef = CsmReferenceResolver.getDefault().findReference(fileImpl, null, offset);
        assertNotNull("reference is not found for " + testFile.getAbsolutePath() + "; line="+line+";column="+column, tgtRef);
        CsmObject target = tgtRef.getReferencedObject();
        assertNotNull("referenced object is not found for " + testFile.getAbsolutePath() + "; line="+line+";column="+column, target);
        streamOut.println("TARGET OBJECT IS\n  " + CsmTracer.toString(target));
        ReferenceRepositoryImpl xRefRepository = new ReferenceRepositoryImpl();
        Collection<CsmReference> out;
        if (inProject) {
            out = xRefRepository.getReferences(target, fileImpl.getProject(), kinds, Interrupter.DUMMY);
        } else {
            out = xRefRepository.getReferences(target, fileImpl, kinds, Interrupter.DUMMY);
        }
        TraceXRef.traceRefs(out, target, streamOut);
    }
    
    private void performTest(String source, int line, int column) throws Exception {
        Set<CsmReferenceKind> kinds = CsmReferenceKind.ALL;
        boolean inProject = true;
        super.performTest(source, getName() + ".res", null, // NOI18N
                            line, column, inProject, kinds);
    }        
}
