package org.jetbrains.kotlin.navigation

import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryPackageSourceElement
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.netbeans.api.project.Project
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.load.kotlin.KotlinJvmBinaryClass
import org.jetbrains.kotlin.load.kotlin.header.KotlinClassHeader
import org.jetbrains.kotlin.load.kotlin.VirtualFileKotlinClass
import org.jetbrains.kotlin.model.KotlinEnvironment
import com.intellij.openapi.vfs.VirtualFile

fun gotoClassByPackageSourceElement(
        sourceElement: KotlinJvmBinaryPackageSourceElement, 
        descriptor: DeclarationDescriptor, 
        project: Project) {
    if (descriptor !is DeserializedCallableMemberDescriptor) return 
    
    val binaryClass = sourceElement.getContainingBinaryClass(descriptor)
    if (binaryClass == null) {
        return
    }
    findImplementingClass(binaryClass,descriptor,project)
    NavigationUtil.gotoElementInBinaryClass(binaryClass, descriptor, project)
}

private fun findImplementingClass(
        binaryClass : KotlinJvmBinaryClass,
        descriptor : DeclarationDescriptor,
        project : Project) {
      if (KotlinClassHeader.Kind.MULTIFILE_CLASS 
              == binaryClass.classHeader.kind) {
          getImplementingFacadePart(binaryClass,descriptor,project)
      } else {
          val virtFile = getClassFile(binaryClass, project)
          NavigationUtil.gotoKotlinStdlib(virtFile, descriptor);
      }
  }

fun getImplementingFacadePart(
        binaryClass : KotlinJvmBinaryClass,
        descriptor : DeclarationDescriptor,
        project : Project) {
    if (descriptor !is DeserializedCallableMemberDescriptor) return
    
    val className = getImplClassName(descriptor)
    if (className == null) {
        return
    }
    
}

fun getClassFile(binaryClass : KotlinJvmBinaryClass, project : Project) : VirtualFile {
    val file = (binaryClass as VirtualFileKotlinClass).file
    val path = file.path
    return KotlinEnvironment.getEnvironment(project).getVirtualFileInJar(path)
}

fun getImplClassName(memberDescriptor: DeserializedCallableMemberDescriptor): Name? {
    val nameIndex: Int
    
    try
    {
        val getProtoMethod = DeserializedCallableMemberDescriptor::class.java.getMethod("getProto")
        val proto = getProtoMethod!!.invoke(memberDescriptor)
        val implClassNameField = Class.forName("org.jetbrains.kotlin.serialization.jvm.JvmProtoBuf").getField("implClassName")
        val implClassName = implClassNameField!!.get(null)
        val protobufCallable = Class.forName("org.jetbrains.kotlin.serialization.ProtoBuf\$Callable")
        val getExtensionMethod = protobufCallable!!.getMethod("getExtension", implClassName!!.javaClass)
        val indexObj = getExtensionMethod!!.invoke(proto, implClassName)
    
        if (indexObj !is Int) return null
    
        nameIndex = indexObj.toInt()
    } catch (e: ReflectiveOperationException) {
        return null
    } catch (e: IllegalArgumentException) {
        return null
    } catch (e: SecurityException) {
        return null
    }
    
    return memberDescriptor.nameResolver.getName(nameIndex)
}