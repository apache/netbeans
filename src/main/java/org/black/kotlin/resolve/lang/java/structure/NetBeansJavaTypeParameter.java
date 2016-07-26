package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.name.SpecialNames;

/**
 *
 * @author Александр
 */
public class NetBeansJavaTypeParameter extends NetBeansJavaClassifier<TypeParameterElement> implements JavaTypeParameter {
    
    public NetBeansJavaTypeParameter(TypeParameterElement binding){
        super(binding);
    }

    @Override
    public Name getName() {
        return SpecialNames.safeIdentifier(getBinding().getSimpleName().toString());
    }

    @Override
    public Collection<JavaClassifierType> getUpperBounds() {
        List<JavaClassifierType> bounds = Lists.newArrayList();
        
        for (TypeMirror bound : getBinding().getBounds()){
            bounds.add(new NetBeansJavaClassifierType(bound));
        }
        
        return bounds;
    }
    
}
