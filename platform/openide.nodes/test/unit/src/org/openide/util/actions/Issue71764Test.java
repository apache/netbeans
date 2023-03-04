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

import java.io.IOException;
import org.netbeans.junit.*;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/** Tests if NodeAction initializes its listener list and resposes
 * to cookie changes. See http://www.netbeans.org/issues/show_bug.cgi?id=71764.
 */
public class Issue71764Test extends NbTestCase {
    
    public Issue71764Test(java.lang.String testName) {
        super(testName);
    }
    
    public void test71764() {
        MockServices.setServices(ContextProvider.class);
        
        ContextProvider provider = Lookup.getDefault().lookup(ContextProvider.class);
        assertNotNull ("ContextProvider is not null.", provider);
        
        NodeAction action = (NodeAction) new TestAction ();
        Node node = new TestNode();

        assertFalse("Global Action should not be enabled yet", action.isEnabled());
        
        ContextProvider.current = node;
        provider.getLookup().lookup(Object.class);
        
        assertTrue("Global Action is enabled", action.isEnabled());
    }
    
    class TestNode extends AbstractNode {
        public TestNode() {
            super(Children.LEAF);
            getCookieSet().add(new SaveCookie() {
                public void save() throws IOException {
                    System.out.println("Save cookie called");
                }
            });
        }
    }
    
    public static class ContextProvider implements ContextGlobalProvider, Lookup.Provider {
        static Node current;
        Lookup lookup = Lookups.proxy (this );

        public Lookup createGlobalContext() {
            return lookup;
        }
    
        public Lookup getLookup() {
            return current == null ? Lookup.EMPTY : current.getLookup();
        }
    }
    
    public static class TestAction extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
        }
        
        protected Class[] cookieClasses() {
            return new Class[] {SaveCookie.class};
        }
        
        protected void performAction(Node[] activatedNodes) {
            assert false;
        }
        public String getName() {
            return "TestAction";
        }
        public HelpCtx getHelpCtx() {
            return null;
        }
        protected boolean asynchronous() {
            return false;
        }
    }
}
