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
package org.netbeans.modules.cnd.makeproject.configurations;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.remote.PathMap;
import org.netbeans.modules.cnd.api.remote.RemoteSyncSupport;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.api.toolchain.Tool;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.support.SmartOutputStream;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectCustomizer;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.api.configurations.ArchiverConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.BasicCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CustomToolConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.DefaultMakefileWriter;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibrariesConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakefileConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.PackagingConfiguration;
import org.netbeans.modules.cnd.makeproject.packaging.DummyPackager;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platform;
import org.netbeans.modules.cnd.makeproject.api.configurations.Platforms;
import org.netbeans.modules.cnd.makeproject.spi.DatabaseProjectProvider;
import org.netbeans.modules.cnd.makeproject.spi.configurations.MakefileWriter;
import org.netbeans.modules.cnd.makeproject.uiapi.ConfirmSupport;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class ConfigurationMakefileWriter {

    private final MakeConfigurationDescriptor projectDescriptor;
    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N

    public ConfigurationMakefileWriter(MakeConfigurationDescriptor projectDescriptor) {
        this.projectDescriptor = projectDescriptor;
    }

    public void write() throws IOException {
        Collection<MakeConfiguration> okConfs = getOKConfigurations(false);
        cleanup();
        if (isMakefileProject()) {
            okConfs.forEach((conf) -> {
                writePackagingScript(conf);
            });
        } else {
            writeMakefileImpl();
            for (MakeConfiguration conf : okConfs) {
                writeMakefileConf(conf);
                writePackagingScript(conf);
            }
            writeMakefileVariables(projectDescriptor, okConfs);
        }
    }

    private boolean isMakefileProject() {
        for (Configuration conf : projectDescriptor.getConfs().getConfigurations()) {
            MakeConfiguration makeConfiguration = (MakeConfiguration)conf;
            if (makeConfiguration.isMakefileConfiguration()) {
                return true;
            }
        }
        return false;
    }

//    public void writeMissingMakefiles() {
//        FileObject nbProjFO = projectDescriptor.getNbprojectFileObject();
//        CndUtils.assertNotNullInConsole(nbProjFO, "null nbproject file object"); //NOI18N
//        if (nbProjFO == null) {
//            return;
//        }
//        FileObject configuraionFO = nbProjFO.getFileObject(MakeConfiguration.CONFIGURATIONS_XML);
//        long xmlFileTimeStamp = (configuraionFO == null) ? -1 : configuraionFO.lastModified().getTime();
//        Collection<MakeConfiguration> okConfs = getOKConfigurations(false);
//        for (MakeConfiguration conf : okConfs) {
//            if (!conf.isMakefileConfiguration()) {
//                String relPath = getMakefileName(conf); // NOI18N
//                FileObject fo = nbProjFO.getFileObject(relPath);
//                if (fo == null || ! fo.isValid() || fo.lastModified().getTime() < xmlFileTimeStamp) {
//                    writeMakefileConf(conf);
//                }
//            }
//            String relPath = getPackageScriptName(conf); // NOI18N
//            FileObject fo = nbProjFO.getFileObject(relPath);
//            if (fo == null || ! fo.isValid() || fo.lastModified().getTime() < xmlFileTimeStamp) {
//                writePackagingScript(conf);
//            }
//        }
//    }

    private String getMakefileName(Configuration conf) {
        return "Makefile-" + conf.getName() + ".mk"; // NOI18N
    }

    private String getPackageScriptName(Configuration conf) {
        return "Package-" + conf.getName() + ".bash"; // NOI18N
    }

    /**
     * @return configurations that should be kept intact, i.e. Makefile
     * and Package script should not be changed. Reason for protecting
     * a configuration is missing tool collection. See IZ #168540.
     */
    private Collection<MakeConfiguration> getOKConfigurations(boolean showWarning) {
        List<MakeConfiguration> ok = new ArrayList<>();
        List<MakeConfiguration> noCompilerSet = new ArrayList<>();
        List<MakeConfiguration> wrongPlatform = new ArrayList<>();
        Configuration[] confs = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration conf = (MakeConfiguration) confs[i];
            if (conf.getDevelopmentHost().isLocalhost()
                    && CompilerSetManager.get(conf.getDevelopmentHost().getExecutionEnvironment()).getPlatform() != conf.getDevelopmentHost().getBuildPlatformConfiguration().getValue()) {
                // add configurations if local host and target platform are different (don't have the right compiler set on this platform)
                wrongPlatform.add(conf);
            } else if (conf.getCompilerSet().getCompilerSet() == null) {
                // add configurations with unknown compiler sets
                noCompilerSet.add(conf);
            } else {
                ok.add(conf);
            }
        }
        if (!wrongPlatform.isEmpty() && showWarning && MakeOptions.getInstance().getShowConfigurationWarning()) {
            ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.fromUniqueID(HostInfoUtils.LOCALHOST);
            int platformID = CompilerSetManager.get(execEnv).getPlatform();
            Platform platform = Platforms.getPlatform(platformID);
            StringBuilder list = new StringBuilder();
            wrongPlatform.forEach((c) -> {
                list.append(getString("CONF", c.getName(), c.getDevelopmentHost().getBuildPlatformConfiguration().getName())).append("\n"); // NOI18N
            });
            final String msg = getString("TARGET_MISMATCH_TXT", platform.getDisplayName(), list.toString());
            final String title = getString("TARGET_MISMATCH_DIALOG_TITLE.TXT");
            if (CndUtils.isUnitTestMode() || CndUtils.isStandalone()) {
                new Exception(msg).printStackTrace(System.err);
            } else {
                ConfirmSupport.getConfirmPlatformMismatchFactory().create(title, msg);
            }
        }
        return ok;
    }

    /**
     * Remove all Makefile-* and Package-* files
     * except for those belonging to protected configurations.
     *
     * @param protectedConfs
     */
    private void cleanup() throws IOException {
        FileObject folder = projectDescriptor.getBaseDirFileObject().getFileObject(MakeConfiguration.NBPROJECT_FOLDER);

        if (folder != null && folder.isValid()) {
            IOException lastException = null;

            FileObject[] children = folder.getChildren();
            if (children != null) {
                for (int i = 0; i < children.length; i++) {
                    String filename = children[i].getNameExt();
                    if (filename.equals("Makefile-impl.mk") || filename.equals("Makefile-variables.mk")) { // NOI18N
                        continue;
                    }
                    if (filename.startsWith("Makefile-") || filename.startsWith("Package-")) { // NOI18N
                        boolean known = false;
                        for (Configuration conf : projectDescriptor.getConfs().toArray()) {
                            if (filename.equals(getMakefileName(conf)) // NOI18N
                                    || filename.equals(getPackageScriptName(conf))) { // NOI18N
                                known = true;
                                break;
                            }
                        }
                        if (!known) {
                            try {
                                children[i].delete();
                            } catch (IOException ex) {
                                lastException = ex;
                            }
                        }
                    }
                }
            }

            if (lastException != null) {
                throw lastException;
            }
        }
    }

    private void writeMakefileImpl() throws IOException {
        String resource = "/org/netbeans/modules/cnd/makeproject/resources/MasterMakefile-impl.mk"; // NOI18N
        InputStream is;
        OutputStream os = null;
        try {
            URL url = new URL("nbresloc:" + resource); // NOI18N
            is = url.openStream();
        } catch (Exception e) {
            is = MakeConfigurationDescriptor.class.getResourceAsStream(resource);
        }

        if (is == null) {
            // FIXUP: ERROR
            return;
        }
        FileObject masterMF = null;
        try {
            FileObject nbprojectFileObject = projectDescriptor.getNbprojectFileObject();
            if (nbprojectFileObject == null) {
                return;
            }
            masterMF = FileUtil.createData(nbprojectFileObject, MakeConfiguration.MAKEFILE_IMPL);
            os = SmartOutputStream.getSmartOutputStream(masterMF);
        } catch (IOException ioe) {
            ioe.printStackTrace(System.err);
        }

        if (os == null) {
            closeInputStream(is);
            // FIXUP: ERROR
            return;
        }
        Charset encoding = FileEncodingQuery.getEncoding(masterMF);
        BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8")); //NOI18N
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, encoding)); //NOI18N

        // Project name
        String projectName = CndPathUtilities.getBaseName(projectDescriptor.getProjectDir());

        // Configurations
        StringBuilder configurations = new StringBuilder();
        for (int i = 0; i < projectDescriptor.getConfs().toArray().length; i++) {
            configurations.append(projectDescriptor.getConfs().toArray()[i].getName());
            configurations.append(" "); // NOI18N
        }

        try {
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                if (line.contains("<PN>")) { // NOI18N
                    line = line.replace("<PN>", projectName); // NOI18N
                } else if (line.contains("<CNS>")) { // NOI18N
                    line = line.replace("<CNS>", configurations.toString()); // NOI18N
                } else if (line.contains("<CN>")) { // NOI18N
                    if (projectDescriptor.getConfs().getConf(0) != null) {
                        line = line.replace("<CN>", projectDescriptor.getConfs().getConf(0).getName()); // NOI18N
                    } else {
                        line = line.replace("<CN>", ""); // NOI18N
                    }
                }
                bw.write(line + "\n"); // NOI18N
            }
        } finally {
            closeReader(br);
            closeWriter(bw);
        }
    }

    private void writeMakefileConf(MakeConfiguration conf) throws IOException {
        // Find MakefileWriter in toolchain.
        MakefileWriter makefileWriter = null;
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet != null) {
            String makefileWriterClassName = compilerSet.getCompilerFlavor().getToolchainDescriptor().getMakefileWriter();
            if (makefileWriterClassName != null) {
                Collection<? extends MakefileWriter> mwc = Lookup.getDefault().lookupAll(MakefileWriter.class);
                for (MakefileWriter instance : mwc) {
                    if (makefileWriterClassName.equals(instance.getClass().getName())) {
                        makefileWriter = instance;
                        break;
                    }
                }
                if (makefileWriter == null) {
                    System.err.println("ERROR: class" + makefileWriterClassName + " is not found or is not instance of MakefileWriter"); // NOI18N
                }
            }
        }

        if (conf.isCustomConfiguration() && conf.getProjectCustomizer().getMakefileWriter() != null) {
            MakeProjectCustomizer makeProjectCustomizer = conf.getProjectCustomizer();
            String makefileWriterClassName = makeProjectCustomizer.getMakefileWriter();
            if (makefileWriterClassName != null) {
                Collection<? extends MakefileWriter> mwc = Lookup.getDefault().lookupAll(MakefileWriter.class);
                for (MakefileWriter instance : mwc) {
                    if (makefileWriterClassName.equals(instance.getClass().getName())) {
                        makefileWriter = instance;
                        break;
                    }
                }
                if (makefileWriter == null) {
                    System.err.println("ERROR: class" + makefileWriterClassName + " is not found or is not instance of MakefileWriter"); // NOI18N
                }
            }
        }
        // Use default MakefileWriter if none is found.
        if (makefileWriter == null) {
            makefileWriter = new DefaultMakefileWriter();
        }

        FileObject nbProjFO = projectDescriptor.getNbprojectFileObject();
        if (nbProjFO == null) {
            LOGGER.info("Error writing makefiles: can not find nbproject");
            return;
        }
        FileObject makefileFO = FileUtil.createData(nbProjFO, getMakefileName(conf)); // NOI18N;
        Charset encoding = FileEncodingQuery.getEncoding(makefileFO);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(SmartOutputStream.getSmartOutputStream(makefileFO), encoding));
        try {
            makefileWriter.writePrelude(projectDescriptor, conf, bw);
            writeBuildTargets(makefileWriter, projectDescriptor, conf, bw);
            writeBuildTestTargets(makefileWriter, projectDescriptor, conf, bw);
            makefileWriter.writeRunTestTarget(projectDescriptor, conf, bw);
            makefileWriter.writeCleanTarget(projectDescriptor, conf, bw);
            //we need to write dependencies only in case they are enabled
            if (conf.getDependencyChecking().getValue() && !conf.isMakefileConfiguration() &&
                    !conf.isQmakeConfiguration() && conf.getCompilerSet().getCompilerSet() != null) {
                makefileWriter.writeDependencyChecking(projectDescriptor, conf, bw);
            }
        } finally {
            closeWriter(bw);
        }

        if (conf.isQmakeConfiguration()) {
            new QmakeProjectWriter(projectDescriptor, conf).write();
        }
    }

    public static String getCompilerName(MakeConfiguration conf, PredefinedToolKind tool) {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet != null) {
            Tool compiler = compilerSet.getTool(tool);
            if (compiler != null) {
                BasicCompilerConfiguration compilerConf = null;
                switch (tool) {
                    case CCompiler:
                        compilerConf = conf.getCCompilerConfiguration();
                        break;
                    case CCCompiler:
                        compilerConf = conf.getCCCompilerConfiguration();
                        break;
                    case FortranCompiler:
                        compilerConf = conf.getFortranCompilerConfiguration();
                        break;
                    case Assembler:
                        compilerConf = conf.getAssemblerConfiguration();
                        break;
                }
                if (compilerConf != null && compilerConf.getTool().getModified()) {
                    return compilerConf.getTool().getValue();
                } else if (compiler.getName().length() > 0) {
                    return compiler.getName();
                } else {
                    // Fake tool, get name from the descriptor (see IZ#174566).
                    if (compiler.getDescriptor() != null) {
                        String[] names = compiler.getDescriptor().getNames();
                        if (names != null && names.length > 0) {
                            return names[0];
                        }
                    }
                }
            }
        }
        return ""; // NOI18N
    }

    public static void writePrelude(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            return;
        }
        AbstractCompiler cCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCompiler);
        AbstractCompiler ccCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.CCCompiler);
        AbstractCompiler fortranCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.FortranCompiler);
        AbstractCompiler assemblerCompiler = (AbstractCompiler) compilerSet.getTool(PredefinedToolKind.Assembler);

        bw.write("#\n"); // NOI18N
        bw.write("# Generated Makefile - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# Edit the Makefile in the project folder instead (../Makefile). Each target\n"); // NOI18N
        bw.write("# has a -pre and a -post target defined where you can add customized code.\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# This makefile implements configuration specific macros and targets.\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Environment\n"); // NOI18N
        bw.write("MKDIR=mkdir\n"); // NOI18N
        bw.write("CP=cp\n"); // NOI18N
        bw.write("GREP=grep\n"); // NOI18N
        bw.write("NM=nm\n"); // NOI18N
        bw.write("CCADMIN=CCadmin\n"); // NOI18N
        bw.write("RANLIB=" + conf.getArchiverConfiguration().getRanlibTool().getValue() + "\n"); // NOI18N
        bw.write("CC=" + getCompilerName(conf, PredefinedToolKind.CCompiler) + "\n"); // NOI18N
        bw.write("CCC=" + getCompilerName(conf, PredefinedToolKind.CCCompiler) + "\n"); // NOI18N
        bw.write("CXX=" + getCompilerName(conf, PredefinedToolKind.CCCompiler) + "\n"); // NOI18N
        bw.write("FC=" + getCompilerName(conf, PredefinedToolKind.FortranCompiler) + "\n"); // NOI18N
        bw.write("AS=" + getCompilerName(conf, PredefinedToolKind.Assembler) + "\n"); // NOI18N

        DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
        if(provider != null) {
            provider.writePrelude(projectDescriptor, conf, bw);
        }

        if (conf.getArchiverConfiguration().getTool().getModified()) {
            bw.write("AR=" + conf.getArchiverConfiguration().getTool().getValue() + "\n"); // NOI18N
        }
        if (conf.isQmakeConfiguration()) {
            bw.write("QMAKE=" + getCompilerName(conf, PredefinedToolKind.QMakeTool) + "\n"); // NOI18N
        }
        bw.write("\n"); // NOI18N

        bw.write("# Macros\n"); // NOI18N
        bw.write("CND_PLATFORM=" + conf.getVariant() + "\n"); // NOI18N
        bw.write("CND_DLIB_EXT=" + conf.getLibraryExtension() + "\n"); // NOI18N
        bw.write("CND_CONF=" + conf.getName() + "\n"); // NOI18N
        bw.write("CND_DISTDIR=" + MakeConfiguration.DIST_FOLDER + "\n"); // NOI18N
        bw.write("CND_BUILDDIR=" + MakeConfiguration.BUILD_FOLDER + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        if (!projectDescriptor.getProjectMakefileName().isEmpty()) {
            bw.write("# Include project Makefile\n"); // NOI18N
            bw.write("include " + projectDescriptor.getProjectMakefileName() + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        bw.write("# Object Directory\n"); // NOI18N
        bw.write(MakeConfiguration.OBJECTDIR_MACRO_NAME + "=" + getObjectDir(conf) + "\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("# Object Files\n"); // NOI18N
        bw.write("OBJECTFILES=" + getObjectFiles(projectDescriptor, conf) + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        if (hasTests(projectDescriptor)) {
            bw.write("# Test Directory\n"); // NOI18N
            bw.write("TESTDIR=" + getObjectDir(conf) + "/tests\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("# Test Files\n"); // NOI18N
            bw.write("TESTFILES=" + getTestTargetFiles(projectDescriptor, conf) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("# Test Object Files\n"); // NOI18N
            bw.write("TESTOBJECTFILES=" + getTestObjectFiles(projectDescriptor, conf) + "\n"); // NOI18N
        }

        bw.write("\n"); // NOI18N
        if (cCompiler != null) {
            bw.write("# C Compiler Flags\n"); // NOI18N
            bw.write("CFLAGS=" + conf.getCCompilerConfiguration().getCFlags(cCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (ccCompiler != null) {
            bw.write("# CC Compiler Flags\n"); // NOI18N
            bw.write("CCFLAGS=" + conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("CXXFLAGS=" + conf.getCCCompilerConfiguration().getCCFlags(ccCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (fortranCompiler != null) {
            bw.write("# Fortran Compiler Flags\n"); // NOI18N
            bw.write("FFLAGS=" + conf.getFortranCompilerConfiguration().getFFlags(fortranCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        if (assemblerCompiler != null) {
            bw.write("# Assembler Flags\n"); // NOI18N
            bw.write("ASFLAGS=" + conf.getAssemblerConfiguration().getAsFlags(assemblerCompiler) + "\n"); // NOI18N
            bw.write("\n"); // NOI18N
        }
        bw.write("# Link Libraries and Options\n"); // NOI18N
        String oicLibOptionsPrefix = ""; //NOI18N
        String oicLibOptionsPostfix = ""; //NOI18N
        if(provider != null) {
             oicLibOptionsPrefix = provider.getLibraryOptionsPrefix(projectDescriptor, conf);
             oicLibOptionsPostfix = provider.getLibraryOptionsPostfix(projectDescriptor, conf);
        }
        String explicitDot = "";
//        if (conf.isLinkerConfiguration() && conf.getLinkerConfiguration().getCopyLibrariesConfiguration().getValue()) {
//            explicitDot = " -L. -L${CND_DISTDIR}/${CND_CONF}/${CND_PLATFORM}";
//        }
        bw.write("LDLIBSOPTIONS=" + oicLibOptionsPrefix + conf.getLinkerConfiguration().getLibraryItems() + oicLibOptionsPostfix + explicitDot + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        if (conf.isQmakeConfiguration()) {
            String qmakeSpec = conf.getQmakeConfiguration().getQmakeSpec().getValue();
            // Bug 159594 - Can't build Qt project on OpenSolaris (64 bit)
            // on unix platforms not passing -spec seems to generate correct makefiles
            // on mac/win32 not passing -spec leads to some problems:
            //      on mac - qmake generates xcode project
            //      on windows - problems with slashes vs. backslashes

            if (qmakeSpec.length() == 0 && (
                    conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX ||
                    conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS)) {
                qmakeSpec = CppUtils.getQmakeSpec(compilerSet, conf.getDevelopmentHost().getBuildPlatform());
                if (qmakeSpec == null) {
                    if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX) {
                        qmakeSpec = "macx-g++";  // NOI18N
                    }
                    if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                        qmakeSpec = "win32-g++";  // NOI18N
                    }
                }
            }

            // On Solaris with OSS toolchain installed and added to PATH we still should pass -spec to qmake
            // or the following error message will appear:
            // QMAKESPEC has not been set, so configuration cannot be deduced.
            // Error processing project file: nbproject/qt-Debug.pro

            if (qmakeSpec.length() == 0 &&
                (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_SOLARIS_INTEL ||
                 conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_SOLARIS_SPARC) &&
                compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                qmakeSpec = CppUtils.getQmakeSpec(compilerSet, conf.getDevelopmentHost().getBuildPlatform());
                if (qmakeSpec == null) {
                    // Never should be here, but still...
                    qmakeSpec = "solaris-cc";  // NOI18N
                }
            }

            if (!qmakeSpec.isEmpty()) {
                qmakeSpec = "-spec " + qmakeSpec + " "; // NOI18N
            }
            bw.write("nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk: nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".pro FORCE\n"); // NOI18N
            // It is important to generate makefile in current directory, and then move it to nbproject/.
            // Otherwise qmake will complain that sources are not found.
            bw.write("\t${QMAKE} VPATH=. " + qmakeSpec + "-o qttmp-"+MakeConfiguration.CND_CONF_MACRO+".mk nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".pro\n"); // NOI18N
            bw.write("\tmv -f qttmp-"+MakeConfiguration.CND_CONF_MACRO+".mk nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk\n"); // NOI18N

            // Removed paths tweak for Windows as when -spec is used everything works....
            // See comment above.
            // Still if project is created out of the QT tree, problem described
            // in http://bugreports.qt.nokia.com/browse/QTBUG-10633 exists.
            // To work-around it us following trick.

            if (conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                // qmake uses backslashes on Windows, this code corrects them to forward slashes
                // bw.write("\t@sed -e 's:\\\\\\(.\\):/\\1:g' nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk >nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".tmp\n"); // NOI18N
                bw.write("\t@sed -e 's/\\/qt\\/bin/\\/qt\\/bin\\//g' nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk >nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".tmp\n"); // NOI18N
                bw.write("\t@mv -f nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".tmp nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk\n"); // NOI18N
            }
            bw.write('\n'); // NOI18N
            bw.write("FORCE:\n\n"); // NOI18N
        }
    }

    protected void writeBuildTargets(MakefileWriter makefileWriter, MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        if (compilerSet == null) {
            bw.write(".build-conf:\n"); // NOI18N
            bw.write("\t@echo 'Tool collection " + conf.getCompilerSet().getCompilerSetName().getValue() // NOI18N
                    + " was missing when this makefile was generated'\n"); // NOI18N
            bw.write("\t@echo 'Please specify existing tool collection in project properties'\n"); // NOI18N
            bw.write("\t@exit 1\n\n"); // NOI18N
            return;
        }
        if (conf.isCompileConfiguration()) {
            makefileWriter.writeBuildTarget(projectDescriptor, conf, bw);
            if (conf.isLinkerConfiguration()) {
                makefileWriter.writeLinkTarget(projectDescriptor, conf, bw);
            }
            if (conf.isArchiverConfiguration()) {
                makefileWriter.writeArchiveTarget(projectDescriptor, conf, bw);
            }
            if (conf.isCompileConfiguration()) {
                makefileWriter.writeCompileTargets(projectDescriptor, conf, bw);
            }
        } else if (conf.isMakefileConfiguration()) {
            makefileWriter.writeMakefileTarget(projectDescriptor, conf, bw);
        } else if (conf.isQmakeConfiguration()) {
            makefileWriter.writeQTTarget(projectDescriptor, conf, bw);
        }
        makefileWriter.writeSubProjectBuildTargets(projectDescriptor, conf, bw);
    }

    protected void writeBuildTestTargets(MakefileWriter makefileWriter, MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, BufferedWriter bw) throws IOException {
        if (hasTests(projectDescriptor)) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                bw.write(".build-test-conf:\n"); // NOI18N
                bw.write("\t@echo 'Tool collection " + conf.getCompilerSet().getCompilerSetName().getValue() // NOI18N
                        + " was missing when this makefile was generated'\n"); // NOI18N
                bw.write("\t@echo 'Please specify existing tool collection in project properties'\n"); // NOI18N
                bw.write("\t@exit 1\n\n"); // NOI18N
                return;
            }
            if (conf.isCompileConfiguration()) {
                makefileWriter.writeBuildTestTarget(projectDescriptor, conf, bw);
                makefileWriter.writeLinkTestTarget(projectDescriptor, conf, bw);
                makefileWriter.writeCompileTestTargets(projectDescriptor, conf, bw);
            }
//        else if (conf.isMakefileConfiguration()) {
//            makefileWriter.writeMakefileTarget(projectDescriptor, conf, bw);
//        } else if (conf.isQmakeConfiguration()) {
//            makefileWriter.writeQTTarget(projectDescriptor, conf, bw);
//        }
        }
    }

    public static void writeQTTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = getOutput(projectDescriptor, conf, compilerSet);
        bw.write("# Build Targets\n"); // NOI18N
        bw.write(".build-conf: ${BUILD_SUBPROJECTS} nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk\n"); // NOI18N
        bw.write("\t\""+MakeArtifact.MAKE_MACRO+"\" -f nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk " + output + "\n\n"); // NOI18N

        // #179140 compile single file (qt project)
        // redirect any request for building an object file to the qmake-generated makefile
        bw.write(MakeConfiguration.CND_BUILDDIR_MACRO+"/" + conf.getName() + "/%.o: nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk\n"); // NOI18N
        bw.write("\t"+MakeArtifact.MAKE_MACRO+" -f nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk \"$@\"\n"); // NOI18N
    }

    public static void writeBuildTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = getOutput(projectDescriptor, conf, compilerSet);
        bw.write("# Build Targets\n"); // NOI18N
        bw.write(".build-conf: ${BUILD_SUBPROJECTS}\n"); // NOI18N
        bw.write("\t\""+MakeArtifact.MAKE_MACRO+"\" " // NOI18N
                + " -f nbproject/Makefile-" + MakeConfiguration.CND_CONF_MACRO + ".mk " // NOI18N
                + output + "\n"); // NOI18N

        if (conf.isLinkerConfiguration() && conf.getLinkerConfiguration().getCopyLibrariesConfiguration().getValue()) {
            Set<String> paths = conf.getLinkerConfiguration().getLibrariesConfiguration().getSharedLibraries();
            if (!paths.isEmpty()) {
                String outputDir = CndPathUtilities.getDirName(output);
                boolean isMac = conf.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_MACOSX;

                for (String path : paths) {
                    String libPath = CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(path));
                    bw.write("\t${CP} " + libPath + " " + outputDir + "\n"); //NOI18N
                    if (isMac) {
                        String baseName = CndPathUtilities.getBaseName(libPath);
                        outputDir = CndPathUtilities.trimSlashes(outputDir);
                        bw.write("\t-install_name_tool -change " + baseName + " @executable_path/" + baseName + " " + output + "\n"); //NOI18N
                    }
                }
            }
        }
        bw.write("\n"); //NOI18N
    }

    public static void writeBuildTestTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (hasTests(projectDescriptor)) {
            bw.write("# Build Test Targets\n"); // NOI18N
            bw.write(".build-tests-conf: .build-tests-subprojects .build-conf ${TESTFILES}\n"); // NOI18N
            bw.write(".build-tests-subprojects:\n"); // NOI18N

            Set<MakeArtifact> subProjects = new LinkedHashSet<>();

            for (Folder folder : getTests(projectDescriptor)) {
                List<LinkerConfiguration> linkerConfigurations = getLinkerConfigurations(folder, conf);

                for (LinkerConfiguration lc : linkerConfigurations) {
                    LibrariesConfiguration librariesConfiguration = lc.getLibrariesConfiguration();

                    for (LibraryItem item : librariesConfiguration.getValue()) {
                        if (item instanceof LibraryItem.ProjectItem) {
                            LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                            MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                            if (makeArtifact.getBuild()) {
                                subProjects.add(makeArtifact);
                            }
                        }
                    }
                }

            }
            for (MakeArtifact makeArtifact : subProjects) {
                bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(makeArtifact.getWorkingDirectory())) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N
        }
    }

    public static void writeLinkTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = getOutput(projectDescriptor, conf, compilerSet);
        LinkerConfiguration linkerConfiguration = conf.getLinkerConfiguration();
        String command = getLinkerTool(projectDescriptor, conf, conf.getLinkerConfiguration(), compilerSet);
        if (conf.getDevelopmentHost().isLocalhost()) {
            command += linkerConfiguration.getOutputOptions() + " "; // NOI18N
        } else {
            // This hack is used to workaround the following issue:
            // the linker options contains linker output file
            // but in case of remote it should be mapped to remote path.
            // It's quite hard to implement mapping in the LinkerConfiguration class,
            // as it's not quite clear when we should do this.
            // See Bug 193797 - '... does not exists or is not an executable' when remote mode and not default linker output
            command += linkerConfiguration.getOutputOptions().replace(conf.getOutputValue(), output) + " "; // NOI18N
        }
        command += "${OBJECTFILES}" + " "; // NOI18N
        command += "${LDLIBSOPTIONS}"; // NOI18N
        String options = linkerConfiguration.getCommandLineConfiguration().getValue();
        if (options.length() > 0) {
            command += " " + options; // NOI18N
        }
        options = linkerConfiguration.getBasicOptions();
        if (options.length() > 0) {
            command += " " + options; // NOI18N
        }
        String[] additionalDependencies = linkerConfiguration.getAdditionalDependencies().getValues();
        for (int i = 0; i < additionalDependencies.length; i++) {
            bw.write(output + ": " + additionalDependencies[i] + "\n\n"); // NOI18N
        }
        for (LibraryItem lib : linkerConfiguration.getLibrariesConfiguration().getValue()) {
            String libPath = lib.getPath();
            if (libPath != null && libPath.length() > 0) {
                bw.write(output + ": " + CndPathUtilities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, libPath)) + "\n\n"); // NOI18N
            }
        }
        bw.write(output + ": ${OBJECTFILES}\n"); // NOI18N
        String folders = CndPathUtilities.getDirName(output);
        if (folders != null) {
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        }
        bw.write("\t" + command + "\n"); // NOI18N
    }

    private static String getLinkerTool(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, LinkerConfiguration linkerConfiguration, CompilerSet compilerSet){
        if (linkerConfiguration.getTool().getModified()) {
            return linkerConfiguration.getTool().getValue() + " "; // NOI18N
        }
        String getPreferredCompiler = null;
        if (compilerSet != null) {
            getPreferredCompiler = compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getPreferredCompiler();
        }
        if (getPreferredCompiler != null) {
            if ("c".equals(getPreferredCompiler)) { // NOI18N
                return "${LINK.c}" + " "; // NOI18N
            } else if ("cpp".equals(getPreferredCompiler)) { // NOI18N
                return  "${LINK.cc}" + " "; // NOI18N
            } else if ("fortran".equals(getPreferredCompiler)) { // NOI18N
                return  "${LINK.f}" + " "; // NOI18N
            }
        }
        if (conf.hasCPPFiles(projectDescriptor)) {
            if (compilerSet != null && compilerSet.getCompilerFlavor().isSunStudioCompiler()) {
                int cppStd = getCppStd(projectDescriptor, conf);
                if (cppStd > 0) {
                    AbstractCompiler cpp = (AbstractCompiler)compilerSet.findTool(PredefinedToolKind.CCCompiler);
                    return  "${LINK.cc}" + " "+cpp.getCppStandardOptions(cppStd) +" "; // NOI18N
                }
            }
            return  "${LINK.cc}" + " "; // NOI18N
        } else if (conf.hasFortranFiles(projectDescriptor)) {
            return  "${LINK.f}" + " "; // NOI18N
        }
        return "${LINK.c}" + " "; // NOI18N
    }

    private static int getCppStd(MakeConfigurationDescriptor configurationDescriptor, MakeConfiguration conf) {
        Item[] items = configurationDescriptor.getProjectItems();
        // Base it on actual files added to project
        for (int x = 0; x < items.length; x++) {
            ItemConfiguration itemConfiguration = items[x].getItemConfiguration(conf);
            if (itemConfiguration == null
                    || itemConfiguration.getExcluded() == null
                    || itemConfiguration.getExcluded().getValue()) {
                continue;
            }
            PredefinedToolKind tool = itemConfiguration.getTool();
            if (tool == PredefinedToolKind.CCCompiler) {
                BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                if (compilerConfiguration instanceof CCCompilerConfiguration) {
                    CCCompilerConfiguration cppConf = (CCCompilerConfiguration) compilerConfiguration;
                    final int cppStd = cppConf.getInheritedCppStandard();
                    if (cppStd != CCCompilerConfiguration.STANDARD_DEFAULT) {
                        return cppStd;
                    }
                }
            }
        }
        return 0;
    }

    public static void writeLinkTestTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (conf.isCompileConfiguration()) {
            List<Folder> tests = getTests(projectDescriptor);
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            if (compilerSet == null) {
                return;
            }
            if (!tests.isEmpty()) {
                for (Folder folder : tests) {
                    LinkerConfiguration testLinkerConfiguration = folder.getFolderConfiguration(conf).getLinkerConfiguration();

                    String output = testLinkerConfiguration.getOutputValue();

                    List<LinkerConfiguration> linkerConfigurations = getLinkerConfigurations(folder, conf);

                    CompilerSet cs = conf.getCompilerSet().getCompilerSet();
                    output = CppUtils.normalizeDriveLetter(cs, output);
                    String command = getLinkerTool(projectDescriptor, conf, testLinkerConfiguration, compilerSet);
                    command += "-o " + output + " "; // NOI18N
                    if (cs != null && testLinkerConfiguration.getStripOption().getValue()) {
                        command += cs.getCompilerFlavor().getToolchainDescriptor().getLinker().getStripFlag() + " "; // NOI18N
                    }
                    command += "$^" + " "; // NOI18N
                    command += "${LDLIBSOPTIONS}" + " "; // NOI18N

                    for (LinkerConfiguration lc : linkerConfigurations) {
                        command += lc.getCommandLineConfiguration().getValue() + " "; // NOI18N
                    }

                    List<String> additionalDependencies = new ArrayList<>();
                    linkerConfigurations.forEach((lc) -> {
                        additionalDependencies.addAll(lc.getAdditionalDependencies().getValuesAsList());
                    });
                    for (String dep : additionalDependencies) {
                        bw.write(output + ": " + dep + "\n\n"); // NOI18N
                    }

                    for (LinkerConfiguration lc : linkerConfigurations) {
                        String libraryItems = lc.getLibraryItems();
                        if (libraryItems != null && !libraryItems.isEmpty()) {
                            command += libraryItems + " "; // NOI18N
                        }
                    }

                    String objectFiles = ""; // NOI18N
                    Item[] items = folder.getAllItemsAsArray();
                    for (int k = 0; k < items.length; k++) {
                        ItemConfiguration itemConfiguration = items[k].getItemConfiguration(conf);
                        if (itemConfiguration == null){
                            continue;
                        }
                        if (itemConfiguration.getExcluded().getValue()) {
                            continue;
                        }
                        if (!itemConfiguration.isCompilerToolConfiguration()) {
                            continue;
                        }
                        if (items[k].hasHeaderOrSourceExtension(false, false)) {
                            continue;
                        }
                        BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                        String file = CndPathUtilities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[k].getPath()));
                        String objectFile = compilerConfiguration.getOutputFile(items[k], conf, false);
                        objectFile = objectFile.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
                        objectFiles += objectFile + " "; // NOI18N
                    }
                    objectFiles += "${OBJECTFILES:%.o=%_nomain.o}"; // NOI18N

                    bw.write(output + ": " + objectFiles + "\n"); // NOI18N
                    String folders = CndPathUtilities.getDirName(output);
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                    bw.write("\t" + command + "\n\n"); // NOI18N
                }
            }
        }
    }

    private static List<LinkerConfiguration> getLinkerConfigurations(Folder test, MakeConfiguration conf) {
        List<LinkerConfiguration> linkerConfigurations = new ArrayList<>();
        if(test == null || conf == null) {
            return linkerConfigurations;
        }
        getLinkerConfigurations(linkerConfigurations, test, conf);
        return linkerConfigurations;
    }

    private static void getLinkerConfigurations(List<LinkerConfiguration> confs, Folder folder, MakeConfiguration conf) {
        if(folder == null) {
            return;
        }
        FolderConfiguration folderConfiguration = folder.getFolderConfiguration(conf);
        if(folderConfiguration == null) {
            return;
        }
        LinkerConfiguration testLinkerConfiguration = folderConfiguration.getLinkerConfiguration();
        if(testLinkerConfiguration == null) {
            return;
        }
        Folder parentFolder = folder.getParent();
        if (parentFolder != null) {
            getLinkerConfigurations(confs, parentFolder, conf);
        }
        confs.add(testLinkerConfiguration);
    }

    public static void writeArchiveTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
        String output = getOutput(projectDescriptor, conf, compilerSet);
        ArchiverConfiguration archiverConfiguration = conf.getArchiverConfiguration();
        String command = "${AR}" + " "; // NOI18N
        command += archiverConfiguration.getOptions() + " "; // NOI18N
        command += "${OBJECTFILES}" + " "; // NOI18N
        bw.write(output + ": " + "${OBJECTFILES}" + "\n"); // NOI18N
        String folders = CndPathUtilities.getDirName(output);
        if (folders != null) {
            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
        }
        bw.write("\t" + "${RM}" + " " + output + "\n"); // NOI18N
        bw.write("\t" + command + "\n"); // NOI18N
        if (archiverConfiguration.getRunRanlib().getValue()) {
            bw.write("\t" + archiverConfiguration.getRunRanlib().getOption() + " " + output + "\n"); // NOI18N
        }
    }

    public static Item[] getSortedProjectItems(MakeConfigurationDescriptor projectDescriptor) {
        List<Item> res = new ArrayList<>(Arrays.asList(projectDescriptor.getProjectItems()));
        Collections.<Item>sort(res, (Item o1, Item o2) -> o1.getPath().compareTo(o2.getPath()));
        return res.toArray(new Item[res.size()]);
    }

    public static void writeCompileTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        Item[] items = getSortedProjectItems(projectDescriptor);
        if (conf.isCompileConfiguration()) {
            String target = null;
            String folders;
            String file;
            String command;
            String comment;
            String additionalDep;
            for (int i = 0; i < items.length; i++) {
                final Folder folder = items[i].getFolder();
                if (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
                    continue;
                }
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration == null){
                    continue;
                }
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                file = CndPathUtilities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath(true)));

                DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                if(provider != null) {
                    if(provider.isProCItem(items[i])) {
                        file = provider.getProCOutput(items[i], conf);
                    }
                }

                command = ""; // NOI18N
                comment = null;
                additionalDep = null;
                PredefinedToolKind tool = itemConfiguration.getTool();
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(tool);
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    target = compilerConfiguration.getOutputFile(items[i], conf, false);
                    if (compiler != null && compiler.getDescriptor() != null) {
                        String fromLinker = ""; // NOI18N
                        if (conf.isDynamicLibraryConfiguration()) {
                            if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                if (compiler.getKind() != PredefinedToolKind.Assembler) {
                                    fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                                }
                            }
                        }
                        command += compilerConfiguration.getOptions(compiler);

                        if(provider != null) {
                            if(provider.isProCItem(items[i])) {
                                command += provider.getCompileOptions(items[i], conf);
                            }
                        }

                        command += fromLinker + " "; // NOI18N
                        if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                            command = "${RM} \"$@.d\"\n\t" + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                        }
                        if (items[i].hasHeaderOrSourceExtension(false, false)) {
                            String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                            if (flags == null) {
                                command = "# command to precompile header "; // NOI18N
                                comment = "Current compiler does not support header precompilation"; // NOI18N
                            } else {
                                command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                            }
                        } else {
                            command += compiler.getDescriptor().getOutputObjectFileFlags() + target + " "; // NOI18N
                        }
                        command += file;
                    }
                    additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                } else if (tool == PredefinedToolKind.CustomTool) {
                    CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                    if (customToolConfiguration.getModified()) {
                        target = customToolConfiguration.getOutputs().getValue();
                        command = customToolConfiguration.getCommandLine().getValue();
                        comment = customToolConfiguration.getDescription().getValue();
                        additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                    } else {
                        continue;
                    }
                } else {
                    assert false;
                }
                StringTokenizer tokennizer = new StringTokenizer(target);
                StringBuilder foldersBuffer = new StringBuilder();
                while (tokennizer.hasMoreTokens()) {
                    String dir = CndPathUtilities.getDirName(tokennizer.nextToken());
                    if (dir != null) {
                        foldersBuffer.append(dir);
                        foldersBuffer.append(" "); // NOI18N
                    }
                }
                folders = foldersBuffer.toString().trim();
                bw.write("\n"); // NOI18N

                if (target.contains(" ")) { // NOI18N
                    bw.write(".NO_PARALLEL:" + target + "\n"); // NOI18N
                }
                bw.write(target + ": "); // NOI18N
                // source is first see IZ #253292
                bw.write(file);
                if (additionalDep != null && !additionalDep.isEmpty()) {
                    bw.write(" " + additionalDep); // NOI18N
                }
                // See IZ #151465 for explanation why Makefile is listed as dependency.
                if (conf.getRebuildPropChanged().getValue()) {
                    bw.write(" nbproject/Makefile-"+MakeConfiguration.CND_CONF_MACRO+".mk"); // NOI18N
                }
                bw.write("\n"); // NOI18N

                if (folders != null && folders.length() > 0) {
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                }
                if (comment != null) {
                    bw.write("\t@echo " + comment + "\n"); // NOI18N
                }
                bw.write("\t" + command + "\n"); // NOI18N
            }

            DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
            if(provider != null) {
                provider.writeProCTargets(projectDescriptor, conf, bw);
            }
        }
    }

    public static void writeCompileTestTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (conf.isCompileConfiguration()) {
            List<Folder> tests = getTests(projectDescriptor);
            if (!tests.isEmpty()) {
                for (Folder folder : tests) {
                    Item[] items = folder.getAllItemsAsArray();

                    String target = null;
                    String folders;
                    String file;
                    String command;
                    String comment;
                    String additionalDep;
                    for (int i = 0; i < items.length; i++) {
                        ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf);
                        if (itemConfiguration == null) {
                            continue;
                        }
                        if (itemConfiguration.getExcluded().getValue()) {
                            continue;
                        }
                        CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                        if (compilerSet == null) {
                            continue;
                        }
                        file = CndPathUtilities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath(true)));

                        DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                        if(provider != null) {
                            if(provider.isProCItem(items[i])) {
                                file = provider.getProCOutput(items[i], conf);
                            }
                        }

                        command = ""; // NOI18N
                        comment = null;
                        additionalDep = null;
                        if (itemConfiguration.isCompilerToolConfiguration()) {
                            AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
                            BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                            target = compilerConfiguration.getOutputFile(items[i], conf, false);
                            if (compiler != null && compiler.getDescriptor() != null) {
                                command += compilerConfiguration.getOptions(compiler) + " "; // NOI18N

                                if(provider != null) {
                                    if(provider.isProCItem(items[i])) {
                                        command += provider.getCompileOptions(items[i], conf);
                                    }
                                }

                                if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                                    command = "${RM} \"$@.d\"\n\t" + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                                }
                                if (items[i].hasHeaderOrSourceExtension(false, false)) {
                                    String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                                    if (flags == null) {
                                        command = "# command to precompile header "; // NOI18N
                                        comment = "Current compiler does not support header precompilation"; // NOI18N
                                    } else {
                                        command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                                    }
                                } else {
                                    command += compiler.getDescriptor().getOutputObjectFileFlags() + target + " "; // NOI18N
                                }
                                command += file;
                            }
                            additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                        } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                            CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                            if (customToolConfiguration.getModified()) {
                                target = customToolConfiguration.getOutputs().getValue();
                                command = customToolConfiguration.getCommandLine().getValue();
                                comment = customToolConfiguration.getDescription().getValue();
                                additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                            } else {
                                continue;
                            }
                        } else {
                            assert false;
                        }
                        target = target.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
                        command = command.replace(MakeConfiguration.OBJECTDIR_MACRO, "${TESTDIR}"); // NOI18N
                        folders = CndPathUtilities.getDirName(target);
                        bw.write("\n"); // NOI18N

                        if (target.contains(" ")) { // NOI18N
                            bw.write(".NO_PARALLEL:" + target + "\n"); // NOI18N
                        }
                        // See IZ #151465 for explanation why Makefile is listed as dependency.
                        if (additionalDep != null) {
                            bw.write(target + ": " + file + " " + additionalDep + "\n"); // NOI18N
                        } else {
                            bw.write(target + ": " + file + "\n"); // NOI18N
                        }
                        if (folders != null) {
                            bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                        }
                        if (comment != null) {
                            bw.write("\t@echo " + comment + "\n"); // NOI18N
                        }
                        bw.write("\t" + command + "\n\n"); // NOI18N
                    }
                }
            }

            writeCompileTargetsWithoutMain(projectDescriptor, conf, bw);

            DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
            if(provider != null) {
                provider.writeProCTargets(projectDescriptor, conf, bw);
            }
        }
    }

    private static String changeToNoMain(String target, String name) {
        String nomainTarget;
        if (target.contains("/")) { // NOI18N
            String baseDir = CndPathUtilities.getDirName(target);
            String baseName = CndPathUtilities.getBaseName(target);
            nomainTarget = baseDir + "/" + baseName.replace(name, name + "_nomain"); // NOI18N;
        } else {
            nomainTarget = target.replace(name, name + "_nomain"); // NOI18N
        }
        return nomainTarget;
    }

    public static void writeCompileTargetsWithoutMain(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        Item[] items = getSortedProjectItems(projectDescriptor);
        if (conf.isCompileConfiguration()) {
            String target = null;
            String folders;
            String file;
            String command;
            String comment;
            String additionalDep;
            for (int i = 0; i < items.length; i++) {
                final Folder folder = items[i].getFolder();
                if (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
                    continue;
                }
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration == null){
                    continue;
                }
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
                if (compilerSet == null) {
                    continue;
                }
                file = CndPathUtilities.escapeOddCharacters(CppUtils.normalizeDriveLetter(compilerSet, items[i].getPath(true)));

                DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                if(provider != null) {
                    if(provider.isProCItem(items[i])) {
                        file = provider.getProCOutput(items[i], conf);
                    }
                }

                command = ""; // NOI18N
                comment = null;
                additionalDep = null;
                String name = items[i].getName();
                String extension = FileUtil.getExtension(name);
                name = name.substring(0, name.length() - (extension.isEmpty() ? 0 : extension.length() + 1));
                String nomainTarget;
                if (itemConfiguration.isCompilerToolConfiguration()) {
                    AbstractCompiler compiler = (AbstractCompiler) compilerSet.getTool(itemConfiguration.getTool());
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    target = compilerConfiguration.getOutputFile(items[i], conf, false);

                    nomainTarget = changeToNoMain(target, name);

                    if (compiler != null && compiler.getDescriptor() != null) {
                        String fromLinker = ""; // NOI18N
                        if (conf.isDynamicLibraryConfiguration()) {
                            if (conf.getLinkerConfiguration().getPICOption().getValue()) {
                                if (compiler.getKind() != PredefinedToolKind.Assembler) {
                                    fromLinker = " " + conf.getLinkerConfiguration().getPICOption(compilerSet); // NOI18N
                                }
                            }
                        }
                        command += compilerConfiguration.getOptions(compiler);

                        if(provider != null) {
                            if(provider.isProCItem(items[i])) {
                                command += provider.getCompileOptions(items[i], conf);
                            }
                        }

                        command += fromLinker + " -Dmain=__nomain "; // NOI18N
                        if (conf.getDependencyChecking().getValue() && compiler.getDependencyGenerationOption().length() > 0) {
                            command = "${RM} \"$@.d\";\\\n\t    " + command + compiler.getDependencyGenerationOption() + " "; // NOI18N
                        }
                        if (items[i].hasHeaderOrSourceExtension(false, false)) {
                            String flags = compiler.getDescriptor().getPrecompiledHeaderFlags();
                            if (flags == null) {
                                command = "# command to precompile header "; // NOI18N
                                comment = "Current compiler does not support header precompilation"; // NOI18N
                            } else {
                                command += compiler.getDescriptor().getPrecompiledHeaderFlags() + " "; // NOI18N
                            }
                        } else {
                            command += compiler.getDescriptor().getOutputObjectFileFlags() + nomainTarget + " "; // NOI18N
                        }
                        command += file;
                    }
                    additionalDep = compilerConfiguration.getAdditionalDependencies().getValue();
                } else if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                    CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                    if (customToolConfiguration.getModified()) {
                        target = customToolConfiguration.getOutputs().getValue();
                        command = customToolConfiguration.getCommandLine().getValue();
                        comment = customToolConfiguration.getDescription().getValue();
                        additionalDep = customToolConfiguration.getAdditionalDependencies().getValue();
                    } else {
                        continue;
                    }
                } else {
                    assert false;
                }
                nomainTarget = changeToNoMain(target, name);
                folders = CndPathUtilities.getDirName(target);
                bw.write("\n"); // NOI18N
                if (target.contains(" ")) { // NOI18N
                    bw.write(".NO_PARALLEL:" + target + "\n"); // NOI18N
                }
                // See IZ #151465 for explanation why Makefile is listed as dependency.
                if (additionalDep != null) {
                    bw.write(nomainTarget + ": " + target + " " + file + " " + additionalDep + "\n"); // NOI18N
                } else {
                    bw.write(nomainTarget + ": " + target + " " + file + "\n"); // NOI18N
                }
                if (folders != null) {
                    bw.write("\t${MKDIR} -p " + folders + "\n"); // NOI18N
                }
                if (comment != null) {
                    bw.write("\t@echo " + comment + "\n"); // NOI18N
                }

                bw.write("\t@NMOUTPUT=`${NM} " + target + "`; \\\n"); // NOI18N
                bw.write("\tif (echo \"$$NMOUTPUT\" | ${GREP} '|main$$') || \\\n"); // NOI18N
                bw.write("\t   (echo \"$$NMOUTPUT\" | ${GREP} 'T main$$') || \\\n"); // NOI18N
                bw.write("\t   (echo \"$$NMOUTPUT\" | ${GREP} 'T _main$$'); \\\n"); // NOI18N
                bw.write("\tthen  \\\n"); // NOI18N
                bw.write("\t    " + command + ";\\\n"); // NOI18N
                bw.write("\telse  \\\n"); // NOI18N
                bw.write("\t    ${CP} " + target + " " + nomainTarget + ";\\\n"); // NOI18N
                bw.write("\tfi\n"); // NOI18N
            }
            bw.write("\n"); // NOI18N
        }
    }

    public static void writeRunTestTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (hasTests(projectDescriptor)) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            bw.write("# Run Test Targets\n"); // NOI18N
            bw.write(".test-conf:\n"); // NOI18N

            bw.write("\t@if [ \"${TEST}\" = \"\" ]; \\\n"); // NOI18N
            bw.write("\tthen  \\\n"); // NOI18N
            for (Folder folder : getTests(projectDescriptor)) {
                String target = folder.getFolderConfiguration(conf).getLinkerConfiguration().getOutputValue();
                bw.write("\t    " + target + " || true; \\\n"); // NOI18N
            }
            bw.write("\telse  \\\n"); // NOI18N
            bw.write("\t    ./${TEST} || true; \\\n"); // NOI18N
            bw.write("\tfi\n\n"); // NOI18N
        }
    }

    public static void writeMakefileTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        MakefileConfiguration makefileConfiguration = conf.getMakefileConfiguration();
        String target = makefileConfiguration.getOutput().getValue();
        String cwd = makefileConfiguration.getBuildCommandWorkingDirValue();
        String command = makefileConfiguration.getBuildCommand().getValue();
        bw.write("# Build Targets\n"); // NOI18N
        bw.write(".build-conf: ${BUILD_SUBPROJECTS}\n"); // NOI18N
        //bw.write(target + ":" + "\n"); // NOI18N
        bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(cwd)) + " && " + command + "\n"); // NOI18N
    }

    public static void writeSubProjectBuildTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        bw.write("\n"); // NOI18N
        bw.write("# Subprojects\n"); // NOI18N
        bw.write(".build-subprojects:" + "\n"); // NOI18N
        LibrariesConfiguration librariesConfiguration;
        if (conf.isLinkerConfiguration()) {
            librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();

            for (LibraryItem item : librariesConfiguration.getValue()) {
                if (item instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild()) {
                        continue;
                    }
                    bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(location)) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
                }
            }
        }

        for (LibraryItem.ProjectItem item : conf.getRequiredProjectsConfiguration().getValue()) {
            MakeArtifact makeArtifact = item.getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild()) {
                continue;
            }
            bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(location)) + " && " + makeArtifact.getBuildCommand() + "\n"); // NOI18N
        }
        bw.write("\n"); // NOI18N
    }

    public static void writeSubProjectCleanTargets(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        bw.write("\n"); // NOI18N
        bw.write("# Subprojects\n"); // NOI18N
        bw.write(".clean-subprojects:" + "\n"); // NOI18N
        LibrariesConfiguration librariesConfiguration;
        if (conf.isLinkerConfiguration()) {
            librariesConfiguration = conf.getLinkerConfiguration().getLibrariesConfiguration();

            for (LibraryItem item : librariesConfiguration.getValue()) {
                if (item instanceof LibraryItem.ProjectItem) {
                    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem) item;
                    MakeArtifact makeArtifact = projectItem.getMakeArtifact();
                    String location = makeArtifact.getWorkingDirectory();
                    if (!makeArtifact.getBuild()) {
                        continue;
                    }
                    bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(location)) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
                }
            }
        }

        for (LibraryItem.ProjectItem item : conf.getRequiredProjectsConfiguration().getValue()) {
            MakeArtifact makeArtifact = item.getMakeArtifact();
            String location = makeArtifact.getWorkingDirectory();
            if (!makeArtifact.getBuild()) {
                continue;
            }
            bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(location)) + " && " + makeArtifact.getCleanCommand() + "\n"); // NOI18N
        }
    }

    public static void writeCleanTarget(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        bw.write("# Clean Targets\n"); // NOI18N
        bw.write(".clean-conf: ${CLEAN_SUBPROJECTS}"); // NOI18N
        if (conf.isQmakeConfiguration()) {
            bw.write(" nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk"); // NOI18N
        }
        bw.write('\n'); // NOI18N
        if (conf.isCompileConfiguration()) {
            CompilerSet compilerSet = conf.getCompilerSet().getCompilerSet();
            bw.write("\t${RM} -r " + MakeConfiguration.CND_BUILDDIR_MACRO + '/'+MakeConfiguration.CND_CONF_MACRO+ "\n"); // UNIX path // NOI18N
            String output = getOutput(projectDescriptor, conf, compilerSet);
            String outputDir = CndPathUtilities.getDirName(output);
            Set<String> paths = conf.getLinkerConfiguration().getLibrariesConfiguration().getSharedLibraries();
            if (!paths.isEmpty()) {
                bw.write("\t${RM} -r"); //NOI18N
                for (String path : paths) {
                    String baseName = CndPathUtilities.getBaseName(path);
                    bw.write(" " + outputDir + "/" + baseName); //NOI18N
                }
                bw.write("\n"); //NOI18N
                bw.write("\t${RM} " + output + "\n"); //NOI18N
            }
            if (compilerSet != null
                    && compilerSet.getCompilerFlavor().isSunStudioCompiler()
                    && conf.hasCPPFiles(projectDescriptor)) {
                bw.write("\t${CCADMIN} -clean" + "\n"); // NOI18N
            }
            if (conf.hasFortranFiles(projectDescriptor)) {
                bw.write("\t${RM} *.mod" + "\n"); // NOI18N
            }

            // Also clean output from custom tool
            Item[] items = getSortedProjectItems(projectDescriptor);
            for (int i = 0; i < items.length; i++) {
                ItemConfiguration itemConfiguration = items[i].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[i].getPath()));
                if (itemConfiguration == null){
                    continue;
                }
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                if (itemConfiguration.getTool() == PredefinedToolKind.CustomTool) {
                    if(itemConfiguration.getCustomToolConfiguration().getModified()) {
                        bw.write("\t${RM} " + itemConfiguration.getCustomToolConfiguration().getOutputs().getValue() + "\n"); // NOI18N
                    }
                } else if(itemConfiguration.isCompilerToolConfiguration()) {
                    CustomToolConfiguration customToolConfiguration = itemConfiguration.getCustomToolConfiguration();
                    if (customToolConfiguration != null) {
                        if (compilerSet != null) {
                            DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                            if(provider != null) {
                                if(provider.isProCItem(items[i])) {
                                    String target = provider.getProCOutput(items[i], conf);
                                    bw.write("\t${RM} " + target + "\n"); // NOI18N
                                }
                            }
                        }
                    }
                }
            }
        } else if (conf.isMakefileConfiguration()) {
            MakefileConfiguration makefileConfiguration = conf.getMakefileConfiguration();
            String target = makefileConfiguration.getOutput().getValue();
            String cwd = makefileConfiguration.getBuildCommandWorkingDirValue();
            String command = makefileConfiguration.getCleanCommand().getValue();

            bw.write("\tcd " + CndPathUtilities.escapeOddCharacters(CndPathUtilities.normalizeSlashes(cwd)) + " && " + command + "\n"); // NOI18N
        } else if (conf.isQmakeConfiguration()) {
            bw.write("\t"+MakeArtifact.MAKE_MACRO+" -f nbproject/qt-"+MakeConfiguration.CND_CONF_MACRO+".mk distclean\n"); // NOI18N
        }

        writeSubProjectCleanTargets(projectDescriptor, conf, bw);
    }

    public static void writeDependencyChecking(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, Writer bw) throws IOException {
        if (conf.getDependencyChecking().getValue() && !conf.isMakefileConfiguration() && !conf.isQmakeConfiguration() && conf.getCompilerSet().getCompilerSet() != null) {
            // if conf.getCompilerSet().getCompilerSet() == null and we write this to makefile,
            // make would give confusing error message (see IZ#168540)
            bw.write("\n"); // NOI18N
            bw.write("# Enable dependency checking\n"); // NOI18N
            bw.write(".dep.inc: .depcheck-impl\n"); // NOI18N
            bw.write("\n"); // NOI18N
            bw.write("include .dep.inc\n"); // NOI18N
        }
    }

    private static String getOutput(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf, CompilerSet compilerSet) {
        String output = conf.getOutputValue();
        if (!conf.getDevelopmentHost().isLocalhost() && ! output.startsWith("$") && ! CndPathUtilities.isPathAbsolute(output)) { //NOI18N
            PathMap mapper = RemoteSyncSupport.getPathMap(projectDescriptor.getProject());
            if (mapper != null) {
                output = mapper.getRemotePath(output, true);
                if (output == null) {
                    output = conf.getOutputValue();
                }
            } else {
                LOGGER.log(Level.SEVERE, "Path Mapper not found for project {0} - using local path {1}", new Object[]{projectDescriptor.getProject(), output}); // NOI18N
            }
        }
        switch (conf.getDevelopmentHost().getBuildPlatform()) {
            case PlatformTypes.PLATFORM_WINDOWS:
                switch (conf.getConfigurationType().getValue()) {
                    case MakeConfiguration.TYPE_APPLICATION:
                    case MakeConfiguration.TYPE_DB_APPLICATION:
                    case MakeConfiguration.TYPE_QT_APPLICATION:
                        output = mangleAppnameWin(output);
                        break;
                }
                break;
        }
        if (compilerSet != null) {
            output = CppUtils.normalizeDriveLetter(compilerSet, output);
        }
        return output;
    }

    private static String mangleAppnameWin(String original) {
        if (original.endsWith(".exe")) { // NOI18N
            return original;
        } else {
            return original + ".exe"; // NOI18N
        }
    }

    public static String getObjectDir(MakeConfiguration conf) {
        return MakeConfiguration.CND_BUILDDIR_MACRO + '/' + MakeConfiguration.CND_CONF_MACRO + '/' + MakeConfiguration.CND_PLATFORM_MACRO; // UNIX path // NOI18N
    }
    
    public static String getTestObjectDir(MakeConfiguration conf) {
        return MakeConfiguration.CND_BUILDDIR_MACRO + '/' + MakeConfiguration.CND_CONF_MACRO + '/' + MakeConfiguration.CND_PLATFORM_MACRO + '/' + "tests"; // UNIX path // NOI18N
    }

    private static String getObjectFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        Item[] items = getSortedProjectItems(projectDescriptor);
        StringBuilder linkObjects = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            for (int x = 0; x < items.length; x++) {
                final Folder folder = items[x].getFolder();
                if (folder.isTest() || folder.isTestLogicalFolder() || folder.isTestRootFolder()) {
                    continue;
                }
                ItemConfiguration itemConfiguration = items[x].getItemConfiguration(conf); //ItemConfiguration)conf.getAuxObject(ItemConfiguration.getId(items[x].getPath()));
                if (itemConfiguration == null){
                    continue;
                }
                //String commandLine = ""; // NOI18N
                if (itemConfiguration.getExcluded().getValue()) {
                    continue;
                }
                if (!itemConfiguration.isCompilerToolConfiguration()) {
                    continue;
                }
                if (items[x].hasHeaderOrSourceExtension(false, false)) {
                    continue;
                }
                BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                linkObjects.append(" \\\n\t"); // NOI18N
                linkObjects.append(compilerConfiguration.getOutputFile(items[x], conf, false));
            }
        }
        return linkObjects.toString();
    }
    
    private static String getTestObjectFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        Item[] items = getSortedProjectItems(projectDescriptor);
        StringBuilder linkObjects = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            for (int x = 0; x < items.length; x++) {
                final Folder folder = items[x].getFolder();
                if (folder.isTest()) {
                    ItemConfiguration itemConfiguration = items[x].getItemConfiguration(conf);
                    BasicCompilerConfiguration compilerConfiguration = itemConfiguration.getCompilerConfiguration();
                    if (compilerConfiguration != null) {
                        String outputFile = compilerConfiguration.getOutputFile(items[x], conf, false);
                        linkObjects.append(" \\\n\t"); // NOI18N
                        linkObjects.append(outputFile);
                    }
                }
            }
        }
        return linkObjects.toString();
    }

    private static String getTestTargetFiles(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration conf) {
        StringBuilder testTargets = new StringBuilder();
        if (conf.isCompileConfiguration()) {
            for (Folder folder : getTests(projectDescriptor)) {
                testTargets.append(" \\\n\t"); // NOI18N
                String target = folder.getFolderConfiguration(conf).getLinkerConfiguration().getOutputValue();
                testTargets.append(target);
            }
        }
        return testTargets.toString();
    }

    private void writeMakefileVariables(MakeConfigurationDescriptor conf, Collection<MakeConfiguration> okConfs) throws IOException {
        FileObject nbprojectFileObject = projectDescriptor.getNbprojectFileObject();
        if (nbprojectFileObject == null) {
            return;
        }
        OutputStream os = null;
        BufferedWriter bw = null;
        try {
            Map<String, String> old = getOldVariables(nbprojectFileObject);
            FileObject vars = FileUtil.createData(nbprojectFileObject, MakeConfiguration.MAKEFILE_VARIABLES);
            Charset encoding = FileEncodingQuery.getEncoding(vars);
            os = SmartOutputStream.getSmartOutputStream(vars);
            bw = new BufferedWriter(new OutputStreamWriter(os, encoding));
            writeMakefileFixedVariablesBody(bw, okConfs, old);
            writeMakefileVariablesRedirector(bw);
        } finally {
            closeWriter(bw);
            closeOutputStream(os);
        }

        FileObject nbPrivateProjectFileObject = projectDescriptor.getNbPrivateProjectFileObject();
        if (nbPrivateProjectFileObject == null) {
            return;
        }
        try {
            Map<String, String> old = getOldVariables(nbPrivateProjectFileObject);
            FileObject vars = FileUtil.createData(nbPrivateProjectFileObject, MakeConfiguration.MAKEFILE_VARIABLES);
            Charset encoding = FileEncodingQuery.getEncoding(vars);
            os = SmartOutputStream.getSmartOutputStream(vars);
            bw = new BufferedWriter(new OutputStreamWriter(os, encoding));
            writeMakefilePrivateVariablesBody(bw, okConfs, old);
        } finally {
            closeWriter(bw);
            closeOutputStream(os);
        }
    }

    private Map<String, String> getOldVariables(FileObject nbprojectFileObject) throws IOException {
        Map<String, String> old = new HashMap<>();
        FileObject oldVars = nbprojectFileObject.getFileObject(MakeConfiguration.MAKEFILE_VARIABLES);
        BufferedReader reader = null;
        try {
            if (oldVars != null && oldVars.isValid()) {
                Charset encoding = FileEncodingQuery.getEncoding(oldVars);
                reader = new BufferedReader(new InputStreamReader(oldVars.getInputStream(), encoding));
                String current = null;
                StringBuilder currentBuf = null;
                for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                    line = line.trim();
                    if (line.startsWith("#")) { // NOI18N
                        if (current != null) {
                            old.put(current, currentBuf.toString());
                        }
                        current = null;
                        currentBuf = null;
                    }
                    // # Win_Debug configuration
                    if (line.startsWith("# ")&& line.endsWith(" configuration")) { // NOI18N
                        current = line.substring(2, line.length() - 14);
                        currentBuf = new StringBuilder();
                    }
                    if (current != null) {
                        currentBuf.append(line);
                        currentBuf.append('\n'); // NOI18N
                    }
                }
                if (current != null) {
                    old.put(current, currentBuf.toString());
                }
            }
        } finally {
            closeReader(reader);
        }

        return old;
    }

    private void writeMakefileVariablesRedirector(BufferedWriter bw) throws IOException {
        bw.write("#\n"); // NOI18N
        bw.write("# include compiler specific variables\n"); // NOI18N

        bw.write("#\n"); // NOI18N
        bw.write("# dmake command\n"); // NOI18N
        bw.write("ROOT:sh = test -f nbproject/private/Makefile-variables.mk || \\\n"); // NOI18N
	bw.write("\t(mkdir -p nbproject/private && touch nbproject/private/Makefile-variables.mk)\n"); // NOI18N

        bw.write("#\n"); // NOI18N
        bw.write("# gmake command\n"); // NOI18N
        bw.write(".PHONY: $(shell test -f nbproject/private/Makefile-variables.mk || (mkdir -p nbproject/private && touch nbproject/private/Makefile-variables.mk))\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("include nbproject/private/Makefile-variables.mk\n"); // NOI18N
    }

    private void writeMakefileFixedVariablesBody(BufferedWriter bw, Collection<MakeConfiguration> okConfs, Map<String, String> old) throws IOException {
        bw.write("#\n"); // NOI18N
        bw.write("# Generated - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# NOCDDL\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("CND_BASEDIR=`pwd`\n"); // NOI18N
        bw.write("CND_BUILDDIR=" + MakeConfiguration.BUILD_FOLDER + "\n"); // NOI18N
        bw.write("CND_DISTDIR=" + MakeConfiguration.DIST_FOLDER + "\n"); // NOI18N

        Configuration[] confs = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration makeConf = (MakeConfiguration) confs[i];
            if (okConfs.contains(makeConf)) {
                bw.write("# " + makeConf.getName() + " configuration\n"); // NOI18N
                bw.write("CND_PLATFORM_" + makeConf.getName() + "=" + makeConf.getVariant()); // NOI18N
                bw.write("\n"); // NOI18N // NOI18N
                // output artifact
                String outputPath = makeConf.expandMacros(makeConf.getOutputValue());
                String outputDir = CndPathUtilities.getDirName(outputPath);
                if (outputDir == null) {
                    outputDir = ""; // NOI18N
                }
                String outputName = CndPathUtilities.getBaseName(outputPath);
                bw.write("CND_ARTIFACT_DIR_" + makeConf.getName() + "=" + outputDir); // NOI18N
                bw.write("\n"); // NOI18N
                bw.write("CND_ARTIFACT_NAME_" + makeConf.getName() + "=" + outputName); // NOI18N
                bw.write("\n"); // NOI18N
                bw.write("CND_ARTIFACT_PATH_" + makeConf.getName() + "=" + outputPath); // NOI18N
                bw.write("\n"); // NOI18N
                // packaging artifact
                PackagerDescriptor packager = PackagerManager.getDefault().getPackager(makeConf.getPackagingConfiguration().getType().getValue());
                outputPath = makeConf.expandMacros(makeConf.getPackagingConfiguration().getOutputValue());
                if (!packager.isOutputAFolder()) {
                    outputDir = CndPathUtilities.getDirName(outputPath);
                    if (outputDir == null) {
                        outputDir = ""; // NOI18N
                    }
                    outputName = CndPathUtilities.getBaseName(outputPath);
                } else {
                    outputDir = outputPath;
                    outputPath = ""; // NOI18N
                    outputName = ""; // NOI18N
                }
                bw.write("CND_PACKAGE_DIR_" + makeConf.getName() + "=" + outputDir); // NOI18N
                bw.write("\n"); // NOI18N
                bw.write("CND_PACKAGE_NAME_" + makeConf.getName() + "=" + outputName); // NOI18N
                bw.write("\n"); // NOI18N
                bw.write("CND_PACKAGE_PATH_" + makeConf.getName() + "=" + outputPath); // NOI18N
                bw.write("\n"); // NOI18N
            } else {
                if (old.containsKey(makeConf.getName())) {
                    bw.write(old.get(makeConf.getName()));
                }
            }
        }
    }

    private void writeMakefilePrivateVariablesBody(BufferedWriter bw, Collection<MakeConfiguration> okConfs, Map<String, String> old) throws IOException {
        bw.write("#\n"); // NOI18N
        bw.write("# Generated - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# NOCDDL\n"); // NOI18N
        bw.write("#\n"); // NOI18N

        Configuration[] confs = projectDescriptor.getConfs().toArray();
        for (int i = 0; i < confs.length; i++) {
            MakeConfiguration makeConf = (MakeConfiguration) confs[i];
            if (okConfs.contains(makeConf)) {
                bw.write("# " + makeConf.getName() + " configuration\n"); // NOI18N
                // Sys includes
                DatabaseProjectProvider provider = Lookup.getDefault().lookup(DatabaseProjectProvider.class);
                if(provider != null) {
                    provider.writePrivateVariables(makeConf, bw);
                }
            } else {
                if (old.containsKey(makeConf.getName())) {
                    bw.write(old.get(makeConf.getName()));
                }
            }
        }
    }

    private void writePackagingScript(MakeConfiguration conf) {
        if (conf.getConfigurationType().getValue() == MakeConfiguration.TYPE_MAKEFILE) {
            return;
        }
        if (conf.getPackagingConfiguration().getFiles().getValue().isEmpty()) {
            // Nothing to do
            return;
        }

        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(conf.getPackagingConfiguration().getType().getValue());
        if (packager == null || packager instanceof DummyPackager) {
            return;
        }

        OutputStream os;
        final String scriptName = getPackageScriptName(conf); // NOI18N
        FileObject projectBaseFO = projectDescriptor.getProject().getProjectDirectory();
        if (projectBaseFO == null) {
            return;
        }
        FileObject nbProjectFO = projectBaseFO.getFileObject(MakeConfiguration.NBPROJECT_FOLDER);
        if (nbProjectFO == null) {
            return;
        }
        FileObject outputFO = null;
        try {
            outputFO = nbProjectFO.getFileObject(scriptName); // UNIX path // NOI18N
            if (outputFO == null) {
                outputFO = nbProjectFO.createData(scriptName);
            }
            os = SmartOutputStream.getSmartOutputStream(outputFO);
        } catch (Exception e) {
            // FIXUP
            System.err.println("Cannot open for writing " + nbProjectFO + '/' + scriptName); // NOI18N
            e.printStackTrace(System.err);
            return;
        }
        Charset encoding = FileEncodingQuery.getEncoding(outputFO);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(os, encoding));
        try {
            writePackagingScriptBody(bw, conf);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            // FIXUP
        }
    }

    private void writePackagingScriptBody(BufferedWriter bw, MakeConfiguration conf) throws IOException {
        String tmpDirName = "tmp-packaging"; // NOI18N
        String tmpdir = getObjectDir(conf) + "/" + tmpDirName; // NOI18N
        PackagingConfiguration packagingConfiguration = conf.getPackagingConfiguration();
        String output = packagingConfiguration.getOutputValue();
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(packagingConfiguration.getType().getValue());

        bw.write("#!/bin/bash"); // NOI18N
        if (conf.getPackagingConfiguration().getVerbose().getValue()) {
            bw.write(" -x"); // NOI18N
        }
        bw.write("\n"); // NOI18N
        bw.write("\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("# Generated - do not edit!\n"); // NOI18N
        bw.write("#\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Macros\n"); // NOI18N
        bw.write("TOP=" + "`pwd`" + "\n"); // NOI18N
        bw.write("CND_PLATFORM=" + conf.getVariant() + "\n"); // NOI18N
        bw.write("CND_CONF=" + conf.getName() + "\n"); // NOI18N
        bw.write("CND_DISTDIR=" + MakeConfiguration.DIST_FOLDER + "\n"); // NOI18N
        bw.write("CND_BUILDDIR=" + MakeConfiguration.BUILD_FOLDER + "\n"); // NOI18N
        bw.write("CND_DLIB_EXT=" + conf.getLibraryExtension() + "\n"); // NOI18N
        bw.write("NBTMPDIR=" + tmpdir + "\n"); // NOI18N
        bw.write("TMPDIRNAME=" + tmpDirName + "\n"); // NOI18N
        String projectOutput = conf.getOutputValue();
        if (projectOutput == null || projectOutput.length() == 0) {
            projectOutput = "MissingOutputInProject"; // NOI18N
        }
        bw.write("OUTPUT_PATH=" + projectOutput + "\n"); // NOI18N
        bw.write("OUTPUT_BASENAME=" + CndPathUtilities.getBaseName(projectOutput) + "\n"); // NOI18N
        bw.write("PACKAGE_TOP_DIR=" + (packagingConfiguration.getTopDirValue().length() > 0 ? packagingConfiguration.getTopDirValue() + "/" : "") + "\n"); // NOI18N
        bw.write("\n"); // NOI18N

        bw.write("# Functions\n"); // NOI18N
        bw.write("function checkReturnCode\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    rc=$?\n"); // NOI18N
        bw.write("    if [ $rc != 0 ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("        exit $rc\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N
        bw.write("function makeDirectory\n"); // NOI18N
        bw.write("# $1 directory path\n"); // NOI18N
        bw.write("# $2 permission (optional)\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    mkdir -p \"$1\"\n"); // NOI18N
        bw.write("    checkReturnCode\n"); // NOI18N
        bw.write("    if [ \"$2\" != \"\" ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("      chmod $2 \"$1\"\n"); // NOI18N
        bw.write("      checkReturnCode\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N
        bw.write("function copyFileToTmpDir\n"); // NOI18N
        bw.write("# $1 from-file path\n"); // NOI18N
        bw.write("# $2 to-file path\n"); // NOI18N
        bw.write("# $3 permission\n"); // NOI18N
        bw.write("{\n"); // NOI18N
        bw.write("    cp \"$1\" \"$2\"\n"); // NOI18N
        bw.write("    checkReturnCode\n"); // NOI18N
        bw.write("    if [ \"$3\" != \"\" ]\n"); // NOI18N
        bw.write("    then\n"); // NOI18N
        bw.write("        chmod $3 \"$2\"\n"); // NOI18N
        bw.write("        checkReturnCode\n"); // NOI18N
        bw.write("    fi\n"); // NOI18N
        bw.write("}\n"); // NOI18N

        bw.write("\n"); // NOI18N
        bw.write("# Setup\n"); // NOI18N
        bw.write("cd \"${TOP}\"\n"); // NOI18N

        if (packager.isOutputAFolder()) {
            bw.write("mkdir -p " + output + "\n"); // NOI18N
        } else {
            bw.write("mkdir -p " + CndPathUtilities.getDirName(output) + "\n"); // NOI18N
        }
        bw.write("rm -rf ${NBTMPDIR}\n"); // NOI18N
        bw.write("mkdir -p ${NBTMPDIR}\n"); // NOI18N
        bw.write("\n"); // NOI18N

        packager.getShellFileWriter().writeShellScript(bw, conf, packagingConfiguration);

        bw.write("# Cleanup\n"); // NOI18N
        bw.write("cd \"${TOP}\"\n"); // NOI18N
        bw.write("rm -rf ${NBTMPDIR}\n"); // NOI18N
    }

    private static boolean hasTests(MakeConfigurationDescriptor projectDescriptor) {
        return !getTests(projectDescriptor).isEmpty();
    }

    private static List<Folder> getTests(MakeConfigurationDescriptor projectDescriptor) {
        Folder root = projectDescriptor.getLogicalFolders();
        Folder testRootFolder = null;
        for (Folder folder : root.getFolders()) {
            if (folder.isTestRootFolder()) {
                testRootFolder = folder;
                break;
            }
        }
        if (testRootFolder != null) {
            return testRootFolder.getAllTests();
        }
        return Collections.<Folder>emptyList();
    }

    /** Look up i18n strings here */
    private static String getString(String s) {
        return NbBundle.getMessage(ConfigurationMakefileWriter.class, s);
    }

    private static String getString(String s, String arg1) {
        return NbBundle.getMessage(ConfigurationMakefileWriter.class, s, arg1);
    }

    private static String getString(String s, String arg1, String arg2) {
        return NbBundle.getMessage(ConfigurationMakefileWriter.class, s, arg1, arg2);
    }

    private void closeReader(BufferedReader br) {
        if (br != null) {
            try {
                br.close();
            } catch (IOException ex) {
            }
        }
    }

    private void closeWriter(BufferedWriter bw) {
        if (bw != null) {
            try {
                bw.flush();
                bw.close();
            } catch (IOException ex) {
            }
        }
    }

    private void closeOutputStream(OutputStream os) {
        if (os != null) {
            try {
                os.flush();
                os.close();
            } catch (IOException ex) {
            }
        }
    }

    private void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException ex) {
            }
        }
    }
}
