package org.black.kotlin.resolve.lang.java.structure;

import java.util.Collection;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.VariableElement;
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
        
        if (value instanceof AnnotationMirror){
            return new NetBeansJavaAnnotationAsAnnotationArgument((AnnotationMirror) value, name);
        }
        else if (/*((Element) value).getKind() == ElementKind.FIELD || 
                ((Element) value).getKind() == ElementKind.LOCAL_VARIABLE ||
                ((Element) value).getKind() == ElementKind.ENUM_CONSTANT ||
                ((Element) value).getKind() == ElementKind.EXCEPTION_PARAMETER ||
                ((Element) value).getKind() == ElementKind.RESOURCE_VARIABLE ||
                ((Element) value).getKind() == ElementKind.PARAMETER*/
                value instanceof VariableElement){
            return new NetBeansJavaReferenceAnnotationArgument((VariableElement) value);
        }
        else if (value instanceof String){
            return new NetBeansJavaLiteralAnnotationArgument(value, name);
        }
        else if (value instanceof Class<?>){
            return new NetBeansJavaClassObjectAnnotationArgument((Class) value, name, project);
        }
        else if (value instanceof Collection<?>){
            return new NetBeansJavaArrayAnnotationArgument((Collection) value, name, project);
        }
        else throw new IllegalArgumentException("Wrong annotation argument");
    }
    
}
