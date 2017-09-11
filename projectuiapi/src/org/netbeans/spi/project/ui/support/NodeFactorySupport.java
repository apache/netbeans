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

package org.netbeans.spi.project.ui.support;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;

/**
 * Support class for creating Project node's children nodes from NodeFactory instances
 * in layers.
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.18
 */
public class NodeFactorySupport {

    private static RequestProcessor RP = new RequestProcessor(NodeFactorySupport.class.getName());
    
    private NodeFactorySupport() {
    }
    
    /**
     * Creates children list that works on top of {@link NodeFactory} instances
     * in layers.
     * @param project the project which is being displayed
     * @param folderPath the path in the System Filesystem that is used as root for subnode composition.
     *        The content of the folder is assumed to be {@link org.netbeans.spi.project.ui.support.NodeFactory} instances
     * @return a new children list
     */
    public static Children createCompositeChildren(Project project, String folderPath) {
        return new DelegateChildren(project, folderPath);
    }

    /**
     * Utility method for creating a non variable NodeList instance.
     * @param nodes a fixed set of nodes to display
     * @return a constant node list
     */
    public static NodeList<?> fixedNodeList(Node... nodes) {
        return new FixedNodeList(nodes);
    }
    
    private static class FixedNodeList implements NodeList<Node> {
        
        private List<Node> nodes;
        
        FixedNodeList(Node... nds) {
            nodes = Arrays.asList(nds);
        }
        public List<Node> keys() {
            return nodes;
        }
        
        public void addChangeListener(ChangeListener l) { }
        
        public void removeChangeListener(ChangeListener l) { }
        
        public void addNotify() {
        }
        
        public void removeNotify() {
        }

        public Node node(Node key) {
            return key;
        }
    }

    static Node createWaitNode() {
        AbstractNode n = new AbstractNode(Children.LEAF);
        n.setIconBaseWithExtension("org/openide/nodes/wait.gif"); //NOI18N
        n.setDisplayName(NbBundle.getMessage(ChildFactory.class, "LBL_WAIT")); //NOI18N
        return n;
    }

    private static NodeListKeyWrapper LOADING_KEY = new NodeListKeyWrapper(null, null);

    static class DelegateChildren extends Children.Keys<NodeListKeyWrapper> implements LookupListener, ChangeListener {


        private String folderPath;
        private Project project;
        private List<NodeList<?>> nodeLists = new ArrayList<NodeList<?>>();
        private List<NodeFactory> factories = new ArrayList<NodeFactory>();
        private Lookup.Result<NodeFactory> result;
        private final HashMap<NodeList<?>, List<NodeListKeyWrapper>> keys;
        private RequestProcessor.Task task;
        
        public DelegateChildren(Project proj, String path) {
            folderPath = path;
            project = proj;
            keys = new HashMap<NodeList<?>, List<NodeListKeyWrapper>>();
        }
        
        // protected for tests..
        protected Lookup createLookup() {
            return Lookups.forPath(folderPath);
        }
        
       protected Node[] createNodes(NodeListKeyWrapper key) {
           // XXX cleaner to use Children.create w/ asynch ChildFactory; getNodes(true) then works for free
           if (key == LOADING_KEY) {
               return new Node[] { createWaitNode() };
           }
           @SuppressWarnings("unchecked") // needs to handle NodeList's of different types
           Node nd = key.nodeList.node(key.object);
           if (nd != null) {
               return new Node[] { nd };
           }
           return new Node[0];
        }
       
       private Collection<NodeListKeyWrapper> createKeys() {
           Collection<NodeListKeyWrapper> col = new ArrayList<NodeListKeyWrapper>();
           assert !Thread.holdsLock(keys);
           synchronized (this) {
               for (NodeList lst : nodeLists) {
                   List<NodeListKeyWrapper> x;
                   synchronized (keys) {
                        x = keys.get(lst);
                   }
                   if (x != null) {
                       col.addAll(x);
                   }
               }
           }
           return col;
       }
      
        protected @Override void addNotify() {
            super.addNotify();
            setKeys(Collections.singleton(LOADING_KEY));
            task = RP.post(new Runnable() {
                public void run() {
                    synchronized (DelegateChildren.this) {
                        result = createLookup().lookupResult(NodeFactory.class);
                        for (NodeFactory factory : result.allInstances()) {
                            NodeList<?> lst = factory.createNodes(project);
                            assert lst != null : "Factory " + factory.getClass() + " has broken the NodeFactory contract."; //NOI18N
                            lst.addNotify();
                            List objects = lst.keys();
                            synchronized (keys) {
                                nodeLists.add(lst);
                                addKeys(lst, objects);
                            }
                            lst.addChangeListener(DelegateChildren.this);
                            factories.add(factory);
                        }
                        result.addLookupListener(DelegateChildren.this);
                    }
                    setKeys(createKeys());
                    task = null;
                }
            });
        }

