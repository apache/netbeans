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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

import com.sun.source.tree.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Pavel Flaska
 */
public class AnnotationOnLocVarTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of AnnotationAttributeValueTest */
    public AnnotationOnLocVarTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new AnnotationOnLocVarTest("testAddAnnToLocVar"));
        suite.addTest(new AnnotationOnLocVarTest("testAddLocVarWithAnn"));
        return suite;
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "AnnOnLocalVar.java");
    }

    public void testAddAnnToLocVar() throws IOException {
        System.err.println("testAddAnnToLocVar");
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = method.getBody();
                List<? extends StatementTree> statements = body.getStatements();
                VariableTree statement = (VariableTree) statements.get(1);
                // mods will be replaced by a new one
                ModifiersTree mods = statement.getModifiers();
                List<AnnotationTree> anns = new ArrayList<AnnotationTree>(1);
                List<AssignmentTree> attribs = new ArrayList<AssignmentTree>(4);
                attribs.add(make.Assignment(make.Identifier("id"), make.Literal(Integer.valueOf(666))));
                attribs.add(make.Assignment(make.Identifier("synopsis"), make.Literal("fat")));
                attribs.add(make.Assignment(make.Identifier("engineer"), make.Literal("PaF")));
                attribs.add(make.Assignment(make.Identifier("date"), make.Literal("2005")));
                anns.add(make.Annotation(make.Identifier("AnnotationType"), attribs));
                workingCopy.rewrite(mods, make.Modifiers(mods.getFlags(), anns));
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        File g = getFile(getGoldenDir(), getGoldenPckg() + "testAddAnnToLocVar_AnnotationOnLocVarTest.pass");
        String gold = TestUtilities.copyFileToString(g);
        assertEquals(res, gold);
    }

    public void testAddLocVarWithAnn() throws java.io.IOException, FileStateInvalidException {
        System.err.println("testAddLocVarWithAnn");
        JavaSource testSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws java.io.IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) workingCopy.getCompilationUnit().getTypeDecls().get(0);
                MethodTree method = (MethodTree) clazz.getMembers().get(0);
                BlockTree bt = method.getBody();
                List<StatementTree> statements = new ArrayList<StatementTree>(bt.getStatements());
                statements.remove(0); // remove super(), which is in class file, but not present in source
                List<AnnotationTree> anns = new ArrayList<AnnotationTree>(1);
                List<AssignmentTree> attribs = new ArrayList<AssignmentTree>(4);
                attribs.add(make.Assignment(make.Identifier("id"), make.Literal(Integer.valueOf(777))));
                attribs.add(make.Assignment(make.Identifier("synopsis"), make.Literal("thin")));
                attribs.add(make.Assignment(make.Identifier("engineer"), make.Literal("Snoopy")));
                attribs.add(make.Assignment(make.Identifier("date"), make.Literal("2001")));
                anns.add(make.Annotation(make.Identifier("AnnotationType"), attribs));
                statements.add(0, make.Variable(
                    make.Modifiers(Collections.singleton(Modifier.FINAL), anns),
                    "testVar",
                    make.Identifier("java.util.List"),
                    make.NewClass(
                        null,
                        Collections.<ExpressionTree>emptyList(),
                        make.Identifier("java.util.ArrayList"),
                        Collections.singletonList(make.Literal(Integer.valueOf(3))),
                        null
                    )
                ));
                BlockTree njuBlock = make.Block(statements, false);
                workingCopy.rewrite(bt, njuBlock);
            }
        };
        testSource.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        System.err.println(res);
        File g = getFile(getGoldenDir(), getGoldenPckg() + "testAddLocVarWithAnn_AnnotationOnLocVarTest.pass");
        String gold = TestUtilities.copyFileToString(g);
        assertEquals(res, gold);
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/AnnotationOnLocVarTest/";
    }
}
