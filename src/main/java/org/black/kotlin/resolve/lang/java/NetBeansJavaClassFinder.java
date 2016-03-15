package org.black.kotlin.resolve.lang.java;

import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaPackage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.JavaClassFinder;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.jvm.JavaClassFinderPostConstruct;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassFinder implements JavaClassFinder {

    private org.netbeans.api.project.Project javaProject = null;
    
    @Inject
    public void setProjectScope(@NotNull org.netbeans.api.project.Project project){
        javaProject = project;
    }
    
    @Inject
    public void setComponentPostConstruct(@NotNull JavaClassFinderPostConstruct finderPostConstruct) {
    }
    
    @Override
    @Nullable
    public JavaClass findClass(ClassId classId) {
        Element element = findType(classId.asSingleFqName(), javaProject);
        if (element != null) {
            return new NetBeansJavaClass((TypeElement) element);
        }
        
        return null;
    }

    @Override
    public JavaPackage findPackage(FqName fqName) {
        Element packageEl = NetBeansJavaProjectElementUtils.findElement(javaProject, fqName.asString());
        if (packageEl != null){
            return new NetBeansJavaPackage((PackageElement) packageEl);
        }
        
        return null;
    }

    @Nullable
    public static Element findType(@NotNull FqName fqName, @NotNull org.netbeans.api.project.Project project){
        Element type = NetBeansJavaProjectElementUtils.findElement(project, fqName.asString());
        if (type != null){
            return !isInKotlinBinFolder(type) ? type : null;
        }
        
        return null;
    }

    
    @Nullable 
    public static PackageElement[] findPackageFragments(org.netbeans.api.project.Project javaProject, String name,
            boolean partialMatch, boolean patternMatch){
        PackageElement pack = (PackageElement) NetBeansJavaProjectElementUtils.findElement(javaProject, name);
        
        return null;
    }
    
    public static boolean isInKotlinBinFolder(@NotNull Element element){
        
        return false;
    }
    
}

