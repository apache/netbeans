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

import org.w3c.dom.Element;

/**
 *
 * @author Petr Pisl, ads
 */
public interface JSFConfigComponentFactory {

    JSFConfigComponent create(Element element, JSFConfigComponent context);

    FacesConfig createFacesConfig();
    ManagedBean createManagedBean();
    NavigationRule createNavigationRule();
    NavigationCase createNavigationCase();
    Converter createConverter();
    Description createDescription();
    DisplayName createDisplayName();
    Icon createIcon();
    Application createApplication();
    ViewHandler createViewHandler();
    LocaleConfig createLocaleConfig();
    DefaultLocale createDefatultLocale();
    SupportedLocale createSupportedLocale();
    ResourceBundle createResourceBundle();
    ActionListener createActionListener();
    DefaultRenderKitId createDefaultRenderKitId();
    MessageBundle createMessageBundle();
    NavigationHandler createNavigationHandler();
    PartialTraversal createPartialTraversal();
    StateManager createStateManager();
    ElResolver createElResolver();
    PropertyResolver createPropertyResolver();
    VariableResolver createVariableResolver();
    ResourceHandler createResourceHandler();
    FacesSystemEventListener createSystemEventListener();
    DefaultValidators createDefaultValidators();
    Ordering createOrdering();
    After createAfter();
    Before createBefore();
    Name createName();
    Others createOthers();
    AbsoluteOrdering createAbsoluteOrdering();
    Factory createFactory();
    FacesValidatorId createValidatorId();
    ApplicationFactory createApplicationFactory ();
    ExceptionHandlerFactory createExceptionHandlerFactory();
    ExternalContextFactory createExternalContextFactory( );
    FacesContextFactory createFacesContextFactory();
    FaceletCacheFactory createFaceletCacheFactory();
    PartialViewContextFactory createPartialViewContextFactory( );
    LifecycleFactory createLifecycleFactory();
    ViewDeclarationLanguageFactory createViewDeclarationLanguageFactory( );
    TagHandlerDelegateFactory createTagHandlerDelegateFactory( );
    RenderKitFactory createRenderKitFactory( );
    VisitContextFactory createVisitContextFactory( );
    FacesComponent createComponent();
    Facet createFacet();
    ConfigAttribute createAttribute();
    Property createProperty();
    FacesManagedProperty createManagedProperty();
    ListEntries createListEntries();
    MapEntries createMapEntries();
    If createIf();
    Redirect createRedirect();
    ViewParam createViewParam();
    ReferencedBean createReferencedBean();
    RenderKit createRenderKit();
    FacesRenderer createRenderer();
    FacesClientBehaviorRenderer createClientBehaviorRenderer();
    Lifecycle createLifecycle();
    PhaseListener createPhaseListener();
    FacesValidator createValidator();
    FacesBehavior createBehavior();
    ProtectedViews createProtectedView();
    UrlPattern createUrlPattern();
    ResourceLibraryContracts createResourceLibraryContracts();
    ContractMapping createContractMapping();
    FlashFactory createFlashFactory();
    FlowHandlerFactory createFlowHandlerFactory();
    FlowDefinition createFlowDefinition();
    FlowStartNode createStartNode();
    FlowInitializer createInitializer();
    FlowFinalizer createFinalizer();
    FlowView createFlowDefinitionView();
    FlowSwitch createFlowDefinitionSwitch();
    FlowDefaultOutcome createFlowDefinitionDefaultOutcome();
    FlowReturn createFlowDefinitionFlowReturn();
    FlowCall createFlowDefinitionFlowCall();
    FlowCallFacesFlowReference createFlowDefinitionFlowCallFacesFlowReference();
    FlowCallInboundParameter createFlowDefinitionFlowCallInboundParameter();
    FlowCallOutboundParameter createFlowDefinitionFlowCallOutboundParameter();
    FlowMethodCall createFlowDefinitionFacesMethodCall();
    FromOutcome createFromOutcome();
    FlowId createFlowId();
    FlowDocumentId createFlowDocumentId();
    Value createValue();
    Method createMethod();
    FlowCallParameter createFlowDefinitionFlowCallParameter();
    Clazz createClass();
}
