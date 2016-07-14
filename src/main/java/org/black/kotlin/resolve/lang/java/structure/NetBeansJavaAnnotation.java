package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import org.black.kotlin.resolve.lang.java.NetBeansJavaProjectElementUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotationArgument;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.load.java.structure.JavaElement;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author Александр
 */
public class NetBeansJavaAnnotation implements JavaAnnotation, JavaElement{

    private final Project kotlinProject;
    private final AnnotationMirror binding;
    
    protected NetBeansJavaAnnotation(AnnotationMirror javaAnnotation){
        this.binding = javaAnnotation;
        this.kotlinProject = NetBeansJavaProjectElementUtils.getProject(binding.getAnnotationType().asElement());
    }
    
    public JavaAnnotationArgument findArgument(@NotNull Name name) {
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                getBinding().getElementValues().entrySet()){
            if (name.asString().equals(entry.getKey().getSimpleName().toString())){
                return NetBeansJavaAnnotationArgument.create(entry.getValue().getValue(),
                        name,
                        kotlinProject);
            }
        }
        
        return null;
    }

    @Override
    public Collection<JavaAnnotationArgument> getArguments() {
        List<JavaAnnotationArgument> arguments = Lists.newArrayList();
        for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                getBinding().getElementValues().entrySet()){
            arguments.add(NetBeansJavaAnnotationArgument.create(entry.getValue().getValue(), 
                    Name.identifier(entry.getKey().getSimpleName().toString()), 
                    kotlinProject));
        }
        return arguments;
    }

    @Override
    public ClassId getClassId() {
        DeclaredType annotationType = getBinding().getAnnotationType();
        return annotationType != null ? 
                NetBeansJavaElementUtil.computeClassId((TypeElement) annotationType.asElement()) : null;
    }

    @Override
    @Nullable
    public JavaClass resolve() {
        DeclaredType annotationType = getBinding().getAnnotationType();
        return annotationType != null ? 
                new NetBeansJavaClass((TypeElement) annotationType.asElement()) : null;
    }
    
    @NotNull
    public AnnotationMirror getBinding(){
        return binding;
    }
    
    @Override
    public int hashCode(){
        return getBinding().hashCode();
    }
    
    @Override
    public boolean equals(Object obj){
        return obj instanceof NetBeansJavaAnnotation && getBinding().equals(((NetBeansJavaAnnotation)obj).getBinding());
    }
    
}
