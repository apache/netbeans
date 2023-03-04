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
package org.netbeans.modules.groovy.support.actions;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.parsing.spi.Parser.Result;

public class TestMethodUtilTest extends NbTestCase {

    private File compileDir;

    private StringBuilder groovyScript;

    private ModuleNode moduleNode;

    public TestMethodUtilTest(final String name) {
        super(name);
    }

    @Override
    protected void setUp() throws IOException {
        compileDir = new File(this.getWorkDir(), UUID.randomUUID().toString());
        compileDir.mkdir();
        groovyScript = new StringBuilder()
                .append("class AGroovyClass {\n")
                .append("public void method1() {\n")
                .append("//some comment\n")
                .append("}\n")
                .append("//just some comment not in a method\n")
                .append("}\n")
                .append("//comment outside of class");
        moduleNode = createGroovyModuleNode();
    }

    @Override
    protected void tearDown() {
        compileDir.deleteOnExit();
    }

    private ModuleNode createGroovyModuleNode() {
        final CompilerConfiguration conf = new CompilerConfiguration();
        conf.setTargetDirectory(compileDir);
        final CompilationUnit cu
                = new CompilationUnit(
                        new GroovyClassLoader(
                                getClass().getClassLoader()
                        )
                );
        cu.configure(conf);
        final SourceUnit sn = cu.addSource("AGroovyClass.groovy", groovyScript.toString());
        try {
            cu.compile();
        } catch (Exception e) {
            //this groovy compile bit didn't work when running tests
            //but did work when debugging them, so odd, but the AST is in
            //place either way, and is all we need for this
            this.log(e.getMessage());
        }
        final ModuleNode mn = sn.getAST();
        return mn;
    }

    public void testGetLineAndColumn() {
        final StringBuilder g = new StringBuilder()
                .append("class MyClass {\n")
                .append("public void method1() {\n")
                .append("//cursor here before h\n")
                .append("}\n")
                .append("}");
        //3rd line, 10th column ... 3,10
        int cursor = g.lastIndexOf("here");
        int[] lc = TestMethodUtil.getLineAndColumn(g.toString(), cursor);
        assert lc[0] == 3;
        assert lc[1] == 10;

        cursor = g.lastIndexOf("//");
        lc = TestMethodUtil.getLineAndColumn(g.toString(), cursor);
        assert lc[0] == 3;
        assert lc[1] == 1;

        lc = TestMethodUtil.getLineAndColumn(g.toString(), 0);
        assert lc[0] == 1;
        assert lc[1] == 1;
    }

    public void testIsBetweenLinesAndColumns() {
        assert TestMethodUtil.isBetweenLinesAndColumns(0, 0, 0, 0, 0, 0);
        assert TestMethodUtil.isBetweenLinesAndColumns(1, 5, 2, 10, 2, 10);
        assert !TestMethodUtil.isBetweenLinesAndColumns(1, 5, 2, 10, 2, 11);
        assert !TestMethodUtil.isBetweenLinesAndColumns(1, 5, 2, 10, 3, 10);
        assert TestMethodUtil.isBetweenLinesAndColumns(1, 5, 1, 30, 1, 20);
        assert !TestMethodUtil.isBetweenLinesAndColumns(1, 5, 1, 30, 1, 31);
        assert !TestMethodUtil.isBetweenLinesAndColumns(1, 5, 1, 30, 1, 4);
        assert !TestMethodUtil.isBetweenLinesAndColumns(2, 5, 4, 30, 1, 4);
    }

    //setup some simple classes to mock behavior of the
    //reflection code in TestMethodUtil
    private class TestASTRoot {

        public ModuleNode getModuleNode() {
            return moduleNode;
        }
    }

    private class TestGroovyParserResult extends Result {

        public TestGroovyParserResult() {
            super(null);
        }

        @Override
        protected void invalidate() {

        }

        public TestASTRoot getRootElement() {
            return new TestASTRoot();
        }

    }

    public void testExtractModuleNode() {
        final TestGroovyParserResult r = new TestGroovyParserResult();
        final ModuleNode lmn = TestMethodUtil.extractModuleNode(r);
        assert moduleNode.equals(lmn);
    }

    public void testGetClassNodeForLineAndColumn() throws IOException {
        List<ClassNode> classes = moduleNode.getClasses();

        assert classes.size() > 0;
        assert TestMethodUtil.getClassNodeForLineAndColumn(moduleNode, 0, 0) == null;
        final ClassNode cn = TestMethodUtil.getClassNodeForLineAndColumn(moduleNode, 5, 2);
        assert cn != null;
        assert "AGroovyClass".equals(cn.getNameWithoutPackage());
        assert TestMethodUtil.getClassNodeForLineAndColumn(moduleNode, 7, 5) == null;
    }

    public void testGetMethodNodeForLineAndColumn() {
        final ClassNode cn = TestMethodUtil.getClassNodeForLineAndColumn(moduleNode, 5, 2);
        assert cn != null;
        MethodNode mn = TestMethodUtil.getMethodNodeForLineAndColumn(cn, 5, 2);
        assert mn == null;
        mn = TestMethodUtil.getMethodNodeForLineAndColumn(cn, 3, 2);
        assert mn != null;
        assert "method1".equals(mn.getName());
    }
}
