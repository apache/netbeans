package org.black.kotlin.resolve.lang.java.structure;

import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaConstructor extends NetBeansJavaMember<ExecutableElement> implements JavaConstructor {

    public NetBeansJavaConstructor(@NotNull ExecutableElement methodBinding){
        super(methodBinding);
    }
    
    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass((TypeElement) getBinding().getEnclosingElement());
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return NetBeansJavaElementUtil.getValueParameters(getBinding());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        List<? extends TypeParameterElement> valueParameters = getBinding().getTypeParameters();
        return typeParameters(valueParameters.toArray(new TypeParameterElement[valueParameters.size()]));
    }
    
}
