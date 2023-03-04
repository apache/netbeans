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
package org.netbeans.modules.css.editor.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.PropertySupportResolver;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.GroupGrammarElement;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyCategory;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class BrowserSpecificDefinitionParser extends PropertySupportResolver {

    private String resourcePath;
    private Browser browser;
    private CssModule module;
    private final Set<String> supportedPropertiesNames = new HashSet<>();
    private final Map<String, PropertyDefinition> vendorSpecificProperties = new HashMap<>();

    public BrowserSpecificDefinitionParser(String resourcePath, Browser browser, CssModule module) {
        this.resourcePath = resourcePath;
        this.browser = browser;
        this.module = module;
        load();
    }

    private void load() {
        ResourceBundle bundle = NbBundle.getBundle(resourcePath);

        Enumeration<String> keys = bundle.getKeys();
        while (keys.hasMoreElements()) {
            String name = keys.nextElement();
            String value = bundle.getString(name).trim();

            if (value.isEmpty()) {
                continue; //ignore empty keys, the meaning is the same as if there was no such key in the file
            }
            char firstValueChar = value.charAt(0);

            //parse bundle key - there might be more properties separated by semicolons
            StringTokenizer nameTokenizer = new StringTokenizer(name, ";"); //NOI18N
            Collection<String> propertyNames = new ArrayList<>();
            while (nameTokenizer.hasMoreTokens()) {
                String parsed_name = nameTokenizer.nextToken().trim();
                propertyNames.add(parsed_name);
            }

            for (String propertyName : propertyNames) {

                if (propertyName.startsWith(browser.getVendorSpecificPropertyPrefix())) {
                    //vendor specific property
                    vendorSpecificProperties.put(propertyName, new PropertyDefinition(propertyName, value, browser.getPropertyCategory(), module));
                    supportedPropertiesNames.add(propertyName);

                } else {
                    //standard property
                    switch (firstValueChar) {
                        case '!':
                            //experimental property only
                            String vendorSpecificPropertyName = createVendorSpecificPropertyName(browser.getVendorSpecificPropertyPrefix(), propertyName);
                            vendorSpecificProperties.put(vendorSpecificPropertyName, new ProxyProperty(vendorSpecificPropertyName, browser.getPropertyCategory(), propertyName));
                            supportedPropertiesNames.add(vendorSpecificPropertyName);
                            break;
                        case '+':
                            //standard property support only
                            supportedPropertiesNames.add(propertyName);
                            break;
                        case '*':
                            //standard + experimental property                            
                            vendorSpecificPropertyName = createVendorSpecificPropertyName(browser.getVendorSpecificPropertyPrefix(), propertyName);
                            vendorSpecificProperties.put(vendorSpecificPropertyName, new ProxyProperty(vendorSpecificPropertyName, browser.getPropertyCategory(), propertyName));
                            supportedPropertiesNames.add(propertyName);
                            supportedPropertiesNames.add(vendorSpecificPropertyName);
                            break;
                        case '-':
                            //discontinued support
                            //just ignore for now == not supported, later we may utilize the info somehow
                            break;

                        default:
                            //even standard property can be vendor specific (zoom for webkit)
                            vendorSpecificProperties.put(propertyName, new PropertyDefinition(propertyName, value, browser.getPropertyCategory(), module));
                            supportedPropertiesNames.add(propertyName);

                    }

                }

            }

        }

    }

    private String createVendorSpecificPropertyName(String prefix, String standardPropertyName) {
        assert prefix != null;
        assert !prefix.trim().isEmpty();
        
        return new StringBuilder().append(prefix).append(standardPropertyName).toString();
    }

    @Override
    public boolean isPropertySupported(String propertyName) {
        return supportedPropertiesNames.contains(propertyName);
    }

    public Map<String, PropertyDefinition> getVendorSpecificProperties() {
        return vendorSpecificProperties;
    }

    private class ProxyProperty extends PropertyDefinition {

        private String delegateToPropertyName;

        private static final String EMPTY_GRAMMAR = "[]";
        
        public ProxyProperty(String name, PropertyCategory category, String delegateToPropertyName) {
            super(name, null, category, module);
            this.delegateToPropertyName = delegateToPropertyName;
        }
        
        @Override
        public synchronized GroupGrammarElement getGrammarElement(FileObject context) {
            //try to get the normal property first
            PropertyDefinition p = Properties.getPropertyDefinition(delegateToPropertyName);
            if(p == null) {
                //the browser specific definition may address an invisible property
                p = Properties.getPropertyDefinition(delegateToPropertyName, true);
            }
            
            if (p == null) {
                Logger.getAnonymousLogger().warning(String.format("Cannot fine property %s referred in %s", delegateToPropertyName, resourcePath)); //NOI18N
                return super.getGrammarElement(context);
            }
            
            return p.getGrammarElement(context);
        }

        @Override
        public String getGrammar() {
            //the property have empty grammar as the getGrammarElement is overridden
            return EMPTY_GRAMMAR;
        }

        @Override
        public String toString() {
            return "ProxyProperty(name=" + getName() + ", delegate=" + delegateToPropertyName + ")";
        }
        
    }
}
