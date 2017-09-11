/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.openide.nodes;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.netbeans.api.actions.Openable;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class NodeLookupDelayedTest extends NbTestCase {
    public NodeLookupDelayedTest(String name) {
        super(name);
    }

    
    public void testDelayedChangeIsNotified() {
        final Collection<String> pros = new HashSet<String>();
        DelayedNode dn = new DelayedNode();
        dn.addNodeListener(new NodeListener() {
            @Override
            public void childrenAdded(NodeMemberEvent ev) {
            }

            @Override
            public void childrenRemoved(NodeMemberEvent ev) {
            }

            @Override
            public void childrenReordered(NodeReorderEvent ev) {
            }

            @Override
            public void nodeDestroyed(NodeEvent ev) {
            }

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                pros.add(evt.getPropertyName());
            }
        });
        CloseCookie close = dn.getLookup().lookup(CloseCookie.class);
        assertNull("Null now", close);
        
        dn.oc = new OpenCookie() {
            @Override
            public void open() {
            }
        };
        
        Openable open = dn.getLookup().lookup(Openable.class);
        assertEquals("Found", dn.oc, open);
        
        assertEquals("One change: " + pros, 1, pros.size());
        assertEquals("Cookie change", Node.PROP_COOKIE, pros.iterator().next());
    }
    
    private static final class DelayedNode extends AbstractNode {
        OpenCookie oc;
        
        public DelayedNode() {
            super(Children.LEAF);
        }

        @Override
        public <T extends Cookie> T getCookie(Class<T> type) {
            if (type.isAssignableFrom(OpenCookie.class)) {
                getCookieSet().add(oc);
                return type.cast(oc);
            }
            T ret = super.getCookie(type);
            return ret;
        }
    }
}
