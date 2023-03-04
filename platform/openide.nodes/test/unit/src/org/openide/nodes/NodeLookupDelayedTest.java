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
