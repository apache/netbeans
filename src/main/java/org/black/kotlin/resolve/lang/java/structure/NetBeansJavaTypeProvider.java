package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.black.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeProvider;
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class NetBeansJavaTypeProvider implements JavaTypeProvider {
    
    private final Project javaProject;
    
    public NetBeansJavaTypeProvider(@NotNull Project javaProject){
        this.javaProject = javaProject;
    }

    @Override
    public JavaType createJavaLangObjectType() {
        TypeMirror type = null; // redo in future
        Element typeBinding = NetBeansJavaClassFinder.createTypeBinding(type);
        assert typeBinding != null : "Type binding for java.lang.Object can not be null";
        
        return NetBeansJavaType.create(typeBinding.asType());
    }

    @Override
    public JavaWildcardType createUpperBoundWildcard(JavaType jt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaWildcardType createLowerBoundWildcard(JavaType jt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaWildcardType createUnboundedWildcard() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
