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
package org.black.kotlin.utils;

import javax.swing.ImageIcon;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.FunctionDescriptor;
import org.jetbrains.kotlin.descriptors.VariableDescriptor;
import org.jetbrains.kotlin.descriptors.PackageFragmentDescriptor;
import org.jetbrains.kotlin.descriptors.PackageViewDescriptor;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Александр
 */
public class KotlinImageProvider {

    public static final KotlinImageProvider INSTANCE = new KotlinImageProvider();
    
    private final String imagesLocation = "org/black/kotlin/completionIcons/";
    
    private KotlinImageProvider(){}
    
    public ImageIcon getImage(DeclarationDescriptor descriptor){
        if (descriptor instanceof ClassDescriptor){
            ClassDescriptor classDescriptor = (ClassDescriptor) descriptor;
            switch (classDescriptor.getKind()){
                case ANNOTATION_CLASS:
                    return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "annotation.png"));
                case ENUM_CLASS:
                    return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "enum.png"));
                case INTERFACE:
                    return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "interface.png"));
                case CLASS:
                default:
                    return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "class.png"));
            }
        } else if (descriptor instanceof FunctionDescriptor){
            return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "method.png"));
        } else if (descriptor instanceof VariableDescriptor){
            return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "field.png"));
        } else if (descriptor instanceof PackageFragmentDescriptor || 
                descriptor instanceof PackageViewDescriptor){
            return new ImageIcon(ImageUtilities.loadImage(imagesLocation + 
                            "package.png"));
        } else
            return null;
    } 
    
}
