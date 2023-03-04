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
package org.netbeans.modules.ide.ergonomics.fod;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Hector Espert
 */
public class ConfigurationPanelTest extends NbTestCase {
    
    private ConfigurationPanel configurationPanel;

    public ConfigurationPanelTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    @Override
    protected void setUp() throws Exception {
        configurationPanel = new ConfigurationPanel(JPanel::new); 
    }
    
    
    public void testSetInfo() throws Exception {
        FeatureInfo featureInfo = FeatureInfo.create("TestFactory", 
                this.getClass().getResource("FeatureInfo.xml"), 
                this.getClass().getResource("TestBundle.properties"));
                
        Collection<UpdateElement> toInstall = new ArrayList<>();
        
        Collection<FeatureInfo.ExtraModuleInfo> missingModules = new ArrayList<>();
        
        Map<FeatureInfo.ExtraModuleInfo, FeatureInfo> extrasMap = new HashMap<>();
        
        FeatureInfo.ExtraModuleInfo extraModuleInfo = new FeatureInfo.ExtraModuleInfo("Feature", null, null);
        extrasMap.put(extraModuleInfo, featureInfo);
        
        configurationPanel.setInfo(featureInfo, null, toInstall, missingModules, extrasMap, true);
    }
    
    
    public void testCollectExtraModulesTextsFromFeatures() throws IOException {
        Collection<FeatureInfo> features = new ArrayList<>();
        
        List<String> texts = configurationPanel.collectExtraModulesTextsFromFeatures(features, true);
        assertNotNull(texts);
        assertTrue(texts.isEmpty());
        
        FeatureInfo featureInfo = FeatureInfo.create("TestFactory", 
                this.getClass().getResource("FeatureInfo.xml"), 
                this.getClass().getResource("TestBundle.properties"));
        
        features.add(featureInfo);
        
        texts = configurationPanel.collectExtraModulesTextsFromFeatures(features, true);
        assertNotNull(texts);
        assertFalse(texts.isEmpty());
        
        List<String> expected = new ArrayList<>();
        expected.add("Required");
        assertEquals(expected, texts);
        
        texts = configurationPanel.collectExtraModulesTextsFromFeatures(features, false);
        assertNotNull(texts);
        assertFalse(texts.isEmpty());
        
        expected = new ArrayList<>();
        expected.add("Recommended");
        assertEquals(expected, texts);
        
        FeatureInfo otherFeatureInfo = FeatureInfo.create("TestFactory", 
                this.getClass().getResource("FeatureInfo.xml"), 
                this.getClass().getResource("TestBundle.properties"));
        
        features.add(otherFeatureInfo);
        
        texts = configurationPanel.collectExtraModulesTextsFromFeatures(features, true);
        assertNotNull(texts);
        assertFalse(texts.isEmpty());
        
        expected = new ArrayList();
        expected.add("Required");
        assertEquals(expected, texts);
        
        otherFeatureInfo = FeatureInfo.create("TestFactory", 
                this.getClass().getResource("FeatureInfo.xml"), 
                this.getClass().getResource("TestBundle2.properties"));
        
        features.add(otherFeatureInfo);
        
        texts = configurationPanel.collectExtraModulesTextsFromFeatures(features, true);
        assertNotNull(texts);
        assertFalse(texts.isEmpty());
        
        expected = new ArrayList();
        expected.add("Required");
        expected.add("Required2");
        assertEquals(expected, texts);
    }
    
    public void testGenerateDownloadMessageFromExtraModulesTexts() {
        List<String> extraModulesTexts = new ArrayList<>();
        
        String downloadText = configurationPanel.generateDownloadMessageFromExtraModulesTexts(extraModulesTexts);
        assertEquals("", downloadText);
        
        extraModulesTexts.add("JDK8");
        downloadText = configurationPanel.generateDownloadMessageFromExtraModulesTexts(extraModulesTexts);
        assertEquals("<html><body>JDK8</body></html>", downloadText);
        
        extraModulesTexts.add("JDK10.5");
        downloadText = configurationPanel.generateDownloadMessageFromExtraModulesTexts(extraModulesTexts);
        assertEquals("<html><body>JDK8<br>JDK10.5</body></html>", downloadText);
        
        extraModulesTexts.add("JDK14");
        downloadText = configurationPanel.generateDownloadMessageFromExtraModulesTexts(extraModulesTexts);
        assertEquals("<html><body>JDK8<br>JDK10.5<br>JDK14</body></html>", downloadText);
    }

}
