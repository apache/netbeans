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

package org.netbeans.modules.web.jsf.xdm.model;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.Application;
import org.netbeans.modules.web.jsf.api.facesmodel.Clazz;
import org.netbeans.modules.web.jsf.api.facesmodel.ContractMapping;
import org.netbeans.modules.web.jsf.api.facesmodel.DefaultLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.Factory;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowFinalizer;
import org.netbeans.modules.web.jsf.api.facesmodel.FlashFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefinition;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCall;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallFacesFlowReference;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallOutboundParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowReturn;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowSwitch;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDefaultOutcome;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowMethodCall;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallInboundParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowCallParameter;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowView;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowDocumentId;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowHandlerFactory;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowId;
import org.netbeans.modules.web.jsf.api.facesmodel.FromOutcome;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowInitializer;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.LocaleConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.Method;
import org.netbeans.modules.web.jsf.api.facesmodel.Name;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.netbeans.modules.web.jsf.api.facesmodel.Ordering;
import org.netbeans.modules.web.jsf.api.facesmodel.ProtectedViews;
import org.netbeans.modules.web.jsf.api.facesmodel.ResourceLibraryContracts;
import org.netbeans.modules.web.jsf.api.facesmodel.FlowStartNode;
import org.netbeans.modules.web.jsf.api.facesmodel.SupportedLocale;
import org.netbeans.modules.web.jsf.api.facesmodel.UrlPattern;
import org.netbeans.modules.web.jsf.api.facesmodel.Value;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.openide.util.Exceptions;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Petr Pisl, ads
 */
public class ElementOrderingTest extends NbTestCase {

