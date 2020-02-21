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
package org.netbeans.modules.cnd.makeproject.api.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.netbeans.modules.cnd.api.toolchain.PlatformTypes;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.MakeOptions;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerFileElement.FileType;
import org.netbeans.modules.cnd.makeproject.api.PackagerDescriptor;
import org.netbeans.modules.cnd.makeproject.api.PackagerInfoElement;
import org.netbeans.modules.cnd.makeproject.api.PackagerManager;
import org.netbeans.modules.cnd.makeproject.packaging.DummyPackager;

public class PackagingConfiguration implements Cloneable {

    private MakeConfiguration makeConfiguration;    // Types
    private StringConfiguration type;
    private BooleanConfiguration verbose;
    private VectorConfiguration<PackagerInfoElement> info;
    private VectorConfiguration<String> additionalInfo;
    private VectorConfiguration<PackagerFileElement> files;
    private StringConfiguration output;
    private StringConfiguration tool;
    private StringConfiguration options;
    private StringConfiguration topDir;
    // Constructors
    public PackagingConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        type = new StringConfiguration(null, "Tar"); // NOI18N // Fixup: better default...
        verbose = new BooleanConfiguration(true);
        info = new VectorConfiguration<>(null); // NOI18N
        additionalInfo = new VectorConfiguration<>(null); // NOI18N
        files = new VectorConfiguration<>(null); // NOI18N
        output = new StringConfiguration(null, ""); // NOI18N
        tool = new StringConfiguration(null, ""); // NOI18N
        options = new StringConfiguration(null, ""); // NOI18N
        topDir = new StringConfiguration(null, null); // NOI18N

