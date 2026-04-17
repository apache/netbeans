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
package org.netbeans.modules.web.beans.api.model;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.modules.web.beans.impl.model.WebBeansModelProviderImpl;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;


/**
 * @author ads
 *
 */
public final class WebBeansModel {
    
    WebBeansModel( AbstractModelImplementation impl ){
        myImpl = impl;
    }

    /**
     * Find injectable elements that could be used for given injection point.
     * 
     * <code>parentType</code> parameter could be a null . In this case 
     * type definition which contains <code>element</code> is used as parentType.  
     * This parameter is needed when <code>element</code> is defined in 
     * superclass and this superclass is generic. In this case <code>element</code>
     * type ( TypeMirror ) could vary respectively subclass definition ( it could uses
     * real class in generic type parameter ). Type of element in this case
     * is not just <code>element.asType()<code>. It is 
     * <code>CompilationInfo.getTypes().asMemberOf(parentType,element)<code>.
     * This is significant difference. 
     * 
     * Return value depends on injection point type.
     * Injection point could be defined via 
     * programmatic lookup which is dynamically specify injectable type.
     * Such situation appears when injection point uses Instance<?> interface. 
     * In case of @Any binding usage this list will contain all 
     * possible binding types for <code>element</code>  ( all beans 
     * that implements or extends type parameter for Instance<> ). 
     * 
     * See <code>parentType</code> parameter explanation in 
     * {@link #lookupInjectables(VariableElement, DeclaredType)}.
     * 
     * @param element injection point
     * @param parentType parent type of <code>element</code>
     * @return search result information
     */
    public DependencyInjectionResult lookupInjectables( VariableElement element , 
            DeclaredType parentType, AtomicBoolean cancel)
    {
        return getProvider().lookupInjectables(element, parentType, cancel);
    }
    
    /**
     * Test if variable element is injection point.
     * <pre> 
     * Two cases possible here:
     * - element has @Inject annotation
     * - element is parameter of method which is annotated with @Inject 
     * </pre> 
     * 
     * @param element element for check
     * @return true if element is simple injection point
     * @throws WebBeansModelException if <code>element</code> could be injection 
     * point but something wrong ( f.e. it has bindings and has no @Produces 
     * annotation bit it is initialized ).
     * @throws InjectionPointDefinitionError if element definition contains error   
     */
    public boolean isInjectionPoint( VariableElement element )  
        throws InjectionPointDefinitionError
    {
        return getProvider().isInjectionPoint(element);
    }
    
    /**
     * Test if variable element is event injection point.
     * 
     * @param element element for check
     * @return true if element is event injection point
     */
    public boolean isEventInjectionPoint( VariableElement element )  
    {
        TypeMirror elementType = element.asType();
        Element typeElement = getCompilationController().
            getTypes().asElement( elementType);
        if ( typeElement instanceof TypeElement ){
            String typeElementFqn = ((TypeElement)typeElement).getQualifiedName().
                toString();
            if ( WebBeansModelProviderImpl.EVENT_INTERFACE.equals( typeElementFqn )
                    || WebBeansModelProviderImpl.EVENT_INTERFACE_JAKARTA.equals( typeElementFqn )){
                try {
                    return isInjectionPoint(element);
                }
                catch(InjectionPointDefinitionError e ){
                    return false;
                }
            }
        }
        return false;
    }
    
    /**
     * Test if variable element is injection point that is used for
     * programmatic lookup. It could happen if variable declared via 
     * Instance<?> interface with qualifier annotations.
     * Typesafe resolution in this case could not be done 
     * statically and method 
     * {@link #lookupInjectables1(VariableElement, DeclaredType)} should
     * be used to access to possible bean types.
     * @param element  element for check
     * @return true if element is dynamic injection point
     */
    public boolean isDynamicInjectionPoint( VariableElement element ) {
        return getProvider().isDynamicInjectionPoint(element);
    }
    
    /**
     * Access to @Named elements. Method {@link #getName(Element)} 
     * should be used for getting name of element. 
     * @return list of elements annotated with @Named
     */
    public List<Element> getNamedElements(){
        return getProvider().getNamedElements( new AtomicBoolean(false) );
    }

    public boolean isCdi11OrLater() {
        return getProvider().isCdi11OrLater();
    }

    /**
     * Returns name of element if it annotated with @Named.
     * Otherwise returns null.
     * @param element @Named element
     * @return name of element
     */
    public String getName( Element element ){
        return getProvider().getName( element);
    }
    
