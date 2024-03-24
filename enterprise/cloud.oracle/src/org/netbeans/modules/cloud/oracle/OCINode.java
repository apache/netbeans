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
package org.netbeans.modules.cloud.oracle;

import com.oracle.bmc.auth.BasicAuthenticationDetailsProvider;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cloud.oracle.items.OCIItem;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jan Horvath
 */
public class OCINode extends AbstractNode {
    private RefreshListener refreshListener;

    final OCIItem item;
    private final CloudChildFactory factory;
    final OCISessionInitiator session;

    public OCINode(OCIItem item) {
        this(new CloudChildFactory(item), item, OCIManager.getDefault().getActiveSession(), Lookups.fixed(item));
    }
    
    public OCINode(OCIItem item, OCISessionInitiator session) {
        this(new CloudChildFactory(session, item), item, session, Lookups.fixed(item, session));
    }
    
    private OCINode(CloudChildFactory factory, OCIItem item, OCISessionInitiator session, Lookup lookup) {
        super(Children.create(factory, true), lookup);
        setName(item.getName());
        this.item = item;
        this.factory = factory;
        this.session = session;
        refreshListener = new RefreshListener();
        item.addChangeListener(refreshListener);
    }
    
    public OCINode(OCIItem item, Children children) {
        super(children, Lookups.fixed(item, OCIManager.getDefault().getActiveSession()));
        setName(item.getName());
        this.item = item;
        this.factory = null;
        this.session = OCIManager.getDefault().getActiveSession();
        refreshListener = new RefreshListener();
        item.addChangeListener(refreshListener);
    }
    
    protected BasicAuthenticationDetailsProvider getAuthProvider() {
        return session.getAuthenticationProvider();
    }

    @Override
    public Action[] getActions(boolean context) {
        List<Action> result = new ArrayList<>();
        
        List<? extends Action> commonActions = actionsForPath(
                "Cloud/Oracle/Common/Actions", getLookup());
        for (Action commonAction : commonActions) {
            if (commonAction.isEnabled()) {
                result.add(commonAction);
            }
        }
        
        result.addAll(actionsForPath(
                String.format("Cloud/Oracle/%s/Actions",
                        item.getKey().getPath()), getLookup()));

        return result.toArray(new Action[0]); // NOI18N
    }
    
    public static final List<? extends Action> actionsForPath(String path, Lookup lkp) {
        List<? extends Action> actions = Utilities.actionsForPath(path);
        List<Action> ret = new ArrayList<>(actions.size());
        for (Action a : actions) {
            if (a instanceof ContextAwareAction) {
                a = ((ContextAwareAction)a).createContextAwareInstance(lkp);
                if (a == null) {
                    continue;
                }
            }
            ret.add(a);
        }
        return ret;
    }
    
    public void refresh() {
        if (factory != null) {
            factory.refreshKeys();
        }
    }

    @Override
    public Node.Handle getHandle() {
        return super.getHandle();
    }

    private final class RefreshListener implements ChangeListener {
        @Override
        public void stateChanged(ChangeEvent e) {
            refresh();
        }
    }
    
}
