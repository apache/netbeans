/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.localhistory.store;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Tomas Stupka
 * 
 * // XXX override for folder and to be deleted 
 * // XXX status or isDeleted
 * 
 */
public abstract class StoreEntry {

    private final VCSFileProxy file;
    private final File storeFile;
    private final long ts;
    private final String label;
    private final Date date;    
    private String mimeType = null;
    private List<StoreEntry> siblingEntries;
      
    public static StoreEntry createStoreEntry(VCSFileProxy file, File storeFile, long ts, String label) {
        return new DefaultStoreEntry(file, storeFile, ts, label);
    }

    public static StoreEntry createDeletedStoreEntry(VCSFileProxy file, long ts) {
        return new DeletedStoreEntry(file, ts);
    }

//    public static StoreEntry createFakeStoreEntry(File file, long ts) {
//        return new FakeStoreEntry(file, ts);
//    }
    
    private StoreEntry(VCSFileProxy file, File storeFile, long ts, String label) {
        this.file = file;
        this.storeFile = storeFile;
        this.ts = ts;
        this.label = label;
        this.date = new Date(ts);
        setSiblings(Collections.<StoreEntry>emptyList());
    }    
    
    public File getStoreFile() {
        return storeFile;
    }

    public VCSFileProxy getFile() {
        return file;
    }
    
    public long getTimestamp() {
        return ts;
    }
    
    public String getLabel() {
        return label != null ? label : "";
    }
    
    public Date getDate() {
        return date;
    }
    
    public boolean representsFile() {
        return storeFile.isFile();
    }

    /**
     * Returns all sibling entries for multi-file DO which this file is part of.
     * @return
     */
    public List<StoreEntry> getSiblingEntries() {
        return siblingEntries;
    }

    /**
     * Sets sibling entries for files comming from the same multi-file DO.
     * @param entries sibling entries
     */
    public void setSiblings (Collection<StoreEntry> entries) {
        siblingEntries = new ArrayList<StoreEntry>(entries.size());
        for (StoreEntry entry : entries) {
            // add only real siblings, not itself
            if (entry.representsFile() && !getFile().equals(entry.getFile())) {
                siblingEntries.add(entry);
            }
        }
        siblingEntries = Collections.unmodifiableList(siblingEntries);
    }

    static OutputStream createStoreFileOutputStream(File storeFile) throws FileNotFoundException, IOException {
        int retry = 0;
        while (true) {
            try {
                ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(storeFile)));
                ZipEntry entry = new ZipEntry(storeFile.getName());
                zos.putNextEntry(entry);
                return zos;
            } catch (IOException ex) {
                retry++;
                if (retry > 7) {
                    throw ex;
                }
                try {
                    Thread.sleep(retry * 34);
                } catch (InterruptedException iex) {
                    throw ex;
                }
            }
        }

    }
        
    abstract OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException;
    public abstract InputStream getStoreFileInputStream() throws FileNotFoundException, IOException;

    private static class DefaultStoreEntry extends StoreEntry {
        
        private DefaultStoreEntry(VCSFileProxy file, File storeFile, long ts, String label) {
            super(file, storeFile, ts, label);
        }    
        
        OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
            return createStoreFileOutputStream(getStoreFile());
        }

        public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(getStoreFile())));
            ZipEntry entry;
            while ( (entry = zis.getNextEntry()) != null ) {
                if( entry.getName().equals(getStoreFile().getName()) ) {
                    return zis;
                }
            }
            throw new FileNotFoundException("no entry in zip store file " + getStoreFile().getAbsolutePath() + " for file " + getFile());
        }                    
    }
    
    private static class DeletedStoreEntry extends StoreEntry {  
        public DeletedStoreEntry(VCSFileProxy file, long ts) {
            super(file, null, ts, "");
        } 
        
        @Override
        OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
            throwNoStoreEntry();
            return null;
        }

        @Override
        public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
            throwNoStoreEntry();
            return null;            
        }            
        
        private void throwNoStoreEntry() throws FileNotFoundException {
            throw new FileNotFoundException("There is no store entry for file " + getFile() + " and timestamp " + getTimestamp());
        }
        
    }

//    private static class FakeStoreEntry extends StoreEntry {
//        
//        public FakeStoreEntry(VCSFileProxy file, long ts) {
//            super(file, file, ts, "");
//        }  
//
//        @Override
//        OutputStream getStoreFileOutputStream() throws FileNotFoundException, IOException {
//            throw new FileNotFoundException("There is no OutputStream for this for file " + getFile());
//        }
//
//        @Override
//        public InputStream getStoreFileInputStream() throws FileNotFoundException, IOException {
//            return new FileInputStream(getFile());            
//        }            
//    }    
}
