package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameterListOwner;
import org.jetbrains.kotlin.load.java.structure.JavaTypeProvider;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.ui.OpenProjects;

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
        return Name.identifier(getBinding().getSimpleName().toString());
    }

    @Override
    public Collection<JavaClassifierType> getUpperBounds() {
        List<JavaClassifierType> bounds = Lists.newArrayList();
        
        for (TypeMirror bound : getBinding().getBounds()){
            bounds.add(new NetBeansJavaClassifierType(bound));
        }
        
        return bounds;
    }

    @Override
    public JavaTypeParameterListOwner getOwner() {
        Element owner = getBinding().getEnclosingElement();
        if (owner != null){
            switch (owner.getKind()) {
                case CONSTRUCTOR:
                    return new NetBeansJavaConstructor((ExecutableElement) owner);
                case METHOD:
                    return new NetBeansJavaMethod((ExecutableElement) owner);
                case CLASS:
                    return new NetBeansJavaClass((TypeElement) owner);
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public JavaType getType() {
        return NetBeansJavaType.create(getBinding().asType());
    }

    @Override
    public JavaTypeProvider getTypeProvider() {
        return new NetBeansJavaTypeProvider(OpenProjects.getDefault().getOpenProjects()[0]);
    }
    
}
