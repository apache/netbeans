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

package org.netbeans.core.startup.preferences;

import java.io.File;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SyncFailedException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.util.EditableProperties;
import org.openide.util.Exceptions;

/**
 * No synchronization - must be called just from NbPreferences which
 *  ensures proper synchronization
 * @author Radek Matous
 */
class PropertiesStorage implements NbPreferences.FileStorage {
    private static final String USERROOT_PREFIX = "/Preferences";//NOI18N
    private static final String SYSTEMROOT_PREFIX = "/SystemPreferences";//NOI18N

    protected final FileObject configRoot;
    private final String folderPath;
    private String filePath;
    private boolean isModified;
    private FileChangeAdapter fileChangeAdapter;
    private static final Logger LOGGER = Logger.getLogger(PropertiesStorage.class.getName());
    
    /*test*/ static Runnable TEST_FILE_EVENT = null;
    
    static NbPreferences.FileStorage instance(final FileObject configRoot, final String absolutePath) {
        return new PropertiesStorage(configRoot, absolutePath, true);
    }
    
    FileObject preferencesRoot() throws IOException {
        return FileUtil.createFolder(configRoot, USERROOT_PREFIX);
    }
    
    static NbPreferences.FileStorage instanceReadOnly(final FileObject configRoot, final String absolutePath) {
        return new PropertiesStorage(configRoot, absolutePath, false) {
            public @Override boolean isReadOnly() {
                return true;
            }
            
            public @Override final String[] childrenNames() {
                return new String[0];
            }
            
            public @Override final EditableProperties load() throws IOException {
                return new EditableProperties(true);
            }
            
            protected @Override FileObject toPropertiesFile(boolean create) throws IOException {
                if (create) {
                    throw new IOException();
                }
                return null;
            }
            
            protected @Override FileObject toFolder(boolean create) throws IOException {
                if (create) {
                    throw new IOException();
                }
                return null;
            }
            
            protected @Override FileObject toPropertiesFile() {
                return null;
            }
            
            protected @Override FileObject toFolder() {
                return null;
            }
            
            @Override FileObject preferencesRoot() throws IOException {
                return FileUtil.createFolder(configRoot, SYSTEMROOT_PREFIX);
            }
            
        };
    }
    
    /** Creates a new instance */
    private PropertiesStorage(final FileObject configRoot, final String absolutePath, boolean userRoot) {
        StringBuilder sb = new StringBuilder();
        String prefix = (userRoot) ? USERROOT_PREFIX : SYSTEMROOT_PREFIX;
        sb.append(prefix).append(absolutePath);
        folderPath = sb.toString();
        this.configRoot = configRoot;
    }
    
    public boolean isReadOnly() {
        return false;
    }
    
    public void markModified() {
        isModified = true;
    }
    
    public final boolean existsNode() {
        return (toPropertiesFile() != null) || (toFolder() != null);
    }
    
