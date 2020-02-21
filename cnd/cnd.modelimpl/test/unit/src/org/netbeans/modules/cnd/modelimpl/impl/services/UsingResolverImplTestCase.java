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
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmUsingResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 *
 */
public class UsingResolverImplTestCase extends TraceModelTestBase {
    
    public UsingResolverImplTestCase(String testName) {
        super(testName);
    }

    public void testOnlyGlobalIsVisible() throws Exception {
        performTest("fileUsing.cc", 3, 5);
    }

    public void testNSOneIsVisible() throws Exception {
        performTest("fileUsing.cc", 10, 5);
    }

    public void testNSOneAndNsTwoAreVisible() throws Exception {
        performTest("fileUsing.cc", 23, 5);
    }
    
    public void testNSOneIsVisibleNsTwoNotYetInFun() throws Exception {
        performTest("fileUsing.cc", 15, 5);
    }    
    
    public void testNSOneIsVisibleNsTwoIsUsedInFun() throws Exception {
        performTest("fileUsing.cc", 17, 5);
    }    

    public void testNSOneAndNsTwoAreVisibleInMain() throws Exception {
        performTest("main.cc", 5, 5);
    }      

    public void testUnnamedIsVisble() throws Exception {
        performTest("unnamedNs.cc", 12, 5);
    }      
    
    public void testOuterIsVisble() throws Exception {
        performTest("main.cc", 10, 10);
    }      
    
    ////////////////////////////////////////////////////////////////////////////
    // general staff
    
    @Override
    protected void postSetUp() throws Exception {
        super.postSetUp();
        log("postSetUp preparing project.");
        initParsedProject();
        log("postSetUp finished preparing project.");
        log("Test "+getName()+  "started");         
    }    
    
    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object ... params) throws Exception {
        assert args.length == 1;
        String path = args[0];
        FileImpl fileImpl = getFileImpl(new File(path));
        assertNotNull("csm file not found for " + path, fileImpl);
        int line = (Integer) params[0];
        int column = (Integer) params[1];
        boolean onlyInProject = (Boolean) params[2];
        int offset = fileImpl.getOffset(line, column);
        CsmUsingResolver impl = new UsingResolverImpl();
        CsmProject inPrj = onlyInProject ? fileImpl.getProject() : null;
        Collection<CsmNamespace> visNSs = impl.findVisibleNamespaces(fileImpl, offset, inPrj);
        for (CsmNamespace nsp : visNSs) {
            streamOut.println("NAMESPACE " + nsp.getName() + " (" + nsp.getQualifiedName() + ") ");
        }
        
        Collection<CsmDeclaration> visDecls = impl.findUsedDeclarations(fileImpl, offset, inPrj);
        for (CsmDeclaration decl : visDecls) {
            streamErr.println("DECLARATION " + decl);
        }
    }
    
    private void performTest(String source, int line, int column) throws Exception {
        boolean onlyInProject = false;
        super.performTest(source, getName() + ".nsp", getName() + ".decl", // NOI18N
                            line, column, onlyInProject);
    }    

}
