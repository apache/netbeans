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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.api.db.explorer.node;

import java.util.Collection;
import java.util.List;
import org.netbeans.modules.db.explorer.node.BaseFilterNode;
import org.netbeans.modules.db.explorer.node.NodeRegistry;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * The ChildNodeFactory is used for getting node instances based on
 * a Lookup instance as the key.  Subclasses of BaseNode that can have
 * children are constructed with an instance of ChildNodeFactory.
 * 
 * @author Rob Englander
 */
public class ChildNodeFactory extends ChildFactory<Lookup> {
    
    private final Lookup dataLookup;

    /**
     * Constructor. 
     * 
     * @param dataLookup the associated data lookup
     */
    public ChildNodeFactory(Lookup lookup) {
        dataLookup = lookup;
    }

    /**
     * Refreshes this factory which causes it to get its
     * child keys and subsequently its child nodes 
     */
    public void refresh() {
        super.refresh(false);
    }

    /**
     * Refreshes this factory which causes it to get its
     * child keys and subsequently its child nodes immeditately.
     */
    public void refreshSync() {
        super.refresh(true);
    }

    @Override
    public Node[] createNodesForKey(Lookup key) {
        
        // the node should be in the lookup
        Node childNode = key.lookup(Node.class);
        
        if (childNode == null) {
            return new Node[] {  };
        }
        else {
            return new Node[]{new BaseFilterNode(childNode)}; // clone - #221817
        }
    }

    @Override
    protected boolean createKeys(List toPopulate) {
        
        // the node registry is in the data lookup
        NodeRegistry registry = dataLookup.lookup(NodeRegistry.class);
        Collection<? extends Node> nodes = registry.getNodes();
        for (Node node : nodes) {
            // the key for each node is its lookup
            Lookup lookup = node.getLookup();
            toPopulate.add(lookup);
        }

        return true;
    }
}
