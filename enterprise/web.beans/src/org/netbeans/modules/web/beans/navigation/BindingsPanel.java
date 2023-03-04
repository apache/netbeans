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
package org.netbeans.modules.web.beans.navigation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.swing.tree.TreePath;

import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelException;
import org.netbeans.modules.web.beans.api.model.CdiException;
import org.netbeans.modules.web.beans.api.model.DependencyInjectionResult;
import org.netbeans.modules.web.beans.api.model.Result;
import org.netbeans.modules.web.beans.api.model.WebBeansModel;
import org.netbeans.modules.web.beans.navigation.actions.ModelActionStrategy.InspectActionId;
import org.netbeans.modules.web.beans.navigation.actions.WebBeansActionHelper;


/**
 * @author ads
 *
 */
public class BindingsPanel extends CDIPanel {
    
    private static final long serialVersionUID = 1230555367053797509L;

    static final String NON_BINDING_MEMBER_ANNOTATION =
        "javax.enterprise.inject.NonBinding";                        // NOI18N

    static final String DEFAULT = "Default";                         // NOI18N

    static final String DEFAULT_QUALIFIER_ANNOTATION = 
        "javax.enterprise.inject."+DEFAULT;                          // NOI18N

    public BindingsPanel( final Object[] subject, MetadataModel<WebBeansModel> metaModel,
            WebBeansModel model, JavaHierarchyModel treeModel, Result result )
    {
        super(treeModel);
        
        myModel = metaModel;
        
        myResult = result;
        setVisibleScope(result instanceof DependencyInjectionResult.ResolutionResult);
        setVisibleStereotypes( result != null );
        
        if ( model == null ){
            try {
                metaModel.runReadAction( new MetadataModelAction<WebBeansModel, Void>() {
                    @Override
                    public Void run( WebBeansModel model ) throws Exception {
                        initCDIContext( subject, model  );
                        return null;
                    }
                });
            }

            catch (MetadataModelException e) {
                Logger.getLogger( CDIPanel.class.getName()).
                    log( Level.WARNING, e.getMessage(), e);
            }            catch (IOException e) {
                Logger.getLogger( CDIPanel.class.getName()).
                    log( Level.WARNING, e.getMessage(), e);
            }
        }
        else {
            initCDIContext( subject, model );
        }
    }
    
    BindingsPanel( Object[] subject, MetadataModel<WebBeansModel> metaModel,
            WebBeansModel model, JavaHierarchyModel treeModel )
    {
        this(subject, metaModel, model, treeModel, null);
    }
    
    protected void setVisibleScope( boolean visible ){
        getScopeComponent().setVisible(visible);
        getScopeLabel().setVisible( visible );
    }
    
    protected void setVisibleStereotypes( boolean visible ){
        getStereotypeLabel().setVisible(visible);
        getStereotypesComponent().setVisible( visible );
    }
    
    protected void setContextElement( Element context,
            CompilationController controller )
    {
        TypeMirror typeMirror  = context.asType();
        setContextType(typeMirror, controller );
    }

    protected void setContextType( TypeMirror typeMirror, 
            CompilationController controller)
    {
        fillElementType(typeMirror, myShortElementName, myFqnElementName, controller);
    }
    
    /*
     * Dialog shows element tree. Bindings (qualifiers) and type are shown for selected 
     * node in this tree. This method is used to access an element which
     * contains bindings (qualifiers) and type.
     * This method is required for derived classes which wants to reuse
     * functionality of this class. Such classes <code>context</code> element
     * could be without required annotations and type (F.e. observer method.
     * It is used as start point for finding its observer parameter ).    
     */
    protected Element getSelectedQualifiedElement( Element context , 
            WebBeansModel model )
    {
        return context;
    }
    
    /*
     * Normally the subject element is context element ( f.e. injection point ).
     * In this case this method returns exactly this element
     * from its context.
     * Subclasses could override this behavior to return some other 
     * element . This element will be used for showing type and bindings (qualifiers). 
     */
    protected Element getSubjectElement ( Element context , 
            WebBeansModel model)
    {
        return context;
    }
    
    @Override
    protected void showSelectedCDI() {
        getSelectedBindingsComponent().setToolTipText(null);
        TreePath treePath = getJavaTree().getSelectionPath();
        if (treePath != null) {
            Object node = treePath.getLastPathComponent();
            if (node instanceof InjectableTreeNode<?>) {
                final ElementHandle<?> elementHandle = 
                    ((InjectableTreeNode<?>)node).getElementHandle();
                try {
                    getModel().runReadAction( new MetadataModelAction<WebBeansModel, Void>() {

                        @Override
                        public Void run( WebBeansModel model ) throws Exception {
                            doShowSelectedCDI(elementHandle, model);
                            return null;
                        }

                    });
                }

                catch (MetadataModelException e) {
                    Logger.getLogger( CDIPanel.class.getName() ).
                        log( Level.WARNING, e.getMessage(), e);
                }                catch (IOException e) {
                    Logger.getLogger( CDIPanel.class.getName() ).
                    log( Level.WARNING, e.getMessage(), e);
                }
                getSelectedBindingsComponent().setCaretPosition(0);
                getSelectedBindingsComponent().setToolTipText(((JavaElement)node).getTooltip());
            }
        }
    }
    
