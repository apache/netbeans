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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.persistence.action.*;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import java.io.File;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.modules.j2ee.metadata.model.support.TestUtilities;
import org.openide.filesystems.FileUtil;

/**
 * Tests for the helper methods in EntityManagerGenerationStrategy.
 *
 * @author Erno Mononen
 */
public class EntityManagerGenerationStrategySupportTest extends EntityManagerGenerationTestSupport{
    
    public EntityManagerGenerationStrategySupportTest(String testName) {
        super(testName);
    }
    
    public void testGetAnnotationOnClass() throws Exception{
        
        final String annotation = "java.lang.Deprecated"; //some annotation
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "@" + annotation + "\n" +
                "public class Test {\n" +
                "}"
                );
        
        assertAnnotation(testFile, annotation, true);
    }
    
    public void testGetAnnotationOnField() throws Exception{
        
        final String annotation = "java.lang.Deprecated"; //some annotation
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "@" + annotation + "\n" +
                "Object myField;\n" +
                "}"
                );
        
        assertAnnotation(testFile, annotation, true);
        
    }
    
    public void testGetAnnotationOnMethod() throws Exception{
        
        final String annotation = "java.lang.Deprecated"; //some annotation
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "@" + annotation + "\n" +
                "Object method(){\n" +
                "return null;\n" +
                "}\n" +
                "}"
                );
        
        assertAnnotation(testFile, annotation, true);
        
    }
    
    
    public void testGetField() throws Exception{
        
        final String field = "java.lang.String"; //some field
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "private " + field + " myField;\n" +
                "}"
                );
        
        assertField(testFile, field, true);
        // test for searching a field that does not exist
        assertField(testFile, "java.lang.Object", false);
        
    }
    
    public void testGetEntityManagerName() throws Exception{
        final String field = "javax.persistence.EntityManager";
        final String fieldName = "myEntityManager";
        
        File testFile = new File(getWorkDir(), "Test.java");
        
        TestUtilities.copyStringToFile(testFile,
                "package org.netbeans.test;\n\n" +
                "import java.util.*;\n\n" +
                "public class Test {\n" +
                "private " + field + " " + fieldName + ";\n" +
                "}"
                );
        
        assertField(testFile, field, fieldName, true);
        
    }
    
    private void assertAnnotation(File testFile, final String annotation, final boolean expectSuccess) throws Exception {
        JavaSource targetSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task task = new TaskSupport() {
            void doAsserts(EntityManagerGenerationStrategySupport strategy) {
                Element result = strategy.getAnnotation(annotation);
                if (expectSuccess){
                    assertNotNull(result);
                    assertTrue(((TypeElement)result).getQualifiedName().contentEquals(annotation));
                } else {
                    assertNull(result);
                }
            }
        };
        
        targetSource.runModificationTask(task);
    }
    
    private void assertField(File testFile, final String field, final boolean expectSuccess) throws Exception {
        assertField(testFile, field, null, expectSuccess);
    }
    
    /**
     * Asserts that a field specified by the given parameters exists / doesn't exist in the given file.
     * @param testFile the 
     * @param field the FQN of the field 
     * @param fieldName the name of the field. May be null in which the name isn't checked.
     * @param expectSuccess specifies whether the field with the given parameters is expected to exist
     * in the given file.
     */ 
    private void assertField(File testFile, final String field, final String fieldName, final boolean expectSuccess) throws Exception {
        JavaSource targetSource = JavaSource.forFileObject(FileUtil.toFileObject(testFile));
        
        Task task = new TaskSupport() {
            void doAsserts(EntityManagerGenerationStrategySupport strategy) {
                VariableTree result = strategy.getField(field);
                if (expectSuccess){
                    assertNotNull(result);
                    TreePath path = strategy.getWorkingCopy().getTrees().getPath(strategy.getWorkingCopy().getCompilationUnit(), result);
                    TypeMirror variableType = strategy.getWorkingCopy().getTrees().getTypeMirror(path);
                    assertEquals(field, variableType.toString());
                    // check that field name matches if was provided
                    if (fieldName != null){
                        assertEquals(fieldName, result.getName().toString());
                    }
                } else {
                    assertNull(result);
                }
            }
        };
        
        targetSource.runModificationTask(task);
    }
    
    
    // a helper class for avoiding some duplicate code
    private abstract class TaskSupport implements Task<WorkingCopy> {
        
        public void run(WorkingCopy workingCopy) throws Exception {
            
            workingCopy.toPhase(Phase.RESOLVED);
            CompilationUnitTree cut = workingCopy.getCompilationUnit();
            TreeMaker make = workingCopy.getTreeMaker();
            
            for (Tree typeDeclaration : cut.getTypeDecls()){
                if (TreeUtilities.CLASS_TREE_KINDS.contains(typeDeclaration.getKind())){
                    ClassTree clazz = (ClassTree) typeDeclaration;
                    EntityManagerGenerationStrategySupport strategy =
                            (EntityManagerGenerationStrategySupport) getStrategy(workingCopy, make, clazz, new GenerationOptions());
                    doAsserts(strategy);
                } else {
                    fail("No class found"); // should not happen
                }
            }
        }
        
        abstract void doAsserts(EntityManagerGenerationStrategySupport strategy);
        
    }
    
    public static class StubEntityManagerGenerationStrategy extends EntityManagerGenerationStrategySupport{
        
        public ClassTree generate() {
            return null;
        }
        
    }

    protected Class<? extends EntityManagerGenerationStrategy> getStrategyClass() {
        return StubEntityManagerGenerationStrategy.class;
    }
}
