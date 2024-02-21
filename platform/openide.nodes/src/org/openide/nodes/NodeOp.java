/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JPopupMenu;
import org.netbeans.modules.openide.nodes.NodesRegistrationSupport;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.ProxyLookup;

/** Utility class for operations on nodes.
 * <p>
 * Since version 7.9 this class supports creation of a <em>lazy node</em> -
 * a node that displays basic information
 * about the original node behind it, without touching the original. As soon as
 * the node is expanded, or a pop up menu is invoked on it, it switches
 * to the real delegates and since then everything is being bridged to
 * the real node.
 * This method is especially useful when used from
 * <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">layers</a>.
 * Use following
 * XML snippet to register your node into Services tab:
 * <pre>
 * &lt;folder name="UI"&gt;
 *   &lt;folder name="Runtime"&gt;
 *     &lt;file name="org-yourorg-yourpgk-YourNode.instance"&gt;
 *       &lt;attr name="instanceCreate" methodvalue="org.openide.nodes.NodeOp.factory"/&gt;
 *       &lt;attr name="name" stringvalue="YourNode"/&gt;
 *       &lt;attr name="displayName" bundlevalue="org.yourorg.yourpkg.Bundle#NAME-KEY"/&gt;
 *       &lt;attr name="shortDescription" bundlevalue="org.yourorg.yourpkg.Bundle#SHORT-KEY"/&gt;
 *       &lt;attr name="iconResource" stringvalue="org/yourorg/yourpkg/image.png"/&gt;
 *       &lt;attr name="original" newvalue="org.yourorg.yourpkg.YourNode"/&gt;
 *     &lt;file/&gt;
 *   &lt;folder/&gt;
 * &lt;folder/&gt;
 * </pre>
 *
 * @author Jaroslav Tulach, Petr Hamernik, Dafe Simonek
 */
public final class NodeOp extends Object {
    private static final Logger LOG = Logger.getLogger(NodeOp.class.getName());
    /** default node actions */
    private static SystemAction[] defaultActions;

    private NodeOp() {
    }

    /** Get the default actions for all nodes.
    * @return array of default actions
     * @deprecated Do not use this method. It is useless now.
    */
    @Deprecated
    public static SystemAction[] getDefaultActions() {
        if (defaultActions == null) {
            defaultActions = createFromNames(new String[] {"Tools", "Properties"}); // NOI18N 
        }

        return defaultActions;
    }

    /** @deprecated Useless. */
    @Deprecated
    public static void setDefaultActions(SystemAction[] def) {
        throw new SecurityException();
    }

    /**
     * Computes a common popup menu for the specified nodes.
     * Provides only those actions supplied by all nodes in the list.
     * <p>Component action maps are not taken into consideration.
     * {@link Utilities#actionsToPopup(Action[], Component)} is a better choice
     * if you want to use actions such as "Paste" which look at action maps.
    * @param nodes the nodes
    * @return the menu for all nodes
    */
    public static JPopupMenu findContextMenu(Node[] nodes) {
        Action[] arr = findActions(nodes);

        // prepare lookup representing all the selected nodes
        List<Lookup> allLookups = new ArrayList<Lookup>();

        for (Node n : nodes) {
            allLookups.add(n.getLookup());
        }

        Lookup lookup = new ProxyLookup(allLookups.toArray(new Lookup[0]));

        return Utilities.actionsToPopup(arr, lookup);
    }

    /** Asks the provided nodes for their actions and those that are common,
     * to all of them returns.
     *
     * @param nodes array of nodes to compose actions for
     * @return array of actions for the nodes or empty array if no actions
     *   were found
     * @since 3.29
     */
    public static Action[] findActions(Node[] nodes) {
        Map<Action,Integer> actions = new HashMap<Action,Integer>();

        Action[][] actionsByNode = new Action[nodes.length][];

        // counts the number of occurences for each action
        for (int n = 0; n < nodes.length; n++) {
            actionsByNode[n] = nodes[n].getActions(false);

            if (actionsByNode[n] == null) {
                // XXX is this permitted by the API?!
                // use default actions
                actionsByNode[n] = getDefaultActions();
            }

            // keeps actions handled for this node iteration
            Set<Action> counted = new HashSet<Action>();

            for (Action a : actionsByNode[n]) {
                if (a != null) {
                    // if this action was handled for this node already, skip to next iteration
                    if (counted.contains(a)) {
                        continue;
                    }

                    counted.add(a);

                    Integer cntInt = actions.get(a);
                    actions.put(a, cntInt == null ? 1 : cntInt + 1);
                }
            }
        }

        // take all actions that are nodes.length number times
        if (!actions.isEmpty()) {
            // keeps actions for which was menu item created already
            List<Action> result = new ArrayList<Action>();
            Set<Action> counted = new HashSet<Action>();

            for (Action action : actionsByNode[0]) {

                if (action != null) {
                    // if this action has menu item already, skip to next iteration
                    if (counted.contains(action)) {
                        continue;
                    }

                    counted.add(action);

                    Integer cntInt = actions.get(action);

                    int cnt = (cntInt == null) ? 0 : cntInt;

                    if (cnt == nodes.length) {
                        result.add(action);
                    }
                } else {
                    // place a separator there
                    result.add(null);
                }
            }

            return result.toArray(new Action[0]);
        } else {
            // no available actions
            return new Action[0];
        }
    }

