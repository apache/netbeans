package org.black.kotlin.resolve.lang.java.structure;


import javax.lang.model.element.VariableElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.structure.JavaEnumValueAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaField;

/**
 *
 * @author Александр
 */
public class NetBeansJavaReferenceAnnotationArgument extends NetBeansJavaAnnotationArgument<VariableElement> 
        implements JavaEnumValueAnnotationArgument{
    
    protected NetBeansJavaReferenceAnnotationArgument(VariableElement javaElement){
        super(javaElement);
    }
    
    @Override
    @Nullable
    public JavaField resolve(){
        return new NetBeansJavaField(getBinding());
    }
    
}
