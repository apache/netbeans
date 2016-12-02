/** *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************** */
package j2k;

import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.j2k.Java2KotlinConverter;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import utils.TestUtilsKt;

public class J2KTest extends NbTestCase {
    
    private final Project project;
    private final FileObject j2kDir;
    
    public J2KTest() {
        super("Converter test");
        project = JavaProject.INSTANCE.getJavaProject();
        j2kDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("j2k");
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(j2kDir);
    }
    
    private void doTest(String fileName) {
        FileObject javaFile = j2kDir.getFileObject(fileName + ".java");
        Document doc = TestUtilsKt.getDocumentForFileObject(j2kDir, fileName + ".java");
        Java2KotlinConverter.convert(doc, project, javaFile);
        
        try {
            Document kotlinDoc = TestUtilsKt.getDocumentForFileObject(j2kDir, fileName + ".kt");
            Document afterDoc = TestUtilsKt.getDocumentForFileObject(j2kDir, fileName + ".after");
            
            String kotlinText = kotlinDoc.getText(0, kotlinDoc.getLength());
            String afterText = afterDoc.getText(0, afterDoc.getLength());
            
            assertEquals(afterText, kotlinText);
        } catch (BadLocationException ex) {
            assertTrue(false);
        }
    }
    
    @Test
    public void testSimpleCase() {
        doTest("simple");
    }
    
    @Test
    public void testWithStaticMethods() {
        doTest("withStaticMethod");
    }
    
    @Test
    public void testMixed() {
        doTest("mixed");
    }
    
    @Test
    public void testWithInnerClass() {
        doTest("withInnerClass");
    }
    
    @Test
    public void testInterface() {
        doTest("interface");
    }
    
}
