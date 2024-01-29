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
package org.netbeans.modules.web.beans.impl.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.ElementUtilities;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationHandler;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.AnnotationModelHelper;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.PersistentObjectManager;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.AnnotationParser;
import org.netbeans.modules.j2ee.metadata.model.api.support.annotation.parser.ParseResult;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.impl.model.results.DefinitionErrorResult;
import org.netbeans.modules.web.beans.impl.model.results.ResultImpl;
import org.netbeans.modules.web.beans.model.spi.WebBeansModelProvider;
import org.openide.util.NbBundle;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.modules.web.beans.analysis.analyzer.AnnotationUtil;
import org.netbeans.modules.web.beans.api.model.BeanArchiveType;

/**
 * @author ads
 */
abstract class FieldInjectionPointLogic {

    static final String PRODUCER_ANNOTATION = 
                    "javax.enterprise.inject.Produces";             // NOI18N

    static final String ANY_QUALIFIER_ANNOTATION = 
                     "javax.enterprise.inject.Any";                 // NOI18N

    static final String DEFAULT_QUALIFIER_ANNOTATION = 
                     "javax.enterprise.inject.Default";             // NOI18N

    static final String NEW_QUALIFIER_ANNOTATION = 
                      "javax.enterprise.inject.New";                // NOI18N
    
    static final String NAMED_QUALIFIER_ANNOTATION = 
                       "javax.inject.Named";                        // NOI18N

    static final String INJECT_ANNOTATION = 
                        "javax.inject.Inject";                      // NOI18N
    
    static final String INSTANCE_INTERFACE = 
                        "javax.enterprise.inject.Instance";         // NOI18N
    
    static final String TYPED_RESTRICTION = 
                        "javax.enterprise.inject.Typed";            // NOI18N
    
    static final String DELEGATE_ANNOTATION =
                        "javax.decorator.Delegate";                 // NOI18N

    static final Logger LOGGER = Logger.getLogger(WebBeansModelProvider.class
            .getName());
    
    
    FieldInjectionPointLogic( WebBeansModelImplementation model) {
        myModel = model;
    }
    
    public abstract TypeMirror resolveType( String fqn ) ;
    
    protected WebBeansModelImplementation getModel(){
        return myModel;
    }
    
    protected DependencyInjectionResult findVariableInjectable( VariableElement element, 
            DeclaredType parentType , ResultLookupStrategy strategy, AtomicBoolean cancel )
    {
        DeclaredType parent = parentType;
        try {
            parent = getParent(element, parentType);
        }
        catch ( DefinitionError e ){
            TypeElement type = e.getElement();
            return new DefinitionErrorResult(element,  parentType, 
                    NbBundle.getMessage(WebBeansModelProviderImpl.class, 
                            "ERR_BadParent", element.getSimpleName(),       // NOI18N
                             type!= null? type.toString(): null));
        }
        
        if(cancel.get()) {
            return null;
        }
        
        TypeMirror elementType = strategy.getType(getModel(), parent , element );
        
        if(elementType instanceof DeclaredType && AnnotationUtil.PROVIDER.equals(""+((DeclaredType)elementType).asElement())) {
            List<? extends TypeMirror> typeArguments = ((DeclaredType)elementType).getTypeArguments();
            if(typeArguments!=null && typeArguments.size()>0) {
                //in case of Provider we need to inspects type argument instead of Provider type, see #245546
                elementType = typeArguments.get(0);
            }
        }
        
        DependencyInjectionResult result  = doFindVariableInjectable(element, elementType, true, cancel);
        return strategy.getResult( getModel() , result, cancel );
    }
    
    protected DeclaredType getParent( Element element , DeclaredType parentType) 
        throws DefinitionError
    {
        DeclaredType parent = parentType;
        if ( parent == null ){
            TypeElement type = getModel().getHelper().getCompilationController().
                getElementUtilities().enclosingTypeElement(element);
            
            boolean isDeclaredType = ( type.asType() instanceof DeclaredType );
            if ( isDeclaredType ){
                parent = (DeclaredType)type.asType();
            }
            else  {
                throw new DefinitionError( type );
            }
        }
        return parent;
    }
    
