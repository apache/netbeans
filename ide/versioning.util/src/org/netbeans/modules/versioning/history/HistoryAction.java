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

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.history.HistoryActionSupport.Callback;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.spi.VCSHistoryProvider.HistoryEntry;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 * Base class for actions returned via {@link HistoryEntry#getActions()}.<br/>
 * Should be used by Versionig systems which depend on the io.File based o.n.m.versioning.spi
 * 
 * @author Tomas Stupka
 */
public abstract class HistoryAction extends NodeAction {

    private Lookup context;
    private final HistoryActionSupport<VCSHistoryProvider.HistoryEntry> support; 
    private HistoryActionSupport.Callback<HistoryEntry> callback;
    private final String name;
    private final boolean multipleHistory;
    
    public HistoryAction() {
        this(null, true);
    }
    public HistoryAction(String name) {
        this(name, true);
    }
    public HistoryAction(String name, boolean multipleHistory) {
        support = new HistoryActionSupport<VCSHistoryProvider.HistoryEntry>(getCallback());
        this.name = name;
        this.multipleHistory = multipleHistory;
    }
    
    /**
     * Perform this action for the given HistoryEntry and files.
     * 
     * @param entry
     * @param value 
     */
    protected abstract void perform(VCSHistoryProvider.HistoryEntry entry, Set<File> files);

    @Override
    public String getName() {
        assert name != null;
        return name;
    }
    
    protected boolean isMultipleHistory() {
        return multipleHistory;
    }

    protected String getRevisionShort() {
        return support.getRevisionShort();
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        this.context = actionContext;
        return super.createContextAwareInstance(actionContext);
    }

    private Lookup getContext() {
        return context;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        support.performAction(activatedNodes);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return support.hasEntryAndFiles(activatedNodes);
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    private Callback<HistoryEntry> getCallback() {
        if(callback == null) {
            callback = new Callback<HistoryEntry>() {
                @Override
                public void call(HistoryEntry entry, Set<VCSFileProxy> files) {
                    Set<File> s = new HashSet<File>();
                    for (VCSFileProxy file : files) {
                        File f = file.toFile();
                        if(f != null) {
                            s.add(f);
                        }
                    }
                    perform(entry, s);
                }
                @Override
                public HistoryActionSupport.HistoryEntryWrapper<HistoryEntry> lookupEntry(Node node) {
                    VCSHistoryProvider.HistoryEntry he = node.getLookup().lookup(VCSHistoryProvider.HistoryEntry.class);
                    return he != null ? new HistoryEntryImpl(he) : null;
                }
                @Override
                public Lookup getContext() {
                    return HistoryAction.this.getContext();
                }

                @Override
                public boolean isMultipleHistory() {
                    return HistoryAction.this.isMultipleHistory();
                }
            };
        }
        return callback;
    }

    private class HistoryEntryImpl implements HistoryActionSupport.HistoryEntryWrapper<VCSHistoryProvider.HistoryEntry> {
        private final VCSHistoryProvider.HistoryEntry he;
        public HistoryEntryImpl(VCSHistoryProvider.HistoryEntry he) {
            this.he = he;
        }
        @Override
        public VCSHistoryProvider.HistoryEntry getHistoryEntry() {
            return he;
        }
        @Override
        public String getRevisionShort() {
            return he.getRevisionShort();
        }
        @Override
        public Date getDateTime() {
            return he.getDateTime();
        }
        @Override
        public String getRevision() {
            return he.getRevision();
        }        
    }
    
}
