package org.black.kotlin.resolve.lang.java.structure;

import java.lang.reflect.Modifier;
import java.lang.annotation.Annotation;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
/**
 *
 * @author Александр
 */
public class NetBeansJavaElementUtil {

    @NotNull
    static Visibility getVisibility(@NotNull Element member){
        int flags = member.getKind().getDeclaringClass().getModifiers();
        
        if (Modifier.isPublic(flags)){
            return Visibilities.PUBLIC;
        } else if (Modifier.isPrivate(flags)){
            return Visibilities.PRIVATE;
        } else if (Modifier.isProtected(flags)){
            return Modifier.isStatic(flags) ? JavaVisibilities.PROTECTED_STATIC_VISIBILITY :
                    JavaVisibilities.PROTECTED_AND_PACKAGE;
        }
        
        return JavaVisibilities.PACKAGE_VISIBILITY;
    }
    
    @Nullable
    public static ClassId computeClassId(Class classBinding){
        Class container = classBinding.getDeclaringClass();
        if (container != null){
            ClassId parentClassId = computeClassId(container);
            return parentClassId == null ? null : parentClassId.createNestedClassId(Name.identifier(classBinding.getName()));
        }
        
        String fqName = classBinding.getCanonicalName(); //Not sure
        return fqName == null ? null : ClassId.topLevel(new FqName(fqName));
    }
    
    public static JavaAnnotation findAnnotation(@NotNull Element bindings, @NotNull FqName fqName){
        for (AnnotationMirror annotation : bindings.getAnnotationMirrors()){//.getKind().getDeclaringClass().getAnnotations()){
            String annotationFQName = annotation.getClass().getCanonicalName();//.annotationType().getCanonicalName(); //not sure
            if (fqName.asString().equals(annotationFQName)){
                return new NetBeansJavaAnnotation(annotation.getAnnotationType().asElement());
            }
        }
        
        return null;
    }
    
}
