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
package javastubgen

import com.intellij.openapi.util.text.StringUtil
import javaproject.JavaProject
import org.jetbrains.kotlin.builder.KotlinPsiManager
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser
import org.jetbrains.kotlin.filesystem.JavaStubGenerator
import org.jetbrains.kotlin.filesystem.lightclasses.KotlinLightClassGeneration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.resolve.AnalysisResultWithProvider
import org.jetbrains.kotlin.resolve.KotlinAnalyzer
import org.jetbrains.org.objectweb.asm.tree.ClassNode
import org.netbeans.api.project.Project
import org.netbeans.junit.NbTestCase
import org.openide.filesystems.FileObject

class JavaStubGeneratorTest : NbTestCase("Stub generator test") {
    private val project: Project
    private val stubGenDir: FileObject

    init {
        project = JavaProject.javaProject
        stubGenDir = project.projectDirectory.getFileObject("src").getFileObject("stubGen")
    }

    fun testProjectCreation() {
        assertNotNull(project)
        assertNotNull(stubGenDir)
    }

    private fun getByteCode(file: FileObject): List<ByteArray> {
        val ktFile = KotlinPsiManager.getParsedFile(file)!!
        val result = KotlinAnalyzer.analyzeFile(project, ktFile)
        KotlinParser.setAnalysisResult(ktFile, result)
        
        return KotlinLightClassGeneration.getByteCode(file, project, result.analysisResult)
    }

    private fun doTest(fileName: String, vararg after: String) {
        val kotlinFile = stubGenDir.getFileObject("$fileName.kt")
            val list = JavaStubGenerator.gen(getByteCode(kotlinFile))
            val spacesRegex = Regex("\\s+")
            if (after.isEmpty()) {
                val expected = stubGenDir.getFileObject("$fileName.after").asText()
                assertEquals(spacesRegex.replace(StringUtil.convertLineSeparators(expected), ""),
                        spacesRegex.replace(StringUtil.convertLineSeparators(list.first().second), ""))
            } else after.forEachIndexed { i, it ->
                val expected = stubGenDir.getFileObject("${after[i]}.after").asText()
                assertEquals(spacesRegex.replace(StringUtil.convertLineSeparators(expected), ""),
                        spacesRegex.replace(StringUtil.convertLineSeparators(list[i].second), ""))
        }    
    }

    fun testSimple() = doTest("simple")

    fun testInterface() = doTest("interface")

    fun testAbstractClass() = doTest("abstractClass")

    fun testOpenClass() = doTest("openClass")

    fun testEnum() = doTest("enum")

    fun testClassWithTypeParameter() = doTest("classWithTypeParameter")

// fun testWithoutClass() = doTest("withoutClass")

    fun testObject() = doTest("object")

    fun testClassWithVal() = doTest("classWithVal")

    fun testClassWithVar() = doTest("classWithVar")

    fun testSeveralClassesInOneFile() = doTest("severalClassesInOneFile", "FirstClass", "SecondClass")

    fun testWithNestedClass() = doTest("withNestedClass")

    fun testWithCompanion() = doTest("withCompanion")
        
    fun testClassWithSeveralMethods() = doTest("withSeveralMethods")
    
    fun testClassImplementsInterface() = doTest("implementsInterface", "implementsInterface2", "implementsInterface1")
    
    fun testFunWithSeveralArguments() = doTest("severalArguments")
    
}