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
 */
public class FortranFileModelTest extends TraceModelTestBase {
    public FortranFileModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("cnd.modelimpl.tracemodel.project.name", "DummyProject"); // NOI18N
        System.setProperty("parser.report.errors", "true");
        System.setProperty("antlr.exceptions.hideExpectedTokens", "true");
        super.setUp();
    }

    @Override
    protected List<Class<?>> getServices() {
        List<Class<?>> list = new ArrayList<>();
        list.add(FileEncodingQueryImplementationImpl.class);
        list.addAll(super.getServices());
        return list;
    }

    @Override
    protected void postSetUp() {
        // init flags needed for file model tests
        getTraceModel().setDumpModel(true);
        getTraceModel().setDumpPPState(true);
    }

    public void testFile1() throws Exception {
        performTest("file1.f"); // NOI18N
    }

    public void testFile2() throws Exception {
        performTest("file2.f"); // NOI18N
    }

    public void testBug182945() throws Exception {
        // Bug 182945 - *Fortran* Navigator shows non-existed items
        performTest("bug182945.f"); // NOI18N
    }

    public void testBug182702() throws Exception {
        // Bug 182702 - *Fortran* Navigator will be empty if Fortran file contains Cyrillic symbols in comments
        performTest("bug182702.f"); // NOI18N
    }

    public void testBug182520() throws Exception {
        // Bug 182520 - Navigator doesn't show all subroutines for *Fortran* files
        performTest("bug182520.f"); // NOI18N
    }

    public void testBug183152() throws Exception {
        // Bug 183152 - keyword pause breaks *Fortran* Navigator
        performTest("bug183152.f"); // NOI18N
    }

    public void testBug183073() throws Exception {
        // Bug 183073 - keyword common breaks *Fortran* Navigator
        performTest("bug183073.f"); // NOI18N
    }

    public void testBug185624() throws Exception {
        // Bug 185624 - *Fortran* navigator: "Go To Source" points a cursor to the end of module rather than at begin 
        performTest("bug185624.f"); // NOI18N
    }

    public void testBug186251() throws Exception {
        // Bug 186251 - *Fortran* Navigator has to recognize long strings
        performTest("bug186251.f"); // NOI18N
    }

    public void testBug184997() throws Exception {
        // Bug 184997 - *Fortran* navigator doesn't show subroutine with two parameters
        performTest("bug184997.f"); // NOI18N
    }

    public void testBug183086() throws Exception {
        // Bug 183086 - *Fortran* Navigator: inner function should be child node of program node
        performTest("bug183086.f"); // NOI18N
    }

    public void testBug205526() throws Exception {
        // Bug 205526 - Fortran parser fails on some do and if statements
        performTest("bug205526.f"); // NOI18N
    }

    public void testBug207573() throws Exception {
        // Bug 207573 - *Fortran* navigator doesn't show subroutines in some cases
        performTest("bug207573.f"); // NOI18N
    }

    public void testBug212602() throws Exception {
        // Bug 212602 - Navigator does not work with Fortran files (new project from existing sources)
        performTest("bug212602.f"); // NOI18N
    }

    public void testBug208927() throws Exception {
        // Bug 208927 - Fortran Navigator not showing subroutines if they contain a continuation line or semicolon
        performTest("bug208927.f"); // NOI18N
    }

    public void testBug207681() throws Exception {
        // Bug 207681 - Navigator does not show the list of subroutines in fortran file
        performTest("bug207681.f"); // NOI18N
    }
    
    public void testBug228631() throws Exception {
        // Bug 228631 - Infinite parsing of fortran source
        performTest("bug228631.f"); // NOI18N
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
