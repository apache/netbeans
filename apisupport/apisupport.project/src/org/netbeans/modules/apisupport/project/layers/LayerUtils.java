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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.xml.tax.cookies.TreeEditorCookie;
import org.netbeans.modules.xml.tax.parser.XMLParsingSupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.tax.TreeDocumentRoot;
import org.netbeans.tax.TreeException;
import org.netbeans.tax.TreeObject;
import org.netbeans.tax.io.TreeStreamResult;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.xml.sax.InputSource;

/**
 * Misc support for dealing with layers.
 * @author Jesse Glick
 */
public class LayerUtils {
    private static final Logger LOG = Logger.getLogger(LayerUtils.class.getName());

    private LayerUtils() {}

    /**
     * Find a resource path for a project.
     * @param project a module project
     * @return a classpath where resources can be found
     */
    public static ClassPath findResourceCP(Project project) {
        Sources s = ProjectUtils.getSources(project);
        List<FileObject> roots = new ArrayList<FileObject>();
        for (String type : new String[] {JavaProjectConstants.SOURCES_TYPE_JAVA, JavaProjectConstants.SOURCES_TYPE_RESOURCES}) {
            for (SourceGroup group : s.getSourceGroups(type)) {
                roots.add(group.getRootFolder());
            }
        }
        if (roots.isEmpty()) {
            LOG.log(Level.WARNING, "no resource path for {0}", project);
        }
        return ClassPathSupport.createClassPath(roots.toArray(new FileObject[0]));
    }
    
    /**
     * Translates nbres: into nbrescurr: for internal use.
     * Returns an array of one or more URLs.
     * May just be original URL, but will try to produce URLs corresponding to real files.
     * If there is a suffix, may produce several, most specific first.
     */
    static URL[] currentify(URL u, String suffix, ClassPath cp) {
        if (cp == null) {
            return new URL[] {u};
        }
            if (u.getProtocol().equals("nbres")) { // NOI18N
                String path = u.getFile();
                if (path.startsWith("/")) path = path.substring(1); // NOI18N
                FileObject fo = cp.findResource(path);
                if (fo != null) {
                    return new URL[] {fo.toURL()};
                }
            } else if (u.getProtocol().equals("nbresloc")) { // NOI18N
                List<URL> urls = new ArrayList<URL>();
                String path = u.getFile();
                if (path.startsWith("/")) path = path.substring(1); // NOI18N
                int idx = path.lastIndexOf('/');
                String folder;
                String nameext;
                if (idx == -1) {
                    folder = ""; // NOI18N
                    nameext = path;
                } else {
                    folder = path.substring(0, idx + 1);
                    nameext = path.substring(idx + 1);
                }
                idx = nameext.lastIndexOf('.');
                String name;
                String ext;
                if (idx == -1) {
                    name = nameext;
                    ext = ""; // NOI18N
                } else {
                    name = nameext.substring(0, idx);
                    ext = nameext.substring(idx);
                }
                List<String> suffixes = new ArrayList<String>(computeSubVariants(suffix));
                suffixes.add(suffix);
                Collections.reverse(suffixes);
                for (String trysuffix : suffixes) {
                    String trypath = folder + name + trysuffix + ext;
                    FileObject fo = cp.findResource(trypath);
                    if (fo != null) {
                        urls.add(fo.toURL());
                    }
                }
                if (!urls.isEmpty()) {
                    return urls.toArray(new URL[0]);
                }
            }
        return new URL[] {u};
    }

    static URL urlForBundle(String bundleName) throws MalformedURLException {
        return new URL("nbresloc:/" + bundleName.replace('.', '/') + ".properties");
    }

    // E.g. for name 'foo_f4j_ce_ja', should produce list:
    // 'foo', 'foo_ja', 'foo_f4j', 'foo_f4j_ja', 'foo_f4j_ce'
    // Will actually produce:
    // 'foo', 'foo_ja', 'foo_ce', 'foo_ce_ja', 'foo_f4j', 'foo_f4j_ja', 'foo_f4j_ce'
    // since impossible to distinguish locale from branding reliably.
    private static List<String> computeSubVariants(String name) {
        int idx = name.indexOf('_');
        if (idx == -1) {
            return Collections.emptyList();
        } else {
            String base = name.substring(0, idx);
            String suffix = name.substring(idx);
            List<String> l = computeSubVariants(base, suffix);
            return l.subList(0, l.size() - 1);
        }
    }
    private static List<String> computeSubVariants(String base, String suffix) {
        int idx = suffix.indexOf('_', 1);
        if (idx == -1) {
            List<String> l = new LinkedList<String>();
            l.add(base);
            l.add(base + suffix);
            return l;
        } else {
            String remainder = suffix.substring(idx);
            List<String> l1 = computeSubVariants(base, remainder);
            List<String> l2 = computeSubVariants(base + suffix.substring(0, idx), remainder);
            List<String> l = new LinkedList<String>(l1);
            l.addAll(l2);
            return l;
        }
    }
    
    /**
     * Representation of in-memory TAX tree which can be saved upon request.
     */
    public interface SavableTreeEditorCookie extends TreeEditorCookie {
        
        /** property change fired when dirty flag changes */
        String PROP_DIRTY = "dirty"; // NOI18N
        
        /** true if there are in-memory mods */
        boolean isDirty();
        
        /** try to save any in-memory mods to disk */
        void save() throws IOException;
        
    }
    
