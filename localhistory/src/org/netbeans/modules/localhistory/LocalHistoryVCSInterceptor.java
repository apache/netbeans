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
package org.netbeans.modules.localhistory;

import java.util.HashMap;
import java.io.File;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.localhistory.store.LocalHistoryStore;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSInterceptor;
import org.openide.util.Exceptions;

/**
 *
 * Listens to file system operations from the IDE and eventually handles them synchronously
 *
 * @author Tomas Stupka
 */
class LocalHistoryVCSInterceptor extends VCSInterceptor {

    static final Logger LOG = Logger.getLogger(LocalHistoryVCSInterceptor.class.getName());

    private class StorageMoveHandler {
        private long ts = -1;

        private final VCSFileProxy from;
        private final VCSFileProxy to;

        StorageMoveHandler(VCSFileProxy from, VCSFileProxy to) {
            this.from = from;
            this.to = to;
        }

        public void delete() {
            getStore().fileDeleteFromMove(from, to, ts);
        }

        public void create() {
            ts = to.lastModified();
            getStore().fileCreateFromMove(from, to, ts);
        }
    }; 

    private LocalHistoryStore getStore() {
        return LocalHistory.getInstance().getLocalHistoryStore();
    }

    private Map<String, StorageMoveHandler> moveHandlerMap;

    // XXX reconsider this. is there realy no other way? is it robust enough?
    private Set<VCSFileProxy> toBeDeleted = new HashSet<VCSFileProxy>();
    private Set<VCSFileProxy> toBeCreated = new HashSet<VCSFileProxy>();
    private Set<VCSFileProxy> wasJustCreated = new HashSet<VCSFileProxy>();

    /** Creates a new instance of LocalHistoryVCSInterceptor */
    public LocalHistoryVCSInterceptor() {
        
    }

    // ==================================================================================================
    // DELETE
    // ==================================================================================================
    
    @Override
    public boolean beforeDelete(VCSFileProxy file) {
        LOG.log(Level.FINE, "beforeDelete {0}", file); // NOI18N
        if(!accept(file)) {
            return false;
        }
        toBeDeleted.add(file); // XXX do this with a hanlder, get the correct ts
        getStore().waitForProcessedStoring(file, "beforeDelete"); // NOI18N
        
        return false;
    }

    @Override
    public void afterDelete(VCSFileProxy file) {
        LOG.log(Level.FINE, "afterDelete {0}", file); // NOI18N
        if(!accept(file)) {
            return;
        } 
        if(!toBeDeleted.remove(file)) {
            // do nothing if the file wasn't marked
            // as to be deleted
            return;
        }

        String key = FileUtils.getPath(file);
        if(getMoveHandlerMap().containsKey(key)) {
            StorageMoveHandler handler = getMoveHandlerMap().get(key);
            try {
                handler.delete();
            } finally {
                getMoveHandlerMap().remove(key);
            }
        } else {
            getStore().fileDelete(file, System.currentTimeMillis());
        }
    }

    // ==================================================================================================
    // MOVE
    // ==================================================================================================
    
    @Override
    public boolean beforeMove(final VCSFileProxy from, final VCSFileProxy to) {
        LOG.log(Level.FINE, "beforeMove {0} to {1}", new Object[] {from, to}); // NOI18N
        if(!accept(from)) {
            return false;
        }
        getStore().waitForProcessedStoring(from, "beforeMove"); // NOI18N
        // moving a package comes either like
        // - create(to) and delete(from)
        // - or the files from the package come like move(from, to)
        StorageMoveHandler handler = new StorageMoveHandler(from, to);
        getMoveHandlerMap().put(FileUtils.getPath(to), handler);
        getMoveHandlerMap().put(FileUtils.getPath(from), handler);
        return false;
    }

