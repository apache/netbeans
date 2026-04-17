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

package org.openide.filesystems;

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.*;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.ServiceProviders;

/**
 * Interface that provides basic information about a virtual filesystem.
 * <p>Implementing classes should also have associated subclasses of {@link FileObject}.
 * But most subclasses will be of either {@link AbstractFileSystem} or {@link MultiFileSystem}.</p>
 * <p>When using the {@code org.netbeans.modules.masterfs} module, you do not need
 * to explicitly create any filesystems just to access disk files; use {@link FileUtil#toFileObject}
 * instead.</p>
 * <p>The system filesystem may be gotten from {@link FileUtil#getConfigRoot} and
 * normally includes XML layers created using {@link XMLFileSystem}.</p>
 * <p>Since version 7.1 you can dynamically change the content of the system filesystem.
 * Either by inserting your own implementation of a filesystem directly or by
 * registering so called {@link Repository.LayerProvider}. With the 
 * {@link Repository.LayerProvider provider} you can show a dialog letting the user log in 
 * to some server and only later make {@link XMLFileSystem additional XML layers}
 * available.
 * <p> 
 * In case you need to insert a dynamic file system with various definitions (NetBeans Platform menus,
 * toolbars, layout of windows, etc.), you can create a filesystem implementation, 
 * then register it in default lookup.
 * It is easiest to subclass {@link AbstractFileSystem} and implement 
 * few simple query interfaces ({@link AbstractFileSystem.List}, 
 * {@link AbstractFileSystem.Info}, {@link AbstractFileSystem.Change}).
 * For example:</p>
 * <pre>
{@code @}{@link ServiceProviders}({
    {@code @}{@link ServiceProvider}(service=FileSystem.class),
    {@code @}{@link ServiceProvider}(service=LoginFileSystem.class)
})
public class LoginFileSystem extends AbstractFileSystem implements
{@link AbstractFileSystem.List}, {@link AbstractFileSystem.Info}, {@link AbstractFileSystem.Change} {
    public LoginFileSystem() {
        this.info = this;
        this.change = this;
        this.list = this;
    }
    public static void initialize() throws SAXException {
        LoginFileSystem lfs = {@link Lookup}.getDefault().lookup(LoginFileSystem.class);
        lfs.initializeSomehow();
    }
     
    private void initializeSomehow() {
        // do some initialization
        super.refresh();
    } 
    
    public String[] children(String path) {
        // compute list of children somehow
    }
    
    // other method implementations follow...
}
 * </pre>
 * <p>Since version 7.3 you can also return {@link Boolean#TRUE} from a call to
 * {@code yourFS.getRoot().getAttribute("fallback")} so as to place your filesystem
 * behind all layers provided by standard modules.</p>
 */
public abstract class FileSystem implements Serializable {

    static final Logger LOG = Logger.getLogger(FileSystem.class.getName());

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -8931487924240189180L;

    /** Property name indicating validity of filesystem. */
    public static final String PROP_VALID = "valid"; // NOI18N

    /**
     * Property name indicating whether filesystem is hidden.
     * @deprecated The property is now hidden.
     */
    @Deprecated
    public static final String PROP_HIDDEN = "hidden"; // NOI18N

    /**
     * Property name giving internal system name of filesystem.
     * @deprecated This system name should now be avoided in favor of identifying files persistently by URL.
     */
    @Deprecated
    public static final String PROP_SYSTEM_NAME = "systemName"; // NOI18N

    /** Property name giving display name of filesystem.
     * @since 2.1
     */
    public static final String PROP_DISPLAY_NAME = "displayName"; // NOI18N    

    /** Property name giving root folder of filesystem. */
    public static final String PROP_ROOT = "root"; // NOI18N

    /** Property name giving read-only state. */
    public static final String PROP_READ_ONLY = "readOnly"; // NOI18N

    /** Used for synchronization purpose*/
    private static final Object internLock = new Object();
    private static transient ThreadLocal<EventControl> thrLocal = new ThreadLocal<EventControl>();

