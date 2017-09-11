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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import junit.framework.Assert;
import org.openide.nodes.Node;
import org.openide.util.ContextGlobalProvider;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.test.MockLookup;

/** Utilities for actions tests.
 * @author Jesse Glick
 */
public class NodeActionsInfraHid {
    
    private static Node[] currentNodes = null;
    private static final NodeLookup nodeLookup = new NodeLookup();
    private static Lookup.Result<Node> nodeResult;
    
    public static void install() {
        MockLookup.setInstances(new ContextGlobalProvider() {
            public Lookup createGlobalContext() {
                return nodeLookup;
            }
        });
        nodeResult = Utilities.actionsGlobalContext().lookupResult(Node.class);
        Assert.assertEquals(Collections.emptySet(), new HashSet<Node>(nodeResult.allInstances()));
    }

    public static void setCurrentNodes(Node[] nue) {
        currentNodes = nue;
        nodeLookup.refresh();
        Assert.assertEquals(nue != null ? new HashSet(Arrays.asList(nue)) : Collections.EMPTY_SET,
                new HashSet(nodeResult.allInstances()));
    }

    private static final class NodeLookup extends AbstractLookup implements InstanceContent.Convertor {
        private final InstanceContent content;
        public NodeLookup() {
            this(new InstanceContent());
        }
        private NodeLookup(InstanceContent content) {
            super(content);
            this.content = content;
            refresh();
        }
        public void refresh() {
            //System.err.println("NL.refresh; currentNodes = " + currentNodes);
            if (currentNodes != null) {
                content.set(Arrays.asList(currentNodes), null);
            } else {
                content.set(Collections.singleton(new Object()), this);
            }
        }
        public Object convert(Object obj) {
            return null;
        }
        public Class type(Object obj) {
            return Node.class;
        }
        public String id(Object obj) {
            return "none"; // magic, see NodeAction.NodesL.resultChanged
        }
        public String displayName(Object obj) {
            return null;
        }
    }

    /*
    private static final class NodeLookup extends ProxyLookup implements InstanceContent.Convertor {
        public NodeLookup() {
            refresh();
        }
        public void refresh() {
            //System.err.println("NL.refresh; currentNodes = " + currentNodes);
            setLookups(new Lookup[] {
                currentNodes != null ?
                    Lookups.fixed(currentNodes) :
                    Lookups.fixed(new Object[] {null}, this),
            });
        }
        public Object convert(Object obj) {
            return null;
        }
        public Class type(Object obj) {
            return Object.class;
        }
        public String id(Object obj) {
            return "none";
        }
        public String displayName(Object obj) {
            return null;
        }
    }
     */

    /** Prop listener that will tell you if it gets a change.
     */
    public static final class WaitPCL implements PropertyChangeListener {
        /** whether a change has been received, and if so count */
        public int gotit = 0;
        /** optional property name to filter by (if null, accept any) */
        private final String prop;
        public WaitPCL(String p) {
            prop = p;
        }
        public synchronized void propertyChange(PropertyChangeEvent evt) {
            if (prop == null || prop.equals(evt.getPropertyName())) {
                gotit++;
                notifyAll();
            }
        }
        public boolean changed() {
            return changed(1500);
        }
        public synchronized boolean changed(int timeout) {
            if (gotit > 0) {
                return true;
            }
            try {
                wait(timeout);
            } catch (InterruptedException ie) {
                ie.printStackTrace();
            }
            return gotit > 0;
        }
    }
    
}
