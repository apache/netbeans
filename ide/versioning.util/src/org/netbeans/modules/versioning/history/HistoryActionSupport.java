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
package org.netbeans.modules.versioning.history;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * Base class for actions returned via {@link HistoryEntry#getActions()}.
 * 
 * @author Tomas Stupka
 */
class HistoryActionSupport<H> {

    private final Callback<H> callback;
    private static final Logger LOG = Logger.getLogger(HistoryActionSupport.class.getName());
    
    public interface Callback<H> {
        void call(H entry, Set<VCSFileProxy> files);
        HistoryEntryWrapper<H> lookupEntry(Node node);
        Lookup getContext();
        boolean isMultipleHistory();
    }

    public HistoryActionSupport(Callback<H> callback) {
        this.callback = callback;
    }

    protected String getRevisionShort() {
        if(callback.getContext() == null) {
            // this is strange, but there seem to be cases when the context wasn't set yet.
            // see also issue #220820
            return null;
        }
        Collection<? extends Node> nodes = callback.getContext().lookupAll(Node.class);
        HistoryEntryWrapper<H> he = null;
        for(Node node : nodes) {
            he = callback.lookupEntry(node);
            if(he != null) {
                break;
            }
        }
        if (he == null) {
            LOG.log(Level.WARNING, "No history entry under the nodes");
            for (Node node : nodes) {
                LOG.log(Level.INFO, "Node {0} --- {1}", new Object[] { node, node.getLookup().lookupAll(Object.class) });
            }
            assert he != null;
        }
        return he == null ? null : he.getRevisionShort();
    }

    protected void performAction(Node[] activatedNodes) {
        Map<HistoryEntryWrapper<H>, Set<VCSFileProxy>> m = new HashMap<HistoryEntryWrapper<H>, Set<VCSFileProxy>>(activatedNodes.length);
        for(Node node : activatedNodes) {
            HistoryEntryWrapper<H> he = callback.lookupEntry(node);
            if(he == null) {
                continue;
            }                    

            Collection<? extends VCSFileProxy> fos = node.getLookup().lookupAll(VCSFileProxy.class);
            assert fos != null;  

            Set<VCSFileProxy> files = m.get(he);
            if(files == null) {
                files = new HashSet<VCSFileProxy>();
                m.put(he, files);
            }
            for (VCSFileProxy f : fos) {
                if(f != null) {
                    files.add(f);
                }
            }
        }
        for(Map.Entry<HistoryEntryWrapper<H>, Set<VCSFileProxy>> e : m.entrySet()) {
            Set<VCSFileProxy> files = e.getValue();
            if(files != null && !files.isEmpty()) {
                callback.call(e.getKey().getHistoryEntry(), e.getValue());
            }
        }
    }

    boolean hasEntryAndFiles(Node[] nodes) {
        boolean multipleHistory = callback.isMultipleHistory();
        VCSFileProxy file = null;
        HistoryEntryWrapper historyEntry = null;
        for(Node node : nodes) {
            HistoryEntryWrapper he = callback.lookupEntry(node);
            if(he == null) {
                continue;
            }                    
            if(historyEntry == null) {
                historyEntry = he;
            } else if(!multipleHistory) {
                if(!he.getDateTime().equals(historyEntry.getDateTime()) ||
                !he.getRevision().equals(historyEntry.getRevision())) 
                {
                    return false;
                }
            }
            Collection<? extends VCSFileProxy> fos = lookupFiles(node);
            if(fos == null) {
                continue;
            }
            for (VCSFileProxy f : fos) {
                if(f != null) {
                    file = f;
                    break;
                }
            }
            if(multipleHistory && historyEntry != null && file != null) {
                return true;
            }
        }
        return historyEntry != null && file != null;
    }
    
    private Collection<? extends VCSFileProxy> lookupFiles(Node node) {
        return node.getLookup().lookupAll(VCSFileProxy.class);
    }
    
    interface HistoryEntryWrapper<H> {
        H getHistoryEntry();
        String getRevisionShort();
        Date getDateTime();
        String getRevision();
    }
}
