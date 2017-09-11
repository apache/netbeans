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

import java.util.List;
import javax.swing.event.ChangeListener;
import org.openide.nodes.Node;

/**
 * Represents a series of nodes which can be spliced into a children list.
 * @param K the type of key you would like to use to represent nodes
 * @author mkleint
 * @since org.netbeans.modules.projectuiapi/1 1.18
 * @see NodeFactory
 * @see NodeFactorySupport
 * @see org.openide.nodes.Children.Keys
 */
public interface NodeList<K> {
    /**
     * Obtains child keys which will be passed to {@link #node}.
     * If there is a change in the set of keys based on external events,
     * fire a <code>ChangeEvent</code>.
     * @return list of zero or more keys to display
     */
    List<K> keys();
    /**
     * Adds a listener to a change in keys.
     * @param l a listener to add
     */
    void addChangeListener(ChangeListener l);
    /**
     * Removes a change listener.
     * @param l a listener to remove
     */
    void removeChangeListener(ChangeListener l);
    /**
     * Creates a node for a given key.
     * @param key a key which was included in {@link #keys}
     * @return a node which should represent that key visually or null if no such node can be created currently.
     */
    Node node(K key);
    /**
     * Called when the node list is to be active. Equivalent to {@link org.openide.nodes.Children#addNotify}.
     * If there is any need to register listeners or begin caching of state, do it here.
     * @see org.openide.nodes.Children#addNotify
     */
    void addNotify();
    /**
     * Called when the node list is no longer needed. Equivalent to {@link org.openide.nodes.Children#removeNotify}.
     * @see org.openide.nodes.Children#removeNotify
     */
    void removeNotify();
}
