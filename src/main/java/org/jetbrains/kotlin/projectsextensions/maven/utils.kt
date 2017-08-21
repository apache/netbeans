/*******************************************************************************
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.kotlin.projectsextensions.maven

import org.jetbrains.kotlin.utils.ProjectUtils
import org.jetbrains.kotlin.utils.hasMain
import org.jetbrains.kotlin.utils.mainClassName
import org.netbeans.spi.project.ActionProvider
import org.openide.filesystems.FileObject
import org.openide.util.Lookup

fun convert(action: String, lookup: Lookup): String? {
    if (ActionProvider.COMMAND_RUN_SINGLE == action || ActionProvider.COMMAND_DEBUG_SINGLE == action) {
        val f = lookup.lookup(FileObject::class.java)

        if (f != null && "text/x-kt" == f.mimeType) {
            val ktFile = ProjectUtils.getKtFile(f)
            if (!ktFile.hasMain()) return null

            return "$action.main"
        }
    }

    return null;
}

fun createReplacements(action: String, lookup: Lookup): Map<String, String> {
    val f = lookup.lookup(FileObject::class.java)

    if (f != null && "text/x-kt" == f.mimeType) {
        val ktFile = ProjectUtils.getKtFile(f) ?: return emptyMap()
        val fqName = ktFile.mainClassName() ?: return emptyMap()
        
        return mapOf("packageClassName" to fqName)
    }

    return emptyMap();
}