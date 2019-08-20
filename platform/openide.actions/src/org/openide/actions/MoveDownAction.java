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
package org.openide.actions;

import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


/** Move an item down in a list.
* This action is final only for performance reasons.
* @see Index
*
* @author   Ian Formanek, Dafe Simonek
*/
public final class MoveDownAction extends NodeAction {
    /** the key to listener to reorder of selected nodes */
    private static final String PROP_ORDER_LISTENER = "sellistener"; // NOI18N

    /** Holds index cookie on which we are listening */
    private Reference<Index> curIndexCookie;

    /* Initilizes the set of properties.
    */
    protected void initialize() {
        super.initialize();

        // initializes the listener
        OrderingListener sl = new OrderingListener();
        putProperty(PROP_ORDER_LISTENER, sl);
    }

    private Index getCurIndexCookie() {
        return curIndexCookie == null ? null : curIndexCookie.get();
    }

    protected void performAction(Node[] activatedNodes) {
        // we need to check activatedNodes, because there's no
        // guarantee that they not changed between enable() and
        // performAction calls
        Index cookie = getIndexCookie(activatedNodes);

        if (cookie == null) {
            return;
        }

        int nodeIndex = cookie.indexOf(activatedNodes[0]);

        if ((nodeIndex >= 0) && (nodeIndex < (cookie.getNodesCount() - 1))) {
            cookie.moveDown(nodeIndex);
        }
    }

    protected boolean asynchronous() {
        return false;
    }

    protected boolean enable(Node[] activatedNodes) {
        // remove old listener, if any
        Index idx = getCurIndexCookie();

        if (idx != null) {
            idx.removeChangeListener((ChangeListener) getProperty(PROP_ORDER_LISTENER));
            idx = null;
        }

        Index cookie = getIndexCookie(activatedNodes);

        if (cookie == null) {
            return false;
        }

        int nodeIndex = cookie.indexOf(activatedNodes[0]);

        // now start listening to reordering changes
        cookie.addChangeListener((OrderingListener) getProperty(PROP_ORDER_LISTENER));
        curIndexCookie = new WeakReference<Index>(cookie);

        return (nodeIndex >= 0) && (nodeIndex < (cookie.getNodesCount() - 1));
    }

    public String getName() {
        return NbBundle.getMessage(MoveDownAction.class, "MoveDown");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(MoveDownAction.class);
    }

    /** Helper method. Returns index cookie or null, if some
    * conditions aren't satisfied */
    private Index getIndexCookie(Node[] activatedNodes) {
        if ((activatedNodes == null) || (activatedNodes.length != 1)) {
            return null;
        }

        Node parent = activatedNodes[0].getParentNode();

        if (parent == null) {
            return null;
        }

        return parent.getCookie(Index.class);
    }

    /** Listens to the ordering changes and enables/disables the
    * action if appropriate */
    private final class OrderingListener implements ChangeListener {
        OrderingListener() {
        }

        public void stateChanged(ChangeEvent e) {
            Node[] activatedNodes = getActivatedNodes();
            Index cookie = getIndexCookie(activatedNodes);

            if (cookie == null) {
                setEnabled(false);
            } else {
                int nodeIndex = cookie.indexOf(activatedNodes[0]);
                setEnabled((nodeIndex >= 0) && (nodeIndex < (cookie.getNodesCount() - 1)));
            }
        }
    }
}
