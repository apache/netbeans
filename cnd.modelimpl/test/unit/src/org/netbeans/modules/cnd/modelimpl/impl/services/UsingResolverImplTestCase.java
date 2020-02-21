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
