/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
