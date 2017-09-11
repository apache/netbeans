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
 * Software is Sun Microsystems, Inc.
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.settings;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
    
    private static final class OverObjects extends ProxyLookup 
    implements PropertyChangeListener, FileChangeListener {
        private final String path;
        
        public OverObjects(String path) {
            super(delegates(null, path));
            this.path = path;
            try {
                ModuleSystem ms = Main.getModuleSystem(false);
                if (ms != null) {
                    ModuleManager man = ms.getManager();
                    man.addPropertyChangeListener(WeakListeners.propertyChange(this, man));
                } else {
                    LOG.log(Level.WARNING, "Not listening on module system");
                }
            } catch (Throwable e) {
                LOG.log(Level.WARNING, "Can't listen on module system", e);
            }
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