    protected DependencyInjectionResult doFindVariableInjectable( VariableElement element,
            TypeMirror elementType, boolean injectRequired, AtomicBoolean cancel)
    {
        List<AnnotationMirror> quilifierAnnotations = new LinkedList<AnnotationMirror>();
        boolean anyQualifier = false;
        try {
            anyQualifier = hasAnyQualifier(element,injectRequired, false, 
                    quilifierAnnotations);
        }
        catch(InjectionPointDefinitionError e ){
            return new DefinitionErrorResult(element, elementType, e.getMessage());
        }
        
        /*
         * Single @Default annotation means increasing types that 
         * is eligible for injection. Each bean without any qualifiers
         * type has @Default qualifier by default. So it should
         * be also considered as injectable.  
         */
        boolean defaultQualifier = !anyQualifier && quilifierAnnotations.size() == 0;
        /*
         * The @New target is 
         * @Target(value={FIELD,PARAMETER})
         * and injectable couldn't have any other qualifiers.
         * So @New should be the only qualifier for injection point 
         * and it could be assigned by user to bean type.
         */
        boolean newQualifier = false; 
        String annotationName = null; 
        Set<TypeElement> types = new HashSet<TypeElement>();
        if ( quilifierAnnotations.size() == 1 ){
            AnnotationMirror annotationMirror = quilifierAnnotations.get( 0 );
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( annotationElement != null ){
                annotationName = annotationElement.getQualifiedName().toString();
                defaultQualifier = annotationElement.getQualifiedName().contentEquals( 
                    DEFAULT_QUALIFIER_ANNOTATION);
                newQualifier = annotationElement.getQualifiedName().contentEquals( 
                    NEW_QUALIFIER_ANNOTATION );
            }
        }
        if ( (quilifierAnnotations.size() == 0 && anyQualifier) ||
                defaultQualifier )
        {
            LOGGER.fine("Found built-in binding "+annotationName); // NOI18N
            Set<TypeElement> assignableTypes = cancel.get() ? new HashSet<TypeElement>() : getAssignableTypes( element , 
                    elementType, cancel );
            if ( defaultQualifier ){
                LOGGER.fine("@Default annotation requires test for implementors" +
                        " of varaible type");                      // NOI18N
                /*
                 *  Filter all appropriate types for presence qualifier.
                 *  It should be either absent at all or qualifiers 
                 *  should contain @Default.  
                 */
                filterBindingsByDefault( assignableTypes );
                filterBindingByArchiveType( assignableTypes );
            }
            types.addAll( assignableTypes );
        }
        else if (newQualifier){
            return handleNewQualifier(element, elementType, quilifierAnnotations);
        }
        else {
            /*
             * This is list with types that have all required qualifiers. This
             * list will be used for further typesafe resolution.
             */
            Set<TypeElement> typesWithQualifiers = getBindingTypes(
                    quilifierAnnotations);
            
            filterBindingsByMembers(quilifierAnnotations, typesWithQualifiers, 
                    TypeElement.class );
            
            /*
             * Now <code>typesWithQualifiers</code> contains appropriate types
             * which has required qualifier with required parameters ( if any ).
             * Next step is filter types via typesafe resolution.
             */
            filterBindingsByType( element , elementType, typesWithQualifiers );
            types.addAll( typesWithQualifiers );
        }
        
        /*
         * This is list with production fields or methods ( they have @Produces annotation )
         * that  have all required bindings.
         * This list will be also used for further typesafe resolution. 
         */
        Set<Element> productionElements;
        if ( (quilifierAnnotations.size() == 0 && anyQualifier) || 
                defaultQualifier )
        {
            productionElements = getAllProductions( );
            if ( defaultQualifier ){
                filterDefaultProductions( productionElements );
            }
        }
        else {
            productionElements = getProductions( quilifierAnnotations, cancel); 
            filterBindingsByMembers( quilifierAnnotations , productionElements , 
                     Element.class );
        }
        filterProductionByType( element, elementType, productionElements );
        
        return createResult( element, elementType, types , productionElements );
    }

    protected boolean isQualifier( TypeElement element, 
            AnnotationModelHelper helper, boolean event )
    {
        return AnnotationObjectProvider.isQualifier(element, helper, event);
    }
    
