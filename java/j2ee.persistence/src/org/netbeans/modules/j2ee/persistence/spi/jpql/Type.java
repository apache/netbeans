/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.j2ee.persistence.spi.jpql;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.eclipse.persistence.jpa.jpql.tools.spi.IConstructor;
import org.eclipse.persistence.jpa.jpql.tools.spi.IType;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeDeclaration;
import org.eclipse.persistence.jpa.jpql.tools.spi.ITypeRepository;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObject;

/**
 *
 * @author sp153251
 */
public class Type implements IType{
    
    private final Element element;
    private PersistentObject po;
    private final ITypeRepository repository;
    private ITypeDeclaration tDeclaration;
    private final Class<?> type;
    private Collection<IConstructor> constructors;
    private String[] enumConstants;
    private  String typeName;

    public Type(ITypeRepository typeRepository, PersistentObject po) {
        element = null;
        this.po = po;
        this.repository = typeRepository;
        type = null;
        typeName = po.getTypeElementHandle().getQualifiedName();
    }
    
    public Type(ITypeRepository typeRepository, Element element){
        this.element = element;
        this.repository = typeRepository;
        type = null;
    }
    
    public Type(ITypeRepository typeRepository, Class<?> type) {
        this.type = type;
        this.repository = typeRepository;
        element = null;
    }
    
    Type(ITypeRepository typeRepository, String typeName){
        this.repository = typeRepository;
        this.typeName = typeName;
        element = null;
        //
        enumConstants = new String[]{};
        constructors = Collections.emptyList();
        type = null;
    } 
    
    @Override
    public Iterable<IConstructor> constructors() {
        if(constructors == null){
            constructors = new ArrayList<IConstructor>();
            if(po!=null) {
                collectConstructors(constructors, getTypeElement(po));
            } else if(element != null){
                collectConstructors(constructors, element);
            } else if (type != null) {
                collectConstructors(constructors, type);
            }
        }
        return constructors;
    }

    @Override
    public boolean equals(IType itype) {
        if (itype == null) {
            return false;
        }
        return this==itype || (getName()!=null && (getName().equals(itype.getName())));
    }
    
    @Override
    public boolean equals(Object itype) {
        return (this == itype) || ((itype instanceof IType) && equals((IType) itype));
    }
    
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public String[] getEnumConstants() {
        if(enumConstants == null){
            Element elt = po != null ? getTypeElement(po) : element;
            if(elt != null){
                ArrayList<String> constants = new ArrayList<>();
                for( Element el:elt.getEnclosedElements() ){
                    if(el.getKind() == ElementKind.ENUM_CONSTANT){
                        constants.add(el.getSimpleName().toString());
                    }
                }
                enumConstants = constants.toArray(new String[]{});
            } else if (type != null) {
                if (!type.isEnum()) {
                    enumConstants = new String[]{};
                } else {
                    Object[] enumC = type.getEnumConstants();
                    enumConstants = new String[enumC.length];

                    for (int index = enumC.length; --index >= 0;) {
                        enumConstants[index] = ((Enum<?>) enumC[index]).name();
                    }

                }
            } else {
                enumConstants = new String[]{};
            }
        }
        return enumConstants;
    }

    @Override
    public String getName() {
        if(typeName == null){
            Element elt = po != null ? getTypeElement(po) : element;
            if(elt != null){
                if(elt instanceof TypeElement) {
                    typeName = ((TypeElement) elt).getQualifiedName().toString();
                }
                else {
                    typeName = elt.asType().toString();
                }
            } else if (type != null) {
                typeName = type.getName();
            }
        }
        return typeName;
    }

    @Override
    public ITypeDeclaration getTypeDeclaration() {
        if(tDeclaration == null){
            tDeclaration = type != null ? new TypeDeclaration(repository, this, null, type.isArray()) : new TypeDeclaration(this, new ITypeDeclaration[0], 0);
        }
        return tDeclaration;
    }

