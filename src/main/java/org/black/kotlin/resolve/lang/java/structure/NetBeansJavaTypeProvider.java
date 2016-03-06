package org.black.kotlin.resolve.lang.java.structure;

import com.intellij.psi.CommonClassNames;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.black.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementFinder;
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
    @NotNull
    public JavaType createJavaLangObjectType() { 
        TypeMirror type = NetBeansJavaProjectElementFinder.findElement(
                javaProject, CommonClassNames.JAVA_LANG_OBJECT).asType();
        Element typeBinding = NetBeansJavaClassFinder.createTypeBinding(type);
        assert typeBinding != null : "Type binding for java.lang.Object cannot be null";
        
        return NetBeansJavaType.create(typeBinding.asType());
    }

    @Override
    @NotNull
    public JavaWildcardType createUpperBoundWildcard(@NotNull JavaType bound) {
        return new NetBeansJavaImmediateWildcardType(bound, true, this);
    }

    @Override
    @NotNull
    public JavaWildcardType createLowerBoundWildcard(@NotNull JavaType bound) {
        return new NetBeansJavaImmediateWildcardType(bound, false, this);
    }

    @Override
    @NotNull
    public JavaWildcardType createUnboundedWildcard() {
        return new NetBeansJavaImmediateWildcardType(null, false, this);
    }
    
}
