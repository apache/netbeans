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

import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.util.VersioningListener;

/**
 *
 * @author Tomas Stupka
 */
// XXX what about multifile dataobjects ?
public interface LocalHistoryStore {
    
    
    /**
     * Indicates that a files history has changed. 
     * First parameter: File which history has changed
     */
    public Object EVENT_HISTORY_CHANGED = new Object();

    /**
     * Indicates that an enmtry from a files history has been deleted
     * First parameter: File which entry has been deleted from its history
     */    
    public Object EVENT_ENTRY_DELETED = new Object();
    
    /**
     * Marks in the given files history that it was created with timestamp ts. The files content won't 
     * be copied into the storage until a change is notified via fileChange(). 
     * 
     * @param file 
     * @param ts 
     */
    public void fileCreate(VCSFileProxy file, long ts);        
    
    /**
     * Stores the files actual state under the given timestamp and marks it as deleted
     * 
     * @param file the file which has to be stored
     * @param ts the timestamp under which the file has to be stored
     */
    public void fileDelete(VCSFileProxy file, long ts);
    
    /**
     * Marks in toFile-s history that it was created with timestamp ts as a result from
     * being moved from fromFile. The toFile-s content won't 
     * be copied into the storage until a change is notified via fileChange(). 
     * 
     * @param fromFile
     * @param toFile 
     * @param ts 
     */
    public void fileCreateFromMove(VCSFileProxy fromFile, VCSFileProxy toFile, long ts);
    
    /**
     * Stores fromFile-s actual state under the given timestamp and 
     * marks that it has been moved to the toFile
     * 
     * @param from 
     * @param to 
     * @param ts 
     */
    public void fileDeleteFromMove(VCSFileProxy fromFile, VCSFileProxy toFile, long ts);
 
    /**
     * Stores the files actual state
     * 
     * @param file the file which has to be stored
     */
    public void fileChange(VCSFileProxy file);                   
        
    /**
     * Sets a label for an entry represented by the given file and timestamp
     * 
     * @param file the file for which entry the label has to be set
     * @param ts timestamp
     * @param label the label to be set 
     */ 
    public StoreEntry setLabel(VCSFileProxy file, long ts, String label);    
    
    /**
     * Adds a property change listener
     * 
     * @param l the property change listener
     */
    public void addVersioningListener(VersioningListener l);
    
    /**
     * Removes a property change listener
     * 
     * @param l the property change listener
     */
    public void removeVersioningListener(VersioningListener l);
    
    /**
     * Returns all entries for a file
     * 
     * @param file the file for which the entries are to be retrieved
     * @return StoreEntry[] all entries present in the storage
     */ 
    public StoreEntry[] getStoreEntries(VCSFileProxy file);
    
    /**
     * Returns an entry representing the given files state in time ts
     * 
     * @param file the file for which the entries are to be retrieved
     * @param ts the time for which the StoreEntry has to retrieved
     * @return StoreEntry a StoreEntry representing the given file in time ts. 
     *         <tt>null</tt> if file is a directory or there is no entry with a timestamp &lt; <tt>ts</tt>
     */ 
    public StoreEntry getStoreEntry(VCSFileProxy file, long ts);
    
    /**
     * Return an StoreEntry array representing the given root folders state 
     * in the history to the given timestamp ts. The files array contains the
     * actually existing files under root.
     * 
     * NOT REALY USED YET
     * 
     * @param root the folder for which the StoreEntry array has to be returned
     * @param files files which actually exist under root
     * @param ts timestamp to which teh history has to be retrieved
     * @return StoreEntry array representing the given root folders state 
     */ 
    public StoreEntry[] getFolderState(VCSFileProxy root, VCSFileProxy[] files, long ts);        
    
    /**
     * Returns StoreEntries for files which are directly 
     * under the given root folder and:
     * <ul>
     *  <li> their youngest entry is marked as deleted
     *  <li> or have an entry in the storage but don't exist under the given root anymore e.g. externally deleted
     * </ul>
     * 
     * @param root 
     * @return an array of StoreEntries
     */ 
    public StoreEntry[] getDeletedFiles(VCSFileProxy root);    
    
    /**
     * Deletes a StoreEntry from the storage represented by the given file and timestamp
     * 
     * @param file the file for which a StoreEntry has to be deleted
     * @param ts the timestamp for which a StoreEntry has to be deleted
     * 
     */ 
    public void deleteEntry(VCSFileProxy file, long ts); 
        
    /**
     * Removes all history information from the storage which is older than now - ttl. 
     * 
     * @param ttl time to live
     */ 
    public void cleanUp(long ttl);    
    
    /**
     * Checks whether the given file is currently being copied into the Local History 
     * storage and blocks eventually until the copying operation finishes.
     * See implementation for info about a possible timeout. 
     * 
     * 
     * @param file the file to check
     * @param caller identifies whoever called this method
     */
    public void waitForProcessedStoring(VCSFileProxy file, String caller);
            
}
