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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.util.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;

/** Simulation for bug 40734.
 *
 * @author Jaroslav Tulach
 */
public class CookieActionIsTooSlowTest extends NbTestCase implements PropertyChangeListener {
    
    static {
        NodeActionsInfraHid.install();
    }

    public CookieActionIsTooSlowTest(String name) {
        super(name);
    }
    
    private SimpleCookieAction a1;
    private Node[] arr;
    private int propertyChange;
    
    protected void setUp() throws Exception {
        a1 = SystemAction.get(SimpleCookieAction.class);
        a1.addPropertyChangeListener(this);
        int count = 10;
        arr = new Node[count];
        for (int i = 0; i < count; i++) {
            arr[i] = new FilterNode(new CookieNode("n" + i));
        }
    }
    
    protected void tearDown() throws Exception {
        a1.removePropertyChangeListener(this);
    }
    
    /**
     * in order to run in awt event queue
     * fix for #39789
     */
    protected boolean runInEQ() {
        return true;
    }
    
    public void propertyChange(PropertyChangeEvent ev) {
        propertyChange++;
    }
    
    public void testSelectionOfMoreNodesMakesToManyCallsToActionEnableMethodIssue40734() throws Exception {
        
        assertFalse("No nodes are enabled", a1.isEnabled());
        assertEquals("One call to enabled method", 1, a1.queried);
        a1.queried = 0;
        
        NodeActionsInfraHid.setCurrentNodes(arr);
        
        assertTrue("All nodes have open cookie", a1.isEnabled());
        assertEquals("The enable method has been called once", 1, a1.queried);
        
        assertEquals("Listener changed once", 1, propertyChange);
    }
    
    
    public static class SimpleCookieAction extends CookieAction {
        private int queried;
        
        protected int mode() {
            return MODE_ALL;
        }
        
        
        protected Class[] cookieClasses() {
            return new Class[] {OpenCookie.class};
        }
        public static final List runOn = new ArrayList(); // List<List<Node>>
        protected void performAction(Node[] activatedNodes) {
            runOn.add(Arrays.asList(activatedNodes));
        }
        public String getName() {
            return "SimpleCookieAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
        
        protected boolean enable(Node[] activatedNodes) {
            queried++;
            
            boolean retValue = super.enable(activatedNodes);
            return retValue;
        }
        
    }
    
    private static final class CookieNode extends AbstractNode {
        private static final class Open implements OpenCookie {
            public void open() {
                // do nothing
            }
        }
        public CookieNode(String name) {
            super(Children.LEAF);
            getCookieSet().add(new Open());
            setName(name);
        }
        public void setHasCookie(boolean b) {
            if (b && getCookie(OpenCookie.class) == null) {
                getCookieSet().add(new Open());
            } else if (!b) {
                OpenCookie o = getCookie(OpenCookie.class);
                if (o != null) {
                    getCookieSet().remove(o);
                }
            }
        }
    }
    
    
}