    /** Empty status */
    private static final StatusDecorator STATUS_NONE = new StatusDecorator() {
            public String annotateName(String name, Set<? extends FileObject> files) {
                return name;
            }

            @Override
            public String annotateNameHtml(String name, Set<? extends FileObject> files) {
                return null;
            }
        };

    /** is this filesystem valid?
    * It can be invalid if there is another filesystem with the
    * same name in the filesystem pool.
    */
    private transient boolean valid = false;

    /** True if the filesystem is assigned to pool.
    * Is modified from Repository methods.
    */
    transient boolean assigned = false;

    /**Repository that contains this FileSystem or null*/
    private transient Repository repository = null;
    private transient FCLSupport fclSupport;

    /** system name */
    private String systemName = ""; // NOI18N

    /** Utility field used by event firing mechanism. */
    private transient ListenerList<FileStatusListener> fileStatusList;
    private transient ListenerList<VetoableChangeListener> vetoableChangeList;
    private transient PropertyChangeSupport changeSupport;

    /** Default constructor. */
    public FileSystem() {
    }

    /** Should check for external modifications. All existing FileObjects will be
     * refreshed. For folders it should reread the content of disk,
     * for data file it should check for the last time the file has been modified.
     *
     * The default implementation is to do nothing, in contradiction to the rest
     * of the description. Unless subclasses override it, the method does not work.
     *
     * @param expected should the file events be marked as expected change or not?
     * @see FileEvent#isExpected
     * @since 2.16
     */
    public void refresh(boolean expected) {
    }

    /** Test whether filesystem is valid.
    * Generally invalidity would be caused by a name conflict in the filesystem pool.
    * @return true if the filesystem is valid
    */
    public final boolean isValid() {
        return valid;
    }

    /** Setter for validity. Accessible only from filesystem pool.
    * @param v the new value
    */
    final void setValid(boolean v) {
        if (v != valid) {
            valid = v;
            firePropertyChange(
                PROP_VALID, (!v) ? Boolean.TRUE : Boolean.FALSE, v ? Boolean.TRUE : Boolean.FALSE, Boolean.FALSE
            );
        }
    }

    /** Provides a name for the system that can be presented to the user.
    * <P>
    * This call should <STRONG>never</STRONG> be used to attempt to identify the file root
    * of the filesystem. On some systems it may happen to look the same but this is a
    * coincidence and may well change in the future. Either check whether
    * you are working with a {@link LocalFileSystem} or similar implementation and use
    * {@link LocalFileSystem#getRootDirectory}; or better, try
    * {@link FileUtil#toFile} which is designed to do this correctly.
    * <p><strong>Note:</strong> for most purposes it is probably a bad idea to use
    * this method. Instead look at {@link FileUtil#getFileDisplayName}.
    * @return user presentable name of the filesystem
    */
    public abstract String getDisplayName();

    /** Internal (system) name of the filesystem.
    * Should uniquely identify the filesystem, as it will
    * be used during serialization of its files. The preferred way of doing this is to concatenate the
    * name of the filesystem type (e.g. the class) and the textual form of its parameters.
    * <P>
    * A change of the system name should be interpreted as a change of the internal
    * state of the filesystem. For example, if the root directory is moved to different
    * location, one should rebuild representations for all files
    * in the system.
    * <P>
    * This call should <STRONG>never</STRONG> be used to attempt to identify the file root
    * of the filesystem. On Unix systems it may happen to look the same but this is a
    * coincidence and may well change in the future. Either check whether
    * you are working with a {@link LocalFileSystem} or similar implementation and use
    * {@link LocalFileSystem#getRootDirectory}; or better, try
    * {@link FileUtil#toFile} which is designed to do this correctly.
    * @return string with system name
     * @deprecated The system name should now be avoided in favor of identifying files persistently by URL.
    */
    @Deprecated
    public final String getSystemName() {
        return systemName;
    }

