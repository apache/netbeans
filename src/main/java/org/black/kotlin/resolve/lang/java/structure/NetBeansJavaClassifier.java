package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationOwner;
import org.jetbrains.kotlin.load.java.structure.JavaClassifier;

/**
 *
 * @author Александр
 */
public abstract class NetBeansJavaClassifier<T extends TypeMirror> extends
        NetBeansJavaElement<T> implements JavaClassifier, JavaAnnotationOwner {
    
    public NetBeansJavaClassifier(T javaType) {
        super(javaType);
    }
    
}