    /** Test whether the second node is a (direct) child of the first one.
    * @param parent parent node
    * @param son son node
    * @return <code>true</code> if so
    */
    public static boolean isSon(Node parent, Node son) {
        return son.getParentNode() == parent;
    }

    /** Find a path (by name) from one node to the root or a parent.
     * @param node the node to start in
     * @param parent parent node to stop in (can be <code>null</code> for the root)
     * @return list of child names--i.e. a path from the parent to the child node
     * @exception IllegalArgumentException if <code>node</code>'s getName()
     * method returns <code>null</code>
     */
    public static String[] createPath(Node node, Node parent) {
        LinkedList<String> ar = new LinkedList<String>();

        while ((node != null) && (node != parent)) {
            if (node.getName() == null) {
                boolean isFilter = false;

                if (node instanceof FilterNode) {
                    isFilter = true;
                }

                throw new IllegalArgumentException(
                    "Node:" + node.getClass() // NOI18N
                     +"[" + node.getDisplayName() + "]" // NOI18N
                     +(isFilter ? (" of original:" + ((FilterNode) node).getOriginal().getClass()) : "") // NOI18N
                     +" gets null name!"
                ); // NOI18N
            }

            ar.addFirst(node.getName());
            node = node.getParentNode();
        }

        String[] res = new String[ar.size()];
        ar.toArray(res);

        return res;
    }

    /** Look for a node child of given name.
    * @param node node to search in
    * @param name name of child to look for
    * @return the found child, or <code>null</code> if there is no such child
    */
    public static Node findChild(Node node, String name) {
        return node.getChildren().findChild(name);
    }

    /** Traverse a path from a parent node down, by an enumeration of names.
    * @param start node to start searching at
    * @param names enumeration of names of nodes
    *   along the path
    * @return the node with such a path from the start node
    * @exception NodeNotFoundException if the node with such name
    *   does not exists; the exception contains additional information
    *   about the failure.
    */
    public static Node findPath(Node start, Enumeration<String> names)
    throws NodeNotFoundException {
        int depth = 0;

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Node next = findChild(start, name);

            if (next == null) {
                // no element in list matched the name => fail
                // fire exception with the last accessed node and the
                // name of child that does not exists
                throw new NodeNotFoundException(start, name, depth);
            } else {
                // go on next node
                start = next;
            }

            // continue on next depth
            depth++;
        }

