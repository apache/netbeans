/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.ide.FXProjectSupport;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Petr Somol
 */
public class JFXConfigsTest extends NbTestCase {
    
    public JFXConfigsTest(String testName) {
        super(testName);
    }

    private static final String PROJECT_NAME = "SampleFXProject";
    private static File projectParentDir;
    private static Project project = null;
    //private static J2SEPropertyEvaluator j2sePropEval = null;
    //private static PropertyEvaluator evaluator = null;
    private static JFXProjectProperties jfxprops = null;
    private static JFXProjectProperties.JFXConfigs CONFIGS = null;
    
    private static final String DEFAULT = null;
    private static final String NONDEF1 = "nondef_test_config_1";
    private static final String NONDEF2 = "nondef_test_config_2";
    
    private static final String PROP1 = "property_test_name_1";
    private static final String PROP2 = "property_test_name_2";
    private static final String PROP3 = "property_test_name_3";
    private static final String[] ALL_PROPS = new String[] {PROP1, PROP2, PROP3};
    private static final String[] PROPGROUP = new String[] {PROP2, PROP3};
    private static final String PROPGROUP_NAME = "property_test_group";

    private static final String CONFIG_PROPERTIES_FILE = "nbproject/private/config.properties";
    private static final String PROJECT_PROPERTIES_FILE = "nbproject/project.properties";
    private static final String PRIVATE_PROPERTIES_FILE = "nbproject/private/private.properties";
    private static final String NONDEF1_PROJECT_PROPERTIES_FILE = "nbproject/configs/" + NONDEF1 + ".properties";
    private static final String NONDEF1_PRIVATE_PROPERTIES_FILE = "nbproject/private/configs/" + NONDEF1 + ".properties";
    private static final String NONDEF2_PROJECT_PROPERTIES_FILE = "nbproject/configs/" + NONDEF2 + ".properties";
    private static final String NONDEF2_PRIVATE_PROPERTIES_FILE = "nbproject/private/configs/" + NONDEF2 + ".properties";

