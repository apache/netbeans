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
package org.netbeans.modules.web.javascript.debugger.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMBreakpoint;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode;
import org.netbeans.modules.web.javascript.debugger.breakpoints.DOMNode.NodeId;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.NodeAction;

/**
 * An action, that adds a DOM breakpoint.
 * 
 * @author Martin Entlicher
 */
@NbBundle.Messages({
    "AddDOMBreakpoint=Add DOM Breakpoint",
    "CTL_BreakOnSubtreeModif=Break on Subtree Modifications",
    "CTL_BreakOnAttributesModif=Break on Attributes Modifications",
    "CTL_BreakOnNodeRemove=Break on Node Removal"
})
@ActionRegistration(displayName="#AddDOMBreakpoint", lazy=false, asynchronous=false)
@ActionID(category="DOM", id="web.javascript.debugger.actions.AddDOMBreakpointAction")
@ActionReference(path="Navigation/DOM/Actions", position=200, separatorBefore=100)
public class AddDOMBreakpointAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
    }

    @Override
    public JMenuItem getPopupPresenter() {
        //Node[] nodes = getActivatedNodes();  // Contains old nodes when changing focus by the right-click.
        Node[] nodes = Utilities.actionsGlobalContext().lookupAll(Node.class).toArray(new Node[] {});
        return new PopupPresenter(nodes, enable(nodes));
    }
    
    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        for (Node n : activatedNodes) {
            org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode =
                    n.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
            if (domNode == null) {
                FileObject fo = n.getLookup().lookup(FileObject.class);
                if (fo == null) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String getName() {
        return Bundle.AddDOMBreakpoint();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("debug.javascript.addDOMBreakpoint");
    }
    
    private static class PopupPresenter extends JMenuItem implements DynamicMenuContent {
        
        private final JCheckBoxMenuItem[] items;

        private PopupPresenter(Node[] activatedNodes, boolean enabled) {
            items = new JCheckBoxMenuItem[] {
                new JCheckBoxMenuItem(Bundle.CTL_BreakOnSubtreeModif()),
                new JCheckBoxMenuItem(Bundle.CTL_BreakOnAttributesModif()),
                new JCheckBoxMenuItem(Bundle.CTL_BreakOnNodeRemove()),
            };
            if (!enabled) {
                for (JComponent c : items) {
                    c.setEnabled(false);
                }
            } else {
                DOMBreakpoint[] domBreakpoints = findDOMBreakpoints();
                for (int i = 0; i < activatedNodes.length; i++) {
                    Node node = activatedNodes[i];
                    final org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode;
                    domNode = node.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                    DOMBreakpoint db = findBreakpointOn(domNode, node, domBreakpoints);
                    bind(items[0], node, DOMBreakpoint.Type.SUBTREE_MODIFIED, db, domNode);
                    bind(items[1], node, DOMBreakpoint.Type.ATTRIBUTE_MODIFIED, db, domNode);
                    bind(items[2], node, DOMBreakpoint.Type.NODE_REMOVED, db, domNode);
                }
            }
        }

        @Override
        public JComponent[] getMenuPresenters() {
            return items;
        }

        @Override
        public JComponent[] synchMenuPresenters(JComponent[] items) {
            return this.items;
        }
        
    }
    
    private static void bind(final JCheckBoxMenuItem cmi, final Node node,
                             final DOMBreakpoint.Type type, DOMBreakpoint db,
                             final org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode) {
        if (db != null) {
            cmi.setSelected(db.getTypes().contains(type));
        }
        final DOMBreakpoint[] dbPtr = new DOMBreakpoint[] { db };
        cmi.addActionListener(new ActionListener() {
            
            @Override
            public void actionPerformed(ActionEvent e) {
                if (dbPtr[0] == null) {
                    URL url;
                    DOMNode dom;
                    if (domNode != null) {
                        url = DOMNode.findURL(domNode);
                        dom = DOMNode.create(domNode);
                    } else {
                        FileObject fo = node.getLookup().lookup(FileObject.class);
                        if (fo == null) {
                            return;
                        }
                        url = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                        if (url == null) {
                            return ;
                        }
                        dom = DOMNode.create(node);
                    }
                    dbPtr[0] = new DOMBreakpoint(url, dom);
                    DebuggerManager.getDebuggerManager().addBreakpoint(dbPtr[0]);
                }
                if (cmi.isSelected()) {
                    dbPtr[0].addType(type);
                } else {
                    dbPtr[0].removeType(type);
                }
                if (dbPtr[0].getTypes().isEmpty()) {
                    DebuggerManager.getDebuggerManager().removeBreakpoint(dbPtr[0]);
                    dbPtr[0] = null;
                }
            }
        });
    }
    
    private static DOMBreakpoint findBreakpointOn(org.netbeans.modules.web.webkit.debugging.api.dom.Node domNode,
                                                  Node node,
                                                  DOMBreakpoint[] domBreakpoints) {
        if (domNode != null) {
            for (DOMBreakpoint db : domBreakpoints) {
                DOMNode dom = db.getNode();
                if (domNode.equals(dom.getNode())) {
                    return db;
                }
            }
        } else {
            //List<DOMNode.NodeId> path = createNodePath(node);
            DOMNode nodeDom = DOMNode.create(node);
            List<? extends NodeId> nodePath = nodeDom.getPath();
            if (nodePath != null) {
                String name = node.getName();
                for (DOMBreakpoint db : domBreakpoints) {
                    DOMNode dom = db.getNode();
                    List<? extends NodeId> path = dom.getPath();
                    if (path != null && name.equals(dom.getNodeName()) && listEquals(nodePath, path)) {
                        return db;
                    }
                }
            } else {
                String id = nodeDom.getID();
                for (DOMBreakpoint db : domBreakpoints) {
                    DOMNode dom = db.getNode();
                    if (id.equals(dom.getID())) {
                        return db;
                    }
                }
            }
        }
        return null;
    }
    
    private static boolean listEquals(List<?> l1, List<?> l2) {
        int n = l1.size();
        if (l2.size() == n) {
            for (int i = 0; i < n; i++) {
                Object o1 = l1.get(i);
                Object o2 = l2.get(i);
                if (o1 == null) {
                    if (o2 != null) {
                        return false;
                    }
                } else {
                    if (!o1.equals(o2)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
    private static DOMBreakpoint[] findDOMBreakpoints() {
        List<DOMBreakpoint> domBreakpoints = new ArrayList<DOMBreakpoint>();
        Breakpoint[] breakpoints = DebuggerManager.getDebuggerManager().getBreakpoints();
        for (Breakpoint b : breakpoints) {
            if (b instanceof DOMBreakpoint) {
                domBreakpoints.add((DOMBreakpoint) b);
            }
        }
        return domBreakpoints.toArray(new DOMBreakpoint[]{});
    }

}
