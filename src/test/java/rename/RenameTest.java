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
package rename;

import com.intellij.psi.PsiElement;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javaproject.JavaProject;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.refactorings.rename.KotlinTransaction;
import org.jetbrains.kotlin.refactorings.rename.RenamePerformer;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.refactoring.spi.Transaction;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import utils.TestUtils;

public class RenameTest extends NbTestCase {
    
    private final Project project;
    private final FileObject renameDir;
    
    public RenameTest() {
        super("Rename Test");
        project = JavaProject.INSTANCE.getJavaProject();
        renameDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("rename");
    }
    
    private void doRefactoring(String newName, FileObject fo, PsiElement psi) {
        Map<FileObject, List<OffsetRange>> renameMap = RenamePerformer.getRenameRefactoringMap(fo, psi, newName);
        Transaction transaction = new KotlinTransaction(renameMap, newName, psi.getText());
        transaction.commit();
    }
  
    private void checkTextsEquality(FileObject actual, FileObject supposed) {
        String actualText = null;
        String supposedText = null;
        
        Document beforeDoc = TestUtils.getDocumentForFileObject(actual);
        Document afterDoc = TestUtils.getDocumentForFileObject(supposed);
        
        try {
            actualText = beforeDoc.getText(0, beforeDoc.getLength());
            supposedText = afterDoc.getText(0, afterDoc.getLength());
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
        }

        assert actualText != null;
        assert supposedText != null;
        
        assertEquals(supposedText, actualText);
    }
    
    private void doTest(String pack, String name, String newName) {
        FileObject packFile = renameDir.getFileObject(pack);
        FileObject before = packFile.getFileObject(name + ".kt");
        FileObject beforeWithCaret = packFile.getFileObject(name + ".caret");
        
        Integer caretOffset = TestUtils.getCaret(TestUtils.getDocumentForFileObject(beforeWithCaret));
        assertNotNull(caretOffset);
        PsiElement psi = null;
        try {
            KtFile ktFile = KotlinPsiManager.INSTANCE.getParsedFile(before);
            psi = ktFile.findElementAt(caretOffset);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        assert psi != null;
        
        doRefactoring(newName, before, psi);
        
        for (FileObject ktFile : TestUtils.getAllKtFilesInFolder(packFile)) {
            String fileName = ktFile.getName();
            FileObject afterFile = packFile.getFileObject(fileName+".after");
            assertNotNull(afterFile);
            
            checkTextsEquality(ktFile, afterFile);
        }
    }
    
    @Test
    public void testSimpleCase() {
        doTest("simple", "file", "NewName");
    }
    
    @Test
    public void testSecondSimpleCase() {
        doTest("properties", "file", "someValue");
    }
    
    @Test
    public void testThirdSimpleCase() {
        doTest("simplesec", "file", "someValue");
    }
    
    @Test
    public void testFunctionParameterRenaming() {
        doTest("functionparameter", "file", "someValue");
    }
    
    @Test
    public void testFunctionRenaming() {
        doTest("function", "file", "fooFunc");
    }
    
    @Test
    public void testClassRenaming() {
        doTest("classrename", "file", "NewName");
    }
    
    @Test
    public void testMethodRenaming() {
        doTest("methodrename", "file", "notSoCoolFun");
    }
    
    @Test
    public void testForLoop() {
        doTest("forloop", "file", "arg1");
    }
    
    @Test
    public void testForLoop2() {
        doTest("forloop2", "file", "arg1");
    }

    @Test
    public void testRenameKotlinClassByConstructorRef() {
        doTest("classbyconstructor", "file", "KotlinRules");
    }
    
}
