/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jakarta.web.beans.impl.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;


/**
 * @author ads
 *
 */
class TypeProductionFilter extends Filter<Element> {
 
    private TypeProductionFilter(  ){
    }
    
    static TypeProductionFilter get( ){
        // could be cached via ThreadLocal attribute
        return new TypeProductionFilter();
    }
    
    void init( TypeMirror elementType , Element injectionPoint ,
            WebBeansModelImplementation model)
    {
        myImpl = model;
        myType = elementType;
        myOriginalElement = injectionPoint;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.jakarta.web.beans.impl.model.Filter#filterElements(java.util.Set)
     */
    @Override
    void filter( Set<Element> productionElements ){
        if ( filterPrimitives(productionElements ) ){
            //fillSimpleResult(productionElements);
            return;
        }
        
        if ( filterArray(productionElements) ){
            //fillSimpleResult(productionElements);
            return ;
        }
        
        TypeBindingFilter filter = TypeBindingFilter.get();
        filter.init(getElementType(), getOriginalElement(), getImplementation());
        
        // this cycle care only about declared types.
        for ( Iterator<? extends Element> iterator = productionElements.iterator() ; 
            iterator.hasNext() ; ) 
        {
            Element productionElement = iterator.next();

            TypeMirror mirror = null;
            if (productionElement.getKind() == ElementKind.FIELD) {
                mirror = productionElement.asType();
            }
            else if (productionElement.getKind() == ElementKind.METHOD)
            {
                mirror = ((ExecutableType) productionElement.asType()).getReturnType();
            }
            if ( !filter.isAssignable(mirror, productionElement )){
                iterator.remove();
            }
            
            /*List<DeclaredType> derived = getDerived( enclosingElement);
            
            for (DeclaredType declaredType : derived) {
                TypeMirror mirror = null;
                try {
                    if (productionElement.getKind() == ElementKind.FIELD) {
                        mirror = getImplementation().getHelper()
                                .getCompilationController().getTypes()
                                .asMemberOf(declaredType, productionElement);
                    }
                    else if (productionElement.getKind() == ElementKind.METHOD)
                    {
                        mirror = getImplementation().getHelper()
                                .getCompilationController().getTypes()
                                .asMemberOf(declaredType, productionElement);
                        mirror = ((ExecutableType) mirror).getReturnType();
                    }
                    if ( filter.isAssignable(mirror, productionElement )){
                        addResult( productionElement , declaredType);
                    }
                }
                catch (IllegalArgumentException e) {
                    /*
                     * call <code>asMemberOf</code> could be a problem for
                     * productionElment and derived. In this case just skip
                     *
                    continue;
                }
            }*/
        }
    }
    
    /*private void addResult( Element productionElement, DeclaredType type )
    {
        List<DeclaredType> list = myResult.get( productionElement );
        if ( list == null ){
            list = new ArrayList<DeclaredType>(2);
            myResult.put( productionElement , list );
        }
        list.add( type );
    }
    
    private void fillSimpleResult( Set<Element> productionElements ){
        for (Element element : productionElements) {
            TypeElement enclosingElement = getImplementation().getHelper().
                getCompilationController().getElementUtilities().
                    enclosingTypeElement(element);
            DeclaredType type = (DeclaredType)enclosingElement.asType();
            myResult.put( element , Collections.singletonList(type) );
        }
    }*/
    
    /*
     * From the spec : producer or disposer method is not inherited.
     *                 producer field is not inherited.
     * It means no need to look at the derived classes and inherited 
     * production there. If method is explicitly defined in the 
     * derived class it will be in the original production list
     * as separate element. 
    private List<DeclaredType> getDerived( TypeElement element ) 
    {
        if ( !isGeneric( element ) ){
            return Collections.singletonList( (DeclaredType)element.asType());
        }
        
        Set<TypeElement> implementors = FieldInjectionPointLogic.
            getImplementors(getImplementation(), element);
        List<DeclaredType> result = new ArrayList<DeclaredType>( 
                implementors.size());
        for (TypeElement typeElement : implementors) {
            result.add((DeclaredType)typeElement.asType());
        }
        
        return result;
        
    }
    
    private boolean isGeneric( TypeElement element ) {
        //DeclaredType type = (DeclaredType)element.asType();
        return element.getTypeParameters().size()!=0;
    }*/

    private boolean filterArray( Set<? extends Element> productionElements)
    {
        if  ( getElementType().getKind() == TypeKind.ARRAY ){
            TypeMirror arrayComponentType = ((ArrayType)getElementType()).getComponentType();
            for (Iterator<? extends Element> iterator = productionElements.iterator() ; 
                    iterator.hasNext() ; ) 
            {
                Element productionElement = iterator.next();
                boolean hasBeanType = hasBeanType(arrayComponentType,
                        productionElement);
                if ( !hasBeanType ){
                    iterator.remove();
                }
            }
            return true;
        }
        return false;
    }

    private boolean hasBeanType( TypeMirror arrayComponentType,
            Element productionElement )
    {
        Collection<TypeMirror> restrictedTypes = RestrictedTypedFilter.
            getRestrictedTypes(productionElement, getImplementation());
        if ( restrictedTypes == null  ){
            TypeMirror productionType= null;
            if ( productionElement.getKind() == ElementKind.FIELD){
                productionType = productionElement.asType();
            }
            else if ( productionElement.getKind() == ElementKind.METHOD){
                productionType = ((ExecutableElement)productionElement).
                    getReturnType();
            }
            return checkArrayBeanType(productionType, arrayComponentType);
        }
        Types types = getImplementation().getHelper().
            getCompilationController().getTypes();
        for( TypeMirror restrictedType : restrictedTypes ){
            if ( types.isSameType( restrictedType, getElementType())){
                return true;
            }
        }
        return false;
    }

    private boolean checkArrayBeanType(TypeMirror productionType,
            TypeMirror arrayComponentType)
    {
        if ( productionType == null ){
            return false;
        }
        if ( productionType.getKind() != TypeKind.ARRAY ){
            return false;
        }
        return getImplementation().getHelper().getCompilationController().
                getTypes().isSameType( arrayComponentType,
                        ((ArrayType) productionType).getComponentType());
    }

    private boolean filterPrimitives( Set<? extends Element> productionElements )
    {
        PrimitiveType primitive = null;
        TypeElement boxedType = null;
        if ( getElementType().getKind().isPrimitive() ){
            primitive = getImplementation().getHelper().getCompilationController().
                getTypes().getPrimitiveType( getElementType().getKind());
            boxedType = getImplementation().getHelper().getCompilationController().
                getTypes().boxedClass( primitive);
        }
        else if ( getElementType().getKind() == TypeKind.DECLARED ){
            Element varElement = getImplementation().getHelper().
                getCompilationController().getTypes().asElement( getElementType() );
            if ( varElement instanceof TypeElement ){
                String typeName = ((TypeElement)varElement).getQualifiedName().
                    toString();
                if ( WRAPPERS.contains( typeName )){
                    primitive = getImplementation().getHelper().
                        getCompilationController().getTypes().unboxedType( 
                                varElement.asType());
                    boxedType = (TypeElement)varElement;
                }
                
            }
        }
        
        if ( primitive!= null ){
            for( Iterator<? extends Element> iterator = productionElements.iterator();
                iterator.hasNext(); )
            {
                Element productionElement =iterator.next();
                Types types = getImplementation().getHelper().
                    getCompilationController().getTypes();
                TypeMirror productionType = null;
                if ( productionElement.getKind() == ElementKind.FIELD){
                    productionType = productionElement.asType();
                }
                else if ( productionElement.getKind() == ElementKind.METHOD){
                    productionType = ((ExecutableElement)productionElement).
                        getReturnType();
                }
                 Collection<TypeMirror> restrictedTypes = 
                     RestrictedTypedFilter.getRestrictedTypes(productionElement, 
                        getImplementation());
                /*
                 *  The required type is either primitive or its wrapper.
                 *  It means that bean type should be either the same primitive
                 *  or wrapper. But all wrappers are final so production cannot 
                 *  restrict some child type of wrapper to wrapper. 
                 *  It can restrict only wrapper to wrapper parent.
                 *  In this case the types are not assignable.    
                 */
                boolean isNotRestricted = true;
                if ( restrictedTypes!= null ){
                    isNotRestricted = false;
                    for (TypeMirror restrictedType : restrictedTypes ){
                        if ( types.isSameType( restrictedType, primitive )||
                                types.isSameType( restrictedType, boxedType.asType() ) )
                        {
                            isNotRestricted = true;
                            break;
                        }
                    }
                }
                if ( !isNotRestricted ){
                    iterator.remove();
                }
                else if ( productionType!= null && 
                        !types.isSameType( productionType, primitive ) &&
                        !types.isSameType( productionType , boxedType.asType()))
                {
                    iterator.remove();
                }
            }
        }
        
        return primitive!= null;
    }

    
    private WebBeansModelImplementation getImplementation(){
        return myImpl;
    }
    
    private TypeMirror getElementType(){
        return myType;
    }
    
    private Element getOriginalElement(){
        return myOriginalElement;
    }

    private WebBeansModelImplementation myImpl;
    private TypeMirror myType;
    private Element myOriginalElement;
    
    
    private static final Set<String> WRAPPERS = new HashSet<String>();
    
    static {
        WRAPPERS.add(Boolean.class.getCanonicalName());
        WRAPPERS.add(Byte.class.getCanonicalName());
        WRAPPERS.add(Character.class.getCanonicalName());
        WRAPPERS.add(Double.class.getCanonicalName());
        WRAPPERS.add(Float.class.getCanonicalName());
        WRAPPERS.add(Integer.class.getCanonicalName());
        WRAPPERS.add(Long.class.getCanonicalName());
        WRAPPERS.add(Short.class.getCanonicalName());
    }
}
