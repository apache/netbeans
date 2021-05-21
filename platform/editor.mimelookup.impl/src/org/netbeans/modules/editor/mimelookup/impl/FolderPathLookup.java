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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.spi.CustomInstanceFactory;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author vita
 */
public final class FolderPathLookup extends AbstractLookup {
    
    /**
     * Attribute that points to the original file in datashadow. See RecognizedInstanceFiles
     * in openide.filesystems.
     */
    private static final String ATTR_ORIGINAL_FILE = "originalFile"; // NOI18N
    
    /**
     * Extension of DataShadows, keep in sync with RecognizedInstanceFiles in openide.filesystems
     */
    private static final String EXTENSION_SHADOW = "shadow"; // NOI18N
    
    private static final String ATTR_INSTANCE_OF = "instanceOf"; // NOI18N

    private static final Logger LOG = Logger.getLogger(FolderPathLookup.class.getName());
    
    private final InstanceContent content;
    
    private final CompoundFolderChildren children;

    private final PCL listener = new PCL();
    
    /**
     * Map holding fileobject to created instance pairs. Once the clients stop
     * referencing the created instance the whole item gets removed from the cache.
     * This ensures that the particular file object always gives the same instance
     * (e.g. when registered for empty mime-type the same instance gets returned
     * when asked for both empty and non-empty mime-types).
     * This is crucial for some of the MimeLookup clients.
     */
    private static final Map<FileObject,InstanceItem> fo2item = new HashMap<>(128);
    
    static InstanceItem getInstanceItem(FileObject fo, InstanceItem ignoreItem) {
        FileObject real = fo;
        if (EXTENSION_SHADOW.equals(fo.getExt())) {
            Object originalFile = fo.getAttribute(ATTR_ORIGINAL_FILE);
            if (originalFile instanceof String) {
                FileObject r;
                try {
                    r = fo.getFileSystem().getRoot().getFileObject(originalFile.toString());
                    if (r != null) {
                        real = r;
                    } else {
                        LOG.warning("Dangling shadow found: " + fo.getPath() + " -> " + originalFile); // NOI18N
                    }
                } catch (FileStateInvalidException ex) {
                    LOG.log(Level.WARNING, "Unexpected error accessing config file " + fo, ex); // NOI18N
                }
            }
        }
        synchronized (fo2item) {
            InstanceItem item = fo2item.get(fo);
            if (item == null || item == ignoreItem) {
                // resolve potential shadows:
                item = new InstanceItem(real);
                fo2item.put(fo, item);
            }
            return item;
        }
    }

    static void releaseInstanceItem(InstanceItem item) {
        synchronized (fo2item) {
            // Optimistically suppose the value in the map is the removed one.
            InstanceItem removed = fo2item.remove(item.getFileObject());
            if (removed != item) {
                fo2item.put(item.getFileObject(), removed);
            }
        }
    }
    
    /** Creates a new instance of InstanceProviderLookup */
    public FolderPathLookup(String [] paths) {
        this(paths, new InstanceContent());
    }
    
    private FolderPathLookup(String [] paths, InstanceContent content) {
        super(content);
        
        this.content = content;
        
        this.children = new CompoundFolderChildren(paths, false);
        this.children.addPropertyChangeListener(listener);
        
        rebuild();
    }

    private void rebuild() {
        List<PairItem> pairItems = new ArrayList<PairItem>();
        for (FileObject fo : children.getChildren()) {
            if (!fo.isValid()) {
                // Can happen after modules are disabled. Ignore it.
                continue;
            }
            pairItems.add(new PairItem(fo));
        }
        content.setPairs(pairItems);
    }
    
