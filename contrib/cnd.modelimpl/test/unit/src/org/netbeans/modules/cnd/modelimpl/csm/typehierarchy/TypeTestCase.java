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
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmTypeHierarchyResolver;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelTestBase;

/**
 * base class for guard block tests
 *
 */
public class TypeTestCase extends TraceModelTestBase {

    public TypeTestCase(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGuard() throws Exception {
        performTest("type.cc"); // NOI18N
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
                if ("type.cc".equals(file.getName().toString())) { // NOI18N
                    found = true;
                    checkFile(file);
                }
            }
            assertTrue("Not found FileImpl for 'type.cc'", found); // NOI18N
        } finally {
            CsmCacheManager.leave();
        }
    }

    private void checkFile(FileImpl file) {
        for (CsmOffsetableDeclaration decl : file.getDeclarations()) {
            if (CsmKindUtilities.isClass(decl)) {
                CsmClass cls = (CsmClass) decl;
                if ("base".equals(cls.getName().toString())) {
                    {
                        Collection<CsmReference> col = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, true);
                        boolean sub1 = false;
                        boolean sub2 = false;
                        boolean sub3 = false;
                        int i = 0;
                        for (CsmReference ref : col) {
                            CsmClass c = (CsmClass) ref.getOwner();
                            assertNotNull("Reference should have owner", c); // NOI18N
                            String name = c.getName().toString();
                            if ("sub1".equals(name)) {
                                sub1 = true;
                                i++;
                            } else if ("sub2".equals(name)) {
                                sub2 = true;
                                i++;
                            } else if ("sub3".equals(name)) {
                                sub3 = true;
                                i++;
                            }
                        }
                        assertTrue("Number of direct subtypes should be 3", col.size() == i); // NOI18N
                        assertTrue("sub1 is not found as subtype", sub1); // NOI18N
                        assertTrue("sub2 is not found as subtype", sub2); // NOI18N
                        assertTrue("sub3 is not found as subtype", sub3); // NOI18N
                    }
                    {
                        Collection<CsmReference> col = CsmTypeHierarchyResolver.getDefault().getSubTypes(cls, false);
                        boolean sub1 = false;
                        boolean sub2 = false;
                        boolean sub3 = false;
                        boolean sub4 = false;
                        boolean sub5 = false;
                        int i = 0;
                        for (CsmReference ref : col) {
                            CsmClass c = (CsmClass) ref.getOwner();
                            assertNotNull("Reference should have owner", c); // NOI18N
                            String name = c.getName().toString();
                            if ("sub1".equals(name)) {
                                sub1 = true;
                                i++;
                            } else if ("sub2".equals(name)) {
                                sub2 = true;
                                i++;
                            } else if ("sub3".equals(name)) {
                                sub3 = true;
                                i++;
                            } else if ("sub4".equals(name)) {
                                sub4 = true;
                                i++;
                            } else if ("sub5".equals(name)) {
                                sub5 = true;
                                i++;
                            }
                        }
                        assertTrue("Number of subtypes should be 5", col.size() == i); // NOI18N
                        assertTrue("sub1 is not found as subtype", sub1); // NOI18N
                        assertTrue("sub2 is not found as subtype", sub2); // NOI18N
                        assertTrue("sub3 is not found as subtype", sub3); // NOI18N
                        assertTrue("sub4 is not found as subtype", sub4); // NOI18N
                        assertTrue("sub5 is not found as subtype", sub5); // NOI18N
                    }
                    return;
                }
            }
        }
        assertTrue("Not found class 'base' in 'type.cc'", false); // NOI18N
    }

    //class base {};
//class sub1 : base{};
//class sub2 : public base{};
//class sub3 : private virtual base{};
//class sub4 : sub3{};
//class sub5 : sub2{};
}
