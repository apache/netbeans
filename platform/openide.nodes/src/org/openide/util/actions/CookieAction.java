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
package org.openide.util.actions;

import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;
import org.openide.util.Lookup;
import org.openide.util.Mutex;

import java.beans.PropertyChangeEvent;

import java.lang.ref.*;

import java.util.*;


/** Not the preferred solution anymore, rather use
* <a href="@org-openide-awt@/org/openide/awt/Actions.html#context-java.lang.Class-boolean-boolean-org.openide.util.ContextAwareAction-java.lang.String-java.lang.String-java.lang.String-boolean-">Actions.context</a>.
* To replace your action
* <a href="@org-openide-modules@/org/openide/modules/doc-files/api.html#how-layer">
* layer definition</a> use more delarative way:
* <pre>
* &lt;file name="action-pkg-ClassName.instance"&gt;
*   &lt;attr name="type" stringvalue="org.netbeans.api.actions.Openable"/&gt;
*   &lt;attr name="delegate" methodvalue="org.openide.awt.Action.inject"/&gt;
*   &lt;attr name="selectionType" stringvalue="ANY"/&gt;
*   &lt;attr name="injectable" stringvalue="pkg.YourClass"/&gt;
*   &lt;attr name="displayName" bundlevalue="your.pkg.Bundle#key"/&gt;
*   &lt;attr name="iconBase" stringvalue="your/pkg/YourImage.png"/&gt;
*   &lt;!-- if desired: &lt;attr name="noIconInMenu" boolvalue="false"/&gt; --&gt;
* &lt;/file&gt;
* </pre>
*
* @author   Petr Hamernik, Jaroslav Tulach, Dafe Simonek, Jesse Glick
*/
public abstract class CookieAction extends NodeAction {
    /** name of property with cookies for this action */
    private static final String PROP_COOKIES = "cookies"; // NOI18N

    /** Action will be enabled if there are one or more selected nodes
    * and there is exactly one node which supports the given cookies. */
    public static final int MODE_ONE = 0x01;

    /** Action will be enabled if there are several selected nodes
    * and some of them (at least one, but not all)
    * support the given cookies. */
    public static final int MODE_SOME = 0x02;

    /** Action will be enabled if there are one or more selected nodes
    * and all of them support the given cookies. */
    public static final int MODE_ALL = 0x04;

    /** Action will be enabled if there is exactly one selected node
    * and it supports the given cookies. */
    public static final int MODE_EXACTLY_ONE = 0x08;

    /** Action will be enabled if there are one or more selected nodes
    * and any of them (one, all, or some) support the given cookies. */
    public static final int MODE_ANY = 0x07;

    // [PENDING] 0x06 should suffice, yes? --jglick
    private static final long serialVersionUID = 6031319415908298424L;
    private CookiesChangeListener listener = new CookiesChangeListener(this);

    /** Get the mode of the action: how strict it should be about
    * cookie support.
    * @return the mode of the action. Possible values are disjunctions of the <code>MODE_XXX</code>
    * constants. */
    protected abstract int mode();

    /** Get the cookies that this action requires. The cookies are disjunctive, i.e. a node
    * must support AT LEAST ONE of the cookies specified by this method.
    *
    * @return a list of cookies
    */
    protected abstract Class<?>[] cookieClasses(); // might not extend Node.Cookie; seems to work with Lookup

    /** Getter for cookies.
    * @return the set of cookies for this
    */
    private Class<?>[] getCookies() {
        Class[] ret = (Class[]) getProperty(PROP_COOKIES);

        if (ret != null) {
            return ret;
        }

        ret = cookieClasses();
        putProperty(PROP_COOKIES, ret);

        return ret;
    }