    /** Changes system name of the filesystem.
    * This property is bound and constrained: first of all
    * all vetoable listeners are asked whether they agree with the change. If so,
    * the change is made and all change listeners are notified of
    * the change.
    *
    * <p><em>Warning:</em> this method is protected so that only subclasses can change
    *    the system name.
    *
    * @param name new system name
    * @exception PropertyVetoException if the change is not allowed by a listener
     * @deprecated The system name should now be avoided in favor of identifying files persistently by URL.
    */
    @Deprecated
    protected final void setSystemName(String name) throws PropertyVetoException {
        String o;
        String n;
        synchronized (PROP_SYSTEM_NAME) {
            if (systemName.equals(name)) {
                return;
            }

            // I must be the only one who works with system pool (that is listening)
            // on this interface
            fireVetoableChange(PROP_SYSTEM_NAME, systemName, name);

            o = systemName;
            n = systemName = name.intern();
        }
        
        firePropertyChange(PROP_SYSTEM_NAME, o, n);

        /** backward compatibility for FileSystems that don`t fire
         * PROP_DISPLAY_NAME*/
        firePropertyChange(PROP_DISPLAY_NAME, null, null);
    }
    
    /**
     * Caches the value of 'default' flag. It is extremely costly to call to FileUtil.getConfigRoot()
     * during startup, as the getter for repository waits for the rapidly changing default Lookup. 
     * In a multi-context environment, the flag <b>might</b> be inappropriate for the current execution context;
     * an execution "A" could possibly reach an instance of default FS for execution "B". But as the two execution
     * should each operate with a different Repository with distinct instances of FileSystems, such condition
     * should be treated as an error.
     */
    private volatile Boolean defFS;

