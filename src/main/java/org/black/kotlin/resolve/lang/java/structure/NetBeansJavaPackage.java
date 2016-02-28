package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.PackageElement;
import kotlin.jvm.functions.Function1;
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

    @Override
    public Collection<JavaPackage> getSubPackages() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public FqName getFqName() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaClass> getClasses(Function1<? super Name, Boolean> fnctn) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
