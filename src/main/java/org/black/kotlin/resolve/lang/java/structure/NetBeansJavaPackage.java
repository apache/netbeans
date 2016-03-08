package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import kotlin.jvm.functions.Function1;
import org.black.kotlin.resolve.lang.java.NetBeansJavaClassFinder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.jetbrains.kotlin.load.java.structure.JavaPackage;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Александр
 */
public class NetBeansJavaPackage implements JavaElement, JavaPackage{
    
    private final List<PackageElement> packages = Lists.newArrayList();
    private final Project javaProject;
    
    public NetBeansJavaPackage(List<PackageElement> packages){
        this.packages.addAll(packages);
        this.javaProject = OpenProjects.getDefault().getOpenProjects()[0];
    }
    
    public NetBeansJavaPackage(PackageElement pack){
        this(Collections.singletonList(pack));
    }

    @Override
    @NotNull
    public Collection<JavaPackage> getSubPackages() {
        String thisPackageName = getFqName().asString();
        String pattern = thisPackageName.isEmpty() ? "*" : thisPackageName + ".";
        
        PackageElement[] packageFragments = NetBeansJavaClassFinder.findPackageFragments(
                javaProject, pattern, true, true);
        
        int thisNestedLevel = thisPackageName.split("\\.").length;
        List<JavaPackage> javaPackages = Lists.newArrayList();
        if (packageFragments != null && packageFragments.length > 0){
            for (PackageElement packageFragment : packageFragments){
                int subNestedLevel = packageFragment.getQualifiedName().toString().split("\\.").length;
                boolean applicableForRootPackage = thisNestedLevel == 1 && thisNestedLevel == subNestedLevel;
                if (!packageFragment.getQualifiedName().toString().isEmpty() &&
                        (applicableForRootPackage || (thisNestedLevel + 1 == subNestedLevel))){
                    javaPackages.add(new NetBeansJavaPackage(packageFragment));
                }
            }
        }
        
        return javaPackages;
    }

    @Override
    @NotNull
    public FqName getFqName() {
        return new FqName(packages.get(0).getQualifiedName().toString());
    }

    @Override
    public Collection<JavaClass> getClasses(Function1<? super Name, Boolean> nameFilter) {
        List<JavaClass> javaClasses = Lists.newArrayList();
        
        for (PackageElement pckg : packages){
            javaClasses.addAll(getClassesInPackage(pckg, nameFilter));
        }
        
        return javaClasses;
    }
    
    private List<JavaClass> getClassesInPackage(PackageElement javaPackage, 
            Function1<? super Name, ? extends Boolean> nameFilter){
        List<JavaClass> javaClasses = Lists.newArrayList();
        List<? extends Element> classes = javaPackage.getEnclosedElements();
        
        for (Element cl : classes){
            TypeMirror type = cl.asType();
            if (isOuterClass((TypeElement) cl)){
//                String elementName = type.toString();
                String elementName = cl.getSimpleName().toString();
                if (Name.isValidIdentifier(elementName) && nameFilter.invoke(Name.identifier(elementName))){
                    Element typeBinding = NetBeansJavaClassFinder.createTypeBinding(type);
                    if (typeBinding != null){
                        javaClasses.add(new NetBeansJavaClass((TypeElement) typeBinding));
                    }
                }
            }
        }
        
        return javaClasses;
    }
    
    private boolean isOuterClass(TypeElement classFile){
        return !classFile.getSimpleName().toString().contains("$");
    }
    
    
    
}
