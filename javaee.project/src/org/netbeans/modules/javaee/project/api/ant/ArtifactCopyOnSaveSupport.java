/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javaee.project.api.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Petr Hejl
 * @since 1.32
 */
public abstract class ArtifactCopyOnSaveSupport implements FileChangeListener,
            PropertyChangeListener, AntProjectListener {

    private static final Logger LOGGER = Logger.getLogger(ArtifactCopyOnSaveSupport.class.getName());

    private final List<ArtifactListener> listeners = new ArrayList<ArtifactListener>();

    private final Map<File, ItemDescription> listeningTo = new HashMap<File, ItemDescription>();

    private final String destDirProperty;

    private final PropertyEvaluator evaluator;

    private final AntProjectHelper antHelper;

    private boolean synchronize;

    private volatile String destDir;

    public ArtifactCopyOnSaveSupport(String destDirProperty, PropertyEvaluator evaluator,
            AntProjectHelper antHelper) {
        super();
        this.destDirProperty = destDirProperty;
        this.evaluator = evaluator;
        this.antHelper = antHelper;
    }

    public synchronized void enableArtifactSynchronization(boolean synchronize) {
        this.synchronize = synchronize;
    }

    public final void addArtifactListener(ArtifactListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            boolean init = listeners.isEmpty();
            listeners.add(listener);
            if (init) {
                initialize();
                reload();
            }
        }
    }

    public final void removeArtifactListener(ArtifactListener listener) {
        if (listener == null) {
            return;
        }

        synchronized (this) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                close();
            }
        }
    }

    public final void initialize() {
        close();
        destDir = evaluator.getProperty(destDirProperty);
        evaluator.addPropertyChangeListener(this);
        antHelper.addAntProjectListener(this);
    }

    protected abstract List<Item> getArtifacts();

    protected ArtifactListener.Artifact filterArtifact(ArtifactListener.Artifact artifact,
            RelocationType type) {
        return artifact;
    }

    public final synchronized void reload() {
        Map<File, ItemDescription> toRemove  = new HashMap<File, ItemDescription>(listeningTo);
        for (Item artifactItem : getArtifacts()) {
            ClassPathSupport.Item item = artifactItem.getItem();
            String path = null;
            Collection<File> files = new ArrayList<File>();

            if (!item.isBroken()) {
                if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY) {
                    path = artifactItem.getDescription().getPathInDeployment();
                    if (path != null) {
                        for (URL url : item.getLibrary().getContent("classpath")) { // FIXME is this OK ?
                            URL norm = FileUtil.getArchiveFile(url);
                            if (norm == null) {
                                norm = url;
                            }
                            FileObject fo = URLMapper.findFileObject(norm);
                            if (fo != null) {
                                File file = FileUtil.toFile(fo);
                                if (file != null) {
                                    files.add(file);
                                }
                            }
                        }
                    }
                } else if (item.getType() == ClassPathSupport.Item.TYPE_ARTIFACT) {
                    // FIXME more precise check when we should ignore it
                    if (item.getArtifact().getProject().getLookup().lookup(J2eeModuleProvider.class) != null) {
                        continue;
                    }
                    File scriptLocation = item.getArtifact().getScriptLocation().getAbsoluteFile();
                    if (!scriptLocation.isDirectory()) {
                        scriptLocation = scriptLocation.getParentFile();
                    }

                    path = artifactItem.getDescription().getPathInDeployment();
                    if (path != null) {
                        for (URI artifactURI : item.getArtifact().getArtifactLocations()) {
                            File file = null;
                            if (artifactURI.isAbsolute()) {
                                file = new File(artifactURI);
                            } else {
                                file = new File(scriptLocation, artifactURI.getPath());
                            }
                            file = FileUtil.normalizeFile(file);
                            if (file != null) {
                                files.add(file);
                            }
                        }
                    }
                } else if (item.getType() == ClassPathSupport.Item.TYPE_JAR) {
                    path = artifactItem.getDescription().getPathInDeployment();
                    if (path != null) {
                        File file = item.getResolvedFile();
                        if (file != null) {
                            files.add(file);
                        }
                    }
                }
            }

            for (File file : files) {
                if (!listeningTo.containsKey(file)) {
                    FileUtil.addRecursiveListener(this, file);
                    listeningTo.put(file, artifactItem.getDescription());
                    if (synchronize) {
                        FileObject fo = FileUtil.toFileObject(file);
                        if (fo == null) {
                            continue;
                        }
                        try {
                            updateFile(fo,
                                    artifactItem.getDescription().getPathInDeployment(),
                                    artifactItem.getDescription().getRelocationType());
                        } catch (IOException ex) {
                            LOGGER.log(Level.FINE, "Initial copy failed", ex);
                        }
                    }
                }
                toRemove.remove(file);
            }
        }

        for (Map.Entry<File, ItemDescription> removeEntry : toRemove.entrySet()) {
            FileUtil.removeRecursiveListener(this, removeEntry.getKey());
            listeningTo.remove(removeEntry.getKey());
            if (synchronize) {
                deleteFile(removeEntry.getKey(),
                        removeEntry.getValue().getPathInDeployment(),
                        removeEntry.getValue().getRelocationType());
            }
        }
    }

    public final void close() {
        synchronized (this) {
            for (Map.Entry<File, ItemDescription> entry : listeningTo.entrySet()) {
                FileUtil.removeRecursiveListener(this, entry.getKey());
            }
            listeningTo.clear();
        }
        antHelper.removeAntProjectListener(this);
        evaluator.removePropertyChangeListener(this);
    }

    public final void propertyChange(PropertyChangeEvent evt) {
        if (ProjectProperties.JAVAC_CLASSPATH.equals(evt.getPropertyName())) {
            LOGGER.log(Level.FINEST, "Classpath changed");
            reload();
        } else if (destDirProperty.equals(evt.getPropertyName())) {
            // TODO copy all files ?
            destDir = evaluator.getProperty(destDirProperty);
        }
    }

    public final void configurationXmlChanged(AntProjectEvent ev) {
        if (AntProjectHelper.PROJECT_XML_PATH.equals(ev.getPath())) {
            LOGGER.log(Level.FINEST, "Project XML changed");
            reload();
        }
    }

    public final void propertiesChanged(AntProjectEvent ev) {
        // noop
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
    }

    @Override
    public void fileDataCreated(FileEvent fe) {
        updateFile(fe);
    }

    @Override
    public void fileChanged(FileEvent fe) {
        updateFile(fe);
    }

    @Override
    public void fileDeleted(FileEvent fe){
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        updateFile(fe);
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe){
    }

    private void fireArtifactChange(File file, RelocationType type) {
        List<ArtifactListener> toFire = null;
        synchronized (this) {
            toFire = new ArrayList<ArtifactListener>(listeners);
        }

        Iterable<ArtifactListener.Artifact> iterable = Collections.singleton(
                filterArtifact(ArtifactListener.Artifact.forFile(file).referencedLibrary(), type));
        for (ArtifactListener listener : toFire) {
            listener.artifactsUpdated(iterable);
        }
    }

    private void updateFile(FileEvent event) {
        ItemDescription desc = null;

        synchronized (this) {
            desc = listeningTo.get(FileUtil.toFile(event.getFile()));
            if (desc == null || desc.getPathInDeployment() == null) {
                return;
            }
        }
        try {
            updateFile(event.getFile(), desc.getPathInDeployment(), desc.getRelocationType());
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
    }

    private void updateFile(FileObject sourceObject, String destPath, RelocationType type) throws IOException {
        assert destPath != null;

        FileObject webBuildBase = destDir == null ? null : antHelper.resolveFileObject(destDir);

        if (webBuildBase == null) {
            return;
        }

        if (sourceObject == null) {
            LOGGER.log(Level.FINE, "Source file does not exist");
            return;
        }

        FileObject destFile = FileUtil.createData(webBuildBase, destPath + "/" + sourceObject.getNameExt());
        copy(sourceObject, destFile);

        // fire event
        File dest = FileUtil.toFile(destFile);
        if (dest != null) {
            fireArtifactChange(dest, type);
        }
        LOGGER.log(Level.FINE, "Artifact jar successfully copied " + sourceObject.getPath()
                + " " + sourceObject.getSize());
    }

    private void deleteFile(File sourceFile, String destPath, RelocationType type) {
        assert sourceFile != null;
        assert destPath != null;

        FileObject webBuildBase = destDir == null ? null : antHelper.resolveFileObject(destDir);

        if (webBuildBase == null) {
            return;
        }

        FileObject destFile = null;
        try {
            destFile = FileUtil.createData(webBuildBase, destPath + "/" + sourceFile.getName());

            if (destFile == null) {
                return;
            }

            destFile.delete();
            LOGGER.log(Level.FINE, "Artifact jar successfully deleted");
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
            if (destFile != null && "jar".equals(destFile.getExt())) { // NOI18N
                // try to zero it out at least
                try {
                    zeroOutArchive(destFile);
                    LOGGER.log(Level.FINE, "Artifact jar successfully zeroed out");
                } catch (IOException ioe) {
                    LOGGER.log(Level.INFO, "Could not zero out archive", ioe);
                }
            }
        }

        // fire event
        if (destFile != null) {
            File dest = FileUtil.toFile(destFile);
            if (dest != null) {
                fireArtifactChange(dest, type);
            }
        }
    }

    private void copy(FileObject sourceFile, FileObject destFile) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        FileLock fl = null;
        try {
            is = sourceFile.getInputStream();
            fl = destFile.lock();
            os = destFile.getOutputStream(fl);
            FileUtil.copy(is, os);
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
            if (fl != null) {
                fl.releaseLock();
            }
        }
    }

    private void zeroOutArchive(FileObject garbage) throws IOException {
        OutputStream fileToOverwrite = garbage.getOutputStream();
        try {
            JarOutputStream jos = new JarOutputStream(fileToOverwrite);
            try {
                jos.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF")); // NOI18N
                // UTF-8 guaranteed on any platform
                jos.write("Manifest-Version: 1.0\n".getBytes("UTF-8")); // NOI18N
            } finally {
                jos.close();
            }
        } finally {
            fileToOverwrite.close();
        }
    }
    
    public static final class Item {
        
        private final ClassPathSupport.Item item;
        
        private final ItemDescription description;

        public Item(ClassPathSupport.Item item, ItemDescription description) {
            this.item = item;
            this.description = description;
        }

        public ClassPathSupport.Item getItem() {
            return item;
        }

        public ItemDescription getDescription() {
            return description;
        }
    }
    
    public enum RelocationType {
        NONE,
        LIB,
        ROOT;
        
        public static RelocationType fromString(String type) {
            if (type == null) {
                // todo is this correct ?
                return ROOT;
            }
            if (AntProjectConstants.DESTINATION_DIRECTORY_LIB.equals(type)) {
                return LIB;
            } else if (AntProjectConstants.DESTINATION_DIRECTORY_ROOT.equals(type)) {
                return ROOT;
            } else {
                return NONE;
            }
        }
    }
    
    public static final class ItemDescription {
        
        private final String pathInDeployment;
        
        private final RelocationType type;

        public ItemDescription(String pathInDeployment, RelocationType type) {
            this.pathInDeployment = pathInDeployment;
            this.type = type;
        }

        public String getPathInDeployment() {
            return pathInDeployment;
        }

        public RelocationType getRelocationType() {
            return type;
        }
    }
}
