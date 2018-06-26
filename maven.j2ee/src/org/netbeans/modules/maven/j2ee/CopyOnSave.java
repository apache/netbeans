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
package org.netbeans.modules.maven.j2ee;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ArtifactListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.spi.cos.AdditionalDestination;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * @author mkleint - copied and adjusted from netbeans.org web project until it gets rewritten there to
 *  be generic.
 */
@ProjectServiceProvider(service = {CopyOnSave.class, AdditionalDestination.class, J2eeModuleProvider.DeployOnSaveSupport.class}, projectType={
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_EJB,
    "org-netbeans-modules-maven/" + NbMavenProject.TYPE_APPCLIENT
})
public class CopyOnSave implements AdditionalDestination, J2eeModuleProvider.DeployOnSaveSupport {

    private final List<ArtifactListener> listeners = new CopyOnWriteArrayList<>();
    private final Project project;


    public CopyOnSave(Project project) {
        this.project = project;
    }

    public void initialize() {
    }

    public void cleanup() {
    }

    protected void copySrcToDest( FileObject srcFile, FileObject destFile) throws IOException {
        if (destFile != null && !srcFile.isFolder()) {
            InputStream is = null;
            OutputStream os = null;
            FileLock fl = null;
            try {
                is = srcFile.getInputStream();
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
    }

    @CheckForNull
    protected J2eeModule getJ2eeModule() {
        J2eeModuleProvider provider = getJ2eeModuleProvider();
        if (provider != null) {
            return provider.getJ2eeModule();
        }
        return null;
    }

    @CheckForNull
    protected J2eeModuleProvider getJ2eeModuleProvider() {
        return project.getLookup().lookup(J2eeModuleProvider.class);
    }

    protected Project getProject() {
        return project;
    }

    /** Returns the destination (parent) directory needed to create file with relative path path under webBuilBase
     */
    protected FileObject ensureDestinationFileExists(FileObject buildBase, String path, boolean isFolder) throws IOException {
        FileObject current = buildBase;
        StringTokenizer st = new StringTokenizer(path, "/"); //NOI18N
        while (st.hasMoreTokens()) {
            String pathItem = st.nextToken();
            FileObject newCurrent = current.getFileObject(pathItem);
            if (newCurrent == null) {
                // need to create it
                if (isFolder || st.hasMoreTokens()) {
                    // create a folder
                    newCurrent = FileUtil.createFolder(current, pathItem);
                } else {
                    newCurrent = FileUtil.createData(current, pathItem);
                }
            }
            current = newCurrent;
        }
        return current;
    }

    /**
     * AdditionalDestination
     */
    @Override
    public void copy(FileObject fo, String path) {
        final J2eeModule j2eeModule = getJ2eeModule();
        if (j2eeModule == null) {
            return;
        }

        try {
            final FileObject contentDir = j2eeModule.getContentDirectory();
            if (contentDir != null) {
                // project was built
                FileObject destFile = ensureDestinationFileExists(contentDir,
                        (getDestinationSubFolderName().length() > 0 ? getDestinationSubFolderName() + "/" : "") + path, fo.isFolder());
                File fil = FileUtil.toFile(destFile);
                copySrcToDest(fo, destFile);
                fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String getDestinationSubFolderName() {
        return ""; // NOI18N
    }

    /**
     * AdditionalDestination
     */
    @Override
    public void delete(FileObject fo, String path) {
        final J2eeModule j2eeModule = getJ2eeModule();
        if (j2eeModule == null) {
            return;
        }

        try {
            FileObject contentDir = j2eeModule.getContentDirectory();
            if (contentDir != null) {
                // project was built
                FileObject classes = getDestinationSubFolderName().length() > 0 ?
                        contentDir.getFileObject(getDestinationSubFolderName()) :
                        contentDir;
                if (classes != null) {
                    FileObject toDelete = classes.getFileObject(path);
                    if (toDelete != null) {
                        File fil = FileUtil.toFile(toDelete);
                        toDelete.delete();
                        fireArtifactChange(Collections.singleton(ArtifactListener.Artifact.forFile(fil)));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * J2eeModuleProvider.DeployOnSaveSupport
     * @param listener
     */
    @Override
    public void addArtifactListener(ArtifactListener listener) {
        listeners.add(listener);
    }

    /**
     * J2eeModuleProvider.DeployOnSaveSupport
     * @param listener
     */
    @Override
    public void removeArtifactListener(ArtifactListener listener) {
        listeners.remove(listener);
    }

    protected final void fireArtifactChange(Iterable<ArtifactListener.Artifact> artifacts) {
        for (ArtifactListener listener : listeners) {
            listener.artifactsUpdated(artifacts);
        }
    }

    private static final String NB_COS = ".netbeans_automatic_build"; //NOI18N
    @Override
    public boolean containsIdeArtifacts() {
        return new File(project.getLookup().lookup(NbMavenProject.class).getOutputDirectory(false), NB_COS).exists();
    }
}
