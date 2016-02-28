package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.lang.reflect.TypeVariable;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;

/**
 *
 * @author Александр
 */
public class NetBeansJavaConstructor extends NetBeansJavaMember<Element> implements JavaConstructor {

    public NetBeansJavaConstructor(@NotNull Element methodBinding){
        super(methodBinding);
    }
    
    @Override
    public JavaClass getContainingClass() {
        return new NetBeansJavaClass(getBinding().getEnclosingElement());
    }

    @Override
    public List<JavaValueParameter> getValueParameters() {
        return NetBeansJavaElementUtil.getValueParameters((ExecutableElement) getBinding());
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        List<? extends TypeParameterElement> valueParameters = ((ExecutableElement) getBinding()).getTypeParameters();
        return typeParameters(valueParameters.toArray(new Element[valueParameters.size()]));
    }
    
}
