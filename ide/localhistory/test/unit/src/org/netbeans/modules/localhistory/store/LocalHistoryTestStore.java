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
package org.netbeans.modules.localhistory.store;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.VersioningListener;

/**
 *
 * @author tomas
 */
public class LocalHistoryTestStore implements LocalHistoryStore {

    private final LocalHistoryStore store;        
    private Method getStoreFolderMethod;
    private Method getDataFileMethod;
    private Method getHistoryFileMethod;
    private Method getLabelsFileMethod;
    private Method getStoreFileMethod;
    private Method cleanUpImplMethod;
    private Field lockedFolders;
    
    public LocalHistoryTestStore(String storePath) {      
        store = LocalHistoryStoreFactory.getInstance().createLocalHistoryStorage();
    }

    @Override
    public StoreEntry setLabel(VCSFileProxy file, long ts, String label) {
        return store.setLabel(file, ts, label);
    }

    public void removeVersioningListener(VersioningListener l) {
        store.removeVersioningListener(l);
    }

    public void addVersioningListener(VersioningListener l) {
        store.addVersioningListener(l);    
    }
    
    public org.netbeans.modules.localhistory.store.StoreEntry getStoreEntry(VCSFileProxy file, long ts) {
        return store.getStoreEntry(file, ts);
    }

    public org.netbeans.modules.localhistory.store.StoreEntry[] getStoreEntries(VCSFileProxy file) {
        return store.getStoreEntries(file);
    }

    public org.netbeans.modules.localhistory.store.StoreEntry[] getFolderState(VCSFileProxy root, VCSFileProxy[] files, long ts) {
        return store.getFolderState(root, files, ts);
    }

    public org.netbeans.modules.localhistory.store.StoreEntry[] getDeletedFiles(VCSFileProxy root) {
        return store.getDeletedFiles(root);
    }

    public void fileDeleteFromMove(VCSFileProxy from, VCSFileProxy to, long ts) {
        store.fileDeleteFromMove(from, to, ts);
    }

    public void fileDelete(VCSFileProxy file, long ts) {
        store.fileDelete(file, ts);
    }

    public void fileCreateFromMove(VCSFileProxy from, VCSFileProxy to, long ts) {
        store.fileCreateFromMove(from, to, ts);
    }

    public void fileCreate(VCSFileProxy file, long ts) {
        store.fileCreate(file, ts);
    }

    public void fileChange(VCSFileProxy file) {
        store.fileChange(file);
    }

    public void deleteEntry(VCSFileProxy file, long ts) {
        store.deleteEntry(file, ts);
    }
    
    public void cleanUp(long ttl) {
        // screw the impl a bit as we won't run the cleanup asynchronously
        try {
            if(cleanUpImplMethod == null) {            
                cleanUpImplMethod = store.getClass().getDeclaredMethod("cleanUpImpl", new Class[] {long.class});
                cleanUpImplMethod.setAccessible(true);            
            }
            cleanUpImplMethod.invoke(store, new Object[]{ttl});           
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    File getHistoryFile(VCSFileProxy file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if(getHistoryFileMethod == null) {            
            getHistoryFileMethod = store.getClass().getDeclaredMethod("getHistoryFile", new Class[] {VCSFileProxy.class});
            getHistoryFileMethod.setAccessible(true);            
        }
        return (File) getHistoryFileMethod.invoke(store, new Object[]{file});           
    }
        
    File getStoreFolder(VCSFileProxy file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getStoreFolderMethod == null) {            
            getStoreFolderMethod = store.getClass().getDeclaredMethod("getStoreFolder", new Class[] {VCSFileProxy.class});
            getStoreFolderMethod.setAccessible(true);            
        }
        return (File) getStoreFolderMethod.invoke(store, new Object[]{file});           
    }    

    File getDataFile(VCSFileProxy file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getDataFileMethod == null) {            
            getDataFileMethod = store.getClass().getDeclaredMethod("getDataFile", new Class[] {VCSFileProxy.class});
            getDataFileMethod.setAccessible(true);            
        }
        return (File) getDataFileMethod.invoke(store, new Object[]{file});           
    }    

    File getLabelsFile(VCSFileProxy file) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {             
        if(getLabelsFileMethod == null) {            
            getLabelsFileMethod = store.getClass().getDeclaredMethod("getLabelsFile", new Class[] {VCSFileProxy.class});
            getLabelsFileMethod.setAccessible(true);            
        }
        return (File) getLabelsFileMethod.invoke(store, new Object[]{file});           
    }    
    
    File getStoreFile(VCSFileProxy file, long ts, boolean forceCreate) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if(getStoreFileMethod == null) {            
            getStoreFileMethod = store.getClass().getDeclaredMethod("getStoreFile", new Class[] {VCSFileProxy.class, String.class, boolean.class});
            getStoreFileMethod.setAccessible(true);            
        }
        return (File) getStoreFileMethod.invoke(store, new Object[]{file, Long.toString(ts), forceCreate});
    }

    Set<File> getReleasedLocks() throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if(lockedFolders == null) {
            lockedFolders = store.getClass().getDeclaredField("lockedFolders");
            lockedFolders.setAccessible(true);
        }
        return (Set<File>) lockedFolders.get(store);
    }
    
    @Override
    public void waitForProcessedStoring(VCSFileProxy file, String caller) {
        store.waitForProcessedStoring(file, caller);
    }
    
}
