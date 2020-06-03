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

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.toolchain.AbstractCompiler;
import org.netbeans.modules.cnd.api.toolchain.CompilerSet;
import org.netbeans.modules.cnd.api.toolchain.PredefinedToolKind;
import org.netbeans.modules.cnd.makeproject.api.support.SmartOutputStream;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCCompilerConfiguration.OptionToString;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.ItemConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.VectorConfiguration;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Writes qmake project (*.pro file) for given configuration.
 *
 */
public class QmakeProjectWriter {

    private static final String PKGCONFIG_BINARY = "pkg-config";   // NOI18N

    /*
     * Project file name is constructed as prefix + confName + suffix.
     */

    // Need to use unix-style separator, FileUtil.createData is unable to create folders otherwise
    private static final String PROJECT_PREFIX = MakeConfiguration.NBPROJECT_FOLDER + "/qt-"; // NOI18N
    private static final String PROJECT_SUFFIX = ".pro"; // NOI18N

    /**
     * Qmake variables.
     */
    private static enum Variable {
        TEMPLATE,
        DESTDIR,
        TARGET,
        VERSION,
        CONFIG,
        PKGCONFIG,
        QT,
        SOURCES,
        HEADERS,
        FORMS,
        RESOURCES,
        TRANSLATIONS,
        DEFINES,
        INCLUDEPATH,
        LIBS,
        QMAKE_CC,
        QMAKE_CXX,
        MOC_DIR,
        RCC_DIR,
        UI_DIR,
        OBJECTS_DIR,
        QMAKE_CXXFLAGS
    }

    /**
     * Qmake variable operations.
     */
    private static enum Operation {
        SET("="), // NOI18N
        ADD("+="), // NOI18N
        SUB("-="); // NOI18N

        private final String op;

        private Operation(String op) {
            this.op = op;
        }

        public String getOp() {
            return op;
        }
    }

    /**
     * Project descriptor.
     */
    private final MakeConfigurationDescriptor projectDescriptor;

    /**
     * Configuration that needs a qmake project.
     */
    private final MakeConfiguration configuration;

    /**
     * Constructs new instance.
     *
     * @param projectDescriptor  project descriptor
     * @param configuration  configuration that needs a qmake project
     */
    public QmakeProjectWriter(MakeConfigurationDescriptor projectDescriptor, MakeConfiguration configuration) {
        this.projectDescriptor = projectDescriptor;
        this.configuration = configuration;
    }

