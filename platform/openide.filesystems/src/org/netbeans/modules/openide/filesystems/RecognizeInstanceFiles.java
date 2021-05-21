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

package org.netbeans.modules.openide.filesystems;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.spi.CustomInstanceFactory;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.lookup.implspi.NamedServicesProvider;

/** Interface for core/startup to provide lookup over system filesystem.
 *
 * @author Jaroslav Tulach
 */
@ServiceProvider(service=NamedServicesProvider.class, position=500)
public final class RecognizeInstanceFiles extends NamedServicesProvider {
    private static final Logger LOG = Logger.getLogger(RecognizeInstanceFiles.class.getName());
    
    
    @Override
    public Lookup create(String path) {
        return new OverFiles(path);
    }        
    @Override
    public <T> T lookupObject(String path, Class<T> type) {
        FileObject fo = FileUtil.getConfigFile(path);
        if (fo != null && fo.isData()) {
            return FOItem.createInstanceFor(fo, type);
        }
        return null;
    }        
    
    
    private static final class OverFiles extends ProxyLookup 
    implements FileChangeListener {
        private final String path;
        private final FileChangeListener weakL;
        private final AbstractLookup.Content content;
        private final AbstractLookup lkp;
        
        public OverFiles(String path) {
            this(path, new ArrayList<FOItem>(), new AbstractLookup.Content());
        }

        private OverFiles(String path, List<FOItem> items, AbstractLookup.Content cnt) {
            this(path, items, new AbstractLookup(cnt), cnt);
        }
        
        private OverFiles(String path, List<FOItem> items, AbstractLookup lkp, AbstractLookup.Content cnt) {
            super(computeDelegates(path, items, lkp));
            this.path = path;
            this.lkp = lkp;
            this.content = cnt;
            this.content.setPairs(order(items));
            FileSystem fs = null;
            try {
                fs = FileUtil.getSystemConfigRoot().getFileSystem();
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            this.weakL = FileUtil.weakFileChangeListener(this, fs);
            fs.addFileChangeListener(weakL);
        }
        
        private static List<FOItem> order(List<FOItem> items) {
            Map<FileObject,FOItem> m = new LinkedHashMap<FileObject,FOItem>();
            for (FOItem item : items) {
                m.put(item.fo, item);
            }
            List<FileObject> files = FileUtil.getOrder(m.keySet(), true);
            List<FOItem> r = new ArrayList<FOItem>(files.size());
            for (FileObject f : files) {
                r.add(m.get(f));
            }
            return r;
        }
        
        private void refresh(FileEvent ev) {
            if (!(ev.getFile().getPath() + "/").startsWith(path)) {
                return;
            }
            List<FOItem> items = new ArrayList<FOItem>();
            Lookup[] delegates = computeDelegates(path, items, lkp);
            this.content.setPairs(order(items));
            this.setLookups(delegates);
        }
        
        private static Lookup[] computeDelegates(String p, List<FOItem> items, Lookup lkp) {
            FileObject fo = FileUtil.getConfigFile(p);
            List<Lookup> delegates = new LinkedList<Lookup>();
            delegates.add(lkp);
            if (fo != null) {
                for (FileObject f : fo.getChildren()) {
                    if (f.isFolder()) {
                        delegates.add(new OverFiles(f.getPath()));
                    } else {
                        if (f.hasExt("shadow")) {
                            Object real = f.getAttribute("originalFile"); // NOI18N
                            if (real instanceof String) {
                                f = FileUtil.getConfigFile((String)real);
                            }
                        }
                        if (f.hasExt("instance") || f.getAttribute("instanceCreate") != null) {
                            items.add(new FOItem(f));
                        }
                    }
                }

            }
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            if (l == null) {
                l = RecognizeInstanceFiles.class.getClassLoader();
            }
            delegates.add(
                Lookups.metaInfServices(l, "META-INF/namedservices/" + p) // NOI18N
            );
            return delegates.toArray(new Lookup[0]);
        }
    
        public void fileFolderCreated(FileEvent fe) {
            refresh(fe);
        }

        public void fileDataCreated(FileEvent fe) {
            refresh(fe);
        }

        public void fileChanged(FileEvent fe) {
            refresh(fe);
        }

        public void fileDeleted(FileEvent fe) {
            refresh(fe);
        }

        public void fileRenamed(FileRenameEvent fe) {
            refresh(fe);
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            refresh(fe);
        }
    } // end of OverFiles
    
    private static volatile Lookup.Result<CustomInstanceFactory> factories;
    
    private static Collection<? extends CustomInstanceFactory> getInstanceFactories() {
        Lookup.Result<CustomInstanceFactory> fr;
        
        if ((fr = factories) == null) {
            factories = fr = Lookup.getDefault().lookupResult(CustomInstanceFactory.class);
        }
        return fr.allInstances();
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
    
    private static final class FOItem extends AbstractLookup.Pair<Object> {
        private static Reference<Object> EMPTY = new WeakReference<Object>(null);
        private FileObject fo;
        private Reference<Object> ref = EMPTY;
        
        public FOItem(FileObject fo) {
            this.fo = fo;
        }

        protected boolean instanceOf(Class<?> c) {
            Object r = ref.get();
            if (r != null) {
                return c.isInstance(r);
            } else {
                String instanceOf = (String) fo.getAttribute("instanceOf");
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
            return ref.get() == obj;
        }

        public synchronized Object getInstance() {
            Object r = ref.get();
            if (r == null) {
                r = createInstanceFor(fo, Object.class);
                Object o = ref.get();
                if (o != null) {
                    return o;
                }
                if (r != null) {
                    ref = new WeakReference<Object>(r);
                }
            }
            return r;
        }

        static <T> T createInstanceFor(FileObject f, Class<T> resultType) {
            Object r = f.getAttribute("instanceCreate");
            if (r == null) {
                try {
                    Class<?> type = findTypeFor(f);
                    if (type == null) {
                        return null;
                    }
                    r = createInstance(type);
                } catch (InstantiationException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (IllegalAccessException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (NoSuchMethodException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return resultType.isInstance(r) ? resultType.cast(r) : null;
        }

        public Class<? extends Object> getType() {
            Class<? extends Object> type = findTypeFor(fo);
            return type != null ? type : Void.class;
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

            //System.out.println ("Original: " + getPrimaryFile ().getName () + " new one: " + name); // NOI18N
            return name;
        }

        public @Override boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final FOItem other = (FOItem) obj;

            if (this.fo != other.fo &&
                (this.fo == null || !this.fo.equals(other.fo)))
                return false;
            return true;
        }

        public @Override int hashCode() {
            int hash = 3;

            hash = 11 * hash + (this.fo != null ? this.fo.hashCode()
                                                : 0);
            return hash;
        }

        private static ClassLoader loader() {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = FOItem.class.getClassLoader();
            }
            return l;
        }

    } // end of FOItem
    
}
