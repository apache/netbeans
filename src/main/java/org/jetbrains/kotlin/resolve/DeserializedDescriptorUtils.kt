package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedCallableMemberDescriptor
import org.jetbrains.kotlin.serialization.deserialization.descriptors.DeserializedClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.parentsWithSelf
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.descriptors.ClassDescriptor

fun isDeserialized(descriptor: DeclarationDescriptor) =
	descriptor is DeserializedCallableMemberDescriptor || descriptor is DeserializedClassDescriptor

fun getContainingClassOrPackage(descriptor: DeclarationDescriptor) =
	descriptor.parentsWithSelf.firstOrNull() {
		(it is ClassDescriptor && DescriptorUtils.isTopLevelDeclaration(it)) ||
		it is PackageFragmentDescriptor
}