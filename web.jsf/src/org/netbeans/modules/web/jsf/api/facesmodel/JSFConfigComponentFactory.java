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
