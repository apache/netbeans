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
package org.netbeans.modules.masterfs.filebasedfs;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.ProvidedExtensionsProxy;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObj;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObjWindows;
import org.netbeans.modules.masterfs.providers.BaseAnnotationProvider;
import org.netbeans.modules.masterfs.providers.ProvidedExtensions;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.StatusDecorator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.BaseUtilities;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Radek Matous
 */
public class FileBasedFileSystem extends FileSystem {
    private static final Logger LOG = Logger.getLogger(FileBasedFileSystem.class.getName());
    private static volatile FileBasedFileSystem INSTANCE;
    private transient RootObj<? extends FileObject> root;
    private final transient StatusImpl status = new StatusImpl();
    private static transient  int modificationInProgress;

    public FileBasedFileSystem() {
        if (BaseUtilities.isWindows()) {
            RootObjWindows realRoot = new RootObjWindows();
            root = new RootObj<RootObjWindows>(realRoot);
        } else {
            FileObjectFactory factory = FileObjectFactory.getInstance(new File("/"));//NOI18N
            root = new RootObj<BaseFileObj>(factory.getRoot());
        }
    }
   
    public static synchronized boolean isModificationInProgress() {
        return modificationInProgress == 0 ? false : true;
    }

    private static synchronized void setModificationInProgress(boolean started) {
        if (started) {
            modificationInProgress++;
        } else {
            modificationInProgress--;
        }
    }

    public static void runAsInconsistent(Runnable r)   {
        try {
            setModificationInProgress(true);
            r.run();
        } finally {
            setModificationInProgress(false);
        }
    }
    
    public static <Retval> Retval runAsInconsistent(FSCallable<Retval> r)  throws IOException {
        Retval retval = null;
        try {
            setModificationInProgress(true);
            retval = r.call();
        } finally {
            setModificationInProgress(false);
        }
        return retval;
    }
    
    public static Map<File, ? extends FileObjectFactory> factories() {
        return FileObjectFactory.factories();
    }        

    public static FileObject getFileObject(final File file) {
        return getFileObject(file, FileObjectFactory.Caller.GetFileObject);
    }
    
    public static FileObject getFileObject(final File file, FileObjectFactory.Caller caller) {
        FileObjectFactory fs = FileObjectFactory.getInstance(file);
        FileObject retval = null;
        if (fs != null) {
            if (file.getParentFile() == null && BaseUtilities.isUnix()) {
                retval = FileBasedFileSystem.getInstance().getRoot();
            } else {
                retval = fs.getValidFileObject(file,caller, true);
            }                
        }         
        return retval;
    }
    
    
    public static FileBasedFileSystem getInstance() {
        FileBasedFileSystem fbfs = INSTANCE;
        if (fbfs == null) {
            final MasterFileSystemFactory fbfsFactory = Lookup.getDefault().lookup(MasterFileSystemFactory.class);
            fbfs = fbfsFactory != null ?
                fbfsFactory.createFileSystem() :
                new FileBasedFileSystem();
            synchronized (FileBasedFileSystem.class) {
                FileBasedFileSystem old = INSTANCE;
                if (old == null) {
                    INSTANCE = fbfs;
                } else {
                    fbfs = old;
                }
            }
        }
        return fbfs;
    }

