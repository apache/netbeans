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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
