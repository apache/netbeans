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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.derby;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 * Provides a node for the local Java DB server under the Databases node
 * in the Database Explorer 
 * 
 * @author David Van Couvering
 */
public class DerbyServerNode extends AbstractNode implements Comparable {
    private static final DerbyDatabasesImpl DATABASES_IMPL = DerbyDatabasesImpl.getDefault();
    private static final ChildFactory FACTORY = new ChildFactory(DATABASES_IMPL);
    private static final DerbyServerNode DEFAULT = new DerbyServerNode(FACTORY);

    private SystemAction[] actions = new SystemAction[] {
            SystemAction.get(StartAction.class),
            SystemAction.get(StopAction.class),
            SystemAction.get(CreateDatabaseAction.class),
            SystemAction.get(CreateSampleDBAction.class),
            SystemAction.get(DerbyPropertiesAction.class)
        };
    
    // I'd like a less generic icon, but this is what we have for now...
    private static final String ICON_BASE = "org/netbeans/modules/derby/resources/catalog.gif";
    
    public static DerbyServerNode getDefault() {
        return DEFAULT;
    }
    
    private DerbyServerNode(ChildFactory f) {
        super(Children.create(f, true));
        this.setIconBaseWithExtension(ICON_BASE);
    }
    
    @Override
    public String getDisplayName() {
       // Product name - no need to internationalize
       return "Java DB"; // NOI18N
    }
   
    @Override
    public SystemAction[] getActions(boolean b) {
        return actions;
    }
    
    @Override
    public boolean canCopy() {
        return false;
    }
    
    @Override
    public boolean canCut() {
        return false;
    }
    
    @Override
    public boolean canRename() {
        return false;
    }
    
    @Override
    public boolean canDestroy() {
        return true;
    }
    
    @Override
    public SystemAction getPreferredAction() {
        return null;
    }

    @Override
    public int compareTo(Object other) {
        Node otherNode = (Node)other;
        return this.getDisplayName().compareTo(otherNode.getDisplayName());
    }

    private static class ChildFactory
            extends org.openide.nodes.ChildFactory<String> implements ChangeListener {

        private DerbyDatabasesImpl databasesImpl;

        @SuppressWarnings("LeakingThisInConstructor")
        public ChildFactory(DerbyDatabasesImpl impl) {
            this.databasesImpl = impl;
            impl.addChangeListener(
                WeakListeners.create(ChangeListener.class, this, impl));
        }

        @Override
        protected Node createNodeForKey(String db) {
            return new DerbyDatabaseNode(db, databasesImpl);
        }

        @Override
        protected boolean createKeys(List<String> toPopulate) {
            List<String> fresh = new ArrayList<String>();

            fresh.addAll(databasesImpl.getDatabases());

            Collections.sort(fresh);
            toPopulate.addAll(fresh);

            return true;
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

}
