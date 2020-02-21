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
package org.netbeans.modules.cnd.makeproject.ui;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ConnectException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.makeproject.api.LogicalFolderItemsInfo;
import org.netbeans.modules.cnd.makeproject.api.LogicalFoldersInfo;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator.ProjectParameters;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.SourceFolderInfo;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor.State;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.runprofiles.RunProfile;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.support.MakeProjectHelper;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.makeproject.api.wizards.MakeSampleProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.CreateFromTemplateHandler;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a MakeProject from scratch according to some initial configuration.
 */
@org.openide.util.lookup.ServiceProvider(service=ProjectGenerator.class)
public class MakeProjectGeneratorImpl extends ProjectGenerator {

    private static final String PROP_DBCONN = "dbconn"; // NOI18N

    public MakeProjectGeneratorImpl() {
    }

    @Override
    public MakeProject createBlankProject(ProjectParameters prjParams) throws IOException {
        MakeConfiguration[] confs = prjParams.getConfigurations();
        String projectFolderPath = prjParams.getProjectFolderPath();

        // work in a copy of confs
        MakeConfiguration[] copyConfs = new MakeConfiguration[confs.length];
        for (int i = 0; i < confs.length; i++) {
            copyConfs[i] = confs[i].clone();
            copyConfs[i].setBaseFSPath(new FSPath(prjParams.getSourceFileSystem(), projectFolderPath));
            RunProfile profile = (RunProfile) copyConfs[i].getAuxObject(RunProfile.PROFILE_ID);
            profile.setBuildFirst(false);
        }

        FileObject dirFO = createProjectDir(prjParams);
        try {
            FileSystemProvider.suspendWritesUpload(dirFO);
            prjParams.setConfigurations(copyConfs);
            createProject(dirFO, prjParams, true);
            MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
            ProjectManager.getDefault().saveProject(p);

            if (prjParams.getOpenFlag()) {
                OpenProjects.getDefault().open(new Project[]{p}, false);
            }

            return p;
        } finally {
            try {
                FileSystemProvider.resumeWritesUpload(dirFO);
            } catch (InterruptedException ex) {
                InterruptedIOException iie = new InterruptedIOException(ex.getMessage());
                iie.setStackTrace(ex.getStackTrace());
                throw iie;
            }            
        }
    }

