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

package org.netbeans.modules.java.editor.fold;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.modules.parsing.api.Source;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Loads I18N strings from property files, keeps cached content, fires aggregated change events.
 * 
 * @author sdedic
 */
public final class ResourceStringLoader {
    private static final Logger LOG = Logger.getLogger(ResourceStringLoader.class.getName());

    private static final AtomicInteger counter = new AtomicInteger();
    
    /**
     * Cached contents of the bundle files
     */
    private final Map<FileObject, Holder>     resourceContents = new WeakHashMap<>(5);
    private final int no;
    private ChangeListener              l;
    
    /**
     * Initializes a loader. The passed ChangeListener is informed after some of the loaded files change,
     * subsequent {@link #getMessage} calls will returned strings from a reloaded content
     * @param l callback listener
     */
    public ResourceStringLoader(ChangeListener l) {
        this.l = l;
        this.no = counter.incrementAndGet();
    }
    
    private void fireStateChanged() {
        ChangeListener l = this.l;
        if (l != null) {
            l.stateChanged(new ChangeEvent(this));
        }
    }
    
    public void retainFiles(Collection<FileObject>  files) {
        synchronized (this) {
            
        }
    }
    
    /**
     * Retrieves a message with key 'key' from the specified FileObject. Contents of files are loaded
     * on demand; if the file is opened in an editor, the edited (possibly modified) version is used.
     * 
     * @param f the FileObject
     * @param key message key
     * @return message content, or {@code null} if the message is not present, or the resource could not be loaded.
     */
    public String getMessage(FileObject f, String key) {
        Holder h;
        synchronized (this) {
            h = resourceContents.get(f);

            if (h == null) {
                LOG.fine(no + ": Getting contents for file " + f);
                resourceContents.put(f, h = CACHE.getProperties(f, this));
            }
        }
        return h.getContent().getProperty(key);
    }
    
    /**
     * A Holder is created for each file. It watches its FileObject and/or Document if it is opened
     * using EditorCookie.Observable.
     * TODO - make a SourceChangeSupport abstract inheritable class.
     */
    private static class Holder extends FileChangeAdapter implements DocumentListener, PropertyChangeListener {
        // held weakly from Cache
        private final FileObject   file;
        
        private final Cache cache;
        // @GuardedBy(this)
        private final Collection<Reference<ResourceStringLoader>>  toNotify = new ArrayList<>(5);
        // @GuardedBy(this)
        private DocumentListener docWL;
        // @GuardedBy(this)
        private volatile Properties content;
        
