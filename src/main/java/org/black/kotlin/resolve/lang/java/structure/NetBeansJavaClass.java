package org.black.kotlin.resolve.lang.java.structure;

import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.typeParameters;
import static org.black.kotlin.resolve.lang.java.structure.NetBeansJavaElementFactory.classifierTypes;

import com.google.common.collect.Lists;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import org.jetbrains.kotlin.descriptors.Visibility;
import org.jetbrains.kotlin.load.java.structure.JavaClass;
import org.jetbrains.kotlin.load.java.structure.JavaClassifierType;
import org.jetbrains.kotlin.load.java.structure.JavaConstructor;
import org.jetbrains.kotlin.load.java.structure.JavaField;
import org.jetbrains.kotlin.load.java.structure.JavaMethod;
import org.jetbrains.kotlin.load.java.structure.JavaType;
import org.jetbrains.kotlin.load.java.structure.JavaTypeParameter;
import org.jetbrains.kotlin.load.java.structure.JavaTypeSubstitutor;
import org.jetbrains.kotlin.name.FqName;
import org.jetbrains.kotlin.name.Name;

/**
 *
 * @author Александр
 */
public class NetBeansJavaClass extends NetBeansJavaClassifier<Element> implements JavaClass {
    
    public NetBeansJavaClass(Element javaElement){
        super(javaElement);
    }

    @Override
    public Name getName() {
        return Name.guess(getBinding().getSimpleName().toString());
    }

    @Override
    public Collection<JavaClass> getInnerClasses() {
        List<? extends Element> enclosedElements = getBinding().getEnclosedElements();
        List<JavaClass> innerClasses = Lists.newArrayList();
        
        for (Element element : enclosedElements){
            if (element.getKind().isClass())
                innerClasses.add(new NetBeansJavaClass(element));
        }
        return innerClasses;
    }

    @Override
    public FqName getFqName() {
        return new FqName(((TypeElement) getBinding()).getQualifiedName().toString());
    }

    @Override
    public boolean isInterface() {
        return ((TypeElement) getBinding()).getKind().isInterface();
    }

    @Override
    public boolean isAnnotationType() {
        return ((TypeElement) getBinding()).getKind() == ElementKind.ANNOTATION_TYPE;
    }

    @Override
    public boolean isEnum() {
        return ((TypeElement) getBinding()).getKind() == ElementKind.ENUM;
    }

    @Override
    public JavaClass getOuterClass() {
        Element outerClass = ((TypeElement) getBinding()).getEnclosingElement();
        return outerClass != null ? new NetBeansJavaClass(outerClass) : null;
    }

    @Override
    public Collection<JavaClassifierType> getSupertypes() {
        return classifierTypes(NetBeansJavaElementUtil.getSuperTypesWithObject(getBinding()));
    }

    @Override
    public Collection<JavaMethod> getMethods() {
        
        List<? extends Element> declaredElements = getBinding().getEnclosedElements();
        List<JavaMethod> javaMethods = Lists.newArrayList();
        
        for (Element element : declaredElements){
            if (element.getKind() == ElementKind.METHOD){
                javaMethods.add(new NetBeansJavaMethod(element));
            }
        }
        return javaMethods;
    }

    @Override
    public Collection<JavaField> getFields() {
        List<? extends Element> declaredElements = getBinding().getEnclosedElements();
        List<JavaField> javaFields = Lists.newArrayList();
        
        for (Element element : declaredElements){
            if (element.getKind() == ElementKind.FIELD){
                String name = element.getSimpleName().toString();
                if (name != null && Name.isValidIdentifier(name)){
                    javaFields.add(new NetBeansJavaField(element));
                }
            }
        }
        
        return javaFields;
    }

    @Override
    public Collection<JavaConstructor> getConstructors() {
        List<? extends Element> declaredElements = getBinding().getEnclosedElements();
        List<JavaConstructor> javaConstructors = Lists.newArrayList();
        
        for (Element element : declaredElements){
            if (element.getKind().equals(ElementKind.CONSTRUCTOR)){
                javaConstructors.add(new NetBeansJavaConstructor(element));
            }
        }
        return javaConstructors;
    }

    @Override
    public JavaClassifierType getDefaultType() {
        return new NetBeansJavaClassifierType(getBinding().asType());
    }

    @Override
    public OriginKind getOriginKind() {
        if (NetBeansJavaElementUtil.isKotlinLightClass(getBinding())){
            return OriginKind.KOTLIN_LIGHT_CLASS;
        } else // to add OriginKind.COMPILED
            return OriginKind.SOURCE;
    }

    @Override
    public JavaType createImmediateType(JavaTypeSubstitutor substitutor) {
        return new NetBeansJavaImmediateClass(this, substitutor);
    }

    @Override
    public List<JavaTypeParameter> getTypeParameters() {
        List<? extends TypeParameterElement> typeParameters = ((TypeElement) getBinding()).getTypeParameters();
        return typeParameters(typeParameters.toArray(new TypeParameterElement[typeParameters.size()]));
    }

    @Override
    public boolean isAbstract() {
//        ((TypeElement) getBinding()).getModifiers().
        return Modifier.isAbstract(getBinding().getKind().getDeclaringClass().getModifiers());
    }

    @Override
    public boolean isStatic() {
        return Modifier.isStatic(getBinding().getKind().getDeclaringClass().getModifiers());
    }

    @Override
    public boolean isFinal() {
        return Modifier.isFinal(getBinding().getKind().getDeclaringClass().getModifiers());
    }

    @Override
    public Visibility getVisibility() {
        return NetBeansJavaElementUtil.getVisibility(getBinding());
    }
    
}