    /** Set up. */
    protected @Override void setUp() throws IOException {
        MockLookup.setLayersAndInstances();
        clearWorkDir();
        System.out.println("FXFXFXFX  "+getName()+"  FXFXFXFX");
        projectParentDir = this.getWorkDir();
        project = (Project)FXProjectSupport.createProject(projectParentDir, PROJECT_NAME);
        //j2sePropEval = project.getLookup().lookup(J2SEPropertyEvaluator.class);
        //evaluator = j2sePropEval.evaluator();        
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.emptyConfiguration()
            .addTest(JFXConfigsTest.class,
                "testProjectPropertiesSetUp",
                "testConfigsInitialState",
                "testSingleProperty",
                "testGetSetPropertyTransparent",
                "testPropertyGroups",
                "testParamProperties",
                "testSavedConfigFiles",
                "testEraseParamProperties",
                "testEraseParamSavedConfigFiles",
                "testCustomManifestEntries"
            )
        .enableModules(".*").clusters(".*"));
    }
    
    public void testProjectPropertiesSetUp() throws Exception {
        assertNotNull(project);
        jfxprops = JFXProjectProperties.getInstance(project.getLookup());
        assertNotNull(jfxprops);
        Project verify = jfxprops.getProject();
        assertNotNull(verify);
        FileObject projectDir = verify.getProjectDirectory();
        assertNotNull(projectDir);
        CONFIGS = jfxprops.getConfigs();
        assertNotNull(CONFIGS);
    }

    public void testConfigsInitialState() throws Exception {
        assertNotNull(CONFIGS);
        // newly created project has DEFAULT config defined and non-empty
        Set<String> names = CONFIGS.getConfigNames();
        assertTrue(names != null);
        //assertTrue(names.size() == 1); // default config only
        assertTrue(names.size() == 3); // default + Run as WebStart + Run in Browser
        assertFalse(CONFIGS.isActiveConfigEmpty());
        assertFalse(CONFIGS.isConfigEmpty(DEFAULT));
        assertFalse(CONFIGS.isDefaultConfigEmpty());
        assertNull(CONFIGS.getActive());
        assertTrue(CONFIGS.isConfigEmpty(NONDEF1));
        assertTrue(CONFIGS.isConfigEmpty(NONDEF2));
        for(String prop : ALL_PROPS) {
            assertFalse(CONFIGS.isPropertySet(DEFAULT, prop));
            assertFalse(CONFIGS.isActivePropertySet(prop));
            assertFalse(CONFIGS.isDefaultPropertySet(prop));
        }
    }
    
    public void testSingleProperty() throws Exception {
        assertNotNull(CONFIGS);
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isPropertySet(DEFAULT, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF2, PROP2));
        Set<String> names = CONFIGS.getConfigNames();
        assertTrue(names != null);
        // add property value, null is a valid value
        CONFIGS.setProperty(NONDEF1, PROP2, null);
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isPropertySet(DEFAULT, PROP2));
        assertTrue(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF2, PROP2));
        // check previously nonexistent config is created
        assertTrue(CONFIGS.hasConfig(DEFAULT));
        assertTrue(CONFIGS.hasConfig(NONDEF1));
        assertFalse(CONFIGS.hasConfig(NONDEF2));
        names = CONFIGS.getConfigNames();
        assertTrue(names != null);
        //assertTrue(names.size() == 2); // default + NONDEF1
        assertTrue(names.size() == 4); // default + NONDEF1 + Run as WebStart + Run in Browser
        // check property value (null is ambiguous)
        assertNull(CONFIGS.getProperty(DEFAULT, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF2, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP1));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP3));
        // change value to non-null
        CONFIGS.setProperty(NONDEF1, PROP2, "value2");
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isPropertySet(DEFAULT, PROP2));
        assertTrue(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF2, PROP2));
        // check property value
        assertNull(CONFIGS.getProperty(DEFAULT, PROP2));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP2), "value2"));
        assertNull(CONFIGS.getProperty(NONDEF2, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP1));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP3));
        // erase property
        CONFIGS.eraseProperty(NONDEF1, PROP2);
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isPropertySet(DEFAULT, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF2, PROP2));
        // check property value (null is ambiguous)
        assertNull(CONFIGS.getProperty(DEFAULT, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF2, PROP2));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP1));
        assertNull(CONFIGS.getProperty(NONDEF1, PROP3));
        // erase non-existent property does nothing
        CONFIGS.eraseProperty(NONDEF1, PROP2);
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isPropertySet(DEFAULT, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF2, PROP2));
        // erase configs
        names = CONFIGS.getConfigNames();
        assertTrue(names != null);
        //assertTrue(names.size() == 2); // default + NONDEF1
        assertTrue(names.size() == 4); // default + NONDEF1 + Run as WebStart + Run in Browser
        CONFIGS.eraseConfig(NONDEF1);
        assertTrue(CONFIGS.hasConfig(DEFAULT));
        assertFalse(CONFIGS.hasConfig(NONDEF1));
        assertFalse(CONFIGS.hasConfig(NONDEF2));
        names = CONFIGS.getConfigNames();
        assertTrue(names != null);
        //assertTrue(names.size() == 1); // default
        assertTrue(names.size() == 3); // default + Run as WebStart + Run in Browser
        CONFIGS.eraseConfig(NONDEF2);
        assertTrue(CONFIGS.hasConfig(DEFAULT));
        assertFalse(CONFIGS.hasConfig(NONDEF1));
        assertFalse(CONFIGS.hasConfig(NONDEF2));
        names = CONFIGS.getConfigNames();
        assertTrue(names != null);
        //assertTrue(names.size() == 1); // default
        assertTrue(names.size() == 3); // default + Run as WebStart + Run in Browser
    }
    
    public void testGetSetPropertyTransparent() throws Exception {
        assertNotNull(CONFIGS);
        // ensure CONFIGS is in required state before transparency testing
        CONFIGS.eraseDefaultProperty(PROP1);
        CONFIGS.eraseDefaultProperty(PROP2);
        CONFIGS.eraseDefaultProperty(PROP3);
        CONFIGS.eraseConfig(NONDEF1);
        CONFIGS.eraseConfig(NONDEF2);
        assertFalse(CONFIGS.isDefaultPropertySet(PROP1));
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isDefaultPropertySet(PROP3));
        assertFalse(CONFIGS.hasConfig(NONDEF1));
        assertFalse(CONFIGS.hasConfig(NONDEF2));
        // set transparent props, i.e., set conditionally only if different from default
        CONFIGS.setDefaultProperty(PROP2, "value2");
        CONFIGS.setPropertyTransparent(NONDEF1, PROP1, "value1");
        CONFIGS.setPropertyTransparent(NONDEF1, PROP2, "value2");
        // nontransparent getters
        assertFalse(CONFIGS.isDefaultPropertySet(PROP1));
        assertFalse(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultProperty(PROP1), null));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultProperty(PROP2), "value2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP1), "value1"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP2), null));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF2, PROP1), null));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF2, PROP2), null));
        // transparent getters
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(DEFAULT, PROP1), null));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(DEFAULT, PROP2), "value2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP1), "value1"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP2), "value2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF2, PROP1), null));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF2, PROP2), null)); // NONDEF2 nonexistent
        // solidify nondefault properties
        CONFIGS.solidifyProperty(NONDEF1, PROP1);
        CONFIGS.solidifyProperty(NONDEF1, PROP2);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP1), "value1"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP2), "value2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP1), "value1"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP2), "value2"));
    }

    public void testPropertyGroups() throws Exception {
        assertNotNull(CONFIGS);
        // ensure CONFIGS is in required state before transparency testing
        CONFIGS.eraseDefaultProperty(PROP1);
        CONFIGS.eraseDefaultProperty(PROP2);
        CONFIGS.eraseDefaultProperty(PROP3);
        CONFIGS.eraseConfig(NONDEF1);
        CONFIGS.eraseConfig(NONDEF2);
        assertFalse(CONFIGS.isDefaultPropertySet(PROP1));
        assertFalse(CONFIGS.isDefaultPropertySet(PROP2));
        assertFalse(CONFIGS.isDefaultPropertySet(PROP3));
        assertFalse(CONFIGS.hasConfig(NONDEF1));
        assertFalse(CONFIGS.hasConfig(NONDEF2));
        // define group consisting of PROP2 and PROP3
        CONFIGS.setDefaultProperty(PROP1, "value1");
        CONFIGS.setDefaultProperty(PROP2, "value2");
        CONFIGS.setDefaultProperty(PROP3, "value3");
        CONFIGS.defineGroup(PROPGROUP_NAME, Arrays.asList(PROPGROUP));
        assertFalse(CONFIGS.isBound(PROP1));
        assertTrue(CONFIGS.isBound(PROP2));
        assertTrue(CONFIGS.isBound(PROP3));
        CONFIGS.setPropertyTransparent(NONDEF1, PROP1, "value1B");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP1), "value1B"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP1), "value1B"));
        assertFalse(CONFIGS.isPropertySet(NONDEF1, PROP2));
        assertFalse(CONFIGS.isPropertySet(NONDEF1, PROP3));
        // setting one property of a group also sets all others (=copies their values from default config)
        CONFIGS.setPropertyTransparent(NONDEF1, PROP2, "value2B");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP2), "value2B"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP3), "value3"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP2), "value2B"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP3), "value3")); // set as part of group
        // erasing one property of a group also erases all others
        CONFIGS.eraseProperty(NONDEF1, PROP3);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP1), "value1B"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP2), null));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getProperty(NONDEF1, PROP3), null)); // set as part of group
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP1), "value1B"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP2), "value2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getPropertyTransparent(NONDEF1, PROP3), "value3")); // set as part of group
    }

    public void testParamProperties() throws Exception {
        assertNotNull(CONFIGS);
        System.out.println("DEBUG");
        System.out.println(CONFIGS.paramsToString());
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: empty"));
        // ensure CONFIGS is in required state before transparency testing
        CONFIGS.eraseDefaultParamsTransparent();
        CONFIGS.eraseConfig(NONDEF1);
        CONFIGS.eraseConfig(NONDEF2);
        // add parameters to default config
        CONFIGS.addDefaultParamTransparent("par1"); // nameless = argument
        CONFIGS.addDefaultParamTransparent("par2", "val2"); // named
        CONFIGS.addDefaultParamTransparent("par3", "val3");
        assertTrue(CONFIGS.hasDefaultParamTransparent("par1"));
        assertFalse(CONFIGS.hasDefaultParamValueTransparent("par1"));
        assertTrue(CONFIGS.hasDefaultParamTransparent("par2"));
        assertTrue(CONFIGS.hasDefaultParamValueTransparent("par2"));
        assertTrue(CONFIGS.hasDefaultParamTransparent("par2", "val2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultParamsTransparentAsString(false), "par1, par2=val2, par3=val3"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultParamsTransparentAsString(true), "par1 --par2=val2 --par3=val3"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getParamsTransparentAsString(DEFAULT, false), "par1, par2=val2, par3=val3"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par1 name=par2 value=val2 name=par3 value=val3"));
        // add nondef params
        CONFIGS.setActive(NONDEF1);
        CONFIGS.addActiveParamTransparent("par4");
        CONFIGS.addParamTransparent(NONDEF1, "par5", "val5b");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamValueTransparent("par5"), "val5b"));
        CONFIGS.addActiveParamTransparent("par5", "val5");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamsAsString(false), "par4, par5=val5"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamsAsString(true), "par4 --par5=val5"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamsTransparentAsString(false), "par1, par2=val2, par3=val3, par4, par5=val5"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamsTransparentAsString(true), "par1 --par2=val2 --par3=val3 par4 --par5=val5"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(), "MultiProperty: {null} name=par1 name=par2 value=val2 name=par3 value=val3 {nondef_test_config_1} name=par4 name=par5 value=val5"));
        CONFIGS.addActiveParamTransparent("par1"); // should not make another instance
        assertFalse(CONFIGS.hasActiveParam("par1"));
        assertTrue(CONFIGS.hasActiveParamTransparent("par1"));
        // erase params
        CONFIGS.eraseParamTransparent(NONDEF1, "par5");
        CONFIGS.eraseDefaultParamTransparent("par1");
        CONFIGS.eraseParamTransparent(DEFAULT, "par3");
        assertFalse(CONFIGS.hasParamTransparent(DEFAULT, "par1"));
        assertTrue(CONFIGS.hasDefaultParamTransparent("par2"));
        assertFalse(CONFIGS.hasParamTransparent(DEFAULT, "par3"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF1, "par4"));
        assertFalse(CONFIGS.hasParamTransparent(NONDEF1, "par5"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamsTransparentAsString(false), "par2=val2, par4"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getActiveParamsTransparentAsString(true), "--par2=val2 par4"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val2 {nondef_test_config_1} name=par4"));
    }
    
    public void testSavedConfigFiles() throws Exception {
        assertNotNull(jfxprops);
        // Note: SFXProjectProperties.store() stores only properties in JFXConfig.PROJECT_PROPERTIES
        CONFIGS.setDefaultProperty(JFXProjectProperties.RUN_APP_WIDTH, "444");
        CONFIGS.setDefaultProperty(JFXProjectProperties.RUN_APP_HEIGHT, "333");
        CONFIGS.setDefaultProperty(JFXProjectProperties.RUN_AS, "dummy");
        CONFIGS.setProperty(NONDEF1, PROP3, "dummy_1_3");
        CONFIGS.setProperty(NONDEF2, PROP3, "dummy_2_3");
        jfxprops.store();
        // verify existence of config files
        FileObject projectDir = jfxprops.getProject().getProjectDirectory();
        assertNotNull(projectDir);
        EditableProperties ep;
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(CONFIG_PROPERTIES_FILE));
        assertTrue(ep.size() == 1);
        assertTrue(ep.getProperty("config").equals(NONDEF1));
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(PROJECT_PROPERTIES_FILE));
        assertTrue(ep.getProperty(JFXProjectProperties.RUN_APP_WIDTH).equals("444"));
        assertTrue(ep.getProperty(JFXProjectProperties.RUN_APP_HEIGHT).equals("333"));
        assertTrue(ep.getProperty("javafx.param.0.name").equals("par2"));
        assertTrue(ep.getProperty("javafx.param.0.value").equals("val2"));
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(PRIVATE_PROPERTIES_FILE));
        assertTrue(ep.getProperty(JFXProjectProperties.RUN_AS).equals("dummy"));
        assertTrue(ep.getProperty("application.args").equals("--par2=val2"));
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(NONDEF1_PROJECT_PROPERTIES_FILE));
        assertTrue(ep.size() == 3);
        assertTrue(ep.getProperty("javafx.param.1.name").equals("par4"));
        assertTrue(ep.getProperty(PROP2).equals("value2"));
        assertTrue(ep.getProperty(PROP3).equals("dummy_1_3"));
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(NONDEF1_PRIVATE_PROPERTIES_FILE));
        assertTrue(ep.size() == 1);
        assertTrue(ep.getProperty("application.args").equals("--par2=val2 par4"));
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(NONDEF2_PROJECT_PROPERTIES_FILE));
        assertTrue(ep.size() == 2);
        assertTrue(ep.getProperty(PROP2).equals("value2"));
        assertTrue(ep.getProperty(PROP3).equals("dummy_2_3"));
        ep = JFXProjectUtils.readFromFile(projectDir.getFileObject(NONDEF2_PRIVATE_PROPERTIES_FILE));
        assertTrue(ep.size() == 0);
    }
    
    public void testEraseParamProperties() throws Exception {
        assertNotNull(CONFIGS);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val2 {nondef_test_config_1} name=par4"));
        CONFIGS.addDefaultParamTransparent("par1"); 
        CONFIGS.addParamTransparent(NONDEF1, "par3", "val3"); 
        CONFIGS.setActive(NONDEF2);
        CONFIGS.addActiveParamTransparent("par5");
        CONFIGS.addActiveParamTransparent("par6");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par1 name=par2 value=val2 {nondef_test_config_1} name=par3 value=val3 name=par4 {nondef_test_config_2} name=par5 name=par6"));
        CONFIGS.eraseParamTransparent(NONDEF1, "par2");
        CONFIGS.eraseDefaultParamTransparent("par1");
        CONFIGS.eraseActiveParamTransparent("par5");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val2 {nondef_test_config_1} name=par2 hidden=true name=par3 value=val3 name=par4 {nondef_test_config_2} name=par6"));
        CONFIGS.setActive(NONDEF1);
        CONFIGS.addActiveParamTransparent("par2");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val2 {nondef_test_config_1} name=par2 name=par3 value=val3 name=par4 {nondef_test_config_2} name=par6"));
        CONFIGS.addActiveParamTransparent("par2", "val2"); // should revert to default
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val2 {nondef_test_config_1} name=par3 value=val3 name=par4 {nondef_test_config_2} name=par6"));
        CONFIGS.setActive(NONDEF2);
        CONFIGS.eraseActiveParamTransparent("par2");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val2 {nondef_test_config_1} name=par3 value=val3 name=par4 {nondef_test_config_2} name=par2 hidden=true name=par6"));
        CONFIGS.eraseDefaultParamTransparent("par2");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} {nondef_test_config_1} name=par3 value=val3 name=par4 {nondef_test_config_2} name=par6"));
        CONFIGS.addDefaultParamTransparent("par3", "val3");
        CONFIGS.addDefaultParamTransparent("par6");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par3 value=val3 name=par6 {nondef_test_config_1} name=par4 {nondef_test_config_2}"));
    }

    public void testEraseParamSavedConfigFiles() throws Exception {
        EditableProperties ep = new EditableProperties(true);
        FileObject projectDir = jfxprops.getProject().getProjectDirectory();
        JFXProjectUtils.deleteFile(projectDir.getFileObject(PROJECT_PROPERTIES_FILE));
        JFXProjectUtils.deleteFile(projectDir.getFileObject(NONDEF1_PROJECT_PROPERTIES_FILE));
        JFXProjectUtils.deleteFile(projectDir.getFileObject(NONDEF2_PROJECT_PROPERTIES_FILE));
        CONFIGS.reset();
        ep.setProperty("javafx.param.0.name", "par1");
        ep.setProperty("javafx.param.0.value", "val1");
        ep.setProperty("javafx.param.1.name", "par2");
        ep.setProperty("javafx.param.1.hidden", "true");
        ep.setProperty("javafx.param.2.name", "par3");
        JFXProjectUtils.saveToFile(projectDir, PROJECT_PROPERTIES_FILE, ep);
        CONFIGS.read();
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par1 value=val1 name=par2 hidden=true name=par3"));
        ep.clear();
        ep.setProperty("javafx.param.2.name", "par1");
        ep.setProperty("javafx.param.2.hidden", "true");
        ep.setProperty("javafx.param.4.name", "par2");
        ep.setProperty("javafx.param.4.value", "val2");
        ep.setProperty("javafx.param.5.name", "par4");
        JFXProjectUtils.saveToFile(projectDir, NONDEF1_PROJECT_PROPERTIES_FILE, ep);
        CONFIGS.read();
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par1 value=val1 name=par2 hidden=true name=par3 {nondef_test_config_1} name=par1 hidden=true name=par2 value=val2 name=par4"));
        System.out.println("DEBUG");
        System.out.println(CONFIGS.paramsToString());
        ep.clear();
        ep.setProperty("javafx.param.3.name", "par1");
        ep.setProperty("javafx.param.4.name", "par2");
        ep.setProperty("javafx.param.4.value", "val222");
        ep.setProperty("javafx.param.5.name", "par4");
        ep.setProperty("javafx.param.5.hidden", "true");
        JFXProjectUtils.saveToFile(projectDir, NONDEF2_PROJECT_PROPERTIES_FILE, ep);
        CONFIGS.read();
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par1 value=val1 name=par2 hidden=true name=par3 {nondef_test_config_1} name=par1 hidden=true name=par2 value=val2 name=par4 {nondef_test_config_2} name=par1 name=par2 value=val222 name=par4 hidden=true"));
        //System.out.println("DEBUG");
        //System.out.println(CONFIGS.paramsToString());
        assertTrue(CONFIGS.hasDefaultParamTransparent("par1"));
        assertFalse(CONFIGS.hasDefaultParamTransparent("par2"));
        assertTrue(CONFIGS.hasDefaultParamTransparent("par3"));
        assertFalse(CONFIGS.hasDefaultParamTransparent("par4"));
        assertFalse(CONFIGS.hasParamTransparent(NONDEF1, "par1"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF1, "par2"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF1, "par3"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF1, "par4"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF2, "par1"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF2, "par2"));
        assertTrue(CONFIGS.hasParamTransparent(NONDEF2, "par3"));
        assertFalse(CONFIGS.hasParamTransparent(NONDEF2, "par4"));
        CONFIGS.setActive(NONDEF1);
        CONFIGS.eraseActiveParamTransparent("par2");
        assertFalse(CONFIGS.hasDefaultParamTransparent("par2"));
        assertFalse(CONFIGS.hasActiveParamTransparent("par2"));
        CONFIGS.addDefaultParamTransparent("par2", "val222");
        assertTrue(CONFIGS.hasParamTransparent(NONDEF2, "par2"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par1 value=val1 name=par2 value=val222 name=par3 {nondef_test_config_1} name=par1 hidden=true name=par4 {nondef_test_config_2} name=par1 name=par4 hidden=true"));
        CONFIGS.eraseDefaultParamTransparent("par1");
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.paramsToString(),"MultiProperty: {null} name=par2 value=val222 name=par3 {nondef_test_config_1} name=par4 {nondef_test_config_2} name=par1 name=par4 hidden=true"));
    }

    public void testCustomManifestEntries() throws Exception {
        assertNotNull(CONFIGS);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.manifestEntriesToString(),"MultiProperty: empty"));
        assertEquals(CONFIGS.getNoOfDefaultManifestEntries(), 0);
        assertEquals(CONFIGS.getNoOfActiveManifestEntries(), 0);
        // ensure CONFIGS is in required state before transparency testing
        CONFIGS.eraseDefaultManifestEntriesTransparent();
        CONFIGS.eraseConfig(NONDEF1);
        CONFIGS.eraseConfig(NONDEF2);
        // add parameters to default config
        CONFIGS.addDefaultManifestEntryTransparent("entry1"); // nameless = argument
        CONFIGS.addDefaultManifestEntryTransparent("entry2", "val2"); // named
        CONFIGS.addDefaultManifestEntryTransparent("entry3", "val3");
        assertTrue(CONFIGS.hasDefaultManifestEntryTransparent("entry1"));
        assertFalse(CONFIGS.hasDefaultManifestEntryValueTransparent("entry1"));
        assertTrue(CONFIGS.hasDefaultManifestEntryTransparent("entry2"));
        assertTrue(CONFIGS.hasDefaultManifestEntryValueTransparent("entry2"));
        assertTrue(CONFIGS.hasDefaultManifestEntryTransparent("entry2", "val2"));
        assertEquals(CONFIGS.getNoOfDefaultManifestEntries(), 3);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultManifestEntriesTransparentAsString(), "entry1, entry2: val2, entry3: val3"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getManifestEntriesTransparentAsString(DEFAULT), "entry1, entry2: val2, entry3: val3"));
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.manifestEntriesToString(),"MultiProperty: {null} name=entry1 name=entry2 value=val2 name=entry3 value=val3"));
        CONFIGS.eraseDefaultManifestEntryTransparent("dummy");
        assertEquals(CONFIGS.getNoOfDefaultManifestEntries(), 3);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultManifestEntriesTransparentAsString(), "entry1, entry2: val2, entry3: val3"));
        CONFIGS.eraseDefaultManifestEntryTransparent("entry2");
        assertEquals(CONFIGS.getNoOfDefaultManifestEntries(), 2);
        assertTrue(JFXProjectProperties.isEqual(CONFIGS.getDefaultManifestEntriesTransparentAsString(), "entry1, entry3: val3"));
    }
    
}
