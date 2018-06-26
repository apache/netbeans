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

package org.netbeans.modules.glassfish.common.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.glassfish.common.CommonServerSupport;
import org.netbeans.modules.glassfish.common.ui.*;
import org.netbeans.modules.glassfish.spi.Decorator;
import org.netbeans.modules.glassfish.spi.GlassfishModule;
import org.netbeans.modules.glassfish.spi.ResourceDecorator;
import org.netbeans.modules.glassfish.spi.ResourceDesc;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


/**
 *
 * @author Peter Williams
 */
public class Hk2ResourcesChildren extends Children.Keys<Object> implements Refreshable {

    private Lookup lookup;
    private String type;
    private final static Node WAIT_NODE = Hk2ItemNode.createWaitNode();

    Hk2ResourcesChildren(Lookup lookup, String type) {
        this.lookup = lookup;
        this.type = type;
    }

    @Override
    public void updateKeys() {
        List<AbstractNode> keys = Collections.synchronizedList(new ArrayList<AbstractNode>());
        String[] childTypes = NodeTypes.getChildTypes(type);
        if ((childTypes != null)) {
            if (childTypes.length > 1) {
                for (int i = 0; i < childTypes.length; i++) {
                    String childtype = childTypes[i];
                    Class customizer = getCustomizer(childtype);
                    keys.add(new Hk2ItemNode(lookup,
                            new Hk2Resources(lookup, childtype, customizer),
                            NbBundle.getMessage(Hk2ResourceContainers.class, "LBL_" + childtype), //TODO
                            Hk2ItemNode.REFRESHABLE_FOLDER));
                }
            } else {
                String childtype = childTypes[0];
                CommonServerSupport commonSupport = lookup.lookup(
                        CommonServerSupport.class);
                if (commonSupport != null) {
                    try {
                        Decorator decorator = DecoratorManager.findDecorator(childtype, null, true);
                        List<ResourceDesc> reslourcesList
                                = ResourceDesc.getResources(commonSupport.getInstance(), childtype);
                        for (ResourceDesc resource : reslourcesList) {
                            keys.add(new Hk2ResourceNode(lookup, resource, (ResourceDecorator) decorator, getCustomizer(childtype)));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }
        setKeys(keys);

    }

    @Override
    protected void addNotify() {
        updateKeys();
    }

    @Override
    protected void removeNotify() {
        setKeys((Set<? extends Object>) java.util.Collections.EMPTY_SET);
    }

    @Override
    protected org.openide.nodes.Node[] createNodes(Object key) {
        if (key instanceof Hk2ItemNode){
            return new Node [] { (Hk2ItemNode) key };
        }

        if (key instanceof String && key.equals(WAIT_NODE)){
            return new Node [] { WAIT_NODE };
        }

        return null;
    }

    class Hk2Resources extends Children.Keys<Object> implements Refreshable {

        private Lookup lookup;
        private String type;
        private Class customizer;
        private final Node WAIT_NODE = Hk2ItemNode.createWaitNode();

        Hk2Resources(Lookup lookup, String type, Class customizer) {
            this.lookup = lookup;
            this.type = type;
            this.customizer = customizer;
        }

        @Override
        public void updateKeys() {
        RequestProcessor t = new RequestProcessor("resource-child-updater");
        t.post(new Runnable() {

                List<AbstractNode> keys = Collections.synchronizedList(new ArrayList<AbstractNode>());

                @Override
                public void run() {
                    CommonServerSupport commonSupport = lookup.lookup(
                            CommonServerSupport.class);
                    if (commonSupport != null) {
                        try {
                            Decorator decorator = DecoratorManager.findDecorator(type, null,true);
                            if (decorator == null) {
                                if (type.equals(GlassfishModule.JDBC_RESOURCE)) {
                                    decorator = Hk2ItemNode.JDBC_MANAGED_DATASOURCES;
                                } else if (type.equals(GlassfishModule.JDBC_CONNECTION_POOL)) {
                                    decorator = Hk2ItemNode.CONNECTION_POOLS;
                                }
                            }
                            if (decorator != null) {
                                List<ResourceDesc> reslourcesList
                                        = ResourceDesc.getResources(commonSupport.getInstance(), type);
                                for (ResourceDesc resource : reslourcesList) {
                                    keys.add(new Hk2ResourceNode(lookup, resource, (ResourceDecorator) decorator, customizer));
                                }
                            }
                        } catch (Exception ex) {
                            Logger.getLogger("glassfish").log(Level.INFO, ex.getLocalizedMessage(), ex);
                        }

                        setKeys(keys);
                    }
                }
            }, 0);
        }

        @Override
        protected void addNotify() {
            updateKeys();
        }

        @Override
        protected void removeNotify() {
            setKeys((Set<? extends Object>) java.util.Collections.EMPTY_SET);
        }

        @Override
        protected org.openide.nodes.Node[] createNodes(Object key) {
            if (key instanceof Hk2ItemNode) {
                return new Node[]{(Hk2ItemNode) key};
            }

            if (key instanceof String && key.equals(WAIT_NODE)) {
                return new Node[]{WAIT_NODE};
            }

            return null;
        }
    }

    private Class getCustomizer(String type) {
        Class customizer = null;
        if (type.equals(GlassfishModule.JDBC_CONNECTION_POOL)) {
            customizer = ConnectionPoolCustomizer.class;
        } else if (type.equals(GlassfishModule.JDBC_RESOURCE)) {
            customizer = JdbcResourceCustomizer.class;
        } else if (type.equals(GlassfishModule.CONN_RESOURCE)) {
            customizer = ConnectorCustomizer.class;
        } else if (type.equals(GlassfishModule.CONN_CONNECTION_POOL)) {
            customizer = ConnectorConnectionPoolCustomizer.class;
        } else if (type.equals(GlassfishModule.ADMINOBJECT_RESOURCE)) {
            customizer = AdminObjectCustomizer.class;
        } else if (type.equals(GlassfishModule.JAVAMAIL_RESOURCE)) {
            customizer = JavaMailCustomizer.class;
        }
        return customizer;
    }
}
