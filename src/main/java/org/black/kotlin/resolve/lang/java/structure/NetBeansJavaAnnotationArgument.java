package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class NetBeansJavaAnnotationArgument<T extends Element> extends NetBeansJavaElement<T> 
        implements JavaAnnotationArgument {
    
    public NetBeansJavaAnnotationArgument(T javaElement){
        super(javaElement);
    }

    @Override
    public Name getName() {
        return Name.identifier(getBinding().getSimpleName().toString());//maybe it requires canonical name
    }
    
    public static JavaAnnotationArgument create(Object value, Name name, Project project){
        //TODO
        return null;//temporary
    }
    
}
