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
package j2k

import javaproject.JavaProject
import javax.swing.text.Document
import org.jetbrains.kotlin.j2k.Java2KotlinConverter
import org.netbeans.api.project.Project
import org.openide.filesystems.FileObject
import utils.*

class J2KTest : KotlinTestCase("Converter test", "j2k") {

    private fun doTest(fileName: String) {
        val javaFile = dir.getFileObject("$fileName.java")
        val doc = getDocumentForFileObject(dir, "$fileName.java")
        Java2KotlinConverter.convert(doc, project, javaFile)
        
        val kotlinDoc = getDocumentForFileObject(dir, "$fileName.kt")
        val afterDoc = getDocumentForFileObject(dir, "$fileName.after")
        val kotlinText = kotlinDoc.getText(0, kotlinDoc.length)
        val afterText = afterDoc.getText(0, afterDoc.length)
        assertEquals(afterText, kotlinText)
    }

    fun testSimpleCase() = doTest("simple")

    fun testWithStaticMethods() = doTest("withStaticMethod")

    fun testMixed() = doTest("mixed")

    fun testWithInnerClass() = doTest("withInnerClass")

    fun testInterface() = doTest("interface")

}