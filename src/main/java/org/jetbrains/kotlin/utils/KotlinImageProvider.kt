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
package org.jetbrains.kotlin.utils

import javax.swing.ImageIcon
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor
import org.openide.util.ImageUtilities

object KotlinImageProvider {

    private val imagesLocation = "org/jetbrains/kotlin/completionIcons/"

    val typeImage = ImageIcon(ImageUtilities.loadImage("${imagesLocation}class.png"))

    private fun getImageIcon(name: String) = ImageIcon(ImageUtilities.loadImage("$imagesLocation$name"))

    fun getImage(descriptor: DeclarationDescriptor?) = when {
        descriptor is ClassDescriptor -> {
            when (descriptor.kind) {
                ClassKind.ANNOTATION_CLASS -> getImageIcon("annotation.png")
                ClassKind.ENUM_CLASS -> getImageIcon("enum.png")
                ClassKind.INTERFACE -> getImageIcon("interface.png")
                else -> getImageIcon("class.png")
            }
        }
        descriptor is FunctionDescriptor -> getImageIcon("method.png")
        descriptor is VariableDescriptor -> getImageIcon("field.png")
        descriptor is PackageFragmentDescriptor ||
                descriptor is PackageViewDescriptor -> getImageIcon("package.png")
        else -> null
    }
}