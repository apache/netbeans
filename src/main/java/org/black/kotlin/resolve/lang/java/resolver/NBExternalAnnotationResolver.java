/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.black.kotlin.resolve.lang.java.resolver;

import java.util.Collection;
import java.util.Collections;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.components.ExternalAnnotationResolver;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.name.FqName;


/**
 *
 * @author polina
 */
public class NBExternalAnnotationResolver implements ExternalAnnotationResolver {

    @Override
    @Nullable
    public JavaAnnotation findExternalAnnotation(@NotNull JavaAnnotationOwner owner, @NotNull FqName fqName) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    @NotNull
    public Collection<JavaAnnotation> findExternalAnnotations(@NotNull JavaAnnotationOwner owner) {
        // TODO Auto-generated method stub
        return Collections.emptyList();
    }

}
