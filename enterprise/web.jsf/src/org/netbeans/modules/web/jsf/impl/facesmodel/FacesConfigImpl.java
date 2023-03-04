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
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.netbeans.modules.web.jsf.api.metamodel.Component;
import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
public class FacesConfigImpl extends IdentifiableComponentImpl implements FacesConfig{
    
    
    /** Creates a new instance of FacesConfigImpl */
    public FacesConfigImpl(JSFConfigModelImpl model, Element element) {
        super(model, element);
    }
    
    public FacesConfigImpl(JSFConfigModelImpl model) {
        this(model, createElementNS(model, JSFConfigQNames.FACES_CONFIG));
    }
    
    public List<ManagedBean> getManagedBeans() {
        return getChildren(ManagedBean.class);
    }
    
    public void addManagedBean(ManagedBean bean) {
        appendChild(MANAGED_BEAN, bean);
    }
    
    public void removeManagedBean(ManagedBean bean) {
        removeChild(MANAGED_BEAN, bean);
    }
    
    public List<NavigationRule> getNavigationRules() {
        return getChildren(NavigationRule.class);
    }
    
    public void addNavigationRule(NavigationRule rule) {
        appendChild(NAVIGATION_RULE, rule);
    }
    
    public void removeNavigationRule(NavigationRule rule) {
        removeChild(NAVIGATION_RULE, rule);
    }
    
    public List<Converter> getConverters() {
        return getChildren(Converter.class);
    }
    
    public void addConverter(Converter converter) {
        appendChild(CONVERTER, converter);
    }

    public void removeConverter(Converter converter) {
        removeChild(CONVERTER, converter);
    }
    
    public List<Application> getApplications() {
        return getChildren(Application.class);
    }

    public void addApplication(Application application) {
        appendChild(APPLICATION, application);
    }


    public void removeApplication(Application application) {
        removeChild(APPLICATION, application);
    }
    
