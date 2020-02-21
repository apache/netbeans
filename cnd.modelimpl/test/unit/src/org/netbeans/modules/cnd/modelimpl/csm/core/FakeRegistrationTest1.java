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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.io.File;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.modelimpl.trace.TraceModelBase;

/**
 * Test for #131967
 */
public class FakeRegistrationTest1 extends ModelImplBaseTestCase  {

    private final static boolean verbose;
    static {
        verbose = Boolean.getBoolean("test.fake.reg.verbose");
        if( verbose ) {
            System.setProperty("cnd.modelimpl.timing", "true");
            System.setProperty("cnd.modelimpl.timing.per.file.flat", "true");
        }
        
        System.setProperty("cnd.modelimpl.parser.threads", "1");
    }    
    
    public FakeRegistrationTest1(String testName) {
        super(testName);
    }
    
    public void testSimple() throws Exception {

        File workDir = getWorkDir();
        
        File sourceFile = new File(workDir, "fake.cc");
        File dummyFile1 = new File(workDir, "dummy1.cc");
        File dummyFile2 = new File(workDir, "dummy2.cc");
        File headerFile = new File(workDir, "fake.h");
        
        writeFile(sourceFile, " #include \"fake.h\"\n BEGIN\n int x;\n void QNAME () {}\n END \n");
        writeFile(headerFile, " #define QNAME Qwe::foo\n  #define BEGIN\n #define END\n");

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("#include \"fake.h\"\n");
        }
        writeFile(dummyFile1, sb.toString());
        writeFile(dummyFile2, sb.toString());

        TraceModelBase traceModel = new  TraceModelBase(true);

	traceModel.processArguments(dummyFile1.getAbsolutePath(), sourceFile.getAbsolutePath(), dummyFile2.getAbsolutePath(), headerFile.getAbsolutePath());
        
	ModelImpl model = traceModel.getModel();
	final CsmProject project = traceModel.getProject();
        
        FileImpl csmSource = (FileImpl) project.findFile(sourceFile.getAbsolutePath(), true, false);
        assert csmSource != null;

        FileImpl csmHeader = (FileImpl) project.findFile(headerFile.getAbsolutePath(), true, false);
        assert csmHeader != null;

        csmSource.scheduleParsing(true);
        
        writeFile(headerFile, " #define QNAME foo\n #define BEGIN class C {\n #define END };\n");
        sleep(500);
        csmHeader.markReparseNeeded(true);
        csmSource.markReparseNeeded(true);
        csmSource.scheduleParsing(true);
        
        project.waitParse();
        sleep(500);
        
        assertNoExceptions();
        
        clearWorkDir();
    }

}
