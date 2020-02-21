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
