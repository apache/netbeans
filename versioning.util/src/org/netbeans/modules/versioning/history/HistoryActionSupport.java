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
