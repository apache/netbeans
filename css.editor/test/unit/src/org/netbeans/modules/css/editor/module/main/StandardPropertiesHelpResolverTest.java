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
        assertPropertyHelpSnipped("line-break");
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
        assertTrue(content.startsWith("<h"));
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
