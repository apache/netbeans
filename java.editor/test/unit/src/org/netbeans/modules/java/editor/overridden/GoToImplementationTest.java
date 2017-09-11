/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.editor.overridden;

import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class GoToImplementationTest extends NbTestCase {
    
    private FileObject testSource;
    private JavaSource js;
    private CompilationInfo info;
    
    public GoToImplementationTest(String name) {
        super(name);
    }
    
    public void testInClassHeader1() throws Exception {
        doTest("package test;\n" +
               "@Deprecated\n" +
               "pu|blic class Test {\n" +
               "}\n",
               "test.Test");
    }
    
    public void testInClassHeader2() throws Exception {
        doTest("package test;\n" +
               "@Deprecated(|)\n" +
               "public class Test {\n" +
               "}\n",
               null);
    }
    
    public void testInClassHeader3() throws Exception {
        doTest("package test;\n" +
               "public class Test {\n" +
               "|\n" +
               "}\n",
               null);
    }

    @Override
    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[] {"org/netbeans/modules/java/editor/resources/layer.xml"}, new Object[0]);

        clearWorkDir();
    }
    
    private void prepareTest(String fileName, String code) throws Exception {
        FileObject workFO = FileUtil.toFileObject(getWorkDir());
        
        assertNotNull(workFO);
        
        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cacheFO = workFO.createFolder("cache");
        
        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cacheFO);
        
        testSource = FileUtil.createData(sourceRoot, fileName);
        
        assertNotNull(testSource);
        
        try (OutputStream out = testSource.getOutputStream()) {
            out.write(code.getBytes());
        }
        
        js = JavaSource.forFileObject(testSource);
        
        assertNotNull(js);
        
        info = SourceUtilsTestUtil.getCompilationInfo(js, Phase.RESOLVED);
        
        assertNotNull(info);
    }
    
    private void doTest(String code, String golden) throws Exception {
        int caret = code.indexOf('|');
        
        assertTrue(caret != (-1));
        
        prepareTest("test/Test.java", code.replace("|", ""));
        
        Element element = GoToImplementation.resolveTarget(info, info.getSnapshot().getSource().getDocument(true), caret, new AtomicBoolean());
        String elementText = element != null ? info.getElementUtilities().getElementName(element, true).toString() : null;
        
        assertEquals(golden, elementText);
    }
}
