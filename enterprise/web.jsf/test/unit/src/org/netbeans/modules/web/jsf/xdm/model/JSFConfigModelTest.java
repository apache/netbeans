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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.web.jsf.api.facesmodel.*;
import org.netbeans.modules.web.jsf.impl.facesmodel.FacesAttributes;
import org.netbeans.modules.web.jsf.impl.facesmodel.JSFConfigModelImpl;
import org.netbeans.modules.xml.xam.Model;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Petr Pisl
 */
public class JSFConfigModelTest extends NbTestCase {

    public JSFConfigModelTest(String testName) {
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

    public static Test suite() {
        TestSuite suite = new TestSuite(JSFConfigModelTest.class);
        return suite;
    }

    // for collecting and testing events
    Hashtable <String, PropertyChangeEvent> events = new Hashtable<String, PropertyChangeEvent>();

    private void checkEvent (String name, String oldValue, String newValue) {
        PropertyChangeEvent event = events.get(name);
        assertNotNull(event);
        assertEquals(oldValue, ((String)event.getOldValue()).trim());
        assertEquals(newValue, ((String)event.getNewValue()).trim());
    }

    public void testReadJSFVersion1_1() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-01.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        System.out.println("facesConfig: " + facesConfig);
        Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
        for (ManagedBean elem : managedBeans) {
            System.out.println(elem.getManagedBeanName() + ", " + elem.getManagedBeanClass() + ", " + elem.getManagedBeanScope());
        }
    }

