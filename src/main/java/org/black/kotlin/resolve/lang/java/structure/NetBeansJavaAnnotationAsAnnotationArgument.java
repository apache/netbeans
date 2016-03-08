package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.AnnotationMirror;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationAsAnnotationArgument;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public class NetBeansJavaAnnotationAsAnnotationArgument implements JavaAnnotationAsAnnotationArgument {

    private final AnnotationMirror annotation;
    private final Name name;
    
    public NetBeansJavaAnnotationAsAnnotationArgument(AnnotationMirror annotation, Name name){
        this.annotation = annotation;
        this.name = name;
    }
    
    @Override
    public JavaAnnotation getAnnotation() {
        return new NetBeansJavaAnnotation(annotation);
    }

    @Override
    public Name getName() {
        return name;
    }
    
}
