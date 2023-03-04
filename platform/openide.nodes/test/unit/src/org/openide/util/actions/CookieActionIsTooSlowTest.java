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

