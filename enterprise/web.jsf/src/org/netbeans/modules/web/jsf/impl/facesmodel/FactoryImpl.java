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

import java.util.List;

import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.w3c.dom.Element;


/**
 * @author ads
 *
 */
class FactoryImpl extends IdentifiableComponentImpl implements Factory {

    FactoryImpl( JSFConfigModelImpl model, Element element ) {
        super(model, element);
    }

    FactoryImpl( JSFConfigModelImpl model ) {
        super(model, createElementNS(model, JSFConfigQNames.FACTORY));
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addApplicationFactory(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory)
     */
    public void addApplicationFactory( ApplicationFactory factory ) {
        appendChild( APPLICATION_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addElement(int, org.netbeans.modules.web.jsf.api.facesmodel.FactoryElement)
     */
    public void addElement( int index, FactoryElement element ) {
        insertAtIndex( getPropName( element ), element, index);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addExceptionHandlerFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory)
     */
    public void addExceptionHandlerFactory( ExceptionHandlerFactory factory ) {
        appendChild(EXCEPTION_HANDLER_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addExternalContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory)
     */
    public void addExternalContextFactory( ExternalContextFactory factory ) {
        appendChild(EXTERNAL_CONTEXT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addFacesContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory)
     */
    public void addFacesContextFactory( FacesContextFactory factory ) {
        appendChild(FACES_CONTEXT_FACTORY, factory);
    }

    public void addFaceletCacheFactory( FaceletCacheFactory factory ) {
        appendChild(FACELET_CACHE_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addFactoryExtension(org.netbeans.modules.web.jsf.api.facesmodel.FactoryExtension)
     */
    public void addFactoryExtension( FactoryExtension extension ) {
        appendChild(FACTORY_EXTENSION, extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addLifecycleFactory(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory)
     */
    public void addLifecycleFactory( LifecycleFactory factory ) {
        appendChild(LIFECYCLE_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addPartialViewContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory)
     */
    public void addPartialViewContextFactory( PartialViewContextFactory factory )
    {
        appendChild(PARTIAL_VIEW_CONTEXT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addRenderKitFactory(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory)
     */
    public void addRenderKitFactory( RenderKitFactory factory ) {
        appendChild(RENDER_KIT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addTagHandlerDelegateFactory(org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory)
     */
    public void addTagHandlerDelegateFactory( TagHandlerDelegateFactory factory )
    {
        appendChild(TAG_HANDLER_DELEGATE_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addViewDeclarationLanguageFactory(org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory)
     */
    public void addViewDeclarationLanguageFactory(
            ViewDeclarationLanguageFactory factory )
    {
        appendChild(VIEW_DECLARATION_LANGUAGE_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#addVisitContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory)
     */
    public void addVisitContextFactory( VisitContextFactory factory ) {
        appendChild(VISIT_CONTEXT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getApplicationFactories()
     */
    public List<ApplicationFactory> getApplicationFactories() {
        return getChildren(ApplicationFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getElements()
     */
    public List<FactoryElement> getElements() {
        return getChildren( FactoryElement.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getExceptionHandlerFactories()
     */
    public List<ExceptionHandlerFactory> getExceptionHandlerFactories() {
        return getChildren( ExceptionHandlerFactory.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getExternalContextFactories()
     */
    public List<ExternalContextFactory> getExternalContextFactories() {
        return getChildren(ExternalContextFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getFacesContextFactories()
     */
    public List<FacesContextFactory> getFacesContextFactories() {
        return getChildren(FacesContextFactory.class);
    }

    public List<FaceletCacheFactory> getFaceletCacheFactories() {
        return getChildren(FaceletCacheFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getFactoryExtensions()
     */
    public List<FactoryExtension> getFactoryExtensions() {
        return getChildren(FactoryExtension.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getLifecycleFactories()
     */
    public List<LifecycleFactory> getLifecycleFactories() {
        return getChildren(LifecycleFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getPartialViewContextFactories()
     */
    public List<PartialViewContextFactory> getPartialViewContextFactories() {
        return getChildren( PartialViewContextFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getRenderKitFactories()
     */
    public List<RenderKitFactory> getRenderKitFactories() {
        return getChildren( RenderKitFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getTagHandlerDelegateFactories()
     */
    public List<TagHandlerDelegateFactory> getTagHandlerDelegateFactories() {
        return getChildren( TagHandlerDelegateFactory.class );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getViewDeclarationLanguageFactories()
     */
    public List<ViewDeclarationLanguageFactory> getViewDeclarationLanguageFactories()
    {
        return getChildren(ViewDeclarationLanguageFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#getVisitContextFactories()
     */
    public List<VisitContextFactory> getVisitContextFactories() {
        return getChildren(VisitContextFactory.class);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeApplicationFactory(org.netbeans.modules.web.jsf.api.facesmodel.ApplicationFactory)
     */
    public void removeApplicationFactory( ApplicationFactory factory ) {

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeExceptionHandlerFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExceptionHandlerFactory)
     */
    public void removeExceptionHandlerFactory( ExceptionHandlerFactory factory )
    {
        removeChild(EXCEPTION_HANDLER_FACTORY, factory );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeExternalContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.ExternalContextFactory)
     */
    public void removeExternalContextFactory( ExternalContextFactory factory ) {
        removeChild( EXTERNAL_CONTEXT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeFacesContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.FacesContextFactory)
     */
    public void removeFacesContextFactory( FacesContextFactory factory ) {
        removeChild( FACES_CONTEXT_FACTORY, factory ) ;
    }

    public void removeFaceletCacheFactory(FaceletCacheFactory factory) {
        removeChild(FACELET_CACHE_FACTORY, factory) ;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeFactoryExtension(org.netbeans.modules.web.jsf.api.facesmodel.FactoryExtension)
     */
    public void removeFactoryExtension( FactoryExtension extension ) {
        removeChild(FACTORY_EXTENSION,  extension);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeLifecycleFactory(org.netbeans.modules.web.jsf.api.facesmodel.LifecycleFactory)
     */
    public void removeLifecycleFactory( LifecycleFactory factory ) {
        removeChild( LIFECYCLE_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removePartialViewContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.PartialViewContextFactory)
     */
    public void removePartialViewContextFactory(
            PartialViewContextFactory factory )
    {
        removeChild( PARTIAL_VIEW_CONTEXT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeRenderKitFactory(org.netbeans.modules.web.jsf.api.facesmodel.RenderKitFactory)
     */
    public void removeRenderKitFactory( RenderKitFactory factory ) {
        removeChild( RENDER_KIT_FACTORY, factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeTagHandlerDelegateFactory(org.netbeans.modules.web.jsf.api.facesmodel.TagHandlerDelegateFactory)
     */
    public void removeTagHandlerDelegateFactory(
            TagHandlerDelegateFactory factory )
    {
        removeChild( TAG_HANDLER_DELEGATE_FACTORY , factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeViewDeclarationLanguageFactory(org.netbeans.modules.web.jsf.api.facesmodel.ViewDeclarationLanguageFactory)
     */
    public void removeViewDeclarationLanguageFactory(
            ViewDeclarationLanguageFactory factory )
    {
        removeChild( VIEW_DECLARATION_LANGUAGE_FACTORY, factory );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.Factory#removeVisitContextFactory(org.netbeans.modules.web.jsf.api.facesmodel.VisitContextFactory)
     */
    public void removeVisitContextFactory( VisitContextFactory factory ) {
        removeChild( VISIT_CONTEXT_FACTORY , factory);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigComponent#accept(org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigVisitor)
     */
    public void accept( JSFConfigVisitor visitor ) {
        visitor.visit(this);
    }

    private String getPropName( FactoryElement element ) {
        if ( element instanceof ApplicationFactory ){
            return APPLICATION_FACTORY;
        }
        else if ( element instanceof ExceptionHandlerFactory ){
            return EXCEPTION_HANDLER_FACTORY;
        }
        else if ( element instanceof ExternalContextFactory ){
            return EXTERNAL_CONTEXT_FACTORY;
        }
        else if ( element instanceof FacesContextFactory ){
            return FACES_CONTEXT_FACTORY;
        }
        else if (element instanceof FaceletCacheFactory){
            return FACELET_CACHE_FACTORY;
        }
        else if ( element instanceof FactoryExtension ){
            return FACTORY_EXTENSION;
        }
        else if ( element instanceof LifecycleFactory ){
            return LIFECYCLE_FACTORY;
        }
        else if ( element instanceof PartialViewContextFactory){
            return PARTIAL_VIEW_CONTEXT_FACTORY;
        }
        else if ( element instanceof RenderKitFactory) {
            return RENDER_KIT_FACTORY;
        }
        else if ( element instanceof TagHandlerDelegateFactory ){
            return TAG_HANDLER_DELEGATE_FACTORY;
        }
        else if ( element instanceof ViewDeclarationLanguageFactory){
            return VIEW_DECLARATION_LANGUAGE_FACTORY;
        }
        else if ( element instanceof VisitContextFactory){
            return VISIT_CONTEXT_FACTORY;
        }
        assert false;
        return null;
    }

    @Override
    public List<FlashFactory> getFlashFactory() {
        return getChildren(FlashFactory.class);
    }

    @Override
    public void addFlashFactory(FlashFactory flashFactory) {
        appendChild(FLASH_FACTORY, flashFactory);
    }

    @Override
    public void removeFlashFactory(FlashFactory flashFactory) {
        removeChild(FLASH_FACTORY, flashFactory);
    }

    @Override
    public List<FlowHandlerFactory> getFlowHandlerFactory() {
        return getChildren(FlowHandlerFactory.class);
    }

    @Override
    public void addFlowHandlerFactory(FlowHandlerFactory flowHandlerFactory) {
        appendChild(FLOW_HANDLER_FACTORY, flowHandlerFactory);
    }

    @Override
    public void removeFlowHandlerFactory(FlowHandlerFactory flowHandlerFactory) {
        removeChild(FLOW_HANDLER_FACTORY, flowHandlerFactory);
    }

}
