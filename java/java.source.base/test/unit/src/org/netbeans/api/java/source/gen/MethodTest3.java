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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil2;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;
import junit.textui.TestRunner;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationInfo;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Tests the method generator.
 *
 * @author  Jan Becicka
 */
public class MethodTest3 extends GeneratorTestBase {
    
    /** Need to be defined because of JUnit */
    public MethodTest3(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MethodTest3("testMethodThrows"));
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodTest3.java");
        i = ClasspathInfo.create(testFile);
    }
    
    ClasspathInfo i;

    /**
     * Changes the modifiers on method. Removes public modifier, sets static
     * and private modifier.
     */
    public void testMethodThrows() throws IOException {
        FileObject fo = FileUtil.toFileObject(testFile);
        JavaSource js = JavaSource.forFileObject(fo);
        js.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy wc) {
                try {
                    wc.toPhase(JavaSource.Phase.PARSED);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                RemoveException remove = new RemoveException(0);
                SourceUtilsTestUtil2.run(wc, remove);
                
                AddException add = new AddException(wc, "java.lang.IllegalMonitorStateException");
                SourceUtilsTestUtil2.run(wc, add);
            }
        }).commit();
        
        assertFiles("testMethodThrows.pass");
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestRunner.run(suite());
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/MethodTest3/MethodTest3/";
    }
    
    private class RemoveException extends Transformer<Void, Object> {
        
        int index;
        public RemoveException(int index) {
            this.index = index;
        }
        
        public Void visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            Element al = model.getElement(node);
            if ("foo()".equals(al.toString())) {
                List<ExpressionTree> l = new ArrayList<ExpressionTree>();
                int i=0;
                for (ExpressionTree n: node.getThrows()) {
                    if (i!=index) {
                        l.add(n);
                    }
                }
                MethodTree njuMethod = make.Method(
                        node.getModifiers(),
                        node.getName(),
                        (ExpressionTree) node.getReturnType(),
                        node.getTypeParameters(),
                        node.getParameters(),
                        l,
                        node.getBody(),
                        (ExpressionTree) node.getDefaultValue()
                        );
                model.setElement(njuMethod, al);
                copy.rewrite(node, njuMethod);
            }
            return null;
        }
    }
    private class AddException extends Transformer<Void, Object> {

        private CompilationInfo info;
        ExpressionTree tr;
        String ex;
        public AddException(CompilationInfo info, String ex) {
            this.info = info;
            this.ex = ex;
        }
        
        public Void visitMethod(MethodTree node, Object p) {
            super.visitMethod(node, p);
            Element al = model.getElement(node);
            if ("foo()".equals(al.toString())) {
                tr = make.Identifier(ex);
                List<ExpressionTree> l = new ArrayList<ExpressionTree>();
                l.addAll(node.getThrows());
                l.add(tr);
                MethodTree njuMethod = make.Method(
                        node.getModifiers(),
                        node.getName(),
                        (ExpressionTree) node.getReturnType(),
                        node.getTypeParameters(),
                        node.getParameters(),
                        l,
                        node.getBody(),
                        (ExpressionTree) node.getDefaultValue()
                        );
                Element el = info.getElements().getTypeElement(((IdentifierTree) tr).getName().toString());
                
                model.setElement(tr, el);
                model.setType(tr, el.asType());
                copy.rewrite(node, njuMethod);
            }
            return null;
        }
    }
    
}
