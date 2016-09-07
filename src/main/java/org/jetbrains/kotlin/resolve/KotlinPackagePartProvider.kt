package org.jetbrains.kotlin.resolve

import org.netbeans.api.project.Project
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.descriptors.PackagePartProvider
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.jetbrains.kotlin.cli.jvm.compiler.JavaRoot

class KotlinPackagePartProvider(val project: Project) : PackagePartProvider {
    val roots = KotlinEnvironment.getEnvironment(project)
            .getRoots()
            .map { it.file }
            .filter { it.findChild("META-INF") != null }

    override fun findPackageParts(packageFqName: String): List<String> {
        val pathParts = packageFqName.split('.')
        val mappings = roots.filter {
            //filter all roots by package path existing
            pathParts.fold(it) {
                parent, part ->
                if (part.isEmpty()) parent
                else parent.findChild(part) ?: return@filter false
            }
            true
        }.mapNotNull {
            it.findChild("META-INF")
        }.flatMap {
            it.children.filter { it.name.endsWith(ModuleMapping.MAPPING_FILE_EXT) }.toList()
        }.map {
            ModuleMapping.create(it.contentsToByteArray())
        }
        
        return mappings.map { it.findPackageParts(packageFqName) }
                .filterNotNull()
                .flatMap { it.parts }.distinct()
    }
}