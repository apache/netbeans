package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaField extends NetBeansJavaMember<VariableElement> implements JavaField {
    
    public NetBeansJavaField(VariableElement javaField){
        super(javaField);
    }

    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass((TypeElement) getBinding().getEnclosingElement());
    }

    @Override
    public boolean isEnumEntry() {
        return getBinding().getKind() == ElementKind.ENUM_CONSTANT;
    }

    @Override
    public JavaType getType() {
        return NetBeansJavaType.create(getBinding().asType());
    }
}
