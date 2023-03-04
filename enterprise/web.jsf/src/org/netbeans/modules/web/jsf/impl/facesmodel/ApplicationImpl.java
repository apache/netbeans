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

package org.netbeans.modules.web.jsf.impl.facesmodel;

import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.ActionListener;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement;
import org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators;
import org.netbeans.modules.web.jsf.api.facesmodel.ElResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal;
import org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceBundle;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.StateManager;
import org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver;
import org.netbeans.modules.web.jsf.api.facesmodel.ViewHandler;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts;
import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;
import org.w3c.dom.Element;


/**
 *
 * @author Petr Pisl, ads
 */
class ApplicationImpl extends IdentifiableComponentImpl 
    implements Application 
{

    /** Creates a new instance of CondverterImpl */
    ApplicationImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    ApplicationImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.APPLICATION));
    }
    
    public List<ViewHandler> getViewHandlers() {
        return getChildren(ViewHandler.class);
    }

    public void addViewHandler(ViewHandler handler) {
        appendChild(VIEW_HANDLER, handler);
    }

    public void removeViewHandler(ViewHandler handler) {
        removeChild(VIEW_HANDLER, handler);
    }

    public List<LocaleConfig> getLocaleConfig() {
        return getChildren(LocaleConfig.class);
    }

    public void addLocaleConfig(LocaleConfig locale) {
        appendChild(LOCALE_CONFIG, locale);
    }

    public void removeLocaleConfig(LocaleConfig locale) {
        removeChild(LOCALE_CONFIG, locale);
    }

    public List<ResourceBundle> getResourceBundles() {
        return getChildren(ResourceBundle.class);
    }

    public void addResourceBundle(ResourceBundle resourceBundle) {
        appendChild(RESOURCE_BUNDLE, resourceBundle);
    }

    public void removeResourceBundle(ResourceBundle resourceBundle) {
        removeChild(RESOURCE_BUNDLE, resourceBundle);
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addActionListener(org.netbeans.modules.web.jsf.api.facesmodel.ActionListener)
     */
    public void addActionListener( ActionListener listener ) {
        appendChild( ACTION_LISTENER, listener );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addApplicationExtension(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension)
     */
    public void addApplicationExtension( ApplicationExtension extension ) {
        appendChild( APPLICATION_EXTENSION, extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addDefaultRenderKitId(org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId)
     */
    public void addDefaultRenderKitId( DefaultRenderKitId id ) {
        appendChild( DEFAULT_RENDER_KIT_ID, id );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addElResolver(org.netbeans.modules.web.jsf.api.facesmodel.ElResolver)
     */
    public void addElResolver( ElResolver resolver ) {
        appendChild( EL_RESOLVER, resolver );        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addMessageBundle(org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle)
     */
    public void addMessageBundle( MessageBundle bundle ) {
        appendChild( MESSAGE_BUNDLE, bundle );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addNavigationHandler(org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler)
     */
    public void addNavigationHandler( NavigationHandler handler ) {
        appendChild( NAVIGATION_HANDLER, handler );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addPartialTraversal(org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal)
     */
    public void addPartialTraversal( PartialTraversal traversal ) {
        appendChild( PARTIAL_TRAVERSAL, traversal );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addPropertyResolver(org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver)
     */
    public void addPropertyResolver( PropertyResolver resolver ) {
        appendChild( PROPERTY_RESOLVER, resolver);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addResourceHandler(org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler)
     */
    public void addResourceHandler( ResourceHandler handler ) {
        appendChild( RESOURCE_HANDLER, handler );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addStateMenager(org.netbeans.modules.web.jsf.api.facesmodel.StateManager)
     */
    public void addStateMenager( StateManager manager ) {
        appendChild( MESSAGE_BUNDLE, manager );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addSystemEventListener(org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener)
     */
    public void addSystemEventListener( FacesSystemEventListener listener ) {
        appendChild( SYSTEM_EVENT_LISTENER, listener );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addVariableResolver(org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver)
     */
    public void addVariableResolver( VariableResolver resolver ) {
        appendChild( VARIABLE_RESOLVER, resolver );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getActionListeners()
     */
    public List<ActionListener> getActionListeners() {
        return getChildren( ActionListener.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getApplicationElements()
     */
    public List<ApplicationElement> getApplicationElements() {
        return getChildren( ApplicationElement.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getApplicationExtensions()
     */
    public List<ApplicationExtension> getApplicationExtensions() {
        return getChildren( ApplicationExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getDefaultRenderKitIds()
     */
    public List<DefaultRenderKitId> getDefaultRenderKitIds() {
        return getChildren( DefaultRenderKitId.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getElResolvers()
     */
    public List<ElResolver> getElResolvers() {
        return getChildren( ElResolver.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getPartialTraversals()
     */
    public List<PartialTraversal> getPartialTraversals() {
        return getChildren( PartialTraversal.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getPropertyResolvers()
     */
    public List<PropertyResolver> getPropertyResolvers() {
        return getChildren( PropertyResolver.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getResourceHandlers()
     */
    public List<ResourceHandler> getResourceHandlers() {
        return getChildren( ResourceHandler.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getStateManagers()
     */
    public List<StateManager> getStateManagers() {
        return getChildren( StateManager.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getSystemEventListeners()
     */
    public List<SystemEventListener> getSystemEventListeners() {
        List<FacesSystemEventListener> list = getChildren( FacesSystemEventListener.class );
        List<SystemEventListener> result = new LinkedList<SystemEventListener>();
        result.addAll( list );
        AbstractJsfModel model = getModel().getModelSource().getLookup().lookup(
                AbstractJsfModel.class);
        if ( model != null ){
            result.addAll( model.getSystemEventListeners() );
        }
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getVariableResolvers()
     */
    public List<VariableResolver> getVariableResolvers() {
        return getChildren( VariableResolver.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeActionListener(org.netbeans.modules.web.jsf.api.facesmodel.ActionListener)
     */
    public void removeActionListener( ActionListener listener ) {
        removeChild(ACTION_LISTENER, listener );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeApplicationExtension(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationExtension)
     */
    public void removeApplicationExtension( ApplicationExtension extension ) {
        removeChild( APPLICATION_EXTENSION, extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeDefaultRenderKitId(org.netbeans.modules.web.jsf.api.facesmodel.DefaultRenderKitId)
     */
    public void removeDefaultRenderKitId( DefaultRenderKitId id ) {
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeElResolver(org.netbeans.modules.web.jsf.api.facesmodel.ElResolver)
     */
    public void removeElResolver( ElResolver resolver ) {
        removeChild( EL_RESOLVER, resolver);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeMessageBundle(org.netbeans.modules.web.jsf.api.facesmodel.MessageBundle)
     */
    public void removeMessageBundle( MessageBundle bundle ) {
        removeChild( MESSAGE_BUNDLE, bundle);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeNavigationHandler(org.netbeans.modules.web.jsf.api.facesmodel.NavigationHandler)
     */
    public void removeNavigationHandler( NavigationHandler handler ) {
        removeChild( NAVIGATION_HANDLER, handler);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removePartialTraversal(org.netbeans.modules.web.jsf.api.facesmodel.PartialTraversal)
     */
    public void removePartialTraversal( PartialTraversal traversal ) {
        removeChild( PARTIAL_TRAVERSAL, traversal);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removePropertyResolver(org.netbeans.modules.web.jsf.api.facesmodel.PropertyResolver)
     */
    public void removePropertyResolver( PropertyResolver resolver ) {
        removeChild( PROPERTY_RESOLVER, resolver);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeResourceHandler(org.netbeans.modules.web.jsf.api.facesmodel.ResourceHandler)
     */
    public void removeResourceHandler( ResourceHandler handler ) {
        removeChild( RESOURCE_HANDLER, handler); 
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeStateMenager(org.netbeans.modules.web.jsf.api.facesmodel.StateManager)
     */
    public void removeStateMenager( StateManager manager ) {
        removeChild( STATE_MANAGER, manager);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeSystemEventListener(org.netbeans.modules.web.jsf.api.facesmodel.FacesSystemEventListener)
     */
    public void removeSystemEventListener( FacesSystemEventListener listener ) {
        removeChild( SYSTEM_EVENT_LISTENER, listener);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeVariableResolver(org.netbeans.modules.web.jsf.api.facesmodel.VariableResolver)
     */
    public void removeVariableResolver( VariableResolver resolver ) {
        removeChild( VARIABLE_RESOLVER,resolver);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#setefaultValidators(org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators)
     */
    public void addDefaultValidators( DefaultValidators validators ) {
       appendChild( DEFAULT_VALIDATORS, validators );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getDefaultValidators()
     */
    public List<DefaultValidators> getDefaultValidators() {
        return getChildren( DefaultValidators.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getMessageBundles()
     */
    public List<MessageBundle> getMessageBundles() {
        return getChildren( MessageBundle.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#getNavigationHandler()
     */
    public List<NavigationHandler> getNavigationHandler() {
        return getChildren( NavigationHandler.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#removeDefaultValidators(org.netbeans.modules.web.jsf.api.facesmodel.DefaultValidators)
     */
    public void removeDefaultValidators( DefaultValidators validators ) {
        removeChild( DEFAULT_VALIDATORS ,  validators);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Application#addApplicationElement(int, org.netbeans.modules.web.jsf.api.facesmodel.ApplicationElement)
     */
    public void addApplicationElement( int index, ApplicationElement element ) {
        insertAtIndex( APPLICATION_ELEMENT, element, index);
    }

    @Override
    public List<ResourceLibraryContracts> getResourceLibraryContracts() {
        return getChildren(ResourceLibraryContracts.class);
    }

    @Override
    public void addResourceLibraryContract(ResourceLibraryContracts contracts) {
        appendChild(RESOURCE_LIBRARY_CONTRACTS, contracts);
    }

    @Override
    public void removeResourceLibraryContract(ResourceLibraryContracts contracts) {
        removeChild(RESOURCE_LIBRARY_CONTRACTS, contracts);
    }

}
