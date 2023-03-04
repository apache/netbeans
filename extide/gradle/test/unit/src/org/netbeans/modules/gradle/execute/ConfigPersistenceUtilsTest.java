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
package org.netbeans.modules.gradle.execute;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertNull;
import org.junit.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.gradle.api.execute.GradleExecConfiguration;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author sdedic
 */
public class ConfigPersistenceUtilsTest {
    private AuxiliaryConfStub aux = new AuxiliaryConfStub();
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private File dataFile(String fn) throws IOException, URISyntaxException {
        URL u = getClass().getResource(fn);
        return new File(u.toURI());
    }
    
    private File element2File(Element el) throws IOException {
        File f = tempFolder.newFile();

        // write out
        Document doc = XMLUtil.createDocument("test", "urn:foobar", null, null);
        doc.getDocumentElement().appendChild(doc.importNode(el, true));
        try (FileOutputStream os = new FileOutputStream(f)) {
            XMLUtil.write(doc, os, "UTF-8");
        }
        return f;
    }
    
    private Element file2Element(File f, String elementName, String namespace) throws IOException, SAXException {
        try (InputStream is = new FileInputStream(f)) {
            InputSource input = new InputSource(is);
            input.setSystemId(f.toURL().toString());
            Element root = XMLUtil.parse(input, false, true, /*XXX*/null, null).getDocumentElement();
            return XMLUtil.findElement(root, elementName, namespace);
        }
    }
    
    GradleExecConfiguration cfg;
    GradleExecConfiguration cfg2;
    GradleExecConfiguration cfg3;
    GradleExecConfiguration cfg4;
    GradleExecConfiguration cfg5;
    GradleExecConfiguration cfg6;
    
    void initConfigurations() {
        cfg = 
                GradleExecAccessor.createDefault();
        
        // null properties and cmdline
        cfg2 = 
                GradleExecAccessor.instance().create("custom2", null, null, "   \t");
        
        // nonnull but empty properties and cmdline
        cfg3 = 
                GradleExecAccessor.instance().create("custom3", null, Collections.emptyMap(), "");
        
        // non null displayname
        cfg4 = 
                GradleExecAccessor.instance().create("custom4", "Boo boo", null, "--arg1 arg2");
        
        // some real content
        Map<String, String> props = new HashMap<>();
        props.put("", "val");
        props.put(" spaced ", "val2");
        props.put("normal", " with spaces around ");
        props.put("justnull", null);
        props.put("novalue", "");
        
        cfg5 = 
                GradleExecAccessor.instance().create("custom5", null, props, " --nospace --evenafter ");
        
        props = new HashMap<>();
        props.put("normal", "value");
        cfg6 = 
                GradleExecAccessor.instance().create("custom6", null, props, "--without spaces");
    }
    
    /**
     * Checks the cannonical form of configuration(s) written out:
     * - empty property map or empty (whitespace only) argument list must not be written
     * - property names should be trimmed
     * - property values must preserve whitespace
     * - null-valued properties and empty-valued are recorded just by their names.
     */
    @Test
    public void testWriteConfigurations() throws Exception {
        initConfigurations();
        List<GradleExecConfiguration> confs = Arrays.asList(cfg, cfg2, cfg3, cfg4, cfg5, cfg6);
        ConfigPersistenceUtils.writeConfigurations(confs, aux, cfg.getId(), true);
        
        // assert that no private configuration was written:
        assertNotNull(aux.shared.confElement);
        assertNull(aux.nonShared.confElement);
        
        File written = element2File(aux.shared.confElement);
        NbTestCase.assertFile(written, dataFile("writeConfigurations.pass.xml"));
    }
    