        Holder(Cache c, FileObject f) {
            this.cache = c;
            this.file = f;
            
            try {
                DataObject d = DataObject.find(f);
                EditorCookie.Observable obs = d.getLookup().lookup(EditorCookie.Observable.class);
                if (obs != null) {
                    obs.addPropertyChangeListener(WeakListeners.propertyChange(this, obs));
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        void unregister(ResourceStringLoader ldr) {
            synchronized (this) {
                toNotify.add(new WeakReference<>(ldr));
            }
        }
        
        Properties getContent() {
            Properties p = content;
            if (p != null) {
                return p;
            }
            synchronized (this) {
                if (content == null) {
                    content = cache.loadProperties(file);
                }
                return content;
            }
        }
        
        @Override
        public void fileRenamed(FileRenameEvent fe) {
            invalidate((FileObject)fe.getSource());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            invalidate((FileObject)fe.getSource());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            synchronized (this) {
                // watching document instead
                if (docWL == null) {
                    return;
                }
            }
            invalidate((FileObject)fe.getSource());
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            invalidate(extractFileObject(e.getDocument()));
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            invalidate(extractFileObject(e.getDocument()));
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
        }
        
        private FileObject extractFileObject(Document d) {
            if (d == null) {
                return null;
            }
            Object o = d.getProperty(Document.StreamDescriptionProperty);
            if (o instanceof FileObject) {
                return (FileObject)o;
            } else if (o instanceof DataObject) {
                return ((DataObject)o).getPrimaryFile();
            } else {
                return null;
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            FileObject closedFile = null;
            
            if (!EditorCookie.Observable.PROP_DOCUMENT.equals(evt.getPropertyName())) {
                return;
            }
            synchronized (this) {
                Document old = (Document)evt.getOldValue();
                if (old != null && docWL != null) {
                    old.removeDocumentListener(docWL);
                    docWL = null;
                }
                Document nue = (Document)evt.getNewValue();
                if (nue != null) {
                    nue.addDocumentListener(docWL = WeakListeners.document(this, nue));
                } else {
                    closedFile = extractFileObject(old);
                }
            }
            if (closedFile != null) {
                invalidate(closedFile);
            }
        }
        
        private void invalidate(FileObject myFile) {
            this.content = null;
            cache.invalidate(myFile);
        }
        
        private Collection<ResourceStringLoader> collectLoaders() {
            List<ResourceStringLoader> clients;
            synchronized (this) {
                clients = new ArrayList<>(toNotify.size());
                for (Iterator<Reference<ResourceStringLoader>> it = toNotify.iterator(); it.hasNext(); ) {
                    Reference<ResourceStringLoader> ref = it.next();
                    ResourceStringLoader cl = ref.get();
                    if (cl == null) {
                        it.remove();
                    } else {
                        clients.add(cl);
                    }
                }
            }
            return clients;
        }
        
        void attach(ResourceStringLoader l) {
            synchronized (this) {
                toNotify.add(new WeakReference<>(l));
            }
        }
        
        public String toString() {
            return "[ResourceHolder for " + file + "]";
        }
    }
    
    private static final RequestProcessor REFRESHER = new RequestProcessor(ResourceStringLoader.class);
    
    private static final Cache CACHE = new Cache();

    /**
     * Caches contents of .properties files
     */
    private static class Cache implements Runnable {
        private final Map<FileObject, Reference<Holder>> loaded = new WeakHashMap<>(5);
        
        /**
         * FileObjects which changed. The content is flushed immediately in Holder, but
         * change events are fired in a request processor in a hope that mutliple changes may be
         * coalesced into one change event.
         */
        private Set<FileObject> invalid = new HashSet<>();
        
        Holder getProperties(FileObject file, ResourceStringLoader loader) {
            Holder h;
            synchronized (loaded) {
                Reference<Holder> refH = loaded.get(file);
                if (refH == null || (h = refH.get()) == null) {
                    h = new Holder(this, file);
                    // TODO poll into active queue to ensure cleanup
                    loaded.put(file, new WeakReference<>(h));
                }
            }
            if (loader != null) {
                h.attach(loader);
            }
            return h;
        }
        
        private void invalidate(FileObject f) {
            synchronized (loaded) {
                invalid.add(f);
                REFRESHER.post(this, CACHE_REFRESH_TIMEOUT);
            }
        }

        @Override
        public void run() {
            Set<FileObject> invalidate;
            Collection<Holder> fireHolders;
            synchronized (loaded) {
                fireHolders = new ArrayList<>(invalid.size());
                invalidate = invalid;
                LOG.fine("Invalidating " + invalidate.size() + " resources");
                invalid = new HashSet<>();
                for (FileObject f : invalidate) {
                    Reference<Holder> ref = loaded.get(f);
                    Holder h = ref != null ? ref.get() : null;
                    if (h == null) {
                        LOG.fine("Found expired holder for " + f);
                        loaded.remove(f);
                    } else {
                        fireHolders.add(h);
                    }
                }
            }
            Set<ResourceStringLoader> loaders = new HashSet<>();
            for (Holder h : fireHolders) {
                loaders.addAll(h.collectLoaders());
            }
            LOG.fine("Invalidating " + loaders.size() + " resource loaders");
            for (ResourceStringLoader l : loaders) {
                l.fireStateChanged();
            }
        }
        
        public static final int CACHE_REFRESH_TIMEOUT = 500;
        
        private Properties loadProperties(FileObject f) {
            Source s = Source.create(f);
            final Document doc = s.getDocument(false);
            final Properties props = new Properties();
            if (doc == null) {
                LOG.fine("Loading properties from " + f);
                try (InputStream is = f.getInputStream()) {
                    props.load(is);
                } catch (IOException ex) {
                    // no op
                }
            } else {
                LOG.fine("Loading properties from document " + doc + " created from " + f);
                doc.render(new Runnable() {
                    public void run() {
                        int l = doc.getLength();
                        try {
                            String s = doc.getText(0, l);
                            props.load(new StringReader(s));
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IOException ex) {
                        }
                    }
                });
            }
            return props;
        }
    }
}
