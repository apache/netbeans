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

import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;

/**
 * The "faces-config" element is the root of the configuration
 * information hierarchy, and contains nested elements for all
 * of the other configuration settings.
 * @author Petr Pisl
 */

public interface FacesConfig extends JSFConfigComponent, IdentifiableElement {
    
    /**
     * Property for &lt;managed-bean&gt; element
     */
    String MANAGED_BEAN = JSFConfigQNames.MANAGED_BEAN.getLocalName();
    /**
     * Property of &lt;navigation-rule&gt; element
     */
    String NAVIGATION_RULE = JSFConfigQNames.NAVIGATION_RULE.getLocalName();
    /**
     * Property of &lt;converter&gt; element
     */
    String CONVERTER = JSFConfigQNames.CONVERTER.getLocalName();
    
    /**
     * Property of &lt;application&gt; element
     */
    String APPLICATION = JSFConfigQNames.APPLICATION.getLocalName();
    
    /**
     * Property of &lt;ordering&gt; element
     */
    String ORDERING = JSFConfigQNames.ORDERING.getLocalName();
    
    /**
     * Property of &lt;absolute-ordering&gt; element
     */
    String ABSOLUTE_ORDERING =JSFConfigQNames.ABSOLUTE_ORDERING.getLocalName();
    
    /**
     * Property of &lt;factory&gt; element
     */
    String FACTORY =JSFConfigQNames.FACTORY.getLocalName();
    
    /**
     * Property of &lt;component&gt; element
     */
    String COMPONENT =JSFConfigQNames.FACTORY.getLocalName();
    
    /**
     * Property of &lt;name&gt; element.
     */
    String NAME = JSFConfigQNames.NAME.getLocalName();
    
    /**
     * Property of &lt;referenced-bean&gt; element.
     */
    String REFERENCED_BEAN = JSFConfigQNames.REFERENCED_BEAN.getLocalName();
    
    /**
     * Property of &lt;referenced-bean&gt; element.
     */
    String RENDER_KIT = JSFConfigQNames.RENDER_KIT.getLocalName();
    
    /**
     * Property of &lt;lifecycle&gt; element.
     */
    String LIFECYCLE= JSFConfigQNames.LIFECYCLE.getLocalName();
    
    /**
     * Property of &lt;validator&gt; element.
     */
    String VALIDATOR= JSFConfigQNames.VALIDATOR.getLocalName();
    
    /**
     * Property of &lt;faces-config-extension&gt; element.
     */
    String FACES_CONFIG_EXTENSION= JSFConfigQNames.FACES_CONFIG_EXTENSION.getLocalName();
    
    /**
     * Property of &lt;behavior&gt; element.
     */
    String BEHAVIOR= JSFConfigQNames.BEHAVIOR.getLocalName();
    
    
    /**
     * Attribute &lt;metadata-complete&gt; element.
     */
    String METADATA_COMPLETE = "metadata-complete";     // NOI18N
    
    /**
     * Attribute &lt;version&gt; element.
     */
    String VERSION = "version";                         // NOI18N

    /**
     * Attribute &lt;faces-flow-definition&gt; element.
     */
    String FLOW_DEFINITION = JSFConfigQNames.FLOW_DEFINITION.getLocalName();

    /**
     * Attribute &lt;protected-views&gt; element.
     */
    String PROTECTED_VIEWS = JSFConfigQNames.PROTECTED_VIEWS.getLocalName();
    
    List<Ordering> getOrderings();
    void addOrdering(Ordering ordering);
    void removeOrdering(Ordering ordering);
    
    List<AbsoluteOrdering> getAbsoluteOrderings();
    void addAbsoluteOrdering(AbsoluteOrdering ordering);
    void removeAbsoluteOrdering(AbsoluteOrdering ordering);
    
    List<Factory> getFactories();
    void addFactories( Factory factory );
    void removeFactory( Factory factory );
    
    List<Component> getComponents();
    void addComponent( FacesComponent component );
    void removeComponent( FacesComponent component );
    
    List<Name> getNames();
    void addName( Name name );
    void removeName(Name name );
    
    List<ReferencedBean> getReferencedBeans();
    void addReferencedBean( ReferencedBean bean );
    void removeReferencedBean( ReferencedBean bean);
    
    List<RenderKit> getRenderKits();
    void addRenderKit( RenderKit kit );
    void removeRenderKit( RenderKit kit );
    
    List<Lifecycle> getLifecycles();
    void addLifecycle( Lifecycle lifecycle );
    void removeLifecycle( Lifecycle lifecycle );
    
    List<FacesValidator> getValidators();
    void addValidator( FacesValidator validator );
    void removeValidator( FacesValidator validator );
    
    List<FacesConfigExtension> getFacesConfigExtensions();
    void addFacesConfigExtension( FacesConfigExtension extension );
    void removeFacesConfigExtension( FacesConfigExtension extension );
    
    List<Converter> getConverters();
    void addConverter(Converter converter);
    void removeConverter(Converter converter);
    
    List <ManagedBean> getManagedBeans();
    void addManagedBean(ManagedBean bean);
    void removeManagedBean(ManagedBean bean);
    
    List<NavigationRule> getNavigationRules();
    void addNavigationRule(NavigationRule rule);
    void removeNavigationRule(NavigationRule rule);
    
    List<Application> getApplications();
    void addApplication(Application application);
    void removeApplication(Application application);
    
    List<FacesBehavior> getBehaviors();
    void addBehavior( FacesBehavior behavior );
    void removeBehavior( FacesBehavior behavior );

    List<FlowDefinition> getFlowDefinitions();
    void addFlowDefinition(FlowDefinition flowDefinition);
    void removeFlowDefinition(FlowDefinition flowDefinition);

    List<ProtectedViews> getProtectedViews();
    void addProtectedView(ProtectedViews protectedView);
    void removeProtectedView(ProtectedViews protectedView);
    
    void addFacesConfigElement( int index, FacesConfigElement element );
    List<FacesConfigElement> getFacesConfigElements();
    
    Boolean isMetaDataComplete();
    void setMetaDataComplete( Boolean isMetadataComplete);
    
    String getVersion();
    void setVersion(String version);
}
