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
package org.netbeans.modules.git.ui.history;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

import java.util.*;
import org.openide.nodes.AbstractNode;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 * Represents children of a Revision Node in Search history results table.
 *
 * @author Maros Sandor
 */
class RevisionNodeChildren extends Children.Keys<Object> implements PropertyChangeListener {

    private RepositoryRevision container;
    private SearchHistoryPanel master;
    private boolean nodesCreated;
    private final PropertyChangeListener list;

    public RevisionNodeChildren(RepositoryRevision container, SearchHistoryPanel master) {
        this.container = container;
        this.master = master;
        container.addPropertyChangeListener(RepositoryRevision.PROP_EVENTS_CHANGED, list = WeakListeners.propertyChange(this, container));
    }

    @Override
    protected void addNotify() {
        refreshKeys();
    }

    @Override
    protected void removeNotify() {
        setKeys (Collections.<Object>emptySet());
    }
    
    private void refreshKeys() {
        if (container.expandEvents()) {
            setKeys(new Object[] { new Object() });
        } else {
            setKeys(container.getEvents());
        }
    }
    
    @Override
    public void propertyChange (PropertyChangeEvent evt) {
        if (nodesCreated && RepositoryRevision.PROP_EVENTS_CHANGED.equals(evt.getPropertyName()) && evt.getSource() == container) {
            refreshKeys();
        }
    }
    
    @Override
    protected Node[] createNodes (Object fn) {
        nodesCreated = true;
        Node node;
        if (fn instanceof RepositoryRevision.Event) {
            node = new RevisionNode((RepositoryRevision.Event) fn, master);
        } else {
            node = new AbstractNode(Children.LEAF) {

                @Override
                public String getName () {
                    return NbBundle.getMessage(RevisionNodeChildren.class, "MSG_RevisionNodeChildren.Loading"); //NOI18N
                }
                
            };
        }
        return new Node[] { node };
    }
}

