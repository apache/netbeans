package org.black.kotlin.resolve.lang.java.structure;

import com.google.common.collect.Lists;
import com.intellij.psi.CommonClassNames;
import java.lang.reflect.Modifier;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.Visibilities;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.JavaVisibilities;
import org.jetbrains.kotlin.load.java.structure.JavaAnnotation;
import org.jetbrains.kotlin.load.java.structure.JavaValueParameter;
import org.jetbrains.kotlin.name.ClassId;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
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
    public static ClassId computeClassId(@NotNull TypeElement classBinding){
        TypeElement container = (TypeElement) classBinding.getEnclosingElement();
        
        if (container != null){
            ClassId parentClassId = computeClassId(container);
            return parentClassId == null ? null : parentClassId.createNestedClassId(
                    Name.identifier(classBinding.getSimpleName().toString()));
        }
        
        String fqName = classBinding.getQualifiedName().toString();
        return fqName == null ? null : ClassId.topLevel(new FqName(fqName));
    }
    
    public static JavaAnnotation findAnnotation(@NotNull TypeMirror binding, @NotNull FqName fqName){
        
        for (AnnotationMirror annotation : binding.getAnnotationMirrors()){//.getKind().getDeclaringClass().getAnnotations()){
            String annotationFQName = annotation.getClass().getCanonicalName();//.annotationType().getCanonicalName(); //not sure
            if (fqName.asString().equals(annotationFQName)){
                return new NetBeansJavaAnnotation(annotation.getAnnotationType().asElement());
            }
        }
        
        return null;
    }
    
    private static List<Element> getSuperTypes(@NotNull Element typeBinding){
        List<Element> superTypes = Lists.newArrayList();
        for (Element superInterface : typeBinding.getEnclosedElements()){
            if (superInterface.getKind().isInterface()){
                superTypes.add(superInterface);
            }
        }
        
        PackageElement packageElement = (PackageElement) typeBinding.getEnclosingElement();
        
//        Element packageElement = typeBinding.getEnclosingElement();
        for (Element elem : packageElement.getEnclosedElements()){
            if (elem.getKind() == ElementKind.CLASS){
                superTypes.add(elem); //searching for a superclass
            }
        }
        return superTypes;
    }
    
    public static TypeMirror[] getSuperTypesWithObject(@NotNull Element typeBinding){
        List<TypeMirror> allSuperTypes = Lists.newArrayList();
        
        boolean javaLangObjectInSuperTypes = false;
        for (Element superType : getSuperTypes(typeBinding)){
            javaLangObjectInSuperTypes = superType.getKind().getDeclaringClass().
                    getCanonicalName().equals(CommonClassNames.JAVA_LANG_OBJECT);
            allSuperTypes.add(superType.asType());
        }
        
        if (!javaLangObjectInSuperTypes && !typeBinding.getKind().getDeclaringClass().getCanonicalName().
                equals(CommonClassNames.JAVA_LANG_OBJECT)){
        //    allSuperTypes.add(getJavaLangObjectBinding(OpenProjects.getDefault().getOpenProjects()[0]));
        }
        
        return allSuperTypes.toArray(new TypeMirror[allSuperTypes.size()]);
    }
    
    @NotNull
    static List<JavaValueParameter> getValueParameters(@NotNull ExecutableElement method){
        List<JavaValueParameter> parameters = new ArrayList<JavaValueParameter>();
        List<? extends VariableElement> valueParameters = method.getParameters();
        String[] parameterNames = getParametersNames(method);
        int parameterTypesCount = valueParameters.size();
        
        for (int i = 0; i < parameterTypesCount; i++){
            boolean isLastParameter = i == parameterTypesCount-1;
            parameters.add(new NetBeansJavaValueParameter(valueParameters.get(i), 
                    parameterNames[i], isLastParameter ? method.isVarArgs() : false));
            
        }
        
        return parameters;
    }
    
    
    @NotNull
    private static String[] getParametersNames(@NotNull ExecutableElement method){
        List<? extends VariableElement> valueParameters = method.getParameters();
        List<String> parameterNames = Lists.newArrayList();
        
        for (VariableElement elem : valueParameters){
            parameterNames.add(elem.getSimpleName().toString());
        }
        
        return parameterNames.toArray(new String[parameterNames.size()]);
        
    } 
    
    
    public static boolean isKotlinLightClass(@NotNull Element element ){
        return false;
    }
    
    
    
}
