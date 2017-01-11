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
package org.jetbrains.kotlin.projectsextensions.gradle.classpath

import org.jetbrains.kotlin.log.KotlinLogger
import org.jetbrains.kotlin.projectsextensions.ClassPathExtender
import org.netbeans.api.java.classpath.ClassPath
import org.netbeans.api.project.Project
import org.netbeans.spi.java.classpath.ClassPathProvider

/**
 *
 * @author baratynskiy
 */
class GradleExtendedClassPath(val project: Project) : ClassPathExtender {
    
    private val classPathProvider: ClassPathProvider?

    init {
        classPathProvider = project.lookup.lookup(ClassPathProvider::class.java)
    }

    override fun getProjectSourcesClassPath(type: String) = if (classPathProvider == null) ClassPath.EMPTY else getClassPath(type)
    
    private fun getClassPath(type: String): ClassPath {
        try {
            val method = classPathProvider?.javaClass?.getMethod("getClassPaths", String::class.java) ?: return ClassPath.EMPTY
            
            return method.invoke(classPathProvider, type) as ClassPath
        } catch (ex: ReflectiveOperationException) {
            KotlinLogger.INSTANCE.logWarning(ex.message)
            
            return ClassPath.EMPTY
        }
    }
    
}