    /**
     * Writes qmake project for configuration.
     *
     * @throws java.io.IOException  if an error occurs when writing the file
     */
    public void write() throws IOException {
        if (configuration.isQmakeConfiguration()) {
            final FSPath baseFSPath = configuration.getBaseFSPath();
            FileObject confBaseFO = baseFSPath.getFileObject();
            if (confBaseFO == null) {
                throw new FileNotFoundException("FileObject not found: " + baseFSPath); //NOI18N
            }
            FileObject qmakeProjectFO = FileUtil.createData(confBaseFO, PROJECT_PREFIX + configuration.getName() + PROJECT_SUFFIX);
            BufferedWriter bw = null;
            try {
                Charset encoding = FileEncodingQuery.getEncoding(qmakeProjectFO);
                bw = new BufferedWriter(new OutputStreamWriter(SmartOutputStream.getSmartOutputStream(qmakeProjectFO), encoding));
                write(bw);
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
        }
    }

    private void write(BufferedWriter bw) throws IOException {
        bw.write("# This file is generated automatically. Do not edit.\n"); // NOI18N
        bw.write("# Use project properties -> Build -> Qt -> Expert -> Custom Definitions.\n"); // NOI18N
        write(bw, Variable.TEMPLATE, Operation.SET, getTemplate());
        write(bw, Variable.DESTDIR, Operation.SET, expandAndQuote(configuration.getQmakeConfiguration().getDestdirValue()));
        write(bw, Variable.TARGET, Operation.SET, expandAndQuote(configuration.getQmakeConfiguration().getTargetValue()));
        write(bw, Variable.VERSION, Operation.SET, expandAndQuote(configuration.getQmakeConfiguration().getVersion().getValue()));
        // debug_and_release is enabled by default on Windows -- we don't need it
        // app_bundle and lib_bundle get enabled on MacOS -- explicitly disable as well
        write(bw, Variable.CONFIG, Operation.SUB, "debug_and_release app_bundle lib_bundle"); // NOI18N
        write(bw, Variable.CONFIG, Operation.ADD, getConfig());
        write(bw, Variable.PKGCONFIG, Operation.ADD, getPkgConfig());
        write(bw, Variable.QT, Operation.SET, configuration.getQmakeConfiguration().getEnabledModules());

        Item[] items = ConfigurationMakefileWriter.getSortedProjectItems(projectDescriptor);
        write(bw, Variable.SOURCES, Operation.ADD, getItems(items, MIMENames.C_MIME_TYPE, MIMENames.CPLUSPLUS_MIME_TYPE));
        write(bw, Variable.HEADERS, Operation.ADD, getItems(items, MIMENames.HEADER_MIME_TYPE));
        write(bw, Variable.FORMS, Operation.ADD, getItems(items, MIMENames.QT_UI_MIME_TYPE));
        write(bw, Variable.RESOURCES, Operation.ADD, getItems(items, MIMENames.QT_RESOURCE_MIME_TYPE));
        write(bw, Variable.TRANSLATIONS, Operation.ADD, getItems(items, MIMENames.QT_TRANSLATION_MIME_TYPE));

        write(bw, Variable.OBJECTS_DIR, Operation.SET,
                expandAndQuote(ConfigurationMakefileWriter.getObjectDir(configuration)));
        write(bw, Variable.MOC_DIR, Operation.SET,
                expandAndQuote(configuration.getQmakeConfiguration().getMocDir().getValue()));
        write(bw, Variable.RCC_DIR, Operation.SET,
                expandAndQuote(configuration.getQmakeConfiguration().getRccDir().getValue()));
        write(bw, Variable.UI_DIR, Operation.SET,
                expandAndQuote(configuration.getQmakeConfiguration().getUiDir().getValue()));

        write(bw, Variable.QMAKE_CC, Operation.SET,
                ConfigurationMakefileWriter.getCompilerName(configuration, PredefinedToolKind.CCompiler));
        write(bw, Variable.QMAKE_CXX, Operation.SET,
                ConfigurationMakefileWriter.getCompilerName(configuration, PredefinedToolKind.CCCompiler));

        CompilerSet compilerSet = configuration.getCompilerSet().getCompilerSet();
        OptionToString defineVisitor = new OptionToString(compilerSet, null);
        write(bw, Variable.DEFINES, Operation.ADD,
                configuration.getCCCompilerConfiguration().getPreprocessorConfiguration().toString(defineVisitor));
        IncludeToString includeVisitor = new IncludeToString(compilerSet);
        write(bw, Variable.INCLUDEPATH, Operation.ADD,
                configuration.getCCCompilerConfiguration().getIncludeDirectories().toString(includeVisitor));
        write(bw, Variable.LIBS, Operation.ADD, getLibs());
        if (configuration.getCCCompilerConfiguration().getCppStandard().getValue() != CCCompilerConfiguration.STANDARD_DEFAULT) {
            AbstractCompiler ccCompiler = compilerSet == null ? null : (AbstractCompiler)compilerSet.getTool(PredefinedToolKind.CCCompiler);
            if (ccCompiler != null) {
                bw.write("equals(QT_MAJOR_VERSION, 4) {\n"); //NOI18N
                write(bw, Variable.QMAKE_CXXFLAGS, Operation.ADD,
                        ccCompiler.getCppStandardOptions(configuration.getCCCompilerConfiguration().getCppStandard().getValue()));
                bw.write("}\n"); //NOI18N
                if (configuration.getCCCompilerConfiguration().getCppStandard().getValue() == CCCompilerConfiguration.STANDARD_CPP11) {
                    bw.write("equals(QT_MAJOR_VERSION, 5) {\n"); //NOI18N
                    write(bw, Variable.CONFIG, Operation.ADD, "c++11"); //NOI18N
                    bw.write("}\n"); //NOI18N
                }
            }
        }

        for (String line : configuration.getQmakeConfiguration().getCustomDefs().getValue()) {
            bw.write(line);
            bw.write('\n'); // NOI18N
        }
    }

    private void write(BufferedWriter bw, Variable var, Operation op, String value) throws IOException {
        bw.write(var.toString());
        bw.write(' '); // NOI18N
        bw.write(op.getOp());
        bw.write(' '); // NOI18N
        bw.write(value);
        bw.write('\n'); // NOI18N
    }

    private void write(BufferedWriter bw, Variable var, Operation op, List<String> values) throws IOException {
        bw.write(var.toString());
        bw.write(' '); // NOI18N
        bw.write(op.getOp());
        for (String value : values) {
            bw.write(' '); // NOI18N
            bw.write(value);
        }
        bw.write('\n'); // NOI18N
    }

    private List<String> getItems(Item[] items, String... mimeTypes) {
        List<String> list = new ArrayList<>();
        for (Item item : items) {
            ItemConfiguration itemConf = item.getItemConfiguration(configuration);
            if (itemConf == null) {
                continue;
            }
            if (itemConf.getExcluded().getValue()) {
                continue;
            }
            FileObject fo = item.getFileObject();
            if (fo == null) {
                continue;
            }
            String actualMimeType = fo.getMIMEType();
            for (String mimeType : mimeTypes) {
                if (mimeType.equals(actualMimeType)) {
                    list.add(CndPathUtilities.quoteIfNecessary(item.getPath()));
                    break;
                }
            }
        }
        return list;
    }

    private String getTemplate() {
        switch (configuration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_QT_APPLICATION:
                return "app"; // NOI18N
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                return "lib"; // NOI18N
            default:
                return ""; // NOI18N
        }
    }

    private List<String> getConfig() {
        List<String> list = new ArrayList<>();
        switch (configuration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                list.add("dll"); // NOI18N
                break;
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                list.add("staticlib"); // NOI18N
                break;
        }
        if (isPkgConfigUsed()) {
            list.add("link_pkgconfig"); // NOI18N
        }
        list.add(configuration.getQmakeConfiguration().getBuildMode().getOption());
        return list;
    }

    private boolean isPkgConfigUsed() {
        for (LibraryItem lib : configuration.getLinkerConfiguration().getLibrariesConfiguration().getValue()) {
            if (lib.getType() == LibraryItem.OPTION_ITEM) {
                LibraryItem.OptionItem option = (LibraryItem.OptionItem) lib;
                if (option.getLibraryOption().contains(PKGCONFIG_BINARY)) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<String> getPkgConfig() {
        List<LibraryItem> libraries = configuration.getLinkerConfiguration().getLibrariesConfiguration().getValue();
        List<String> list = new ArrayList<>(libraries.size());
        for (LibraryItem lib : libraries) {
            if (lib.getType() == LibraryItem.OPTION_ITEM) {
                LibraryItem.OptionItem option = (LibraryItem.OptionItem) lib;
                if (option.getLibraryOption().contains(PKGCONFIG_BINARY)) {
                    list.add(option.getLibraryOption().replaceAll("`", "").replace(PKGCONFIG_BINARY, "").replace("--libs", "").trim()); // NOI18N
                }
            }
        }
        return list;
    }

    private String getLibs() {
        StringBuilder buf = new StringBuilder();
        CompilerSet compilerSet = configuration.getCompilerSet().getCompilerSet();
        LibraryToString libVisitor = new LibraryToString(configuration);
        buf.append(configuration.getLinkerConfiguration().getLibrariesConfiguration().toString(libVisitor));
        if (compilerSet != null) {
            if (0 < buf.length()) {
                buf.append(' '); // NOI18N
            }
            OptionToString dynamicSearchVisitor = new OptionToString(compilerSet,
                    compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getDynamicLibrarySearchFlag());
            buf.append(configuration.getLinkerConfiguration().getDynamicSearch().toString(dynamicSearchVisitor));
        }
        return buf.toString();
    }

    private String expandAndQuote(String s) {
        return CndPathUtilities.quoteIfNecessary(configuration.expandMacros(s));
    }

    private static class LibraryToString implements VectorConfiguration.ToString<LibraryItem> {

        private final MakeConfiguration configuration;

        public LibraryToString(MakeConfiguration configuration) {
            this.configuration = configuration;
        }

        @Override
        public String toString(LibraryItem item) {
            switch (item.getType()) {
                case LibraryItem.PROJECT_ITEM:
                case LibraryItem.LIB_FILE_ITEM:
                    return libFileToOptionsString(item.getPath());
                case LibraryItem.LIB_ITEM:
                case LibraryItem.STD_LIB_ITEM:
                    return item.getOption(configuration);
                case LibraryItem.OPTION_ITEM:
                    LibraryItem.OptionItem option = (LibraryItem.OptionItem) item;
                    if (!option.getLibraryOption().contains(PKGCONFIG_BINARY)) {
                        return item.getOption(configuration);
                    } else {
                        return ""; // NOI18N
                    }
                default:
                    return ""; // NOI18N
            }
        }

        private String libFileToOptionsString(String path) {
            StringBuilder buf = new StringBuilder();
            CompilerSet compilerSet = configuration.getCompilerSet().getCompilerSet();
            if (compilerSet != null && isDynamicLib(path)) {
                String searchOption = compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getDynamicLibrarySearchFlag();
                if (searchOption.length() == 0) {
                    // According to code in PlatformWindows and PlatformMacOSX,
                    // on Windows and MacOS the "-L" option is used
                    // for searching both static and dynamic libraries. (Why?)
                    // Let's be consistent with that behavior. Detect this
                    // special case by empty dynamic_library_search
                    // and use the library_search option instead.
                    searchOption = compilerSet.getCompilerFlavor().getToolchainDescriptor().getLinker().getLibrarySearchFlag();
                }

                buf.append(searchOption);
                String dirName = CndPathUtilities.getDirName(path);
                if (dirName != null && dirName.length() > 0) {
                    buf.append(CndPathUtilities.quoteIfNecessary(dirName));
                    buf.append(' '); // NOI18N
                }
            }
            buf.append(CndPathUtilities.quoteIfNecessary(path));
            return buf.toString();
        }

        private boolean isDynamicLib(String path) {
            return path.endsWith(".dll") || path.endsWith(".dylib") // NOI18N
                    || path.endsWith(".so") || 0 <= path.indexOf(".so."); // NOI18N
        }
    }

    private static class IncludeToString implements VectorConfiguration.ToString<String> {

        private final CompilerSet compilerSet;

        public IncludeToString(CompilerSet compilerSet) {
            this.compilerSet = compilerSet;
        }

        @Override
        public String toString(String item) {
            if (0 < item.length()) {
                if (compilerSet != null) {
                    item = CppUtils.normalizeDriveLetter(compilerSet, item);
                }
                return CndPathUtilities.quoteIfNecessary(item);
            } else {
                return ""; // NOI18N
            }
        }

    }

}
