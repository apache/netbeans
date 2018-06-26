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
