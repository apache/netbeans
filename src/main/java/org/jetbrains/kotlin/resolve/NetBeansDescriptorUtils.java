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
package org.jetbrains.kotlin.resolve;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor;
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor.Kind;
import org.jetbrains.kotlin.descriptors.ClassDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithSource;
import org.jetbrains.kotlin.descriptors.SourceElement;
import org.jetbrains.kotlin.resolve.DescriptorToSourceUtils;

import com.google.common.collect.Lists;
import org.netbeans.api.project.Project;

// Note: copied with some changes from DescriptorToSourceUtils
public class NetBeansDescriptorUtils {
    // NOTE this is also used by KDoc
    @Nullable
    public static SourceElement descriptorToDeclaration(@NotNull DeclarationDescriptor descriptor) {
        if (descriptor instanceof CallableMemberDescriptor) {
            return callableDescriptorToDeclaration((CallableMemberDescriptor) descriptor);
        } else if (descriptor instanceof ClassDescriptor) {
            return classDescriptorToDeclaration((ClassDescriptor) descriptor);
        } else {
            return doGetDescriptorToDeclaration(descriptor);
        }
    }

    @NotNull
    public static List<SourceElement> descriptorToDeclarations(@NotNull DeclarationDescriptor descriptor,
            @NotNull Project project) {
        if (BuiltInsReferenceResolver.isFromBuiltinModule(descriptor)) {
            
            Collection<DeclarationDescriptor> effectiveReferencedDescriptors = DescriptorToSourceUtils.getEffectiveReferencedDescriptors(descriptor);
            
            HashSet<SourceElement> result = new HashSet<SourceElement>();
            BuiltInsReferenceResolver resolver = BuiltInsReferenceResolver.getInstance(project);
            for (DeclarationDescriptor effectiveReferenced: effectiveReferencedDescriptors) {
                DeclarationDescriptor resultDescriptor = resolver.findCurrentDescriptor(effectiveReferenced);
                if (resultDescriptor != null && resultDescriptor instanceof DeclarationDescriptorWithSource) {
                    SourceElement element = ((DeclarationDescriptorWithSource)resultDescriptor).getSource();
                    if (!element.equals(SourceElement.NO_SOURCE)) {
                        result.add(element);
                    }
                }
            }
            return new ArrayList<SourceElement>(result);
        }
        //TODO: this returns a source element for containing element with existing .class file. This logic should be moved to a caller.
        if (DeserializedDescriptorUtils.isDeserialized(descriptor)) {
            DeclarationDescriptor containing = DeserializedDescriptorUtils.getContainingClassOrPackage(descriptor);
            if (containing != null) {
                return Lists.newArrayList(descriptorToDeclaration(containing));
            }
        }
        if (descriptor instanceof CallableMemberDescriptor) {
            return callableDescriptorToDeclarations((CallableMemberDescriptor) descriptor);
        } else {
            SourceElement sourceElement = descriptorToDeclaration(descriptor);
            if (sourceElement != null) {
                return Lists.newArrayList(sourceElement);
            } else {
                return Lists.newArrayList();
            }
        }
    }
    
    @Nullable
    public static SourceElement callableDescriptorToDeclaration(@NotNull CallableMemberDescriptor callable) {
        if (callable.getKind() == Kind.DECLARATION || callable.getKind() == Kind.SYNTHESIZED) {
            return doGetDescriptorToDeclaration(callable);
        }
        //TODO: should not use this method for fake_override and delegation
        Collection<? extends CallableMemberDescriptor> overriddenDescriptors = callable.getOverriddenDescriptors();
        if (overriddenDescriptors.size() == 1) {
            return callableDescriptorToDeclaration(overriddenDescriptors.iterator().next());
        }
        return null;
    }

    @NotNull
    public static List<SourceElement> callableDescriptorToDeclarations(@NotNull CallableMemberDescriptor callable) {
        if (callable.getKind() == Kind.DECLARATION || callable.getKind() == Kind.SYNTHESIZED) {
            SourceElement sourceElement = doGetDescriptorToDeclaration(callable);
            return sourceElement != null ? Lists.newArrayList(sourceElement) : Lists.<SourceElement>newArrayList();
        }

        List<SourceElement> r = Lists.newArrayList();
        Collection<? extends CallableMemberDescriptor> overriddenDescriptors = callable.getOverriddenDescriptors();
        for (CallableMemberDescriptor overridden : overriddenDescriptors) {
            r.addAll(callableDescriptorToDeclarations(overridden));
        }
        return r;
    }
    
    @Nullable
    public static SourceElement classDescriptorToDeclaration(@NotNull ClassDescriptor clazz) {
        return doGetDescriptorToDeclaration(clazz);
    }
    
    @Nullable
    private static SourceElement doGetDescriptorToDeclaration(@NotNull DeclarationDescriptor descriptor) {
        DeclarationDescriptor original = descriptor.getOriginal();
        if (!(original instanceof DeclarationDescriptorWithSource)) {
            return null;
        }
        return ((DeclarationDescriptorWithSource) original).getSource();
    }
}