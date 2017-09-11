/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.j2seproject.api;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider.ConfigChangeListener;
import org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider.ExtensibleCategory;
import org.openide.util.Lookup;

/**
 * Test of SPI class org.netbeans.modules.java.j2seproject.api.J2SECategoryExtensionProvider
 * 
 * @author Petr Somol
 */
public class J2SECategoryExtensionProviderTest {
    
    public J2SECategoryExtensionProviderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        MockServices.setServices(J2SEMockRunProvider1.class, J2SEMockPackagingProvider.class, J2SEMockRunProvider2.class, J2SEMockApplicationProvider.class);
    }

    private Map<String, String> properties = new TreeMap<String, String>();
    
    /**
     * Test of J2SECategoryExtensionProvider.
     */
    @Test
    public void testCustomizerExtension() {
        System.out.println("testCustomizerExtension():");
        assertEquals(Lookup.getDefault().lookupAll(J2SECategoryExtensionProvider.class).size(), 4);

        java.util.List<J2SECategoryExtensionProvider> compProviders = new LinkedList<J2SECategoryExtensionProvider>();
        JPanel pane = new JPanel();
        int nextExtensionYPos = 0;
        for (J2SECategoryExtensionProvider compProvider : Lookup.getDefault().lookupAll(J2SECategoryExtensionProvider.class)) {
            if( compProvider.getCategory() == J2SECategoryExtensionProvider.ExtensibleCategory.RUN ) {
                if( addExtPanel(pane,compProvider,nextExtensionYPos) ) {
                    compProviders.add(compProvider);
                    nextExtensionYPos++;
                }
            }
        }
        System.out.println("registered extension components");
        assertEquals(compProviders.size(), 2);
        assertEquals(pane.getComponents().length, 2);
        for(J2SECategoryExtensionProvider provider: compProviders) {
            assertEquals( provider.getCategory(), J2SECategoryExtensionProvider.ExtensibleCategory.RUN);
        }
        for(Component comp : pane.getComponents()) {
            assertTrue(comp instanceof MockupComponent);
            assertEquals(((MockupComponent)comp).getState(), false);
        }
        assertNull(properties.get("MockupRunExtender1"));
        assertNull(properties.get("MockupRunExtender2"));
        assertNull(properties.get("MockupPackagingExtender"));
        assertNull(properties.get("MockupApplicationExtender"));
        System.out.println("OK");
        
        System.out.println("simulate event generated by user using the extension component");
        MockupComponent extComp = (MockupComponent)pane.getComponent(0);
        assertNotNull(extComp);
        extComp.setState(true);
        extComp.actionPerformed(null);
        assertEquals(properties.get("MockupRunExtender1"), "true");
        assertNull(properties.get("MockupRunExtender2"));
        assertNull(properties.get("MockupPackagingExtender"));
        assertNull(properties.get("MockupApplicationExtender"));
        System.out.println("OK");
        
        System.out.println("simulate event from outside the extension component");
        properties.put("MockupRunExtender2", "true");
        for(J2SECategoryExtensionProvider provider: compProviders) {
            provider.configUpdated(properties);
        }
        for(Component comp : pane.getComponents()) {
            assertTrue(comp instanceof MockupComponent);
            assertEquals(((MockupComponent)comp).getState(), true);
        }
        System.out.println("OK");
    }

    private boolean addExtPanel(JPanel p, J2SECategoryExtensionProvider compProvider, int gridY) {
        if (compProvider != null) {
            J2SECategoryExtensionProvider.ConfigChangeListener ccl = new J2SECategoryExtensionProvider.ConfigChangeListener() {
                @Override
                public void propertiesChanged(Map<String, String> updates) {
                    properties.putAll(updates);
                }
            };
            JComponent comp = compProvider.createComponent(null, ccl);
            if (comp != null) {
                java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
                constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
                constraints.gridx = 0;
                constraints.gridy = gridY;
                constraints.weightx = 1.0;
                p.add(comp, constraints);
                return true;
            }
        }
        return false;
    }

    public static final class J2SEMockRunProvider1 implements J2SECategoryExtensionProvider {

        MockupComponent extender;

        @Override
        public ExtensibleCategory getCategory() {
            return ExtensibleCategory.RUN;
        }

        @Override
        public JComponent createComponent(Project proj, ConfigChangeListener listener) {
            extender = new MockupComponent("MockupRunExtender1");
            extender.setState(false);
            extender.addListener(listener);
            return extender;
        }

        @Override
        public void configUpdated(Map<String, String> props) {
            String value = props.get("MockupRunExtender1");
            if(value != null) {
                if( "true".equals(value)) {
                    extender.setState(true);
                } else {
                    if( "false".equals(value)) {
                        extender.setState(false);
                    }
                }
            }
        }
    }

    public static final class J2SEMockRunProvider2 implements J2SECategoryExtensionProvider {

        MockupComponent extender;

        @Override
        public ExtensibleCategory getCategory() {
            return ExtensibleCategory.RUN;
        }

        @Override
        public JComponent createComponent(Project proj, ConfigChangeListener listener) {
            extender = new MockupComponent("MockupRunExtender2");
            extender.setState(false);
            extender.addListener(listener);
            return extender;
        }

        @Override
        public void configUpdated(Map<String, String> props) {
            String value = props.get("MockupRunExtender2");
            if(value != null) {
                if( "true".equals(value)) {
                    extender.setState(true);
                } else {
                    if( "false".equals(value)) {
                        extender.setState(false);
                    }
                }
            }
        }
    }

    public static final class J2SEMockPackagingProvider implements J2SECategoryExtensionProvider {

        MockupComponent extender;

        @Override
        public ExtensibleCategory getCategory() {
            return ExtensibleCategory.PACKAGING;
        }

        @Override
        public JComponent createComponent(Project proj, ConfigChangeListener listener) {
            extender = new MockupComponent("MockupPackagingExtender");
            extender.setState(false);
            extender.addListener(listener);
            return extender;
        }

        @Override
        public void configUpdated(Map<String, String> props) {
            String value = props.get("MockupPackagingExtender");
            if(value != null) {
                if( "true".equals(value)) {
                    extender.setState(true);
                } else {
                    if( "false".equals(value)) {
                        extender.setState(false);
                    }
                }
            }
        }
    }

    public static final class J2SEMockApplicationProvider implements J2SECategoryExtensionProvider {

        MockupComponent extender;

        @Override
        public ExtensibleCategory getCategory() {
            return ExtensibleCategory.APPLICATION;
        }

        @Override
        public JComponent createComponent(Project proj, ConfigChangeListener listener) {
            extender = new MockupComponent("MockupApplicationExtender");
            extender.setState(false);
            extender.addListener(listener);
            return extender;
        }

        @Override
        public void configUpdated(Map<String, String> props) {
            String value = props.get("MockupApplicationExtender");
            if(value != null) {
                if( "true".equals(value)) {
                    extender.setState(true);
                } else {
                    if( "false".equals(value)) {
                        extender.setState(false);
                    }
                }
            }
        }
    }

    public static final class MockupComponent extends JComponent implements ActionListener {

        public boolean state;
        private String name;

        private J2SECategoryExtensionProvider.ConfigChangeListener listener;

        public MockupComponent(String name) {
            state = false;
            this.name = name;
        }

        public void setState(boolean state) {
            this.state = state;
        }

        public boolean getState() {
            return state;
        }

        public void addListener(J2SECategoryExtensionProvider.ConfigChangeListener l) {
            listener = l;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Map<String,String> props = new TreeMap<String,String>();
            if( getState() ) {
                props.put(name, "true");
            } else {
                props.put(name, "false");
            }
            listener.propertiesChanged(props);
        }
    }

}