    public void accept(JSFConfigVisitor visitor) {
        visitor.visit(this);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getVersion()
     */
    public String getVersion() {
        return getAttribute( FacesAttributes.VERSION );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#isMetaDataComplete()
     */
    public Boolean isMetaDataComplete() {
        String value = getAttribute( FacesAttributes.METADATA_COMPLETE );
        return value == null ? null : Boolean.valueOf( value );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#setMetaDataComplete(java.lang.Boolean)
     */
    public void setMetaDataComplete( Boolean isMetadataComplete ) {
        setAttribute( FacesAttributes.METADATA_COMPLETE.getName(),
                FacesAttributes.METADATA_COMPLETE ,isMetadataComplete==null?
                        null: isMetadataComplete.toString().toLowerCase());
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#setVersion(java.lang.String)
     */
    public void setVersion( String version ) {
        setAttribute( FacesAttributes.VERSION.getName(),FacesAttributes.VERSION ,
                version);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addAbsoluteOrdering(org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering)
     */
    public void addAbsoluteOrdering( AbsoluteOrdering ordering ) {
        appendChild( ABSOLUTE_ORDERING, ordering );
        
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addFactories(org.netbeans.modules.web.jsf.api.facesmodel.Factory)
     */
    public void addFactories( Factory factory ) {
        appendChild( FACTORY, factory );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addOrdering(org.netbeans.modules.web.jsf.api.facesmodel.Ordering)
     */
    public void addOrdering( Ordering ordering ) {
        appendChild( ORDERING, ordering );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getAbsoluteOrderings()
     */
    public List<AbsoluteOrdering> getAbsoluteOrderings() {
        return getChildren(AbsoluteOrdering.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getFactories()
     */
    public List<Factory> getFactories() {
        return getChildren( Factory.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getOrderings()
     */
    public List<Ordering> getOrderings() {
        return getChildren( Ordering.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeAbsoluteOrdering(org.netbeans.modules.web.jsf.api.facesmodel.AbsoluteOrdering)
     */
    public void removeAbsoluteOrdering( AbsoluteOrdering ordering ) {
        removeChild( ABSOLUTE_ORDERING, ordering );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeFactory(org.netbeans.modules.web.jsf.api.facesmodel.Factory)
     */
    public void removeFactory( Factory factory ) {
        removeChild( FACTORY, factory );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeOrdering(org.netbeans.modules.web.jsf.api.facesmodel.Ordering)
     */
    public void removeOrdering( Ordering ordering ) {
        removeChild( ORDERING, ordering );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addBehavior(org.netbeans.modules.web.jsf.api.facesmodel.Behavior)
     */
    public void addBehavior( FacesBehavior behavior ) {
        appendChild( BEHAVIOR, behavior );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addFacesConfigElement(int, org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigElement)
     */
    public void addFacesConfigElement( int index, FacesConfigElement element ) {
        insertAtIndex(FACTORY, element, index, FacesConfigElement.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getBehaviors()
     */
    public List<FacesBehavior> getBehaviors() {
        return getChildren( FacesBehavior.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeBehavior(org.netbeans.modules.web.jsf.api.facesmodel.Behavior)
     */
    public void removeBehavior( FacesBehavior behavior ) {
        removeChild( BEHAVIOR, behavior );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addComponent(org.netbeans.modules.web.jsf.api.facesmodel.Component)
     */
    public void addComponent( FacesComponent component ) {
        appendChild(COMPONENT, component);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addFacesConfigExtension(org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigExtension)
     */
    public void addFacesConfigExtension( FacesConfigExtension extension ) {
        appendChild( FACES_CONFIG_EXTENSION, extension );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addLifecycle(org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle)
     */
    public void addLifecycle( Lifecycle lifecycle ) {
        appendChild(LIFECYCLE, lifecycle );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
     */
    public void addName( Name name ) {
        appendChild(NAME, name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addReferencedBean(org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean)
     */
    public void addReferencedBean( ReferencedBean bean ) {
        appendChild( REFERENCED_BEAN, bean);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addRenderKit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKit)
     */
    public void addRenderKit( RenderKit kit ) {
        appendChild( RENDER_KIT, kit);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#addValidator(org.netbeans.modules.web.jsf.api.facesmodel.Validator)
     */
    public void addValidator( FacesValidator validator ) {
        appendChild(VALIDATOR, validator );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getComponents()
     */
    public List<Component> getComponents() {
        List<FacesComponent> list = getChildren( FacesComponent.class );
        List<Component> result = new LinkedList<Component>( list );
        return result;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getFacesConfigExtensions()
     */
    public List<FacesConfigExtension> getFacesConfigExtensions() {
        return getChildren( FacesConfigExtension.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getLifecycles()
     */
    public List<Lifecycle> getLifecycles() {
        return getChildren( Lifecycle.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getNames()
     */
    public List<Name> getNames() {
        return getChildren( Name.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getReferencedBeans()
     */
    public List<ReferencedBean> getReferencedBeans() {
        return getChildren( ReferencedBean.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getRenderKits()
     */
    public List<RenderKit> getRenderKits() {
        return getChildren( RenderKit.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getValidators()
     */
    public List<FacesValidator> getValidators() {
        return getChildren( FacesValidator.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeComponent(org.netbeans.modules.web.jsf.api.facesmodel.Component)
     */
    public void removeComponent( FacesComponent component ) {
        removeChild(  COMPONENT, component);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeFacesConfigExtension(org.netbeans.modules.web.jsf.api.facesmodel.FacesConfigExtension)
     */
    public void removeFacesConfigExtension( FacesConfigExtension extension ) {
        removeChild(FACES_CONFIG_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeLifecycle(org.netbeans.modules.web.jsf.api.facesmodel.Lifecycle)
     */
    public void removeLifecycle( Lifecycle lifecycle ) {
        removeChild( LIFECYCLE, lifecycle);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeName(org.netbeans.modules.web.jsf.api.facesmodel.Name)
     */
    public void removeName( Name name ) {
        removeChild(NAME, name);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeReferencedBean(org.netbeans.modules.web.jsf.api.facesmodel.ReferencedBean)
     */
    public void removeReferencedBean( ReferencedBean bean ) {
        removeChild(REFERENCED_BEAN, bean);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeRenderKit(org.netbeans.modules.web.jsf.api.facesmodel.RenderKit)
     */
    public void removeRenderKit( RenderKit kit ) {
        removeChild( RENDER_KIT , kit );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#removeValidator(org.netbeans.modules.web.jsf.api.facesmodel.Validator)
     */
    public void removeValidator( FacesValidator validator ) {
        removeChild( VALIDATOR, validator);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig#getFacesConfigElements()
     */
    public List<FacesConfigElement> getFacesConfigElements() {
        return getChildren(FacesConfigElement.class);
    }

    @Override
    public List<FlowDefinition> getFlowDefinitions() {
        return getChildren(FlowDefinition.class);
    }

    @Override
    public void addFlowDefinition(FlowDefinition facesFlowDefinition) {
        appendChild(FLOW_DEFINITION, facesFlowDefinition);
    }

    @Override
    public void removeFlowDefinition(FlowDefinition facesFlowDefinition) {
        removeChild(FLOW_DEFINITION, facesFlowDefinition);
    }

    @Override
    public List<ProtectedViews> getProtectedViews() {
        return getChildren(ProtectedViews.class);
    }

    @Override
    public void addProtectedView(ProtectedViews protectedView) {
        appendChild(PROTECTED_VIEWS, protectedView);
    }

    @Override
    public void removeProtectedView(ProtectedViews protectedView) {
        removeChild(PROTECTED_VIEWS, protectedView);
    }
}