    public ElementOrderingTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }
    
    protected void setUp() throws Exception {
        Logger.getLogger(JSFConfigModelImpl.class.getName()).setLevel(Level.FINEST);
    }
    
    protected void tearDown() throws Exception {
    }
    
    private static void endModelTransaction(JSFConfigModel model) {
        try {
            model.endTransaction();
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
    }
    
    public void testNavigationCase99906() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-99906.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        NavigationRule rule = facesConfig.getNavigationRules().get(0);
        
        model.startTransaction();
        rule.setFromViewId("frompage.jsp");
        endModelTransaction(model);
        model.sync();
        
        NodeList nodes = rule.getPeer().getChildNodes();
        assertEquals(nodes.item(1).getNodeName(), "from-view-id");
        assertEquals(nodes.item(5).getNodeName(), "navigation-case");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);

    }
    
    /**
     * This test makes sure that from-outcome is always listed before to-view-id regardless of which was set first.
     * @throws java.lang.Exception 
     */
    public void test98691() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-empty.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        NavigationRule newRule = model.getFactory().createNavigationRule();
        newRule.setFromViewId("frompage.jsp");
        NavigationCase newCase = model.getFactory().createNavigationCase();
        
        //When order is switched.
        newCase.setToViewId("toPage.jsp");
        newCase.setFromOutcome("fromoutcome");
        
        newRule.addNavigationCase(newCase);
        facesConfig.addNavigationRule(newRule);
        endModelTransaction(model);
        model.sync();
        
        NodeList list = newCase.getPeer().getChildNodes();
        
        assertEquals(list.item(1).getNodeName(), "from-outcome");
        assertEquals(list.item(3).getNodeName(), "to-view-id");
        
        //One more test to make sure that even if the outcome is reset, it is still listed as first.
        model.startTransaction();
        newCase.setFromOutcome("fromoutcome2");
        endModelTransaction(model);
        model.sync();
        
        NodeList list2 = newCase.getPeer().getChildNodes();        
        assertEquals(list2.item(1).getNodeName(), "from-outcome");
        assertEquals(list2.item(3).getNodeName(), "to-view-id");
    }
    
    public void testDefaultLocale() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-locale-1.xml");
        FacesConfig facesConfig = model.getRootComponent();

        model.startTransaction();
        model.sync();
        Application application = facesConfig.getApplications().get(0);
        LocaleConfig config = application.getLocaleConfig().get(0);
        DefaultLocale locale = config.getDefaultLocale();
        assertNull( "test xml file doesn't containt default-locale element," +
        		" but its found there", locale );
        
        locale = model.getFactory().createDefatultLocale();
        config.setDefaultLocale(locale);
        endModelTransaction(model);
        model.sync();
        
        Element element  = Util.getElement( config.getPeer(), 0 );
        assertEquals( "Element locale-config should contain " +
                "default-locale as first child element, " +
                "but it contians :" +element.getNodeName(), element.getNodeName(), "default-locale");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testEmptyLocale() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-locale-2.xml");
        FacesConfig facesConfig = model.getRootComponent();

        model.startTransaction();
        Application application = facesConfig.getApplications().get(0);
        LocaleConfig config = application.getLocaleConfig().get(0);
        clearConfig(config);

        SupportedLocale locale = model.getFactory().createSupportedLocale();
        config.addSupportedLocales(locale);
        DefaultLocale defaultLocale = model.getFactory().createDefatultLocale();
        config.setDefaultLocale( defaultLocale );
        locale = model.getFactory().createSupportedLocale();
        config.addSupportedLocales( locale );

        endModelTransaction(model);
        model.sync();

        Element element  = Util.getElement( config.getPeer(), 0 );
        assertEquals( "Element locale-config should contain " +
        		"default-locale as first child element, " +
        		"but it contians :" +element.getNodeName(), element.getNodeName(), "default-locale");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testOrdering() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-ordering.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        
        List<Ordering> orderings = facesConfig.getOrderings();
        assertEquals( 1, orderings.size());
        
        Ordering ordering = orderings.get(0);
        assertNull(ordering.getAfter());
        assertNull(ordering.getBefore());
        
        ordering.setBefore( model.getFactory().createBefore());
        ordering.setAfter( model.getFactory().createAfter());
        
        assertNotNull(ordering.getAfter());
        assertNotNull(ordering.getBefore());
        
        Element element = Util.getElement( ordering.getPeer(), 0);
        assertEquals( "after", element.getNodeName());
        
        element = Util.getElement( ordering.getPeer(), 1);
        assertEquals("before", element.getNodeName());
        
        endModelTransaction(model);
        model.sync();
        
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }
    
    public void testNavigationCase() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-navigation-case.xml");
        FacesConfig facesConfig = model.getRootComponent();
        
        model.startTransaction();
        
        List<NavigationRule> rules = facesConfig.getNavigationRules();
        assertEquals( 1 , rules.size());
        
        NavigationRule rule = rules.get(0);
        List<NavigationCase> cases = rule.getNavigationCases();
        assertEquals( 1 , cases.size());
        
        NavigationCase caze = cases.get(0);
        
        assertNotNull( caze.getRedirect());
        
        caze.setToViewId( "toViewId");
        caze.setIf( model.getFactory().createIf());
        caze.setFromAction("fromAction");
        caze.addDescription( model.getFactory().createDescription());
        
        endModelTransaction(model);
        model.sync();
        
        Element element = Util.getElement(caze.getPeer(), 0);
        assertEquals( "description",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 1);
        assertEquals( "icon",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 2);
        assertEquals( "from-action",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 3);
        assertEquals( "from-outcome",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 4);
        assertEquals( "if",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 5);
        assertEquals( "to-view-id",  element.getNodeName());
        
        element = Util.getElement(caze.getPeer(), 6);
        assertEquals( "redirect",  element.getNodeName());
        
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
    }

    public void testProtectedView() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-protected-view.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<ProtectedViews> protectedViews = facesConfig.getProtectedViews();
        assertEquals(1, protectedViews.size());
        ProtectedViews view = protectedViews.get(0);
        assertEquals(1, view.getUrlPatterns().size());
        assertEquals("myPattern", view.getUrlPatterns().get(0).getText());

        // add view, check
        model.startTransaction();
        ProtectedViews newView = model.getFactory().createProtectedView();
        facesConfig.addProtectedView(newView);
        endModelTransaction(model);
        model.sync();
        assertEquals(2, facesConfig.getProtectedViews().size());

        // remove view, check
        model.startTransaction();
        facesConfig.removeProtectedView(newView);
        endModelTransaction(model);
        model.sync();
        assertEquals(1, facesConfig.getProtectedViews().size());

        // add pattern, check
        model.startTransaction();
        UrlPattern pattern = model.getFactory().createUrlPattern();
        pattern.setText("helloAll");
        view.addUrlPatterns(pattern);
        endModelTransaction(model);
        model.sync();
        assertEquals(2, view.getUrlPatterns().size());
        assertEquals("helloAll", view.getUrlPatterns().get(1).getText());

        // remove pattern, check
        model.startTransaction();
        view.removeUrlPatterns(pattern);
        endModelTransaction(model);
        model.sync();
        assertEquals(1, facesConfig.getProtectedViews().size());
    }

    public void testResourceLibraryContracts() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-resource-library-contracts.xml");
        FacesConfig facesConfig = model.getRootComponent();
        Application application = facesConfig.getApplications().get(0);

        // check actual
        ResourceLibraryContracts resourceLibraryContract = application.getResourceLibraryContracts().get(0);
        assertNotNull(resourceLibraryContract);
        List<ContractMapping> mappings = resourceLibraryContract.getContractMappings();
        assertEquals(2, mappings.size());
        ContractMapping mapping = mappings.get(0);
        assertEquals("user", mapping.getContracts());
        assertEquals("/user/*", mapping.getUrlPattern().getText());

        // remove RLC, check
        model.startTransaction();
        application.removeResourceLibraryContract(resourceLibraryContract);
        endModelTransaction(model);
        model.sync();
        assertEquals(0, application.getResourceLibraryContracts().size());

        // add RLC, check
        model.startTransaction();
        resourceLibraryContract = model.getFactory().createResourceLibraryContracts();
        application.addResourceLibraryContract(resourceLibraryContract);
        endModelTransaction(model);
        model.sync();
        assertEquals(1, application.getResourceLibraryContracts().size());

        // add mapping, check
        model.startTransaction();
        ContractMapping newMapping = model.getFactory().createContractMapping();
        UrlPattern pattern = model.getFactory().createUrlPattern();
        pattern.setText("/helloAll");
        newMapping.setUrlPattern(pattern);
        newMapping.setContracts("helloContract");
        resourceLibraryContract.addContractMapping(newMapping);
        endModelTransaction(model);
        model.sync();
        assertEquals(1, application.getResourceLibraryContracts().size());
        assertEquals(1, application.getResourceLibraryContracts().get(0).getContractMappings().size());
        ContractMapping contract = application.getResourceLibraryContracts().get(0).getContractMappings().get(0);
        assertEquals("/helloAll", contract.getUrlPattern().getText());
        assertEquals("helloContract", contract.getContracts());
    }

    public void testFlashFactory() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flash-factory.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<Factory> factories = facesConfig.getFactories();
        assertEquals(1, factories.size());
        Factory factory = factories.get(0);
        assertEquals(1, factory.getFlashFactory().size());
        assertEquals("com.example.MyFlashFactory", factory.getFlashFactory().get(0).getFullyQualifiedClassType());

        // remove factory, check
        model.startTransaction();
        facesConfig.removeFactory(factory);
        endModelTransaction(model);
        model.sync();
        assertEquals(0, facesConfig.getFactories().size());

        // add factory, check
        model.startTransaction();
        FlashFactory ff = model.getFactory().createFlashFactory();
        ff.setFullyQualifiedClassType("myFQN");
        factory = model.getFactory().createFactory();
        factory.addFlashFactory(ff);
        facesConfig.addFactories(factory);
        endModelTransaction(model);
        model.sync();
        assertEquals(1, facesConfig.getFactories().size());
        assertEquals(1, facesConfig.getFactories().get(0).getFlashFactory().size());
        assertEquals("myFQN", facesConfig.getFactories().get(0).getFlashFactory().get(0).getFullyQualifiedClassType());
    }

    public void testFlowHandlerFactory() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-handler-factory.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<Factory> factories = facesConfig.getFactories();
        assertEquals(1, factories.size());
        Factory factory = factories.get(0);
        assertEquals(1, factory.getFlowHandlerFactory().size());
        assertEquals("flowHandlerFactory", factory.getFlowHandlerFactory().get(0).getFullyQualifiedClassType());

        // remove factory, check
        model.startTransaction();
        facesConfig.removeFactory(factory);
        endModelTransaction(model);
        model.sync();
        assertEquals(0, facesConfig.getFactories().size());

        // add factory, check
        model.startTransaction();
        FlowHandlerFactory fhf = model.getFactory().createFlowHandlerFactory();
        fhf.setFullyQualifiedClassType("myHandlerFact");
        factory = model.getFactory().createFactory();
        factory.addFlowHandlerFactory(fhf);
        facesConfig.addFactories(factory);
        endModelTransaction(model);
        model.sync();
        assertEquals(1, facesConfig.getFactories().size());
        assertEquals(1, facesConfig.getFactories().get(0).getFlowHandlerFactory().size());
        assertEquals("myHandlerFact", facesConfig.getFactories().get(0).getFlowHandlerFactory().get(0).getFullyQualifiedClassType());
    }

    public void testFacesFlowDefinitionInitializer() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getInitializers().size());
        assertEquals("#{someBean.init}", flowDefinition.getInitializers().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeInitializer(flowDefinition.getInitializers().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getInitializers().size());

        // add some elements, check
        model.startTransaction();
        FlowInitializer initializer = model.getFactory().createInitializer();
        initializer.setText("myInit");
        flowDefinition.addInitializer(initializer);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getInitializers().size());
        assertEquals("myInit", flowDefinition.getInitializers().get(0).getText());
    }

    public void testFacesFlowDefinitionStartNode() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getStartNodes().size());
        assertEquals("startNode", flowDefinition.getStartNodes().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeStartNode(flowDefinition.getStartNodes().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getStartNodes().size());

        // add some elements, check
        model.startTransaction();
        FlowStartNode startNode = model.getFactory().createStartNode();
        startNode.setText("myStart");
        flowDefinition.addStartNode(startNode);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getStartNodes().size());
        assertEquals("myStart", flowDefinition.getStartNodes().get(0).getText());
    }

    public void testFacesFlowDefinitionFinalizer() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getFinalizers().size());
        assertEquals("#{someBean.finish}", flowDefinition.getFinalizers().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeFinalizer(flowDefinition.getFinalizers().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getFinalizers().size());

        // add some elements, check
        model.startTransaction();
        FlowFinalizer finalizer = model.getFactory().createFinalizer();
        finalizer.setText("myFinal");
        flowDefinition.addFinalizer(finalizer);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getFinalizers().size());
        assertEquals("myFinal", flowDefinition.getFinalizers().get(0).getText());
    }

    public void testFacesFlowDefinitionView() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getViews().size());
        assertEquals("barFlow", flowDefinition.getViews().get(0).getId());
        assertEquals("barFlow.xhtml", flowDefinition.getViews().get(0).getVdlDocument());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeView(flowDefinition.getViews().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getViews().size());

        // add some elements, check
        model.startTransaction();
        FlowView view = model.getFactory().createFlowDefinitionView();
        view.setId("myId");
        view.setVdlDocument("myVdlDcoument");
        flowDefinition.addView(view);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getViews().size());
        assertEquals("myId", flowDefinition.getViews().get(0).getId());
        assertEquals("myVdlDcoument", flowDefinition.getViews().get(0).getVdlDocument());
    }

    public void testFacesFlowDefinitionSwitch() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getSwitches().size());
        assertEquals("startNode", flowDefinition.getSwitches().get(0).getId());
        assertEquals(1, flowDefinition.getSwitches().get(0).getNavigationCases().size());
        assertEquals("fooView", flowDefinition.getSwitches().get(0).getNavigationCases().get(0).getFromOutcome());
        assertEquals(1, flowDefinition.getSwitches().get(0).getDefaultOutcomes().size());
        assertEquals("default", flowDefinition.getSwitches().get(0).getDefaultOutcomes().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeSwitch(flowDefinition.getSwitches().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getSwitches().size());

        // add some elements, check
        model.startTransaction();
        FlowSwitch switch1 = model.getFactory().createFlowDefinitionSwitch();
        NavigationCase navCase = model.getFactory().createNavigationCase();
        navCase.setToViewId("myIde");
        switch1.addNavigationCase(navCase);
        FlowDefaultOutcome defOut = model.getFactory().createFlowDefinitionDefaultOutcome();
        defOut.setText("myOutcome");
        switch1.addDefaultOutcome(defOut);
        flowDefinition.addSwitch(switch1);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getSwitches().size());
        assertEquals(1, flowDefinition.getSwitches().get(0).getNavigationCases().size());
        assertEquals("myIde", flowDefinition.getSwitches().get(0).getNavigationCases().get(0).getToViewId());
        assertEquals(1, flowDefinition.getSwitches().get(0).getDefaultOutcomes().size());
        assertEquals("myOutcome", flowDefinition.getSwitches().get(0).getDefaultOutcomes().get(0).getText());
    }

    public void testFacesFlowDefinitionFlowReturn() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getFlowReturns().size());
        assertEquals("exit", flowDefinition.getFlowReturns().get(0).getId());
        assertEquals(1, flowDefinition.getFlowReturns().get(0).getFromOutcomes().size());
        assertEquals("/exit", flowDefinition.getFlowReturns().get(0).getFromOutcomes().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeFlowReturn(flowDefinition.getFlowReturns().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getFlowReturns().size());

        // add some elements, check
        model.startTransaction();

        FlowReturn retrn = model.getFactory().createFlowDefinitionFlowReturn();
        retrn.setId("myexit");
        FromOutcome from = model.getFactory().createFromOutcome();
        from.setText("/myexit");
        retrn.addFromOutcome(from);
        flowDefinition.addFlowReturn(retrn);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getFlowReturns().size());
        assertEquals("myexit", flowDefinition.getFlowReturns().get(0).getId());
        assertEquals(1, flowDefinition.getFlowReturns().get(0).getFromOutcomes().size());
        assertEquals("/myexit", flowDefinition.getFlowReturns().get(0).getFromOutcomes().get(0).getText());
    }

    public void testFacesFlowDefinitionFlowCall() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getFlowCalls().size());
        assertEquals("call", flowDefinition.getFlowCalls().get(0).getId());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().size());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getOutboundParameters().size());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowDocumentIds().size());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowDocumentIds().size());
        assertEquals("docId", flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowDocumentIds().get(0).getText());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowIds().size());
        assertEquals("flowId", flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowIds().get(0).getText());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getNames().size());
        assertEquals("parameter", flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getNames().get(0).getText());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getValues().size());
        assertEquals("#{value}", flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getValues().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeFlowCall(flowDefinition.getFlowCalls().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getFlowCalls().size());

        // add some elements, check
        model.startTransaction();
        FlowCall call = model.getFactory().createFlowDefinitionFlowCall();
        call.setId("mycall");
        FlowCallFacesFlowReference reference = model.getFactory().createFlowDefinitionFlowCallFacesFlowReference();
        FlowDocumentId docId = model.getFactory().createFlowDocumentId();
        docId.setText("mydocId");
        FlowId flowId = model.getFactory().createFlowId();
        flowId.setText("myflowId");
        reference.addFlowDocumentId(docId);
        reference.addFlowId(flowId);
        call.addFacesFlowReference(reference);
        FlowCallOutboundParameter outboundParam = model.getFactory().createFlowDefinitionFlowCallOutboundParameter();
        Name fName = model.getFactory().createName();
        fName.setText("myFName");
        Value fValue = model.getFactory().createValue();
        fValue.setText("myFValue");
        outboundParam.addName(fName);
        outboundParam.addValue(fValue);
        call.addOutboundParameter(outboundParam);
        flowDefinition.addFlowCall(call);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getFlowCalls().size());
        assertEquals("mycall", flowDefinition.getFlowCalls().get(0).getId());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().size());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getOutboundParameters().size());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowDocumentIds().size());
        assertEquals("mydocId", flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowDocumentIds().get(0).getText());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowIds().size());
        assertEquals("myflowId", flowDefinition.getFlowCalls().get(0).getFacesFlowReferences().get(0).getFlowIds().get(0).getText());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getNames().size());
        assertEquals("myFName", flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getNames().get(0).getText());
        assertEquals(1, flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getValues().size());
        assertEquals("myFValue", flowDefinition.getFlowCalls().get(0).getOutboundParameters().get(0).getValues().get(0).getText());
    }

    public void testFacesFlowDefinitionMethodCall() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getMethodCalls().size());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getDefaultOutcomes().size());
        assertEquals("outcome", flowDefinition.getMethodCalls().get(0).getDefaultOutcomes().get(0).getText());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getMethods().size());
        assertEquals("#{bean.method}", flowDefinition.getMethodCalls().get(0).getMethods().get(0).getText());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getParameters().size());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getParameters().get(0).getClasses().size());
        assertEquals("java.lang.Boolean", flowDefinition.getMethodCalls().get(0).getParameters().get(0).getClasses().get(0).getText());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getParameters().get(0).getValues().size());
        assertEquals("#{bean.isFull}", flowDefinition.getMethodCalls().get(0).getParameters().get(0).getValues().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeMethodCall(flowDefinition.getMethodCalls().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getMethodCalls().size());

        // add some elements, check
        model.startTransaction();
        FlowMethodCall methodCall = model.getFactory().createFlowDefinitionFacesMethodCall();
        Method method = model.getFactory().createMethod();
        method.setText("#{bean.mymethod}");
        methodCall.addMethod(method);
        FlowDefaultOutcome dOutcome = model.getFactory().createFlowDefinitionDefaultOutcome();
        dOutcome.setText("myoutcome");
        methodCall.addDefaultOutcome(dOutcome);
        FlowCallParameter fParam = model.getFactory().createFlowDefinitionFlowCallParameter();
        Clazz clazz = model.getFactory().createClass();
        clazz.setText("my.Boolean");
        fParam.addClass(clazz);
        Value value = model.getFactory().createValue();
        value.setText("#{bean.isMyFull}");
        fParam.addValue(value);
        methodCall.addParameter(fParam);
        flowDefinition.addMethodCall(methodCall);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getMethodCalls().size());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getDefaultOutcomes().size());
        assertEquals("myoutcome", flowDefinition.getMethodCalls().get(0).getDefaultOutcomes().get(0).getText());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getMethods().size());
        assertEquals("#{bean.mymethod}", flowDefinition.getMethodCalls().get(0).getMethods().get(0).getText());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getParameters().size());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getParameters().get(0).getClasses().size());
        assertEquals("my.Boolean", flowDefinition.getMethodCalls().get(0).getParameters().get(0).getClasses().get(0).getText());
        assertEquals(1, flowDefinition.getMethodCalls().get(0).getParameters().get(0).getValues().size());
        assertEquals("#{bean.isMyFull}", flowDefinition.getMethodCalls().get(0).getParameters().get(0).getValues().get(0).getText());
    }

    public void testFacesFlowDefinitionInboundParams() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-flow-definition.xml");
        FacesConfig facesConfig = model.getRootComponent();

        // check actual
        List<FlowDefinition> flowDefinitions = facesConfig.getFlowDefinitions();
        assertEquals(1, flowDefinitions.size());
        FlowDefinition flowDefinition = flowDefinitions.get(0);

        assertEquals(1, flowDefinition.getInboundParameters().size());
        assertEquals(1, flowDefinition.getInboundParameters().get(0).getNames().size());
        assertEquals("parameter", flowDefinition.getInboundParameters().get(0).getNames().get(0).getText());
        assertEquals(1, flowDefinition.getInboundParameters().get(0).getValues().size());
        assertEquals("#{value}", flowDefinition.getInboundParameters().get(0).getValues().get(0).getText());

        // remove some elements, check
        model.startTransaction();
        flowDefinition.removeInboundParameter(flowDefinition.getInboundParameters().get(0));
        endModelTransaction(model);
        model.sync();
        assertEquals(0, flowDefinition.getInboundParameters().size());

        // add some elements, check
        model.startTransaction();
        FlowCallInboundParameter inParam = model.getFactory().createFlowDefinitionFlowCallInboundParameter();
        Name inName = model.getFactory().createName();
        inName.setText("myparameter");
        Value inValue = model.getFactory().createValue();
        inValue.setText("#{myvalue}");
        inParam.addName(inName);
        inParam.addValue(inValue);
        flowDefinition.addInboundParameter(inParam);
        endModelTransaction(model);
        model.sync();

        assertEquals(1, flowDefinition.getInboundParameters().size());
        assertEquals(1, flowDefinition.getInboundParameters().get(0).getNames().size());
        assertEquals("myparameter", flowDefinition.getInboundParameters().get(0).getNames().get(0).getText());
        assertEquals(1, flowDefinition.getInboundParameters().get(0).getValues().size());
        assertEquals("#{myvalue}", flowDefinition.getInboundParameters().get(0).getValues().get(0).getText());
    }

    private void clearConfig( LocaleConfig config ) {
        for ( SupportedLocale loc : config.getSupportedLocales() ){
            config.removeSupportedLocale(loc);
        }
        if ( config.getDefaultLocale() != null ){
            config.setDefaultLocale( null );
        }
    }
}
