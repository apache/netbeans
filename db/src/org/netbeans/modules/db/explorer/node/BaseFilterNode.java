/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.node;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.db.explorer.action.RefreshAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 * Extension of FilterNode. It can refresh its parent node (or some other
 * ancestor) after it is destroyed, if the delegate wishes to do it (the
 * delegate sets attribute {@link #REFRESH_ANCESTOR_DISTANCE}). See bug #222113.
 *
 * @author jhavlin
 */
public class BaseFilterNode extends FilterNode {

    /**
     * Distance of ancestor to refresh. If the original node (delegate) needs to
     * refresh its parent (ancestor at distance 1) or some other ancestor after
     * it is destroyed, it has to set attribute with this key to distance of
     * ancestor to refresh. (1 for parent, 2 for grandparent, ...). See
     * {@link Node#setValue(java.lang.String, java.lang.Object)}.
     */
    public static final String REFRESH_ANCESTOR_DISTANCE =
            "BaseFilterNode.refreshAncestorDistance";                   //NOI18N
    /** */
    private static RequestProcessor RP =
            new RequestProcessor(BaseFilterNode.class);

    private static final Map<Node, Object> NODES_TO_REFRESH =
            new WeakHashMap<Node, Object>();

    public BaseFilterNode(Node original) {
        super(original);
    }

    /**
     * Destroy the original node and check if it wishes to refresh some of its
     * ancestors.
     */
    @Override
    public void destroy() throws IOException {
        super.destroy();
        Object ancestorDist = getOriginal().getValue(REFRESH_ANCESTOR_DISTANCE);
        int ancestorDistInt = (ancestorDist instanceof Integer)
                ? (Integer) ancestorDist : 0;
        if (ancestorDistInt > 0) {
            int currentDist = 0;
            Node ancestor = this;
            while (currentDist < ancestorDistInt) {
                if (ancestor.getParentNode() != null) {
                    ancestor = ancestor.getParentNode();
                    currentDist++;
                } else {
                    break;
                }
            }
            if (currentDist > 0) {
                scheduleRefresh(ancestor);
            }
        }
    }

    /**
     * Schedule refreshing of a node. Do not schedule refreshing if it is
     * already scheduled. See bug #216907
     */
    private static void scheduleRefresh(final Node node) {
        synchronized (NODES_TO_REFRESH) {
            if (NODES_TO_REFRESH.containsKey(node)) {
                return;
            } else {
                NODES_TO_REFRESH.put(node, new Object());
            }
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                SystemAction.get(RefreshAction.class).performAction(
                        new Node[]{node});
                synchronized (NODES_TO_REFRESH) {
                    NODES_TO_REFRESH.remove(node);
                }
            }
        }, 250);
    }
}