    @Test
    public void testWriteConfigurationsWithActive() throws Exception {
        initConfigurations();
        List<GradleExecConfiguration> confs = Arrays.asList(cfg, cfg2, cfg3);
        ConfigPersistenceUtils.writeConfigurations(confs, aux, cfg2.getId(), true);
        ConfigPersistenceUtils.writeConfigurations(confs, aux, cfg2.getId(), false);

        File writtenShared = element2File(aux.shared.confElement);
        File writtenNonshared = element2File(aux.nonShared.confElement);
        NbTestCase.assertFile(writtenShared, dataFile("writeConfigurationsWithActive-shared.pass.xml"), null, null);
        NbTestCase.assertFile(writtenNonshared, dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), null, null);
    }
    
    /**
     * Checks that if no configuraitons are written, then the XML still contains the
     * active configuration setting in private info.
     * @throws Exception 
     */
    @Test
    public void testWriteNoConfigurations() throws Exception {
        List<GradleExecConfiguration> confs = new ArrayList<>();
        
        ConfigPersistenceUtils.writeConfigurations(confs, aux, "custom", true);
        assertNull("No active config in shared", aux.shared.confElement);
        assertNull(aux.nonShared.confElement);
        
        ConfigPersistenceUtils.writeConfigurations(confs, aux, "custom", false);
        assertNull(aux.shared.confElement);
        assertNotNull(aux.nonShared.confElement);
       
        assertEquals("Active config must be recorded", "custom", aux.nonShared.confElement.getAttribute("active"));
        
        aux = new AuxiliaryConfStub();
        ConfigPersistenceUtils.writeConfigurations(confs, aux, null, true);
        assertNull(aux.shared.confElement);
        assertNull(aux.nonShared.confElement);
        ConfigPersistenceUtils.writeConfigurations(confs, aux, null, false);
        assertNull(aux.shared.confElement);
        assertNull(aux.nonShared.confElement);

        ConfigPersistenceUtils.writeConfigurations(confs, aux, GradleExecConfiguration.DEFAULT, true);
        assertNull(aux.shared.confElement);
        assertNull(aux.nonShared.confElement);
        ConfigPersistenceUtils.writeConfigurations(confs, aux, GradleExecConfiguration.DEFAULT, false);
        assertNull(aux.shared.confElement);
        assertNull(aux.nonShared.confElement);
    }
    
    /**
     * Checks that configurations are read properly. Configuration #5 is not compared as
     * its values are normalized on output, so it will be read back a little different.
     */
    @Test
    public void testReadConfigurations() throws Exception {
        initConfigurations();
        aux.shared.confElement = file2Element(dataFile("writeConfigurations.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);
        
        Map<String, GradleExecConfiguration> result = new HashMap<>();
        ConfigPersistenceUtils.readConfigurations(result, aux, true);
        
        assertEquals(6, result.size());
        assertConfiguration(cfg, result.get(GradleExecConfiguration.DEFAULT));
        assertConfiguration(cfg2, result.get("custom2"));
        assertConfiguration(cfg3, result.get("custom3"));
        assertConfiguration(cfg4, result.get("custom4"));
        assertConfiguration(cfg6, result.get("custom6"));
    }
    
    @Test
    public void testWriteActiveConfiguration() throws Exception {
        initConfigurations();
        aux.shared.confElement = file2Element(dataFile("writeConfigurationsWithActive-shared.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);
        aux.nonShared.confElement = file2Element(dataFile("writeConfigurationsWithActive-shared.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);
        ConfigPersistenceUtils.writeActiveConfiguration(aux, "custom2", false);
        
        File writtenShared = element2File(aux.shared.confElement);
        File writtenNonshared = element2File(aux.nonShared.confElement);
        NbTestCase.assertFile(writtenShared, dataFile("writeConfigurationsWithActive-shared.pass.xml"), null, null);
        NbTestCase.assertFile(writtenNonshared, dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), null, null);
    }
    
    @Test
    public void testWriteDefaultActiveConfiguration() throws Exception {
        initConfigurations();
        aux.shared.confElement = file2Element(dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);
        aux.nonShared.confElement = file2Element(dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);

        ConfigPersistenceUtils.writeActiveConfiguration(aux, GradleExecConfiguration.DEFAULT, false);
        
        File writtenShared = element2File(aux.shared.confElement);
        File writtenNonshared = element2File(aux.nonShared.confElement);
        NbTestCase.assertFile("Should be unchanged", writtenShared, dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), null, null);
        NbTestCase.assertFile("Active attribute must be removed", writtenNonshared, dataFile("writeConfigurationsWithActive-shared.pass.xml"), null, null);
    }

    @Test
    public void testDefaultActiveAndEmpty() throws Exception {
        initConfigurations();
        
        aux.shared.confElement = file2Element(dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);
        aux.nonShared.confElement = file2Element(dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), 
                "configurations", ConfigPersistenceUtils.CONFIG_NAMESPACE);

        ConfigPersistenceUtils.writeActiveConfiguration(aux, GradleExecConfiguration.DEFAULT, true);
        
        File writtenShared = element2File(aux.shared.confElement);
        NbTestCase.assertFile("Should be unchanged", writtenShared, dataFile("writeConfigurationsWithActive-nonshared.pass.xml"), null, null);
        assertNull(aux.nonShared.confElement);
    }
    
    public static void assertConfiguration(GradleExecConfiguration golden, GradleExecConfiguration compare) {
        assertNotNull("Configuration " + golden.getId() + " exists", compare);
        assertEquals(golden.getId(), compare.getId());
        assertEquals(golden.getName(), compare.getName());
        assertEquals(golden.getCommandLineArgs(), compare.getCommandLineArgs());
        assertEquals(golden.getProjectProperties(), compare.getProjectProperties());
    }
    
    static class ConfData {
        Element oldElement;
        Element confElement;

        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            if (confElement == null) {
                return null;
            }
            if (elementName.equals(confElement.getNodeName()) && Objects.equals(namespace, confElement.getNamespaceURI())) {
                return confElement;
            } else {
                return null;
            }
        }

        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            if (oldElement != null) {
                Assert.fail("Too many putElements");
            }
            oldElement = confElement;
            confElement = fragment;
        }

        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            if (confElement == null) {
                return false;
            }
            oldElement = confElement;
            confElement = null;
            return true;
        }
    }
    
    static class AuxiliaryConfStub implements AuxiliaryConfiguration {
        ConfData shared = new ConfData();
        ConfData nonShared = new ConfData();
        
        private ConfData d(boolean s) {
            return s ? shared :nonShared;
        }

        @Override
        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            return d(shared).getConfigurationFragment(elementName, namespace, shared);
        }

        @Override
        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
            d(shared).putConfigurationFragment(fragment, shared);
        }

        @Override
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            return d(shared).removeConfigurationFragment(elementName, namespace, shared);
        }
    }
}
