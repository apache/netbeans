/**
 * *****************************************************************************
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
 ******************************************************************************
 */
package indentation;

import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import static junit.framework.TestCase.assertNotNull;
import org.jetbrains.kotlin.formatting.KotlinIndentStrategy;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import utils.TestUtils;

/**
 *
 * @author Alexander.Baratynski
 */
public class IndentationTest extends NbTestCase {
    
    private final Project project;
    private final FileObject indentationDir;
    
    public IndentationTest() {
        super("Indentation test");
        project = JavaProject.INSTANCE.getJavaProject();
        indentationDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("indentation");
    }
    
    private void doTest(String fileName) {
        try {
            StyledDocument doc = (StyledDocument) TestUtils.getDocumentForFileObject(indentationDir, fileName);
            int offset = TestUtils.getCaret(doc) + 1;
            doc.insertString(offset - 1, "\n", null);
            
            KotlinIndentStrategy strategy = new KotlinIndentStrategy(doc, offset);
            int newOffset = strategy.addIndent();
            
            StyledDocument doc2 = (StyledDocument) TestUtils.getDocumentForFileObject(indentationDir, fileName.replace(".kt", ".after"));
            int expectedOffset = TestUtils.getCaret(doc2);
            
            assertEquals(expectedOffset, newOffset);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(indentationDir);
    }
    
    @Test
    public void testAfterOneOpenBrace() {
        doTest("afterOneOpenBrace.kt");
    }
    
    @Test
    public void testBeforeFunctionStart() {
        doTest("beforeFunctionStart.kt");
    }
}
