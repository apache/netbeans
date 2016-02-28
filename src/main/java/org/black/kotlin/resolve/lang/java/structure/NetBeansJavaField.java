package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaType;

/**
 *
 * @author Александр
 */
public class NetBeansJavaField extends NetBeansJavaMember<Element> implements JavaField {
    
    public NetBeansJavaField(Element javaField){
        super(javaField);
    }

    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass(getBinding().getEnclosingElement());
    }

    @Override
    public boolean isEnumEntry() {
        return getBinding().getKind().equals(ElementKind.ENUM_CONSTANT);
    }

    @Override
    public JavaType getType() {
        return NetBeansJavaType.create(getBinding().asType());
    }
}
