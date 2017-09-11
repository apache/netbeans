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

package org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator;

import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.j2ee.persistence.action.*;
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategySupport;
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
import org.netbeans.modules.j2ee.persistence.spi.entitymanagergenerator.EntityManagerGenerationStrategy;
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
