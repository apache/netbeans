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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
