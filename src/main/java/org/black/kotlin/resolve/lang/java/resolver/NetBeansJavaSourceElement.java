package org.black.kotlin.resolve.lang.java.resolver;

import javax.lang.model.element.Element;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.SourceFile;
import org.jetbrains.kotlin.load.java.sources.JavaSourceElement;
import org.jetbrains.kotlin.load.java.structure.JavaElement;

/**
 *
 * @author Александр
 */
public class NetBeansJavaSourceElement implements JavaSourceElement {
    
    private final JavaElement javaElement;
    
    public NetBeansJavaSourceElement(JavaElement javaElement){
        this.javaElement = javaElement;
    }

    @Override
    @NotNull
    public JavaElement getJavaElement() {
        return javaElement;
    }

    @Override
    @NotNull
    public SourceFile getContainingFile() {
        return SourceFile.NO_SOURCE_FILE;
    }
    
    @NotNull
    public Element getElementBinding() {
        return ((NetBeansJavaElement<?>) javaElement).getBinding();
    }
    
}
