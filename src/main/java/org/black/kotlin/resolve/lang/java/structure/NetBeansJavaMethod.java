package org.black.kotlin.resolve.lang.java.structure;

import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaMethod extends NetBeansJavaMember<ExecutableElement> implements JavaMethod{
    public NetBeansJavaMethod(ExecutableElement method){
        super(method);
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return NetBeansJavaElementUtil.getValueParameters(getBinding());
    }

    @Override
    public boolean getHasAnnotationParameterDefaultValue() {
        return getBinding().getDefaultValue() != null;
    }

    @Override
    @NotNull
    public JavaType getReturnType() {
        return NetBeansJavaType.create(getBinding().getReturnType());
    }

    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass((TypeElement) getBinding().getEnclosingElement());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        List<? extends TypeParameterElement> valueParameters = getBinding().getTypeParameters();
        return typeParameters(valueParameters.toArray(new TypeParameterElement[valueParameters.size()]));
    }
}
