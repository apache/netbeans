/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.javascript2.editor.hint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.TestSuite;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.csl.api.HintSeverity;
import org.netbeans.modules.csl.api.Rule;
import org.netbeans.modules.javascript2.editor.classpath.ClasspathProviderImplAccessor;
import org.netbeans.modules.javascript2.editor.hints.GlobalIsNotDefined;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;

import static org.netbeans.modules.javascript2.editor.JsTestBase.JS_SOURCE_ID;

/**
 *
 * @author Petr Pisl
 */
public class JsGlobalIsNotDeclaredTest extends HintTestBase {

    private static boolean CLEAN_CACHE_DIR = true;

    public JsGlobalIsNotDeclaredTest(String testName) {
        super(testName);
    }

    private static class GlobalIsNotDefinedHint extends GlobalIsNotDefined {

        @Override
        public HintSeverity getDefaultSeverity() {
            return HintSeverity.WARNING;
        }

    }

    private Rule createRule() {
        GlobalIsNotDefined gind = new GlobalIsNotDefinedHint();
        return gind;
    }

    public static TestSuite suite() {
        CLEAN_CACHE_DIR = true;
        return new TestSuite(JsGlobalIsNotDeclaredTest.class);
    }

    public void testSimple01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/globalIsNotDeclared.js", null);
    }

    public void testIssue224040() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224040.js", null);
    }

    public void testIssue224041() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224041.js", null);
    }

    public void testIssue224035() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue224035.js", null);
    }

    public void testIssue225048() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue225048.js", null);
    }

    public void testIssue225048_01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue225048_01.js", null);
    }

    public void testIssue250372() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue250372.js", null);
    }

    public void testIssue248696_01() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue248696_01.js", null);
    }

    public void testIssue248696_02() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue248696_02.js", null);
    }

    public void testIssue252022() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue252022.js", null);
    }

    public void testIssue249487() throws Exception {
        checkHints(this, createRule(), "testfiles/markoccurences/issue249487.js", null);
    }

    public void testIssue255494() throws Exception {
        checkHints(this, createRule(), "testfiles/coloring/issue255494.js", null);
    }

    public void testIssue268384() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issue268384.js", null);
    }

    public void testIssueGH4246() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issueGH4246.js", null);
    }

    public void testIssueGH4213() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issueGH4213.js", null);
    }

    public void testIssueGH4568() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issueGH4568.js", null);
    }

    public void testIssueGH5740() throws Exception {
        checkHints(this, createRule(), "testfiles/hints/issueGH5740.js", null);
    }

    @Override
    protected boolean cleanCacheDir() {
        // The cache dir also holds the index cache - if the cache is cleared,
        // the runtime of the tests increased ten-fold (the core stubs take
        // around 2s, the dom stubs 8s))
        if (CLEAN_CACHE_DIR) {
            CLEAN_CACHE_DIR = false;
            return true;
        } else {
            return CLEAN_CACHE_DIR;
        }
    }

    @Override
    protected Map<String, ClassPath> createClassPathsForTest() {
        List<FileObject> cpRoots = new ArrayList<>();
        // Both the core stubs and the dom-stubs need to be made available
        cpRoots.addAll(ClasspathProviderImplAccessor.getJsStubs());
        return Collections.singletonMap(
                JS_SOURCE_ID,
                ClassPathSupport.createClassPath(cpRoots.toArray(new FileObject[0]))
        );
    }

    @Override
    protected boolean classPathContainsBinaries() {
        return true;
    }

}