    /** Returns <code>true</code> if the filesystem is default.
     * @return true if this is {@link Repository#getDefaultFileSystem}
    */
    public final boolean isDefault() {
        boolean check = false;
        // XXX hotfix
        //	assert check = true;
        if (defFS != null && !check) {
            return defFS;
        }
        FileSystem fs = null;
        try {
            fs = FileUtil.getConfigRoot().getFileSystem();
        } catch (FileStateInvalidException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (defFS != null) {
            // XXX hotfix 
            //	    assert defFS == (this == fs) : "Default filesystem used in foreign execution";
        }
        return defFS = (this == fs);
    }

    /** Test if the filesystem is read-only or not.
    * @return true if the system is read-only
    */
    public abstract boolean isReadOnly();

    /** Getter for root folder in the filesystem.
    *
    * @return root folder of whole filesystem
    */
    public abstract FileObject getRoot();

    /** Finds file in the filesystem by name.
    * <P>
    * The default implementation converts dots in the package name into slashes,
    * concatenates the strings, adds any extension prefixed by a dot and calls
    * the {@link #findResource findResource} method.
    *
    * <p><em>Note:</em> when both of <code>name</code> and <code>ext</code> are <CODE>null</CODE> then name and
    *    extension should be ignored and scan should look only for a package.
    *
    * @param aPackage package name where each package component is separated by a dot
    * @param name name of the file (without dots) or <CODE>null</CODE> if
    *    one wants to obtain a folder (package) and not a file in it
    * @param ext extension of the file (without leading dot) or <CODE>null</CODE> if one needs
    *    a package and not a file
    *
    * @return a file object that represents a file with the given name or
    *   <CODE>null</CODE> if the file does not exist
    * @deprecated Please use the <a href="@org-netbeans-api-java-classpath@/org/netbeans/api/java/classpath/ClassPath.html">ClassPath API</a> instead, or use {@link #findResource} if you are not interested in classpaths.
    */
    @Deprecated
    public FileObject find(String aPackage, String name, String ext) {
        assert false : "Deprecated.";

        StringBuffer bf = new StringBuffer();

        // append package and name
        if (!aPackage.equals("")) { // NOI18N

            String p = aPackage.replace('.', '/');
            bf.append(p);
            bf.append('/');
        }

        // append name
        if (name != null) {
            bf.append(name);
        }

        // append extension if there is one
        if (ext != null) {
            bf.append('.');
            bf.append(ext);
        }

        return findResource(bf.toString());
    }

    /** Finds a file given its full resource path.
    * @param name the resource path, e.g. "dir/subdir/file.ext" or "dir/subdir" or "dir"
    * @return a file object with the given path or
    *   <CODE>null</CODE> if no such file exists
    */
    public abstract FileObject findResource(String name);

    /** Returns temporary folder if it is avaliable on this file system.
     * Method never returns null. IOException is thrown instead.
     * @return a file object for temporary folder
     * @throws IOException 
     * @since 7.60
     */
    public FileObject getTempFolder() throws IOException {
        throw new IOException("Unsupported operation"); // NOI18N
    }

    /** Creates temporary file in the given parent folder.
     * Method never returns null. IOException is thrown instead.
     * @param parent the parent folder where temporary file will be created
     * @param prefix prefix of the name of created file
     * @param suffix suffix of the name of created file
     * @param deleteOnExit delete file on exit
     * @return new temporary file
     * @throws IOException 
     * @since 7.60
     */
    public FileObject createTempFile(FileObject parent, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        throw new IOException("Unsupported operation"); // NOI18N
    }
        
    /** Finds various extensions for set of file objects coming from
     * this file system.
     * For example actions should be obtainable as:<pre>
     * actions = fs.{@link #findExtrasFor(java.util.Set) findExtrasFor}(foSet).{@link Lookup#lookupAll(java.lang.Class) lookupAll}({@link javax.swing.Action});
     * </pre>
     * @param objects the set of objects
     * @return the lookup providing various extensions (usually visual) 
     * for these objects
     * @since 8.12
     */
    public Lookup findExtrasFor(Set<FileObject> objects) {
        return new FileExtrasLkp(this, objects);
    }

    /** Reads object from stream and creates listeners.
    * @param in the input stream to read from
    * @exception IOException error during read
    * @exception ClassNotFoundException when class not found
    */
    @SuppressWarnings("deprecation")
    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException, java.lang.ClassNotFoundException {
        in.defaultReadObject();
    }

    @Override
    public String toString() {
        return getSystemName() + "[" + super.toString() + "]"; // NOI18N
    }
    
    private static volatile Lookup.Result<StatusDecorator> statusResult;
    
    private static StatusDecorator defaultStatus() {
        if (statusResult == null) {
            statusResult = Lookup.getDefault().lookupResult(StatusDecorator.class);
        }
        Iterator<? extends StatusDecorator> it = statusResult.allInstances().iterator();
        return it.hasNext() ? it.next() : SFS_STATUS;
    }

    /**
     * Gets a status object that can annotate a set of files by changing the names or icons
     * associated with them.
     * <p>
     * The default implementation returns a status object making no modifications,
     * unless this {@link #isDefault()} in which case certain special attributes
     * will be honored as of org.openide.filesystems 7.25:
     * <dl>
     * <dt>{@code displayName}</dt>
     * <dd>Value of <a href="https://github.com/apache/netbeans/tree/master/platform/openide.filesystems.nb/src/org/netbeans/modules/openide/filesystems/FileSystemStatus.java">FileSystemStatus#annotateName</a>. Often used with {@code bundlevalue} in an {@link XMLFileSystem}.</dd>
     * <dt>{@code SystemFileSystem.localizingBundle}</dt>
     * <dd>Name of a bundle (as per {@link NbBundle#getBundle(String)}) in which to look up a display name.
     * The bundle key is the {@link FileObject#getPath}.
     * {@code displayName} is preferred for new code.</dd>
     * <dt>{@code iconBase}</dt>
     * <dd>Resource path to icon for <a href="https://github.com/apache/netbeans/tree/master/platform/openide.filesystems.nb/src/org/netbeans/modules/openide/filesystems/FileSystemStatus.java">FileSystemStatus#annotateIcon</a>.
     * {@code _32} will be inserted before the file suffix for 32x32 icons.</dd>
     * <dt>{@code SystemFileSystem.icon} and {@code SystemFileSystem.icon32}</dt>
     * <dd>Icon specified directly as a {@link URL} (usually {@code nbresloc} protocol)
     * or {@link Image}. {@code iconBase} is preferred for new code.</dd>
     * </dl>
     * @return the status object for this filesystem
     */
    public StatusDecorator getDecorator() {
        return isDefault() ? defaultStatus() : STATUS_NONE;
    }
    
    /** Executes atomic action. The atomic action represents a set of
    * operations constituting one logical unit. It is guaranteed that during
    * execution of such an action no events about changes in the filesystem
    * will be fired.
    * <P>
    * <em>Warning:</em> the action should not take a significant amount of time, and should finish as soon as
    * possible--otherwise all event notifications will be blocked.
    * <p><strong>Warning:</strong> do not be misled by the name of this method;
    * it does not require the filesystem to treat the changes as an atomic block of
    * commits in the database sense! That is, if an exception is thrown in the middle
    * of the action, partial results will not be undone (in general this would be
    * impossible to implement for all filesystems anyway).
    * @param run the action to run
    * @exception IOException if there is an <code>IOException</code> thrown in the actions' {@link AtomicAction#run run}
    *    method
    */
    public final void runAtomicAction(final AtomicAction run)
    throws IOException {
        getEventControl().runAtomicAction(run);
    }

    /**
     * Begin of block, that should be performed without firing events.
     * Firing of events is postponed after end of block .
     * There is strong necessity to use always both methods: beginAtomicAction
     * and finishAtomicAction. It is recomended use it in try - finally block.
     * @param run Events fired from this atomic action will be marked as events
     * that were fired from this run.
     */
    void beginAtomicAction(FileSystem.AtomicAction run) {
        getEventControl().beginAtomicAction(run);
    }

    void beginAtomicAction() {
        beginAtomicAction(null);
    }

    /**
     * End of block, that should be performed without firing events.
     * Firing of events is postponed after end of block .
     * There is strong necessity to use always both methods: beginAtomicAction
     * and finishAtomicAction. It is recomended use it in try - finally block.
     */
    void finishAtomicAction() {
        getEventControl().finishAtomicAction();
    }

    /**
     *  Inside atomicAction adds an event dispatcher to the queue of FS events
     *  and firing of events is postponed. If not event handlers are called directly.
     * @param run dispatcher to run
     */
    void dispatchEvent(EventDispatcher run) {
        getEventControl().dispatchEvent(run);
    }

    private final EventControl getEventControl() {
        EventControl evnCtrl = thrLocal.get();

        if (evnCtrl == null) {
            thrLocal.set(evnCtrl = new EventControl());
        }

        return evnCtrl;
    }

    /** Registers FileStatusListener to receive events.
    * The implementation registers the listener only when getStatus () is
    * overriden to return a special value.
    *
    * @param listener The listener to register.
    */
    public final void addFileStatusListener(FileStatusListener listener) {
        synchronized (internLock) {
            // JST: Ok? Do not register listeners when the fs cannot change status?
            if (getDecorator() == STATUS_NONE) {
                return;
            }

            if (fileStatusList == null) {
                fileStatusList = new ListenerList<FileStatusListener>();
            }

            fileStatusList.add(listener);
        }
    }

    /** Removes FileStatusListener from the list of listeners.
     *@param listener The listener to remove.
     */
    public final void removeFileStatusListener(FileStatusListener listener) {
        if (fileStatusList == null) {
            return;
        }

        fileStatusList.remove(listener);
    }

    /** Notifies all registered listeners about change of status of some files.
    *
    * @param event The event to be fired
    */
    protected final void fireFileStatusChanged(FileStatusEvent event) {
        if (fileStatusList == null) {
            return;
        }

        List<FileStatusListener> listeners = fileStatusList.getAllListeners();
        dispatchEvent(new FileStatusDispatcher(listeners, event));
    }

    /** Adds listener for the veto of property change.
    * @param listener the listener
    */
    public final void addVetoableChangeListener(VetoableChangeListener listener) {
        synchronized (internLock) {
            if (vetoableChangeList == null) {
                vetoableChangeList = new ListenerList<VetoableChangeListener>();
            }

            vetoableChangeList.add(listener);
        }
    }

    /** Removes listener for the veto of property change.
    * @param listener the listener
    */
    public final void removeVetoableChangeListener(VetoableChangeListener listener) {
        if (vetoableChangeList == null) {
            return;
        }

        vetoableChangeList.remove(listener);
    }

    /** Fires property vetoable event.
    * @param name name of the property
    * @param o old value of the property
    * @param n new value of the property
    * @exception PropertyVetoException if an listener vetoed the change
    */
    protected final void fireVetoableChange(String name, Object o, Object n)
    throws PropertyVetoException {
        if (vetoableChangeList == null) {
            return;
        }

        PropertyChangeEvent e = null;

        for (VetoableChangeListener l : vetoableChangeList.getAllListeners()) {
            if (e == null) {
                e = new PropertyChangeEvent(this, name, o, n);
            }

            l.vetoableChange(e);
        }
    }

    /** Registers PropertyChangeListener to receive events.
    *@param listener The listener to register.
    */
    public final void addPropertyChangeListener(PropertyChangeListener listener) {
        synchronized (internLock) {
            if (changeSupport == null) {
                changeSupport = new PropertyChangeSupport(this);
            }
        }

        changeSupport.addPropertyChangeListener(listener);
    }

    /** Removes PropertyChangeListener from the list of listeners.
    *@param listener The listener to remove.
    */
    public final void removePropertyChangeListener(PropertyChangeListener listener) {
        if (changeSupport != null) {
            changeSupport.removePropertyChangeListener(listener);
        }
    }

    /** Fires property change event.
    * @param name name of the property
    * @param o old value of the property
    * @param n new value of the property
    */
    protected final void firePropertyChange(String name, Object o, Object n) {
        firePropertyChange(name, o, n, null);
    }

    final void firePropertyChange(String name, Object o, Object n, Object propagationId) {
        if (changeSupport == null) {
            return;
        }

        if ((o != null) && (n != null) && o.equals(n)) {
            return;
        }

        PropertyChangeEvent e = new PropertyChangeEvent(this, name, o, n);
        e.setPropagationId(propagationId);
        changeSupport.firePropertyChange(e);
    }

    /** Notifies this filesystem that it has been added to the repository.
    * Various initialization tasks could go here. The default implementation does nothing.
    * <p>Note that this method is <em>advisory</em> and serves as an optimization
    * to avoid retaining resources for too long etc. Filesystems should maintain correct
    * semantics regardless of whether and when this method is called.
    */
    public void addNotify() {
    }

    /** Notifies this filesystem that it has been removed from the repository.
    * Concrete filesystem implementations could perform clean-up here.
    * The default implementation does nothing.
    * <p>Note that this method is <em>advisory</em> and serves as an optimization
    * to avoid retaining resources for too long etc. Filesystems should maintain correct
    * semantics regardless of whether and when this method is called.
    */
    public void removeNotify() {
    }

    /** getter for Repository
    * @return Repository that contains this FileSystem or null if FileSystem
    * is not part of any Repository
    */
    final Repository getRepository() {
        return repository;
    }

    void setRepository(Repository rep) {
        repository = rep;
    }

    final FCLSupport getFCLSupport() {
        synchronized (FCLSupport.class) {
            if (fclSupport == null) {
                fclSupport = new FCLSupport();
            }
        }

        return fclSupport;
    }

    /** Add new listener to this object.
    * @param fcl the listener
    * @since 2.8
    */
    public final void addFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().addFileChangeListener(fcl);
    }

