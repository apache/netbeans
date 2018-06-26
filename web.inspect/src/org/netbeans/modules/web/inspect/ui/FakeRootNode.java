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
package org.netbeans.modules.web.inspect.ui;

import javax.swing.Action;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Node whose sole purpose is to serve as a hidden root node
 * in {@code TreeView} and provide a context menu for this view
 * through its actions.
 *
 * @author Jan Stola
 * @param <T> type of the real root node.
 */
public class FakeRootNode<T extends Node> extends AbstractNode {
    /** Real root node, i.e., the only child of this fake root. */
    private final T realRoot;
    /** Actions of this node. */
    private final Action[] actions;

    /**
     * Creates a new {@code FakeRootNode}.
     * 
     * @param realRoot real root node.
     * @param actions actions of the new fake root node.
     */
    public FakeRootNode(T realRoot, Action[] actions) {
        super(new FakeRootChildren(realRoot));
        this.realRoot = realRoot;
        this.actions = actions;
    }

    /**
     * Returns the real root node.
     * 
     * @return real root node.
     */
    public T getRealRoot() {
        return realRoot;
    }
    
    @Override
    public Action[] getActions(boolean context) {
        return actions;
    }

    /**
     * Children used by {@code FakeRootNode}. The only child provided by
     * these children is the real root node.
     * 
     * @param <T> type of the real root node.
     */
    static class FakeRootChildren<T extends Node> extends Children.Keys<String> {
        /** Key for the real root node. */
        private static final String ROOT_KEY = "root"; // NOI18N
        /** Real root node. */
        private final T realRoot;

        /**
         * Creates a new {@code FakeRootChildren}.
         * 
         * @param realRoot real root node.
         */
        FakeRootChildren(T realRoot) {
            this.realRoot = realRoot;
            setKeys(new String[]{ROOT_KEY});
        }

        @Override
        protected Node[] createNodes(String key) {
            Node[] result;
            if (ROOT_KEY.equals(key)) {
                result = new Node[] {realRoot};
            } else {
                result = new Node[0];
            }
            return result;
        }

    }
    
}