    public String[] childrenNames() {
        Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.CHILDREN_NAMES, true);
        try {
            FileObject folder = toFolder();
            List<String> folderNames = new ArrayList<String>();
            
            if (folder != null) {
                for (FileObject fo : Collections.list(folder.getFolders(false))) {
                    Enumeration<? extends FileObject> en = fo.getChildren(true);
                    while (en.hasMoreElements()) {
                        FileObject ffo = en.nextElement();
                        if (ffo.hasExt("properties")) { // NOI18N
                            folderNames.add(fo.getNameExt());
                            break;
                        }
                    }
                }
                for (FileObject fo : Collections.list(folder.getData(false))) {
                    if (fo.hasExt("properties")) { // NOI18N
                        folderNames.add(fo.getName());
                    }
                }
            }
            
            return folderNames.toArray(new String[0]);
        } finally {
            sw.stop();
        }
    }
    
    public final void removeNode() throws IOException {
        Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.REMOVE_NODE, true);
        try {
            FileObject propertiesFile = toPropertiesFile();
            if (propertiesFile != null && propertiesFile.isValid()) {
                propertiesFile.delete();
                FileObject folder = propertiesFile.getParent();
                while (folder != null && folder != preferencesRoot() && folder.getChildren().length == 0) {
                    folder.delete();
                    folder = folder.getParent();
                }
            }
        } finally {
            sw.stop();
        }
    }
    
    public EditableProperties load() throws IOException {
        Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.LOAD, true);
        try {
            EditableProperties retval = new EditableProperties(true);
            FileObject file = toPropertiesFile(false);
            if (file != null) {
                try {
                    InputStream is = file.getInputStream();
                    try {
                        retval.load(is);
                    } finally {
                        is.close();
                    }
                } catch (IllegalArgumentException x) { // #167745
                    Logger.getLogger(PropertiesStorage.class.getName()).log(Level.INFO, "While loading " + file, x);
                    file.delete();
                }
            }
            return retval;
        } finally {
            sw.stop();
        }
    }
    
    public void save(final EditableProperties properties) throws IOException {
        if (isModified) {
            Statistics.StopWatch sw = Statistics.getStopWatch(Statistics.FLUSH, true);
            try {
                isModified = false;
                if (!properties.isEmpty()) {
                    OutputStream os = null;
                    try {
                        os = outputStream();
                        properties.store(os);
                    } finally {
                        if (os != null) {
			    LOGGER.log(Level.FINE, "Closing output-stream for file {0} in {1}.", new Object[]{filePath, folderPath});
			    os.close();
			}
                    }
                } else {
                    FileObject file = toPropertiesFile();
                    if (file != null) {
                        file.delete();
                    }
                    FileObject folder = toFolder();
                    while (folder != null && folder != preferencesRoot() && folder.getChildren().length == 0) {
                        folder.delete();
                        folder = folder.getParent();
                    }
                }
            } finally {
                sw.stop();
            }
        }
    }
    
    private OutputStream outputStream() throws IOException {
        FileObject fo = toPropertiesFile(true);
        final FileLock lock = fo.lock();
        OutputStream os = null;
        try {
            os = fo.getOutputStream(lock);
        } finally {
            if(os == null && lock != null) {
                // release lock if getOutputStream failed
                lock.releaseLock();
            }
        }
        return new FilterOutputStream(os) {
            public @Override void close() throws IOException {
                super.close();
                lock.releaseLock();
            }
        };
    }
    
    private String folderPath() {
        return folderPath;
    }

    private String filePath() {
        if (filePath == null) {
            String[] all = folderPath().split("/");//NOI18N
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < all.length-1; i++) {
                sb.append(all[i]).append("/");//NOI18N
            }
            if (all.length > 0) {
                sb.append(all[all.length-1]).append(".properties");//NOI18N
            } else {
                sb.append("root.properties");//NOI18N
            }
            filePath = sb.toString();
        }
        return filePath;
    }        

    protected FileObject toFolder()  {
        return configRoot.getFileObject(folderPath());
    }

    protected  FileObject toPropertiesFile() {
        return configRoot.getFileObject(filePath());
    }

    protected FileObject toFolder(boolean create) throws IOException {
        FileObject retval = toFolder();
        if (retval == null && create) {
            retval = FileUtil.createFolder(configRoot, folderPath);
        }
        assert (retval == null && !create) || (retval != null && retval.isFolder());
        return retval;
    }
    
    protected FileObject toPropertiesFile(boolean create) throws IOException {
        FileObject retval = toPropertiesFile();
        if (retval == null && create) {
	    // there might be inconsistency between the cache and the disk (#208227)
	    configRoot.refresh();
	    // and try again
	    retval = toPropertiesFile();
	    if (retval == null) {
                // let's see if the file exists on disk and a FileObject can be obtained for it
                retval = FileUtil.toFileObject(FileUtil.normalizeFile(new File(FileUtil.toFile(configRoot), filePath())));
            }
	    if (retval == null) {
		// we really need to create the file
		try {
		    retval = FileUtil.createData(configRoot, filePath());
		} catch (SyncFailedException sfex) {
		    // File could not be created as it already exists!!!
		    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		    LOGGER.log(Level.WARNING, "File {0} seems to already exist in default filesystem {1}."
			    + "\nCurrent date/time: {2}",
			    new Object[]{filePath(),
                                FileUtil.toFile(configRoot).getCanonicalPath(),
				dateFormat.format(Calendar.getInstance().getTime())});
		}
	    }
        }
        assert (retval == null && !create) || (retval != null && retval.isData());
        return retval;
    }

    public void runAtomic(final Runnable run) {
        try {
            configRoot.getFileSystem().runAtomicAction(new AtomicAction() {
                public void run() throws IOException {
                    run.run();
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }


     @Override
     public void attachChangeListener(final ChangeListener changeListener) {
         try {            
             fileChangeAdapter = new FileChangeAdapter(){

              @Override
              public void fileDataCreated(FileEvent fe) {                  
                  if(fe.getFile().equals(toPropertiesFile())){
                      if (TEST_FILE_EVENT != null) {
                          TEST_FILE_EVENT.run();
                      }
                      changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                  }
              }

              @Override
              public void fileFolderCreated(FileEvent fe) {                  
                  if(fe.getFile().equals(toPropertiesFile())){
                      changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                  }
              }

              @Override
              public void fileChanged(FileEvent fe) {                  
                  if(fe.getFile().equals(toPropertiesFile())){
                      if (TEST_FILE_EVENT != null) {
                          TEST_FILE_EVENT.run();
                      }
                      changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                  }
              }

              @Override
              public void fileDeleted(FileEvent fe) {                 
                  if(fe.getFile().equals(toPropertiesFile())){
                      changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                  }
              }

              @Override
              public void fileRenamed(FileRenameEvent fe) {                 
                  if(fe.getFile().equals(toPropertiesFile())){
                      changeListener.stateChanged(new ChangeEvent(PropertiesStorage.this));
                  }
              }

          };
             configRoot.getFileSystem().addFileChangeListener(FileUtil.weakFileChangeListener(fileChangeAdapter, configRoot.getFileSystem()));
         } catch (FileStateInvalidException ex) {
             Exceptions.printStackTrace(ex);
         }
     }
}
