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

package org.netbeans.core.startup.layers;

import org.netbeans.core.startup.base.LayerFactory;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.NotSerializableException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import static org.netbeans.core.startup.layers.Bundle.*;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MIMEResolver;
import org.openide.filesystems.MultiFileSystem;
import org.openide.modules.PatchedPublic;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;

/** The system FileSystem - represents system files under $NETBEANS_HOME/system.
*
* @author Jan Jancura, Ian Formanek, Petr Hamernik
*/
@NbBundle.Messages({
    "INSTANCE_FILES=Instance Files"
})
@MIMEResolver.ExtensionRegistration(
    displayName="#INSTANCE_FILES",
    mimeType="content/unknown",
    extension="instance",
    position=90
)
public final class SystemFileSystem extends MultiFileSystem 
implements FileChangeListener {
    // Must be public for BeanInfo to work: #11186.

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7761052280240991668L;

    /** system name of this filesystem */
    private static final String SYSTEM_NAME = "SystemFileSystem"; // NOI18N

    private static final Logger LOG = Logger.getLogger(SystemFileSystem.class.getName());

    /** A mutex to use to guard access to changes of layers in the 
     * system file system. */
    public static void registerMutex(Mutex mutex) {
        ModuleLayeredFileSystem.registerMutex(mutex);
    }

    /** user fs */
    private ModuleLayeredFileSystem user;
    /** home fs */
    private ModuleLayeredFileSystem home;

    /** @param fss list of file systems to delegate to
    */
    @SuppressWarnings("deprecation")
    private SystemFileSystem (FileSystem[] fss) throws PropertyVetoException {
        super (fss);
        user = (ModuleLayeredFileSystem) fss[0];
        home = fss.length > 1 ? (ModuleLayeredFileSystem) fss[1] : null;
        
        setSystemName(SYSTEM_NAME);
        addFileChangeListener(this);
    }


    /** Name of the system */
    @Messages("CTL_SystemFileSystem=Default System")
    @Override public String getDisplayName() {
        return CTL_SystemFileSystem();
    }
    
    /** Getter for the instalation layer filesystem.
     * May be null if there is none such.
    */
    public ModuleLayeredFileSystem getInstallationLayer () {
        return home;
    }
    
    /** Getter for the user layer filesystem.
    */
    public ModuleLayeredFileSystem getUserLayer () {
        return user;
    }
    
    /** Changes layers to provided values.
     * @param arr the new layers
     * @throws IllegalArgumentException if there is an overlap
     */
    public final void setLayers (FileSystem[] arr) throws IllegalArgumentException {
        Set<FileSystem> s = new HashSet<FileSystem> ();
        for (int i = 0; i < arr.length; i++)
            if (s.contains (arr[i]))
                throw new IllegalArgumentException ("Overlap in filesystem layers"); // NOI18N
            else
                s.add (arr[i]);

        // create own internal copy of passed filesystems
        setDelegates(arr.clone());
        firePropertyChange ("layers", null, null); // NOI18N
    }
    
    /** Getter for the array of filesystems that are currently used 
    * in the IDE.
    *
    * @return array of filesystems
    */
    public FileSystem[] getLayers() {
        // don't return reference to internal buffer
        return getDelegates().clone();
    }
    
    protected @Override FileSystem createWritableOnForRename(String oldName, String newName) throws IOException {
        return createWritableOn (oldName);
    }
    
    protected @Override FileSystem createWritableOn(String name) throws IOException {
        FileSystem[] fss = getDelegates ();
        for (int index = 0; index < fss.length; index++) {
            if (! fss[index].isReadOnly ())
                return fss[index];
        }
        // Can really happen if invoked from e.g. org.netbeans.core.Plain.
        throw new IOException("No writable filesystems in our delegates"); // NOI18N
    }
    
    protected @Override Set<? extends FileSystem> createLocksOn(String name) throws IOException {
        LocalFileSystemEx.potentialLock (name);
        return super.createLocksOn (name);
    }
    
    /** Initializes and creates new repository. This repository's system fs is
    * based on the content of ${HOME_DIR}/system and ${USER_DIR}/system directories
    *
    * @param userDir directory where user can write, or null to do it in memory
    * @param homeDir directory where netbeans has been installed, user need not have write access, or null if none
    * @param extradirs 0+ additional directories to use like homeDir
    * @return repository
    * @exception PropertyVetoException if something fails
    */
    // note: PatchedPublic, used from core.startup, but should not be exposed as an API;
    // possibly could be trampolined through some private packate as core.startup uses impl dependency
    @PatchedPublic
    static SystemFileSystem create (File userDir, File homeDir, File[] extradirs)
    throws java.beans.PropertyVetoException, IOException {
        FileSystem user;

        String customFSClass = System.getProperty("org.netbeans.core.systemfilesystem.custom"); // NOI18N
        if (customFSClass != null) {
            try {
                Class<?> clazz = Class.forName(customFSClass);
                Object instance = clazz.getDeclaredConstructor().newInstance();
                user = (FileSystem)instance;
            } catch (Exception x) {
                ModuleLayeredFileSystem.err.log(
                    Level.WARNING,
                    "Custom system file system writable layer init failed ", x); // NOI18N
                user = FileUtil.createMemoryFileSystem ();
            }
        } else {
            if (userDir != null) {
                // only one file system
                if (!userDir.exists()) {
                    userDir.mkdirs();
                }
                LocalFileSystem l = new LocalFileSystemEx(true);
                l.setRootDirectory(userDir);
                user = l;
            } else {
                user = FileUtil.createMemoryFileSystem ();
            }
        }

        ModuleLayeredFileSystem homeFS;
        
        if (homeDir == null || !homeDir.isDirectory()) {
            homeFS = null;
        } else {
            homeFS =  createInstallHomeSystem(homeDir, extradirs, true, false).getUserLayer();
        }
        FileSystem[] arr = new FileSystem[homeFS == null ? 1 : 2];
        
        LayerFactory.Provider f = Lookup.getDefault().lookup(LayerFactory.Provider.class);
        arr[0] = new ModuleLayeredFileSystem(user, true, new FileSystem[0], f.create(false));
        if (homeFS != null) {
            arr[1] = homeFS;
        }
        return new SystemFileSystem (arr);
    }
    
    /**
     * Creates a layered filesystem for the NetBeans installation. The 'homeDir' must be writable,
     * and will be used as a writable layer of the default filesystem. If `includeLookup' is set,
     * the resulting FileSystem will contain entries from XML layers specified in manifests or generated,
     * to provide configuration/data for Lookup implementations.
     * 
     * @param homeDir the writable area
     * @param extradirs extra directories merged into the filesystem.
     * @param readOnly creates the filesystem as read-only; useful for system-wide configuration which is not used
     * itself, but only provides a base for user filesystems.
     * @param includeLookup if true, layers are merged into the filesystem to provide Lookup definitions
     * @return created Filesystem instance.
     * @since 1.60
     */
    public static SystemFileSystem createInstallHomeSystem(File homeDir, File[] extradirs, boolean readOnly, boolean includeLookup) throws IOException, PropertyVetoException {
        LayerFactory.Provider f = Lookup.getDefault().lookup(LayerFactory.Provider.class);
        LocalFileSystem[] extras = new LocalFileSystem[extradirs.length];
        for (int i = 0; i < extradirs.length; i++) {
            extras[i] = new LocalFileSystemEx();
            extras[i].setRootDirectory(extradirs[i]);
            extras[i].setReadOnly(true);
        }
        LocalFileSystem home = new LocalFileSystemEx ();
        home.setRootDirectory (homeDir);
        home.setReadOnly (readOnly);                        
        FileSystem[] arr = new FileSystem[] {
            new ModuleLayeredFileSystem(home, includeLookup, extras, f.create(true))
        };
        return new SystemFileSystem(arr);
    }
    
    /**
     * Creates user-level filesystem. The system is created as an user layer over a r/o base FileSystem (`base').
     * @param userDir the writable area
     * @param base the base filesystem, which MUST include Lookup entries
     * @return the system filesystem customized for the user.
     * 
     * @throws IOException
     * @throws PropertyVetoException 
     * @since 1.60 
     */
    public static SystemFileSystem createUserFileSystem(File userDir, ModuleLayeredFileSystem base)  throws IOException, PropertyVetoException {
        LayerFactory.Provider f = Lookup.getDefault().lookup(LayerFactory.Provider.class);
        LocalFileSystem user = new LocalFileSystemEx ();
        user.setRootDirectory (userDir);
        FileSystem[] arr = new FileSystem[] {
            new ModuleLayeredFileSystem(user, false, new FileSystem[0], f.create(false)),
            base
        };
        return new SystemFileSystem(arr);
    }

    /** Notification that a file has migrated from one file system
    * to another. Usually when somebody writes to file on readonly file
    * system and the file has to be copied to write one. 
    * <P>
    * This method allows subclasses to fire for example FileSystem.PROP_STATUS
    * change to notify that annotation of this file should change.
    *
    * @param fo file object that change its actual file system
    */
    @Override
    protected void notifyMigration (FileObject fo) {
        fireFileStatusChanged (new FileStatusEvent (this, fo, false, true));
    }

    public void fileFolderCreated(FileEvent fe) {
        log("fileFolderCreated", fe); // NOI18N
    }

    public void fileDataCreated(FileEvent fe) {
        log("fileDataCreated", fe); // NOI18N
    }

    public void fileChanged(FileEvent fe) {
        log("fileChanged", fe); // NOI18N
    }

    public void fileDeleted(FileEvent fe) {
        log("fileDeleted", fe); // NOI18N
    }

    public void fileRenamed(FileRenameEvent fe) {
        log("fileDeleted", fe); // NOI18N
    }

    public void fileAttributeChanged(FileAttributeEvent fe) {
        log("fileAttributeChanged", fe); // NOI18N
    }

    @Messages({"# {0} - type", "# {1} - path to the file", "# {2} - file object itself", "# {3} - event itself", "LOG_FILE_EVENT=File event {0}, file {1}"})
    private static void log(String type, FileEvent fe) {
        if (LOG.isLoggable(Level.FINER)) {
            LogRecord r = new LogRecord(Level.FINER, "LOG_FILE_EVENT");
            r.setLoggerName(LOG.getName());
            r.setParameters(new Object[] {
                type,
                fe.getFile().getPath(),
                fe.getFile(),
                fe
            });
            r.setResourceBundle(NbBundle.getBundle(SystemFileSystem.class));
            LOG.log(r);
        }
    }

    // --- SAFETY ---
    private Object writeReplace() throws ObjectStreamException {
        new NotSerializableException("WARNING - SystemFileSystem is not designed to be serialized").printStackTrace(); // NOI18N
        return new SingletonSerializer();
    }
    
    private static final class SingletonSerializer extends Object implements Serializable {
        private static final long serialVersionUID = 6436781994611L;
        SingletonSerializer() {}
        private Object readResolve () throws ObjectStreamException {
            try {
                return FileUtil.getConfigRoot().getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        }
    }
    // --- SAFETY ---

}
