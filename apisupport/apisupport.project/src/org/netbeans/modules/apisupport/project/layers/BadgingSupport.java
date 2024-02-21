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

package org.netbeans.modules.apisupport.project.layers;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Action;
import javax.swing.JSeparator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;
import org.openide.awt.Actions;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.InstanceDataObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.Presenter;

/**
 * Handles addition of badges to a filesystem a la system filesystem.
 * Specifically interprets attributes as specified by {@link FileSystem#getStatus}.
 * Also tries to provide display labels for {@link InstanceDataObject}s.
 * @author Jesse Glick
 */
final class BadgingSupport implements SynchronousStatus, FileChangeListener {

    static final RequestProcessor RP = new RequestProcessor(BadgingSupport.class.getName());
    private static final Logger LOG = Logger.getLogger(BadgingSupport.class.getName());

    /** for branding/localization like "_f4j_ce_ja"; never null, but may be "" */
    private String suffix = "";
    private final FileSystem fs;
    private final FileChangeListener fileChangeListener;
    private final List<FileStatusListener> listeners = new ArrayList<FileStatusListener>();
    // #171204: compute badged information asynch since it can be quite slow
    private final Map<String,String> names = new HashMap<String,String>();
    private final Map<String,Image> smallIcons = new HashMap<String,Image>();
    private final Map<String,Image> bigIcons = new HashMap<String,Image>();
    
    public BadgingSupport(FileSystem fs) {
        this.fs = fs;
        fileChangeListener = FileUtil.weakFileChangeListener(this, null);
        fs.addFileChangeListener(fileChangeListener);
    }
    
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
    
    public void addFileStatusListener(FileStatusListener l) {
        listeners.add(l);
    }
    
    public void removeFileStatusListener(FileStatusListener l) {
        listeners.remove(l);
    }
    
    private void fireFileStatusChanged(FileStatusEvent e) {
        for (FileStatusListener l : listeners) {
            l.annotationChanged(e);
        }
    }

    @Override
    public String annotateNameHtml(String name, Set<? extends FileObject> files) {
        return null;
    }
    
    public String annotateName(final String name, final Set<? extends FileObject> files) {
        synchronized (names) {
            for (FileObject f : files) {
                String path = f.getPath();
                if (names.containsKey(path)) {
                    return names.get(path);
                }
            }
        }
        RP.post(new Runnable() {
            public void run() {
                Set<FileObject> toFire = new HashSet<FileObject>(files);
                String r = annotateNameGeneral(name, files, suffix, fileChangeListener, toFire);
                synchronized (names) {
                    for (FileObject f : files) {
                        names.put(f.getPath(), r);
                    }
                }
                fireFileStatusChanged(new FileStatusEvent(fs, toFire, false, true));
            }
        });
        return name;
    }
    public @Override String annotateNameSynch(String name, Set<? extends FileObject> files) {
        // XXX could participate in names cache
        return annotateNameGeneral(name, files, suffix, null, null);
    }
    private static String annotateNameGeneral(String name, Set<? extends FileObject> files,
            String suffix, FileChangeListener fileChangeListener, Set<FileObject> toFire) {
        for (FileObject fo : files) {
            // #168446: try <attr name="displayName" bundlevalue="Bundle#key"/> first
            String bundleKey = (String) fo.getAttribute("literal:displayName"); // NOI18N
            String bundleName;
            if (bundleKey != null) {
                String[] arr = bundleKey.split(":", 2); // NOI18N
                if (arr[0].equals("bundle")) { // NOI18N
                    // bundlevalue
                    arr = arr[1].split("#", 2);    // NOI18N
                    bundleName = arr[0];
                    bundleKey = arr[1];
                } else {
                    // stringvalue
                    return bundleKey;
                }
            } else {
                bundleName = (String) fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
                bundleKey = fo.getPath();
            }
            if (bundleName != null) {
                try {
                    URL[] u = LayerUtils.currentify(LayerUtils.urlForBundle(bundleName), suffix, classpathForFile(fo));
                    for (int i = 0; i < u.length; i++) {
                    InputStream is = u[i].openStream();
                    try {
                        Properties p = new Properties();
                        p.load(is);
                        String val = p.getProperty(bundleKey);
                        if (fileChangeListener != null) {
                        // Listen to changes in the origin file if any...
                        FileObject ufo = URLMapper.findFileObject(u[i]);
                        if (ufo != null) {
                            ufo.removeFileChangeListener(fileChangeListener);
                            ufo.addFileChangeListener(fileChangeListener);
                            // In case a sibling bundle is added, that may be relevant:
                            ufo.getParent().removeFileChangeListener(fileChangeListener);
                            ufo.getParent().addFileChangeListener(fileChangeListener);
                        }
                        }
                        if (val != null) {
                            if (fo.getPath().matches("(Menu|Toolbars)/.+")) { // NOI18N
                                // Special-case these folders to trim the mnemonics, since they are ugly.
                                return Actions.cutAmpersand(val);
                            } else {
                                return val;
                            }
                        }
                        // if null, fine--normal for key to not be found
                    } finally {
                        is.close();
                    }
                    }
                } catch (IOException ioe) {
                    // For debugging; SFS will rather notify a problem separately...
                    Util.err.notify(ErrorManager.INFORMATIONAL, ioe);
                    return NbBundle.getMessage(BadgingSupport.class, "LBL_no_such_bundle", name, bundleName);
                }
            }
            if (fo.hasExt("instance")) { // NOI18N
                return getInstanceLabel(fo);
            }
            if (fo.hasExt("shadow")) { // NOI18N
                Object originalFile = fo.getAttribute("originalFile"); // NOI18N
                if (originalFile instanceof String) {
                    FileObject orig;
                    try {
                        orig = fo.getFileSystem().findResource((String) originalFile);
                    } catch (FileStateInvalidException e) {
                        orig = null;
                    }
                    if (orig != null && orig.hasExt("instance")) { // NOI18N
                        if (toFire != null) {
                            toFire.add(orig);
                        }
                        return annotateNameGeneral((String) originalFile, Collections.singleton(orig), suffix, fileChangeListener, toFire);
                    }
                }
            }
        }
        return name;
    }