    @Override
    protected void reloadSubjectElement(){
        if ( showFqns() ) {
            getInitialBindingsComponent().setText( getFqnBindings() );
            getInitialElement().setText(getFqnElementName().toString());
        }
        else {
            getInitialBindingsComponent().setText( getShortBindings() );
            getInitialElement().setText( getShortElementName().toString());
        } 
    }
    
    protected StringBuilder getShortElementName(){
        return myShortElementName;
    }
    
    protected StringBuilder getFqnElementName(){
        return myFqnElementName;
    }
    
    protected String getFqnBindings(){
        return myFqnBindings;
    }
    
    protected String getShortBindings(){
        return myShortBindings;
    }
    
    protected void setFqnBindings( String bindings ){
        myFqnBindings = bindings;
    }
    
    protected void setShortBindings( String bindings ){
        myShortBindings = bindings;
    }

    protected void initBindings( WebBeansModel model, Element element ) {
        List<AnnotationMirror> qualifiers = model.getQualifiers( element , true );
        
        StringBuilder fqnBuilder = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        if ( model.hasImplicitDefaultQualifier(element)){
            fqnBuilder.append('@');
            builder.append('@');
            fqnBuilder.append(DEFAULT_QUALIFIER_ANNOTATION);
            builder.append(DEFAULT);
            fqnBuilder.append(", ");           // NOI18N
            builder.append(", ");           // NOI18N
        }
        
        for (AnnotationMirror annotationMirror : qualifiers) {
            appendAnnotationMirror(annotationMirror, fqnBuilder,  true );
            appendAnnotationMirror(annotationMirror, builder,  false );
        }
        if ( fqnBuilder.length() >0 ){
            myFqnBindings  = fqnBuilder.substring(0 , fqnBuilder.length() -2 );
            myShortBindings = builder.substring(0 , builder.length() -2 );
        }
        else {
            // this should never happens actually.
            myFqnBindings = "";
            myShortBindings = "";
        }
        if ( showFqns() ) {
            getInitialBindingsComponent().setText( myFqnBindings );
        }
        else {
            getInitialBindingsComponent().setText( myShortBindings );
        }
    }
    
    protected void appendAnnotationMirror( AnnotationMirror mirror , StringBuilder builder , 
            boolean isFqn )
    {
        DeclaredType type = mirror.getAnnotationType();
        Element annotation = type.asElement();
        
        builder.append('@');
        String annotationName ;
        if ( isFqn ) {
            annotationName= ( annotation instanceof TypeElement )?
                ((TypeElement)annotation).getQualifiedName().toString() : 
                    annotation.getSimpleName().toString();
        }
        else { 
            annotationName = annotation.getSimpleName().toString();
        }
        
        builder.append( annotationName );
        
        appendBindingParamters( mirror , builder );
        
        builder.append(", ");           // NOI18N
    }
    
    protected void doShowSelectedCDI(ElementHandle<?> elementHandle,
            WebBeansModel model ) throws CdiException
    {
        Element element = elementHandle.resolve(
                model.getCompilationController());
        if ( element == null ){
            getSelectedBindingsComponent().setText("");
        }
        else {
            element = getSelectedQualifiedElement( element, model);
            List<AnnotationMirror> bindings = 
                model.getQualifiers(element, true);
            StringBuilder builder = new StringBuilder();
            
            if ( model.hasImplicitDefaultQualifier(element)){
                builder.append('@');
                if (showFqns() ){
                    builder.append(DEFAULT_QUALIFIER_ANNOTATION);
                }
                else {
                    builder.append(DEFAULT);
                }
                builder.append(", ");           // NOI18N
            }
            
            for (AnnotationMirror annotationMirror : bindings) {
                appendAnnotationMirror(annotationMirror, builder,  showFqns() );
            }
            String bindingsString = "";
            if ( builder.length() >0 ){
                bindingsString = builder.substring(0 , 
                        builder.length() -2 );
            }
            getSelectedBindingsComponent().setText( bindingsString);
            setScope(model, element);
            setStereotypes(model, element);
        }
    }
    
    protected void setStereotypes( WebBeansModel model, Element element )
            throws CdiException
    {
        if (getResult() != null) {
            List<AnnotationMirror> stereotypes = getResult().getAllStereotypes(
                    element);
            if (stereotypes.isEmpty()) {
                getStereotypesComponent().setText("");
                return;
            }
            StringBuilder text = new StringBuilder();
            boolean isFqn = showFqns();
            for (AnnotationMirror stereotype : stereotypes) {
                appendAnnotationMirror(stereotype, text, isFqn);
            }
            getStereotypesComponent().setText(text.substring(0, text.length() - 2));
        }
    }
    