    protected Set<Element> getChildSpecializes( Element productionElement,
            WebBeansModelImplementation model, AtomicBoolean cancel )
    {
        TypeElement typeElement = model.getHelper().getCompilationController()
                .getElementUtilities().enclosingTypeElement(productionElement);
        Set<TypeElement> implementors = getImplementors(model, typeElement, cancel);
        implementors.remove( productionElement.getEnclosingElement());
        Set<Element> specializeElements = new HashSet<Element>();
        specializeElements.add(productionElement);
        for (TypeElement implementor : implementors) {
            if(cancel.get()) {
                break;
            }
            inspectHierarchy(productionElement, implementor,
                    specializeElements, model);
        }
        specializeElements.remove(productionElement);
        return specializeElements;
    }
    
    protected boolean hasAnyQualifier( VariableElement element,boolean injectRequired,
            boolean eventQualifiers, List<AnnotationMirror> quilifierAnnotations ) 
            throws InjectionPointDefinitionError
    {
        List<? extends AnnotationMirror> annotations = 
            getModel().getHelper().getCompilationController().getElements().
            getAllAnnotationMirrors(element);
        boolean isProducer = false;
        
        /* Single @Any annotation means skip searching in qualifiers .
         * One need to check any bean that has required type .
         * @Any qualifier type along with other qualifiers 
         * equivalent to the same list of qualifiers without @Any.
         */
        boolean anyQualifier = false;
        
        boolean hasInject = false;
        
        for (AnnotationMirror annotationMirror : annotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( annotationElement == null ){
                continue;
            }
            if ( ANY_QUALIFIER_ANNOTATION.equals( 
                    annotationElement.getQualifiedName().toString()))
            {
                anyQualifier = true;
            }
            else if ( isQualifier( annotationElement , getModel().getHelper(),
                    eventQualifiers) )
            {
                quilifierAnnotations.add( annotationMirror );
            }
            if ( PRODUCER_ANNOTATION.contentEquals( 
                    annotationElement.getQualifiedName()))
            {
                isProducer = true;
            }
            else if ( INJECT_ANNOTATION.contentEquals( 
                    annotationElement.getQualifiedName()))
            {
                hasInject = true;
            }
        }
        if ( isProducer ){
            throw new InjectionPointDefinitionError(
                    NbBundle.getMessage( WebBeansModelProviderImpl.class, 
                            "ERR_ProducerInjectPoint" ,     // NOI18N
                            element.getSimpleName() ));
        }
        if ( element.asType().getKind() == TypeKind.TYPEVAR ){
            throw new InjectionPointDefinitionError(
                    NbBundle.getMessage( WebBeansModelProviderImpl.class, 
                            "ERR_InjectPointTypeVar" ,           // NOI18N
                            element.getSimpleName() ));
        }
        if ( injectRequired ){
            checkInjectionPoint(element);
        }
        if ( injectRequired && !hasInject ){
            throw new InjectionPointDefinitionError(
                    NbBundle.getMessage( WebBeansModelProviderImpl.class, 
                            "ERR_NoInjectPoint" ,           // NOI18N
                            element.getSimpleName() ));
        }
        return anyQualifier;
    }
    
    private void checkInjectionPoint( VariableElement element ) 
        throws InjectionPointDefinitionError
    {
        CompilationController compilationController = getModel().getHelper().
            getCompilationController();
        Tree tree = compilationController.getTrees().getTree( element );
        if ( tree instanceof VariableTree ){
            VariableTree varTree = (VariableTree)tree;
            ExpressionTree initializer = varTree.getInitializer();
            if ( initializer != null ){
                throw new InjectionPointDefinitionError(NbBundle.getMessage( 
                        FieldInjectionPointLogic.class, 
                        "ERR_InitializedInjectionPoint"));      // NOI18N
            }
        }
        Set<Modifier> modifiers = element.getModifiers();
        if ( modifiers.contains(Modifier.STATIC)){
            throw new InjectionPointDefinitionError(NbBundle.getMessage( 
                    FieldInjectionPointLogic.class, 
                    "ERR_StaticInjectionPoint"));      // NOI18N
        }
        if ( modifiers.contains(Modifier.FINAL)){
            throw new InjectionPointDefinitionError(NbBundle.getMessage( 
                    FieldInjectionPointLogic.class, 
                    "ERR_FinalInjectionPoint"));      // NOI18N
        }
    }

