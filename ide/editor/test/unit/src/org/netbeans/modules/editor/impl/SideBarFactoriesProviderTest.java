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
package org.netbeans.modules.editor.impl;

import java.net.URL;
import java.util.List;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.EditorTestLookup;
import org.netbeans.modules.editor.impl.CustomizableSideBar.SideBarPosition;

/**
 *
 * @author
 * sdedic
 */
public class SideBarFactoriesProviderTest extends NbTestCase {

    public SideBarFactoriesProviderTest(String name) {
        super(name);
    }
    
    protected @Override void setUp() throws Exception {
        super.setUp();

        clearWorkDir();

        EditorTestLookup.setLookup(
            new URL[] {
                SideBarFactoriesProviderTest.class.getResource("/org/netbeans/modules/editor/impl/SideBarFactoriesProvider-test-layer.xml"),
            },
            new Object[] {},
            getClass().getClassLoader()
        );
    }
    
    /**
     * Checks that the Provider accepts both new and old SideBars, and that they can be ordered
     * at will using position FO attribute
     */
    public void testAcceptBothFactories() throws Exception {
        SideBarFactoriesProvider p = MimeLookup.getLookup("text/x-type-A").lookup(SideBarFactoriesProvider.class);
        Map<SideBarPosition, List> c = p.getFactories();
        
        assertEquals(3, c.size());
        for (Map.Entry<SideBarPosition, List> entry : c.entrySet()) {
            SideBarPosition pos = entry.getKey();
            List ll = entry.getValue();
            if (SideBarPosition.WEST_NAME.equals(pos.getPositionName())) {
                Object o = ll.get(0);
                assertTrue(o instanceof SBF2);
                o = ll.get(1);
                assertTrue(o instanceof SBF1);
            } else {
                assertEquals(1, ll.size());
            }
        }
    }
    
    static Object c1() {
        return new SBF1(1);
    }
    
    static Object c2() {
        return new SBF2(2);
    }

    static Object c3() {
        return new SBF1(3);
    }
    
    static Object c4() {
        return new SBF2(4);
    }

    @SuppressWarnings("deprecation")
    public static class SBF1 extends ASBF implements org.netbeans.editor.SideBarFactory {
        int code;

        public SBF1(int code) {
            this.code = code;
        }
    }

    public static class SBF2 extends ASBF implements org.netbeans.spi.editor.SideBarFactory {
        int code;

        public SBF2(int code) {
            this.code = code;
        }
    }

    static class ASBF {
        public JComponent createSideBar(JTextComponent target) {
            return new JLabel();
        }
    }
}
