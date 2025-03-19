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
package org.netbeans.api.java.source;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import static org.netbeans.api.java.source.CommentCollectorTest.getJavaSource;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author homberghp
 */
public class RecordUtilsTest extends NbTestCase {

    public RecordUtilsTest(String s) {
        super(s);
    }
    private File work;

    @Override
    protected void setUp() throws Exception {
        File wd = getWorkDir();
        FileObject cache = FileUtil.createFolder(new File(wd, "cache"));
        IndexUtil.setCacheFolder(FileUtil.toFile(cache));
        work = new File(wd, "work");
        work.mkdirs();

        super.setUp();
    }

    @Test
    public void testComponentCount() throws Exception {

        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        assertEquals(3, RecordUtils.countComponents(records[0]));
    }

    /**
     * records[0] = classtree records[1] = methodtree ctor compact (hidden)
     * records[2] = methodtree ctor with 2 params records[3] = variabletree
     * static records[4] = methodtree method m
     */
    String recordCode
            = """
            package test;
            public record Test<G>(int age, String name, G... grade) implements Serializable {
               public Test(String name, G... grade){
                  this(1, name, grade);
               }
               public Test{
                    assert age > 0;
                    assert !name.isBlank();
                }

                public static String me = "puk";
                public int m(){
                    return age+1;
                }
            }
            """;

    static JCClassDecl[] prepareJCClassTree(File testFile, final String recordCode) throws Exception, IOException {
        TestUtilities.copyStringToFile(testFile, recordCode);
        JavaSource src = getJavaSource(testFile);
        final JCClassDecl[] records = new JCClassDecl[1];
        Task<WorkingCopy> task = new Task<WorkingCopy>() {
            public void run(final WorkingCopy workingCopy) throws Exception {
                workingCopy.toPhase(JavaSource.Phase.PARSED);
                records[0] = (JCClassDecl) workingCopy.getCompilationUnit().getTypeDecls().get(0);
            }
        };
        src.runModificationTask(task);
        return records;
    }

    @Test
    public void testCanonicalParameters() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        List<JCTree.JCVariableDecl> canonicalParameters = RecordUtils.canonicalParameters(records[0]);
        assertEquals(3, canonicalParameters.size());
    }

    @Test
    public void testComponentNames() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        List<String> componentNames = RecordUtils.componentNames(records[0]);
        List<String> expected = List.of("age", "name", "grade");
        assertEquals(expected, componentNames);
    }

    @Test
    public void testGetComponents() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);

        List<Tree> recordComponents = RecordUtils.components(records[0]);
        assertEquals(3, recordComponents.size());
    }

    @Test
    public void testHassAllParams() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        List<JCTree> members = records[0].getMembers();
        JCTree.JCMethodDecl cand = (JCTree.JCMethodDecl) members.get(4);
        Set<String> componentNames = new LinkedHashSet(RecordUtils.componentNames(records[0]));
        assertTrue(RecordUtils.hasAllParameterNames(cand, componentNames));
    }

    @Test
    public void testNonComponentMembers() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        List<Tree> members = RecordUtils.nonComponentMembers(records[0]);
        assertEquals("onon components ", 4, members.size());
    }

    @Test
    public void testIsCompact() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        List<Tree> members = RecordUtils.nonComponentMembers(records[0]);
        Tree rec = records[0];
        Tree candidate =  members.get(1);
        assertTrue(RecordUtils.isCompactConstructor(rec, candidate));
    }

    @Test
    public void testIsNotCompact() throws Exception {
        File testFile = new File(work, "Test.java");
        JCClassDecl[] records = prepareJCClassTree(testFile, recordCode);
        List<Tree> members = RecordUtils.nonComponentMembers(records[0]);
        Tree rec = records[0];
        Tree cand= members.get(2);
        System.err.println("cand = " + cand);
        assertFalse(RecordUtils.isCompactConstructor(rec, cand));
    }


}