    @Override
    public void refresh(final boolean expected) {                        
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                refreshImpl(expected);
            }            
        };
        try {
            FileBasedFileSystem.getInstance().runAtomicAction(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    FileBasedFileSystem.runAsInconsistent(r);
                }
            });
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public void refreshImpl(boolean expected) {                        
        FileObject fo = root.getRealRoot();
        if (fo instanceof BaseFileObj) {
            ((BaseFileObj)fo).getFactory().refresh(expected);
        } else if (fo instanceof RootObjWindows) {
            Collection<? extends FileObjectFactory> fcs =  factories().values();
            for (FileObjectFactory fileObjectFactory : fcs) {
                fileObjectFactory.refresh(expected);
            }
        }
    }
        
    @Override
    public String getDisplayName() {
        return getClass().getName();
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public FileObject getRoot() {
        return root;
    }

    @Override
    public FileObject findResource(String name) {
        if (BaseUtilities.isWindows()) {
            if ("".equals(name)) {//NOI18N
                return FileBasedFileSystem.getInstance().getRoot();
            }
        }  else {
            name = (name.startsWith("/")) ? name : ("/"+name);    
        }               
        File f = new File(name);
        if (name.contains("..") || name.contains("./") || name.contains("/.")) { // NOI18N
            f = FileUtil.normalizeFile(f);
        }
        return getFileObject(f);
    }
    
    @Override
    public FileObject getTempFolder() throws IOException {
        FileObject tmpDir =  FileUtil.toFileObject(new File(System.getProperty("java.io.tmpdir")));
        if (tmpDir != null && tmpDir.isFolder() && tmpDir.isValid()) {
            return tmpDir;
        }
        throw new IOException("Cannot find temporary folder"); // NOI18N
    }
    
    @Override
    public FileObject createTempFile(FileObject parent, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        if (parent.isFolder() && parent.isValid()) {
            File tmpFile = Files.createTempFile(FileUtil.toFile(parent).toPath(), prefix, suffix).toFile();
            if (deleteOnExit) {
                tmpFile.deleteOnExit();
            }
            FileObject fo = FileUtil.toFileObject(tmpFile);
            if (fo != null && fo.isData() && fo.isValid()) {
                return fo;
            }
            tmpFile.delete();
        }
        throw new IOException("Cannot create temporary file"); // NOI18N
    }

    @Override
    public Lookup findExtrasFor(Set<FileObject> objects) {
        return status.findExtrasFor(objects);
    }

    @Override
    public StatusDecorator getDecorator() {
        return status;
    }
    

    public class StatusImpl implements StatusDecorator,
            org.openide.util.LookupListener, org.openide.filesystems.FileStatusListener {

        /** result with providers */
        protected org.openide.util.Lookup.Result<BaseAnnotationProvider> annotationProviders;
        private Collection<? extends BaseAnnotationProvider> previousProviders;

        {
            //Force Lookup to load AnnotationProvider to correctly
            //set BaseAnnotationProvider <- AnnotationProvider class hierarchy.
            try {
                Class.forName(
                    "org.netbeans.modules.masterfs.ui.Init",    //NOI18N
                    true,
                    Lookup.getDefault().lookup(ClassLoader.class));
            } catch (ClassNotFoundException e) {
                //pass - no masterfs.ui module no @ServiceProvider(service=AnnotationProvider.class) hack needed.
            }
            annotationProviders = Lookup.getDefault().lookup(new Lookup.Template<BaseAnnotationProvider>(BaseAnnotationProvider.class));
            annotationProviders.addLookupListener(this);
            resultChanged(null);
        }

        public ProvidedExtensions getExtensions() {
            Collection<? extends BaseAnnotationProvider> c;
            if (previousProviders != null) {
                c = Collections.unmodifiableCollection(previousProviders);
            } else {
                c = Collections.emptyList();
            }
            return new ProvidedExtensionsProxy(c);
        }

        @Override
        public void resultChanged(org.openide.util.LookupEvent ev) {
            Collection<? extends BaseAnnotationProvider> now = annotationProviders.allInstances();
            Collection<? extends BaseAnnotationProvider> add;

            if (previousProviders != null) {
                add = new HashSet<BaseAnnotationProvider>(now);
                add.removeAll(previousProviders);

                HashSet<BaseAnnotationProvider> toRemove = new HashSet<BaseAnnotationProvider>(previousProviders);
                toRemove.removeAll(now);
                for (BaseAnnotationProvider ap : toRemove) {
                    ap.removeFileStatusListener(this);
                }

            } else {
                add = now;
            }



            for (BaseAnnotationProvider ap : add) {
                try {
                    ap.addFileStatusListener(this);
                } catch (java.util.TooManyListenersException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            previousProviders = now;
        }

        public Lookup findExtrasFor(Set<FileObject> foSet) {
            List<Lookup> arr = new ArrayList<Lookup>();
            for (BaseAnnotationProvider ap : annotationProviders.allInstances()) {
                final Lookup lkp = ap.findExtrasFor(foSet);
                if (lkp != null) {
                    arr.add(lkp);
                }
            }
            return new ProxyLookup(arr.toArray(new Lookup[0]));
        }

        @Override
        public void annotationChanged(org.openide.filesystems.FileStatusEvent ev) {
            fireFileStatusChanged(ev);
        }


        @Override
        public String annotateName(String name, Set<? extends FileObject> files) {
            String retVal = null;
            Iterator<? extends BaseAnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                BaseAnnotationProvider ap = it.next();
                retVal = ap.annotateName(name, files);
            }
            if (retVal != null) {
                return retVal;
            }
            return name;
        }

        @Override
        public String annotateNameHtml(String name, Set<? extends FileObject> files) {
            String retVal = null;
            Iterator<? extends BaseAnnotationProvider> it = annotationProviders.allInstances().iterator();
            while (retVal == null && it.hasNext()) {
                BaseAnnotationProvider ap = it.next();
                retVal = ap.annotateNameHtml(name, files);
            }
            return retVal;
        }
    }

    public Object writeReplace() throws ObjectStreamException {
        return new SerReplace();
    }

    private static class SerReplace implements Serializable {
        /** serial version UID */
        static final long serialVersionUID = -3714631266626840241L;
        public Object readResolve() throws ObjectStreamException {
            return FileBasedFileSystem.getInstance();
        }
    }
    
    public static interface  FSCallable<V>  {
        public V call() throws IOException;                
    }
    
    @ServiceProvider(service = MasterFileSystemFactory.class)
    public static class Factory implements MasterFileSystemFactory {
        @Override
        public FileBasedFileSystem createFileSystem() {
            return new FileBasedFileSystem();
        }
    }
}
