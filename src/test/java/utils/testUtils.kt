/*******************************************************************************
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
package utils

import javax.swing.text.Document
import junit.framework.TestCase.assertNotNull
import org.jetbrains.kotlin.utils.ProjectUtils
import org.openide.filesystems.FileObject

fun getCaret(doc: Document): Int = doc.getText(0,doc.length).indexOf("<caret>")

fun getDocumentForFileObject(fo: FileObject) = ProjectUtils.getDocumentFromFileObject(fo)

fun getDocumentForFileObject(dir: FileObject, fileName: String): Document {
    val file = dir.getFileObject(fileName)
    
    assertNotNull(file)
    
    return getDocumentForFileObject(file)
}

fun getAllKtFilesInFolder(folder: FileObject) = folder.children.filter{ it.hasExt("kt") }