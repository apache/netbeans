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
package org.netbeans.modules.java.hints;

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.List;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.modules.java.hints.infrastructure.TreeRuleTestBase;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Jan Lahoda
 */
public class IllegalInstanceOfTest extends TreeRuleTestBase {
    
    public IllegalInstanceOfTest(String testName) {
        super(testName);
    }

    public void testSimple1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.element.*; public class Test {public void test() {Element e = null; boolean b = e instanceof TypeElement;}}",
                            154 - 30,
                            "0:118-0:142:verifier:Illegal Use of instanceOf");
    }
    
    public void testSimple2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.element.*; public class Test {public void test() {Element e = null; boolean b = e instanceof CharSequence;}}",
                            154 - 30);
    }
    
    public void testSimple3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.type.*; public class Test {public void test() {TypeMirror e = null; boolean b = e instanceof DeclaredType;}}",
                            154 - 30,
                            "0:118-0:143:verifier:Illegal Use of instanceOf");
    }
    
    public void testSimple4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import com.sun.source.tree.*; public class Test {public void test() {Tree e = null; boolean b = e instanceof StatementTree;}}",
                            146 - 30,
                            "0:110-0:136:verifier:Illegal Use of instanceOf");
    }
    
    public void testSimple5() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import com.sun.source.tree.*; public class Test {public void test() {Tree e = null; boolean b = e instanceof Scope;}}",
                            146 - 30);
    }
    
    public void test106461() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; import javax.lang.model.element.*; public class Test {public void test() {Element e = null; if (e instanceof )}}",
                            146 - 30);
    }
    
    protected List<ErrorDescription> computeErrors(CompilationInfo info, TreePath path) {
        return new IllegalInstanceOf().run(info, path);
    }

    @Override
    protected FileObject[] extraClassPath() {
        String s = System.getProperty("hints-tools.jar.location");
        if (s != null) {
            File f = new File(s);
            if (f.canRead()) {
                FileObject arch = FileUtil.toFileObject(f);
                FileObject root = FileUtil.getArchiveRoot(arch);
                return new FileObject[] { root == null ? arch : root };
            }
        }
        return super.extraClassPath();
    }

    
}
