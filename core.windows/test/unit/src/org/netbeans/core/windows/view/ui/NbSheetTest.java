/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.core.windows.view.ui;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javax.swing.SwingUtilities;
import org.netbeans.core.windows.actions.GlobalPropertiesAction;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.modules.ModuleInfo;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Jaroslav Tulach
 */
@RandomlyFails
public class NbSheetTest extends NbTestCase {
    static {
        System.setProperty("org.openide.windows.DummyWindowManager.VISIBLE", "false");    
    }
    
    NbSheet s;
    GlobalPropertiesAction a;
    static TopComponent tc;
    
    public NbSheetTest(String testName) {
        super(testName);
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    @Override
    protected String logRoot() {
        return NbSheetTest.class.getPackage().getName();
    }
    
    @Override
    protected int timeOut() {
        return 15000;
    }

    @Override
    protected void setUp() throws Exception {
        Lookup.getDefault().lookup(ModuleInfo.class);
        SwingUtilities.invokeAndWait(new Runnable() {

            @Override
            public void run() {
                s = NbSheet.findDefault();
            }
        });
        assertNotNull("Sheet found", s);
        assertFalse("Not yet visible", s.isShowing());
        a = GlobalPropertiesAction.get(GlobalPropertiesAction.class);
        if (tc == null) {
            tc = new TopComponent();
        }
    }

    public void testIssue97069EgUseSetActivatedNodesNull() throws Exception {
        class Empty implements Runnable {
            public void run() { }
        }
        Empty empty = new Empty();
        
        class R implements Runnable {
            N node = new N("node1");
            public void run() {
                tc.setActivatedNodes(new Node[] { node });
                tc.open();
                tc.requestActive();

                assertTrue("action enabled", a.isEnabled());
                a.actionPerformed(new ActionEvent(a, 0, ""));
            }
        }
        R activate = new R();
        SwingUtilities.invokeAndWait(activate);
        SwingUtilities.invokeAndWait(empty);
        
        for (int i = 0; i < 5; i++) {
            if (s == TopComponent.getRegistry().getActivated()) {
                break;
            }
            Thread.sleep(500);
        }
        assertEquals("sheet activated", s, TopComponent.getRegistry().getActivated());
        assertEquals("One node displayed", 1, s.getNodes().length);
        assertEquals("it is node", activate.node, s.getNodes()[0]);
        assertEquals("No activated nodes", null, s.getActivatedNodes());

        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                s.close();
            }
        });
        
        final N another = new N("another");
        
        SwingUtilities.invokeAndWait( new Runnable() {

            @Override
            public void run() {
                tc.setActivatedNodes(new Node[] { another });
                tc.requestActive();
            }
        });
                
        
        class R2 implements Runnable {
            public void run() {
                assertTrue("action enabled", a.isEnabled());
                a.actionPerformed(new ActionEvent(a, 0, ""));
            }
        }
        R2 anotherAct = new R2();
        SwingUtilities.invokeAndWait(anotherAct);
        SwingUtilities.invokeAndWait(empty);

        for (int i = 0; i < 5; i++) {
            if (s == TopComponent.getRegistry().getActivated()) {
                break;
            }
            Thread.sleep(500);
        }
        assertEquals("sheet activated another time", s, TopComponent.getRegistry().getActivated());
        
        assertEquals("One node displayed", 1, s.getNodes().length);
        assertEquals("it is another", another, s.getNodes()[0]);
        assertEquals("No activated nodes", null, s.getActivatedNodes());
        
    }

    private static final class N extends AbstractNode {
        public N(String n) {
            super(Children.LEAF);
            setName(n);
        }
    }


    public void testMemoryLeakIssue125057() throws Exception {
        final NbSheet sheet = NbSheet.getDefault();
        SwingUtilities.invokeAndWait( new Runnable() {
            @Override
            public void run() {
                N node = new N("node1");
                tc.setActivatedNodes(new Node[] { node });
                tc.open();
                tc.requestActive();

                NbSheet sheet = NbSheet.getDefault();
                sheet.open();
                Node[] activated = sheet.getNodes();
                assertNotNull(activated);
                assertEquals(activated.length, 1);
                assertEquals(activated[0], node);
                sheet.close();
                assertEquals("NbSheet's nodes are gone", 0, sheet.getNodes().length );

            }
        });
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                List<Node> arr = Arrays.asList(sheet.getNodes());
                assertEquals("NbSheet's nodes point to tc: " + arr, 1, arr.size());
                assertEquals("It is the one of tc", tc.getActivatedNodes()[0], arr.get(0));
            }
        });
    }
}