    protected void setScope( WebBeansModel model, Element element )
            throws CdiException
    {
        if (getResult() instanceof DependencyInjectionResult.ResolutionResult) {
            String scope = model.getScope(element);
            if (scope == null) {
                return;
            }
            String text = "";
            if (showFqns()) {
                text = "@" + scope; // NOI8N
            }
            else {
                TypeMirror scopeType = model.resolveType(scope);
                if (scopeType != null) {
                    Element scopeElement = model.getCompilationController()
                            .getTypes().asElement(scopeType);
                    if (scopeElement instanceof TypeElement) {
                        Name name = ((TypeElement) scopeElement)
                                .getSimpleName();
                        text = "@" + name.toString();
                    }
                }
            }
            getScopeComponent().setText(text);
        }
    }
    
    private void initCDIContext( Object[] subject, WebBeansModel model ) {
        Element element = null;
        if ( subject[2] == InspectActionId.INJECTABLES_CONTEXT ){
            element = WebBeansActionHelper.findVariable(model, subject);
        }
        else {
            element = ((ElementHandle<?>)subject[0]).resolve( 
                    model.getCompilationController());
        }
        Element context = getSubjectElement(element, model);
        if ( context == null ){
            return;
        }
        
        myShortElementName = new StringBuilder();
        myFqnElementName = new StringBuilder();
        setContextElement(context, model.getCompilationController());
        
        initBindings(model, context);
        
        reloadSubjectElement();
    }
    
    private void appendBindingParamters( AnnotationMirror mirror,
            StringBuilder builder )
    {
        Map<? extends ExecutableElement, ? extends AnnotationValue> 
            elementValues = mirror.getElementValues();
        StringBuilder params = new StringBuilder();
        for ( Entry<? extends ExecutableElement, ? extends AnnotationValue> 
            entry :  elementValues.entrySet()) 
        {
            ExecutableElement key = entry.getKey();
            AnnotationValue value = entry.getValue();
            List<? extends AnnotationMirror> annotationMirrors = 
                key.getAnnotationMirrors();
            boolean nonBinding = false;
            for (AnnotationMirror annotationMirror : annotationMirrors) {
                DeclaredType annotationType = annotationMirror.getAnnotationType();
                Element element = annotationType.asElement();
                if ( ( element instanceof TypeElement ) && 
                        ((TypeElement)element).getQualifiedName().
                        contentEquals(NON_BINDING_MEMBER_ANNOTATION))
                {
                    nonBinding = true;
                    break;
                }
            }
            if ( !nonBinding ){
                params.append( key.getSimpleName().toString() );
                params.append( "=" );               // NOI18N
                if ( value.getValue() instanceof String ){
                    params.append('"');
                    params.append( value.getValue().toString());
                    params.append('"');
                }
                else {
                    params.append( value.getValue().toString());
                }
                params.append(", ");                // NOI18N
            }
        }
        if ( params.length() >0 ){
            builder.append( "(" );                   // NOI18N
            builder.append( params.substring(0 , params.length() -2 ));
            builder.append( ")" );                   // NOI18N
        }
    }
    
    private Result getResult() {
        return myResult;
    }
    
    private MetadataModel<WebBeansModel> getModel(){
        return myModel;
    }
    
    static void fillElementType( TypeMirror typeMirror, StringBuilder shortName,
            StringBuilder fqnName , CompilationController controller)
    {
        if ( typeMirror.getKind().isPrimitive()){
            shortName.append( typeMirror.getKind().toString().toLowerCase());
            fqnName.append(  shortName );
            return;
        }
        if ( typeMirror.getKind() == TypeKind.ARRAY ){
            fillArrayType( typeMirror , shortName, fqnName , controller );
            shortName = shortName.append("[]");     // NOI18N
            fqnName = fqnName.append("[]");         // NOI18N
        }
        Element element = controller.getTypes().asElement( typeMirror );
        if ( element != null ){
            fqnName.append( (element instanceof TypeElement )?
                    ((TypeElement)element).getQualifiedName().toString() :
                        element.getSimpleName().toString());
            shortName.append(element.getSimpleName().toString());
        }
    }
    
    static void fillArrayType( TypeMirror typeMirror, StringBuilder shortName,
            StringBuilder fqnName , CompilationController controller )
    {
        TypeMirror componentType = ((ArrayType)typeMirror).getComponentType();
        fillElementType(componentType, shortName , fqnName , controller);
    }
    
    private StringBuilder myFqnElementName;
    private StringBuilder myShortElementName;
    
    private String myFqnBindings;
    private String myShortBindings;
    
    private MetadataModel<WebBeansModel> myModel;
    
    private Result myResult;

}
