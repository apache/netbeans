/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.repository;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle.Messages;
import org.openide.util.WeakListeners;
import static org.netbeans.modules.maven.repository.Bundle.*;

/**
 *
 * @author mkleint
 * @author Anuradha
 */
public class GroupListChildren extends ChildFactory.Detachable<String> implements PropertyChangeListener {

    private RepositoryInfo info;
    static final String KEY_PARTIAL = "____PARTIAL_RESULT";
    private boolean noIndex;

    public GroupListChildren(RepositoryInfo info) {
        this.info = info;
    }

    public void setInfo(RepositoryInfo info) {
        this.info = info;
        refresh(false);
    }
    
    @Override 
    @Messages("TXT_Partial_result=<No result, processing index...>")
    protected Node createNodeForKey(String key) {
        if (KEY_PARTIAL.equals(key)) {
            return createPartialNode();
        }
        return new GroupNode(info, key);
    }

    static Node createPartialNode() {
        AbstractNode node = new AbstractNode(Children.LEAF);
        node.setIconBaseWithExtension("org/netbeans/modules/maven/resources/wait.gif");
        node.setDisplayName(TXT_Partial_result());
        return node;
    }

    protected @Override boolean createKeys(List<String> toPopulate) {
        if(noIndex) {
            return true;
        }
        Result<String> result = RepositoryQueries.getGroupsResult(Collections.singletonList(info));
        toPopulate.addAll(result.getResults());
        if (result.isPartial()) {
            toPopulate.add(KEY_PARTIAL);
        }      
        return true;
    }
    
    protected @Override void addNotify() {
        info.addPropertyChangeListener(WeakListeners.propertyChange(this, info));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(RepositoryInfo.PROP_INDEX_CHANGE.equals(evt.getPropertyName())) {
            noIndex = false;
            refresh(false);
        } else if(RepositoryInfo.PROP_NO_REMOTE_INDEX.equals(evt.getPropertyName())) {
            noIndex = true;
            refresh(false);
        }
    }
}
