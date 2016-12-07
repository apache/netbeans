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
package org.jetbrains.kotlin.navigation

import java.io.File
import kotlin.Pair
import org.openide.filesystems.FileObject
import org.openide.filesystems.FileUtil

fun getFileObjectFromJar(path: String): FileObject? {
    val pathParts = getJarAndInternalPaths(path) ?: return null
    
    val jar = File(pathParts.first).absoluteFile
    
    var fileObject = FileUtil.toFileObject(jar) ?: return null
    fileObject = FileUtil.getArchiveRoot(fileObject) ?: return null
    
    val internalPathParts = pathParts.second.split("/")
    for (pathPart in internalPathParts) {
        fileObject = fileObject.getFileObject(pathPart) ?: return null
    }
    
    return fileObject
}

private fun getJarAndInternalPaths(path: String): Pair<String, String>? {
    val separator = "!/"
    val pathParts = path.split(separator)
    if (pathParts.size < 2) {
        return null
    }
    return Pair(pathParts[0], pathParts[1])
}
