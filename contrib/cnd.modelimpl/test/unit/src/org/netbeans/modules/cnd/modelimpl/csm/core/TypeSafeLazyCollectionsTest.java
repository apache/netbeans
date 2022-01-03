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

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.NativeProjectProvider;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * 
 */
public class TypeSafeLazyCollectionsTest extends ModelImplBaseTestCase {

    public TypeSafeLazyCollectionsTest(String testName) {
        super(testName);
    }

    public void testSimple() throws Exception {

        File workDir = getWorkDir();
        File sourceFile = new File(workDir, "test1.cc");

        // 
        // create a file that uses macros;
        // parse this file with these macros set to class start/end
        //
        writeFile(sourceFile, "START\nvoid foo();\nEND\n");
        
	final TraceModelBase traceModel = new  TraceModelBase(true);
	
        String className = "MyClass";
	traceModel.processArguments(sourceFile.getAbsolutePath(), "-DSTART=class " + className + "{", "-DEND=};");
	ModelImpl model = traceModel.getModel();
	//ModelSupport.instance().setModel(model);
	final CsmProject project = traceModel.getProject();

        project.waitParse();
        
        //new CsmTracer(System.err).dumpModel(project);
                
        CsmClass cls = (CsmClass) findDeclaration(className, project);
        assertNotNull(className + " can not be found", cls);
        
        //
        // Remember the members collection
        //
        Collection<CsmMember> members = cls.getMembers();
        
        
        //
        // Parse it once more with macros unset
        //
        NativeProject nativeProject = (NativeProject)project.getPlatformProject();
        List<String> macros = new ArrayList<>();
        macros.add("START=");
        macros.add("END=");
        NativeProjectProvider.setUserMacros(nativeProject, macros /*Collections.<String>emptyList()*/);
        ((FileImpl) cls.getContainingFile()).markReparseNeeded(true);
        cls.getContainingFile().scheduleParsing(true);
        
        project.waitParse();
        
        //new CsmTracer(System.err).dumpModel(project);
        
        //
        // Make sure no class cast exception happens
        //
        CharSequence tmp;
        try {
            for( CsmMember member : members ) {
                tmp = member.getName();
            }
        } catch( Exception e) {
            DiagnosticExceptoins.register(e);
        }
        assertNoExceptions();
    }
   
}
