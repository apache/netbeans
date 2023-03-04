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

import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.java.source.transform.Transformer;

/**
 *
 * @author Pavel Flaska
 */

public class CompareTreeTest extends GeneratorTestBase {
    
    /** Creates a new instance of CompareTreeTest */
    public CompareTreeTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(CompareTreeTest.class);
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodTest1.java");
    }
    /*
    public void testMethodModifiers() throws IOException {
        final TokenHierarchy[] cut = new TokenHierarchy[2];
        getJavaSource(getTestFile()).runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController cc) {
                cut[0] = cc.getTokenHierarchy();
            }
        });
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("firstMethod".contentEquals(node.getName())) {
                        ModifiersTree origMods = node.getModifiers();
                        Set<Modifier> njuMods = new HashSet<Modifier>();
                        njuMods.add(Modifier.PRIVATE);
                        njuMods.add(Modifier.STATIC);
                        copy.rewrite(origMods, make.Modifiers(njuMods));
                    }
                    return null;
                }
            }
        );
        getJavaSource(getTestFile()).runUserActionTask(new CancellableTask<CompilationController>() {
            public void cancel() {
            }
            public void run(CompilationController cc) {
                cut[1] = cc.getTokenHierarchy();
            }
        });
        Map<Object, CharSequence[]> result = TreeChecker.compareTokens(cut[0], cut[1]);
        //CharSequence[][] cs = .iterator().next().
        for (Map.Entry<Object, CharSequence[]> item : result.entrySet()) {
            System.out.println(item.getKey() + ": '" + item.getValue()[0] + "' != '" + item.getValue()[1] + "'");
        }
    }*/
    
    public void testMethodName() throws IOException {
        final TokenHierarchy[] cut = new TokenHierarchy[2];
        getJavaSource(getTestFile()).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) {
                cut[0] = cc.getTokenHierarchy();
            }
        },true);
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("secondMethod".contentEquals(node.getName())) {
                        MethodTree njuMethod = make.setLabel(node, "druhaMetoda");
                        copy.rewrite(node, njuMethod);
                    }
                    return null;
                }
            }
        );
        getJavaSource(getTestFile()).runUserActionTask(new Task<CompilationController>() {

            public void run(CompilationController cc) {
                cut[1] = cc.getTokenHierarchy();
            }
        },true);
        Map<Object, CharSequence[]> result = TreeChecker.compareTokens(cut[0], cut[1]);
        //CharSequence[][] cs = .iterator().next().
        for (Map.Entry<Object, CharSequence[]> item : result.entrySet()) {
            System.out.println(item.getKey() + ": '" + item.getValue()[0] + "' != '" + item.getValue()[1] + "'");
        }
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/MethodTest1/MethodTest1/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }
    
}
