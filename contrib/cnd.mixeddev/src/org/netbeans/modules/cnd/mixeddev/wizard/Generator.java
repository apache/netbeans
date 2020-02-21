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

package org.netbeans.modules.cnd.mixeddev.wizard;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.remote.RemoteFileUtil;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.QmakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.wizards.ProjectGenerator;
import org.netbeans.modules.cnd.makeproject.api.ui.wizard.WizardConstants;
import org.netbeans.modules.cnd.mixeddev.java.JNISupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMEExtensions;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.java.api.common.project.ProjectPlatformProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.RequestProcessor;

/**
 *
 */
public class Generator implements PropertyChangeListener {

    private static final Logger LOG = Logger.getLogger("org.netbeans.modules.mixeddev.wizard"); //NOI18N
    private static final RequestProcessor RP = new RequestProcessor(Generator.class.getName(), 2);
    private final FileObject fileObject;
    private final WizardDescriptor wiz;
    private volatile Project javaProject;
    private volatile Project makeProject;
    private volatile FileObject header;
    private volatile FileObject include;
    private volatile boolean isCpp = false;
    private volatile String extension = "c"; //NOI18N
    
    
    protected Generator(WizardDescriptor wiz, FileObject fileObject) {
        this.fileObject = fileObject;
        this.wiz = wiz;
    }

