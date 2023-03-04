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

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.modules.css.editor.Css3Utils;
import org.netbeans.modules.css.editor.module.BrowserSpecificDefinitionParser;
import org.netbeans.modules.css.editor.module.spi.Browser;
import org.netbeans.modules.css.editor.module.spi.CssEditorModule;
import org.netbeans.modules.css.editor.module.spi.HelpResolver;
import org.netbeans.modules.css.editor.module.spi.PropertySupportResolver;
import org.netbeans.modules.css.editor.module.spi.PropertySupportResolver.Factory;
import org.netbeans.modules.css.lib.api.CssModule;
import org.netbeans.modules.css.lib.api.properties.Properties;
import org.netbeans.modules.css.lib.api.properties.PropertyDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author mfukala@netbeans.org
 */
public class BrowserSupportModule extends CssEditorModule implements CssModule {

    private Browser browser;
    private BrowserSpecificDefinitionParser parser;
    
    private final String DEFINITION_FILES_BASE = "org/netbeans/modules/css/editor/module/main/browsers/"; //NOI18N

    public BrowserSupportModule(Browser browser, String propertiesDefinitionFileName) {
        this.browser = browser;
        parser = new BrowserSpecificDefinitionParser(DEFINITION_FILES_BASE + propertiesDefinitionFileName, browser, this);
    }

    protected Browser getBrowser() {
        return browser;
    }
    
    @Override
    public Collection<Browser> getExtraBrowsers(FileObject file) {
        return Collections.singleton(browser);
    }

    @Override
    public Collection<String> getPropertyNames(FileObject file) {
        return parser.getVendorSpecificProperties().keySet();
    }

    @Override
    public PropertyDefinition getPropertyDefinition(String propertyName) {
        PropertyDefinition pd = parser.getVendorSpecificProperties().get(propertyName);
        if (pd != null || propertyName == null) {
            return pd;
        } else {
            return parser.getVendorSpecificProperties().get(propertyName.toLowerCase());
        }
    }

    @Override
    public Factory getPropertySupportResolverFactory() {
        return new PropertySupportResolver.Factory() {
            @Override
            public PropertySupportResolver createPropertySupportResolver(Browser browser) {
                return browser == getBrowser() ? parser : null;
            }
        };
    }

    @Override
    public Collection<HelpResolver> getHelpResolvers(FileObject context) {
        return Collections.<HelpResolver>singleton(new HelpResolver() {

            @Override
            public String getHelp(FileObject context, PropertyDefinition property) {
                if(property.getName().startsWith(getBrowser().getVendorSpecificPropertyPrefix())) {
                    //try to delegate to the corresponding standard property help
                    String standardPropertyName = property.getName().substring(getBrowser().getVendorSpecificPropertyPrefix().length());
                    PropertyDefinition standardPropertyDefinition = Properties.getPropertyDefinition(standardPropertyName);
                    if(standardPropertyDefinition != null) {
                        StandardPropertiesHelpResolver resolver = new StandardPropertiesHelpResolver();
                        String help = resolver.getHelp(context, standardPropertyDefinition);
                        if(help != null) {
                            return help;
                        }
                    }
                    
                    return NbBundle.getMessage(this.getClass(), "completion-help-no-documentation-found"); 
                } else {
                    return null;
                }
            }

            @Override
            public URL resolveLink(FileObject context, PropertyDefinition property, String link) {
                return null;
            }

            @Override
            public int getPriority() {
                return 10000;
            }
            
        });
    }

    @Override
    public String getName() {
        return browser.getName();
    }

    @Override
    public String getDisplayName() {
        return String.format("%s CSS Extensions", browser.getName());
    }

    @Override
    public String getSpecificationURL() {
        return null;
    }
    
    protected class SupportAllFactory implements PropertySupportResolver.Factory {

        @Override
        public PropertySupportResolver createPropertySupportResolver(Browser browser) {
            return getBrowser().equals(browser) ? new  SupportAll(browser) : null;
        }
        
    }
    
    //support all standard properties + own extra properties
    protected class SupportAll extends PropertySupportResolver {

        private Browser browser;

        public SupportAll(Browser browser) {
            this.browser = browser;
        }
                
        @Override
        public boolean isPropertySupported(String propertyName) {
            if(Css3Utils.isVendorSpecificProperty(propertyName)) {
                if(propertyName.startsWith(browser.getVendorSpecificPropertyPrefix())) {
                    return true;
                } else {
                    return false;
                }
            } else {                
                return true;
            }
        }
        
    }
    
}