    /** Remove listener from this object.
    * @param fcl the listener
    * @since 2.8
    */
    public final void removeFileChangeListener(FileChangeListener fcl) {
        getFCLSupport().removeFileChangeListener(fcl);
    }

    /** A hook for JAR FS */
    void waitRefreshed() {
    }

    /** An action that it is to be called atomically with respect to filesystem event notification.
    * During its execution (via {@link FileSystem#runAtomicAction runAtomicAction})
    * no events about changes in filesystems are fired.
     * <p><strong>Nomenclature warning:</strong> the action is by no means "atomic"
     * in the usual sense of the word, i.e. either running to completion or rolling
     * back. There is no rollback support. The actual semantic property here is
     * close to "isolation" - the action appears as a single operation as far as
     * listeners are concerned - but not quite, since it is perfectly possible for
     * some other thread to see half of the action if it happens to run during
     * that time. Generally it is a mistake to assume that using AtomicAction gives
     * you any kind of consistency guarantees; rather, it avoids producing change
     * events too early and thus causing listener code to run before it should.
    */
    public static interface AtomicAction {
        /** Executed when it is guaranteed that no events about changes
        * in filesystems will be notified.
        *
        * @exception IOException if there is an error during execution
        */
        public void run() throws IOException;
    }
    
    static interface AsyncAtomicAction extends AtomicAction {
        boolean isAsynchronous();
    }