        public @Override Node[] getNodes(boolean optimalResult) {
            Node[] ns = super.getNodes(optimalResult);
            RequestProcessor.Task _task = task;
            if (optimalResult && _task != null) {
                _task.waitFinished();
                ns = super.getNodes(optimalResult);
            }
            return ns;
        }

        public @Override int getNodesCount(boolean optimalResult) {
            int cnt = super.getNodesCount(optimalResult);
            RequestProcessor.Task _task = task;
            if (optimalResult && _task != null) {
                _task.waitFinished();
                cnt = super.getNodesCount(optimalResult);
            }
            return cnt;
        }

        public @Override Node findChild(String name) {
            if (name != null) {
                getNodes(true);
            }
            return super.findChild(name);
        }
        
        protected @Override void removeNotify() {
            super.removeNotify();
            setKeys(Collections.<NodeListKeyWrapper>emptySet());
            synchronized (this) {
                for (NodeList elem : nodeLists) {
                    elem.removeChangeListener(this);
                    elem.removeNotify();
                }
                synchronized (keys) {
                    keys.clear();
                    nodeLists.clear();
                }
                factories.clear();
                if (result != null) {
                    result.removeLookupListener(this);
                    result = null;
                }
            }
        }
        
        public void stateChanged(final ChangeEvent e) {
            final Runnable action = new Runnable() {
                public void run() {
                    NodeList list = (NodeList) e.getSource();
                    List objects = list.keys();
                    synchronized (keys) {
                        removeKeys(list);
                        addKeys(list, objects);
                    }
                    final Collection<NodeListKeyWrapper> ks = createKeys();
                    NodeFactorySupport.createWaitNode();
                    EventQueue.invokeLater(new RunnableImpl(DelegateChildren.this, ks));
                }
            };
            if (ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess()) {
                //having lock (non blocking)
                action.run();
            }
            else {
                //may block for > 10s when waiting on project save
                RP.post(action);
            }
        }
        
        //to be called under lock.
        private void addKeys(NodeList list, List objects) {
            assert Thread.holdsLock(keys);
            List<NodeListKeyWrapper> wrps = new ArrayList<NodeListKeyWrapper>();
            for (Object key : objects) {
                wrps.add(new NodeListKeyWrapper(key, list));
            }
            keys.put(list, wrps);
            
        }
        
        //to be called under lock.
        private void removeKeys(NodeList list) {
            assert Thread.holdsLock(keys);
            keys.remove(list);
        }


        public void resultChanged(LookupEvent ev) {
            int index = 0;
            synchronized (this) {
                Lookup.Result<?> res = (Lookup.Result<?>) ev.getSource();
                for (Object _factory : res.allInstances()) {
                    NodeFactory factory = (NodeFactory) _factory;
                    if (!factories.contains(factory)) {
                        factories.add(index, factory);
                        NodeList<?> lst = factory.createNodes(project);
                        assert lst != null;
                        List objects = lst.keys();
                        synchronized (keys) {
                            nodeLists.add(index, lst);
                            addKeys(lst, objects);
                        }
                        lst.addNotify();
                        lst.addChangeListener(this);
                    } else {
                        while (!factory.equals(factories.get(index))) {
                            factories.remove(index);
                            synchronized (keys) {
                                NodeList<?> lst = nodeLists.remove(index);
                                removeKeys(lst);
                                lst.removeNotify();
                                lst.removeChangeListener(this);                            
                            }
                        }
                    }
                    index++;
                }
            }
            //#115128 prevent deadlock in Children mutex
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    setKeys(createKeys());
                }
            });
        }

        private static class RunnableImpl implements Runnable {
            private Collection<NodeListKeyWrapper> ks;
            private DelegateChildren ch;


            public RunnableImpl(DelegateChildren aThis, Collection<NodeListKeyWrapper> ks) {
                this.ks = ks;
                this.ch = aThis;
            }

            public void run() {
                ch.setKeys(ks);
                ch = null;
                ks = null;
            }
        }
    }
    
    /**
     * this class makes sure the bond between the NodeList and individial
     * items is not lost, prevents duplicates about different NodeLists
     * while allowing for fine-grained updating of nodes on stateChange()
     * 
     */ 
    private static class NodeListKeyWrapper  {
        NodeList nodeList;
        Object object;

        NodeListKeyWrapper(Object obj, NodeList list) {
            nodeList = list;
            object = obj;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final NodeListKeyWrapper other = (NodeListKeyWrapper) obj;
            if (this.nodeList != other.nodeList && (this.nodeList == null || !this.nodeList.equals(other.nodeList))) {
                return false;
            }
            if (this.object != other.object && (this.object == null || !this.object.equals(other.object))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 67 * hash + (this.nodeList != null ? this.nodeList.hashCode() : 0);
            hash = 67 * hash + (this.object != null ? this.object.hashCode() : 0);
            return hash;
        }
        
        }

    }