    @Override
    public void afterMove(VCSFileProxy from, VCSFileProxy to) {
        LOG.log(Level.FINE, "afterMove {0} to {1}", new Object[] {from, to}); // NOI18N
        if(!accept(from)) {
            return;
        } 
        String key = FileUtils.getPath(to);
        if(getMoveHandlerMap().containsKey(key)) {
            StorageMoveHandler handler = getMoveHandlerMap().get(key);
            try {
                handler.create();
                handler.delete();
            } finally {
                getMoveHandlerMap().remove(key);
                getMoveHandlerMap().remove(FileUtils.getPath(from));
            }
        }
    }

    // ==================================================================================================
    // COPY
    // ==================================================================================================
    
    @Override
    public boolean beforeCopy(VCSFileProxy from, VCSFileProxy to) {
        getStore().waitForProcessedStoring(from, "beforeCopy"); // NOI18N
        return super.beforeCopy(from, to); 
    }
    
    // ==================================================================================================
    // CREATE
    // ==================================================================================================

    @Override
    public boolean beforeCreate(VCSFileProxy file, boolean isDirectory) {
        LOG.log(Level.FINE, "beforeCreate {0}", file); // NOI18N
        if(!accept(file)) {
            return false;
        } 
        toBeCreated.add(file);
        getStore().waitForProcessedStoring(file, "beforeCreate"); // NOI18N
        return false;
    }

    @Override
    public void afterCreate(VCSFileProxy file) {
        LOG.log(Level.FINE, "afterCreate {0}", file); // NOI18N
        if(!accept(file)) {
            return;
        } 
        if(LocalHistory.getInstance().isManagedByParent(file) == null) {
            // XXX: the VCS interceptor doesn't filter afterCreate because
            // of a workaround for caching problems in other VCS systems. 
            // For now this must be done here ...
            return;
        }
        LocalHistory.getInstance().fireFileEvent(LocalHistory.EVENT_FILE_CREATED, file);
        toBeCreated.remove(file);
        if(file.isFile()) {
            // no change events for folders seen yet
            wasJustCreated.add(file);
        }

        String key = FileUtils.getPath(file);
        if(getMoveHandlerMap().containsKey(key)) {                                
            StorageMoveHandler handler = getMoveHandlerMap().get(key);
            try {
                handler.create();
            } finally {
                getMoveHandlerMap().remove(key);
            }
        }
    }

    // ==================================================================================================
    // CHANGE
    // ==================================================================================================
    
    @Override
    public void beforeChange(VCSFileProxy file) {
        LOG.log(Level.FINE, "beforeChange {0}", file); // NOI18N
        if(toBeCreated.contains(file) || 
           wasJustCreated.remove(file)) 
        {
            // ignore change events
            // if they happen in scope of a create
            // or just after a create
            return;
        }
        if(!accept(file)) {
            return;
        } 
        getStore().waitForProcessedStoring(file, "beforeChange"); // NOI18N
    }

    @Override
    public void afterChange(VCSFileProxy file) {
        LOG.log(Level.FINE, "afterChange {0}", file); // NOI18N
        // just in case
        wasJustCreated.remove(file);
        if(!accept(file)) {
            return;
        } 
        LocalHistory.getInstance().touch(file);
    }

    @Override
    public void beforeEdit(VCSFileProxy file) {
        LOG.log(Level.FINE, "beforeEdit {0}", file); // NOI18N
        if(!accept(file)) {
            return;
        } 
        getStore().fileChange(file);
    }
    
    private Map<String, StorageMoveHandler> getMoveHandlerMap() {
        if(moveHandlerMap == null) {
            moveHandlerMap = new HashMap<String, StorageMoveHandler>();
        }
        return moveHandlerMap;
    }

    /**
     *
     * Decides if a file has to be stored in the Local History or not.
     *
     * @param file the file to be stored
     * @return true if the file has to be stored in the Local History, otherwise false
     */
    private boolean accept(VCSFileProxy file) {
        if(!LocalHistory.getInstance().isManaged(file)) {
            return false;
        }
        return true;
    }

}
