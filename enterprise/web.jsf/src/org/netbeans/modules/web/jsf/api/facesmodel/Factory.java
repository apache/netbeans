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

import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigQNames;


/**
 * @author ads
 *
 */
public interface Factory extends FacesConfigElement, IdentifiableElement {

    /**
     * Property name of &lt;application-factory&gt; element.
     */
    String APPLICATION_FACTORY = JSFConfigQNames.APPLICATION_FACTORY.getLocalName();

    /**
     * Property name of &lt;exception-handler-factory&gt; element.
     */
    String EXCEPTION_HANDLER_FACTORY = JSFConfigQNames.EXCEPTION_HANDLER_FACTORY.getLocalName();

    /**
     * Property name of &lt;external-context-factory&gt; element.
     */
    String EXTERNAL_CONTEXT_FACTORY = JSFConfigQNames.EXTERNAL_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;faces-context-factory&gt; element.
     */
    String FACES_CONTEXT_FACTORY = JSFConfigQNames.FACES_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;facelet-cache-factory&gt; element.
     */
    String FACELET_CACHE_FACTORY = JSFConfigQNames.FACELET_CACHE_FACTORY.getLocalName();

    /**
     * Property name of &lt;partial-view-context-factory&gt; element.
     */
    String PARTIAL_VIEW_CONTEXT_FACTORY = JSFConfigQNames.PARTIAL_VIEW_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;lifecycle-factory&gt; element.
     */
    String LIFECYCLE_FACTORY = JSFConfigQNames.LIFECYCLE_FACTORY.getLocalName();

    /**
     * Property name of &lt;view-declaration-language-factory&gt; element.
     */
    String VIEW_DECLARATION_LANGUAGE_FACTORY = JSFConfigQNames.VIEW_DECLARATION_LANGUAGE_FACTORY.getLocalName();

    /**
     * Property name of &lt;tag-handler-delegate-factory&gt; element.
     */
    String TAG_HANDLER_DELEGATE_FACTORY = JSFConfigQNames.TAG_HANDLER_DELEGATE_FACTORY.getLocalName();

    /**
     * Property name of &lt;render-kit-factory&gt; element.
     */
    String RENDER_KIT_FACTORY = JSFConfigQNames.RENDER_KIT_FACTORY.getLocalName();

    /**
     * Property name of &lt;visit-context-factory&gt; element.
     */
    String VISIT_CONTEXT_FACTORY = JSFConfigQNames.VISIT_CONTEXT_FACTORY.getLocalName();

    /**
     * Property name of &lt;factory-extension&gt; element.
     */
    String FACTORY_EXTENSION = JSFConfigQNames.FACTORY_EXTENSION.getLocalName();

    /**
     * Property name of &lt;flash-factory&gt; element.
     */
    String FLASH_FACTORY = JSFConfigQNames.FLASH_FACTORY.getLocalName();

    /**
     * Property name of &lt;flow-handler-factory&gt; element.
     */
    String FLOW_HANDLER_FACTORY = JSFConfigQNames.FLOW_HANDLER_FACTORY.getLocalName();

    List<FactoryElement> getElements();
    void addElement( int index , FactoryElement element );

    List<ApplicationFactory> getApplicationFactories();
    void addApplicationFactory( ApplicationFactory factory );
    void removeApplicationFactory( ApplicationFactory factory );

    List<ExceptionHandlerFactory> getExceptionHandlerFactories();
    void addExceptionHandlerFactory( ExceptionHandlerFactory factory );
    void removeExceptionHandlerFactory( ExceptionHandlerFactory factory );

    List<ExternalContextFactory> getExternalContextFactories();
    void addExternalContextFactory( ExternalContextFactory factory  );
    void removeExternalContextFactory( ExternalContextFactory factory );

    List<FacesContextFactory> getFacesContextFactories();
    void addFacesContextFactory( FacesContextFactory factory );
    void removeFacesContextFactory( FacesContextFactory factory );

    List<FaceletCacheFactory> getFaceletCacheFactories();
    void addFaceletCacheFactory( FaceletCacheFactory factory );
    void removeFaceletCacheFactory( FaceletCacheFactory factory );

    List<PartialViewContextFactory> getPartialViewContextFactories();
    void addPartialViewContextFactory( PartialViewContextFactory factory );
    void removePartialViewContextFactory( PartialViewContextFactory factory );

    List<LifecycleFactory> getLifecycleFactories();
    void addLifecycleFactory( LifecycleFactory factory );
    void removeLifecycleFactory(LifecycleFactory factory );

    List<ViewDeclarationLanguageFactory> getViewDeclarationLanguageFactories();
    void addViewDeclarationLanguageFactory( ViewDeclarationLanguageFactory factory );
    void removeViewDeclarationLanguageFactory( ViewDeclarationLanguageFactory factory );

    List<TagHandlerDelegateFactory> getTagHandlerDelegateFactories();
    void addTagHandlerDelegateFactory(TagHandlerDelegateFactory factory );
    void removeTagHandlerDelegateFactory( TagHandlerDelegateFactory factory );

    List<RenderKitFactory> getRenderKitFactories();
    void addRenderKitFactory( RenderKitFactory factory );
    void removeRenderKitFactory( RenderKitFactory factory );

    List<VisitContextFactory> getVisitContextFactories();
    void addVisitContextFactory( VisitContextFactory factory );
    void removeVisitContextFactory( VisitContextFactory factory );

    List<FactoryExtension> getFactoryExtensions();
    void addFactoryExtension(FactoryExtension extension);
    void removeFactoryExtension( FactoryExtension extension );

    List<FlashFactory> getFlashFactory();
    void addFlashFactory(FlashFactory flashFactory);
    void removeFlashFactory(FlashFactory flashFactory);

    List<FlowHandlerFactory> getFlowHandlerFactory();
    void addFlowHandlerFactory(FlowHandlerFactory flowHandlerFactory);
    void removeFlowHandlerFactory(FlowHandlerFactory flowHandlerFactory);
}