    private static final class CookieImpl implements SavableTreeEditorCookie, FileChangeListener, AtomicAction {
        private TreeDocumentRoot root;
        private boolean dirty;
        private Exception problem;
        private final FileObject f;
        private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        private static RequestProcessor RP = new RequestProcessor(CookieImpl.class.getName());
        public CookieImpl(FileObject f) {
            //System.err.println("new CookieImpl for " + f);
            this.f = f;
            f.addFileChangeListener(FileUtil.weakFileChangeListener(this, f));
        }
        public TreeDocumentRoot getDocumentRoot() {
            return root;
        }
        public int getStatus() {
            if (problem != null) {
                return TreeEditorCookie.STATUS_ERROR;
            } else if (root != null) {
                return TreeEditorCookie.STATUS_OK;
            } else {
                return TreeEditorCookie.STATUS_NOT;
            }
        }
        public TreeDocumentRoot openDocumentRoot() throws IOException, TreeException {
            if (root == null && f.isValid()) {
                try {
                    //System.err.println("openDocumentRoot: really opening");
                    boolean oldDirty = dirty;
                    int oldStatus = getStatus();
                    root = new XMLParsingSupport().parse(new InputSource(f.toURL().toExternalForm()));
                    problem = null;
                    dirty = false;
                    pcs.firePropertyChange(PROP_DIRTY, oldDirty, false);
                    pcs.firePropertyChange(PROP_STATUS, oldStatus, TreeEditorCookie.STATUS_OK);
                    //pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, root);
                } catch (IOException e) {
                    problem = e;
                    throw e;
                } catch (TreeException e) {
                    problem = e;
                    throw e;
                }
                ((TreeObject) root).addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        //System.err.println("tree modified");
                        modified();
                    }
                });
            }
            return root;
        }
        public Task prepareDocumentRoot() {
            throw new UnsupportedOperationException();
        }
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }
        private void modified() {
            //System.err.println("modified(): dirty=" + dirty + " in " + Thread.currentThread().getName() + " for " + this);
            if (!dirty) {
                dirty = true;
                pcs.firePropertyChange(PROP_DIRTY, false, true);
            }
        }
        public boolean isDirty() {
            return dirty;
        }
        public synchronized void save() throws IOException {
            //System.err.println("save(): dirty=" + dirty + " in " + Thread.currentThread().getName() + " for " + this);
            if (root == null || !dirty) {
                return;
            }
            //System.err.println("saving in " + Thread.currentThread().getName() + " for " + this);
            f.getFileSystem().runAtomicAction(this);
            //System.err.println("!saving in " + Thread.currentThread().getName() + " for " + this);
            dirty = false;
            pcs.firePropertyChange(PROP_DIRTY, true, false);
        }
        public void run() throws IOException {
            OutputStream os = f.getOutputStream();
            try {
                new TreeStreamResult(os).getWriter(root).writeDocument();
            } catch (TreeException e) {
                throw (IOException) new IOException(e.toString()).initCause(e);
            } finally {
                os.close();
            }
        }
        public void fileChanged(final FileEvent fe) {
            RP.post(
                new Runnable() {
                    @Override
                    public void run() {
                        changed(fe);
                    }
                });
        }
        public void fileDeleted(FileEvent fe) {
            changed(fe);
        }
        public void fileRenamed(FileRenameEvent fe) {
            changed(fe);
        }
        public void fileAttributeChanged(FileAttributeEvent fe) {
            // ignore
        }
        public void fileFolderCreated(FileEvent fe) {
            assert false;
        }
        public void fileDataCreated(FileEvent fe) {
            assert false;
        }
        private void changed(FileEvent fe) {
            //System.err.println("changed on disk in " + Thread.currentThread().getName() + " for " + this);
            //Thread.dumpStack();
            synchronized (this) {
                if (fe.firedFrom(this)) {
                    //System.err.println("(my own change)");
                    return;
                }
                problem = null;
                dirty = false;
                root = null;
            }
            pcs.firePropertyChange(PROP_DOCUMENT_ROOT, null, null);
        }
    }
    
    public static SavableTreeEditorCookie cookieForFile(FileObject f) {
        return new CookieImpl(f);
    }
    
    /**
     * Get a filesystem that will look like what this project would "see".
     * <p>There are four possibilities:</p>
     * <ol>
     * <li><p>For a standalone module project, the filesystem will include all the XML
     * layers from all modules in the selected platform, plus this module's XML layer
     * as the writable layer (use {@link LayerHandle#save} to save changes as needed).</p></li>
     * <li><p>For a module suite project, the filesystem will include all the XML layers
     * from all modules in the selected platform which are not excluded in the current
     * suite configuration, plus the XML layers for modules in the suite (currently all
     * read-only, i.e. the filesystem is read-only).</p></li>
     * <li><p>For a suite component module project, the filesystem will include all XML
     * layers from non-excluded platform modules, plus the XML layers for modules in the
     * suite, with this module's layer being writable.</p></li>
     * <li><p>For a netbeans.org module, the filesystem will include all XML layers
     * from all netbeans.org modules that are not in the <code>extra</code> cluster,
     * plus the layer from this module (if it is in the <code>extra</code> cluster,
     * with this module's layer always writable.</p></li>
     * </ol>
     * <p>Does not currently attempt to cache the result,
     * though that could be attempted later as needed.</p>
     * <p>Will try to produce pleasant-looking display names and/or icons for files.</p>
     * <p>Note that parsing XML layers is not terribly fast so it would be wise to show
     * a "please wait" label or some other simple progress indication while this
     * is being called, if blocking the UI.</p>
     * @param project a project of one of the three types enumerated above
     * @return the effective system filesystem seen by that project
     * @throws IOException if there were problems loading layers, etc.
     * @see "#62257"
     * @see NbModuleProvider#getEffectiveSystemFilesystem
     */
    public static @NonNull FileSystem getEffectiveSystemFilesystem(Project p) throws IOException {
        NbModuleProvider nbm = p.getLookup().lookup(NbModuleProvider.class);
        assert nbm != null;
        return nbm.getEffectiveSystemFilesystem();
    }
    
}
