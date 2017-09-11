/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
        return parser.getVendorSpecificProperties().get(propertyName);
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
