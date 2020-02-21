/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.debugger.common2.ui.processlist;


import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessInfo;
import org.netbeans.modules.cnd.debugger.common2.debugger.processlist.api.ProcessList;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;

/**
 *
 */
public final class ProcessesRootNode extends AbstractNode {
    private final boolean treeMode;
    private final LoadingChildrenImpl loadingChildren;
    private final Refreshable refreshableChildren;

    public ProcessesRootNode(boolean treeMode, ProcessPanelCustomizer customizer, ProcessFilter filter) {
        super(treeMode ? new TreeModeChildren(customizer, filter) : new PlainModeChildren(customizer, filter));
        this.treeMode = treeMode;
        this.refreshableChildren = (Refreshable)getChildren();
        this.loadingChildren = new LoadingChildrenImpl();
    }

    public void refresh(ProcessList plist) {
        assert SwingUtilities.isEventDispatchThread();

        Children children = getChildren();
        if (children instanceof Refreshable) {
            ((Refreshable) children).refreshChildren(plist);
        }
        else  if (children instanceof LoadingChildren) {
            //get children
            //setChildren((Children)refreshableChildren);
            refreshableChildren.refreshChildren(plist);

            setChildren((Children)refreshableChildren);
        }
    }

    Node getNode(Integer pid) {
        assert SwingUtilities.isEventDispatchThread();

        Children children = getChildren();

        if (children instanceof NodesCache) {
            return ((NodesCache) children).getNode(pid);
        }

        return null;
    }

    void setLoading() {
        setChildren(loadingChildren);
    }

    private interface LoadingChildren {

    }

    private class LoadingChildrenImpl extends Children.Array implements LoadingChildren {

        public LoadingChildrenImpl() {
            super();
            add(new Node[]{new LoadingNode()});
        }

    }

    interface Refreshable {

        void refreshChildren(ProcessList processList);
    }

    interface NodesCache {

        Node getNode(Integer pid);
    }

    private static class TreeModeChildren extends Children.Keys<ProcessInfo> implements Refreshable, NodesCache {

        private ProcessPanelCustomizer customizer = null;
        private ProcessList currentData = null;
        private final HashMap<Integer, Node> nodesCache;
        private final ProcessFilter filter;

        TreeModeChildren(ProcessPanelCustomizer customizer, ProcessFilter filter) {
            this.nodesCache = new HashMap<Integer, Node>();
            this.filter = filter;
            this.customizer = customizer;
        }

        @Override
        protected Node[] createNodes(final ProcessInfo info) {
            if (info == null || currentData == null) {
                return new Node[0];
            }

            Node n;

            if (info.getPID() == -1) {
                n = new AbstractNode(LEAF) {

                    @Override
                    public String getDisplayName() {
                        return loc("WaitNode.displayName"); // NOI18N
                    }
                };
            } else {
                final Collection<Integer> cpids = currentData.getPIDs(info.getPID());

                Children children = cpids.isEmpty() ? Children.LEAF
                        : Children.create(new ChildFactory<ProcessInfo>() {

                    @Override
                    protected boolean createKeys(List<ProcessInfo> toPopulate) {
                        for (Integer cpid : cpids) {
                            toPopulate.add(currentData.getInfo(cpid));
                        }
                        return true;
                    }

                    @Override
                    protected Node[] createNodesForKey(ProcessInfo key) {
                        return createNodes(key);
                    }
                }, false);

                n = new ProcessNode(info, children,
                        //ProcessActionsSupport.getDefault(), 
                        customizer, filter);
            }

            nodesCache.put(info.getPID(), n);
            return new Node[]{n};
        }

        @Override
        public void refreshChildren(final ProcessList data) {
            //in case Show User Process Only, need to get 2 lists:
            //one with the pid numbers with user processes.
            //another one with the fullList
            currentData = data;

            if (data == null) {
               // setKeys(Arrays.asList(new ProcessInfo(null, -1, -1, null, null)));
                setKeys(Collections.EMPTY_LIST);
                return;
            }

            TreeSet<ProcessInfo> result = new TreeSet<ProcessInfo>();

            Integer apid, appid, prevPID;
            for (Integer pid : data.getPIDs(filter.get())) {
                apid = pid;
                prevPID = pid;
                ProcessInfo info;
                while (true) {                    
                    info = data.getInfo(apid);
                    if (info == null) {
                        break;
                    }
                    appid = info.getPPID();
                    if (appid <= 1) {
                        break;
                    }       
                    prevPID = apid;
                    apid = appid;
                }
                info = data.getInfo(apid);
                if (apid >= 4 && info != null) {
                    result.add(info);
                } else {
                    info = data.getInfo(prevPID);
                    if (info != null) {
                        result.add(info);
                    }
                }
                
            }

            setKeys(result);
        }

        @Override
        public Node getNode(Integer pid) {
            return nodesCache.get(pid);
        }
    }

    private static class PlainModeChildren extends Children.Keys<ProcessInfo> implements Refreshable, NodesCache {

        private ProcessPanelCustomizer customizer = null;
        private final HashMap<Integer, Node> nodesCache;
        private final ProcessFilter filter;

        PlainModeChildren(ProcessPanelCustomizer customizer, ProcessFilter filter) {
            this.nodesCache = new HashMap<Integer, Node>();
            this.customizer = customizer;
            this.filter = filter;
        }

        @Override
        protected Node[] createNodes(ProcessInfo info) {
            if (info == null) {
                return new Node[0];
            }

            Node n;

            if (info.getPID() == -1) {
                n = new AbstractNode(LEAF) {

                    @Override
                    public String getDisplayName() {
                        return loc("WaitNode.displayName"); // NOI18N
                    }
                };
            } else {
                n = new ProcessNode(info, LEAF, 
                       // ProcessActionsSupport.getDefault(),
                        customizer, filter);
            }

            nodesCache.put(info.getPID(), n);
            return new Node[]{n};
        }

        @Override
        public synchronized void refreshChildren(final ProcessList data) {
            if (data == null) {
                //setKeys(Arrays.asList(new ProcessInfo(null, -1, -1, null, null)));
                setKeys(Collections.EMPTY_LIST);
                return;
            }

          
            Collection<Integer> pids = data.getPIDs(filter.get());

            List<ProcessInfo> notSystemProcesses = new LinkedList<ProcessInfo>();

            for (Integer pid : pids) {
                if (pid > 5) {
                    notSystemProcesses.add(data.getInfo(pid));
                }
            }

            setKeys(notSystemProcesses);
        }

        @Override
        public Node getNode(Integer pid) {
            return nodesCache.get(pid);
        }
    }

    private static String loc(String key, String... params) {
        return NbBundle.getMessage(ProcessesRootNode.class, key, params);
    }
}
