package org.black.kotlin.resolve.lang.java.resolver;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.sources.JavaSourceElement;
import org.jetbrains.kotlin.load.java.sources.JavaSourceElementFactory;
import org.jetbrains.kotlin.load.java.structure.JavaElement;

/**
 *
 * @author Александр
 */
public class NetBeansJavaSourceElementFactory implements JavaSourceElementFactory {
    
    @Override
    @NotNull
    public JavaSourceElement source(@NotNull JavaElement javaElement){
        return new NetBeansJavaSourceElement(javaElement);
    }
    
}
