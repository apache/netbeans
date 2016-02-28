package org.black.kotlin.resolve.lang.java.structure;

import javax.lang.model.element.Element;
import org.black.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClassObjectAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;
/**
 *
 * @author Александр
 */
public class NetBeansJavaClassObjectAnnotationArgument implements JavaClassObjectAnnotationArgument {

    private final Class<?> javaClass;
    private final Project javaProject;
    private final Name name;
    
    protected NetBeansJavaClassObjectAnnotationArgument(Class<?> javaClass,
            @NotNull Name name, @NotNull Project project){
        this.javaClass = javaClass;
        this.javaProject = project;
        this.name = name;
    }
    
    @Override
    public JavaType getReferencedType() {
        Element typeBinding = NetBeansJavaClassFinder.findType(
                new FqName(javaClass.getCanonicalName()), javaProject);
        assert typeBinding != null;
        return NetBeansJavaType.create(typeBinding.asType());
    }

    @Override
    public Name getName() {
        return name;
    }
    
    
    
}
