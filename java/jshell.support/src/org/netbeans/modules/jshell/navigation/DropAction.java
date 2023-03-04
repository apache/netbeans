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
package org.netbeans.modules.jshell.navigation;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.jshell.env.JShellEnvironment;
import org.netbeans.modules.jshell.model.SnippetHandle;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@NbBundle.Messages({
    "ACTION_DropSnippet=Drop snippet",
    "# {0} - snippet count",
    "ACTION_DropSnippet_Count=Drop {0} snippets"
})
public class DropAction extends AbstractAction implements ContextAwareAction {
    private final Collection<SnippetHandle> handles;
    private final JShellEnvironment env;
    
    public DropAction() {
        super(Bundle.ACTION_DropSnippet());
        handles = Collections.emptyList();
        this.env = null;
    }
    
    private DropAction(JShellEnvironment e, Collection<SnippetHandle> handles) {
        this.handles = handles;
        boolean enable = false;
        int cnt = 0;
        
        for (SnippetHandle theHandle : handles) {
            switch (theHandle.getStatus()) {
                case DROPPED:
                case NONEXISTENT:
                case OVERWRITTEN:
                case REJECTED:
                    break;
                default:
                    boolean x = e.getShell() == theHandle.getState();
                    if (x) {
                        cnt++;
                        enable = true;
                    }
                    break;
            }
        }
        setEnabled(enable);
        this.env = e;
        putValue(Action.NAME, enable && cnt > 1 ?
                            Bundle.ACTION_DropSnippet_Count(cnt) :
                            Bundle.ACTION_DropSnippet()
        );
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (handles.isEmpty()) {
            return;
        }
        for (SnippetHandle h : handles) {
            if (env.getShell() == h.getState()) {
                switch (h.getStatus()) {
                    case DROPPED:
                    case NONEXISTENT:
                    case OVERWRITTEN:
                    case REJECTED:
                        break;
                    default:
                        env.getShell().drop(h.getSnippet());
                }
            }
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<? extends Node> nodes = actionContext.lookupAll(Node.class);
        Collection<SnippetHandle> handles = new ArrayList<>();
        for (Node n : nodes) {
            SnippetHandle h = n.getLookup().lookup(SnippetHandle.class);
            if (h != null) {
                handles.add(h);
            }
        }
        return new DropAction(actionContext.lookup(JShellEnvironment.class), handles);
    }
    
    
}
