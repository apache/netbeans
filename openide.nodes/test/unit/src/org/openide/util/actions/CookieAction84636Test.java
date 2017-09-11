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
import javax.swing.Action;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.nodes.NodeEvent;
import org.openide.nodes.NodeListener;
import org.openide.nodes.NodeMemberEvent;
import org.openide.nodes.NodeReorderEvent;
import org.openide.util.HelpCtx;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.ProxyLookup;

/** Check problem with recursive loop.
 * @author Jaroslav Tulach
 */
public class CookieAction84636Test extends NbTestCase 
implements NodeListener, PropertyChangeListener {
    
    public CookieAction84636Test(String name) {
        super(name);
    }
    
    private SimpleCookieAction a1;
    private CookieNode n1;
    
    protected void setUp() throws Exception {
        a1 = SystemAction.get(SimpleCookieAction.class);
        n1 = new CookieNode();
        n1.setName("n1");
    }
    
    protected boolean runInEQ() {
        return true;
    }
    
    public void testBasicUsage() throws Exception {
        a1.addPropertyChangeListener(this);
        
        FilterNode f1 = new FilterNode(n1);

        f1.addPropertyChangeListener(this);
        f1.addNodeListener(this);
        Action context = a1.createContextAwareInstance(f1.getLookup());
        
        
    }

    public void propertyChange(PropertyChangeEvent evt) {
    }

    public void childrenAdded(NodeMemberEvent ev) {
    }

    public void childrenRemoved(NodeMemberEvent ev) {
    }

    public void childrenReordered(NodeReorderEvent ev) {
    }

    public void nodeDestroyed(NodeEvent ev) {
    }

    
    public static class SimpleCookieAction extends CookieAction {
        protected int mode() {
            return MODE_EXACTLY_ONE;
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

        private boolean called = false;
        protected boolean enable(Node[] activatedNodes) {
            boolean retValue;
            if (called) {
                fail("Called already!");
            }
            called =true;
            
            retValue = super.enable(activatedNodes);
            return retValue;
        }
    }
    
    private static final class CookieNode extends AbstractNode {
        private static final class Open implements OpenCookie {
            public void open() {
                // do nothing
            }
        }
        public CookieNode() {
            super(Children.LEAF);
            getCookieSet().add(new Open());
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

