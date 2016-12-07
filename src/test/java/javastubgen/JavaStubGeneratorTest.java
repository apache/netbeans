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
 *******************************************************************************/
package javastubgen;

import com.intellij.openapi.util.text.StringUtil;
import java.io.IOException;
import java.util.List;
import javaproject.JavaProject;
import kotlin.Pair;
import org.jetbrains.kotlin.builder.KotlinPsiManager;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.filesystem.JavaStubGenerator;
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider;
import org.jetbrains.kotlin.resolve.KotlinAnalyzer;
import org.jetbrains.org.objectweb.asm.tree.ClassNode;
import org.junit.Test;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class JavaStubGeneratorTest extends NbTestCase {
    
    private final Project project;
    private final FileObject stubGenDir;
    
    public JavaStubGeneratorTest() {
        super("Stub generator test");
        project = JavaProject.INSTANCE.getJavaProject();
        stubGenDir = project.getProjectDirectory().
                getFileObject("src").getFileObject("stubGen");
    }
    
    @Test
    public void testProjectCreation() {
        assertNotNull(project);
        assertNotNull(stubGenDir);
    }
    
    private List<byte[]> getByteCode(FileObject file) throws IOException {
        KtFile ktFile = KotlinPsiManager.INSTANCE.getParsedFile(file);
        AnalysisResultWithProvider result
                    = KotlinAnalyzer.INSTANCE.analyzeFile(project, ktFile);
        KotlinParser.setAnalysisResult(ktFile, result);
        
        return KotlinLightClassGeneration.INSTANCE.getByteCode(file, project, result.getAnalysisResult());
    }
    
    private void doTest(String fileName, String... after) {
        FileObject kotlinFile = stubGenDir.getFileObject(fileName + ".kt");
        try {
            List<Pair<ClassNode, String>> list = JavaStubGenerator.INSTANCE.gen(getByteCode(kotlinFile));
            
            if (after.length == 0) {
                String expected = stubGenDir.getFileObject(fileName + ".after").asText();
                assertEquals(StringUtil.convertLineSeparators(expected).replaceAll("\\s+",""), 
                        StringUtil.convertLineSeparators(list.get(0).getSecond()).replaceAll("\\s+",""));
            } else {
                for (int i = 0; i < after.length; i++) {
                    String expected = stubGenDir.getFileObject(after[i] + ".after").asText();
                    assertEquals(StringUtil.convertLineSeparators(expected).replaceAll("\\s+",""), 
                            StringUtil.convertLineSeparators(list.get(i).getSecond()).replaceAll("\\s+",""));
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    @Test
    public void testSimple() {
        doTest("simple");
    }
    
    @Test
    public void testInterface() {
        doTest("interface");
    }
    
    @Test
    public void testAbstractClass() {
        doTest("abstractClass");
    }
    
    @Test
    public void testOpenClass() {
        doTest("openClass");
    }
 
    @Test
    public void testEnum() {
        doTest("enum");
    }
    
    @Test
    public void testClassWithTypeParameter() {
        doTest("classWithTypeParameter");
    }
    
    @Test
    public void testWithoutClass() {
        doTest("withoutClass");
    }
    
    @Test
    public void testObject() {
        doTest("object");
    }
    
    @Test
    public void testClassWithVal() {
        doTest("classWithVal");
    }
    
    @Test
    public void testClassWithVar() {
        doTest("classWithVar");
    }
    
    @Test
    public void testSeveralClassesInOneFile() {
        doTest("severalClassesInOneFile", "FirstClass", "SecondClass");
    }
    
    @Test
    public void testWithNestedClass() {
        doTest("withNestedClass");
    }
    
}
