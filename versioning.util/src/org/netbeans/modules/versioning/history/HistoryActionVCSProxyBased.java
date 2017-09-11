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
package org.netbeans.modules.versioning.history;

import java.util.Date;
import java.util.Set;
import javax.swing.Action;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider;
import org.netbeans.modules.versioning.core.spi.VCSHistoryProvider.HistoryEntry;
import org.netbeans.modules.versioning.history.HistoryActionSupport.Callback;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 * Base class for actions returned via {@link HistoryEntry#getActions()}.<br/>
 * Should be used by Versionig systems which depend on the io.File based o.n.m.versioning.spi
 * 
 * @author Tomas Stupka
 */
public abstract class HistoryActionVCSProxyBased extends NodeAction {

    
    private final HistoryActionSupport<VCSHistoryProvider.HistoryEntry> support; 
    private final String name;
    private final boolean multipleHistory;
    private Lookup context;
    
    public HistoryActionVCSProxyBased() {
        this(null, true);
    }
    public HistoryActionVCSProxyBased(String name) {
        this(name, true);
    }
    public HistoryActionVCSProxyBased(String name, boolean multipleHistory) {
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
    protected abstract void perform(VCSHistoryProvider.HistoryEntry entry, Set<VCSFileProxy> files);

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
    
    protected Callback<HistoryEntry> getCallback() {
        return new Callback<HistoryEntry>() {
            @Override
            public void call(HistoryEntry entry, Set<VCSFileProxy> files) {
                perform(entry, files);
            }
            @Override
            public HistoryActionSupport.HistoryEntryWrapper<HistoryEntry> lookupEntry(Node node) {
                VCSHistoryProvider.HistoryEntry he = node.getLookup().lookup(VCSHistoryProvider.HistoryEntry.class);
                return he != null ? new HistoryEntryImpl(he) : null;
            }
            @Override
            public Lookup getContext() {
                return HistoryActionVCSProxyBased.this.getContext();
            }
            @Override
            public boolean isMultipleHistory() {
                return HistoryActionVCSProxyBased.this.isMultipleHistory();
            }
        };
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
