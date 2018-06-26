/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.j2ee;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.RootedEntry;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 * Abstract class for all implementation of J2eeModuleImplementation2 interface. That means this class is used as a base
 * for Ejb, Web and App client modules. It wraps general code and servers only for purpose of code duplication minimalization 
 * 
 * @author Martin Janicek
 */
public abstract class BaseEEModuleImpl implements J2eeModuleImplementation2, ModuleChangeReporter {

    protected final Project project;
    protected final BaseEEModuleProvider provider;
    protected final String ddName;
    protected final String ddPath;

    
    public BaseEEModuleImpl(Project project, BaseEEModuleProvider provider, String ddName, String ddPath) {
        this.project = project;
        this.provider = provider;
        this.ddName = ddName;
        this.ddPath = ddPath;
    }

    protected final NbMavenProject mavenproject() {
        return project.getLookup().lookup(NbMavenProject.class);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            return metaInf.getFileObject(ddName); //NOI18N
        }
        return null;
    }
    
    public FileObject getMetaInf() {
        Sources srcs = ProjectUtils.getSources(project);
        if (srcs != null) {
            SourceGroup[] grp = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_RESOURCES);
            for (int i = 0; i < grp.length; i++) {
                if (grp[i] != null && grp[i].getRootFolder() != null) {
                    FileObject fo = grp[i].getRootFolder().getFileObject("META-INF"); //NOI18N
                    if (fo != null) {
                        return fo;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public String getUrl() {
        return "/" + mavenproject().getMavenProject().getBuild().getFinalName(); //NOI18N
    }
    
    public FileObject[] getJavaSources() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroup = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<FileObject> toRet = new ArrayList<FileObject>();
        if (sourceGroup != null) {
            for (SourceGroup group : sourceGroup) {
                toRet.add(group.getRootFolder());
            }
        }
        return toRet.toArray(new FileObject[toRet.size()]);
    }

    /**
     * Returns the archive file for the module or <code>null</code> if the archive file
     * does not exist (for example, has not been compiled yet).
     * 
     * @returns archive file or null
     */
    protected final FileObject getArchive(String groupID, String artifactID, String goal, String archiveType) throws IOException {
        MavenProject projectModel = mavenproject().getMavenProject();
        
        String archiveName = PluginPropertyUtils.getPluginProperty(project, groupID, artifactID, archiveType + "Name", goal); //NOI18N
        if (archiveName == null) {
            archiveName = projectModel.getBuild().getFinalName();
        }

        // See issue: #231886
        String archiveDir = PluginPropertyUtils.getPluginProperty(project, groupID, artifactID, "outputDirectory", goal); //NOI18N
        if (archiveDir == null) {
            archiveDir = projectModel.getBuild().getDirectory();
        } else {
            archiveDir = projectModel.getBasedir().getPath() + "/" + archiveDir;  //NOI18N
        }

        File archiveFile = FileUtil.normalizeFile(new File(archiveDir, archiveName + "." + archiveType)); //NOI18N
        
        return FileUtil.toFileObject(archiveFile);
    }
    
    /**
     * Returns a live bean representing the final deployment descriptor
     * that will be used for deployment of the module. This can be
     * taken from sources, constructed on fly or a combination of these
     * but it needs to be available even if the module has not been built yet.
     *
     * @param location Parameterized by location because of possibility of multiple
     * deployment descriptors for a single module (jsp.xml, webservices.xml, etc).
     * Location must be prefixed by /META-INF or /WEB-INF as appropriate.
     * @return a live bean representing the final DD
     */
    protected final RootInterface getDeploymentDescriptor(String location) {
        if (ddName.equals(location)) { //NOI18N
            location = ddPath;
        }
        if (ddPath.equals(location)) {
            try {
                FileObject content = getContentDirectory();
                if (content == null) {
                    URI[] uris = mavenproject().getResources(false);
                    if (uris.length > 0) {
                        content = URLMapper.findFileObject(uris[0].toURL());
                    }
                }
                if (content != null) {
                    FileObject deploymentDescriptor = content.getFileObject(ddPath);
                    if(deploymentDescriptor != null) {
                        return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
                    }
                }
             } catch (IOException e) {
                ErrorManager.getDefault().log(e.getLocalizedMessage());
             }
        }
        return null;
    }

    /**
     * Returns the contents of the archive, in copyable form. Used for incremental deployment. Currently uses its 
     * own {@link RootedEntry} interface. If the J2eeModule instance describes a j2ee application, the result 
     * should not contain module archives.
     *
     * @return Iterator through {@link RootedEntry}s
     * 
     * according to sharold@netbeans.org this should return the iterator over
     * non-warred file, meaning from the expanded webapp. weird.
     */
    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws IOException {
        FileObject fo = getContentDirectory();
        if (fo != null) {
            return new ContentIterator(fo);
        }
        return null;
    }
    
    /**
     * This call is used in in-place deployment. Returns the directory staging the contents of the archive.
     * This directory is the one from which the content entries returned by {@link #getArchiveContents} came from.
     *
     * @return FileObject for the content directory
     */
    @Override
    public FileObject getContentDirectory() throws IOException {
        File file = mavenproject().getOutputDirectory(false);
        FileObject fo = FileUtil.toFileObject(file.getParentFile());
        if (fo != null) {
            fo.refresh();
        }
        return FileUtil.toFileObject(file);
    }

    /**
     * Returns the module resource directory, or null if the module has no resource directory.
     * 
     * @return the module resource directory, or null if the module has no resource directory.
     */
    @Override
    public File getResourceDirectory() {
        return new File(FileUtil.toFile(project.getProjectDirectory()), "src" + File.separator + "main" + File.separator + "setup"); //NOI18N
    }

    /**
     * Returns source deployment configuration file path for the given deployment configuration file name.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml for example.
     * @return absolute path to the deployment configuration file, or null if the specified file name is not known
     *         to this J2eeModule.
     */
    @Override
    public File getDeploymentConfigurationFile(String name) {
        if (name == null) {
            return null;
        }
        if (ddName.equals(name)) { //NOI18N
            name = ddPath;
        } else {
            String path = provider.getConfigSupport().getContentRelativePath(name);
            if (path != null) {
                name = path;
            }
        }
        return getDDFile(name);
    }
    
    public File getDDFile(String path) {
        URI[] dir = mavenproject().getResources(false);
        if (dir.length == 0) {
            return null;
        }
        
        File file = new File(new File(dir[0]), path);
        return FileUtil.normalizeFile(file);
    }
    
    public boolean isValid() {
        return true;
    }

    
    
    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }
    
    @Override
    public boolean isManifestChanged(long timestamp) {
        return false;
    }

    @Override
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return new EjbChangeDescriptorImpl();
    }
    
    
    
    private static final class ContentIterator implements Iterator<J2eeModule.RootedEntry> {
        private ArrayList<FileObject> ch;
        private FileObject root;
        
        private ContentIterator(FileObject f) {
            this.ch = new ArrayList<FileObject>();
            ch.add(f);
            this.root = f;
        }
        
        @Override
        public boolean hasNext() {
            return ! ch.isEmpty();
        }
        
        @Override
        public J2eeModule.RootedEntry next() {
            FileObject f = ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                for (FileObject fo : f.getChildren()) {
                    ch.add(fo);
                }
            }
            return new FSRootRE(root, f);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        private FileObject f;
        private FileObject root;
        
        FSRootRE(FileObject rt, FileObject fo) {
            f = fo;
            root = rt;
        }
        
        @Override
        public FileObject getFileObject() {
            return f;
        }
        
        @Override
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }
}
