package org.black.kotlin.resolve.lang.java.structure;

import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaMethod extends NetBeansJavaMember<Element> implements JavaMethod{
    public NetBeansJavaMethod(Element method){
        super(method);
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return NetBeansJavaElementUtil.getValueParameters((ExecutableElement)getBinding());
    }

    @Override
    public boolean hasAnnotationParameterDefaultValue() {
        return ((ExecutableElement) getBinding()).getDefaultValue() != null;
    }

    @Override
    public JavaType getReturnType() {
        return NetBeansJavaType.create(((ExecutableElement) getBinding()).getReturnType());
    }

    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass(getBinding().getEnclosingElement());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        List<? extends TypeParameterElement> valueParameters = ((ExecutableElement) getBinding()).getTypeParameters();
        return typeParameters(valueParameters.toArray(new Element[valueParameters.size()]));
    }
}