    protected <T extends Element> void filterBindingsByMembers(
            Collection<AnnotationMirror> bindingAnnotations,
            Set<T> elementsWithBindings,  Class<T> clazz)
    {
        MemberBindingFilter<T> filter = MemberBindingFilter.get( clazz );
        filter.init( bindingAnnotations, getModel() );
        filter.filter( elementsWithBindings );
    }
    
    protected void filterBindingsByType( VariableElement element, 
            TypeMirror elementType,Set<TypeElement> typesWithBindings)
    {
        TypeBindingFilter filter = TypeBindingFilter.get();
        filter.init( elementType, element, getModel() );
        filter.filter( typesWithBindings );
    }
    
    protected ResultImpl handleNewQualifier( VariableElement element,
            TypeMirror elementType,List<AnnotationMirror> quilifierAnnotations)
    {
        AnnotationMirror annotationMirror = quilifierAnnotations.get( 0 );
        AnnotationParser parser = AnnotationParser.create( getModel().getHelper());
        parser.expectClass( "value", null);                         // NOI18N 
        ParseResult parseResult = parser.parse(annotationMirror);
        String clazz = parseResult.get( "value" , String.class );   // NOI18N
        
        TypeMirror typeMirror;
        if ( clazz == null ){
            typeMirror = elementType;
        }
        else {
            typeMirror = resolveType( clazz );
        }
        Element typeElement = null;
        if ( typeMirror != null ) {
            typeElement = getModel().getHelper().getCompilationController().
                getTypes().asElement(typeMirror);
        }
        if ( typeElement!= null ){
            /*
             *  No need to look at implementors .
             *  Because they have qualifier @New(X.class) where X their class.
             *  X is binding parameter which should equals to binding 
             *  parameter of @New qualifier for injection point. This
             *  parameter is <code>typeMirror</code> class . So X should
             *  be ONLY typeMirror class which is typeElement.  
             *  types.addAll(getImplementors(modelImpl, typeElement ));
             */
            if( getModel().getHelper().getCompilationController().getTypes().
                    isAssignable(typeMirror, elementType))
            {
                return new ResultImpl(element, elementType , (TypeElement)typeElement , 
                        getModel().getHelper());
            }
        }
        return new ResultImpl(element, elementType, getModel().getHelper());
    }
    
    static Set<TypeElement> getImplementors( WebBeansModelImplementation modelImpl,
            Element typeElement, AtomicBoolean cancel )
    {
        if (! (typeElement instanceof TypeElement )){
            return Collections.emptySet();
        }
        Set<TypeElement> result = new HashSet<TypeElement>();
        result.add( (TypeElement) typeElement );
        
        Set<TypeElement> toProcess = new HashSet<TypeElement>();
        toProcess.add((TypeElement) typeElement );
        while ( toProcess.size() >0 && !cancel.get()){
            TypeElement element = toProcess.iterator().next();
            toProcess.remove( element );
            Set<TypeElement> set = doGetImplementors(modelImpl, element, cancel );
            if ( set.size() == 0 ){
                continue;
            }
            result.addAll( set );
            for (TypeElement impl : set) {
                toProcess.add(impl);
            }
        }
        return result;
    }
    
    private DependencyInjectionResult createResult( VariableElement element, 
            TypeMirror elementType, Set<TypeElement> types, Set<Element> productions )
    {
        return new ResultImpl(element, elementType, types, productions, 
                getModel().getHelper() );
    }
    
