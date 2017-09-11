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
        for (SideBarPosition pos : c.keySet()) {
            List ll = c.get(pos);
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
