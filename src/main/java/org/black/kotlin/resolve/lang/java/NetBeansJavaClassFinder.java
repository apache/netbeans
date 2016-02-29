package org.black.kotlin.resolve.lang.java;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.JavaClassFinder;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.netbeans.api.project.Project;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassFinder implements JavaClassFinder{

    @Override
    public JavaClass findClass(ClassId classId) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public JavaPackage findPackage(FqName fqName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Nullable
    public static Element findType(@NotNull FqName fqName, @NotNull Project project){
        return null;//temporary
    }

    public static Element createTypeBinding(TypeMirror type){
        return null;
    }
    
    @Nullable 
    public static PackageElement[] findPackageFragments(Project javaProject, String name,
            boolean partialMatch, boolean patternMatch){
        
        return null;
    }
    
}