    /** Class used to notify events for the filesystem.
    */
    abstract static class EventDispatcher extends Object implements Runnable {
        public final void run() {
            dispatch(false, null);
        }

        /** @param onlyPriority if true then invokes only priority listeners
         *  else all listeners are invoked.
         */
        protected abstract void dispatch(boolean onlyPriority, Collection<Runnable> postNotify);

        /** @param propID  */
        protected abstract void setAtomicActionLink(EventControl.AtomicActionLink propID);
    }

    private static class FileStatusDispatcher extends EventDispatcher {
        private List<FileStatusListener> listeners;
        private FileStatusEvent fStatusEvent;

        public FileStatusDispatcher(List<FileStatusListener> listeners, FileStatusEvent fStatusEvent) {
            this.listeners = listeners;
            this.fStatusEvent = fStatusEvent;
        }

        protected void dispatch(boolean onlyPriority, Collection<Runnable> postNotify) {
            if (onlyPriority) {
                return;
            }

            for (FileStatusListener fStatusListener : listeners) {
                fStatusListener.annotationChanged(fStatusEvent);
            }
        }

        protected void setAtomicActionLink(EventControl.AtomicActionLink propID) {
            /** empty no fireFrom in FileStatusEvent*/
        }
    }

    @SuppressWarnings("FieldMayBeFinal")
    private static StatusDecorator SFS_STATUS = new StatusDecorator() {

        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            // no HTML annotation
            return null;
        }

