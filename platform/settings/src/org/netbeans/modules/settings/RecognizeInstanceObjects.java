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

package org.netbeans.modules.settings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.ModuleManager;
import org.netbeans.core.startup.Main;
import org.netbeans.core.startup.ModuleSystem;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/** Use FolderLookup to find out instances of named services.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(
    service=NamedServicesProvider.class,
    position=200,
    supersedes="org.netbeans.modules.openide.filesystems.RecognizeInstanceFiles"
)
public final class RecognizeInstanceObjects extends NamedServicesProvider {
    private static final Logger LOG = Logger.getLogger(RecognizeInstanceObjects.class.getName());
    
    @Override
    public <T> T lookupObject(String path, Class<T> type) {
        FileObject fo = FileUtil.getConfigFile(path)    ;
        if (fo != null) {
            try {
                InstanceCookie ic = DataObject.find(fo).getLookup().lookup(InstanceCookie.class);
                Object obj = ic != null ? ic.instanceCreate() : null;
                return type.isInstance(obj) ? type.cast(obj) : null;
            } catch (IOException ex) {
                LOG.log(Level.INFO, "Cannot create instance for " + path, ex);
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.INFO, "Cannot create instance for " + path, ex);
            }
        }
        return null;
    }
    
    @Override
    public Lookup create(String path) {
        return new OverObjects(path);
    }        

    // XXX: Update dependency
    @Override
    protected Lookup lookupFor(Object obj) {
        if (obj instanceof FileObject) {
            try {
                return DataObject.find((FileObject)obj).getLookup();
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.INFO, "Can't find DataObject for " + obj, ex);
            }
        }
        return null;
    }
    
    private static final class MSL implements PropertyChangeListener {
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        public static final MSL DEFAULT = new MSL();
        static {
            try {
                ModuleSystem ms = Main.getModuleSystem(false);
                if (ms != null) {
                    ModuleManager man = ms.getManager();
                    man.addPropertyChangeListener(WeakListeners.propertyChange(DEFAULT, man));
                } else {
                    LOG.log(Level.WARNING, "Not listening on module system");
                }
            } catch (Throwable e) {
                LOG.log(Level.WARNING, "Can't listen on module system", e);
            }
        }
        private MSL() {
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            pcs.firePropertyChange(evt);
        }
    }

    private static final class OverObjects extends ProxyLookup
    implements PropertyChangeListener, FileChangeListener {
        private final String path;

        public OverObjects(String path) {
            super(delegates(null, path));
            this.path = path;
            MSL.DEFAULT.addPropertyChangeListener(
                WeakListeners.propertyChange(this, MSL.DEFAULT)
            );
            try {
                FileSystem sfs = FileUtil.getConfigRoot().getFileSystem();
                sfs.addFileChangeListener(FileUtil.weakFileChangeListener(this, sfs));
            } catch (FileStateInvalidException x) {
                assert false : x;
            }
        }
        
        @SuppressWarnings("deprecation")
        private static Lookup[] delegates(Lookup prevFolderLkp, String path) {
            ClassLoader loader = Lookup.getDefault().lookup(ClassLoader.class);
            LOG.log(Level.FINEST, "lkp loader: {0}", loader);
            if (loader == null) {
                loader = Thread.currentThread().getContextClassLoader();
                LOG.log(Level.FINEST, "ccl: {0}", loader);
            }
            if (loader == null) {
                loader = RecognizeInstanceObjects.class.getClassLoader();
            }
            LOG.log(Level.FINER, "metaInfServices for {0}", loader);
            Lookup base = Lookups.metaInfServices(loader, "META-INF/namedservices/" + path); // NOI18N
            FileObject fo = FileUtil.getConfigFile(path);
            if (fo == null) {
                return new Lookup[] {base};
            }
            String s;
            if (path.endsWith("/")) { // NOI18N
                s = path.substring(0, path.length() - 1);
            } else {
                s = path;
            }
            if (prevFolderLkp == null) {
                prevFolderLkp = new org.openide.loaders.FolderLookup(DataFolder.findFolder(fo), s).getLookup();
            }
            return new Lookup[] {prevFolderLkp, base};
        }
        
        Lookup extractFolderLkp() {
            Lookup[] arr = getLookups();
            return arr.length == 2 ? arr[0] : null;
        }
    
        @Override
        public void propertyChange(PropertyChangeEvent ev) {
            setLookups(delegates(extractFolderLkp(), path));
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            ch(fe);
        }
        @Override
        public void fileDataCreated(FileEvent fe) {
            ch(fe);
        }
        @Override
        public void fileChanged(FileEvent fe) {
            ch(fe);
        }
        @Override
        public void fileDeleted(FileEvent fe) {
            ch(fe);
        }
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            ch(fe);
        }
        @Override
        public void fileAttributeChanged(FileAttributeEvent fe) {
            ch(fe);
        }
        private void ch(FileEvent e) {
            if ((e.getFile().getPath() + "/").startsWith(path)) { // NOI18N
                setLookups(delegates(extractFolderLkp(), path));
            }
        }

    } // end of OverObjects
}