    private void inspectHierarchy( Element productionElement,
            TypeElement implementor, Set<Element> specializeElements ,
            WebBeansModelImplementation model )
    {
        List<? extends Element> enclosedElements = implementor.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if ( enclosedElement.getKind() != ElementKind.METHOD) {
                continue;
            }
            if ( !productionElement.getSimpleName().contentEquals(
                    enclosedElement.getSimpleName()))
            {
                continue;
            }
            Set<Element> probableSpecializes = new HashSet<Element>();
            if ( collectSpecializes( productionElement ,
                    (ExecutableElement)enclosedElement , model ,
                    probableSpecializes , specializeElements))
            {
                // for one method there could be just one override method in considered class
                specializeElements.addAll( probableSpecializes );
                return;
            }
        }
    }
    
    private boolean collectSpecializes( Element productionElement,
            ExecutableElement element, WebBeansModelImplementation model,
            Set<Element> probableSpecializes, Set<Element> specializeElements )
    {
        ElementUtilities elementUtilities =
            model.getHelper().getCompilationController().getElementUtilities();
        if ( !elementUtilities.overridesMethod(element)){
            return false;
        }
        ExecutableElement overriddenMethod = elementUtilities.
            getOverriddenMethod( element);
        if ( overriddenMethod == null ){
            return false;
        }
        if (!AnnotationObjectProvider.hasSpecializes(element,  model.getHelper())){
            return false;
        }
        probableSpecializes.add( element);
        if( overriddenMethod.equals( productionElement ) ||
                specializeElements.contains( productionElement))
        {
            return true;
        }
        else {
            return collectSpecializes(productionElement, overriddenMethod, model,
                    probableSpecializes, specializeElements);
        }
    }

    private static Set<TypeElement> doGetImplementors( 
            WebBeansModelImplementation modelImpl, TypeElement typeElement, AtomicBoolean cancel )
    {
        Set<TypeElement> result = new HashSet<TypeElement>();
        ElementHandle<TypeElement> handle = ElementHandle.create(typeElement);
        ClassIndex classIndex = modelImpl
                .getHelper().getClasspathInfo().getClassIndex();
        if(cancel.get()) {
            return Collections.emptySet();
        }
        final Set<ElementHandle<TypeElement>> handles = classIndex
                .getElements(
                        handle,
                        EnumSet.of(SearchKind.IMPLEMENTORS),
                        EnumSet.of(SearchScope.SOURCE,
                                SearchScope.DEPENDENCIES));
        if (handles == null) {
            LOGGER.log(Level.WARNING,
                    "ClassIndex.getElements() was interrupted"); // NOI18N
            return Collections.emptySet();
        }
        for (ElementHandle<TypeElement> elementHandle : handles) {
            if(cancel.get()) {
                return Collections.emptySet();
            }
            LOGGER.log(Level.FINE, "found derived element {0}",
                    elementHandle.getQualifiedName()); // NOI18N
            TypeElement derivedElement = elementHandle.resolve(modelImpl
                    .getHelper().getCompilationController());
            if (derivedElement == null) {
                continue;
            }
            result.add(derivedElement);
        }
        return result;
    }
    
    private void filterDefaultProductions( Set<Element> productionElements ) 
    {
        DefaultBindingTypeFilter<Element> filter = DefaultBindingTypeFilter.get( 
                Element.class);
        filter.init( getModel() );
        filter.filter( productionElements );
    }

    private Set<Element> getAllProductions( ){
        final Set<Element> result = new HashSet<Element>();
        try {
            getModel().getHelper().getAnnotationScanner().findAnnotations( 
                    PRODUCER_ANNOTATION, 
                    EnumSet.of( ElementKind.FIELD, ElementKind.METHOD), 
                    new AnnotationHandler() {
                        @Override
                        public void handleAnnotation( TypeElement type, 
                                Element element,AnnotationMirror annotation )
                        {
                                result.add( element );
                        }
                    });
        }
        catch (InterruptedException e) {
            LOGGER.warning("Finding annotation "+PRODUCER_ANNOTATION+
                    " was interrupted"); // NOI18N
        }
        return result;
    }

    private void filterProductionByType( VariableElement element, 
            TypeMirror elementType, Set<Element> productionElements )
    {
        TypeProductionFilter filter = TypeProductionFilter.get( );
        filter.init( elementType, element, getModel());
        filter.filter( productionElements );
    }
    
    private void filterBindingsByDefault( Set<TypeElement> assignableTypes ){
        DefaultBindingTypeFilter<TypeElement> filter = DefaultBindingTypeFilter.get( 
                TypeElement.class);
        filter.init( getModel() );
        filter.filter( assignableTypes );
    }
    
    private void filterBindingByArchiveType(Set<TypeElement> assignableTypes) {
        ArchiveTypeBindingTypeFilter<TypeElement> filter = ArchiveTypeBindingTypeFilter.get(TypeElement.class);
        filter.init(getModel());
        filter.filter(assignableTypes);
    }

    private Set<TypeElement> getAssignableTypes( VariableElement element,
            TypeMirror elementType, AtomicBoolean cancel )
    {
        if (elementType.getKind() != TypeKind.DECLARED) {
            return Collections.emptySet();
        }
        Element typeElement = ((DeclaredType) elementType).asElement();
        if (!(typeElement instanceof TypeElement)) {
            return Collections.emptySet();
        }
        if (!((TypeElement) typeElement).getTypeParameters().isEmpty()) {
            return getAssignables(  elementType, (TypeElement)typeElement, 
                    element, cancel );
        }
        else {
            Set<TypeElement> implementors = getImplementors(getModel(), typeElement, cancel);
            restrictedTypeFilter( implementors , (TypeElement)typeElement );
            return implementors;
        }
    }
    
    private void restrictedTypeFilter( Set<TypeElement> allImplementors , 
            TypeElement originalElement  ) {
        RestrictedTypedFilter filter = new RestrictedTypedFilter();
        filter.init( originalElement , getModel());
        filter.filter( allImplementors );
    }

    private Set<TypeElement> getAssignables(  TypeMirror elementType, 
            TypeElement typeElement  , VariableElement element, AtomicBoolean cancel) 
    {
        Set<TypeElement> result = getImplementors(getModel(), typeElement, cancel);
        
        // Now filter all found child classes according to real element type ( type mirror )  
        TypeBindingFilter filter = TypeBindingFilter.get();
        filter.init( elementType, element, getModel() );
        filter.filter( result );
        return result;
    }

    /*
     * Method finds production elements which have appropriate binding types.
     */
    private Set<Element> getProductions( 
            List<AnnotationMirror> qualifierAnnotations, AtomicBoolean cancel) 
    {
        List<Set<Element>> bindingCollections = 
            new ArrayList<Set<Element>>( qualifierAnnotations.size());
        /*
         * One need to handle special case with @Default annotation 
         * in case of specialization. There can be a case 
         * when production method doesn't explicitly declare @Default but 
         * specialize other method with several appropriate qualifiers.
         * In this case original method will have @Default along with 
         * qualifiers "inherited" from specialized methods.  
         */
        boolean hasDefault = getModel().getHelper().getAnnotationsByType( 
                qualifierAnnotations ).get(DEFAULT_QUALIFIER_ANNOTATION) != null ;
        Set<Element> currentBindings = new HashSet<Element>();
        for (AnnotationMirror annotationMirror : qualifierAnnotations) {
            if(cancel.get()) {
                currentBindings.clear();
                break;
            }
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement)type.asElement();
            if ( annotationElement == null ){
                continue;
            }
            String annotationFQN = annotationElement.getQualifiedName().toString();
            findAnnotation( bindingCollections, annotationFQN , hasDefault,
                    currentBindings, cancel);
        }

        if ( hasDefault ){
            bindingCollections.add( currentBindings );
        }
        
        Set<Element> result= null;
        for ( int i=0; i<bindingCollections.size() ; i++ ){
            Set<Element> list = bindingCollections.get(i);
            if ( i==0 ){
                result = list;
            }
            else {
                result.retainAll( list );
            }
        }
        if ( result == null ){
            return Collections.emptySet();
        }
        return result;
    }

    private void findAnnotation( final List<Set<Element>> bindingCollections, 
            final String annotationFQN ,final boolean hasCurrent , 
            final Set<Element> currentBindings,
            final AtomicBoolean cancel)
    {
        try {
            final Set<Element> bindings = new HashSet<Element>();
            getModel().getHelper().getAnnotationScanner().findAnnotations( 
                    annotationFQN, 
                    EnumSet.of( ElementKind.FIELD, ElementKind.METHOD), 
                    new AnnotationHandler() {
                        @Override
                        public void handleAnnotation( TypeElement type, 
                                Element element,AnnotationMirror annotation )
                                {
                                    if (AnnotationObjectProvider.hasAnnotation(
                                            element, PRODUCER_ANNOTATION,
                                            getModel().getHelper()))
                                    {
                                        bindings.add(element);
                                        bindings.addAll(getChildSpecializes(
                                                element, getModel(), cancel));
                                        if (annotationFQN
                                                .contentEquals(DEFAULT_QUALIFIER_ANNOTATION))
                                        {
                                            currentBindings.addAll(bindings);
                                        }
                                        else {
                                            bindingCollections.add(bindings);
                                        }
                                    }
                                }
                    });
            if ( hasCurrent ){
                for (Element element : bindings) {
                    if ( AnnotationObjectProvider.checkDefault(
                            element, getModel().getHelper()))
                    {
                        currentBindings.add( element );
                    }
                }
            }
        }
        catch (InterruptedException e) {
            LOGGER.warning("Finding annotation "+annotationFQN+
                    " was interrupted"); // NOI18N
        }
    }

    /*
     * Method finds type elements which have appropriate binding types.
     */
    private Set<TypeElement> getBindingTypes( List<AnnotationMirror> qualifierAnnotations ){
        List<Set<BindingQualifier>> bindingCollections = 
            new ArrayList<Set<BindingQualifier>>( qualifierAnnotations.size());

        /*
         * One need to handle special case with @Default annotation 
         * in case of specialization. There can be a case 
         * when bean doesn't explicitly declare @Default but 
         * specializes other beans with several appropriate qualifiers.
         * In this case original bean will have @Default along with 
         * qualifiers "inherited" from specialized beans.  
         */
        boolean hasDefault = getModel().getHelper().getAnnotationsByType( 
                qualifierAnnotations ).get(DEFAULT_QUALIFIER_ANNOTATION) != null ;
        Set<BindingQualifier> defaultQualifiers = new HashSet<BindingQualifier>();
        for (AnnotationMirror annotationMirror : qualifierAnnotations) {
            DeclaredType type = annotationMirror.getAnnotationType();
            TypeElement annotationElement = (TypeElement) type.asElement();
            if ( annotationElement == null ){
                continue;
            }
            String annotationFQN = annotationElement.getQualifiedName()
                    .toString();
            PersistentObjectManager<BindingQualifier> manager = getModel()
                    .getManager(annotationFQN);
            Collection<BindingQualifier> bindings = manager.getObjects();
            if (annotationFQN.contentEquals(DEFAULT_QUALIFIER_ANNOTATION)) {
                defaultQualifiers.addAll(bindings);
            }
            else {
                bindingCollections.add(new HashSet<BindingQualifier>(bindings));
                if (hasDefault) {
                    for (BindingQualifier binding : bindings) {
                        if (AnnotationObjectProvider
                                .checkDefault(binding.getTypeElement(),
                                        getModel().getHelper()))
                        {
                            defaultQualifiers.add(new BindingQualifier(
                                    getModel().getHelper(), binding
                                            .getTypeElement(),
                                    DEFAULT_QUALIFIER_ANNOTATION));
                        }
                    }
                }
            }
        }
        
        if ( hasDefault ){
            bindingCollections.add( defaultQualifiers );
        }
        
        Set<BindingQualifier> result= null;
        for ( int i=0; i<bindingCollections.size() ; i++ ){
            Set<BindingQualifier> set = bindingCollections.get(i);
            if ( i==0 ){
                result = set;
            }
            else {
                result.retainAll( set );
            }
        }
        if ( result == null ){
            return Collections.emptySet();
        }
        else {
            Set<TypeElement> set = new HashSet<TypeElement>();
            for (BindingQualifier binding : result) {
                set.add( binding.getTypeElement() );
            }
            return set;
        }
    }

    protected static class InjectionPointDefinitionError extends Exception{
        private static final long serialVersionUID = -1568276063434281036L;

        private InjectionPointDefinitionError(String msg){
            super( msg );
        }
    }
    
    protected static class DefinitionError extends Exception {
        
        private static final long serialVersionUID = 8538541504206293629L;

        protected DefinitionError( TypeElement element ){
            myElement = element;
        }
        
        public TypeElement getElement(){
            return myElement;
        }
        
        private TypeElement myElement; 
    }
    private WebBeansModelImplementation myModel;
}
