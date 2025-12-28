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
package org.netbeans.api.java.source.gen;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ImportTree;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.FileStateInvalidException;

/**
 *
 * @author Pavel Flaska
 */
public class ImportFormatTest extends GeneratorTestMDRCompat {
    
    /** Creates a new instance of ImportFormatTest */
    public ImportFormatTest(String name) {
        super(name);
    }

    public static NbTestSuite suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(new ImportFormatTest("testFirstAddition"));
        suite.addTest(new ImportFormatTest("testAddFirstImport"));
        suite.addTest(new ImportFormatTest("testAddLastImport"));
        suite.addTest(new ImportFormatTest("testRemoveInnerImport"));
        suite.addTest(new ImportFormatTest("testRemoveFirstImport"));
        suite.addTest(new ImportFormatTest("testRemoveLastImport"));
        suite.addTest(new ImportFormatTest("testRemoveAllRemaning"));
        return suite;
    }
    
    @Override
    protected void setUp() throws FileStateInvalidException, Exception {
        super.setUp();
        testFile = getFile(getSourceDir(), getSourcePckg() + "ImportClass1.java");
    }

    public void testFirstAddition() throws IOException, FileStateInvalidException {
        System.err.println("testFirstAddition");
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>();
                imports.add(make.Import(make.Identifier("java.util.List"), false));
                imports.add(make.Import(make.Identifier("java.util.Set"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testFirstAddition_ImportFormatTest.pass");
    }

    public void testAddFirstImport() throws IOException, FileStateInvalidException {
        System.err.println("testAddFirstImport");
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                imports.add(0, make.Import(make.Identifier("java.util.AbstractList"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddFirstImport_ImportFormatTest.pass");
    }
    

    public void testAddLastImport() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                imports.add(make.Import(make.Identifier("java.io.IOException"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddLastImport_ImportFormatTest.pass");
    }
    
    public void testRemoveInnerImport() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                imports.remove(1);
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testRemoveInnerImport_ImportFormatTest.pass");
    }
    
    public void testRemoveFirstImport() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                imports.remove(0);
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testRemoveFirstImport_ImportFormatTest.pass");
    }

    public void testRemoveLastImport() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                imports.remove(1);
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testRemoveLastImport_ImportFormatTest.pass");
    }
    
    public void testRemoveAllRemaning() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        Collections.EMPTY_LIST,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testRemoveAllRemaning_ImportFormatTest.pass");
    }

    
    public void testAddSeveral() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = (List<ImportTree>) cut.getImports();
                imports.add(make.Import(make.Identifier("java.util.List"), false));
                imports.add(make.Import(make.Identifier("java.util.Set"), false));
                imports.add(make.Import(make.Identifier("javax.swing.CellRendererPane"), false));
                imports.add(make.Import(make.Identifier("javax.swing.BorderFactory"), false));
                imports.add(make.Import(make.Identifier("javax.swing.ImageIcon"), false));
                imports.add(make.Import(make.Identifier("javax.swing.InputVerifier"), false));
                imports.add(make.Import(make.Identifier("javax.swing.GrayFilter"), false));
                imports.add(make.Import(make.Identifier("javax.swing.JFileChooser"), false));
                imports.add(make.Import(make.Identifier("javax.swing.AbstractAction"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testAddSeveral_ImportFormatTest.pass");
    }
    
    public void testRemoveInside() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                imports.remove(4);
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testRemoveInside_ImportFormatTest.pass");
    }
    
    public void testMoveFirst() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                ImportTree oneImport = imports.remove(0);
                imports.add(3, oneImport);
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testMoveFirst_ImportFormatTest.pass");
    }
    
    public void testMoveLast() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                ImportTree oneImport = imports.remove(7);
                imports.add(1, oneImport);
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testMoveLast_ImportFormatTest.pass");
    }
    
    public void testReplaceLine() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                ImportTree oneImport = imports.remove(4);
                imports.add(4, make.Import(make.Identifier("java.util.Collection"), false));
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testReplaceLine_ImportFormatTest.pass");
    }

    public void testSort() throws IOException, FileStateInvalidException {
        JavaSource src = getJavaSource(testFile);
        
        Task<WorkingCopy> task = new Task<WorkingCopy>() {

            public void run(WorkingCopy workingCopy) throws IOException {
                workingCopy.toPhase(Phase.RESOLVED);
                CompilationUnitTree cut = workingCopy.getCompilationUnit();
                TreeMaker make = workingCopy.getTreeMaker();
                List<ImportTree> imports = new ArrayList<ImportTree>(cut.getImports());
                ImportTree oneImport = imports.remove(4);
                imports.add(4, make.Import(make.Identifier("java.util.Collection"), false));
                imports.sort(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        if (o1 == o2) {
                            return 0;
                        }
                        ImportTree i1 = (ImportTree) o1;
                        ImportTree i2 = (ImportTree) o2;

                        return i1.toString().compareTo(i2.toString());
                    }

                    @Override
                    public boolean equals(Object obj) {
                        return this == obj ? true : false;
                    }

                    @Override
                    public int hashCode() {
                        int hash = 7;
                        return hash;
                    }
                });
                CompilationUnitTree unit = make.CompilationUnit(
                        cut.getPackageName(),
                        imports,
                        cut.getTypeDecls(),
                        cut.getSourceFile()
                );
                workingCopy.rewrite(cut, unit);
            }
        };
        src.runModificationTask(task).commit();
        String res = TestUtilities.copyFileToString(testFile);
        //System.err.println(res);
        assertFiles("testSort_ImportFormatTest.pass");
    }
    
    String getGoldenPckg() {
        return "org/netbeans/jmi/javamodel/codegen/indent/ImportFormatTest/";
    }

    String getSourcePckg() {
        return "org/netbeans/test/codegen/indent/imports/";
    }
    
}
