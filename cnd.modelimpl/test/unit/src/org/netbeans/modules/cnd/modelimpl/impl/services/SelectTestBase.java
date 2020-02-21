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
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import org.netbeans.junit.Manager;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.indexing.impl.TextIndexStorageManager;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ModelImplTest;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Common base class for CsmSelect unit tests
 */
public abstract class SelectTestBase extends ModelImplBaseTestCase {
    private static final boolean TRACE = false;
    private TraceModelBase traceModel;

    public SelectTestBase(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        super.clearWorkDir();
        File projectRoot = Manager.normalizeFile(getProjectRoot());
        assertTrue(projectRoot.exists());
	traceModel = new  TraceModelBase(true);
	traceModel.setUseSysPredefined(false);
	traceModel.processArguments(projectRoot.getAbsolutePath());
    }

    protected abstract File getProjectRoot();

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        traceModel.shutdown(true);
        TextIndexStorageManager.shutdown();
    }

    @Override
    protected File getTestCaseDataDir() {
        return getDataDir();
    }


    public void doTestGetMethods() throws Exception {
        CsmCacheManager.enter();
        try {
            CsmProject project = traceModel.getProject();
            project.waitParse();
            _testGetMethods(project.getGlobalNamespace());
            for (CsmProject lib : project.getLibraries()) {
                _testGetMethods(lib.getGlobalNamespace());
            }
            assertNoExceptions();
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    public void doTestGetFunctions() throws Exception {
        CsmCacheManager.enter();
        try {
            CsmProject project = traceModel.getProject();
            project.waitParse();
            _testGetFunctions(project.getGlobalNamespace());
            for (CsmProject lib : project.getLibraries()) {
                _testGetFunctions(lib.getGlobalNamespace());
            }
            assertNoExceptions();
        } finally {
            CsmCacheManager.leave();
        }
    }    
    
    public void doTestGetFields() throws Exception {
        CsmCacheManager.enter();
        try {
            CsmProject project = traceModel.getProject();
            project.waitParse();
            _testGetFields(project.getGlobalNamespace());
            for (CsmProject lib : project.getLibraries()) {
                _testGetFields(lib.getGlobalNamespace());
            }
            assertNoExceptions();
        } finally {
            CsmCacheManager.leave();
        }
    }    
    
    public void doTestGetVariables() throws Exception {
        CsmCacheManager.enter();
        try {
            CsmProject project = traceModel.getProject();
            project.waitParse();
            _testGetVariables(project.getGlobalNamespace());
            for (CsmProject lib : project.getLibraries()) {
                _testGetVariables(lib.getGlobalNamespace());
            }
            assertNoExceptions();
        } finally {
            CsmCacheManager.leave();
        }
    }    
    
    protected void _testGetMethods(CsmNamespace nsp) throws Exception {
        CsmProject project = nsp.getProject();
        boolean dumpProjectContainer = true;
        for (CsmDeclaration decl : nsp.getDeclarations()) {
            if (CsmKindUtilities.isClass(decl)) {

            }
        }
        for (CsmNamespace nested : nsp.getNestedNamespaces()) {
            _testGetMethods(nested);
        }
    }    
    
    protected void _testGetMethods(CsmProject project, CsmClass cls) throws Exception {
        boolean dumpProjectContainer = true;
        for (CsmMember member : cls.getMembers()) {
            if (CsmKindUtilities.isClass(member)) {
                _testGetMethods(project, (CsmClass) member);
            } else if (CsmKindUtilities.isMethod(member)) {
                CsmFunction func = (CsmFunction) member;
                Iterator<CsmFunction> iter = _getFunctions(project, func);
                final CsmFile containingFile = func.getContainingFile();
                boolean ok = _checkFound(func, iter);
                if (!ok) {
                    System.err.println("ERROR FOR: " + func + "\n\tUIN=" + func.getUniqueName() + "\n\tFQN="+func.getQualifiedName() + "\n\tCLS="+cls);
                    // more trace
                    if (dumpProjectContainer && project instanceof ProjectBase) {
                        dumpProjectContainer = false;
                        ModelImplTest.dumpProjectContainers(System.err, (ProjectBase) project, false);
                    }
                    if (containingFile instanceof FileImpl) {
                        ((FileImpl)containingFile).dumpPPStates(new PrintWriter(System.err));
                    }
                }
                assertTrue("Method " + func.getQualifiedName().toString() + 
                        " from " + containingFile.getAbsolutePath() + ":" + func.getStartPosition() + 
                        " not found in project " + project.getName(), ok);
            }
        }        
    }

    protected void _testGetFunctions(CsmNamespace nsp) throws Exception {
        CsmProject project = nsp.getProject();
        boolean dumpProjectContainer = true;
        for (CsmDeclaration decl : nsp.getDeclarations()) {
            if (CsmKindUtilities.isFunction(decl)) {
                CsmFunction func = (CsmFunction) decl;
                Iterator<CsmFunction> iter = _getFunctions(project, func);
                final CsmFile containingFile = func.getContainingFile();
                boolean ok = _checkFound(func, iter);
                if (!ok) {
                    System.err.println("ERROR FOR: " + decl + "\n\tUIN=" + decl.getUniqueName() + "\n\tFQN="+decl.getQualifiedName() + "\n\tNS="+nsp);
                    // more trace
                    if (dumpProjectContainer && project instanceof ProjectBase) {
                        dumpProjectContainer = false;
                        ModelImplTest.dumpProjectContainers(System.err, (ProjectBase) project, false);
                    }
                    if (containingFile instanceof FileImpl) {
                        ((FileImpl)containingFile).dumpPPStates(new PrintWriter(System.err));
                    }
                }
                assertTrue("Function " + decl.getQualifiedName().toString() + 
                        " from " + containingFile.getAbsolutePath() + ":" + func.getStartPosition() + 
                        " not found in project " + project.getName(), ok);
            }
        }
        for (CsmNamespace nested : nsp.getNestedNamespaces()) {
            _testGetFunctions(nested);
        }
    }
    
    protected void _testGetFields(CsmNamespace nsp) throws Exception {
        for (CsmDeclaration decl : nsp.getDeclarations()) {
            if (CsmKindUtilities.isClass(decl)) {
                _testGetFields(nsp.getProject(), (CsmClass) decl);
            }
        }
        for (CsmNamespace nested : nsp.getNestedNamespaces()) {
            _testGetFields(nested);
        }
    }    
    
    protected void _testGetFields(CsmProject project, CsmClass cls) throws Exception {
        boolean dumpProjectContainer = true;        
        for (CsmMember member : cls.getMembers()) {
            if (CsmKindUtilities.isClass(member)) {
                _testGetFields(project, (CsmClass) member);
            } else if (CsmKindUtilities.isField(member)) {
                CsmField field = (CsmField) member;
                Iterator<CsmVariable> iter = _getVariables(project, field);
                final CsmFile containingFile = field.getContainingFile();
                boolean ok = _checkFound(field, iter);
                if (!ok) {
                    System.err.println("ERROR FOR: " + field + "\n\tUIN=" + field.getUniqueName() + "\n\tFQN="+field.getQualifiedName() + "\n\tCLS="+cls);
                    // more trace
                    if (dumpProjectContainer && project instanceof ProjectBase) {
                        dumpProjectContainer = false;
                        ModelImplTest.dumpProjectContainers(System.err, (ProjectBase) project, false);
                    }
                    if (containingFile instanceof FileImpl) {
                        ((FileImpl)containingFile).dumpPPStates(new PrintWriter(System.err));
                    }
                }
                assertTrue("Field " + field.getQualifiedName().toString() + 
                        " from " + containingFile.getAbsolutePath() + ":" + field.getStartPosition() + 
                        " not found in project " + project.getName(), ok);
            }
        }        
    }
    
    protected void _testGetVariables(CsmNamespace nsp) throws Exception {
        CsmProject project = nsp.getProject();
        boolean dumpProjectContainer = true;
        for (CsmDeclaration decl : nsp.getDeclarations()) {
            if (CsmKindUtilities.isVariable(decl)) {
                CsmVariable var = (CsmVariable) decl;
                Iterator<CsmVariable> iter = _getVariables(project, var);
                final CsmFile containingFile = var.getContainingFile();
                boolean ok = _checkFound(var, iter);
                if (!ok) {
                    System.err.println("ERROR FOR: " + decl + "\n\tUIN=" + decl.getUniqueName() + "\n\tFQN="+decl.getQualifiedName() + "\n\tNS="+nsp);
                    // more trace
                    if (dumpProjectContainer && project instanceof ProjectBase) {
                        dumpProjectContainer = false;
                        ModelImplTest.dumpProjectContainers(System.err, (ProjectBase) project, false);
                    }
                    if (containingFile instanceof FileImpl) {
                        ((FileImpl)containingFile).dumpPPStates(new PrintWriter(System.err));
                    }
                }
                assertTrue("Variable " + decl.getQualifiedName().toString() + 
                        " from " + containingFile.getAbsolutePath() + ":" + var.getStartPosition() + 
                        " not found in project " + project.getName(), ok);
            }
        }
        for (CsmNamespace nested : nsp.getNestedNamespaces()) {
            _testGetVariables(nested);
        }
    }    
    
    protected Iterator<CsmFunction> _getFunctions(CsmProject project, CsmFunction func) {
        CharSequence qName = func.getQualifiedName();
        if (TRACE) { 
            System.err.printf("Searching for function %s\n", func); 
        }
        return CsmSelect.getFunctions(project, qName);
    }
    
    protected Iterator<CsmVariable> _getVariables(CsmProject project, CsmVariable var) {
        return Collections.<CsmVariable>emptyList().iterator();
    }    
    
    protected boolean _checkFound(CsmObject obj, Iterator<? extends CsmObject> answer) {
        return answer.hasNext();
    }
    
    protected final CsmProject getProject() {
        return traceModel.getProject();
    }
}