    private static String getInstanceLabel(FileObject fo) {
        try {
            // First try to load it in current IDE, as this handles most platform cases OK.
            InstanceCookie ic = DataObject.find(fo).getLookup().lookup(InstanceCookie.class);
            if (ic != null) {
                final Object o;
                Logger fslogger = Logger.getLogger("org.openide.filesystems"); // NOI18N
                Logger cachelogger = Logger.getLogger("org.netbeans.core.startup.layers.BinaryFS"); // NOI18N
                Level fsLevel = fslogger.getLevel();
                Level cacheLevel = cachelogger.getLevel();
                fslogger.setLevel(Level.OFF); // #99744
                cachelogger.setLevel(Level.OFF); // #166199
                try {
                    o = ic.instanceCreate();
                    if (o instanceof Action) {
                        String name = Mutex.EVENT.readAccess(new Mutex.ExceptionAction<String>() {
                            public String run() throws Exception {
                                return (String) ((Action) o).getValue(Action.NAME);
                            }
                        });
                        if (name != null) {
                            return Actions.cutAmpersand(name);
                        } else {
                            return toStringOf(o);
                        }
                    } else if (o instanceof Presenter.Menu) {
                        return Mutex.EVENT.readAccess(new Mutex.ExceptionAction<String>() {
                            public String run() throws Exception {
                                return ((Presenter.Menu) o).getMenuPresenter().getText();
                            }
                        });
                    } else if (o instanceof JSeparator) {
                        return NbBundle.getMessage(BadgingSupport.class, "LBL_separator");
                    } else {
                        return toStringOf(o);
                    }
                } finally {
                    fslogger.setLevel(fsLevel);
                    cachelogger.setLevel(cacheLevel);
                }
            }
        } catch (Exception e) {
            // ignore, OK
            LOG.log(Level.FINE, "Ignored exception: ({0}) {1}", new Object[] {e.getClass().getSimpleName(), e.getMessage()});
        }
        // OK, probably a developed module, so take a guess.
        String clazz = (String) fo.getAttribute("instanceClass"); // NOI18N
        if (clazz == null) {
            clazz = fo.getName().replace('-', '.');
        }
        String instanceCreate = (String) fo.getAttribute("literal:instanceCreate"); // NOI18N
        if (instanceCreate != null && instanceCreate.startsWith("new:")) { // NOI18N
            clazz = instanceCreate.substring("new:".length()); // NOI18N
        } else if (instanceCreate != null && instanceCreate.startsWith("method:")) { // NOI18N
            String factoryDisplayLabel = instanceCreate.substring(instanceCreate.lastIndexOf('.', instanceCreate.lastIndexOf('.') - 1) + 1);
            return NbBundle.getMessage(BadgingSupport.class, "LBL_instance_from", factoryDisplayLabel);
        }
        String clazzDisplayLabel = clazz.substring(clazz.lastIndexOf('.') + 1);
        return NbBundle.getMessage(BadgingSupport.class, "LBL_instance_of", clazzDisplayLabel);
    }
    private static String toStringOf(Object o) {
        String s = o.toString();
        if ((o.getClass().getName() + "@" + Integer.toHexString(o.hashCode())).equals(s)) {
            // Does not override toString, so no point in using pkg.Clazz@123456.
            String clazz = o.getClass().getName();
            String clazzDisplayLabel = clazz.substring(clazz.lastIndexOf('.') + 1);
            return NbBundle.getMessage(BadgingSupport.class, "LBL_instance_of", clazzDisplayLabel);
        } else {
            return s;
        }
    }
    
