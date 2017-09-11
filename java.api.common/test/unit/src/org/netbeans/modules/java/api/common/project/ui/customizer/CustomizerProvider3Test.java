/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.project.ui.customizer;

import java.util.HashMap;
import java.util.Map;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.openide.util.Lookup;

/**
 * Test of SPI class org.netbeans.modules.java.api.common.project.ui.customizer.CustomizerProvider3
 *
 * @author Petr Somol
 */
public class CustomizerProvider3Test {

    public CustomizerProvider3Test() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        MockServices.setServices(CustomizerProvider3Test.MockCustomizerProvider.class);
    }

    @Test
    public void testShowCustomizer() {
         CustomizerProvider3 provider = Lookup.getDefault().lookup(CustomizerProvider3.class);
         assertNotNull(provider);
         MockCustomizerProvider mockProvider = (MockCustomizerProvider)provider;
         assertFalse(mockProvider.customizerOpen());
         
         provider.showCustomizer();
         assertTrue(mockProvider.customizerOpen());
    }
    
    @Test
    public void testCloseCancelCustomizer() {
         CustomizerProvider3 provider = Lookup.getDefault().lookup(CustomizerProvider3.class);
         assertNotNull(provider);
         MockCustomizerProvider mockProvider = (MockCustomizerProvider)provider;
         
         provider.showCustomizer();
         assertTrue(mockProvider.customizerOpen());

         MockCustomizer.invokeProjectModifyingAction();
         assertFalse(mockProvider.customizerOpen());
    }

    public static final class MockCustomizerProvider implements CustomizerProvider3 {

        private MockCustomizer customizerDialog = null;
        private Map<String,String> props = new HashMap<>();
        
        public MockCustomizerProvider() {
        }
        
        @Override
        public void cancelCustomizer() {
            customizerDialog = null;
        }

        @Override
        public void showCustomizer(String preselectedCategory, String preselectedSubCategory) {
            showCustomizer();
        }

        @Override
        public void showCustomizer() {
            customizerDialog = new MockCustomizer();
        }
        
        public void loadProperties(Map<String,String> properties) {
            this.props.putAll(properties);
        }
        
        public void saveProperties(Map<String,String> properties) {
            properties.putAll(this.props);
        }
        
        public boolean customizerOpen() {
            return customizerDialog != null;
        }
        
    }
    
    /**
     *
     */
    public static final class MockCustomizer{

        // user invokes an action that changes
        // project metafiles to such extent that project
        // properties dialog needs to be closed
        // before making changes to project metafiles
        public static void invokeProjectModifyingAction() {
            CustomizerProvider3 provider = Lookup.getDefault().lookup(CustomizerProvider3.class);
            provider.cancelCustomizer();
            // ..do whatever is needed
        }
        
    }
    
}