        setDefaultValues();
    }

    public final void setDefaultValues() {
        // Clear default values
        VectorConfiguration<PackagerFileElement> customFiles = new VectorConfiguration<>(null);
        for (PackagerFileElement packagerFileElement : files.getValue()) {
            if (!packagerFileElement.isDefaultValue()) {
                customFiles.add(packagerFileElement);
            }
        }
        if (files.getValue().size() != customFiles.getValue().size()) {
            files.assign(customFiles);
        }

        // Init default values
        String perm = MakeOptions.getInstance().getDefExePerm();
        String packageDir = "${PACKAGE_TOP_DIR}bin"; // NOI18N
        String suffix = ""; // NOI18N

        if (makeConfiguration.isMakefileConfiguration()) {
            perm = MakeOptions.getInstance().getDefExePerm();
            packageDir = "${PACKAGE_TOP_DIR}bin"; // NOI18N
        } else if (makeConfiguration.isApplicationConfiguration()) {
            perm = MakeOptions.getInstance().getDefExePerm();
            packageDir = "${PACKAGE_TOP_DIR}bin"; // NOI18N
            if (makeConfiguration.getDevelopmentHost().getBuildPlatform() == PlatformTypes.PLATFORM_WINDOWS) {
                suffix = ".exe"; // NOI18N
            }
        } else if (makeConfiguration.isLibraryConfiguration()) {
            perm = MakeOptions.getInstance().getDefFilePerm();
            packageDir = "${PACKAGE_TOP_DIR}lib"; // NOI18N
        }
        else {
            assert false;
        }
        if (makeConfiguration.isLinkerConfiguration()) {
            LinkerConfiguration linkerConfiguration = makeConfiguration.getLinkerConfiguration();
            if (linkerConfiguration.getCopyLibrariesConfiguration().getValue()) {
                LibrariesConfiguration librariesConfiguration = linkerConfiguration.getLibrariesConfiguration();

                Set<String> sharedLibraries = librariesConfiguration.getSharedLibraries();
                for (String sharedLibrary : sharedLibraries) {
                    String outputDir = CndPathUtilities.getDirName(linkerConfiguration.getOutputValue());
                    String libName = CndPathUtilities.getBaseName(sharedLibrary);
                    PackagerFileElement elem = new PackagerFileElement(
                            FileType.FILE,
                            outputDir + "/" + libName, // NOI18N
                            packageDir + "/" + libName, // NOI18N
                            perm,
                            MakeOptions.getInstance().getDefOwner(),
                            MakeOptions.getInstance().getDefGroup());
                    elem.setDefaultValue(true);
                    files.add(elem);
                }
            }
        }

        PackagerFileElement elem = new PackagerFileElement(
                FileType.FILE,
                "${OUTPUT_PATH}" + suffix, // NOI18N
                packageDir + "/${OUTPUT_BASENAME}" + suffix, // NOI18N
                perm,
                MakeOptions.getInstance().getDefOwner(),
                MakeOptions.getInstance().getDefGroup());
        elem.setDefaultValue(true);
        files.add(elem);

        // Add default info lists
        List<PackagerInfoElement> infoList = getInfo().getValue();
        if (infoList.isEmpty()) {
            List<PackagerDescriptor> packagerList = PackagerManager.getDefault().getPackagerList();
            for (PackagerDescriptor packagerDescriptor : packagerList) {
                if (packagerDescriptor.hasInfoList()) {
                    infoList.addAll(packagerDescriptor.getDefaultInfoList(makeConfiguration, this));
                }
            }
        }
    }

    public List<PackagerInfoElement> getHeaderSubList(String packager) {
        List<PackagerInfoElement> list = new ArrayList<>();
        List<PackagerInfoElement> headerList = getInfo().getValue();
        for (PackagerInfoElement elem : headerList) {
            if (elem.getPackager().equals(packager)) {
                list.add(elem);
            }
        }
        return list;
    }

    public boolean isModified() {
        if (getType().getModified() || getOutput().getModified() || getOptions().getModified() || getVerbose().getModified() || getTool().getModified()) {
            return true;
        }
        if (files.getValue().size() != 1) {
            return true;
        }
        for (PackagerFileElement elem : files.getValue()) {
            if (!elem.isDefaultValue()) {
                return true;
            }
        }

        List<PackagerInfoElement> headerList = getHeaderSubList(getType().getValue());
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
        if (packager != null && packager.hasInfoList()) {
            if (headerList.size() != packager.getDefaultInfoList(makeConfiguration, this).size()) {
                return true;
            }
        }
        return false;
    }

    // MakeConfiguration
    public void setMakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
    }

    public MakeConfiguration getMakeConfiguration() {
        return makeConfiguration;
    }

    public StringConfiguration getType() {
        return type;
    }

    public void setType(StringConfiguration type) {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(type.getValue());
        if (packager == null) {
            // Doesn't exist. Switch to a dummy packager...
            packager = new DummyPackager(type.getValue());
            PackagerManager.getDefault().addPackagingDescriptor(packager);
        }
        this.type = type;
    }

    public BooleanConfiguration getVerbose() {
        return verbose;
    }

    public void setVerbose(BooleanConfiguration verbose) {
        this.verbose = verbose;
    }

    public VectorConfiguration<PackagerInfoElement> getInfo() {
        return info;
    }

    public void setInfo(VectorConfiguration<PackagerInfoElement> svr4Header) {
        this.info = svr4Header;
    }

    public VectorConfiguration<String> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(VectorConfiguration<String> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public VectorConfiguration<PackagerFileElement> getFiles() {
        setDefaultValues();

        return files;
    }

    public void setFiles(VectorConfiguration<PackagerFileElement> files) {
        this.files = files;
    }

    public void setOutput(StringConfiguration output) {
        this.output = output;
    }

    public StringConfiguration getOutput() {
        return output;
    }

    public void setTool(StringConfiguration output) {
        this.tool = output;
    }

    public StringConfiguration getTool() {
        return tool;
    }

    public void setOptions(StringConfiguration options) {
        this.options = options;
    }

    public StringConfiguration getOptions() {
        return options;
    }

    public void setTopDir(StringConfiguration topDir) {
        this.topDir = topDir;
    }

    public StringConfiguration getTopDir() {
        return topDir;
    }

    // Clone and assign
    public void assign(PackagingConfiguration conf) {
        //setMakeConfiguration(conf.getMakeConfiguration()); // MakeConfiguration should not be assigned
        getType().assign(conf.getType());
        getVerbose().assign(conf.getVerbose());
        getInfo().assign(conf.getInfo());
        getAdditionalInfo().assign(conf.getAdditionalInfo());
        getFiles().assign(conf.getFiles());
        getOutput().assign(conf.getOutput());
        getTool().assign(conf.getTool());
        getOptions().assign(conf.getOptions());
        getTopDir().assign(conf.getTopDir());
    }

    @Override
    public PackagingConfiguration clone() {
        PackagingConfiguration clone = new PackagingConfiguration(getMakeConfiguration());
        clone.setType(getType().clone());
        clone.setVerbose(getVerbose().clone());
        clone.setInfo(getInfo().clone());
        clone.setAdditionalInfo(getAdditionalInfo().clone());
        clone.setFiles(getFiles().clone());
        clone.setOutput(getOutput().clone());
        clone.setTool(getTool().clone());
        clone.setOptions(getOptions().clone());
        clone.setTopDir(getTopDir().clone());
        return clone;
    }

    public String getOutputValue() {
        if (getOutput().getModified()) {
            return getOutput().getValue();
        } else {
            return getOutputDefault();
        }
    }

    public String getTopDirValue() {
        if (getTopDir().getModified()) {
            return getTopDir().getValue();
        } else {
            PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
            if (packager != null) {
                return packager.getTopDir(makeConfiguration, this);
            }
            else {
                return ""; // NOI18N
            }
        }
    }

    public String getOutputName() {
        String outputName = CndPathUtilities.getBaseName(getMakeConfiguration().getBaseDir());
        if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_APPLICATION ||
            getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DB_APPLICATION) {
            outputName = outputName.toLowerCase(Locale.getDefault());
        } else if (getMakeConfiguration().getConfigurationType().getValue() == MakeConfiguration.TYPE_DYNAMIC_LIB) {
            Platform platform = Platforms.getPlatform(getMakeConfiguration().getDevelopmentHost().getBuildPlatform());
            outputName = platform.getLibraryName(outputName);
        }
        outputName = createValidPackageName(outputName);
        return outputName;
    }

    public String getOutputDefault() {
        String outputPath = MakeConfiguration.CND_DISTDIR_MACRO + "/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO+"/package"; // NOI18N
//        String outputName = getOutputName();
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());

        if (!packager.isOutputAFolder()) {
            outputPath += "/" + packager.getOutputFileName(makeConfiguration, this) + "." + packager.getOutputFileSuffix(); // NOI18N
        }

        return outputPath;
    }

    public String getToolValue() {
        if (getTool().getModified()) {
            return getTool().getValue();
        } else {
            return getToolDefault();
        }
    }

    public String getToolDefault() {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
        return packager.getDefaultTool();
    }

    public String getOptionsValue() {
        if (getOptions().getModified()) {
            return getOptions().getValue();
        } else {
            return getOptionsDefault();
        }
    }

    public String getOptionsDefault() {
        PackagerDescriptor packager = PackagerManager.getDefault().getPackager(getType().getValue());
        return packager.getDefaultOptions();
    }

    public String[] getDisplayNames() {
//        return TYPE_DISPLAY_NAMES;
        return PackagerManager.getDefault().getDisplayNames(); // FIXUP?
    }

    public String getDisplayName() {
//        return TYPE_DISPLAY_NAMES[getType().getValue()];
        return PackagerManager.getDefault().getDisplayName(getType().getValue()); // FIXUP?
    }

    public String getName() {
        return getType().getValue();
    }

    public String expandMacros(String s) {
        s = makeConfiguration.expandMacros(s);
        s = CndPathUtilities.expandMacro(s, "${PACKAGE_TOP_DIR}", getTopDirValue().length() > 0 ? getTopDirValue() + "/" : ""); // NOI18N
        return s;
    }

    private String createValidPackageName(String name) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                continue;
            }
            else if (c == ' ') {
                continue;
            }
            stringBuilder.append(c);
        }
        return stringBuilder.toString();
    }


    public PackagerInfoElement findInfoElement(String name) {
        List<PackagerInfoElement> infoList = getInfo().getValue();
        for (PackagerInfoElement elem : infoList) {
            if (elem.getName().equals(name)) {
                return elem;
            }
        }
        return null;
    }

    public String findInfoValueName(String name) {
        List<PackagerInfoElement> infoList = getInfo().getValue();
        for (PackagerInfoElement elem : infoList) {
            if (elem.getName().equals(name)) {
                return elem.getValue();
            }
        }
        return null;
    }
}
