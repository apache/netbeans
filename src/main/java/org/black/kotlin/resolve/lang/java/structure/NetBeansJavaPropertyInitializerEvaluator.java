package org.black.kotlin.resolve.lang.java.structure;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.descriptors.PropertyDescriptor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.components.JavaPropertyInitializerEvaluator;
import org.jetbrains.kotlin.resolve.constants.ConstantValue;

/**
 *
 * @author Александр
 */
public class NetBeansJavaPropertyInitializerEvaluator implements JavaPropertyInitializerEvaluator {

    @Override
    @Nullable
    public ConstantValue<?> getInitializerConstant(JavaField jf, PropertyDescriptor pd) {
        return null;
    }

    @Override
    public boolean isNotNullCompileTimeConstant(JavaField jf) {
        return false;
    }
    
}
