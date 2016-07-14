package org.black.kotlin.resolve.lang.java;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.black.kotlin.project.KotlinProject;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaClass;
import org.black.kotlin.resolve.lang.java.structure.NetBeansJavaPackage;
import org.black.kotlin.utils.ProjectUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.JavaClassFinder;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.resolve.jvm.JavaClassFinderPostConstruct;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClassFinder implements JavaClassFinder {

    private org.netbeans.api.project.Project kotlinProject = null;
    
    @Inject
    public void setProjectScope(@NotNull org.netbeans.api.project.Project project){
        kotlinProject = project;
    }
    
    @Inject
    public void setComponentPostConstruct(@NotNull JavaClassFinderPostConstruct finderPostConstruct) {
    }
    
    @Override
    @Nullable
    public JavaClass findClass(ClassId classId) {
        TypeElement element = findType(classId.asSingleFqName(), kotlinProject);
        if (element != null) {
            return new NetBeansJavaClass(element);
        }
        
        return null;
    }

    @Override
    public JavaPackage findPackage(FqName fqName) {
        PackageElement packageEl = NetBeansJavaProjectElementUtils.findPackageElement(kotlinProject, fqName.asString());
        if (packageEl != null){
            return new NetBeansJavaPackage(packageEl, kotlinProject);
        } 
        
        return null;
    }

    @Nullable
    public static TypeElement findType(@NotNull FqName fqName, @NotNull org.netbeans.api.project.Project project){
        TypeElement type = NetBeansJavaProjectElementUtils.findTypeElement(project, fqName.asString());
        if (type != null){
            return !isInKotlinBinFolder(type) ? type : null;
        }
        
        return null;
    }

    
    @Nullable 
    public static PackageElement[] findPackageFragments(org.netbeans.api.project.Project kotlinProject, String name,
            boolean partialMatch, boolean patternMatch){
        if (name.endsWith(".")){
            name = name.substring(0, name.length()-1);
        }
        
        List<PackageElement> subpackageElements = Lists.newArrayList();
        PackageElement pack = NetBeansJavaProjectElementUtils.findPackageElement(kotlinProject, name);
        if (pack == null){
            return null;
        }
        
        FileObject packageFile = NetBeansJavaProjectElementUtils.getFileObjectForElement(pack, kotlinProject);
        if (packageFile == null){
            return null;
        }
        
        for (FileObject subpackage : packageFile.getChildren()){
            if (subpackage.isFolder()){
                String path = subpackage.getPath();
                
                // fix in future
                path.replace("\\", "/");
                path.replace(ProjectUtils.FILE_SEPARATOR, "/");
                
                String[] pathParts = path.split("/");
                PackageElement subpackageElement = NetBeansJavaProjectElementUtils.findPackageElement(kotlinProject,name + "." + pathParts[pathParts.length-1]);
                if (subpackageElement == null){
                    continue;
                }
                subpackageElements.add(subpackageElement);
            }
        }
        
        if (subpackageElements.isEmpty()){
            return null;
        }
        
        return subpackageElements.toArray(new PackageElement[subpackageElements.size()]);
    }
    
    public static boolean isInKotlinBinFolder(@NotNull Element element){
        
        return false;
    }

    @Override
    public Set<String> knownClassNamesInPackage(FqName packageFqName) {
        return null;
    }
    
}

