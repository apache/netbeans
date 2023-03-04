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

package org.netbeans.modules.java.source.usages;

import com.sun.tools.javac.api.ClientCodeWrapper;
import java.net.URISyntaxException;
import org.netbeans.modules.java.preprocessorbridge.spi.VirtualSourceProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.tools.JavaFileObject;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.ForwardingPrefetchableJavaFileObject;
import org.netbeans.modules.java.source.parsing.PrefetchableJavaFileObject;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
public final class VirtualSourceProviderQuery {
    private VirtualSourceProviderQuery () {}
    
    private static final Lookup.Result<VirtualSourceProvider> result = Lookup.getDefault().lookupResult(VirtualSourceProvider.class);
    private static Map<String,VirtualSourceProvider> ext2prov;
    private static final LookupListener l = new LookupListener() {
        public void resultChanged(LookupEvent ev) {
            reset();
        }
    };
    
    static {
        result.addLookupListener(WeakListeners.create(LookupListener.class, l, result));
    }
    
    public static boolean hasVirtualSource (final File file) {
        Parameters.notNull("file", file);
        final String ext = FileObjects.getExtension(file.getName());
        return getExt2ProvMap().containsKey(ext);
    }
    
    public static boolean hasVirtualSource (final FileObject file) {
        Parameters.notNull("file", file);
        final String ext = file.getExt();
        return getExt2ProvMap().containsKey(ext);
    }
    
    public static boolean hasVirtualSource (final String extension) {
        Parameters.notNull("extension", extension);
        return getExt2ProvMap().containsKey(extension);
    }
    
    public static boolean hasVirtualSource (final Indexable indexable) {
        Parameters.notNull("indexable", indexable);
        URL url = indexable.getURL();
        if (url == null) {
            // Issue #168179: This is probably deleted source file. Just skipping.
            return false;
        }
        final String extension = FileObjects.getExtension(url.getFile());
        return hasVirtualSource(extension);
    }
    
    public static Collection<? extends CompileTuple> translate (final Iterable<? extends Indexable> indexables, final File root) throws IOException {
        Parameters.notNull("files", indexables);     //NOI18N
        Parameters.notNull("root", root);       //NOI18N
        final Map<String,Pair<VirtualSourceProvider,List<File>>> m = new HashMap<String,Pair<VirtualSourceProvider,List<File>>>();
        final Map<String,VirtualSourceProvider> e2p = getExt2ProvMap();
        final Map<File,Indexable> file2indexables = new HashMap<File, Indexable>();
        for (Indexable indexable : indexables) {

           final String ext = FileObjects.getExtension(indexable.getURL().getPath());
           final VirtualSourceProvider prov = e2p.get(ext);
           if (prov != null) {
               Pair<VirtualSourceProvider,List<File>> p = m.get(ext);
               List<File> l = null;
               if (p == null) {
                   l = new LinkedList<File>();
                   m.put(ext, Pair.of(prov, l));
               }
               else {
                   l = p.second();
               }
               try {
                   final File file = BaseUtilities.toFile(indexable.getURL().toURI());
                   l.add(file);
                   file2indexables.put(file, indexable);
               } catch (URISyntaxException use) {
                   final IOException ioe = new IOException();
                   ioe.initCause(use);
                   throw ioe;
               }
           }
        }
        
        final R r = new R (root, file2indexables);
        for (Pair<VirtualSourceProvider,List<File>> p : m.values()) {
            final VirtualSourceProvider prov = p.first();
            final List<File> tf = p.second();
            r.setProvider(prov);
            prov.translate(tf, root,r);
        }
        return r.getResult();
    }

    private static Map<String,VirtualSourceProvider> getExt2ProvMap () {
        synchronized (VirtualSourceProviderQuery.class) {
            if (ext2prov != null) {
                return ext2prov;
            }
        }
        final Collection<? extends VirtualSourceProvider> allInstances = new LinkedList<VirtualSourceProvider>(result.allInstances());
        synchronized (VirtualSourceProviderQuery.class) {
            if (ext2prov == null) {            
                ext2prov = new HashMap<String, VirtualSourceProvider>();
                for (VirtualSourceProvider vsp : allInstances) {
                    for (String ext : vsp.getSupportedExtensions()) {
                        ext2prov.put(ext, vsp);
                    }
                }
            }
            return ext2prov;
        }
    }

    private static synchronized void reset () {
        ext2prov = null;
    }
    
    private static class R implements VirtualSourceProvider.Result {
        
        private final File root;
        private final Map<? extends File,Indexable> file2indexables;
        private final String rootURL;
        private VirtualSourceProvider currentProvider;
        final List<CompileTuple> res = new LinkedList<CompileTuple>();
        
        public R (final File root, final Map<? extends File, Indexable> file2indexables) throws IOException {
            assert root != null;
            assert file2indexables != null;
            this.root = root;
            String _rootURL = BaseUtilities.toURI(root).toURL().toString();
            if (!_rootURL.endsWith("/")) {   //NOI18N
                _rootURL = _rootURL + '/';    //NOI18N
            }
            this.rootURL = _rootURL;
            this.file2indexables = file2indexables;
        }
        
        public List<CompileTuple> getResult () {
            this.currentProvider = null;
            return res;
        }
        
        void setProvider (final VirtualSourceProvider provider) {
            assert provider != null;
            this.currentProvider = provider;
        }                

        public void add(final File source, final String packageName, final String relativeName, final CharSequence content) {
            try {
                final Indexable indexable = this.file2indexables.get(source);
                assert indexable != null : "Unknown file: " + source.getAbsolutePath();
                final String baseName = relativeName + '.' + FileObjects.getExtension(source.getName());
                String folder = FileObjects.convertPackage2Folder(packageName);
                if (folder.length() > 0) {
                    folder += '/';
                }
                res.add(new CompileTuple(
                        new ForwardingPrefetchableJavaFileObjectImpl(
                        FileObjects.memoryFileObject(packageName,
                            baseName,new URI(rootURL + folder + baseName),
                            System.currentTimeMillis(), content)),
                        indexable,true, this.currentProvider.index()));
            } catch (URISyntaxException ex) {
                Exceptions.printStackTrace(ex);
            }
        }                

        @ClientCodeWrapper.Trusted
        static class ForwardingPrefetchableJavaFileObjectImpl extends ForwardingPrefetchableJavaFileObject {

            public ForwardingPrefetchableJavaFileObjectImpl(PrefetchableJavaFileObject pjfo) {
                super(pjfo);
            }

            @Override
            public JavaFileObject.Kind getKind() {
                return JavaFileObject.Kind.SOURCE;
            }
        }
    }
}