    public void testReadJSFVersion2_1() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config_2_1.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        System.out.println("facesConfig: " + facesConfig);
        Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
        for (ManagedBean elem : managedBeans) {
            System.out.println(elem.getManagedBeanName() + ", " + elem.getManagedBeanClass() + ", " + elem.getManagedBeanScope());
        }
        List<Factory> factories = facesConfig.getFactories();
        for (Factory factory : factories) {
            List<FaceletCacheFactory> faceletCacheFactories = factory.getFaceletCacheFactories();
            assertEquals(1, faceletCacheFactories.size());
            assertEquals("my.bean.FaceletCacheFactory", faceletCacheFactories.get(0).getFullyQualifiedClassType());
            System.out.println(faceletCacheFactories.get(0).getFullyQualifiedClassType());
        }
    }

    public void testReadJSFVersion2_2() throws Exception {
        // TODO finish with JSF2.2 update
        JSFConfigModel model = Util.loadRegistryModel("faces-config_2_2.xml");
        assertEquals(JSFVersion.JSF_2_2, model.getVersion());
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        System.out.println("facesConfig: " + facesConfig);
        Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
        for (ManagedBean elem : managedBeans) {
            System.out.println(elem.getManagedBeanName() + ", " + elem.getManagedBeanClass() + ", " + elem.getManagedBeanScope());
        }
        List<Factory> factories = facesConfig.getFactories();
        for (Factory factory : factories) {
            List<FaceletCacheFactory> faceletCacheFactories = factory.getFaceletCacheFactories();
            assertEquals(1, faceletCacheFactories.size());
            assertEquals("my.bean.FaceletCacheFactory", faceletCacheFactories.get(0).getFullyQualifiedClassType());
            System.out.println(faceletCacheFactories.get(0).getFullyQualifiedClassType());
        }
    }

    public void testReadJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example2.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        // testing managed bean
        Collection<ManagedBean> managedBeans = facesConfig.getManagedBeans();
        assertEquals("Number of managed beans ", 1, managedBeans.size());
        ManagedBean managedBean = managedBeans.iterator().next();
        assertEquals("ManagedBean name ", "usermanager", managedBean.getManagedBeanName());
        assertEquals("ManagedBean class ", "enterprise.jsf_jpa_war.UserManager", managedBean.getManagedBeanClass());
        assertEquals("ManagedBean scope ", ManagedBean.Scope.REQUEST, managedBean.getManagedBeanScope());
        //testing navigation rule
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        assertEquals("Rule description ", 0, navigationRule.getDescriptions().size());
        assertNull("Rule from-view-id ", navigationRule.getFromViewId());
        Collection<NavigationCase> navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 3, navigationCases.size());
        Iterator<NavigationCase> it = navigationCases.iterator();
        NavigationCase navigationCase = it.next();
        assertEquals("login", navigationCase.getFromOutcome());
        assertEquals("/login.jsp", navigationCase.getToViewId());
        assertTrue(navigationCase.isRedirected());
        assertEquals(0, navigationCase.getDescriptions().size());
        assertNull(navigationCase.getFromAction());
        navigationCase = it.next();
        assertEquals("create", navigationCase.getFromOutcome());
        assertEquals("/create.jsp", navigationCase.getToViewId());
        assertFalse(navigationCase.isRedirected());
        assertEquals(0, navigationCase.getDescriptions().size());
        assertNull(navigationCase.getFromAction());
        navigationCase = it.next();
        assertEquals("app-main", navigationCase.getFromOutcome());
        assertEquals("/welcomeJSF.jsp", navigationCase.getToViewId());
        assertTrue(navigationCase.isRedirected());
        assertEquals(0, navigationCase.getDescriptions().size());
        assertNull(navigationCase.getFromAction());
    }

    private static void endModelTransaction(JSFConfigModel model) {
        try {
            model.endTransaction();
        } catch (IllegalStateException ise) {
            Exceptions.printStackTrace(ise);
        }
    }

    final String newDescription = "Some text.\n Test description\nnew line\n\nnew second line.";
    final String newFromViewID = "/haha.jsp";
    public void testChangeNavigationRuleJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example3.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        assertEquals("Rule description ", 0,  navigationRule.getDescriptions().size());
        assertNull("Rule from-view-id ", navigationRule.getFromViewId());

        // provide change in the NavigationRule
        model.startTransaction();
        Description description = model.getFactory().createDescription();
        description.setValue(newDescription);
        navigationRule.addDescription(description);
        navigationRule.setFromViewId(newFromViewID);
        endModelTransaction(model);

        // test whether the change is in the model
        navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        navigationRule = navigationRules.iterator().next();
        assertEquals(newDescription, navigationRule.getDescriptions().get(0).getValue());
        assertEquals(newFromViewID, navigationRule.getFromViewId());

        // save the model into a tmp file and reload. then test again.
        dumpModelToFile(model, "test-config-01.xml");
        navigationRules = model.getRootComponent().getNavigationRules();
        navigationRule = navigationRules.iterator().next();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        assertEquals(newDescription, navigationRule.getDescriptions().get(0).getValue());
        assertEquals(newFromViewID, navigationRule.getFromViewId());

        // delete change in the NavigationRule
        model.startTransaction();
        navigationRule.removeDescription(navigationRule.getDescriptions().get(0));
        navigationRule.setFromViewId(null);
        endModelTransaction(model);

        navigationRules = model.getRootComponent().getNavigationRules();
        navigationRule = navigationRules.iterator().next();
        assertEquals(0, navigationRule.getDescriptions().size());
        assertNull(navigationRule.getFromViewId());
        dumpModelToFile(model, "test-config-02.xml");
    }

    public void testAddRemoveNavigationRuleJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example4.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);

        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());

        NavigationRule newRule = model.getFactory().createNavigationRule();

        Description description = model.getFactory().createDescription();
        description.setValue(newDescription);
        newRule.addDescription(description);
        newRule.setFromViewId(newFromViewID);

        model.startTransaction();
        facesConfig.addNavigationRule(newRule);
        endModelTransaction(model);

        // save the model into a tmp file and reload. then test.
        dumpModelToFile(model, "test-config-03.xml");
        navigationRules = model.getRootComponent().getNavigationRules();
        assertEquals("Number of navigation rules ", 2, navigationRules.size());
        Iterator <NavigationRule> iterator = navigationRules.iterator();
        iterator.next();
        newRule = iterator.next();
        assertEquals(newDescription, newRule.getDescriptions().get(0).getValue());
        assertEquals(newFromViewID, newRule.getFromViewId());

        model.startTransaction();
        model.getRootComponent().removeNavigationRule(newRule);
        endModelTransaction(model);

        dumpModelToFile(model, "test-config-04.xml");
        navigationRules = model.getRootComponent().getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
    }

    public void testChangeNavigationCase() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);

        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        Collection<NavigationCase> navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 3, navigationCases.size());
        NavigationCase navigationCase = navigationCases.iterator().next();

        model.startTransaction();
        Description description = navigationCase.getModel().getFactory().createDescription();
        description.setValue("Test Description");
        description.setLang("cz");
        navigationCase.addDescription(description);
        navigationCase.setFromAction("hahatest");
        navigationCase.setToViewId("welcomme.test");
        navigationCase.setRedirected(false);
        endModelTransaction(model);

        dumpModelToFile(model, "test-config-01.xml");

        model.startTransaction();
        navigationCase.setRedirected(true);
        endModelTransaction(model);
        dumpModelToFile(model, "test-config-02.xml");

    }

    public void testAddRemoveNavigationCaseJSFJPAExample() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example5.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);

        Collection<NavigationRule> navigationRules = facesConfig.getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        NavigationRule navigationRule = navigationRules.iterator().next();
        Collection<NavigationCase> navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 3, navigationCases.size());
        NavigationCase newCase = model.getFactory().createNavigationCase();
        Description description = newCase.getModel().getFactory().createDescription();
        description.setValue("Test case description");
        description.setLang("cz");
        newCase.addDescription(description);
        newCase.setFromOutcome("/fromOutcame.jsp");
        newCase.setToViewId("/toviewide.jsp");

        navigationRule.getModel().startTransaction();
        navigationRule.addNavigationCase(newCase);
        endModelTransaction(navigationRule.getModel());

        System.out.println("pridam case");
        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
        //model = Util.dumpAndReloadModel(model);
        navigationRules = model.getRootComponent().getNavigationRules();
        assertEquals("Number of navigation rules ", 1, navigationRules.size());
        navigationRule = navigationRules.iterator().next();
        navigationCases = navigationRule.getNavigationCases();
        assertEquals("Number of navigation cases ", 4, navigationCases.size());

    }

    public void testJSFVersion() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        assertEquals(JSFVersion.JSF_1_2, model.getVersion());
        model = Util.loadRegistryModel("faces-config-01.xml");
        assertEquals(JSFVersion.JSF_1_1, model.getVersion());
    }

    public void testComments() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);
        facesConfig.getPeer();

    }

    public void testEditable() throws Exception {
        File file = new File(getDataDir(), "faces-config1.xml");

        FileObject fileObject = FileUtil.toFileObject(file);
        ModelSource model = TestCatalogModel.getDefault().createModelSource(fileObject, false);
        //JSFConfigModel jsfConfig1 = JSFConfigModelFactory.getInstance().getModel(model);
        //assertFalse(jsfConfig1.getModelSource().isEditable());
        assertFalse(model.isEditable());
        ModelSource model2 = TestCatalogModel.getDefault().createModelSource(fileObject, true);
        //JSFConfigModel jsfConfig2 = JSFConfigModelFactory.getInstance().getModel(model2);
        //assertTrue("The model should be editable ", jsfConfig2.getModelSource().isEditable());
        assertTrue(model2.isEditable());
    }

    private File dumpModelToFile(JSFConfigModel model, String fileName) throws Exception{
        File file = new File(getWorkDir(), fileName);
        System.out.println("workfile: " + file.getAbsolutePath());
        Util.dumpToFile(model, file);
        return file;
    }

    public void testConverter() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-jsfjpa-example.xml");
        FacesConfig facesConfig = model.getRootComponent();
        assertNotNull(facesConfig);

        List <Converter> converters = facesConfig.getConverters();
        assertEquals("Number of converters ", 1, converters.size());

    }

    public void testManagedBeanID() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-02.xml");
        List <ManagedBean> beans = model.getRootComponent().getManagedBeans();
        ManagedBean managedBean = null;
        for (ManagedBean bean : beans) {
            String value = bean.getAttribute(FacesAttributes.ID);
            if ("person".equals(bean.getManagedBeanName()))
                assertNull(value);
            if ("student".equals(bean.getManagedBeanName())){
                managedBean = bean;
                assertEquals("student", value);
            }
        }
        managedBean.getModel().startTransaction();
        managedBean.setAttribute(FacesAttributes.ID.getName(), FacesAttributes.ID, "girl");
        endModelTransaction(managedBean.getModel());
        assertEquals("girl", managedBean.getAttribute(FacesAttributes.ID));
    }

    public void test98276() throws Exception{
        JSFConfigModel model = Util.loadRegistryModel("faces-config-98276.xml");
        model.addPropertyChangeListener(
                new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent event) {
                assertNotSame(Model.STATE_PROPERTY, event.getPropertyName());
            }

        });
        List <NavigationRule> rules = model.getRootComponent().getNavigationRules();
        NavigationRule rule = rules.get(0);
        assertEquals("RealPage3.jsp", rule.getFromViewId());
        assertEquals("RealPage5.jsp", rule.getNavigationCases().get(0).getToViewId());
        model.startTransaction();
        rule.setFromViewId("RealPage4.jsp");
        endModelTransaction(model);
        model.sync();
        assertEquals("RealPage4.jsp", rule.getFromViewId());
    }



    public void testDescriptionGroup() throws Exception {
        JSFConfigModel model = Util.loadRegistryModel("faces-config-description.xml");

        model.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent event) {
                System.out.println("property: " +  event.getPropertyName());
            }
        });

        NavigationRule rule = model.getRootComponent().getNavigationRules().get(0);


        assertEquals("Count of Descriptions ", 2, rule.getDescriptions().size());
        Description description = rule.getDescriptions().get(0);
        assertEquals("cz", description.getLang());
        description = rule.getDescriptions().get(1);
        assertEquals("en", description.getLang());

        assertEquals("Count of DisplayNames ", 1, rule.getDisplayNames().size());
        assertEquals("Count of Icons ", 2, rule.getIcons().size());
    }

    public void testManagedBeanScopeOrdering() throws Exception{
        assertTrue(ManagedBean.Scope.REQUEST.compareTo(ManagedBean.Scope.SESSION) < 0);
        assertTrue(ManagedBean.Scope.SESSION.compareTo(ManagedBean.Scope.APPLICATION) < 0);
        assertTrue(ManagedBean.Scope.APPLICATION.compareTo(ManagedBean.Scope.NONE) < 0);
    }


    public void testApplication() throws Exception {

        JSFConfigModel model = Util.loadRegistryModel("faces-config-application.xml");

        model.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent event) {
                System.out.println("property: " +  event.getPropertyName());
                events.put(event.getPropertyName(), event);
            }
        });

        List<Application> applications = model.getRootComponent().getApplications();
        assertEquals("Number of applications ", 1, applications.size());

        List<ViewHandler> viewHandlers = applications.get(0).getViewHandlers();
        assertEquals("Number of view hadlers ", 1, viewHandlers.size());
        assertEquals("Name of handler ", "org.test.ViewHandler", viewHandlers.get(0).getFullyQualifiedClassType());

        List<LocaleConfig> localeConfigs = applications.get(0).getLocaleConfig();
        assertEquals("Number of locale-config ", 1, localeConfigs.size());
        LocaleConfig locale = localeConfigs.get(0);
        assertEquals("Defautl locale ", "en", locale.getDefaultLocale().getLocale());
        List<SupportedLocale> supportedLocales = locale.getSupportedLocales();
        assertEquals("Number of supported-locale ", 2, supportedLocales.size());
        assertEquals("Suported locale ", "cz", supportedLocales.get(0).getLocale());
        assertEquals("Suported locale ", "jn", supportedLocales.get(1).getLocale());

        List<ResourceBundle> resourceBundles = applications.get(0).getResourceBundles();
        assertEquals("Number of resource-bundle ", 2, resourceBundles.size());
        ResourceBundle resourceBundle = resourceBundles.get(0);
        assertEquals("Description of resource-bundle ", "This is a test resource bundle.", resourceBundle.getDescriptions().get(0).getValue().trim());
        assertEquals("Display name of resource-bundle ", "Test Resource Bundle", resourceBundle.getDisplayNames().get(0).getValue());
        assertEquals("Base name of resource-bundle ", "org.test.TestMessages", resourceBundle.getBaseName());
        assertEquals("Var of resource-bundle ", "test", resourceBundle.getVar());
        resourceBundle = resourceBundles.get(1);
        assertEquals("Base name of resource-bundle ", "org.test.Messages", resourceBundle.getBaseName());
        assertEquals("Var of resource-bundle ", "msg", resourceBundle.getVar());

        events.clear();

        model.startTransaction();
        viewHandlers.get(0).setFullyQualifiedClassType("a.b.c.Handler");
        locale.getDefaultLocale().setLocale("cz");
        supportedLocales.get(0).setLocale("en");
        resourceBundles.get(0).setVar("testMessages");
        endModelTransaction(model);
        model.sync();

        checkEvent(ViewHandler.VIEW_HANDLER, "org.test.ViewHandler", "a.b.c.Handler");
        checkEvent(LocaleConfig.DEFAULT_LOCALE, "en", "cz");
        checkEvent(LocaleConfig.SUPPORTED_LOCALE, "cz", "en");
        checkEvent(ResourceBundle.VAR, "test", "testMessages");

        assertEquals("Name of handler ", "a.b.c.Handler", viewHandlers.get(0).getFullyQualifiedClassType());

        Application newApplication = model.getFactory().createApplication();
        model.startTransaction();
        model.getRootComponent().addApplication(newApplication);

        ViewHandler viewHandler = model.getFactory().createViewHandler();
        viewHandler.setFullyQualifiedClassType("a.b.c.Handler2");
        newApplication.addViewHandler(viewHandler);
        viewHandler = model.getFactory().createViewHandler();
        viewHandler.setFullyQualifiedClassType("a.b.c.Handler3");
        newApplication.addViewHandler(viewHandler);
        /*
         * Application has its children without order ( it is not "sequence",
         * it's "choice" ) so this method is no longer exist.
         * viewHandler = model.getFactory().createViewHandler();
        viewHandler.setFullyQualifiedClassType("a.b.c.Handler1");
        newApplication.addViewHandler(0, viewHandler);*/

        LocaleConfig newLocale = model.getFactory().createLocaleConfig();
        newLocale.setDefaultLocale(model.getFactory().createDefatultLocale());
        newLocale.getDefaultLocale().setLocale("cz");
        newLocale.addSupportedLocales(model.getFactory().createSupportedLocale());
        newLocale.getSupportedLocales().get(0).setLocale("en");
        newLocale.addSupportedLocales(0,model.getFactory().createSupportedLocale());
        newLocale.getSupportedLocales().get(0).setLocale("hr");
        newApplication.addLocaleConfig(newLocale);

        ResourceBundle newResourceBundle = model.getFactory().createResourceBundle();
        newResourceBundle.setVar("czech");
        newResourceBundle.setBaseName("org.test.Messages");

        newApplication.addResourceBundle( newResourceBundle);
        endModelTransaction(model);
        model.sync();

        //Util.dumpToStream(((AbstractDocumentModel)model).getBaseDocument(), System.out);
        // very bad idea to check XML files against golden files assertFile(dumpModelToFile(model, "test-application.xml"), getGoldenFile("gold-application.xml"));
    }


}
