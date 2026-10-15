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

package org.netbeans.modules.maven.actions;

import org.codehaus.plexus.util.FileUtils;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.apache.maven.artifact.Artifact;
import org.netbeans.api.annotations.common.SuppressWarnings;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.dependency.tree.DependencyNode;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.libraries.LibrariesCustomizer;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.exec.ProgressTransferListener;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.project.libraries.support.LibrariesSupport;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import static org.netbeans.modules.maven.actions.Bundle.*;
import org.netbeans.modules.maven.api.ModelUtils;
import org.openide.NotifyDescriptor;
import org.openide.util.Utilities;

/**
 *
 * @author mkleint
 */
public class CreateLibraryAction extends AbstractAction implements LookupListener {
    private final Lookup lookup;
    private final Lookup.Result<DependencyNode> result;
    private static final @StaticResource String LIBRARIES_ICON = "org/netbeans/modules/maven/actions/libraries.gif";
    private boolean createRunning;

    @Messages("ACT_Library=Create Library")
    @java.lang.SuppressWarnings("LeakingThisInConstructor")
    public CreateLibraryAction(Lookup lkp) {
        this.lookup = lkp;
        putValue(NAME, ACT_Library());
        //TODO proper icon
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage(LIBRARIES_ICON, true))); //NOI18N
        putValue("iconBase", "org/netbeans/modules/maven/actions/libraries.gif"); //NOI18N
        result = lookup.lookupResult(DependencyNode.class);
        setEnabled(result.allInstances().size() > 0);
        result.addLookupListener(this);

    }

    @Messages("LBL_CreateLibrary=Create a NetBeans Library from Maven metadata")
    public @Override void actionPerformed(ActionEvent e) {
        Iterator<? extends DependencyNode> roots = result.allInstances().iterator();
        if (!roots.hasNext()) {
            return;
        }
        final DependencyNode root = roots.next();
        final MavenProject project = lookup.lookup(MavenProject.class);
        final CreateLibraryPanel pnl = new CreateLibraryPanel(root);
        DialogDescriptor dd = new DialogDescriptor(pnl,  LBL_CreateLibrary());
        pnl.createValidations(dd);

        if (DialogDisplayer.getDefault().notify(dd) == DialogDescriptor.OK_OPTION) {
            createRunning = true;
            setEnabled();
            RequestProcessor.getDefault().post(new Runnable() {
                public @Override void run() {
                    try {
                        Library lib = createLibrary(pnl.getLibraryManager(), pnl.getLibraryName(), pnl.getIncludeArtifacts(), pnl.isAllSourceAndJavadoc(), project, pnl.getCopyDirectory());
                        if (lib != null) {
                            LibrariesCustomizer.showCustomizer(lib, pnl.getLibraryManager());
                        }
                    } finally {
                        createRunning = false;
                        setEnabled();
                    }
                }
            });
        }
    }

    public @Override void resultChanged(LookupEvent ev) {
        setEnabled();
    }

    private void setEnabled() {
        SwingUtilities.invokeLater(new Runnable() {
            public @Override void run() {
                setEnabled(!createRunning && result.allInstances().size() > 0);
            }
        });
    }
    
    @Messages({
        "MSG_Create_Library=Create Library",
        "# {0} - Maven coordinates", "MSG_Downloading=Maven: downloading {0}",
        "# {0} - Maven coordinates", "MSG_Downloading_javadoc=Maven: downloading Javadoc {0}",
        "# {0} - Maven coordinates", "MSG_Downloading_sources=Maven: downloading sources {0}",
        "# {0} - Maven coordinates", "MSG_NoJar=No jar file available for {0}"
    })
    @SuppressWarnings("RV_RETURN_VALUE_IGNORED_BAD_PRACTICE") // baseFolder.mkdirs; will throw IOE later from getJarUri
    private static @CheckForNull Library createLibrary(LibraryManager libraryManager, String libraryName, List<Artifact> includeArtifacts, boolean allSourceAndJavadoc, MavenProject project, String copyTo) {
        ProgressHandle handle = ProgressHandle.createHandle(MSG_Create_Library(),
                ProgressTransferListener.cancellable());
        int count = includeArtifacts.size() * (allSourceAndJavadoc ? 3 : 1) + 5;
        handle.start(count);
        try {
            MavenEmbedder online = EmbedderFactory.getOnlineEmbedder();
            int index = 1;
            List<URI> classpathVolume = new ArrayList<URI>();
            List<URI> javadocVolume = new ArrayList<URI>();
            List<URI> sourceVolume = new ArrayList<URI>();
            Map<String, String> properties = new HashMap<String, String>();
            Map<String, List<URI>> volumes = new HashMap<String, List<URI>>();
            File baseFolder = null;
            File nonDefaultLibBase = null;
            if (copyTo != null) {
                //resolve there to copy files
                URL libRoot = libraryManager.getLocation();
                File base;
                if (libRoot != null) {
                    try {
                        base = Utilities.toFile(libRoot.toURI());
                        //getLocation() points to a file
                        base = base.getParentFile();
                        nonDefaultLibBase = base;
                    } catch (URISyntaxException ex) {
                        Exceptions.printStackTrace(ex);
                        base = new File(System.getProperty("netbeans.user"), "libraries");
                    }
                }  else {
                    base = new File(System.getProperty("netbeans.user"), "libraries");
                }
                base = FileUtil.normalizeFile(base);
                baseFolder = FileUtilities.resolveFilePath(base, copyTo);
                baseFolder.mkdirs();
            }
            volumes.put("classpath", classpathVolume); //NOI18N
            if (allSourceAndJavadoc) {
                volumes.put("javadoc", javadocVolume); //NOI18N
                volumes.put("src", sourceVolume); //NOI18N
            }
            StringBuilder mavendeps = new StringBuilder();
            for (Artifact a : includeArtifacts) {
                if (mavendeps.length() > 0) {
                    mavendeps.append(" ");
                }
                mavendeps.append(a.getGroupId()).append(":").append(a.getArtifactId()).append(":").append(a.getVersion()).append(":");
                if (a.hasClassifier()) {
                    mavendeps.append(a.getClassifier()).append(":");
                }
                mavendeps.append(a.getType());
                
                handle.progress(MSG_Downloading(a.getId()), index);
                
                //XXX --------
                //XXX project.getRemoteArtifactRepositories() might not be entirely reliable, we might want to use
                //XXX     RepositoryPreferences.getInstance().getRepositoryInfos() as well..
                try {
                    online.resolveArtifact(a, project.getRemoteArtifactRepositories(), online.getLocalRepository());
                    AtomicBoolean cancel = ProgressTransferListener.activeListener().cancel;
                    if (cancel != null && cancel.get()) {
                        return null;
                    }
                    if(!a.getFile().exists()) { 
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(MSG_NoJar(a.getId()), NotifyDescriptor.WARNING_MESSAGE));
                        return null;
                    }
                    classpathVolume.add(getJarUri(a, baseFolder, nonDefaultLibBase, ClassifierType.BINARY));
                    try {
                        if (allSourceAndJavadoc) {
                            handle.progress(MSG_Downloading_javadoc(a.getId()), index + 1);
                            Artifact javadoc = online.createArtifactWithClassifier(
                                    a.getGroupId(),
                                    a.getArtifactId(),
                                    a.getVersion(),
                                    a.getType(),
                                    "javadoc"); //NOI18N
                            online.resolveArtifact(javadoc, project.getRemoteArtifactRepositories(), online.getLocalRepository());
                            cancel = ProgressTransferListener.activeListener().cancel;
                            if (cancel != null && cancel.get()) {
                                return null;
                            }
                            if (javadoc.getFile().exists()) {
                                URI javadocUri = getJarUri(javadoc, baseFolder, nonDefaultLibBase, ClassifierType.JAVADOC);
                                javadocVolume.add(javadocUri);
                            }

                            handle.progress(MSG_Downloading_sources(a.getId()), index + 2);
                            Artifact sources = online.createArtifactWithClassifier(
                                    a.getGroupId(),
                                    a.getArtifactId(),
                                    a.getVersion(),
                                    a.getType(),
                                    "sources"); //NOI18N
                            online.resolveArtifact(sources, project.getRemoteArtifactRepositories(), online.getLocalRepository());
                            cancel = ProgressTransferListener.activeListener().cancel;
                            if (cancel != null && cancel.get()) {
                                return null;
                            }
                            if (sources.getFile().exists()) {
                                sourceVolume.add(getJarUri(sources, baseFolder, nonDefaultLibBase, ClassifierType.SOURCES));
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(CreateLibraryAction.class.getName()).log(Level.FINE, "Failed to download artifact", ex);
                    }

                } catch (Exception ex) {
                    Logger.getLogger(CreateLibraryAction.class.getName()).log(Level.FINE, "Failed to download artifact", ex);
                }
                index = index + (allSourceAndJavadoc ? 3 : 1);
            }
            try {
                handle.progress("Adding library",  index + 4);
                properties.put(ModelUtils.LIBRARY_PROP_DEPENDENCIES, mavendeps.toString());
                return libraryManager.createURILibrary("j2se", libraryName, libraryName, "Library created from Maven artifacts", volumes, properties); //NOI18N
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } catch (ThreadDeath d) { // download interrupted
        } catch (IllegalStateException ise) { //download interrupted in dependent thread. #213812
            if (!(ise.getCause() instanceof ThreadDeath)) {
                throw ise;
            }
        } finally {
            handle.finish();
        }
        return null;
    }

    /** append path to given jar root uri */
    private static URI appendJarFolder(URI u, String jarFolder) {
        try {
            if (u.isAbsolute()) {
                return new URI("jar:" + u.toString() + "!/" + (jarFolder == null ? "" : jarFolder.replace('\\', '/'))); // NOI18N
            } else {
                return new URI(u.toString() + "!/" + (jarFolder == null ? "" : jarFolder.replace('\\', '/'))); // NOI18N
            }
        } catch (URISyntaxException e) {
            throw new AssertionError(e);
        }
    }

    private enum ClassifierType {BINARY, JAVADOC, SOURCES}

    private static URI getJarUri(Artifact a, File copyTo, File nonDefaultLibBase, ClassifierType type) throws IOException {
        File res = a.getFile();        
        URI uri = Utilities.toURI(res);
        String jarPath = null;
        if (copyTo != null) {
            res = new File(copyTo, a.getFile().getName());
            FileUtils.copyFile(a.getFile(), res);
            if (nonDefaultLibBase != null) {
                String path = FileUtilities.getRelativePath(nonDefaultLibBase, res);
                if (path != null) {
                    uri = LibrariesSupport.convertFilePathToURI(path);
                }
            }
        }
        FileUtil.refreshFor(res);
        FileObject fo = FileUtil.toFileObject(res);
        if (type == ClassifierType.JAVADOC && FileUtil.isArchiveFile(fo)) {
            fo = FileUtil.getArchiveRoot(fo);
            FileObject docRoot = JavadocAndSourceRootDetection.findJavadocRoot(fo);
            if (docRoot != null) {
                jarPath = FileUtil.getRelativePath(fo, docRoot);
            }
        } else if (type == ClassifierType.SOURCES && FileUtil.isArchiveFile(fo)) {
            fo = FileUtil.getArchiveRoot(fo);
            FileObject srcRoot = JavadocAndSourceRootDetection.findSourceRoot(fo);
            if (srcRoot != null) {
                jarPath = FileUtil.getRelativePath(fo, srcRoot);
            }
        }
        return appendJarFolder(uri, jarPath);
    }

}
