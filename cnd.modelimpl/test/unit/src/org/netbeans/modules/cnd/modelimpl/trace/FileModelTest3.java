/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.trace;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;

/**
 *
 */
public class FileModelTest3 extends TraceModelTestBase {

    public FileModelTest3(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
//        System.setProperty("cnd.modelimpl.trace.registration", "true");
//        System.setProperty("cnd.modelimpl.parser.threads", "1");
        super.setUp();
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(FortranFileModelTest.FileEncodingQueryImplementationImpl.class);
        list.addAll(super.getServices());
        return list;
    }


    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    // it behaved differently on 1-st and subsequent runs
    public void testResolverClassString_01() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }

    // it behaved differently on 1-st and subsequent runs
    public void testResolverClassString_02() throws Exception {
        performTest("resolver_class_string.cc"); // NOI18N
    }
    
    public void testBug242674() throws Exception {
        performTest("bug242674.cpp"); // NOI18N
    }    
    
    public void testBug242861() throws Exception {
        performTest("bug242861.cpp");
    }
    
    public void testBug243546() throws Exception {
        performTest("bug243546.cpp");
    }
    
    public void testBug248661() throws Exception {
        performTest("bug248661.cpp");
    }
    
    public void testBug249746() throws Exception {
        performTest("bug249746.cpp");
    }
    
    public void testBug250243() throws Exception {
        performTest("bug250243.cpp");
    }
    
    public void testBug250270() throws Exception {
        performTest("bug250270.cpp");
    }
    
    public void testBug250324() throws Exception {
        performTest("bug250324.cpp");
    }
    
    public void testBug250325() throws Exception {
        performTest("bug250325.cpp");
    }
    
    public void testBug251621() throws Exception {
        performTest("bug251621.cpp");
    }
    
    public void testBug252427() throws Exception {
        performTest("bug252427.cpp");
    }
    
    public void testBug252425() throws Exception {
        performTest("bug252425.cpp");
    }
    
    public void testBug252875() throws Exception {
        performTest("bug252875.c");
    }

    public void testBug252875_UTF() throws Exception {
        performTest("bug252875_1.c");
    }

    @Override
    protected Class<?> getTestCaseDataClass() {
        return FileModelTest.class;
    }

    public static class FileEncodingQueryImplementationImpl extends FileEncodingQueryImplementation {

        public FileEncodingQueryImplementationImpl() {
        }

        @Override
        public Charset getEncoding(FileObject file) {
            return Charset.forName("UTF-8");
        }
    }
}
