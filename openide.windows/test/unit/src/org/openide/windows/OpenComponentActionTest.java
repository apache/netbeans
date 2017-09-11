/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.openide.windows;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.Action;
import javax.swing.Icon;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach
 */
public class OpenComponentActionTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(OpenComponentActionTest.class);
    }

    public OpenComponentActionTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }
    
    

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        TC.instance = null;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testMethodCallDirectly() {
        ActionEvent e = new ActionEvent(this, 0, "");
        TC tc = new TC();
        final String img = "org/openide/windows/icon.png";
        Action instance = TopComponent.openAction(tc, "Ahoj", img, false);
        instance.actionPerformed(e);
        
        tc.close();
        
        assertEquals("Opened once", 1, tc.cntOpen);
        assertEquals("Activated once", 1, tc.cntRequest);
        
        Icon icon = (Icon) instance.getValue(Action.SMALL_ICON);
        assertEquals("Width", 133, icon.getIconWidth());
        assertEquals("Height", 133, icon.getIconHeight());
        assertEquals("Name", "Ahoj", instance.getValue(Action.NAME));
    }

    public void testMapInstantiation() {
        final String img = "org/openide/windows/icon.png";
        
        Map<String,Object> m = new HashMap<String,Object>() {
            @Override
            public Object get(Object key) {
                if ("component".equals(key)) {
                    return new TC();
                }
                if ("displayName".equals(key)) {
                    return "Ahoj";
                }
                if ("iconBase".equals(key)) {
                    return img;
                }
                return null;
            }
        };
        
        ActionEvent e = new ActionEvent(this, 0, "");
        Action instance = TopComponent.openAction(m);
        
        assertNull("No instance yet", TC.instance);
        instance.actionPerformed(e);
        
        assertNotNull("Instance created", TC.instance);
        TC tc = TC.instance;
        tc.close();
        
        assertEquals("Opened once", 1, tc.cntOpen);
        assertEquals("Activated once", 1, tc.cntRequest);
        
        Icon icon = (Icon) instance.getValue(Action.SMALL_ICON);
        assertEquals("Width", 133, icon.getIconWidth());
        assertEquals("Height", 133, icon.getIconHeight());
        assertEquals("Name", "Ahoj", instance.getValue(Action.NAME));
    }

    
    public static final class TC extends TopComponent {
        static TC instance;

        int cntOpen;
        int cntRequest;
        
        public TC() {
            assertNull("No previous one", instance);
            instance = this;
        }

        @Override
        public void open() {
            super.open();
            cntOpen++;
        }

        @Override
        public void requestActive() {
            super.requestActive();
            cntRequest++;
        }
    }
}
