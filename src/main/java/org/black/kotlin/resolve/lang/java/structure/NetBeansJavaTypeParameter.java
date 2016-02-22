package org.black.kotlin.resolve.lang.java.structure;

import java.util.Collection;
import javax.lang.model.element.Element;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameterListOwner;
import org.jetbrains.kotlin.load.java.structure.JavaTypeProvider;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public class NetBeansJavaTypeParameter extends NetBeansJavaClassifier<Element> implements JavaTypeParameter {
    
    public NetBeansJavaTypeParameter(Element binding){
        super(binding);
    }

    @Override
    public Name getName() {
        return Name.identifier(getBinding().getSimpleName().toString());
    }

    @Override
    public Collection<JavaClassifierType> getUpperBounds() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaTypeParameterListOwner getOwner() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaType getType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaTypeProvider getTypeProvider() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
