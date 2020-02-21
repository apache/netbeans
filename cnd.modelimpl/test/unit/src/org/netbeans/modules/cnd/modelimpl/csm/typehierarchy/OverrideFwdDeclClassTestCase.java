/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
