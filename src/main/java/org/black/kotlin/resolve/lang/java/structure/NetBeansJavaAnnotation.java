package org.black.kotlin.resolve.lang.java.structure;

import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.annotations.Nullable;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Александр
 */
public class NetBeansJavaAnnotation extends NetBeansJavaElement<Element> implements JavaAnnotation{

    private final Project javaProject;
    
    protected NetBeansJavaAnnotation(Element javaAnnotation){
        super(javaAnnotation);
        this.javaProject = OpenProjects.getDefault().getOpenProjects()[0];
    }
    
    @Override
    public JavaAnnotationArgument findArgument(Name name) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<JavaAnnotationArgument> getArguments() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ClassId getClassId() {
        Class cl = getBinding().getKind().getDeclaringClass();
        return cl.isAnnotation() == true ? 
                NetBeansJavaElementUtil.computeClassId(cl) : null;
    }

    @Override
    @Nullable
    public JavaClass resolve() {
        return getBinding().getKind().getDeclaringClass().isAnnotation() == true ? 
                new NetBeansJavaClass(getBinding()) : null;
    }
    
}