    public Image annotateIcon(final Image icon, int type, final Set<? extends FileObject> files) {
        assert icon != null;
        final boolean big;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            big = false;
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            big = true;
        } else {
            return icon;
        }
        final Map<String,Image> icons = big ? bigIcons : smallIcons;
        synchronized (icons) {
            for (FileObject f : files) {
                String path = f.getPath();
                if (icons.containsKey(path)) {
                    return icons.get(path);
                }
            }
        }
        RP.post(new Runnable() {
            public void run() {
                Image r = annotateIconGeneral(icon, big, files);
                assert r != null : files;
                synchronized (icons) {
                    for (FileObject f : files) {
                        icons.put(f.getPath(), r);
                    }
                }
                fireFileStatusChanged(new FileStatusEvent(fs, files, true, false));
            }
        });
        return icon;
    }
    public @Override Image annotateIconSynch(Image icon, int type, Set<? extends FileObject> files) {
        final boolean big;
        if (type == BeanInfo.ICON_COLOR_16x16) {
            big = false;
        } else if (type == BeanInfo.ICON_COLOR_32x32) {
            big = true;
        } else {
            return icon;
        }
        // XXX could participate in bigIcons/smallIcons cache
        return annotateIconGeneral(icon, big, files);
    }
    private Image annotateIconGeneral(Image icon, boolean big, Set<? extends FileObject> files) {
        for (FileObject fo : files) {
            Object value = fo.getAttribute(big ? "SystemFileSystem.icon32" : "SystemFileSystem.icon"); // NOI18N
            if (value instanceof Image) {
                // #18832
                return (Image)value;
            }
            if (value == null) {
                Object iconBase = fo.getAttribute("iconBase");
                if (iconBase instanceof String) {
                    try {
                        value = new URL("nbresloc:/" + iconBase);
                    } catch (MalformedURLException x) {
                        assert false : x;
                    }
                }
            }
            if (value != null) {
                try {
                    URL[] u = LayerUtils.currentify((URL) value, suffix, classpathForFile(fo));
                    FileObject ufo = URLMapper.findFileObject(u[0]);
                    if (ufo != null) {
                        ufo.removeFileChangeListener(fileChangeListener);
                        ufo.addFileChangeListener(fileChangeListener);
                    }
                    return Toolkit.getDefaultToolkit().getImage(u[0]);
                } catch (Exception e) {
                    LOG.log(Level.INFO, "For " + value + " on " + fo.getPath(), e);
                }
            }
        }
        return icon;
    }
    
    // Listen to changes in
    // bundles & icons used to annotate names. If these change,
    // the filesystem needs to show something else. Properly we would
    // keep track of *which* file changed and thus which of our resources
    // is affected. Practically this would be a lot of work and gain
    // very little.
    public void fileDeleted(FileEvent fe) {
        // not ineresting here
    }
    public void fileFolderCreated(FileEvent fe) {
        // does not apply to us
    }
    public void fileDataCreated(FileEvent fe) {
        // In case a file was created that makes an annotation be available.
        // We are listening to the parent folder, so if e.g. a new branded variant
        // of a bundle is added, the display ought to be refreshed accordingly.
        someFileChange();
    }
    public void fileAttributeChanged(FileAttributeEvent fe) {
        if ("DataEditorSupport.read-only.refresh".equals(fe.getName())) { // NOI18N
            return;
        }
        someFileChange();
    }
    public void fileRenamed(FileRenameEvent fe) {
        someFileChange();
    }
    public void fileChanged(FileEvent fe) {
        someFileChange();
    }
    private void someFileChange() {
        synchronized (names) {
            names.clear();
        }
        synchronized (smallIcons) {
            smallIcons.clear();
        }
        synchronized (bigIcons) {
            bigIcons.clear();
        }
        RP.post(new Runnable() {
            public void run() {
                // If used as nbres: annotation, fire status change.
                fireFileStatusChanged(new FileStatusEvent(fs, true, true));
            }
        });
    }

    private static ClassPath classpathForFile(FileObject fo) {
        URL[] layers = (URL[]) fo.getAttribute("layers"); // NOI18N
        if (layers != null) {
            for (URL layer : layers) {
                URL jar = FileUtil.getArchiveFile(layer);
                if (jar != null) {
                    List<URL> roots = new ArrayList<URL>();
                    roots.add(FileUtil.getArchiveRoot(jar));
                    Matcher m = Pattern.compile("(file:.+/)([^/]+)[.]jar").matcher(jar.toString()); // NOI18N
                    if (m.matches()) {
                        try {
                            for (String suffix : NbCollections.iterable(NbBundle.getLocalizingSuffixes())) {
                                roots.add(new URL("jar:" + m.group(1) + "locale/" + m.group(2) + suffix + ".jar!/")); // NOI18N
                            }
                        } catch (MalformedURLException x) {
                            LOG.log(Level.WARNING, "could not find locale variants of " + jar, x);
                        }
                    } else {
                        LOG.log(Level.WARNING, "could not find locale variants of {0}", jar);
                    }
                    LOG.log(Level.FINE, "from {0} getting {1}", new Object[] {layer, roots});
                    return ClassPathSupport.createClassPath(roots.toArray(new URL[0]));
                }
                Project p;
                try {
                    p = FileOwnerQuery.getOwner(layer.toURI());
                    if (p != null) {
                        return LayerUtils.findResourceCP(p);
                    }
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        LOG.log(Level.WARNING, "no classpath found for {0} @{1}", new Object[] {fo, Arrays.toString(layers)});
        return ClassPathSupport.createClassPath(new URL[0]);
    }
    
}