    private class PCL implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            rebuild();
        }
    } // End of PCL class
    
    private static final class PairItem extends AbstractLookup.Pair<Object> {
        
        private final InstanceItem instanceItem;
        
        PairItem(FileObject fo) {
            instanceItem = getInstanceItem(fo, null);
            assert (instanceItem != null) : "InstanceItem must not be null";
        }

        @Override
        protected boolean instanceOf(Class<?> c) {
            return instanceItem.instanceOf(c);
        }

        @Override
        protected boolean creatorOf(Object obj) {
            return instanceItem.creatorOf(obj);
        }

        @Override
        public Object getInstance() {
            return instanceItem.getInstance();
        }

        @Override
        public Class<? extends Object> getType() {
            return instanceItem.getType();
        }

        @Override
        public String getId() {
            return instanceItem.getId();
        }

        @Override
        public String getDisplayName() {
            return instanceItem.getDisplayName();
        }

        @Override
        public boolean equals(Object obj) {
            // Transferred from RecognizeInstanceFiles.FOItem
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final PairItem other = (PairItem) obj;
            return instanceItem.equals(other.instanceItem);
        }

        @Override
        public int hashCode() {
            // Transferred from RecognizeInstanceFiles.FOItem
            int hash = 3;
            hash = 11 * hash + instanceItem.hashCode();
            return hash;
        }

    }
    
    private static volatile Lookup.Result<CustomInstanceFactory> factories;
    
    private static Collection<? extends CustomInstanceFactory> getInstanceFactories() {
        Lookup.Result<CustomInstanceFactory> v = factories;
        if (v != null) {
            return v.allInstances();
        }
        final Lookup.Result<CustomInstanceFactory>[] fr = new Lookup.Result[1];
        // ensure the system - global Lookup is used
        Lookups.executeWith(null, new Runnable() {
            public void run() {
                fr[0] = factories = Lookup.getDefault().lookupResult(CustomInstanceFactory.class);
            }
        });
        return fr[0].allInstances();
    }
            
    public static final <T> T createInstance(Class<T> type) throws InstantiationException, 
            IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        T r = null;
        for (CustomInstanceFactory fif : getInstanceFactories()) {
            r = fif.createInstance(type);
            if (r != null) {
                break;
            }
        }
        if (r == null) {
            Constructor<T> init = type.getDeclaredConstructor();
            init.setAccessible(true);
            r = init.newInstance();
        }
        return r;
    }

    /**
     * Item referencing a file object and object instance that was created from it.
     * <br/>
     * Once the instance gets released the item will be removed from cache.
     */
    private static final class InstanceItem {

        static final long serialVersionUID = 10L;
        
        private final FileObject fo;
        
        /** reference to created object */
        private transient Reference<Object> ref;

        /** Constructs new item. */
        InstanceItem (FileObject fo) {
            assert (fo != null) : "FileObject must not be null";
            this.fo = fo;
        }
        
        FileObject getFileObject() {
            return fo;
        }
        
        private synchronized Reference<Object> getRef() {
            return ref;
        }
        
        private synchronized void setRef(Reference<Object> ref) {
            this.ref = ref;
        }

        protected boolean instanceOf(Class<?> c) {
            Reference<Object> refL = getRef();
            Object inst = (refL != null) ? refL.get() : null;
            if (inst != null) {
                return c.isInstance(inst);
            } else {
                String instanceOf = (String) fo.getAttribute(ATTR_INSTANCE_OF);
                if (instanceOf != null) {
                    for (String xface : instanceOf.split(",")) {
                        try {
                            if (c.isAssignableFrom(Class.forName(xface, false, loader()))) {
                                return true;
                            }
                        } catch (ClassNotFoundException x) {
                            // Not necessarily a problem, e.g. from org-netbeans-lib-commons_net-antlibrary.instance
                            LOG.log(Level.FINE, "could not load " + xface + " for " + fo.getPath(), x);
                        }
                    }
                    return false;
                } else {
                    return c.isAssignableFrom(getType());
                }
            }
        }

        protected boolean creatorOf(Object obj) {
            Reference<Object> refL = getRef();
            return (refL != null) ? refL.get() == obj : false;
        }

        public synchronized Object getInstance() {
            Reference<Object> refL = getRef();
            Object inst = null;
            if (refL != null) {
                inst = refL.get();
                if (inst == null) { // Instance already released -> get a fresh item
                    return getInstanceItem(fo, this).getInstance();
                }
            }
            if (inst == null) {
                inst = createInstanceFor(fo, Object.class);
                if (inst != null) {
                    setRef(new Ref(inst));
                }
            }
            return inst;
        }

        public Class<? extends Object> getType() {
            Class<? extends Object> type = findTypeFor(fo);
            return type != null ? type : Void.class;
        }

        public String getId() {
            String s = fo.getPath();
            if (s.endsWith(".instance")) { // NOI18N
                s = s.substring(0, s.length() - ".instance".length());
            }
            return s;
        }

        public String getDisplayName() {
            String n = fo.getName();
            try {
                n = fo.getFileSystem().getDecorator().annotateName(n, Collections.singleton(fo));
            } catch (FileStateInvalidException ex) {
                LOG.log(Level.WARNING, ex.getMessage(), ex);
            }
            return n;
        }
        
        public @Override boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final InstanceItem other = (InstanceItem) obj;

            if (this.fo != other.fo &&
                (this.fo == null || !this.fo.equals(other.fo)))
                return false;
            return true;
        }

        public @Override int hashCode() {
           return fo.hashCode();
        }
        
        private static ClassLoader loader() {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = InstanceItem.class.getClassLoader();
            }
            return l;
        }

        static <T> T createInstanceFor(FileObject f, Class<T> resultType) {
            Object inst = f.getAttribute("instanceCreate");
            if (inst == null) {
                try {
                    Class<?> type = findTypeFor(f);
                    if (type == null) {
                        return null;
                    }
                    inst = createInstance(type);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return resultType.isInstance(inst) ? resultType.cast(inst) : null;
        }

        private static Class<? extends Object> findTypeFor(FileObject f) {
            String clazz = getClassName(f);
            if (clazz == null) {
                return null;
            }
            try {
                return Class.forName(clazz, false, loader());
            } catch (ClassNotFoundException ex) {
                LOG.log(Level.FINE, ex.getMessage(), ex);
                return null;
            }
        }
        /** get class name from specified file object*/
        private static String getClassName(FileObject fo) {
            // first of all try "instanceClass" property of the primary file
            Object attr = fo.getAttribute ("instanceClass");
            if (attr instanceof String) {
                return BaseUtilities.translate((String) attr);
            } else if (attr != null) {
                LOG.warning(
                    "instanceClass was a " + attr.getClass().getName()); // NOI18N
            }

            attr = fo.getAttribute("instanceCreate");
            if (attr != null) {
                return attr.getClass().getName();
            } else {
                Enumeration<String> attributes = fo.getAttributes();
                while (attributes.hasMoreElements()) {
                    if (attributes.nextElement().equals("instanceCreate")) {
                        // It was specified, just unloadable (usually a methodvalue).
                        return null;
                    }
                }
            }

            // otherwise extract the name from the filename
            String name = fo.getName ();

            int first = name.indexOf('[') + 1;
            if (first != 0) {
                LOG.log(Level.WARNING, "Cannot understand {0}", fo);
            }

            int last = name.indexOf (']');
            if (last < 0) {
                last = name.length ();
            }

            // take only a part of the string
            if (first < last) {
                name = name.substring (first, last);
            }

            name = name.replace ('-', '.');
            name = BaseUtilities.translate(name);

            return name;
        }
        
        void release() {
            releaseInstanceItem(this);
        }
        
        private final class Ref extends WeakReference<Object> implements Runnable {
            
            Ref(Object inst) {
                super(inst, BaseUtilities.activeReferenceQueue());
            }

            @Override
            public void run() {
                release();
            }
            
        }

    }
        
}