        return start;
    }

    /** Traverse a path from a parent node down, by an enumeration of names.
     * @param start node to start searching at
     * @param names names of nodes
     *   along the path
     * @return the node with such a path from the start node
     * @exception NodeNotFoundException if the node with such name
     *   does not exists; the exception contains additional information
     *   about the failure.
     */
    public static Node findPath(Node start, String[] names)
    throws NodeNotFoundException {
        return findPath(start, Enumerations.array(names));
    }

    /** Find the root for a given node.
    * @param node the node
    * @return its root
    */
    public static Node findRoot(Node node) {
        for (;;) {
            Node parent = node.getParentNode();

            if (parent == null) {
                return node;
            }

            node = parent;
        }
    }

    /** Compute a permutation between two arrays of nodes. The arrays
    * must have the same size. The permutation then can be
    * applied to the first array to create the
    * second array.
    *
    * @param arr1 first array
    * @param arr2 second array
    * @return the permutation, or <code>null</code> if the arrays are the same
    * @exception IllegalArgumentException if the arrays cannot be permuted to each other. Either
    *    they have different sizes or they do not contain the same elements.
    */
    public static int[] computePermutation(Node[] arr1, Node[] arr2)
    throws IllegalArgumentException {
        if (arr1.length != arr2.length) {
            int max = Math.max(arr1.length, arr2.length);
            StringBuffer sb = new StringBuffer();

            for (int i = 0; i < max; i++) {
                sb.append(i + " "); // NOI18N

                if (i < arr1.length) {
                    sb.append(arr1[i].getName());
                } else {
                    sb.append("---"); // NOI18N
                }

                sb.append(" = "); // NOI18N

                if (i < arr2.length) {
                    sb.append(arr2[i].getName());
                } else {
                    sb.append("---"); // NOI18N
                }

                sb.append('\n');
            }

            throw new IllegalArgumentException(sb.toString());
        }

        // creates map that assignes to nodes their original
        // position
        Map<Node,Integer> map = new HashMap<Node,Integer>();

        for (int i = 0; i < arr2.length; i++) {
            map.put(arr2[i], i);
        }

        // takes nodes one by one in the new order and
        // creates permutation array
        int[] perm = new int[arr1.length];
        int diff = 0;

        for (int i = 0; i < arr1.length; i++) {
            // get the position of the i-th argument in the second array
            Integer newPos = map.get(arr1[i]);

            if (newPos == null) {
                // not permutation i-th element is missing in the array
                throw new IllegalArgumentException("Missing permutation index " + i); // NOI18N
            }

            // perm must move the object to the newPos
            perm[i] = newPos;

            if (perm[i] != i) {
                diff++;
            }
        }

        return (diff == 0) ? null : perm;
    }

    /** Takes array of nodes and creates array of handles. The nodes that do not
    * have handles are not included in the resulting array.
    *
    * @param nodes array of nodes
    * @return array of Node.Handles
    */
    public static Node.Handle[] toHandles(Node[] nodes) {
        List<Node.Handle> ll = new LinkedList<Node.Handle>();

        for (Node n : nodes) {
            Node.Handle h = n.getHandle();

            if (h != null) {
                ll.add(h);
            }
        }

        return ll.toArray(new Node.Handle[0]);
    }

    /** Takes array of handles and creates array of nodes.
    * @param handles array of handles
    * @return array of nodes
    * @exception IOException if a node cannot be created from the handle
    */
    public static Node[] fromHandles(Node.Handle[] handles)
    throws IOException {
        Node[] arr = new Node[handles.length];

        for (int i = 0; i < handles.length; i++) {
            arr[i] = handles[i].getNode();
        }

        return arr;
    }

    /** Creates a weak implementation of NodeListener.
     *
     * @param l the listener to delegate to
     * @param source the source that the listener should detach from when
     *     listener <CODE>l</CODE> is freed, can be <CODE>null</CODE>
     * @return a NodeListener delegating to <CODE>l</CODE>.
     * @since 4.10
     */
    public static NodeListener weakNodeListener(NodeListener l, Object source) {
        return WeakListeners.create(NodeListener.class, l, source);
    }
    
    /** Registers Property Editors and sets the Property Editors search path.
     * 
     * @see PropertyEditorSearchPath
     * @see PropertyEditorRegistration
     * 
     * @since 7.30
     */
    public static void registerPropertyEditors() {
        NodesRegistrationSupport.registerPropertyEditors();
    }

    /** more info in class javadoc.
     * @since 7.9
     */
    static Node factory(Map<String,?> map) {
        return new LazyNode(map);
    }

    /** Utility method to remove dependency of this package on
     * org.openide.actions. This method takes names of classes from
     * that package and creates their instances.
     *
     * @param arr the array of names like "Tools", "Properties", etc. can
     *   contain nulls
     */
    static SystemAction[] createFromNames(String[] arr) {
        List<SystemAction> ll = new LinkedList<SystemAction>();

        for (String n : arr) {
            if (n == null) {
                ll.add(null);

                continue;
            }

            String name = "org.openide.actions." + n + "Action"; // NOI18N

            try {
                ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
                if (l == null) {
                    l = Thread.currentThread().getContextClassLoader();
                }
                if (l == null) {
                    l = NodeOp.class.getClassLoader();
                }
                Class<? extends SystemAction> c = Class.forName(name, true, l).asSubclass(SystemAction.class);
                ll.add(SystemAction.get(c));
            } catch (ClassNotFoundException ex) {
                warning(ex); // NOI18N

                // otherwise it is probably ok, that the class is missing
            }
        }

        return ll.toArray(new SystemAction[0]);
    }

    /** Notifies an exception to error manager or prints its it to stderr.
     * @param ex exception to notify
     */
    static void exception(Throwable ex) {
        warning(ex); // XXX delete & replace all usages
    }

    /** Notifies an exception to error manager or prints its it to stderr.
     * @param ex exception to notify
     */
    static void warning(Throwable ex) {
        LOG.log(Level.WARNING, null, ex);
    }
}
