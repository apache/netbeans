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

package org.netbeans.modules.web.jsf.api.facesmodel;

import java.util.List;

import org.netbeans.modules.web.jsf.api.metamodel.SystemEventListener;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "application" element provides a mechanism to define the
 * various per-application-singleton implementation artifacts for
 * a particular web application that is utilizing 
 * JavaServer Faces.  For nested elements that are not specified, 
 * the JSF implementation must provide a suitable default.
 * 
 * @author Petr Pisl, ads
 */
public interface Application extends FacesConfigElement, IdentifiableElement {
    /**
     * Property name of &lt;view-handler&gt; element.
     */ 
    String VIEW_HANDLER = JSFConfigQNames.VIEW_HANDLER.getLocalName();
    
    /**
     * Property name of &lt;locale-config&gt; element.
     */ 
    String LOCALE_CONFIG = JSFConfigQNames.LOCALE_CONFIG.getLocalName();
    
    /**
     * Property name of &lt;resource-bundle&gt; element.
     */ 
    String RESOURCE_BUNDLE = JSFConfigQNames.RESOURCE_BUNDLE.getLocalName();
    
    /**
     * Property name of &lt;action-listener&gt; element.
     */ 
    String ACTION_LISTENER = JSFConfigQNames.ACTION_LISTENER.getLocalName();
    
    /**
     * Property name of &lt;default-render-kit-id&gt; element.
     */ 
    String DEFAULT_RENDER_KIT_ID = JSFConfigQNames.DEFAULT_RENDER_KIT_ID.getLocalName();
    
    /**
     * Property name of &lt;message-bundle&gt; element.
     */ 
    String MESSAGE_BUNDLE = JSFConfigQNames.MESSAGE_BUNDLE.getLocalName();
    
    /**
     * Property name of &lt;navigation-handler&gt; element.
     */ 
    String NAVIGATION_HANDLER = JSFConfigQNames.NAVIGATION_HANDLER.getLocalName();
    
    /**
     * Property name of &lt;partial-traversal&gt; element.
     */ 
    String PARTIAL_TRAVERSAL = JSFConfigQNames.PARTIAL_TRAVERSAL.getLocalName();
    
    /**
     * Property name of &lt;state-manager&gt; element.
     */ 
    String STATE_MANAGER = JSFConfigQNames.STATE_MANAGER.getLocalName();
    
    /**
     * Property name of &lt;el-resolver&gt; element.
     */ 
    String EL_RESOLVER = JSFConfigQNames.EL_RESOLVER.getLocalName();
    
    /**
     * Property name of &lt;system-event-listener&gt; element.
     */ 
    String SYSTEM_EVENT_LISTENER = JSFConfigQNames.SYSTEM_EVENT_LISTENER.getLocalName();
    
    /**
     * Property name of &lt;property-resolver&gt; element.
     */ 
    String PROPERTY_RESOLVER = JSFConfigQNames.PROPERTY_RESOLVER.getLocalName();
    
    /**
     * Property name of &lt;variable-resolver&gt; element.
     */ 
    String VARIABLE_RESOLVER = JSFConfigQNames.VARIABLE_RESOLVER.getLocalName();
    
    /**
     * Property name of &lt;resource-handler&gt; element.
     */ 
    String RESOURCE_HANDLER = JSFConfigQNames.RESOURCE_HANDLER.getLocalName();
    
    /**
     * Property name of &lt;application-extension&gt; element.
     */ 
    String APPLICATION_EXTENSION = JSFConfigQNames.APPLICATION_EXTENSION.getLocalName();
    
    /**
     * Property name of &lt;default-validators&gt; element.
     */ 
    String DEFAULT_VALIDATORS = JSFConfigQNames.DEFAULT_VALIDATORS.getLocalName();

    /**
     * Property name of &lt;resource-library-contracts&gt; element.
     */
    String RESOURCE_LIBRARY_CONTRACTS = JSFConfigQNames.RESOURCE_LIBRARY_CONTRACTS.getLocalName();

    /**
     * This property doesn't present in XML file. It aggregates
     * all possible children for Application.
     * These children are represented as "choice" so 
     * they don't have special order. It means that insertion 
     * by index is possible only over all children .
     * ( index should be considered inside all children component list.
     * This is the opposite to index consideration inside group of
     * elements with the same name ).  
     */
    String APPLICATION_ELEMENT = "application-element";      // NOI18N
    
    
    List<ViewHandler> getViewHandlers();
    void addViewHandler(ViewHandler handler);
    void removeViewHandler(ViewHandler handler);
    
    List<LocaleConfig> getLocaleConfig();
    void addLocaleConfig(LocaleConfig locale);
    void removeLocaleConfig(LocaleConfig locale);
    
    List<ResourceBundle> getResourceBundles();
    void addResourceBundle(ResourceBundle locale);
    void removeResourceBundle(ResourceBundle locale);
    
    List<ActionListener> getActionListeners();
    void addActionListener( ActionListener listener );
    void removeActionListener( ActionListener listener );
    
    List<DefaultRenderKitId> getDefaultRenderKitIds();
    void addDefaultRenderKitId( DefaultRenderKitId id );
    void removeDefaultRenderKitId( DefaultRenderKitId id );
    
    List<MessageBundle> getMessageBundles();
    void addMessageBundle( MessageBundle bundle );
    void removeMessageBundle( MessageBundle bundle );
    
    List<NavigationHandler> getNavigationHandler();
    void addNavigationHandler( NavigationHandler handler );
    void removeNavigationHandler(NavigationHandler handler );
    
    List<PartialTraversal> getPartialTraversals();
    void addPartialTraversal( PartialTraversal traversal );
    void removePartialTraversal( PartialTraversal traversal );
    
    List<StateManager> getStateManagers();
    void addStateMenager( StateManager manager );
    void removeStateMenager( StateManager manager );
    
    List<ElResolver> getElResolvers();
    void addElResolver( ElResolver resolver );
    void removeElResolver( ElResolver resolver );
    
    List<PropertyResolver> getPropertyResolvers();
    void addPropertyResolver( PropertyResolver resolver );
    void removePropertyResolver( PropertyResolver resolver );
    
    List<VariableResolver> getVariableResolvers();
    void addVariableResolver( VariableResolver resolver );
    void removeVariableResolver( VariableResolver resolver );
    
    List<ResourceHandler> getResourceHandlers();
    void addResourceHandler( ResourceHandler handler);
    void removeResourceHandler( ResourceHandler handler);
    
    List<SystemEventListener> getSystemEventListeners();
    void addSystemEventListener( FacesSystemEventListener listener );
    void removeSystemEventListener( FacesSystemEventListener listener );
    
    List<ApplicationExtension> getApplicationExtensions();
    void addApplicationExtension( ApplicationExtension extension );
    void removeApplicationExtension( ApplicationExtension extension );
    
    List<DefaultValidators> getDefaultValidators();
    void addDefaultValidators( DefaultValidators validators );
    void removeDefaultValidators( DefaultValidators validators );

    List<ResourceLibraryContracts> getResourceLibraryContracts();
    void addResourceLibraryContract(ResourceLibraryContracts resourceLibraryContracts);
    void removeResourceLibraryContract(ResourceLibraryContracts resourceLibraryContracts);

    List<ApplicationElement> getApplicationElements();
    void addApplicationElement( int index , ApplicationElement element );
}
