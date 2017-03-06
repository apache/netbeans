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
package org.jetbrains.kotlin.project

import java.beans.PropertyChangeListener
import javax.swing.ImageIcon
import javax.swing.event.ChangeListener
import org.jetbrains.kotlin.builder.isKotlinFile
import org.jetbrains.kotlin.projectsextensions.maven.MavenHelper
import org.netbeans.api.project.Project
import org.netbeans.api.project.SourceGroup
import org.openide.filesystems.FileObject

class KotlinSources(private val kotlinProject: Project) {

    private fun findSrc(fo: FileObject,
                        files: MutableCollection<FileObject>,
                        type: KotlinProjectConstants?,
                        test: Boolean) {
        if (fo.isFolder()) {
            if (!test && fo.name == "test" && fo.parent.name == "src") return
            if (fo.name == "resources" &&
                    (fo.parent.name == "test" || fo.parent.name == "main")) return

            
            fo.children.forEach { findSrc(it, files, type, test) }
        } else when (type) {
            KotlinProjectConstants.KOTLIN_SOURCE -> if (fo.isKotlinFile()) files.add(fo.parent)

            KotlinProjectConstants.JAVA_SOURCE -> if (fo.hasExt("java")) files.add(fo.parent)

            KotlinProjectConstants.JAR -> if (fo.hasExt("jar")) {
                if (fo.parent.name != "build") files.add(fo.parent)
            }
        }
    }

    fun isMavenModuledProject(): Boolean {
        if (kotlinProject::class.java.name != "org.netbeans.modules.maven.NbMavenProjectImpl") return false

        val originalProject = MavenHelper.getOriginalMavenProject(kotlinProject) ?: return false
        val modules = originalProject.modules ?: return false

        return modules.isNotEmpty()
    }

    fun getSrcDirectories(type: KotlinProjectConstants,
                          test: Boolean = true): List<FileObject> {
        val orderedFiles = hashSetOf<FileObject>()
        val srcDir = kotlinProject.projectDirectory.getFileObject("src") ?: return emptyList()

        findSrc(srcDir, orderedFiles, type, test)
        return orderedFiles.toList()
    }

    fun getAllKtFiles() = getSrcDirectories(KotlinProjectConstants.KOTLIN_SOURCE)
            .flatMap { it.children.toList() }
            .filter { it.hasExt("kt") }


    fun getSourceGroups(type: KotlinProjectConstants,
                        test: Boolean = true) = getSrcDirectories(type, test)
            .map { KotlinSourceGroup(it) }
            .toTypedArray()

    fun getSourceGroupForFileObject(fo: FileObject) = KotlinSourceGroup(fo)

}

class KotlinSourceGroup(private val root: FileObject) : SourceGroup {

    override fun getRootFolder() = root

    override fun getName() = root.path

    override fun getDisplayName() = rootFolder.name

    override fun getIcon(bool: Boolean) = ImageIcon("org/jetbrains/kotlin.png")

    override fun contains(fo: FileObject) = fo.toURI().toString().startsWith(rootFolder.toURI().toString())

    override fun addPropertyChangeListener(pl: PropertyChangeListener?) {}

    override fun removePropertyChangeListener(pl: PropertyChangeListener?) {}
}