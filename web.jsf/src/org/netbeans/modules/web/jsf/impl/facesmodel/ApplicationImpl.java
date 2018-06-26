 /*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
