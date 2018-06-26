/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2013 Oracle and/or its affiliates. All rights reserved.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.glassfish.common.GlassfishInstance;
import org.netbeans.modules.glassfish.spi.GlassfishModule.ServerState;
import org.netbeans.modules.glassfish.spi.PluggableNodeProvider;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


/**
 * 
 * @author Peter Williams
 */
public class Hk2InstanceChildren extends Children.Keys<Node> implements Refreshable, ChangeListener {
    
    private GlassfishInstance serverInstance;
    
    @SuppressWarnings("LeakingThisInConstructor")
    Hk2InstanceChildren(GlassfishInstance instance) {
        serverInstance = instance;
        serverInstance.getCommonSupport().addChangeListener(
                WeakListeners.change(this, serverInstance));
    }

    @Override
    public void updateKeys(){
        List<Node> keys = new LinkedList<Node>();
        serverInstance.getCommonSupport().refresh();
        if(serverInstance.getServerState() == ServerState.RUNNING) {
            keys.add(new Hk2ItemNode(serverInstance.getLookup(), 
                    new Hk2ApplicationsChildren(serverInstance.getLookup()),
                    NbBundle.getMessage(Hk2InstanceNode.class, "LBL_Apps"),
                    Hk2ItemNode.J2EE_APPLICATION_FOLDER));
            keys.add(new Hk2ItemNode(serverInstance.getLookup(), 
                    new Hk2ResourceContainers(serverInstance.getLookup()),
                    NbBundle.getMessage(Hk2InstanceNode.class, "LBL_Resources"),
                    Hk2ItemNode.RESOURCES_FOLDER));
            String iid = serverInstance.getDeployerUri();
            if (null != iid && iid.contains("gfv3ee6wc")) {
                keys.add(new Hk2ItemNode(serverInstance.getLookup(),
                        new Hk2WSChildren(serverInstance.getLookup()),
                        NbBundle.getMessage(Hk2InstanceNode.class, "LBL_WS"),
                        Hk2ItemNode.WS_FOLDER));
            }
            List<Node> pluggableNodes = getExtensionNodes();
            for (Iterator itr = pluggableNodes.iterator(); itr.hasNext();) {
                keys.add((Node)itr.next());
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
        Collection<Node> noKeys = java.util.Collections.emptySet();
        setKeys(noKeys);
    }
    
    @Override
    protected org.openide.nodes.Node[] createNodes(Node key) {
        return new Node [] { key };
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                updateKeys();
            }
        });
    }

    List<Node> getExtensionNodes() {
       List<Node> nodesList = new ArrayList<Node>();
        for (PluggableNodeProvider nep
                : Lookup.getDefault().lookupAll(PluggableNodeProvider.class)) {
            if (nep != null) {
                try {
                    Node node = nep.getPluggableNode(
                            serverInstance.getProperties());
                    if (node != null) {
                        nodesList.add(node);
                    }
                } catch (Exception ex) {
                    Logger.getLogger("glassfish-common").log(Level.SEVERE,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()));
                    Logger.getLogger("glassfish-common").log(Level.FINER,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()), ex);
                } catch (AssertionError ae) {
                    Logger.getLogger("glassfish-common").log(Level.SEVERE,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()+".")); // NOI18N
                    Logger.getLogger("glassfish-common").log(Level.FINER,
                            NbBundle.getMessage(Hk2InstanceChildren.class,
                            "WARN_BOGUS_GET_EXTENSION_NODE_IMPL", // NOI18N
                            nep.getClass().getName()), ae);
                }
             }
        }
       return nodesList;
   }
}