        @Override
        public String annotateName(String s, Set<? extends FileObject> files) {
            // Look for a localized file name.
            // Note: all files in the set are checked. But please only place the attribute
            // on the primary file, and use this primary file name as the bundle key.
            for (FileObject fo : files) {
                // annotate a name
                String displayName = annotateName(fo);
                if (displayName != null) {
                    return displayName;
                }
            }
            return s;
        }

        private final String annotateName(FileObject fo) {
            String bundleName = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
            if (bundleName != null) {
                try {
                    bundleName = BaseUtilities.translate(bundleName);
                    ResourceBundle b = NbBundle.getBundle(bundleName);
                    try {
                        return b.getString(fo.getPath());
                    } catch (MissingResourceException ex) {
                        // ignore--normal
                    }
                } catch (MissingResourceException ex) {
                    Exceptions.attachMessage(ex, warningMessage(bundleName, fo));
                    LOG.log(Level.INFO, null, ex);
                    // ignore
                }
            }
            return (String) fo.getAttribute("displayName"); // NOI18N
        }

        private String warningMessage(String name, FileObject fo) {
            Object by = fo.getAttribute("layers"); // NOI18N
            if (by instanceof Object[]) {
                by = Arrays.toString((Object[]) by);
            }
            return "Cannot load " + name + " for " + fo + " defined by " + by; // NOI18N
        }
    };
}