    /**
     * This method is used for resolve name to Java model type.
     * One can resolve enclosed elements ( fields , methods ,....  )
     * via Java model API and reference which method returns.  
     * @param fqn fully qualified name of type 
     * @return type with given FQN <code>fqn</code>
     */
    public TypeMirror resolveType(String fqn){
        return getProvider().resolveType(fqn);
    }
    
    public CompilationController getCompilationController(){
        return getProvider().getCompilationController();
    }
    
    /**
     * Returns all qualifiers for <code>element</code>.
     * <code>element</code> could be variable ( injection point , producer field ),
     * type element ( bean type with binding ) and production method. 
     * @param element element with qualifiers
     * @param all if <code>true</code> all annotations ( including inherited by @Specializes ) will be returned
     * @return list of all bindings for <code>element</code>
     */
    public List<AnnotationMirror> getQualifiers( Element element , boolean all){
        return getProvider().getQualifiers( element , all );
    }
    
    /**
     * If <code>element</code> has no declared qualifiers or just @Named 
     * qualifier then it has implicit @Default qualifier.
     *  
     * @param element element with qualifiers
     * @return true if element has @Default qualifier which is not declared explicitly 
     */
    public boolean hasImplicitDefaultQualifier( Element element ){
        return getProvider().hasImplicitDefaultQualifier( element );
    }
    
    /**
     * Returns all observer methods for given injection point <code>element</code>
     * field. 
     * 
    * @param element event injection point
    * @param parentType parent type of <code>element</code>
    * @return list of observer methods that will be notified about event fired by <code>element</code>
    */
    public List<ExecutableElement> getObservers(VariableElement element , 
            DeclaredType parentType)
    {
        return getProvider().getObservers( element , parentType);
    }
    
    /**
     * Returns all event injection points that notifies observer method
     * <code>element</code> on event fire.
     * 
     * @param element observer method
     * @param parentType parent type of <code>element</code>
     * @return list of event injection points that are used for firing event 
     */
    public List<VariableElement> getEventInjectionPoints( ExecutableElement element,
            DeclaredType parentType  )
    {
        return getProvider().getEventInjectionPoints( element , parentType);
    }
    
    /**
     * Returns parameter of method <code>element</code> annotated by @Observes.
     * Null will be returned if method is not observer method.
     * 
     * @param element observer method
     * @param parentType parent type of <code>element</code>
     * @return observer parameter 
     */
    public VariableElement getObserverParameter(ExecutableElement element )
    {
        return getProvider().getObserverParameter( element );
    }
    
    /**
     * Returns Scope FQN for the specified <code>element</code>.
     * @param element element which scope needs to be got
     * @return scope of the element
     */
    public String getScope( Element element ) throws CdiException{
        return getProvider().getScope( element );
    }
    
    /**
     * Returns decorators for given type <code>element</code>.
     * Decorator resolution is described in the 8.3 section of JSR-299 spec:
     * - element bean should be assignable to the @Delegate injection point according special rules
     * - decorator should be also enabled in the beans.xml
     * The latter condition is not checked here. One should ask the
     * BeansModel ( it is accessed via AbstractModelImplementation ) for
     * enabled decorators. 
     * @param element decorated element
     * @return collection of matched decorators
     */
    public Collection<TypeElement> getDecorators( TypeElement element ){
        return getProvider().getDecorators( element );
    }
    
    /**
     * Lookup interceptors ( classes annotated with @Interceptor ) which 
     * are resolved for <code>element</code>.
     * The <code>element</code> could be Class definition ( TypeElment ) 
     * or method ( ExecutableElement ).
     * Interceptors could be applied to the methods only but class 
     * could also have interceptor bindings so this method could be 
     * useful for classes also. 
     * @param element type element or method element
     * @return found interceptors  
     */
    public InterceptorsResult getInterceptors( Element element ){
        return getProvider().getInterceptors( element );
    }
    
    /**
     * Returns interceptor bindings declared for <code>element</code>. 
     * @param element element annotated with interceptor bindings
     * @return interceptor bindings 
     */
    public Collection<AnnotationMirror> getInterceptorBindings( Element element ){
        return getProvider().getInterceptorBindings(element);
    }
    
    public AbstractModelImplementation getModelImplementation(){
        return myImpl;
    }
    
    private WebBeansModelProvider getProvider(){
        return getModelImplementation().getProvider();
    }
    
    private AbstractModelImplementation myImpl;
}