    /**
     * Create a new empty Make project.
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Override
    public MakeProject createProject(ProjectParameters prjParams) throws IOException {
        FileObject dirFO = createProjectDir(prjParams);
        try {
            FileSystemProvider.suspendWritesUpload(dirFO);
            createProject(dirFO, prjParams, false); //NOI18N
            MakeProject p = (MakeProject) ProjectManager.getDefault().findProject(dirFO);
            ProjectManager.getDefault().saveProject(p);
            if(prjParams.getDatabaseConnection() != null) {
                Preferences prefs = ProjectUtils.getPreferences(p, ProjectSupport.class, true);
                prefs.put(PROP_DBCONN, prjParams.getDatabaseConnection());
            }
            //FileObject srcFolder = dirFO.createFolder("src"); // NOI18N
            return p;
        } finally {
            try {
                FileSystemProvider.resumeWritesUpload(dirFO);
            } catch (InterruptedException ex) {
                InterruptedIOException iie = new InterruptedIOException(ex.getMessage());
                iie.setStackTrace(ex.getStackTrace());
                throw iie;
            }
        }
    }

    private MakeProjectHelper createProject(FileObject dirFO, final ProjectParameters prjParams, boolean saveNow) throws IOException {
        String name = prjParams.getProjectName();
        String makefileName = prjParams.getMakefileName();
        Configuration[] confs = prjParams.getConfigurations();
        if (prjParams.getCustomizerId() != null) {
            dirFO.createData("cndcustomizerid." + prjParams.getCustomizerId()); // NOI18N
        }
        final Iterator<? extends SourceFolderInfo> sourceFolders = prjParams.getSourceFolders();
        final String sourceFoldersFilter = prjParams.getSourceFoldersFilter();
        final Iterator<? extends SourceFolderInfo> testFolders = prjParams.getTestFolders();
        final Iterator<String> importantItems = prjParams.getImportantFiles();
        final Iterator<LogicalFolderItemsInfo> logicalFolderItems = prjParams.getLogicalFolderItems();
        final Iterator<LogicalFoldersInfo> logicalFolders = prjParams.getLogicalFolders();
        String mainFile = prjParams.getMainFile();
        MakeProjectHelper h = null;
        try {
            h = MakeProjectGenerator.createProject(dirFO, MakeProjectType.TYPE);
        } catch (IllegalArgumentException e) {
            throw new IOException(e);
        }
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(MakeProjectType.PROJECT_CONFIGURATION_NAMESPACE, MakeProjectType.PROJECT_CONFIGURATION__NAME_NAME);
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);

        FileObject sourceBaseFO = dirFO;
        h.putPrimaryConfigurationData(data, true);

        //EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        //h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        //ep = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        //h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);

        // Create new project descriptor with default configurations and save it to disk.
        final MakeConfigurationDescriptor projectDescriptor = new MakeConfigurationDescriptor(dirFO, sourceBaseFO);
        if (makefileName != null) {
            projectDescriptor.setProjectMakefileName(makefileName);
        }
        projectDescriptor.init(confs);
        projectDescriptor.setState(State.READY);

        Project project = projectDescriptor.getProject();
        projectDescriptor.setProject(project);
        // create main source file
        final CreateMainParams mainFileParams = prepareMainIfNeeded(mainFile, dirFO, prjParams.getTemplateParams());
        if (sourceFoldersFilter != null && !MakeConfigurationDescriptor.DEFAULT_IGNORE_FOLDERS_PATTERN.equals(sourceFoldersFilter)) {
            projectDescriptor.setFolderVisibilityQuery(sourceFoldersFilter);
        }

        projectDescriptor.initLogicalFolders(sourceFolders, sourceFolders == null, testFolders,
                logicalFolders, logicalFolderItems, importantItems, mainFileParams.mainFilePath, prjParams.getMainFileTool(), false); // FIXUP: need a better check whether logical folder should be ccreated or not.
         
        projectDescriptor.save();
        // finish postponed activity when project metadata is ready
        MakeTemplateListener instance = MakeTemplateListener.getInstance();
        if (instance != null) {
            instance.setContext(project, projectDescriptor);
        }
        mainFileParams.doPostProjectCreationWork();
        instance = MakeTemplateListener.getInstance();
        if (instance != null) {
            instance.clearContext();
        }
        projectDescriptor.closed();
        projectDescriptor.clean();

        if (!prjParams.isMakefileProject()) {
            FileObject baseDirFileObject = projectDescriptor.getBaseDirFileObject();
            FileObject createData = baseDirFileObject.createData(projectDescriptor.getProjectMakefileName());
            // create Makefile
            copyURLFile("nbresloc:/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile", // NOI18N
                    createData.getOutputStream());
        }
        return h;
    }

    private void copyURLFile(String fromURL, OutputStream os) throws IOException {
        InputStream is = null;
        try {
            URL url = new URL(fromURL);
            is = url.openStream();
        } catch (Exception e) {
            // FIXUP
        }
        if (is != null) {
            copy(is, os);
        }
    }

    /**
     * Replacement for FileUtil.copy(). The problem with FU.c is that on Windows it terminates lines with
     * <CRLF> rather than <LF>. Now that we do remote development, this means that if a remote project is
     * created on Windows to be built by Sun Studio's dmake, then the <CRLF> breaks the build (this is
     * probably true with Solaris "make" as well).
     *
     * @param is The InputStream
     * @param os The Output Stream
     * @throws java.io.IOException
     */
    private void copy(InputStream is, OutputStream os) throws IOException {
        BufferedReader br = null;
        BufferedWriter bw = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, "UTF-8")); //NOI18N
            bw = new BufferedWriter(new OutputStreamWriter(os, FileEncodingQuery.getDefaultEncoding())); //NOI18N
            String line;

            while ((line = br.readLine()) != null) {
                bw.write(line + "\n"); // NOI18N
            }
            bw.flush();
        } finally {
            if (br != null) {
                br.close();
            }
            if (bw != null) {
                bw.close();
            }
        }
    }

    private FileObject createProjectDir(ProjectParameters prjParams) throws IOException {
        String projectFolderPath = prjParams.getProjectFolderPath();
        MakeSampleProjectGenerator.FOPath fopath = new MakeSampleProjectGenerator.FOPath(projectFolderPath);
        FileObject dirFO;
        if (fopath.root !=  null) {
            dirFO = FileUtil.createFolder(fopath.root, fopath.relPath);
        } else {
            dirFO = FileUtil.createFolder(prjParams.getSourceFileSystem().getRoot(), projectFolderPath);
        }
        //File dir = prjParams.getProjectFolder();
        //if (!dir.exists()) {
        //    //Refresh before mkdir not to depend on window focus
        //    // refreshFileSystem (dir); // See 136445
        //    if (!dir.mkdirs()) {
        //        throw new IOException("Can not create project folder."); // NOI18N
        //    }
        //    // refreshFileSystem (dir); // See 136445
        //}
        //dirFO = CndFileUtils.toFileObject(dir);
        assert dirFO != null : "No such dir on disk: " + prjParams.getProjectFolderPath(); // NOI18N
        assert dirFO.isValid() : "No such dir on disk: " + prjParams.getProjectFolderPath(); // NOI18N
        assert dirFO.isFolder() : "Not really a dir: " + prjParams.getProjectFolderPath(); // NOI18N
        return dirFO;
    }

    private CreateMainParams prepareMainIfNeeded(String mainFile, FileObject srcFolder, Map<String, Object> templateParams) throws IOException {
        if (mainFile.length() == 0) {
            return new CreateMainParams(null, null, null);
        }
        String mainName = mainFile.substring(0, mainFile.indexOf('|'));
        String template = mainFile.substring(mainFile.indexOf('|') + 1);

        if (mainName.length() == 0) {
            return new CreateMainParams(null, null, null);
        }

        FileObject mainTemplate = FileUtil.getConfigFile(template);

        if (mainTemplate == null) {
            return new CreateMainParams(null, null, null); // Don't know the template
        }
        final String createdMainName;
         if (mainName.indexOf('\\') > 0 || mainName.indexOf('/') > 0) {
            String absPath = CndPathUtilities.toAbsolutePath(srcFolder, mainName);
            absPath = FileSystemProvider.getCanonicalPath(srcFolder.getFileSystem(), absPath);
            srcFolder = FileUtil.createFolder(srcFolder, CndPathUtilities.getDirName(mainName));
            createdMainName = CndPathUtilities.getBaseName(absPath);
         } else {
            createdMainName = mainName;
         }

        final DataObject mt = DataObject.find(mainTemplate);
        final DataFolder pDf = DataFolder.findFolder(srcFolder);

        final Map<String, Object> params = new HashMap<>();
        params.put(CreateFromTemplateHandler.FREE_FILE_EXTENSION, true);
        params.putAll(templateParams);

        // manipulation with file content should be postponed
        // project does not yet know about main file and can not provide
        // settings which can affect i.e. formatting of file
        Runnable runnable = () -> {
            try {
                mt.createFromTemplate(pDf, createdMainName, params);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
        String fromMap = WizardConstants.PROPERTY_LANGUAGE_STANDARD.fromMap(params);
        return new CreateMainParams(mainName, mt, runnable);
    }

    private static final class CreateMainParams {
        final String mainFilePath;
        final DataObject templateDO;
        private final Runnable postProjectSaveWorker;

        public CreateMainParams(String mainName, DataObject dob, Runnable postProjectSaveWorker) {
            this.mainFilePath = mainName;
            this.templateDO = dob;
            this.postProjectSaveWorker = postProjectSaveWorker;
        }

        private void doPostProjectCreationWork() {
            if (postProjectSaveWorker != null) {
                postProjectSaveWorker.run();
            }
        }

    }
}


