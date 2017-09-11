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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