    @Override
    public boolean hasAnnotation(Class<? extends Annotation> type) {
        Element elt = po != null ? getTypeElement(po) : element;
        return elt != null ? (elt.getAnnotation(type) != null) : (type!=null && type.isAnnotationPresent(type));
    }

    @Override
    public boolean isAssignableTo(IType itype) {
        if(this == itype) {
            return true;
        }
        Type tp = (Type) itype;
        Element elt1 = po != null ? getTypeElement(po) : element;
        Element elt2 = tp.po != null ? getTypeElement(tp.po) : tp.element;
        if(elt1 != null && elt2 !=null){
            //interbal nb type
            String rootName = itype.getName();
            TypeElement tEl = (TypeElement) (elt1 instanceof TypeElement ? elt1 : null);
            return haveInHierarchy(tEl, rootName);
        } else if (type !=null && tp.type!=null) {
            //java type
            return tp.type.isAssignableFrom(type);
        } else {
            return false;
        }
    }

    @Override
    public boolean isEnum() {
        Element elt = po != null ? getTypeElement(po) : element;
        return  (elt instanceof TypeElement ? ((TypeElement)elt).getKind() == ElementKind.ENUM : (type != null) && type.isEnum());
    }

    @Override
    public boolean isResolvable() {
        return type!=null || element!=null || po!=null;
    }

    @Override
    public String toString() {
        return super.toString() + ", name = " + getName();
    }
    
    private void collectConstructors(Collection<IConstructor> constructors, Element element){
        if(element == null || element.getKind()!=ElementKind.CLASS) {
            return;
        }
        TypeElement el = (TypeElement) element;
        for(Element sub: el.getEnclosedElements()){
            if(sub.getKind() == ElementKind.CONSTRUCTOR){
                constructors.add(new Constructor(this, (ExecutableElement)sub));
            } else if ((sub.getKind() == ElementKind.CLASS) && (((TypeElement) sub).getSuperclass() != null)){
                TypeMirror supMirror = ((TypeElement) sub).getSuperclass();
                if (supMirror.getKind() == TypeKind.DECLARED) {
                    DeclaredType superclassDeclaredType = (DeclaredType)supMirror;
                    Element superclassElement = superclassDeclaredType.asElement();  
                    collectConstructors(constructors, superclassElement);
                }
            }
        }
    }
    
    private void collectConstructors(Collection<IConstructor> constructors, Class<?> type){
        java.lang.reflect.Constructor<?>[] javaConstructors = type.getDeclaredConstructors();

        for (java.lang.reflect.Constructor<?> javaConstructor : javaConstructors) {
            constructors.add(new Constructor(this,javaConstructor));
        }
    }
    
    private boolean haveInHierarchy(TypeElement el, String name){
        
        TypeElement tmpEl = el;
        while(tmpEl != null){
            if(tmpEl.getQualifiedName().contentEquals(name)) {
                return true;
            }
            else {
                TypeMirror supMirror = tmpEl.getSuperclass();
                if (supMirror.getKind() == TypeKind.DECLARED) {
                    DeclaredType superclassDeclaredType = (DeclaredType)supMirror;
                    Element superclassElement = superclassDeclaredType.asElement();  
                    if(superclassElement instanceof TypeElement) {
                        tmpEl = (TypeElement) superclassElement;
                    }
                    else {
                        tmpEl = null;
                    }
                } else {
                    tmpEl = null;
                }
            }
        }
        for(TypeMirror tmpMirr: el.getInterfaces()){
            if(tmpMirr.getKind()== TypeKind.DECLARED) {
                    DeclaredType intDeclType = (DeclaredType)tmpMirr;
                    Element intElement = intDeclType.asElement();  
                    if(intElement instanceof TypeElement){
                        tmpEl = (TypeElement) intElement;
                        if(haveInHierarchy(tmpEl, name)) {
                            return true;
                        }
                    }
            }
        }
        return false;
    }
    
    ITypeRepository getTypeRepository() {
        return repository;
    }
    
    private TypeElement getTypeElement(final PersistentObject po){
        if(((TypeRepository) repository).isValid()){
            return po.getTypeElement();
        } else {
            return null;
        }
    }
}
