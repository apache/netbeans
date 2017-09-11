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
