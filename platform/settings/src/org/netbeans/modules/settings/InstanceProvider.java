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

package org.netbeans.modules.settings;

import java.beans.PropertyChangeEvent;
import java.lang.ref.SoftReference;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.settings.convertors.SerialDataNode;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.cookies.SaveCookie;
import org.openide.cookies.InstanceCookie;

import org.netbeans.spi.settings.Convertor;
import org.openide.util.Exceptions;

/** Provides the Lookup content.
 *
 * @author  Jan Pokorsky
 */
final class InstanceProvider extends org.openide.filesystems.FileChangeAdapter
implements java.beans.PropertyChangeListener, FileSystem.AtomicAction {
    /** Logging for events in this class */
    private static final Logger LOG = Logger.getLogger(InstanceProvider.class.getName()); // NOI18N
    
    /** container handling objects provided by {@link #lookup} */
    private final org.openide.util.lookup.InstanceContent lkpContent;
    /** container exposing setting to the outside world */
    private final org.openide.util.Lookup lookup;
    private final org.openide.loaders.DataObject dobj;
    private final FileObject settingFO;
    private final FileObject providerFO;
    private final NodeConvertor node;
    //save support
    private SaveSupport saver;
    private SaveCookie scCache;
    private boolean wasReportedProblem = false;
    private java.util.Set<String> instanceOfSet;
    private String instanceClassName;
    /** lock used to sync read/write operations for .settings file */
    final Object READWRITE_LOCK = new Object();
    
    /** Creates a new instance of InstanceCooikeProvider */
    public InstanceProvider(org.openide.loaders.DataObject dobj, FileObject providerFO) {
//        System.out.println("new IP: " + dobj);
        this.settingFO = dobj.getPrimaryFile();
        this.providerFO = providerFO;
        this.dobj = dobj;
        
        settingFO.addFileChangeListener(
            FileUtil.weakFileChangeListener(this, settingFO));
        
        lkpContent = new org.openide.util.lookup.InstanceContent();
        lkpContent.add(createInstance(null));
        node = new NodeConvertor();
        lkpContent.add(this, node);
        lookup = new org.openide.util.lookup.AbstractLookup(lkpContent);
    }
    
    /** provides content like InstanceCookie, SaveCokie */
    public Lookup getLookup() {
        return lookup;
    }
    /** file contanining various attributes related to setting like convertor
     * class, ...
     */
    FileObject getProvider() {
        return providerFO;
    }
    /** file containing a persisted setting object */
    FileObject getFile () {
        return settingFO;
    }
    
    org.openide.loaders.DataObject getDataObject() {
        return dobj;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null) return;

        String name = evt.getPropertyName();
        if (name == null)
            return;
        else if (name == SaveSupport.PROP_SAVE)
            provideSaveCookie();
        else if (name == SaveSupport.PROP_FILE_CHANGED) {
            synchronized (this) {
                instanceOfSet = null;
            }
            instanceCookieChanged(null);
        }
    }
    
    /** process events coming from the file object*/
    public void fileChanged(org.openide.filesystems.FileEvent fe) {
        if (saver != null && fe.firedFrom((FileSystem.AtomicAction) saver.getSaveCookie())) return;
        propertyChange(new PropertyChangeEvent(this, SaveSupport.PROP_FILE_CHANGED, null, null));
    }
  
    public void fileDeleted(org.openide.filesystems.FileEvent fe) {
        if (saver != null && fe.firedFrom((FileSystem.AtomicAction) saver.getSaveCookie())) return;
        releaseInstance();
    }
    
    
    /** allow to listen on changes of the object inst; should be called when
     * new instance is created */
    private synchronized void attachToInstance(Object inst) {
        if (saver != null) {
            saver.removePropertyChangeListener(this);
            getScheduledRequest().forceToFinish();
        }
        saver = createSaveSupport(inst);
        saver.addPropertyChangeListener(this);
    }
    
    /** create own InstanceCookie implementation */
    private InstanceCookie.Of createInstance(Object inst) {
        return new InstanceCookieImpl(inst);
    }
    
    /** method provides a support storing the setting */
    private SaveSupport createSaveSupport(Object inst) {
        return new SaveSupport(this, inst);
    }
    
    private void provideSaveCookie() {
        SaveCookie scNew = saver.getSaveCookie();
        if (scCache != null) {
            if (!saver.isChanged()) {
                if (LOG.isLoggable(Level.FINE)) LOG.fine("remove save cookie: " + dobj); // NOI18N
                lkpContent.remove(scCache);
                scCache = null;
                return;
            }
        } else {
            if (saver.isChanged()) {
                scCache = scNew;
                if (LOG.isLoggable(Level.FINE)) LOG.fine("add save cookie: " + dobj + " cookie: " + scNew); // NOI18N
                lkpContent.add(scNew);
                return;
            }
        }
    }
    
    private void releaseInstance() {
        SaveSupport _saver = saver;
        if (_saver != null) {
            _saver.removePropertyChangeListener(this);
        }
        
        if (scCache != null) {
            if (LOG.isLoggable(Level.FINE)) LOG.fine("release instance and remove save cookie: " + dobj); // NOI18N
            lkpContent.remove(scCache);
            getScheduledRequest().cancel();
            scCache = null;
        }
        
        lkpContent.remove(this, node);
    }
    
    private void instanceCookieChanged(Object inst) {
        if (LOG.isLoggable(Level.FINE)) LOG.fine("instanceCookieChanged: " + dobj + " inst: " + inst); // NOI18N
        releaseInstance();
        
        lkpContent.add(this, node);
        
        Object ic = lookup.lookup(InstanceCookie.class);
        lkpContent.remove(ic);

        Object newCookie = createInstance(inst);
        lkpContent.add(newCookie);
        if (LOG.isLoggable(Level.FINE)) LOG.fine("cookie replaced: " + dobj + " old: " + ic + " new: " + newCookie); // NOI18N
    }
    
    private Convertor convertor;
    
    /** find out  proper convertor */
    Convertor getConvertor() throws IOException {
        if (convertor == null) {
            Object attrb = providerFO.getAttribute(Env.EA_CONVERTOR);
            if (!(attrb instanceof Convertor)) {
                throw new IOException("cannot create convertor: " + attrb + ", provider:" +providerFO); //NOI18N
            }
            convertor = (Convertor) attrb;
        }
        return convertor;
    }
    
    /** find out setting object class name */
    private synchronized String getInstanceClassName() {
        if (instanceClassName == null) {
            Object name = providerFO.getAttribute(Env.EA_INSTANCE_CLASS_NAME);
            if (name instanceof String) {
                instanceClassName = org.openide.util.Utilities.translate((String) name);
            } else {
                instanceClassName = null;
            }
        }
        return instanceClassName;
    }
    
    public String toString() {
        return this.getClass().getName() + '@' +
            Integer.toHexString(System.identityHashCode(this)) +
            '[' + getDataObject() + ", " + getProvider() + ']';
    }
    
    
    /** called by ScheduledRequest in order to perform the request */
    public void run() throws IOException {
        saver.writeDown();
    }
    
    /** scheduled request to store setting */
    private ScheduledRequest request;
    
    /** get the scheduled request to store setting */
    synchronized ScheduledRequest getScheduledRequest() {
        if (request == null) {
            request = new ScheduledRequest(settingFO, this);
        }
        return request;
    }
    
    // InstanceCookieImpl
    /** InstanceCookie implementation. */
    final class InstanceCookieImpl implements InstanceCookie.Of {
        private SoftReference<Object> cachedInstance;// = new SoftReference(null);
        
        public InstanceCookieImpl(Object inst) {
            setCachedInstance(inst);
        }
        
        public Class instanceClass() throws IOException, ClassNotFoundException {
            String name = getInstanceClassName();
            if (name == null) {
		Object instanceCreate = instanceCreate();
		if(instanceCreate != null) {
		    return instanceCreate.getClass();
		}
		if (LOG.isLoggable(Level.FINE)) {
		    LOG.log(Level.FINE, "instance could not be created for: {0}", getInstanceClassName()); // NOI18N
		}
		return null;
            } else {
                return ((ClassLoader)Lookup.getDefault().lookup(ClassLoader.class)).loadClass(name);
            }
        }

        public Object instanceCreate() throws java.io.IOException, ClassNotFoundException {
            Object inst;
            
            synchronized (this) {
                inst = getCachedInstance();
                if (inst != null) return inst;
            }
            
            try {
                synchronized (READWRITE_LOCK) {
                    java.io.Reader r = ContextProvider.createReaderContextProvider(
                        new InputStreamReader(settingFO.getInputStream(), StandardCharsets.UTF_8), getFile());
                    inst = getConvertor().read(r);
                }
            } catch (IOException ex) {
                throw (IOException) Exceptions.attachLocalizedMessage(ex,
                                                  InstanceProvider.this.toString());
            } catch (ClassNotFoundException ex) {
                throw (ClassNotFoundException) Exceptions.attachLocalizedMessage(ex,
                                                  InstanceProvider.this.toString());
            }
            
            synchronized (this) {
                Object existing = getCachedInstance();
                if (existing != null) return existing;
                setCachedInstance(inst);
            }
            attachToInstance(inst);
            
            return inst;
        }

        public String instanceName() {
            String name = getInstanceClassName();
            if (name != null) return name;
            
            Exception e = null;
            try {
		Class instanceClass = instanceClass();
		if(instanceClass != null) {
		    return instanceClass.getName();
		}
            } catch (IOException ex) {
                e = ex;
            } catch (ClassNotFoundException ex) {
                e = ex;
            }
            if (e != null && !wasReportedProblem) {
                wasReportedProblem = true;
                Exceptions.attachLocalizedMessage(e, dobj.toString());
                Logger.getLogger(InstanceProvider.class.getName()).log(Level.WARNING, null, e);
            }
            return "Unknown"; // NOI18N
        }

        public boolean instanceOf(Class<?> type) {
            synchronized (InstanceProvider.this) {
                if (instanceOfSet == null) {
                    instanceOfSet = Env.parseAttribute(providerFO.getAttribute(Env.EA_INSTANCE_OF));
                    java.util.Iterator<String> it = instanceOfSet.iterator();
                    instanceOfSet = new java.util.HashSet<String>(instanceOfSet.size() * 5 / 4);
                    while (it.hasNext()) {
                        instanceOfSet.add(org.openide.util.Utilities.translate(it.next()));
                    }
                }
            }
            if (instanceOfSet.isEmpty()) {
                Exception e = null;
                try {
		    Class instanceClass = instanceClass();
		    if(instanceClass != null) {
			return type.isAssignableFrom(instanceClass);
		    }
                } catch (IOException ex) {
                    e = ex;
                } catch (ClassNotFoundException ex) {
                    e = ex;
                }
                if (e != null && !wasReportedProblem) {
                    wasReportedProblem = true;
                    Exceptions.attachLocalizedMessage(e, dobj.toString());
                    Logger.getLogger(InstanceProvider.class.getName()).log(Level.WARNING, null, e);
                }
                return false;
            } else {
                return instanceOfSet.contains(type.getName());
            }
        }
        
        // called by InstanceDataObject to set new object
        public void setInstance(Object inst, boolean save) throws IOException {
            instanceCookieChanged(inst);
            if (inst != null) {
                attachToInstance(inst);
                if (save) getScheduledRequest().runAndWait();
            }
        }
    
        private Object getCachedInstance() {
            return cachedInstance.get();
        }
        private void setCachedInstance(Object inst) {
            cachedInstance = new SoftReference<Object>(inst);
        }
    }
    
// NodeConvertor
    /** allow to postpone the node creation */
    private static final class NodeConvertor 
            implements org.openide.util.lookup.InstanceContent.Convertor<InstanceProvider, Node> {
        NodeConvertor() {}
     
        public Node convert(InstanceProvider o) {
            return new SerialDataNode(o.getDataObject());
        }
     
        public Class<Node> type(InstanceProvider o) {
            return Node.class;
        }
     
        public String id(InstanceProvider o) {
            // Generally irrelevant in this context.
            return o.toString();
        }
     
        public String displayName(InstanceProvider o) {
            // Again, irrelevant here.
            return o.toString();
        }
     
    }
}
