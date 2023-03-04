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
package org.netbeans.modules.css.editor.module.main;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.GrammarElement;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;

/**
 *
 * @author mfukala@netbeans.org
 */
public class StandardPropertiesHelpResolverTest extends NbTestCase {

    public StandardPropertiesHelpResolverTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //allow the InstalledFilesLocator to work
        System.setProperty("netbeans.dirs", System.getProperty("cluster.path.final"));//NOI18N
    }

    public void testPropertyHelp() {
        assertPropertyHelp("animation");
    }
    
    public void testProperty_direction() {
        //not whole page but just stripped part
        assertPropertyHelpSnipped("direction");
    }
    
    public void testPropertiesWithExceptionInAnchorName() {
        // Was fixed in recent releases, no this test just checks, that the
        // documentation file/resolving does not regress
        //
        // Both line-break and transform were affected, but are not anymore
        // (offending properties are listed in
        // StandardPropertiesHelpResolver#propertyNamesTranslationTable
        assertPropertyHelpSnipped("line-break");
        assertPropertyHelpSnipped("transform");
    }
    
    public void testRubyProperties() {
        //ruby specification doesn't use "dfn" tag for property refs but "a" tag 
        assertPropertyHelpSnipped("ruby-merge");
        assertPropertyHelpSnipped("ruby-align");
    }
    
    public void testGetHelpForAllCSS3StandardProperties() {
        for(PropertyDefinition prop : Properties.getPropertyDefinitions(null)) {
            if(!Css3Utils.isVendorSpecificProperty(prop.getName()) 
                    && !GrammarElement.isArtificialElementName(prop.getName())) {
                
                CssModule module = prop.getCssModule();
                if(module == null) {
                    continue;
                }
                if(module instanceof BrowserSupportModule) {
                    continue;
                }
                if("http://www.w3.org/TR/CSS2".equals(module.getSpecificationURL())) {
                    continue;
                }
                assertPropertyHelp(prop.getName());
            }
        }
    }
    
    private String assertPropertyHelpSnipped(String propertyName) {
        String content = assertPropertyHelp(propertyName);
        assertTrue(content.startsWith("<base"));
        return content;
    }
    
    private String assertPropertyHelp(String propertyName) {
        StandardPropertiesHelpResolver instance = new StandardPropertiesHelpResolver();
        PropertyDefinition property = Properties.getPropertyDefinition( propertyName);
        assertNotNull(property);
        String helpContent = instance.getHelp(null, property);
//        System.out.println(helpContent);
        
        assertNotNull(String.format("Null help for property %s from module %s", propertyName, property.getCssModule().getDisplayName()), helpContent);
        
        return helpContent;
        
    }

}
