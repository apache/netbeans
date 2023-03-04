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

package org.netbeans.modules.refactoring.java.callhierarchy;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Pokorsky
 */
final class CallNode extends AbstractNode {
    private String htmlDisplayName;
    
    private CallNode() {
        super(Children.LEAF);
    }

    private CallNode(Children children, CallDescriptor desc) {
        super(children, Lookups.singleton(desc));
        setDisplayName(desc.getDisplayName());
    }
    
    void setHtmlDisplayName(String s) {
        this.htmlDisplayName = s;
    }

    public static CallNode createDefault() {
        CallNode node = new CallNode();
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Default.displayName"));
        return node;
    }
    
    public static CallNode createPleaseWait() {
        CallNode node = new CallNode();
        node.setIconBaseWithExtension("org/netbeans/modules/java/navigation/resources/wait.gif"); // NOI18N
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.PleaseWait.displayName"));
        return node;
    }
    
    private static CallNode createCanceled() {
        CallNode node = new CallNode();
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Canceled.displayName"));
        return node;
    }

    private static CallNode createIncomplete() {
        CallNode node = new CallNode();
        node.setHtmlDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Incomplete.htmlDisplayName"));
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Incomplete.displayName"));
        node.setIconBaseWithExtension("org/netbeans/modules/java/navigation/resources/wait.gif"); // NOI18N
        return node;
    }

    private static CallNode createBroken() {
        CallNode node = new CallNode();
        node.setDisplayName(NbBundle.getMessage(CallNode.class, "CallNode.Broken.displayName"));

        return node;
    }
    
    public static CallNode createCall(CallDescriptor desc) {
        CallNode node = new CallNode(desc.isLeaf() ? Children.LEAF : new CallChildren(), desc);
        return node;
    }
    
    public static Node createRoot(CallHierarchyModel model) {
        Call root = null;
        if (model != null) {
            root = model.getRoot();
        }
        
        if (root == null) {
            return createDefault();
        }
        if (root.isBroken()) {
            return createBroken();
        } else if (root.isCanceled()) {
            return createCanceled();
        } else if (root.isIncomplete()) {
            return createPleaseWait();
        }
        return createCall(root);
    }

    @Override
    public Image getIcon(int type) {
        CallDescriptor desc = getLookup().lookup(CallDescriptor.class);
        Icon icon = desc != null ? desc.getIcon() : null;
        return icon != null ? ImageUtilities.icon2Image(icon) : super.getIcon(type);
    }

    @Override
    public Image getOpenedIcon(int type) {
        return getIcon(type);
    }

    @Override
    public String getHtmlDisplayName() {
        if (htmlDisplayName != null) {
            return htmlDisplayName;
        }
        CallDescriptor desc = getLookup().lookup(CallDescriptor.class);
        String htmlDisplayName = desc != null ? desc.getHtmlDisplayName() : null;
        return htmlDisplayName != null ? htmlDisplayName : super.getHtmlDisplayName();
    }

    @Override
    public Action[] getActions(boolean context) {
        CallDescriptor desc = getLookup().lookup(CallDescriptor.class);
        if (desc != null) {
            if (!(desc instanceof Call) || !((Call) desc).getOccurrences().isEmpty()) {
                return new Action[] {new GoToSourceAction(desc)};
            }
        }
        
        return new Action[0];
    }

    @Override
    public Action getPreferredAction() {
        Action[] actions = getActions(true);
        for (Action action : actions) {
            if (action instanceof GoToSourceAction) {
                return action;
            }
        }
        return null;
    }
    
    static final class CallChildren extends Children.Keys<Object> implements Runnable {
        
        private final boolean isOccurrenceView;
        private final AtomicInteger state = new AtomicInteger(0);

        public CallChildren() {
            this(false);
        }

        public CallChildren(boolean isOccurrenceView) {
            this.isOccurrenceView = isOccurrenceView;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            Node parent = this.getNode();
            Call desc = parent != null ? parent.getLookup().lookup(Call.class) : null;
            if (desc == null) {
                return;
            }
            
            if (isOccurrenceView) {
                this.setKeys(desc.getOccurrences());
            } else {
                this.setKeys(new Node[] {CallNode.createPleaseWait()});
                state.set(0);
                desc.getModel().computeCalls(desc, this);
            }
            
        }

        @Override
        protected Node[] createNodes(Object key) {
            Node node = null;
//            System.out.println("... create nodes: " + key);
            if (key instanceof Node) {
                node = (Node) key;
            } else if (key instanceof CallDescriptor) {
                node = CallNode.createCall((CallDescriptor) key);
            } else {
                // XXX log unknown key
                return null;
            }
            return new Node[] {node};
        }

        @Override
        public void run() {
            if (state.incrementAndGet() == 1) {
                Children.MUTEX.writeAccess(this);
                return;
            }
            
            // runs under Children.MUTEX
            Node parent = this.getNode();
            Call desc = parent.getLookup().lookup(Call.class);
            List<? extends Object> keys;
            if (desc == null) {
                keys = Collections.emptyList();
            } else {
                keys = desc.getReferences();
                ArrayList<Object> temp = null;
                
                if (!isOccurrenceView && (desc.isCanceled() || desc.isBroken())) {
                    // speculatively allocate +1 for potential 'incomplete'
                    temp = new ArrayList<Object>(keys.size() + 2);
                    temp.addAll(keys);
                    temp.add(desc.isBroken() ? CallNode.createBroken() : CallNode.createCanceled());
                    keys = temp;
                }
                if (desc.isIncomplete()) {
                    if (temp == null) {
                        temp = new ArrayList<Object>(keys.size() + 1);
                        temp.add(CallNode.createIncomplete());
                        temp.addAll(keys);
                    } else {
                        temp.add(0, CallNode.createIncomplete());
                    }
                    keys = temp;
                }
            }
            this.setKeys(keys);
        }
        
    }
    
    private static final class GoToSourceAction extends AbstractAction {
        
        private CallDescriptor desc;

        public GoToSourceAction(CallDescriptor desc) {
            super(NbBundle.getMessage(GoToSourceAction.class, "GoToSourceAction.displayName"));
            this.desc = desc;
        }
        
        @Override
        public void actionPerformed(ActionEvent e) {
            desc.open();
        }
        
    }
    
}