    protected void instantiate() throws IOException {
        try {
            if (generate()) {
                makeProject = instantiateImpl();
                if (makeProject != null) {
                    switchModel(false);
                    OpenProjects.getDefault().addPropertyChangeListener(this);
                    OpenProjects.getDefault().open(new Project[]{makeProject}, true);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    protected String validate() {
        ClassPath sourceCP = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        FileObject sr = sourceCP != null ? sourceCP.findOwnerRoot(fileObject) : null;
        javaProject = FileOwnerQuery.getOwner(sr);
        if (javaProject == null) {
            return NbBundle.getMessage(Generator.class, "Generator_NoOwner", sr.getPath()); // NOI18N
        }
        ProjectPlatformProvider pp = javaProject.getLookup().lookup(ProjectPlatformProvider.class);
        if (pp == null) {
            return NbBundle.getMessage(Generator.class, "Generator_NoJavaSE", javaProject.getClass()); // NOI18N
        }
        JavaPlatform jp = pp.getProjectPlatform();
        if (jp == null) {
            return NbBundle.getMessage(Generator.class, "Generator_NoJavaPlatform", pp.getClass()); //NOI18N
        }
        final FileObject binFO = jp.findTool("javah"); // NOI18N
        File javah = FileUtil.toFile(binFO); //NOI18N
        if (javah == null) {
            return NbBundle.getMessage(Generator.class, "Generator_NoJavah", jp.getClass()); //NOI18N
        }
        String classNameRelPath = FileUtil.getRelativePath(sr, fileObject);
        if (classNameRelPath.endsWith(".java")){ // NOI18N
            classNameRelPath = classNameRelPath.substring(0, classNameRelPath.length() - 5);
        }
        File workingDir = new File(FileUtil.toFile(sr.getParent()), "build/classes"); // NOI18N
        File classNamePath = new File(workingDir, classNameRelPath+".class"); // NOI18N
        if (!classNamePath.exists()) {
            return NbBundle.getMessage(Generator.class, "Generator_NoCompiled", fileObject.getPath()); //NOI18N
        }
        return null;
    }
    
    private boolean generate() {
        ClassPath sourceCP = ClassPath.getClassPath(fileObject, ClassPath.SOURCE);
        ClassPath compileCP = ClassPath.getClassPath(fileObject, ClassPath.COMPILE);
        FileObject sr = sourceCP != null ? sourceCP.findOwnerRoot(fileObject) : null;
        javaProject = FileOwnerQuery.getOwner(sr);
        ProjectPlatformProvider pp = javaProject.getLookup().lookup(ProjectPlatformProvider.class);
        JavaPlatform jp = pp.getProjectPlatform();
        final FileObject binFO = jp.findTool("javah"); // NOI18N
        final String headerName = fileObject.getName() + ".h"; // NOI18N
        header = JNISupport.generateJNIHeader(binFO, sr, fileObject, headerName, sourceCP, compileCP);
        if (header != null) {
            include = binFO.getParent().getParent().getFileObject("include"); // NOI18N
            return true;
        }
        return false;
    }
    
    private Project instantiateImpl() throws IOException {
        FSPath dirF = (FSPath) WizardConstants.PROPERTY_PROJECT_FOLDER.get(wiz);
        String hostUID = (String) WizardConstants.PROPERTY_HOST_UID.get(wiz);
        CompilerSet toolchain = (CompilerSet) WizardConstants.PROPERTY_TOOLCHAIN.get(wiz);
        boolean defaultToolchain = Boolean.TRUE.equals(WizardConstants.PROPERTY_TOOLCHAIN_DEFAULT.get(wiz));
        if (dirF != null) {
            ExecutionEnvironment ee = (ExecutionEnvironment) WizardConstants.PROPERTY_REMOTE_FILE_SYSTEM_ENV.get(wiz);
            if (ee == null) {
                ee = ExecutionEnvironmentFactory.getLocal();
            }
            dirF = new FSPath(dirF.getFileSystem(), RemoteFileUtil.normalizeAbsolutePath(dirF.getPath(), ee));
        }
        String projectName = (String) WizardConstants.PROPERTY_NAME.get(wiz);
        String makefileName = (String) WizardConstants.PROPERTY_GENERATED_MAKEFILE_NAME.get(wiz);
        int conftype = MakeConfiguration.TYPE_DYNAMIC_LIB;
        //LibraryItem lib = new LibraryItem.OptionItem("-lstdc++"); // NOI18N
        //List<LibraryItem> libs = Arrays.asList(lib);
        Pair<String, Integer> languageStandard = PanelProjectLocationVisual.getLanguageStandard(WizardConstants.PROPERTY_LANGUAGE_STANDARD.get(wiz));
        int stdCode = languageStandard.second();
        isCpp = languageStandard.first().equals(PanelProjectLocationVisual.CPP[0]);
        if (isCpp) {
            MIMEExtensions extensions = MIMEExtensions.get(MIMENames.CPLUSPLUS_MIME_TYPE);
            extension = extensions.getDefaultExtension();
        } else {
            MIMEExtensions extensions = MIMEExtensions.get(MIMENames.C_MIME_TYPE);
            extension = extensions.getDefaultExtension();
        }
        int arch = WizardConstants.PROPERTY_ARCHITECURE.get(wiz);
        MakeConfiguration debug = MakeConfiguration.createConfiguration(dirF, "Debug", conftype, null, hostUID, toolchain, defaultToolchain); // NOI18N
        debug.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
        debug.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
        if (isCpp) {
            debug.getCCCompilerConfiguration().getCppStandard().setValue(stdCode);
        } else {
            debug.getCCompilerConfiguration().getCStandard().setValue(stdCode);
        }
        debug.getCCompilerConfiguration().getSixtyfourBits().setValue(arch);
        debug.getCCCompilerConfiguration().getSixtyfourBits().setValue(arch);
        debug.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
        debug.getAssemblerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_DEBUG);
        debug.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.DEBUG_MODE);
        debug.getCCompilerConfiguration().getIncludeDirectories().setValue(getIncludePaths(dirF.getPath()));
        debug.getCCCompilerConfiguration().getIncludeDirectories().setValue(getIncludePaths(dirF.getPath()));
        //debug.getLinkerConfiguration().getLibrariesConfiguration().setValue(libs);
        if (toolchain != null && toolchain.getCompilerFlavor().isSunStudioCompiler()) {
            debug.getLinkerConfiguration().getNorunpathOption().setValue(false);
        }
        
        MakeConfiguration release = MakeConfiguration.createConfiguration(dirF, "Release", conftype, null, hostUID, toolchain, defaultToolchain); // NOI18N
        release.getCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
        release.getCCCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
        if (isCpp) {
            release.getCCCompilerConfiguration().getCppStandard().setValue(stdCode);
        } else {
            release.getCCompilerConfiguration().getCStandard().setValue(stdCode);
        }
        release.getCCompilerConfiguration().getSixtyfourBits().setValue(arch);
        release.getCCCompilerConfiguration().getSixtyfourBits().setValue(arch);
        release.getFortranCompilerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
        release.getAssemblerConfiguration().getDevelopmentMode().setValue(BasicCompilerConfiguration.DEVELOPMENT_MODE_RELEASE);
        release.getQmakeConfiguration().getBuildMode().setValue(QmakeConfiguration.RELEASE_MODE);
        release.getCCompilerConfiguration().getIncludeDirectories().setValue(getIncludePaths(dirF.getPath()));
        release.getCCCompilerConfiguration().getIncludeDirectories().setValue(getIncludePaths(dirF.getPath()));
        //release.getLinkerConfiguration().getLibrariesConfiguration().setValue(libs);
        if (toolchain != null && toolchain.getCompilerFlavor().isSunStudioCompiler()) {
            release.getLinkerConfiguration().getNorunpathOption().setValue(false);
        }
        
        MakeConfiguration[] confs = new MakeConfiguration[]{debug, release};
        ProjectGenerator.ProjectParameters prjParams = new ProjectGenerator.ProjectParameters(projectName, dirF);
        prjParams.setMakefileName(makefileName);
        prjParams.setConfigurations(confs);
        prjParams.setHostUID(hostUID);

        prjParams.setTemplateParams(new HashMap<String,Object>(wiz.getProperties()));
        Project createProject = ProjectGenerator.getDefault().createProject(prjParams);
        return createProject;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(OpenProjects.PROPERTY_OPEN_PROJECTS)) {
            if (evt.getNewValue() instanceof Project[]) {
                final Project[] projects = (Project[])evt.getNewValue();
                if (projects.length == 0) {
                    return;
                }
                OpenProjects.getDefault().removePropertyChangeListener(this);
                RP.post(new Runnable() {

                    @Override
                    public void run() {
                        doWork();
                    }
                });
            }
        }
    }

    private void doWork() {
        try {
            ConfigurationDescriptorProvider pdp = makeProject.getLookup().lookup(ConfigurationDescriptorProvider.class);
            pdp.getConfigurationDescriptor();
            if (pdp.gotDescriptor()) {
                final MakeConfigurationDescriptor configurationDescriptor = pdp.getConfigurationDescriptor();
                if (header != null && header.isValid()) {
                    final FileObject newHeader = createHeader(configurationDescriptor);
                    final StringBuilder buf = new StringBuilder();
                    final FileObject newSource = createSource(configurationDescriptor, buf);
                    configurationDescriptor.save();
                    final CsmModel model = CsmModelAccessor.getModel();
                    if (model != null && makeProject != null) {
                        switchModel(true);
                        final NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
                        final CsmProject p = model.getProject(np);
                        LOG.log(Level.FINE, "Generate stub, CsmProject: {0}", p); // NOI18N
                        if (p != null) {
                            p.waitParse();
                            try {
                                createStub(newHeader, newSource, buf);
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            p.waitParse();
                            np.fireFilesPropertiesChanged();
                            CsmUtilities.openSource(newSource, 0);
                            updateLibraryPath(configurationDescriptor);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void updateLibraryPath(MakeConfigurationDescriptor configurationDescriptor) throws FileNotFoundException, IOException {
        FileObject propFile = javaProject.getProjectDirectory().getFileObject("nbproject/project.properties"); // NOI18N
        if (propFile != null && propFile.isValid() && configurationDescriptor.getActiveConfiguration() != null) {
            final InputStream inputStream = propFile.getInputStream();
            Properties prop = new Properties();
            prop.load(inputStream);
            inputStream.close();
            String args = prop.getProperty("run.jvmargs",""); // NOI18N
            String outputValue = configurationDescriptor.getActiveConfiguration().getAbsoluteOutputValue();
            if (!args.contains("-Djava.library.path=") ) { // NOI18N
                args+=" -Djava.library.path="+CndPathUtilities.getDirName(outputValue); // NOI18N
                prop.put("run.jvmargs", args); // NOI18N
                OutputStream outputStream = propFile.getOutputStream();
                prop.store(outputStream, args);
                outputStream.close();
            }
        }
    }
    
    private void switchModel(boolean state) {
        CsmModel model = CsmModelAccessor.getModel();
        if (model != null && makeProject != null) {
            NativeProject np = makeProject.getLookup().lookup(NativeProject.class);
            if (state) {
                model.enableProject(np);
            } else {
                model.disableProject(np);
            }
        }
    }

    public static void createStub(FileObject newHeader, FileObject newSource, final StringBuilder buf) throws IOException {
        CsmFile includeFile = CsmUtilities.getCsmFile(newHeader, true, false);
        if (includeFile != null) {
            for(CsmOffsetableDeclaration declaration : includeFile.getDeclarations()) {
                if (CsmKindUtilities.isFunction(declaration)) {
                    CsmFunction f = (CsmFunction) declaration;
                    String declarationText = f.getText().toString();
                    int beg = declarationText.lastIndexOf('('); //NOI18N
                    int end = declarationText.lastIndexOf(')'); //NOI18N
                    if (beg > 0 && beg < end) {
                        buf.append('\n'); //NOI18N
                        buf.append(declarationText.substring(0, beg+1));
                        String[] params = declarationText.substring(beg+1,end).split(","); //NOI18N
                        for(int i = 0; i < params.length; i++) {
                            if (i > 0) {
                                buf.append(',').append(' '); //NOI18N
                            }
                            if (i == 0) {
                                buf.append(params[i].trim()).append(' ').append("env"); //NOI18N
                            } else if (i == 1) {
                                buf.append(params[i].trim()).append(' ').append("object"); //NOI18N
                            } else {
                                buf.append(params[i].trim()).append(' ').append("param").append(Integer.toString(i-1)); //NOI18N
                            }
                        }
                        buf.append(") {\n}\n"); //NOI18N
                    }
                }
            }
            Document document = CsmUtilities.getDocument(newSource);
            if (document instanceof BaseDocument) {
                final BaseDocument doc = (BaseDocument) document;
                doc.runAtomicAsUser(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            doc.insertString(0, buf.toString(), null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                });
            } else {
                Writer w = new OutputStreamWriter(newSource.getOutputStream());
                w.write(buf.toString());
                w.close();
            }
        }
    }

    private FileObject createSource(final MakeConfigurationDescriptor configurationDescriptor, StringBuilder buf) throws IOException {
        FileObject newSource;
        Folder sourceFolder = getRootSource(configurationDescriptor);
        FileObject folder;
        if (sourceFolder.isDiskFolder()) {
            folder = RemoteFileUtil.getFileObject(sourceFolder.getAbsolutePath(), makeProject);
        } else {
            folder = configurationDescriptor.getBaseDirFileObject();
        }
        newSource = folder.createData(header.getName(), extension); //NOI18N
        buf.append("// Native methods implementation of\n// ").append(fileObject.getPath()).append("\n\n"); //NOI18N
        buf.append("#include \"").append(header.getNameExt()).append("\"\n"); //NOI18N
        Item item = ItemFactory.getDefault().createInFileSystem(configurationDescriptor.getBaseDirFileSystem(),newSource.getPath());
        sourceFolder.addItemAction(item);
        return newSource;
    }

    private List<String> getIncludePaths(String baseDir) {
        List<String> includeDirectoriesVector = new ArrayList<String>();
        String includeDirectory = include.getPath();
        includeDirectory = CndPathUtilities.toRelativePath(baseDir, CndPathUtilities.naturalizeSlashes(includeDirectory));
        includeDirectory = CndPathUtilities.normalizeSlashes(includeDirectory);
        includeDirectoriesVector.add(includeDirectory);
        for(FileObject child : include.getChildren()) {
            if (child.isFolder()) {
                includeDirectory = child.getPath();
                includeDirectory = CndPathUtilities.toRelativePath(baseDir, CndPathUtilities.naturalizeSlashes(includeDirectory));
                includeDirectory = CndPathUtilities.normalizeSlashes(includeDirectory);
                includeDirectoriesVector.add(includeDirectory);
            }
        }
        return includeDirectoriesVector;
    }
    
    private FileObject createHeader(final MakeConfigurationDescriptor configurationDescriptor) throws IOException {
        Folder headersFolder = getRootHeader(configurationDescriptor);
        FileObject folder;
        if (headersFolder.isDiskFolder()) {
            folder = RemoteFileUtil.getFileObject(headersFolder.getAbsolutePath(), makeProject);
        } else {
            folder = configurationDescriptor.getBaseDirFileObject();
        }
        FileObject newHeader = FileUtil.copyFile(header, folder, header.getName());
        Item item = ItemFactory.getDefault().createInFileSystem(configurationDescriptor.getBaseDirFileSystem(), newHeader.getPath());
        headersFolder.addItemAction(item);
        return newHeader;
    }
    
    public static Folder getRootHeader(MakeConfigurationDescriptor configurationDescriptor) {
        Folder folder = configurationDescriptor.getLogicalFolders();
        List<Folder> sources = folder.getFolders();
        for (Folder sub : sources){
            if (sub.isProjectFiles()) {
                if (MakeConfigurationDescriptor.HEADER_FILES_FOLDER.equals(sub.getName())) {
                    return sub;
                }
            }
        }
        return folder;
    }

    public static Folder getRootSource(MakeConfigurationDescriptor configurationDescriptor) {
        Folder folder = configurationDescriptor.getLogicalFolders();
        List<Folder> sources = folder.getFolders();
        for (Folder sub : sources){
            if (sub.isProjectFiles()) {
                if (MakeConfigurationDescriptor.SOURCE_FILES_FOLDER.equals(sub.getName())) {
                    return sub;
                }
            }
        }
        return folder;
    }
}
