/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.weblogic9.ui.nodes;

import java.util.List;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.ResourceNode.ResourceNodeType;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.RefreshModulesCookie;
import org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions.UnregisterCookie;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class JdbcChildrenFactory extends ChildFactory<ResourceNode> implements RefreshModulesCookie {

    enum JdbcNodeTypes {
        RESOURCES,
        POOL;
    }

    private final JdbcNodeTypes type;

    private final Retriever retriever;

    private final UnregisterFactory unregisterFactory;

    private final Lookup lookup;

    public JdbcChildrenFactory(JdbcNodeTypes type, Retriever retriever,
            UnregisterFactory unregisterFactory, Lookup lookup) {
        this.type = type;
        this.retriever = retriever;
        this.unregisterFactory = unregisterFactory;
        this.lookup = lookup;
    }

    @Override
    public final void refresh() {
        retriever.clean();
        refresh(false);
    }

    @Override
    protected boolean createKeys(List<ResourceNode> children) {
        retriever.waitForCompletion();

        List<JDBCDataBean> jdbcDataBeans = retriever.get();
        if (jdbcDataBeans != null) {
            if (type == JdbcNodeTypes.POOL) {
                for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                    String name = jdbcDataBean.getName();
                    if (jdbcDataBean.isApplication()) {
                        name = jdbcDataBean.getDeploymentName();
                    }
                    children.add(new ResourceNode(Children.LEAF,
                            ResourceNodeType.JDBC,jdbcDataBean.getName(),
                            unregisterFactory != null ?
                                unregisterFactory.createUnregisterForPool(name, this, lookup) : null));
                }
            } else if (type == JdbcNodeTypes.RESOURCES) {
                for (JDBCDataBean jdbcDataBean : jdbcDataBeans) {
                    String[] jndiNames = jdbcDataBean.getJndiNames();
                    boolean isApplication = jdbcDataBean.isApplication();
                    for (String name : jndiNames) {
                        // no "unregister" action if jdbc data source is deployed application
                        ResourceNode node = new ResourceNode(Children.LEAF,
                                ResourceNodeType.JDBC, name, isApplication ? null :
                                unregisterFactory != null ?
                                    unregisterFactory.createUnregisterForResource(name, this, lookup) : null);
                        children.add(node);
                    }
                }
            }
            return true;
        }
        retriever.retrieve();
        return false;
    }

    @Override
    protected Node createNodeForKey(ResourceNode key) {
        return key;
    }

    public static interface Retriever {

        List<JDBCDataBean> get();

        void retrieve();

        void clean();

        void waitForCompletion();

    }

    public static interface UnregisterFactory {

        UnregisterCookie createUnregisterForPool(
                String name, RefreshModulesCookie refresh, Lookup lookup);

        UnregisterCookie createUnregisterForResource(
                String name, RefreshModulesCookie refresh, Lookup lookup);

    }

    public static final class JDBCDataBean {

        private final String name;

        private final String jndiNames[];

        private final String deploymentName;

        public JDBCDataBean(String poolName, String[] jndiNames ){
            this(poolName, jndiNames, null);
        }

        public JDBCDataBean( String poolName , String[] jndiNames, String deploymentName){
            name = poolName;
            this.jndiNames = jndiNames.clone();
            this.deploymentName = deploymentName;
        }

        String getName(){
            return name;
        }

        String[] getJndiNames(){
            return jndiNames;
        }

        boolean isApplication(){
            return deploymentName != null;
        }

        String getDeploymentName(){
            return deploymentName;
        }
    }
}
