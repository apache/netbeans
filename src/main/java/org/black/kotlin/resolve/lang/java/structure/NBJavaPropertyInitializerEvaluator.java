/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.black.kotlin.resolve.lang.java.structure;

/**
 *
 * @author polina
 */

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.PropertyDescriptor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaPropertyInitializerEvaluator;
import org.jetbrains.kotlin.resolve.constants.ConstantValue;

public class NBJavaPropertyInitializerEvaluator  implements JavaPropertyInitializerEvaluator {
    @Override
    @Nullable
    public ConstantValue<?> getInitializerConstant(@NotNull JavaField field, @NotNull PropertyDescriptor descriptor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isNotNullCompileTimeConstant(@NotNull JavaField field) {
        // TODO Auto-generated method stub
        return false;
    }
}
