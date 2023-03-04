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

import com.sun.source.tree.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Dusan Balek
 */
public class ClashingImportsTest extends GeneratorTestBase {

    /** Creates a new instance of ClashingImportsTest */
    public ClashingImportsTest(String name) {
        super(name);
    }
    
    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite(ClashingImportsTest.class);
        return suite;
    }
    
    public void testAddImport() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for(StatementTree st : body.getStatements())
                    stats.add(st);
                TypeElement e = workingCopy.getElements().getTypeElement("java.awt.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "awtList", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddImport_ClashingImports.pass");
    }

    public void testAddClashingImport() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports2.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for(StatementTree st : body.getStatements())
                    stats.add(st);
                TypeElement e = workingCopy.getElements().getTypeElement("java.util.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "list", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddClashingImport_ClashingImports.pass");
    }

    public void testAddClashingImport2() throws IOException {
        testFile = getFile(getSourceDir(), getSourcePckg() + "ClashingImports3.java");
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                ClassTree clazz = (ClassTree) cut.getTypeDecls().get(0);
                MethodTree node = (MethodTree) clazz.getMembers().get(0);
                BlockTree body = node.getBody();
                List<StatementTree> stats = new ArrayList<StatementTree>();
                for(StatementTree st : body.getStatements())
                    stats.add(st);
                TypeElement e = workingCopy.getElements().getTypeElement("java.awt.List");
                ExpressionTree type = make.QualIdent(e);
                stats.add(make.Variable(make.Modifiers(Collections.<Modifier>emptySet()), "awtList", type, null));
                workingCopy.rewrite(body, make.Block(stats, false));
            }
        };
        src.runModificationTask(task).commit();
        assertFiles("testAddClashingImport2.pass");
    }
    
    String getSourcePckg() {
        return "org/netbeans/test/codegen/";
    }

    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/ClashingImportsTest/";
    }

    @Override
    void assertFiles(final String aGoldenFile) throws IOException, FileStateInvalidException {
        assertFile("File is not correctly generated.",
                getTestFile(),
                getFile(getGoldenDir(), getGoldenPckg() + aGoldenFile),
                getWorkDir(),
                new WhitespaceIgnoringDiff()
                );
    }
    
}
