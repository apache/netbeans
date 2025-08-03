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

package org.netbeans.modules.settings.convertors;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.logging.Level;
import javax.swing.JComponent;
import org.netbeans.modules.settings.Env;
import org.netbeans.modules.settings.ScheduledRequest;
import org.netbeans.spi.settings.Convertor;
import org.netbeans.spi.settings.Saver;
import org.openide.cookies.InstanceCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.Environment;
import org.openide.loaders.InstanceDataObject;
import org.openide.modules.ModuleInfo;
import org.openide.modules.Modules;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.SharedClassObject;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

/** Convertor handles serialdata format described in
 * http://www.netbeans.org/dtds/sessionsettings-1_0.dtd. The convertor replaces
 * the old org.netbeans.core.projects.SerialDataConvertor.
 * @author  Jan Pokorsky
 */
public final class SerialDataConvertor extends FileChangeAdapter
implements PropertyChangeListener, FileSystem.AtomicAction {
    /** data object name cached in the attribute to prevent instance creation when
     * its node is displayed.
     * @see org.openide.loaders.InstanceDataObject#EA_NAME
     */
    static final String EA_NAME = "name"; // NOI18N
    /** lock used to sync read/write operations for .settings file */
    final Object READWRITE_LOCK = new Object();
    private final InstanceContent lkpContent;
    private final Lookup lookup;
    private final DataObject dobj;
    private final FileObject provider;
    private final SerialDataConvertor.NodeConvertor node;
    private SerialDataConvertor.SettingsInstance instance;
    private SaveSupport saver;
    
    /** Creates a new instance of SDConvertor */
    public SerialDataConvertor(DataObject dobj, FileObject provider) {
        this.dobj = dobj;
        this.provider = provider;
        lkpContent = new InstanceContent();
        
        FileObject fo = dobj.getPrimaryFile();
        fo.addFileChangeListener(FileUtil.weakFileChangeListener(this, fo));
        
        SerialDataConvertor.SettingsInstance si = createInstance(null);
        if (isModuleEnabled(si)) {
            // 137240 - if instanceName is empty string, it doesn't provide instance
            if (si.instanceName().length() > 0) {
                instance = si;
                lkpContent.add(instance);
            }
        }
        lkpContent.add(this);
        node = new SerialDataConvertor.NodeConvertor();
        lkpContent.add(this, node);
        lookup = new AbstractLookup(lkpContent);
    }
    
    /** can store an object inst in the serialdata format
     * @param w stream into which inst is written
     * @param inst the setting object to be written
     * @exception IOException if the object cannot be written
     */
    public void write(Writer w, Object inst) throws IOException {
        XMLSettingsSupport.storeToXML10(inst, w, Modules.getDefault().ownerOf(inst.getClass()));
    }
    
    /** delegate to SaveSupport to handle an unfired setting object change
     * @see SerialDataNode#resolvePropertyChange
     */
    void handleUnfiredChange() {
        saver.propertyChange(null);
    }
    
    DataObject getDataObject() {
        return dobj;
    }
    
    FileObject getProvider() {
        return provider;
    }
    
    /** provides content like InstanceCookie, SaveCokie */
    public final Lookup getLookup() {
        return lookup;
    }
    
    /** create own InstanceCookie implementation */
    private SettingsInstance createInstance(Object inst) {
        return new SettingsInstance(inst);
    }
    
    /** method provides a support storing the setting */
    private SaveSupport createSaveSupport(Object inst) {
        return new SaveSupport(inst);
    }
    
    /** allow to listen on changes of the object inst; should be called when
     * new instance is created */
    private void attachToInstance(Object inst) {
        SerialDataConvertor.SaveSupport _saver = null;
        synchronized (this) {
            if (saver != null) {
                saver.removePropertyChangeListener(this);
                _saver = saver;
            }
        }    
        
        if (_saver != null) {
            /** creates new Thread and waits for finish - danger of deadlock, 
             * then called outside of lock*/            
            _saver.flush();
        }

        synchronized (this) {
            saver = createSaveSupport(inst);
            saver.addPropertyChangeListener(this);
        }
    }
    
    private void provideSaveCookie() {
        if (saver.isChanged()) {
            lkpContent.add(saver);
        } else {
            lkpContent.remove(saver);
        }
    }
    
    private void instanceCookieChanged(Object inst) {
        if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("instanceCookieChanged: " + this.dobj); // NOI18N
        if (saver != null) {
            if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("canceling saver: " + this.dobj); // NOI18N
            saver.removePropertyChangeListener(this);
            getScheduledRequest().cancel();
            saver = null;
        }

        SerialDataConvertor.SettingsInstance si = createInstance(inst);

        //#34155 - is this already instantiated SystemOption?
        boolean recreate = false;
        if (instance != null && instance.getCachedInstance() != null) {
            if (isSystemOption(instance.getCachedInstance())) {
                recreate = true;
            }
        }
        if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("need recreate: " + recreate + " for " + this.dobj); // NOI18N
        if (isModuleEnabled(si) && si.instanceName().length() > 0) {  // 137240 - if instanceName is empty string, it doesn't provide instance
            instance = si;
            lkpContent.set(Arrays.asList(new Object [] { this, si }), null);
            if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("module enabled: " + this.dobj); // NOI18N
        } else {
            lkpContent.set(Collections.singleton(this), null);
            instance = null;
            if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("module disabled: " + this.dobj + "  or instanceName is empty: " + si.instanceName()); // NOI18N
        }
        
        lkpContent.add(this, node);
        
        //#34155 - if it was instantiated SystemOptions then force its recreation
        // See issue for more details.
        if (isModuleEnabled(si) && recreate) {
            if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("recreating: " + this.dobj); // NOI18N
            try {
                if(instance != null) {
                    instance.instanceCreate();
                }
            } catch (Exception ex) {
                XMLSettingsSupport.err.log(Level.WARNING, null, ex);
            }
        }
        if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("done: " + this.dobj); // NOI18N
    }
    
    private static boolean isSystemOption(final Object obj) {
        boolean b =  false;
        if (obj instanceof SharedClassObject){
            for(Class c = obj.getClass(); !b && c != null; c = c.getSuperclass()) {
                b =  "org.openide.options.SystemOption".equals(c.getName());//NOI18N
            }
        }
        return b;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;

        String name = evt.getPropertyName();
        if (name == null)
            return;
        // setting was changed
        else if (name == SaveSupport.PROP_SAVE)
            provideSaveCookie();
        // .settings file was changed
        else if (name == SaveSupport.PROP_FILE_CHANGED) {
            miUnInitialized = true;
            if (moduleCodeBase != null) {
                ModuleInfo mi = ModuleInfoManager.getDefault().getModule(moduleCodeBase);
                ModuleInfoManager.getDefault().
                    unregisterPropertyChangeListener(this, mi);
            }
            instanceCookieChanged(null);
        } else if(ModuleInfo.PROP_ENABLED.equals(evt.getPropertyName())) {
            instanceCookieChanged(null);
        }
    }

    /** process events coming from the file object*/
    public void fileChanged(FileEvent fe) {
        if (saver != null && fe.firedFrom(saver)) return;
        propertyChange(new PropertyChangeEvent(this, SaveSupport.PROP_FILE_CHANGED, null, null));
    }
    
    public void fileDeleted(FileEvent fe) {
        if (saver != null && fe.firedFrom(saver)) return;
        if (saver != null) {
            saver.removePropertyChangeListener(this);
            getScheduledRequest().cancel();
            saver = null;
        }
    }
    
    private String moduleCodeBase = null;
    private boolean miUnInitialized = true;
    private boolean moduleMissing;    
    
    private boolean isModuleEnabled(SerialDataConvertor.SettingsInstance si) {
        ModuleInfo mi = null;
        if (miUnInitialized) {
            moduleCodeBase = getModuleCodeNameBase(si);
            miUnInitialized = false;
            if (moduleCodeBase != null) {
                mi = ModuleInfoManager.getDefault().getModule(moduleCodeBase);
                moduleMissing = (mi == null);
                if (mi != null) {
                    ModuleInfoManager.getDefault().
                        registerPropertyChangeListener(this, mi);
                } else {
                    XMLSettingsSupport.err.warning(
                        "Warning: unknown module code base: " + // NOI18N
                        moduleCodeBase + " in " +  // NOI18N
                        getDataObject().getPrimaryFile());
                }
            } else {
                moduleMissing = false;
            }
        } else {
            mi = ModuleInfoManager.getDefault().getModule(moduleCodeBase);
        }
        
        return !moduleMissing && (mi == null || mi.isEnabled());
    }
    
    private String getModuleCodeNameBase(SerialDataConvertor.SettingsInstance si) {
        try {
            String module = si.getSettings(true).getCodeNameBase();
            return module;
        } catch (IOException ex) {
            XMLSettingsSupport.err.log(Level.WARNING, null, ex);
        }
        return null;
    }
    
    /** Little utility method for posting an exception
     *  to the default <CODE>ErrorManager</CODE> with severity
     *  <CODE>ErrorManager.INFORMATIONAL</CODE>
     */
    static void inform(Throwable t) {
        XMLSettingsSupport.err.log(Level.WARNING, null, t);
    }
    
    /** called by ScheduledRequest in order to perform the request */
    public void run() throws IOException {
	if(saver != null) {
	    saver.writeDown();
	}
    }
    
    /** scheduled request to store setting */
    private ScheduledRequest request;
    
    /** get the scheduled request to store setting */
    private synchronized ScheduledRequest getScheduledRequest() {
        if (request == null) {
            request = new ScheduledRequest(this.getDataObject().getPrimaryFile(), this);
        }
        return request;
    }
    
    // SettingsInstance
    /** InstanceCookie implementation */
    private final class SettingsInstance implements InstanceCookie.Of {
        
        /** created instance   */
        private SoftReference<Object> inst;
        
        /* Lifecycle of SettingsRecognizer:
         * Initially: settings = null
         * Parsed header: settings created, light object (no byte[], no char[])
         * Full parsing: Create char[], convert it to byte[] and release char[]
         *   create instance, throw away settings
         *
         */
        /** holder of parsed settings  */
        private XMLSettingsSupport.SettingsRecognizer settings = null;
        
        /** Creates new SettingsInstance   */
        public SettingsInstance(Object instance) {
            setCachedInstance(instance);
        }
        
        /** Getter for parsed settings
         * @param header if <code>true</code> parse just header(instanceof, module, classname)
         */
        private XMLSettingsSupport.SettingsRecognizer getSettings(boolean header) throws IOException {
            synchronized (this) {
                if (settings == null) {
                    synchronized (READWRITE_LOCK) {
                        settings = new XMLSettingsSupport.SettingsRecognizer(
                            header, getDataObject().getPrimaryFile());
                        settings.parse();
                    }
                    return settings;
                }
                if (!header) {
                    if (!settings.isAllRead()) {
                        settings.setAllRead(false);
                        settings.parse();
                    }
                }

                return settings;
            }
        }
        
        public Object instanceCreate() throws IOException, ClassNotFoundException {
            Object inst;
            XMLSettingsSupport.SettingsRecognizer recog;
            
            synchronized (this) {
                inst = getCachedInstance();
                if (inst != null) {
                    if (XMLSettingsSupport.err.isLoggable(Level.FINE)) XMLSettingsSupport.err.fine("Cached instance1: " + inst); // NOI18N
                    return inst;
                }
            }
            
            recog = getSettings(false);
            inst = recog.instanceCreate();
            
            
            synchronized (this) {
                Object existing = getCachedInstance();
                if (existing != null) {
                    if (XMLSettingsSupport.err.isLoggable(Level.FINER)) XMLSettingsSupport.err.finer("Cached instance2: " + existing); // NOI18N
                    return existing;
                }
                setCachedInstance(inst);
            }
            if (XMLSettingsSupport.err.isLoggable(Level.FINER)) XMLSettingsSupport.err.finer("Attached to instance: " + inst); // NOI18N
            attachToInstance(inst);
            
            return inst;
        }
        
        public Class instanceClass() throws IOException, ClassNotFoundException {
            // cached
            Object inst = getCachedInstance();
            if (inst != null) {
                return inst.getClass();
            }
            
            XMLSettingsSupport.SettingsRecognizer recog = getSettings(false);
            return recog.instanceClass();
        }
        
        public boolean instanceOf(Class<?> type) {
            try {
                if (
                    moduleCodeBase != null && 
                    ModuleInfoManager.getDefault().isReloaded(moduleCodeBase) &&
                    type.getClassLoader () != ClassLoader.getSystemClassLoader () &&
                    type.getClassLoader() != null
                ) {
                    // special treatment for classes that could be reloaded
                    ModuleInfo info = ModuleInfoManager.getDefault().getModule (moduleCodeBase);
                    if (info == null || !info.isEnabled ()) {
                        // false to disabled modules
                        return false;
                    }
                    // otherwise really try to load
                    Class<?> instanceType = instanceClass ();
                    return type.isAssignableFrom (instanceType);
                }
                
                // check existing instance first:
                Object inst = getCachedInstance();
                if (inst != null) {
                    return type.isInstance(inst);
                }
                
                // check the settings cache/file
                return getSettings(true).getInstanceOf().contains(type.getName());
            } catch (ClassNotFoundException ex) {
                XMLSettingsSupport.err.log(Level.WARNING, getDataObject().getPrimaryFile().toString());
                inform(ex);
            } catch (IOException ex) {
                XMLSettingsSupport.err.log(Level.WARNING, getDataObject().getPrimaryFile().toString());
                inform(ex);
            }
            return false;
        }
        
        public String instanceName() {
            // try cached instance
            Object inst = getCachedInstance();
            if (inst != null) {
                return inst.getClass().getName();
            }
            
            try {
                return getSettings(true).instanceName();
            } catch (IOException ex) {
                XMLSettingsSupport.err.warning(getDataObject().getPrimaryFile().toString());
                inform(ex);
                return ""; // NOI18N
            }
        }
        
        private Object getCachedInstance() {
            return inst.get();
        }
        
        private void setCachedInstance(Object o) {
            inst = new SoftReference<Object>(o);
            settings = null; // clear reference to settings
        }
        // called by InstanceDataObject to set new object
        public void setInstance(Object inst, boolean save) throws IOException {
            instanceCookieChanged(inst);
            if (inst != null) {
                attachToInstance(inst);
                if (save) getScheduledRequest().runAndWait();                
            }
        }
        
    }
    
    /** Support handles automatic setting objects storing and allows to identify
     * the origin of file events fired as a consequence of this storing
     */
    private final class SaveSupport implements FileSystem.AtomicAction,
    SaveCookie, PropertyChangeListener, Saver {
        /** property means setting is changed and should be changed */
        public static final String PROP_SAVE = "savecookie"; //NOI18N
        /** property means setting file content is changed */
        public static final String PROP_FILE_CHANGED = "fileChanged"; //NOI18N
        
        /** support for PropertyChangeListeners */
        private PropertyChangeSupport changeSupport;
        /** the number of registered PropertyChangeListeners */
        private int propertyChangeListenerCount = 0;
        
        /** setting is already changed */
        private boolean isChanged = false;
        /** file containing persisted setting */
        private final FileObject file;
        /** weak reference to setting object */
        private final WeakReference<Object> instance;
        /** remember whether the DataObject is a template or not; calling isTemplate() is slow  */
        private Boolean knownToBeTemplate = null;
        /** the setting object is serialized, if true ignore prop. change
         * notifications
         */
        private boolean isWriting = false;
        /** convertor for possible format upgrade */
        private Convertor convertor;
        
        /** Creates a new instance of SaveSupport  */
        public SaveSupport(Object inst) {
            this.instance = new WeakReference<Object>(inst);
            file = getDataObject().getPrimaryFile();
        }
        
        /** is setting object changed? */
        public final boolean isChanged() {
            return isChanged;
        }
        
        /** store setting or provide just SaveCookie? */
        private boolean acceptSave() {
            Object inst = instance.get();
            if (!(inst instanceof Serializable) ||
                // XXX bad dep; should perhaps have some marker in the .settings file for this??
                inst instanceof TopComponent) return false;
            
            return true;
        }
        
        /** place where to filter events comming from setting object */
        private boolean ignoreChange(PropertyChangeEvent pce) {
            if (isChanged || isWriting || !getDataObject().isValid()) return true;
            
            // undocumented workaround used in 3.3; since 3.4 convertors make
            // possible to customize the setting change notification filtering 
            if (pce != null && Boolean.FALSE.equals(pce.getPropagationId())) return true;
            
            if (knownToBeTemplate == null) knownToBeTemplate = getDataObject().isTemplate() ? Boolean.TRUE : Boolean.FALSE;
            return knownToBeTemplate.booleanValue();
        }
        
        /** get convertor for possible upgrade; can be null */
        private Convertor getConvertor() {
            return convertor;
        }

        /** try to find out convertor for possible upgrade and cache it; can be null */
        private Convertor initConvertor() {
            Object inst = instance.get();
            if (inst == null) {
                throw new IllegalStateException(
                    "setting object cannot be null: " + getDataObject());// NOI18N
            }

            try {
                FileObject newProviderFO = Env.findProvider(inst.getClass());
                if (newProviderFO != null) {
                    FileObject foEntity = Env.findEntityRegistration(newProviderFO);
                    if (foEntity == null) foEntity = newProviderFO;
                    Object attrb = foEntity.getAttribute(Env.EA_PUBLICID);
                    if (!(attrb instanceof String)) {
                        throw new IOException("missing or invalid attribute: " + //NOI18N
                            Env.EA_PUBLICID + ", provider: " + foEntity); //NOI18N
                    }
                    if (XMLSettingsSupport.INSTANCE_DTD_ID.equals(attrb)) {
                        convertor = null;
                        return convertor;
                    }
                    
                    attrb = newProviderFO.getAttribute(Env.EA_CONVERTOR);
                    if (!(attrb instanceof Convertor)) {
                        throw new IOException("cannot create convertor: " + //NOI18N
                            attrb + ", provider: " + newProviderFO); //NOI18N
                    } else {
                        convertor = (Convertor) attrb;
                        return convertor;
                    }
                }
            } catch (IOException ex) {
                inform(ex);
            }
            return convertor;
        }
        
        /** Registers PropertyChangeListener to receive events and initialize
         * listening to events comming from the setting object and file object.
         * @param listener The listener to register.
         */
        public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
            if (changeSupport == null || propertyChangeListenerCount <= 0) {
                Object inst = instance.get();
                if (inst == null) return;
                if (changeSupport == null) {
                    changeSupport = new PropertyChangeSupport(this);
                    propertyChangeListenerCount = 0;
                }
                Convertor conv = initConvertor();
                if (conv != null) {
                    conv.registerSaver(inst, this);
                } else {
                    registerPropertyChangeListener(inst);
                }
            }
            propertyChangeListenerCount++;
            changeSupport.addPropertyChangeListener(listener);
        }
        
        /** Removes PropertyChangeListener from the list of listeners.
         * @param listener The listener to remove.
         */
        public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
            if (changeSupport == null)
                return;
            
            propertyChangeListenerCount--;
            changeSupport.removePropertyChangeListener(listener);

            if (propertyChangeListenerCount == 0) {
                Object inst = instance.get();
                if (inst == null) return;
                
                Convertor conv = getConvertor();
                if (conv != null) {
                    conv.unregisterSaver(inst, this);
                } else {
                    unregisterPropertyChangeListener(inst);
                }
            }
        }
        
        /** try to register PropertyChangeListener to the setting object
         * to be notified about its changes.
         */
        private void registerPropertyChangeListener(Object inst) {
            if (inst instanceof SharedClassObject) {
                ((SharedClassObject)inst).addPropertyChangeListener(this);
            }
            else if (inst instanceof JComponent) {
                ((JComponent) inst).addPropertyChangeListener(this);
            }
            else {
                // add propertyChangeListener
                try {
                    Method method = inst.getClass().getMethod(
                        "addPropertyChangeListener", // NOI18N
                        new Class[] {PropertyChangeListener.class});
                    method.invoke(inst, new Object[] {this});
                } catch (NoSuchMethodException ex) {
                    // just changes done through gui will be saved
                    if (XMLSettingsSupport.err.isLoggable(Level.FINE)) {
                        XMLSettingsSupport.err.warning(
                        "NoSuchMethodException: " + // NOI18N
                        inst.getClass().getName() + ".addPropertyChangeListener"); // NOI18N
                    }
                } catch (IllegalAccessException ex) {
                    // just changes done through gui will be saved
                    XMLSettingsSupport.err.warning("Instance: " + inst); // NOI18N
                    XMLSettingsSupport.err.log(Level.WARNING, null, ex);
                } catch (java.lang.reflect.InvocationTargetException ex) {
                    // just changes done through gui will be saved
                    XMLSettingsSupport.err.log(Level.WARNING, null, ex.getTargetException());
                }
            }
        }
        
        /** @see #registerPropertyChangeListener
         */
        private void unregisterPropertyChangeListener(Object inst) {
            try {
                Method method = inst.getClass().getMethod(
                    "removePropertyChangeListener", // NOI18N
                    new Class[] {PropertyChangeListener.class});
                method.invoke(inst, new Object[] {this});
            } catch (NoSuchMethodException ex) {
                // just changes done through gui will be saved
                if (XMLSettingsSupport.err.isLoggable(Level.FINE)) {
                    XMLSettingsSupport.err.log(Level.FINE, 
                    "NoSuchMethodException: " + // NOI18N
                    inst.getClass().getName() + ".removePropertyChangeListener"); // NOI18N
                }
            } catch (IllegalAccessException ex) {
                XMLSettingsSupport.err.log(Level.WARNING, "Instance: " + inst); // NOI18N
                XMLSettingsSupport.err.log(Level.WARNING, null, ex);
            } catch (java.lang.reflect.InvocationTargetException ex) {
                XMLSettingsSupport.err.log(Level.WARNING, null, ex.getTargetException());
            }
        }
        
        /** Notifies all registered listeners about the event.
         * @param event The event to be fired
         * @see #PROP_FILE_CHANGED
         * @see #PROP_SAVE
         */
        private void firePropertyChange(String name) {
            if (changeSupport != null)
                changeSupport.firePropertyChange(name, null, null);
        }

        /** force to finish scheduled request */
        public void flush() {
            getScheduledRequest().forceToFinish();
        }
        
        private ByteArrayOutputStream buf;

        /** process events coming from a setting object */
        public final void propertyChange(PropertyChangeEvent pce) {
            if (ignoreChange(pce)) return ;
            isChanged = true;
            firePropertyChange(PROP_SAVE);
            if (acceptSave()) {
                getScheduledRequest().schedule(instance.get());
            }
        }
        
        public void markDirty() {
            if (ignoreChange(null)) return;
            isChanged = true;
            firePropertyChange(PROP_SAVE);
        }
        
        public void requestSave() throws IOException {
            if (ignoreChange(null)) return;
            isChanged = true;
            firePropertyChange(PROP_SAVE);
            getScheduledRequest().schedule(instance.get());
        }

        /** store buffer to the file. */
        public void run() throws IOException {
            if (!getDataObject().isValid()) {
                //invalid data object cannot be used for storing
                if (XMLSettingsSupport.err.isLoggable(Level.FINE)) {
                    XMLSettingsSupport.err.fine("invalid data object cannot be used for storing " + getDataObject()); // NOI18N
                }
                return;
            }
            
            try {
                try2run();
            } catch (IOException ex) {
                //#25288: DO can be invalidated asynchronously (module disabling)
                //then ignore IO exceptions
                if (getDataObject().isValid()) {
                    throw ex;
                } else {
                    return;
                }
            }
        }
        
        /** try to perform atomic action */
        private void try2run() throws IOException {
            FileLock lock;
            OutputStream los;
            synchronized (READWRITE_LOCK) {
                if (XMLSettingsSupport.err.isLoggable(Level.FINER)) {
                    XMLSettingsSupport.err.finer("saving " + getDataObject()); // NOI18N
                }
                lock = getScheduledRequest().getFileLock();
                if (lock == null) return;
                los = file.getOutputStream(lock);

                OutputStream os = new BufferedOutputStream(los, 1024);
                try {
                    buf.writeTo(os);
                    if (XMLSettingsSupport.err.isLoggable(Level.FINER)) {
                        XMLSettingsSupport.err.finer("saved " + dobj); // NOI18N
                    }
                } finally {
                    os.close();
                }
            }
        }

        /** Implementation of SaveCookie.  */
        public void save() throws IOException {
            if (!isChanged) return;
            getScheduledRequest().runAndWait();
        }

        /** store the setting object even if was not changed */
        private void writeDown() throws IOException {
            Object inst = instance.get();
            if (inst == null) return ;
            
            ByteArrayOutputStream b = new ByteArrayOutputStream(1024);
            Writer w = new OutputStreamWriter(b, StandardCharsets.UTF_8);
            try {
                isWriting = true;
                Convertor conv = getConvertor();
                if (conv != null) {
                    conv.write(w, inst);
                } else {
                    write(w, inst);
                }
            } finally {
                w.close();
                isWriting = false;
            }
            isChanged = false;

            buf = b;
            file.getFileSystem().runAtomicAction(this);
            buf = null;
            if (!isChanged) firePropertyChange(PROP_SAVE);
        }                
    }

// Provider
    /** A provider for .settings files  containing serial data format
     * (hexa stream)
     */
    public static final class Provider implements Environment.Provider {
        private final FileObject providerFO;
        
        public static Environment.Provider create(FileObject fo) {
            return new Provider(fo);
        }

        private Provider(FileObject fo) {
            providerFO = fo;
        }

        public Lookup getEnvironment(DataObject dobj) {
            if (!(dobj instanceof InstanceDataObject)) return Lookup.EMPTY;
            return new SerialDataConvertor(dobj, providerFO).getLookup();
        }

    }
    
// NodeConvertor
    /** allow to postpone the node creation */
    private static final class NodeConvertor implements InstanceContent.Convertor<SerialDataConvertor, Node> {
        NodeConvertor() {}
     
        public Node convert(SerialDataConvertor o) {
            return new SerialDataNode(o);
        }
     
        public Class<Node> type(SerialDataConvertor o) {
            return Node.class;
        }
     
        public String id(SerialDataConvertor o) {
            // Generally irrelevant in this context.
            return o.toString();
        }
     
        public String displayName(SerialDataConvertor o) {
            // Again, irrelevant here.
            return o.toString();
        }
     
    }
    
}
