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

package org.netbeans.modules.apisupport.project.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.layers.LayerUtils.SavableTreeEditorCookie;
import org.netbeans.modules.apisupport.project.layers.WritableXMLFileSystem;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.xml.sax.SAXException;

/**
 * Manages one project's XML layer.
 */
public final class LayerHandle {

    private static class HandleRef extends WeakReference<LayerHandle> {
        LayerHandle handle; // strong ref while modified, so handle will not be collected
        HandleRef(LayerHandle handle) {
            super(handle);
        }
    }

    private static final Map<Project,HandleRef> layerHandleCache = new WeakHashMap<Project,HandleRef>();

    /**
     * Gets a handle for one project's XML layer.
     */
    public static LayerHandle forProject(Project project) {
        HandleRef ref = layerHandleCache.get(project);
        LayerHandle handle = ref != null ? ref.get() : null;
        if (handle == null) {
            handle = new LayerHandle(project, null);
            handle.ref = new HandleRef(handle);
            layerHandleCache.put(project, handle.ref);
        }
        return handle;
    }

    private final Project project;
    private final FileObject layerXML;
    private FileSystem fs;
    private SavableTreeEditorCookie cookie;
    private boolean autosave;
    private /*final*/ HandleRef ref;

    public LayerHandle(Project project, FileObject layerXML) {
        //System.err.println("new LayerHandle for " + project);
        Parameters.notNull("project", project);
        this.project = project;
        this.layerXML = layerXML;
    }

