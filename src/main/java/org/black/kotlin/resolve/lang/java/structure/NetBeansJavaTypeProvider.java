package org.black.kotlin.resolve.lang.java.structure;

import com.intellij.psi.CommonClassNames;
import javax.lang.model.element.Element;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeProvider;
import org.jetbrains.kotlin.load.java.structure.JavaWildcardType;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Александр
 */
public class NetBeansJavaTypeProvider implements JavaTypeProvider {
    
    
    @Override
    @NotNull
    public JavaType createJavaLangObjectType() { 
        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        Project kotlinProject = null;
        
        for (Project project : projects){
            if (project instanceof KotlinProject){
                kotlinProject = project;
                break;
            }
        }
        
        Element typeBinding = NetBeansJavaProjectElementUtils.findTypeElement(kotlinProject, CommonClassNames.JAVA_LANG_OBJECT);
        
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
