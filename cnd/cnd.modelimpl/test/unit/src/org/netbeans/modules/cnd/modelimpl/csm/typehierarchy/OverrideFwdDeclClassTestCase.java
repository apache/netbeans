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
package org.netbeans.modules.cnd.modelimpl.csm.typehierarchy;

import java.io.File;
import java.util.Collection;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 *
 */
public class OverrideFwdDeclClassTestCase extends TraceModelTestBase {

    public OverrideFwdDeclClassTestCase(String testName) {
        super(testName);
    }

    private final String fileName = "override.cc";

    public void testFwdDeclOverride() throws Exception {
        performTest(fileName);
    }

    @Override
    protected void performTest(String source) throws Exception {
        CsmCacheManager.enter();
        try {
            File testFile = getDataFile(source);
            assertTrue("File not found " + testFile.getAbsolutePath(), testFile.exists()); // NOI18N
            performModelTest(testFile, System.out, System.err);
            boolean found = false;
            for (FileImpl file : getProject().getAllFileImpls()) {
                if (fileName.equals(file.getName().toString())) { // NOI18N
                    found = true;
                    checkFile(file);
                }
            }
            assertTrue("Not found FileImpl for " + fileName, found); // NOI18N
        } finally {
            CsmCacheManager.leave();
        }
    }

    private void checkFile(FileImpl file) {
        System.out.println(file.getDeclarations());
        for (CsmOffsetableDeclaration decl : file.getDeclarations()) {
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                if ("AAA".equals(cls.getName().toString())) {
                    // Check for subtypes of AAA
                    Collection<CsmReference> aaaSubTypes = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, true);
                    assertTrue("AAA shouldn't have subtypes", aaaSubTypes.isEmpty());

                    // Check for subtypes of AAA::inner
                    Collection<CsmMember> aaaMembers = cls.getMembers();
                    assertEquals("AAA should have only one member", aaaMembers.size(), 1);
                    CsmMember aaaMember = aaaMembers.iterator().next();

                    if (CsmKindUtilities.isClassForwardDeclaration(aaaMember)) {
                        CsmClassForwardDeclaration fwdDecl = (CsmClassForwardDeclaration) aaaMember;
                        CsmClass fwdClass = fwdDecl.getCsmClass();
                        Collection<CsmReference> fwdSubTypes = CsmTypeHierarchyResolver.getDefault().getSubTypes(fwdClass, false);

                        assertTrue("AAA::inner should have 1 subtype", fwdSubTypes.size() == 1);
                        CsmObject referencedObject = fwdSubTypes.iterator().next().getReferencedObject();
                        assertEquals("subtype of AAA::inner should be BBB", ((CsmClass) referencedObject).getName().toString(), "BBB");

                        return;
                    }
                }
            }
        }

        fail("No declarations found in " + file.getName());
    }
}