    /**
     * Get the layer as a structured filesystem.
     * You can make whatever Filesystems API calls you like to it.
     * Just call {@link #save} when you are done so the modified XML document is saved
     * (or the user can save it explicitly if you don't).
     * If there is a {@code META-INF/generated-layer.xml} this will be included as well.
     * @param create if true, and there is no layer yet, create it now; if false, just return null
     */
    public synchronized FileSystem layer(boolean create) {
        if (fs == null) {
            FileObject xml = getLayerFile();
            if (xml == null) {
                if (!create) {
                    return new DualLayers(null);
                }
                try {
                    NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
                    FileObject manifest = module.getManifestFile();
                    if (manifest != null) { // #121056
                        // Check to see if the manifest entry is already specified.
                        String layerSrcPath = ManifestManager.getInstance(Util.getManifest(manifest), false).getLayer();
                        if (layerSrcPath == null) {
                            layerSrcPath = newLayerPath();
                            EditableManifest m = Util.loadManifest(manifest);
                            m.setAttribute(ManifestManager.OPENIDE_MODULE_LAYER, layerSrcPath, null);
                            Util.storeManifest(manifest, m);
                        }
                    }
                    xml = createLayer(project.getProjectDirectory(), module.getResourceDirectoryPath(false) + '/' + newLayerPath());
                } catch (IOException e) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    return fs = FileUtil.createMemoryFileSystem();
                }
            }
            fs = new DualLayers(new WritableXMLFileSystem(xml.toURL(), cookie = LayerUtils.cookieForFile(xml), LayerUtils.findResourceCP(project)));
            cookie.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    //System.err.println("changed in mem");
                    if (SavableTreeEditorCookie.PROP_DIRTY.equals(evt.getPropertyName())) {
                        if (autosave) {
                        //System.err.println("  will save...");
                        try {
                            save();
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                        } else if (ref != null) {
                            ref.handle = LayerHandle.this;
                        }
                    }
                }
            });
        }
        return fs;
    }
    
    
    /**
     * Get the layer as a structured filesystem.
     * You can make whatever Filesystems API calls you like to it.
     * Just call {@link #save} when you are done so the modified XML document is saved
     * (or the user can save it explicitly if you don't).
     * No generated {@code META-INF/generated-layer.xml} will be included.
     * @param create if true, and there is no layer yet, create it now; if false, just return null
     */
    public synchronized FileSystem explicitLayer(boolean create) {
        if (fs == null) {
            FileObject xml = getLayerFile();
            if (xml == null) {
                if (!create) {
                    return new SingleLayer(null);
                }
                try {
                    NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
                    FileObject manifest = module.getManifestFile();
                    if (manifest != null) { // #121056
                        // Check to see if the manifest entry is already specified.
                        String layerSrcPath = ManifestManager.getInstance(Util.getManifest(manifest), false).getLayer();
                        if (layerSrcPath == null) {
                            layerSrcPath = newLayerPath();
                            EditableManifest m = Util.loadManifest(manifest);
                            m.setAttribute(ManifestManager.OPENIDE_MODULE_LAYER, layerSrcPath, null);
                            Util.storeManifest(manifest, m);
                        }
                    }
                    xml = createLayer(project.getProjectDirectory(), module.getResourceDirectoryPath(false) + '/' + newLayerPath());
                } catch (IOException e) {
                    Util.err.notify(ErrorManager.INFORMATIONAL, e);
                    return fs = FileUtil.createMemoryFileSystem();
                }
            }
            fs = new SingleLayer(new WritableXMLFileSystem(xml.toURL(), cookie = LayerUtils.cookieForFile(xml), LayerUtils.findResourceCP(project)));
            cookie.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    //System.err.println("changed in mem");
                    if (SavableTreeEditorCookie.PROP_DIRTY.equals(evt.getPropertyName())) {
                        if (autosave) {
                        //System.err.println("  will save...");
                        try {
                            save();
                        } catch (IOException e) {
                            Util.err.notify(ErrorManager.INFORMATIONAL, e);
                        }
                        } else if (ref != null) {
                            ref.handle = LayerHandle.this;
                        }
                    }
                }
            });
        }
        return fs;
    }

    public static FileObject createLayer(FileObject projectDir, String layerPath) throws IOException {
        FileObject layerFO = createFileObject(projectDir, layerPath);
        InputStream is = LayerHandle.class.getResourceAsStream("/org/netbeans/modules/apisupport/project/ui/resources/layer_template.xml"); // NOI18N
        try {
            OutputStream os = layerFO.getOutputStream();
            try {
                FileUtil.copy(is, os);
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
        return layerFO;
    }
    private static FileObject createFileObject(FileObject dir, String relToDir) throws IOException {
        FileObject createdFO = dir.getFileObject(relToDir);
        if (createdFO != null) {
            throw new IllegalArgumentException("File " + createdFO + " already exists."); // NOI18N
        }
        createdFO = FileUtil.createData(dir, relToDir);
        return createdFO;
    }

    private final class DualLayers extends MultiFileSystem implements FileChangeListener {
        private final FileSystem explicit;
        private final File generated;
        DualLayers(FileSystem explicit) {
            this.explicit = explicit;
            NbModuleProvider nbmp = project.getLookup().lookup(NbModuleProvider.class);
            File clazz = nbmp != null ? nbmp.getClassesDirectory() : null;
            if (clazz != null) {
                generated = new File(clazz, ManifestManager.GENERATED_LAYER_PATH);
                FileUtil.addFileChangeListener(this, generated);
            } else {
                generated = null;
            }
            configure();
            setPropagateMasks(true);
        }
        private void configure() {
            List<FileSystem> layers = new ArrayList<FileSystem>(2);
            if (explicit != null) {
                layers.add(explicit);
            }
            if (generated != null && generated.isFile()) {
                try {
                    layers.add(new XMLFileSystem(Utilities.toURI(generated).toString()));
                } catch (SAXException x) {
                    Logger.getLogger(DualLayers.class.getName()).log(Level.INFO, "could not load " + generated, x);
                }
            }
            setDelegates(layers.toArray(new FileSystem[0]));
        }
        public @Override void fileDataCreated(FileEvent fe) {
            configure();
        }
        public @Override void fileChanged(FileEvent fe) {
            configure();
        }
        public @Override void fileDeleted(FileEvent fe) {
            configure();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            configure(); // ???
        }
        public @Override void fileFolderCreated(FileEvent fe) {}
        public @Override void fileAttributeChanged(FileAttributeEvent fe) {}
    }
    
    private static final SystemAction[] NO_ACTIONS = new SystemAction[0];
    
    /**
     * Extracts actions from a FileSystem that implements the old contract.
     * 
     * @param fs the filesystem
     * @return FD actions, or an empty array.
     */
    private static SystemAction[] getFSActions(FileSystem fs) {
        try {
            Method m = fs.getClass().getMethod("getActions");
            return (SystemAction[])m.invoke(fs);
        } catch (NoSuchMethodException ex) {
            // OK, no such method exists
        } catch (SecurityException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IllegalArgumentException ex) {
            Exceptions.printStackTrace(ex);
        } catch (InvocationTargetException ex) {
            Exceptions.printStackTrace(ex);
        }
        return NO_ACTIONS;
    }
    
    private final class SingleLayer extends FileSystem implements FileChangeListener {
        private final FileSystem explicit;
        SingleLayer(FileSystem explicit) {
            this.explicit = explicit;
            configure();
        }
        private void configure() {
            List<FileSystem> layers = new ArrayList<FileSystem>(2);
            if (explicit != null) {
                layers.add(explicit);
            }
        }
        public @Override void fileDataCreated(FileEvent fe) {
            configure();
        }
        public @Override void fileChanged(FileEvent fe) {
            configure();
        }
        public @Override void fileDeleted(FileEvent fe) {
            configure();
        }
        public @Override void fileRenamed(FileRenameEvent fe) {
            configure(); // ???
        }
        public @Override void fileFolderCreated(FileEvent fe) {}
        public @Override void fileAttributeChanged(FileAttributeEvent fe) {}

        @Override
        public String getDisplayName() {
            return this.explicit!=null?this.explicit.getDisplayName():null;
        }

        @Override
        public boolean isReadOnly() {
            return this.explicit!=null?this.explicit.isReadOnly():false;
        }

        @Override
        public FileObject getRoot() {
            return this.explicit!=null?this.explicit.getRoot():null;
        }

        @Override
        public FileObject findResource(String name) {
            return this.explicit!=null?this.explicit.findResource(name):null;
        }

//        @Override
        public SystemAction[] getActions() {
            return this.explicit!=null?getFSActions(this.explicit):null;
        }
    }

    /**
     * Save the layer, if it was in fact modified.
     * Note that nonempty layer entries you created will already be on disk.
     */
    public void save() throws IOException {
        if (cookie == null) {
            throw new IOException("Cannot save a nonexistent layer"); // NOI18N
        }
        cookie.save();
        if (ref != null) {
            ref.handle = null;
        }
    }

    /**
     * Find the XML layer file for this project, if it exists.
     * @return the layer, or null
     */
    public FileObject getLayerFile() {
        if (layerXML != null) {
            return layerXML;
        }
        NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
        if (module == null) { // #126939: other project type
            return null;
        }
        Manifest mf = Util.getManifest(module.getManifestFile());
        if (mf == null) {
            return null;
        }
        String path = ManifestManager.getInstance(mf, false).getLayer();
        if (path == null) {
            return null;
        }
        return Util.getResource(project, path);
    }

    /**
     * Set whether to automatically save changes to disk.
     * @param autosave true to save changes immediately, false to save only upon request
     */
    public void setAutosave(boolean autosave) {
        this.autosave = autosave;
        if (autosave && cookie != null) {
            try {
                cookie.save();
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        }
    }

    /**
     * Check whether this handle is currently in autosave mode.
     */
    public boolean isAutosave() {
        return autosave;
    }

    /**
     * Resource path in which to make a new XML layer.
     */
    public String newLayerPath() {
        NbModuleProvider module = project.getLookup().lookup(NbModuleProvider.class);
        FileObject manifest = module.getManifestFile();
        if (manifest != null) {
            String bundlePath = ManifestManager.getInstance(Util.getManifest(manifest), false).getLocalizingBundle();
            if (bundlePath != null) {
                return bundlePath.replaceFirst("/[^/]+$", "/layer.xml"); // NOI18N
            }
        }
        return module.getCodeNameBase().replace('.', '/') + "/layer.xml"; // NOI18N
    }

    public @Override String toString() {
        FileObject layer = getLayerFile();
        if (layer != null) {
            return FileUtil.getFileDisplayName(layer);
        } else {
            return FileUtil.getFileDisplayName(project.getProjectDirectory());
        }
    }

}
