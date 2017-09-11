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
