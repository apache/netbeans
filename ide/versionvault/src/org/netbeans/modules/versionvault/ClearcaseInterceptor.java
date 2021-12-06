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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault;

import org.netbeans.modules.versionvault.*;
import org.netbeans.modules.versioning.spi.VCSInterceptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.modules.versionvault.client.ClearcaseClient;
import org.netbeans.modules.versionvault.client.ClearcaseCommand;
import org.netbeans.modules.versionvault.client.DeleteCommand;
import org.netbeans.modules.versionvault.client.MkElemCommand;
import org.netbeans.modules.versionvault.client.MoveCommand;
import org.netbeans.modules.versionvault.client.UnCheckoutCommand;
import org.netbeans.modules.versionvault.client.status.FileEntry;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versioning.util.Utils;

/**
 * Listens on file system changes and reacts appropriately, mainly refreshing affected files' status.
 * 
 * @author Maros Sandor
 */
public class ClearcaseInterceptor extends VCSInterceptor {

    private final FileStatusCache cache;
    private final ClearcaseClient client;
    
    
    public ClearcaseInterceptor() {
        cache = Clearcase.getInstance().getFileStatusCache();
        client = new ClearcaseClient();        
    }

    @Override
    public boolean beforeDelete(File file) {                
        Clearcase.LOG.finer("beforeDelete " + file);        
                
        // let the IDE take care for deletes of unversioned files        
        FileEntry entry = ClearcaseUtils.readEntry(client, file);       
        return entry != null && !entry.isViewPrivate();            
    }

    @Override
    public void doDelete(final File file) throws IOException {
        Clearcase.LOG.finer("doDelete " + file);        
        fileDeletedImpl(file);
    }

    @Override
    public void afterDelete(final File file) {
        Clearcase.LOG.finer("afterDelete " + file);
        ClearcaseUtils.afterCommandRefresh(new File[] { file }, false, false);
    }

    private void deleteFile(File file) {
        File parent = file.getParentFile();
        
        // 1. checkout parent if needed
        ClearcaseUtils.ensureMutable(client, parent);

        // 2. uncheckout - even if the delete is invoked with the --force switch
        // ct rm on a file which was checkedout causes that after ct unco on its parent
        // it becomes [checkedout but removed]. This actually is not what we want.
        FileEntry entry = ClearcaseUtils.readEntry(client, file);
        if (entry != null && entry.isCheckedout()) {
            exec(new UnCheckoutCommand(new File[]{ file }, false),false);
        }

        // 3. remove the file
        exec(new DeleteCommand(new File[]{ file }),false);
                
        // XXX the file stays on the filessytem if it was checkedout eventually
        if (file.exists()) {
            file.delete();
        }        
    }
    
    private void fileDeletedImpl(File file) {       
        File parent = file.getParentFile();
        if(parent == null) {
            // how is this possible ?
            return;
        }                        
        if(Clearcase.getInstance().isManaged(parent)) {
            deleteFile(file);            
        }                                 
    }    

    @Override
    public boolean beforeMove(File from, File to) {
        Clearcase.LOG.finer("beforeMove " + from + " " + to);
        if(!Clearcase.getInstance().isManaged(from)) {
            // let the IDE take care for move of unversioned files
            return false;
        }
        
        // let the IDE take care for view private files - they are defacto unversioned
        FileEntry entry = ClearcaseUtils.readEntry(client, from);
        return entry != null && !entry.isViewPrivate();
    }

    @Override
    public void doMove(File from, File to) throws IOException {
        Clearcase.LOG.finer("doMove " + from + " " + to);
        fileMovedImpl(from, to);                
    }

    @Override
    public void afterMove(final File from, final File to) {
        Clearcase.LOG.finer("afterMove " + from + " " + to);
    }

