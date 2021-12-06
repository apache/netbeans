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
import org.netbeans.modules.versionvault.client.ClearcaseClient;
import org.netbeans.modules.versionvault.client.status.FileEntry;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level; 
import java.util.regex.Pattern;
import javax.swing.JButton;
import org.netbeans.api.queries.SharabilityQuery;
import org.netbeans.modules.versionvault.util.ClearcaseUtils;
import org.netbeans.modules.versionvault.util.ProgressSupport;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;
import org.netbeans.modules.versioning.util.FileUtils;
import org.netbeans.modules.versioning.util.ListenersSupport;
import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.VersioningListener;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Central part of status management, deduces and caches statuses of files under version control.
 *
 * @author Maros Sandor
 */
public class FileStatusCache {
    
    /**
     * Indicates that status of a file changed and listeners SHOULD check new status
     * values if they are interested in this file.
     * The New value is a ChangedEvent object (old FileInformation object may be null)
     */
    public static final String EVENT_FILE_STATUS_CHANGED = "status.changed";        
    
    // Constant FileInformation objects that can be safely reused
    // Files that have a revision number cannot share FileInformation objects
    private static final FileInformation FILE_INFORMATION_IGNORED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_IGNORED, false);
    private static final FileInformation FILE_INFORMATION_EXCLUDED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_IGNORED, true);    
    private static final FileInformation FILE_INFORMATION_NOTMANAGED = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, false);
    private static final FileInformation FILE_INFORMATION_NOTMANAGED_DIRECTORY = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NOTMANAGED, true);
    public static final FileInformation FILE_INFORMATION_UNKNOWN = new FileInformation(FileInformation.STATUS_UNKNOWN, false);
    
    private ListenersSupport listenerSupport = new ListenersSupport(this);

    private Map<File, Map<File, FileInformation>> statusMap = new HashMap<File, Map<File, FileInformation>>();
    
    private Clearcase clearcase;
    
    private RequestProcessor rp;
    
    private Set<File> filesToRefresh = new HashSet<File>();
    private RequestProcessor.Task filesToRefreshTask;

    private static final Pattern ignorePattern = Pattern.compile(".*\\.(keep|contrib)(\\.\\d+)?");
    private final ClearcaseClient client;
    
    FileStatusCache() {
        this.clearcase = Clearcase.getInstance();        
        client = new ClearcaseClient();
    }
    
    // --- Public interface -------------------------------------------------

    public void addVersioningListener(VersioningListener listener) {
        listenerSupport.addListener(listener);
    }

    public void removeVersioningListener(VersioningListener listener) {
        listenerSupport.removeListener(listener);
    }
    
    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.     
     * <p>This method returns both folders and files.
     *
     * @param context context to examine. If null all files applying to the given status will be returned
     * @param includeStatus limit returned files to those having one of supplied statuses
     * @return File [] array of interesting files
     */
    public synchronized File [] listFiles(VCSContext context, int includeStatus) {
        return listFiles(context, includeStatus, false);
    }

    /**
     * Lists <b>interesting files</b> that are known to be inside given folders.
     * <p>This method returns both folders and files.
     *
     * @param context context to examine. If null all files applying to the given status will be returned
     * @param includeStatus limit returned files to those having one of supplied statuses (if exactStatusConformity is false)
     * @param exactStatusConformity if true only files with an equal status as includeStatus will be returned, otherwise bitwise comparison is used
     * @return File [] array of interesting files
     */
    public synchronized File [] listFiles(VCSContext context, int includeStatus, boolean exactIncludeStatusConformity) {
        Set<File> set = new HashSet<File>();        
        
        // XXX this is crap. check for files from context
        for(Entry<File, Map<File, FileInformation>> entry : statusMap.entrySet()) {
            
            Map<File, FileInformation> map = entry.getValue();    
            for (Iterator i = map.keySet().iterator(); i.hasNext();) {                
                File file = (File) i.next();                                   
                FileInformation info = (FileInformation) map.get(file);
                if (!exactIncludeStatusConformity && (info.getStatus() & includeStatus) == 0
                        || exactIncludeStatusConformity && info.getStatus() != includeStatus) {
                    continue;
                }
                
                if(context != null) {
                    for (File root : context.getRootFiles()) {
                        if (VersioningSupport.isFlat(root)) {
                            if (file.equals(root) || file.getParentFile().equals(root)) {
                                set.add(file);
                                break;
                            }
                        } else {
                            if (Utils.isAncestorOrEqual(root, file)) {
                                set.add(file);
                                break;
                            }   
                        }
                    }
                } else {
                    set.add(file);
                }
            }
        }
        
        if (context != null && context.getExclusions().size() > 0) {
            for (Iterator i = context.getExclusions().iterator(); i.hasNext();) {
                File excluded = (File) i.next();
                for (Iterator j = set.iterator(); j.hasNext();) {
                    File file = (File) j.next();
                    if (Utils.isAncestorOrEqual(excluded, file)) {
                        j.remove();
                    }
                }
            }
        }
        return set.toArray(new File[set.size()]);
    }
    
    /**
     * Returns the versionig status for a file as long it is already stored in the cache or {@link #FILE_INFORMATION_UNKNOWN}. 
     * If refreshUnknown true and no status value is available at the moment a status refresh for the given file is triggered 
     * asynchronously and subsequently status change events will be fired to notify all registered listeners.
     * 
     * @param file file to get status for
     * @param refreshUnknown if true and no status value is stored in the cache the {@link #refresh} is called asynchronouslly
     * @return FileInformation structure containing the file status or {@link #FILE_INFORMATION_UNKNOWN} if there is no staus known yet
     * @see FileInformation 
     * @see #refreshAsync
     */
    public FileInformation getCachedInfo(final File file) {
        File dir = file.getParentFile();
        
        if (dir == null) {
            return FILE_INFORMATION_NOTMANAGED; // default for filesystem roots
        }                         
        
        Map<File, FileInformation> dirMap = get(dir);         
        FileInformation info = dirMap != null ? dirMap.get(file) : null;
        if(info == null) {
            if(!Clearcase.getInstance().isManaged(file)) {
                // TODO what about children if dir?
                return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
            }     
        }        
        return info;
    }
    
    /**
     * Determines the versioning status information for a file.
     * This method synchronously accesses disk and may block for a long period of time.
     *
     * @param file file to get the {@link FileInformation} for
     * @return FileInformation structure containing the file status
     * @see FileInformation
     */
    public FileInformation getInfo(File file) {      
        FileInformation fi = getCachedInfo(file);
        if(fi == null) {
            fi = refresh(file, false); 
        }        
        return fi;               
    }
        
    /**
     * Asynchronously refreshes the status for the given files.
     * Status change events will be fired to notify all registered listeners.
     * 
     * @param files
     */
    public void refreshLater(File ...files) {
        synchronized(filesToRefresh) {
            for (File file : files) {
                if(file == null) continue;
                filesToRefresh.add(file);                
            }
        }
        getFilesToRefreshTask().schedule(200); 
    }    
    
    // --- Package private contract ------------------------------------------
        
    /**
     * XXX now whats the difference to listFiles ?
     * @param root
     * @return
     */
    synchronized Map<File, FileInformation> getAllModifiedValues(File root) {  // XXX add recursive flag
        Map<File, FileInformation> ret = new HashMap<File, FileInformation>();
        
        for(File modifiedDir : statusMap.keySet()) {
            if(Utils.isAncestorOrEqual(root, modifiedDir)) {                
                Map<File, FileInformation> map = get(modifiedDir);
                for(File file : map.keySet()) {
                    FileInformation info = map.get(file);

                    if( (info.getStatus() & FileInformation.STATUS_LOCAL_CHANGE) != 0 ) { // XXX anything else?
                        ret.put(file, info);
                    }                
                }                
            }            
        }        
        return ret;
    }
        
    /**
     * Refreshes the status value for the given file, all its siblings and its parent folder. 
     * Status change events will be eventually thrown for the file, all its siblings and its parent folder. 
     * 
     * @param file the file to be refreshed
     * @param forceChangeEvent if true status change event will fired even if 
     *                         the newly retrieved status value for a file is the same as the already cached one
     * @return FileInformation
     * @see #ChangedEvent
     */
    private FileInformation refresh(File file, boolean forceChangeEvent) { 
        
        // check if it is a managed directory structure
        File dir = file.getParentFile();
        if (dir == null) {
            return FileStatusCache.FILE_INFORMATION_NOTMANAGED; // default for filesystem roots
        }
        
        if(!Clearcase.getInstance().isManaged(file)) {
            // TODO what about children if dir?
            return file.isDirectory() ? FILE_INFORMATION_NOTMANAGED_DIRECTORY : FILE_INFORMATION_NOTMANAGED;
        }     

        boolean isRoot;
        List<FileEntry> statusValues;                
        Map<File, FileInformation> oldDirMap = get(dir); // get the old values before you read the new ones
        if(!Clearcase.getInstance().isManaged(dir)) {                        
            // file seems to be the vob root            
            isRoot = true;            
            statusValues = ClearcaseUtils.readEntries(client, file, true);
        } else {
            isRoot = false;
            statusValues = ClearcaseUtils.readEntries(client, dir, false);
        }              
                
        Map<File, FileInformation> newDirMap;        
        if(!isRoot || oldDirMap == null) {
            newDirMap = new HashMap<File, FileInformation>();            
        } else {
            // XXX what if vob gets deleted?
            newDirMap = oldDirMap;  // XXX do we need this?
        }
                 
        for(FileEntry fs : statusValues) {            
            FileInformation fiNew = createFileInformation(fs);
            try {
                newDirMap.put(normalizeFile(fs.getFile()), fiNew);
            } catch (IOException ioe) {
                Clearcase.LOG.log(Level.SEVERE, null, ioe);
            }
        }       
        
        put(dir, newDirMap);
        FileInformation fi = null;        
        try {
            fi = newDirMap.get(normalizeFile(file));
        } catch (IOException ioe) {
            Clearcase.LOG.log(Level.SEVERE, null, ioe);
        }

        if(fi == null) {                        
            // e.g. when triggered by interceptor.delete()
            fi = FILE_INFORMATION_UNKNOWN;            
        }         
            
        fireStatusEvents(newDirMap, oldDirMap, forceChangeEvent);                
        return fi;                
    }    
    
    /**
     * Examines a file or folder and computes its status. 
     * 
     * @param status entry for this file or null if the file is unknown to subversion
     * @return FileInformation file/folder status bean
     */ 
    private FileInformation createFileInformation(FileEntry entry) { 
        FileInformation info;
        
        if(entry.isViewPrivate()) {
            if(isIgnored(entry.getFile())) {
                info = FILE_INFORMATION_IGNORED; // XXX what if file does not exists -> isDir = false;   
            } else {
                info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_NEWLOCALLY, entry, entry.getFile().isDirectory()); 
            }            
        } else if(entry.isCheckedout()) {
            if(entry.isRemoved()) {
                
                // XXX we don't know if directory but could be retrieved from ct ls -long or ct describe 
                
                if(entry.isReserved()) {
                    info = new FileInformation(FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED, entry, entry.getFile().isDirectory());    
                } else {
                    info = new FileInformation(FileInformation.STATUS_VERSIONED_CHECKEDOUT_BUT_REMOVED | FileInformation.STATUS_UNRESERVED, entry, entry.getFile().isDirectory());            
                }                   
            } else {                
                if(entry.isReserved()) {
                    info = new FileInformation(FileInformation.STATUS_VERSIONED_CHECKEDOUT, entry, entry.getFile().isDirectory());    
                } else {
                    info = new FileInformation(FileInformation.STATUS_VERSIONED_CHECKEDOUT | FileInformation.STATUS_UNRESERVED, entry, entry.getFile().isDirectory());            
                }   
            }        
        } else if(entry.isLoadedButMissing()) {
            // XXX we don't know if directory but could be retrieved from ct ls -long or ct describe 
            info = new FileInformation(FileInformation.STATUS_VERSIONED_LOADED_BUT_MISSING, entry, entry.getFile().isDirectory());              
        } else if(entry.isHijacked()) {
            info = new FileInformation(FileInformation.STATUS_VERSIONED_HIJACKED, entry, entry.getFile().isDirectory());        
        } else if(entry.isEclipsed()) {
            info = new FileInformation(FileInformation.STATUS_NOTVERSIONED_ECLIPSED, entry, entry.getFile().isDirectory());
        } else if(entry.getVersion() != null) {
            // has predecesor (is versioned) and no other status value known ...
            info = new FileInformation(FileInformation.STATUS_VERSIONED_UPTODATE, entry, entry.getFile().isDirectory());
        } else {
            info = new FileInformation(FileInformation.STATUS_UNKNOWN, entry, entry.getFile().isDirectory());   
        }        
        
        Clearcase.LOG.finer("createFileInformation " + entry + " : " + info);
        return info;
        
    }
    
    /**
     * Non-recursive ignore check.
     *
     * <p>Side effect: if versioned by CC and ignored then also stores the is ignored information
     *
     * @return true if file is listed in parent's ignore list
     * or IDE thinks it should be.
     */
    private boolean isIgnored(final File file) {        
        if (!ClearcaseModuleConfig.getAddViewPrivate()) {
            return true;
        }
        if(ClearcaseModuleConfig.isIgnored(file)) {
            return true;
        }
        
        if( ignorePattern.matcher(file.getName()).matches()) {
            return true;
        }
        
        if (SharabilityQuery.getSharability(file) == SharabilityQuery.NOT_SHARABLE) {
            // BEWARE: In NetBeans VISIBILTY == SHARABILITY                                 
            ClearcaseModuleConfig.setIgnored(file);
            return true;
        } else {
            return false;
        }
    }            
            
    private RequestProcessor.Task getFilesToRefreshTask() {
        if(filesToRefreshTask == null) {
            filesToRefreshTask = getRequestProcessor().create(new Runnable() {
                public void run() {
                    File[] files;
                    synchronized(filesToRefresh) {
                        files = filesToRefresh.toArray(new File[filesToRefresh.size()]);
                        filesToRefresh.clear();
                    }                        
                    new RefreshSupport(getRequestProcessor(), false, true, files).refresh();
                }
            });
        }
        return filesToRefreshTask;
    }   

    private File normalizeFile(File file) throws IOException {
        return FileUtil.normalizeFile(file);
    }

    private synchronized void put(File dir, Map<File, FileInformation> newDirMap) {
        statusMap.put(dir, newDirMap);
    }

    private synchronized Map<File, FileInformation> get(File dir) {
        return statusMap.get(dir);
    }
    
    public static class ChangedEvent {
        
        private File file;
        private FileInformation oldInfo;
        private FileInformation newInfo;
        
        public ChangedEvent(File file, FileInformation oldInfo, FileInformation newInfo) {
            this.file = file;
            this.oldInfo = oldInfo;
            this.newInfo = newInfo;
        }
        
        public File getFile() {
            return file;
        }
        
        public FileInformation getOldInfo() {
            return oldInfo;
        }
        
        public FileInformation getNewInfo() {
            return newInfo;
        }
    }    

    private void fireStatusEvents(Map<File, FileInformation> newDirMap, Map<File, FileInformation> oldDirMap, boolean force) {
        for (File file : newDirMap.keySet()) { 
            FileInformation newInfo;
            FileInformation oldInfo;
            try {
                newInfo = newDirMap.get(normalizeFile(file));
                oldInfo = oldDirMap != null ? oldDirMap.get(normalizeFile(file)) : null;
                fireFileStatusChanged(file, oldInfo, newInfo, force);
            } catch (IOException ex) {
                Clearcase.LOG.log(Level.SEVERE, null, ex);
            }            
        }
        if(oldDirMap == null) {
            return;
        }
        for (File file : oldDirMap.keySet()) { 
            FileInformation newInfo = newDirMap.get(file);
            if(newInfo == null) {
                FileInformation oldInfo;
                try {
                    oldInfo = oldDirMap.get(normalizeFile(file));
                    fireFileStatusChanged(file, oldInfo, newInfo, force);    
                } catch (IOException ex) {
                    Clearcase.LOG.log(Level.SEVERE, null, ex);
                }                                        
            }
        }                                        
    }
    
    private void fireFileStatusChanged(File file, FileInformation oldInfo, FileInformation newInfo, boolean force) {        
        force = false;
        if(!force) {            
           if (oldInfo == null && newInfo == null) {
               return;
           }
           if(oldInfo != null && newInfo != null && oldInfo.getStatus() == newInfo.getStatus()) {
                return;
           } 
        }                              
        listenerSupport.fireVersioningEvent(EVENT_FILE_STATUS_CHANGED, new Object [] { file, oldInfo, newInfo != null ? newInfo : FILE_INFORMATION_UNKNOWN });                
    }    
    
    private RequestProcessor getRequestProcessor() {        
        if(rp == null) {
           rp = new RequestProcessor("ClearCase - FileStatusCache");    
        }        
        return rp;
    }

    public static class RefreshSupport extends ProgressSupport {
        private final boolean recursivelly;
        private final boolean fireEvents;
        private File[] files;

        FileStatusCache cache = Clearcase.getInstance().getFileStatusCache();

        public RefreshSupport(RequestProcessor rp, VCSContext ctx, String displayName) {                                
            super(rp, displayName); 
            this.recursivelly = true;
            this.fireEvents = false;
            this.files = getRootFiles(ctx);
        }
        
        public RefreshSupport(RequestProcessor rp, VCSContext ctx, String displayName, JButton cancel) {                                
            super(rp, displayName, cancel); 
            this.recursivelly = true;
            this.fireEvents = false;
            this.files = getRootFiles(ctx);
        }

        public RefreshSupport(RequestProcessor rp, boolean recursivelly, boolean fireEvents, File[] files) {
            super(rp, NbBundle.getMessage(FileStatusCache.class, "Progress_RefreshingStatus")); 
            this.recursivelly = recursivelly;
            this.fireEvents = fireEvents;
            this.files = files;
        }

        public void setRootFiles(File[] files) {
            this.files = files;
        }        
        
        private File[] getRootFiles(VCSContext ctx) {
            Set<File> roots = ctx.getRootFiles();
            return roots.toArray(new File[roots.size()]);
        }

        @Override
        protected void perform() {
            // do nothing
        }        
        
        protected void refresh() {            
            Set<File> parents = new HashSet<File>();
            for (File file : files) {
                File parent = file.getParentFile();
                if (recursivelly && file.isDirectory()) {
                    refreshRecursively(file, fireEvents, this, cache);
                } else {
                    if (!parents.contains(parent)) {
                        // refresh the file, all its siblings and the parent (dir)
                        cache.refresh(file, true);
                        // all following kids from this files parent should be be skipped
                        // as they were already refreshed
                        parents.add(parent);
                    }
                }
            }
        }
        
    }

    /**
     * Refreshes recursively all files in the given directory.
     * @param dir
     */
    public static void refreshRecursively(File dir, boolean fireEvents, ProgressSupport support, FileStatusCache cache) {
        File[] dirFiles = dir.listFiles();
        if(dirFiles == null || dirFiles.length == 0) {
            return;
        }
        boolean kidsRefreshed = false;
        for(File file : dirFiles) {
            if(support != null && support.isCanceled()) {
                return;
            }
            if(!kidsRefreshed) {
                // refresh the file, all its siblings and the parent (dir)
                cache.refresh(file, fireEvents);
                // files parent directory (dir) and all it's children are refreshed
                // so skip for the next child
                kidsRefreshed = true;
            }
            if (file.isDirectory()) {
                refreshRecursively(file, fireEvents, support, cache);
            }
        }
    }
    
}
