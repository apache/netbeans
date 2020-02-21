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
