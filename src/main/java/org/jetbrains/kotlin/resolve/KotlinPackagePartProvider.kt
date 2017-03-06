package org.jetbrains.kotlin.resolve

import com.intellij.openapi.vfs.VirtualFile
import java.io.EOFException
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.model.KotlinEnvironment
import org.jetbrains.kotlin.descriptors.PackagePartProvider
import org.jetbrains.kotlin.load.kotlin.ModuleMapping
import org.jetbrains.kotlin.load.kotlin.PackageParts
import org.jetbrains.kotlin.utils.SmartList

class KotlinPackagePartProvider(val project: Project) : PackagePartProvider {
    private data class ModuleMappingInfo(val root: VirtualFile, val mapping: ModuleMapping)
    
    private val notLoadedRoots by lazy(LazyThreadSafetyMode.NONE) {
        KotlinEnvironment.getEnvironment(project).roots
                .map { it.file }
                .filter { it.findChild("META-INF") != null }
                .toMutableList()
    }
    
    private val loadedModules: MutableList<ModuleMappingInfo> = SmartList()
    
    override fun findPackageParts(packageFqName: String): List<String> {
        val rootToPackageParts = getPackageParts(packageFqName)
        if (rootToPackageParts.isEmpty()) return emptyList()
        
        val result = linkedSetOf<String>()
        val visitedMultifileFacades = linkedSetOf<String>()
        for ((_, packageParts) in rootToPackageParts) {
            for (name in packageParts.parts) {
                val facadeName = packageParts.getMultifileFacadeName(name)
                if (facadeName == null || facadeName !in visitedMultifileFacades) {
                    result.add(name)
                }
            }
            packageParts.parts.mapNotNullTo(visitedMultifileFacades) { packageParts.getMultifileFacadeName(it) }
        }
        
        return result.toList()
    }
    
    override fun findMetadataPackageParts(packageFqName: String) = getPackageParts(packageFqName).values
            .flatMap(PackageParts::metadataParts)
            .distinct()
    
    @Synchronized private fun getPackageParts(packageFqName: String): Map<VirtualFile, PackageParts> {
        processNotLoadedRelevantRoots(packageFqName)
        
        val result = mutableMapOf<VirtualFile, PackageParts>()
        for ((root, mapping) in loadedModules) {
            val newParts = mapping.findPackageParts(packageFqName) ?: continue
            result[root]?.let{ parts -> parts += newParts } ?: result.put(root, newParts)
        }
        
        return result
    }
    
    private fun processNotLoadedRelevantRoots(packageFqName: String) {
        if (notLoadedRoots.isEmpty()) return
        
        val pathParts = packageFqName.split('.')
        val relevantRoots = notLoadedRoots.filter { 
            pathParts.fold(it) { parent, part ->
                if (part.isEmpty()) parent
                else parent.findChild(part) ?: return@filter false
            }
            true
        }
        notLoadedRoots.removeAll(relevantRoots)
        
        for (root in relevantRoots) {
            val metaInf = root.findChild("META-INF") ?: continue
            val moduleFiles = metaInf.children.filter { it.name.endsWith(ModuleMapping.MAPPING_FILE_EXT) }
            for (moduleFile in moduleFiles) {
                val mapping = try {
                    if (!moduleFile.exists()) continue
                    ModuleMapping.create(moduleFile.contentsToByteArray(), moduleFile.toString())
                }
                catch (e: EOFException) {
                    throw RuntimeException("Error on reading package parts for '$packageFqName' package in '$moduleFile', roots: $notLoadedRoots", e)
                }
                loadedModules.add(ModuleMappingInfo(root, mapping))
            }
        }
    }
    
}