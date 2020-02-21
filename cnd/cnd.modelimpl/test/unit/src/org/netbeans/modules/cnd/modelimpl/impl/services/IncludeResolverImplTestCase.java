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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.completion.impl.xref.ReferenceResolverImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;

/**
 * Tests for IncludeResolver
 *
 */
public class IncludeResolverImplTestCase extends TraceModelTestBase {
    
    public IncludeResolverImplTestCase(String testName) {
        super(testName);
    }
    
    public void testIncludeResolverInMainCC() throws Exception {
        performTest("main.cc", 1, 1); // NOI18N
    }

    public void testIncludeResolverInLocalCC() throws Exception {
        performTest("local.cc", 1, 1); // NOI18N
    }

    public void testIncludeResolverInHeader2H() throws Exception {
        performTest("header2.h", 1, 1); // NOI18N
    }

    public void testIncludeResolverInMainCCOnLocalVar() throws Exception {
        performTest("main.cc", 1, 1, "local.cc", 2, 8); // NOI18N
    }
    
    public void testIncludeResolverInHeader1HOnLocalVar() throws Exception {
        performTest("header1.h", 1, 1, "local.cc", 2, 8); // NOI18N
    }

    public void testIncludeResolverInMainCCOnExternVar() throws Exception {
        performTest("main.cc", 1, 1, "local.cc", 4, 8); // NOI18N
    }

    public void testIncludeResolverInHeader1HOnExternVar() throws Exception {
        performTest("header1.h", 1, 1, "local.cc", 4, 8); // NOI18N
    }

    public void testIncludeResolverInHeader2HOnExternVar() throws Exception {
        performTest("header2.h", 1, 1, "local.cc", 4, 8); // NOI18N
    }
    
    public void testIncludeResolverInMainCCOnFunction() throws Exception {
        performTest("main.cc", 1, 1, "local.cc", 6, 8); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // general staff
    
    @Override
    protected void postSetUp() throws Exception {
        super.postSetUp();
        log("postSetUp is preparing project"); // NOI18N
        initParsedProject();
        log("postSetUp finished project preparing"); // NOI18N
        log("Test " + getName() + "started"); // NOI18N
    }    
    
    @Override
    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        assert args.length == 1;
        String path = args[0];
        FileImpl currentFile = getFileImpl(new File(path));

        assertNotNull("Csm file was not found for " + path, currentFile); // NOI18N

        if (params.length == 2) {
            // Common test
            
            //int line = (Integer) params[0];
            //int column = (Integer) params[1];

            CsmIncludeResolver ir = new IncludeResolverImpl();
            CsmProject project = currentFile.getProject();
            assertNotNull(project);
            
            List<CsmFile> files = new ArrayList<>();
            files.addAll(project.getAllFiles());
            Collections.sort(files, new Comparator<CsmFile>() {
                @Override
                public int compare(CsmFile file1, CsmFile file2) {
                    assertNotNull(file1);
                    assertNotNull(file1);
                    return file1.getAbsolutePath().toString().compareTo(file2.getAbsolutePath().toString());
                }
            });
            
            for (CsmFile file : files) {
                assertNotNull(file);
                List<CsmOffsetableDeclaration> decls = new ArrayList<>();
                decls.addAll(file.getDeclarations());
                Collections.sort(decls, FileImpl.START_OFFSET_COMPARATOR);
                for (CsmOffsetableDeclaration decl : decls) {
                    assertNotNull(decl);
                    if (ir.isObjectVisible(currentFile, decl)) {
                        streamOut.println("Declaration " + decl.getName() + " is visible"); // NOI18N
                    } else {
                        streamOut.println("Declaration " + decl.getName() + " is not visible"); // NOI18N
                        String include = ir.getIncludeDirective(currentFile, decl);
                        assertNotNull(include);
                        if (include.length() != 0) {
                            streamOut.println("    Include directive is " + include); // NOI18N
                        } else {
                            streamOut.println("    Include directive was not found"); // NOI18N
                        }
                    }
                }
            }
        } else if (params.length == 5) {
            // Visibitity test for specific object
            
            //int line = (Integer) params[0];
            //int column = (Integer) params[1];

            String objectSource = (String) params[2];
            
            int objectLine = (Integer) params[3];
            int objectColumn = (Integer) params[4];

            BaseDocument doc = getBaseDocument(getDataFile(objectSource));
            assertNotNull(doc);
            int offset = CndCoreTestUtils.getDocumentOffset(doc, objectLine, objectColumn);
            
            CsmReferenceResolver rr = new ReferenceResolverImpl();
            
            CsmFile objectFile = CsmUtilities.getCsmFile(doc, true, false);
            assertNotNull(objectFile);
            CsmReference objectReference = rr.findReference(objectFile, doc, offset);
            assertNotNull(objectReference);
            CsmObject ob = objectReference.getReferencedObject();
            assertNotNull(ob);
            
            CsmIncludeResolver ir = new IncludeResolverImpl();
            
            String objectName;
            if(CsmKindUtilities.isDeclaration(ob)) {
                objectName = ((CsmOffsetableDeclaration)ob).getName().toString();
            } else {
                objectName = ob.toString();
            }
            
            if (ir.isObjectVisible(currentFile, ob)) {
                streamOut.println("Declaration " + objectName + " is visible"); // NOI18N
            } else {
                streamOut.println("Declaration " + objectName + " is not visible"); // NOI18N
                String include = ir.getIncludeDirective(currentFile, ob);
                if (include.length() != 0) {
                    streamOut.println("    Include directive is " + include); // NOI18N
                } else {
                    streamOut.println("    Include directive was not found"); // NOI18N
                }
            }
        } else {
            assert true; // Bad test params
        }
    }
    
    private void performTest(String source, int line, int column) throws Exception {
        super.performTest(source, getName(), null, line, column);
    }
    
    private void performTest(String source, int line, int column, String objectSource, int objectLine, int objectColumn) throws Exception {
        super.performTest(source, getName(), null, line, column, objectSource, objectLine, objectColumn);
    }
}