    /** Test for enablement based on the cookies of selected nodes.
    * Generally subclasses should not override this except for strange
    * purposes, and then only calling the super method and adding a check.
    * Just use {@link #cookieClasses} and {@link #mode} to specify
    * the enablement logic.
    * @param activatedNodes the set of activated nodes
    * @return <code>true</code> to enable
    */
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }

        // sets new nodes to cookie change listener
        listener.setNodes(activatedNodes);

        // perform enable / disable logic
        return doEnable(activatedNodes);
    }

    /** Implements <code>ContextAwareAction</code> interface method. */
    public javax.swing.Action createContextAwareInstance(Lookup actionContext) {
        return new CookieDelegateAction(this, actionContext);
    }

    /** Helper, actually performs enable / disable logic */
    boolean doEnable(Node[] activatedNodes) {
        int supported = resolveSupported(activatedNodes);

        if (supported == 0) {
            return false;
        }

        int mode = mode();

        return 
        // [PENDING] shouldn't MODE_ONE also say: && supported == 1? --jglick
        ((mode & MODE_ONE) != 0) || (((mode & MODE_ALL) != 0) && (supported == activatedNodes.length)) ||
        (((mode & MODE_EXACTLY_ONE) != 0) && (activatedNodes.length == 1)) ||
        (((mode & MODE_SOME) != 0) && (supported < activatedNodes.length));
    }

    /**
    * Implementation of the above method.
    *
    * @param activatedNodes gives array of actually activated nodes.
    * @return number of supported classes
    */
    private int resolveSupported(Node[] activatedNodes) {
        int ret = 0;

        Class<?>[] cookies = getCookies();

        for (Node n : activatedNodes) {
            for (Class<?> cookie : cookies) {
                // test for supported cookies
                @SuppressWarnings("unchecked")
                Lookup.Template<?> templ = new Lookup.Template<>(cookie);
                if (n.getLookup().lookupItem(templ) != null) {
                    ret++;

                    break;
                }
            }
        }

        return ret;
    }

    /** Tracks changes of cookie classes in currently selected nodes
    */
    private static final class CookiesChangeListener extends NodeAdapter {
        /** our weak listener */
        private org.openide.nodes.NodeListener listener;

        /** The nodes we are currently listening */
        private volatile List<Reference<Node>> nodes;

        /** the associated action */
        private Reference<CookieAction> action;

        /** Constructor - asociates with given cookie action
        */
        public CookiesChangeListener(CookieAction a) {
            listener = org.openide.nodes.NodeOp.weakNodeListener(this, null);
            action = new WeakReference<CookieAction>(a);
        }

        /** Sets the nodes to work on */
        void setNodes(Node[] newNodes) {
            // detach old nodes
            List<Reference<Node>> nodes2 = this.nodes;

            if (nodes2 != null) {
                detachListeners(nodes2);
            }

            nodes = null;

            // attach to new nodes
            if (newNodes != null) {
                List<Reference<Node>> tmp = new ArrayList<Reference<Node>>(newNodes.length);

                for (int i = 0; i < newNodes.length; i++)
                    tmp.add(new WeakReference<Node>(newNodes[i]));

                attachListeners(tmp);
                this.nodes = tmp;
            }
        }

        /** Removes itself as a listener from given nodes */
        void detachListeners(List<Reference<Node>> nodes) {
            if (nodes == null) {
                return;
            }
            Iterator<Reference<Node>> it = nodes.iterator();

            while (it.hasNext()) {
                Node node = it.next().get();

                if (node != null) {
                    node.removeNodeListener(listener);
                }
            }
        }

        /** Attach itself as a listener to the given nodes */
        void attachListeners(List<Reference<Node>> nodes) {
            Iterator<Reference<Node>> it = nodes.iterator();

            while (it.hasNext()) {
                Node node = it.next().get();

                if (node != null) {
                    node.addNodeListener(listener);
                }
            }
        }

        /** Reacts to the cookie classes change -
        * calls enable on asociated action */
        public void propertyChange(PropertyChangeEvent ev) {
            // filter only cookie classes changes
            if (!Node.PROP_COOKIE.equals(ev.getPropertyName())) {
                return;
            }

            // find asociated action
            final CookieAction a = action.get();

            if (a == null) {
                return;
            }

            List<Reference<Node>> _nodes = this.nodes;

            if (_nodes != null) {
                ArrayList<Node> nonNullNodes = new ArrayList<Node>(_nodes.size());
                Iterator<Reference<Node>> it = _nodes.iterator();

                while (it.hasNext()) {
                    Node node = it.next().get();

                    if (node != null) {
                        nonNullNodes.add(node);
                    } else {
                        // If there is really a selection, it should not have been collected.
                        return;
                    }
                }

                final Node[] nodes2 = new Node[nonNullNodes.size()];
                nonNullNodes.toArray(nodes2);

                Mutex.EVENT.writeAccess(
                    new Runnable() {
                        public void run() {
                            a.setEnabled(a.enable(nodes2));
                        }
                    }
                );
            }
        }

        protected void finalize() {
            detachListeners(nodes);
        }
    }
     // end of CookiesChangeListener

    /** A delegate action that is usually associated with a specific lookup and
     * extract the nodes it operates on from it. Otherwise it delegates to the
     * regular NodeAction.
     */
    static final class CookieDelegateAction extends org.openide.util.actions.NodeAction.DelegateAction
    implements org.openide.nodes.NodeListener, Runnable {
        /** our weak listener */
        private org.openide.nodes.NodeListener listener;

        /** The nodes we are currently listening */
        private List<Reference<Node>> nodes;

        public CookieDelegateAction(CookieAction a, Lookup actionContext) {
            super(a, actionContext);
            listener = org.openide.nodes.NodeOp.weakNodeListener(this, null);
            setNodes(nodes());
        }

        public void resultChanged(org.openide.util.LookupEvent ev) {
            setNodes(nodes());
            superResultChanged(ev);
        }

        private void superResultChanged(org.openide.util.LookupEvent ev) {
            super.resultChanged(ev);
        }

        public void childrenAdded(org.openide.nodes.NodeMemberEvent ev) {
        }

        public void childrenRemoved(org.openide.nodes.NodeMemberEvent ev) {
        }

        public void childrenReordered(org.openide.nodes.NodeReorderEvent ev) {
        }

        public void nodeDestroyed(org.openide.nodes.NodeEvent ev) {
        }

        public void propertyChange(PropertyChangeEvent ev) {
            // filter only cookie classes changes
            if (!Node.PROP_COOKIE.equals(ev.getPropertyName())) {
                return;
            }

            // find asociated action
            Mutex.EVENT.readAccess(this);
        }

        public void run() {
            superResultChanged(null);
        }

        /** Attach itself as a listener own nodes */
        private void setNodes(org.openide.nodes.Node[] newNodes) {
            // detach listeners from old nodes
            detachListeners(nodes);

            // attach to new nodes
            if (newNodes != null) {
                nodes = new ArrayList<Reference<Node>>(newNodes.length);

                for (int i = 0; i < newNodes.length; i++)
                    nodes.add(new WeakReference<Node>(newNodes[i]));
            }

            // attach listeners to new nodes
            attachListeners(nodes);
        }

        /** Removes itself as a listener from given nodes */
        private void detachListeners(List<Reference<Node>> nodes) {
            if (nodes != null) {
                Iterator<Reference<Node>> it = nodes.iterator();

                while (it.hasNext()) {
                    Node node = it.next().get();

                    if (node != null) {
                        node.removeNodeListener(listener);
                    }
                }
            }
        }

        /** Attach itself as a listener to the given nodes */
        private void attachListeners(List<Reference<Node>> nodes) {
            if (nodes != null) {
                Iterator<Reference<Node>> it = nodes.iterator();

                while (it.hasNext()) {
                    Node node = it.next().get();

                    if (node != null) {
                        node.addNodeListener(listener);
                    }
                }
            }
        }

        protected void finalize() {
            detachListeners(nodes);
        }
    }
     // end of CookieDelegateAction
}
