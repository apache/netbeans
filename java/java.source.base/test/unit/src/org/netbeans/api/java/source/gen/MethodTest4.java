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
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import java.io.IOException;
import java.util.Collections;
import org.netbeans.modules.java.source.transform.Transformer;
import org.netbeans.junit.NbTestSuite;

/**
 * Testing method throws() section.
 *
 * @author Pavel Flaska
 */
public class MethodTest4 extends GeneratorTestMDRCompat {
    
    /** Need to be defined because of JUnit */
    public MethodTest4(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new MethodTest4("testAddFirstThrows"));
        suite.addTest(new MethodTest4("testAddSecondThrows"));
        suite.addTest(new MethodTest4("testAddThirdThrows"));
        /*suite.addTest(new MethodTest4("testRemoveFirstThrows"));
        suite.addTest(new MethodTest4("testRemoveLastThrows"));
        suite.addTest(new MethodTest4("testRemoveAllThrows"));
        suite.addTest(new MethodTest4("testAnnotationAndThrows"));
        suite.addTest(new MethodTest4("testRemoveAnnAndAddThrows"));
        suite.addTest(new MethodTest4("testAddTypeParam"));*/
        return suite;
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "MethodTest4.java");
    }

    public void testAddFirstThrows() throws IOException {
        System.err.println("testAddFirstThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("java.io.IOException"));
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddFirstThrows.pass");
    }

    public void testAddSecondThrows() throws IOException {
        System.err.println("testAddSecondThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("java.io.FileNotFoundException"));
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddSecondThrows.pass");
    }
    
    public void testAddThirdThrows() throws IOException {
        System.err.println("testAddThirdThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.insertMethodThrows(node, 0, make.Identifier("java.io.WriteAbortedException"));
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddThirdThrows.pass");
    }
    
    public void testRemoveFirstThrows() throws IOException {
        System.err.println("testRemoveFirstThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveFirstThrows.pass");
    }
    
    public void testRemoveLastThrows() throws IOException {
        System.err.println("testRemoveLastThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        // just to test the method
                        ExpressionTree lastThrows = node.getThrows().get(1);
                        MethodTree copy = make.removeMethodThrows(node, lastThrows);
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveLastThrows.pass");
    }
    
    public void testRemoveAllThrows() throws IOException {
        System.err.println("testRemoveAllThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        // there will be nothing in throws section.
                        MethodTree copy = make.removeMethodThrows(node, 0);
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveAllThrows.pass");
    }
    
    public void testAnnotationAndThrows() throws IOException {
        System.err.println("testAnnotationAndThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        ModifiersTree newMods = make.addModifiersAnnotation(
                            node.getModifiers(), 
                            make.Annotation(
                                make.Identifier("Deprecated"),
                                Collections.<ExpressionTree>emptyList()
                            )
                        );
                        this.copy.rewrite(node.getModifiers(), newMods);
                        MethodTree copy = make.addMethodThrows(node, make.Identifier("java.io.IOException"));
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAnnotationAndThrows.pass");
    }
    
    public void testRemoveAnnAndAddThrows() throws IOException {
        System.err.println("testRemoveAnnAndAddThrows");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        ModifiersTree newMods = make.removeModifiersAnnotation(node.getModifiers(), 0);
                        this.copy.rewrite(node.getModifiers(), newMods);
                        MethodTree copy = make.insertMethodThrows(node, 0, make.Identifier("java.io.WriteAbortedException"));
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testRemoveAnnAndAddThrows.pass");
    }
    
    public void testAddTypeParam() throws IOException {
        System.err.println("testAddTypeParam");
        process(
            new Transformer<Void, Object>() {
                public Void visitMethod(MethodTree node, Object p) {
                    super.visitMethod(node, p);
                    if ("fooMethod".contentEquals(node.getName())) {
                        MethodTree copy = make.addMethodTypeParameter(
                            node, 
                            make.TypeParameter("T", Collections.<ExpressionTree>emptyList())
                        );
                        this.copy.rewrite(node, copy);
                    }
                    return null;
                }
            }
        );
        assertFiles("testAddTypeParam.pass");
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/MethodTest4/";
    }
    
}