    private void fileMovedImpl(File from, File to) {        
        File fromParent = from.getParentFile();
        File toParent = to.getParentFile();
        if(fromParent == null || toParent == null) {
            // how is this possible ?
            return;
        }
                
        List<File> refreshFiles = ClearcaseUtils.getFilesTree(from); // all children under from have to be explicitly refreshed
        refreshFiles.add(from);
        refreshFiles.add(to);
        
        if(Clearcase.getInstance().isManaged(from) && Clearcase.getInstance().isManaged(to)) {
            
            FileEntry fromEntry = ClearcaseUtils.readEntry(client, from);                
            
            if(fromEntry.isViewPrivate()) { // XXX HIJACKED?
                // 'from' is not versioned yet - let's just rename it
                from.renameTo(to);
            } else {
                
                // 1. add parents if needed
                List<File> newParents = getViewPrivateParents(toParent);
                if(newParents.size() > 0) {
                    Collections.sort(newParents);        
                    exec(new MkElemCommand(newParents.toArray(new File[newParents.size()]), null, MkElemCommand.Checkout.Default, false),false);                                        
                }
                
                // 2. checkout parents if needed                
                ClearcaseUtils.ensureMutable(client, fromParent);                                
                if(!fromParent.equals(toParent)) {
                    ClearcaseUtils.ensureMutable(client, toParent);
                }    
            
                // 3. move the file
                exec(new MoveCommand(from, to),false);               
                
            }    
        } else if (!Clearcase.getInstance().isManaged(from)) {
            // 'from' is not versioned yet - let's just rename it
            from.renameTo(to);                            
        } else { // !Clearcase.getInstance().isManaged(to)
            FileEntry fromEntry = ClearcaseUtils.readEntry(client, fromParent);                
            if (fromEntry.isViewPrivate()) {
                // 'from' is not versiomed yet - let's just rename it
                from.renameTo(to);                                            
            } else {
                try {
                    // XXX what if not file???
                    // 1. checkout parents if needed
                    Utils.copyStreamsCloseAll(new FileOutputStream(to), new FileInputStream(from));
                } catch (IOException ex) {
                    Clearcase.LOG.log(Level.SEVERE, null, ex);
                }
                deleteFile(from);
            }
        }            
        ClearcaseUtils.afterCommandRefresh(refreshFiles.toArray(new File[refreshFiles.size()]), true, false);
    }
    
    @Override
    public boolean beforeCreate(File file, boolean isDirectory) {
        Clearcase.LOG.finer("beforeCreate " + file);        
        return false;
    }

    @Override
    public void doCreate(File file, boolean isDirectory) throws IOException {
        Clearcase.LOG.finer("doCreate " + file);                
        // do nothing
    }

    @Override
    public void afterCreate(final File file) {
        Clearcase.LOG.finer("afterCreate " + file);
        cache.refreshLater(file);                 
    }
    
    @Override
    public void afterChange(final File file) {
        Clearcase.LOG.finer("afterChange " + file);        
        Clearcase.getInstance().getRequestProcessor().post(new Runnable() {
            public void run() {
                fileChangedImpl(file);
            }
        });
    }

    private void fileChangedImpl(File file) {
        cache.refreshLater(file);
    }

    @Override
    public void beforeEdit(File file) {
        Clearcase.LOG.finer("beforeEdit " + file);
        FileEntry entry = ClearcaseUtils.readEntry(client, file);
        if(entry == null || entry.isViewPrivate()) return;
        ClearcaseUtils.ensureMutable(client, file, entry);
    }

    @Override
    public boolean isMutable(File file) {
        return true;
    }

    @Override
    public Object getAttribute(final File file, String attrName) {
        if("ProvidedExtensions.Refresh".equals(attrName)) {
            return new Runnable() {
                public void run() {
                    FileStatusCache.refreshRecursively(file, true, null, cache);
                }
            };
        } else {
            return super.getAttribute(file, attrName);
        }
    }

    private void exec(ClearcaseCommand command, boolean notifyErrors) {        
        Clearcase.getInstance().getClient().exec(command, notifyErrors);
    }

    private List<File> getViewPrivateParents(File parent) {
        List<File> ret = new ArrayList<File>();
        if(parent == null) {
            return ret;
        }
        FileEntry parentEntry = ClearcaseUtils.readEntry(client, parent);                        
        if (parentEntry.isViewPrivate()) {
            ret.add(parent);
            parent = parent.getParentFile();
            if(parent != null) {
                ret.addAll(getViewPrivateParents(parent));
            }
        } 
        return ret;
    }
}
