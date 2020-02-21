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

import java.util.StringTokenizer;
import org.netbeans.modules.cnd.utils.CndPathUtilities;

/**
 */
public class QmakeConfiguration implements Cloneable {

    public static final int DEBUG_MODE = 0;
    public static final int RELEASE_MODE = 1;
    private final String[] BUILD_MODE_NAMES = {"Debug", "Release"}; // NOI18N
    private final String[] BUILD_MODE_OPTIONS = {"debug", "release"}; // NOI18N
    private static final String CORE = "core"; // NOI18N
    private static final String GUI = "gui"; // NOI18N
    private static final String WIDGETS = "widgets"; // NOI18N
    private static final String NETWORK = "network"; // NOI18N
    private static final String OPENGL = "opengl"; // NOI18N
    private static final String PHONON = "phonon"; // NOI18N
    private static final String QT3SUPPORT = "qt3support"; // NOI18N
    private static final String PRINTSUPPORT = "printsupport"; // NOI18N
    private static final String SQL = "sql"; // NOI18N
    private static final String SVG = "svg"; // NOI18N
    private static final String WEBKIT = "webkit"; // NOI18N
    private static final String XML = "xml"; // NOI18N
    private final MakeConfiguration makeConfiguration;

    // general
    private StringConfiguration destdir;
    private StringConfiguration target;
    private StringConfiguration version;
    private IntConfiguration buildMode;

    // modules
    private BooleanConfiguration coreEnabled;
    private BooleanConfiguration guiEnabled;
    private BooleanConfiguration widgetsEnabled;
    private BooleanConfiguration networkEnabled;
    private BooleanConfiguration openglEnabled;
    private BooleanConfiguration phononEnabled;
    private BooleanConfiguration qt3SupportEnabled;
    private BooleanConfiguration printSupportEnabled;
    private BooleanConfiguration sqlEnabled;
    private BooleanConfiguration svgEnabled;
    private BooleanConfiguration webkitEnabled;
    private BooleanConfiguration xmlEnabled;

    // intermediate files
    private StringConfiguration mocDir;
    private StringConfiguration rccDir;
    private StringConfiguration uiDir;

    // expert
    private StringConfiguration qmakespec;
    private VectorConfiguration<String> customDefs;

    public QmakeConfiguration(MakeConfiguration makeConfiguration) {
        this.makeConfiguration = makeConfiguration;
        destdir = new StringConfiguration(null, ""); // NOI18N
        target = new StringConfiguration(null, ""); // NOI18N
        version = new StringConfiguration(null, "1.0.0"); // NOI18N
        buildMode = new IntConfiguration(null, 0, BUILD_MODE_NAMES, BUILD_MODE_OPTIONS);
        coreEnabled = new BooleanConfiguration(true);
        guiEnabled = new BooleanConfiguration(true);
        widgetsEnabled = new BooleanConfiguration(true);
        networkEnabled = new BooleanConfiguration(false);
        openglEnabled = new BooleanConfiguration(false);
        phononEnabled = new BooleanConfiguration(false);
        qt3SupportEnabled = new BooleanConfiguration(false);
        printSupportEnabled = new BooleanConfiguration(false);
        sqlEnabled = new BooleanConfiguration(false);
        svgEnabled = new BooleanConfiguration(false);
        xmlEnabled = new BooleanConfiguration(false);
        webkitEnabled = new BooleanConfiguration(false);
        mocDir = new StringConfiguration(null, ""); // NOI18N
        rccDir = new StringConfiguration(null, ""); // NOI18N
        uiDir = new StringConfiguration(null, ""); // NOI18N
        customDefs = new VectorConfiguration<>(null);
        qmakespec = new StringConfiguration(null, ""); // NOI18N
    }

    public String getDestdirValue() {
        if (destdir.getModified()) {
            return destdir.getValue();
        } else {
            return getDestdirDefault();
        }
    }

    public String getDestdirDefault() {
        return MakeConfiguration.CND_DISTDIR_MACRO+"/"+MakeConfiguration.CND_CONF_MACRO+"/"+MakeConfiguration.CND_PLATFORM_MACRO; // NOI18N
    }

    public StringConfiguration getDestdir() {
        return destdir;
    }

    private void setDestdir(StringConfiguration destdir) {
        this.destdir = destdir;
    }

    public String getTargetValue() {
        if (target.getModified()) {
            return target.getValue();
        } else {
            return getTargetDefault();
        }
    }

    public String getTargetDefault() {
        return ConfigurationSupport.makeNameLegal(CndPathUtilities.getBaseName(makeConfiguration.getBaseDir()));
    }

    public StringConfiguration getTarget() {
        return target;
    }

    private void setTarget(StringConfiguration target) {
        this.target = target;
    }

    public StringConfiguration getVersion() {
        return version;
    }

    private void setVersion(StringConfiguration version) {
        this.version = version;
    }

    public String getOutputValue() {
        String dir = getDestdirValue();
        String file = getTargetValue();
        switch (makeConfiguration.getConfigurationType().getValue()) {
            case MakeConfiguration.TYPE_QT_DYNAMIC_LIB:
                file = Platforms.getPlatform(makeConfiguration.getDevelopmentHost().getBuildPlatform()).getQtLibraryName(file, getVersion().getValue());
                break;
            case MakeConfiguration.TYPE_QT_STATIC_LIB:
                file = "lib" + file + ".a"; // NOI18N
                break;
        }
        return 0 < dir.length() ? dir + "/" + file : file; // NOI18N
    }

    public IntConfiguration getBuildMode() {
        return buildMode;
    }

    private void setBuildMode(IntConfiguration buildMode) {
        this.buildMode = buildMode;
    }

    public String getEnabledModules() {
        StringBuilder buf = new StringBuilder();
        if (isCoreEnabled().getValue()) {
            append(buf, CORE);
        }
        if (isGuiEnabled().getValue()) {
            append(buf, GUI);
        }
        if (isWidgetsEnabled().getValue()) {
            append(buf, WIDGETS);
        }
        if (isNetworkEnabled().getValue()) {
            append(buf, NETWORK);
        }
        if (isOpenglEnabled().getValue()) {
            append(buf, OPENGL);
        }
        if (isPhononEnabled().getValue()) {
            append(buf, PHONON);
        }
        if (isQt3SupportEnabled().getValue()) {
            append(buf, QT3SUPPORT);
        }
        if (isPrintSupportEnabled().getValue()) {
            append(buf, PRINTSUPPORT);
        }
        if (isSqlEnabled().getValue()) {
            append(buf, SQL);
        }
        if (isSvgEnabled().getValue()) {
            append(buf, SVG);
        }
        if (isXmlEnabled().getValue()) {
            append(buf, XML);
        }
        if (isWebkitEnabled().getValue()) {
            append(buf, WEBKIT);
        }
        return buf.toString();
    }

    public void setEnabledModules(String modules) {
        isCoreEnabled().setValue(false);
        isGuiEnabled().setValue(false);
        isWidgetsEnabled().setValue(false);
        isNetworkEnabled().setValue(false);
        isOpenglEnabled().setValue(false);
        isPhononEnabled().setValue(false);
        isQt3SupportEnabled().setValue(false);
        isPrintSupportEnabled().setValue(false);
        isSqlEnabled().setValue(false);
        isSvgEnabled().setValue(false);
        isXmlEnabled().setValue(false);
        isWebkitEnabled().setValue(false);
        StringTokenizer st = new StringTokenizer(modules);
        while (st.hasMoreTokens()) {
            String t = st.nextToken();
            if (t.equals(CORE)) {
                isCoreEnabled().setValue(true);
            } else if (t.equals(GUI)) {
                isGuiEnabled().setValue(true);
            } else if (t.equals(WIDGETS)) {
                isWidgetsEnabled().setValue(true);
            } else if (t.equals(NETWORK)) {
                isNetworkEnabled().setValue(true);
            } else if (t.equals(OPENGL)) {
                isOpenglEnabled().setValue(true);
            } else if (t.equals(PHONON)) {
                isPhononEnabled().setValue(true);
            } else if (t.equals(QT3SUPPORT)) {
                isQt3SupportEnabled().setValue(true);
            } else if (t.equals(PRINTSUPPORT)) {
                isPrintSupportEnabled().setValue(true);
            } else if (t.equals(SQL)) {
                isSqlEnabled().setValue(true);
            } else if (t.equals(SVG)) {
                isSvgEnabled().setValue(true);
            } else if (t.equals(XML)) {
                isXmlEnabled().setValue(true);
            } else if (t.equals(WEBKIT)) {
                isWebkitEnabled().setValue(true);
            } else {
                // unknown module
            }
        }
    }

    public BooleanConfiguration isCoreEnabled() {
        return coreEnabled;
    }

    private void setCoreEnabled(BooleanConfiguration val) {
        coreEnabled = val;
    }

    public BooleanConfiguration isGuiEnabled() {
        return guiEnabled;
    }

    private void setGuiEnabled(BooleanConfiguration val) {
        guiEnabled = val;
    }

    public BooleanConfiguration isWidgetsEnabled() {
        return widgetsEnabled;
    }

    private void setWidgetsEnabled(BooleanConfiguration val) {
        widgetsEnabled = val;
    }

    public BooleanConfiguration isNetworkEnabled() {
        return networkEnabled;
    }

    private void setNetworkEnabled(BooleanConfiguration val) {
        networkEnabled = val;
    }

    public BooleanConfiguration isOpenglEnabled() {
        return openglEnabled;
    }

    private void setOpenglEnabled(BooleanConfiguration val) {
        openglEnabled = val;
    }

    public BooleanConfiguration isPhononEnabled() {
        return phononEnabled;
    }

    private void setPhononEnabled(BooleanConfiguration val) {
        this.phononEnabled = val;
    }

    public BooleanConfiguration isQt3SupportEnabled() {
        return qt3SupportEnabled;
    }

    private void setQt3SupportEnabled(BooleanConfiguration val) {
        this.qt3SupportEnabled = val;
    }
    
    public BooleanConfiguration isPrintSupportEnabled() {
        return printSupportEnabled;
    }

    private void setPrintSupportEnabled(BooleanConfiguration val) {
        this.printSupportEnabled = val;
    }

    public BooleanConfiguration isSqlEnabled() {
        return sqlEnabled;
    }

    private void setSqlEnabled(BooleanConfiguration val) {
        sqlEnabled = val;
    }

    public BooleanConfiguration isSvgEnabled() {
        return svgEnabled;
    }

    private void setSvgEnabled(BooleanConfiguration val) {
        svgEnabled = val;
    }

    public BooleanConfiguration isXmlEnabled() {
        return xmlEnabled;
    }

    private void setXmlEnabled(BooleanConfiguration val) {
        xmlEnabled = val;
    }

    public BooleanConfiguration isWebkitEnabled() {
        return webkitEnabled;
    }

    private void setWebkitEnabled(BooleanConfiguration val) {
        this.webkitEnabled = val;
    }

    public StringConfiguration getMocDir() {
        return mocDir;
    }

    private void setMocDir(StringConfiguration mocDir) {
        this.mocDir = mocDir;
    }

    public StringConfiguration getRccDir() {
        return rccDir;
    }

    private void setRccDir(StringConfiguration rccDir) {
        this.rccDir = rccDir;
    }

    public StringConfiguration getUiDir() {
        return uiDir;
    }

    private void setUiDir(StringConfiguration uicDir) {
        this.uiDir = uicDir;
    }

    public VectorConfiguration<String> getCustomDefs() {
        return customDefs;
    }

    private void setCustomDefs(VectorConfiguration<String> customDefs) {
        this.customDefs = customDefs;
    }

    public StringConfiguration getQmakeSpec() {
        return qmakespec;
    }

    private void setQmakespec(StringConfiguration qmakespec) {
        this.qmakespec = qmakespec;
    }

    public void assign(QmakeConfiguration other) {
        getDestdir().assign(other.getDestdir());
        getTarget().assign(other.getTarget());
        getVersion().assign(other.getVersion());
        getBuildMode().assign(other.getBuildMode());
        isCoreEnabled().assign(other.isCoreEnabled());
        isGuiEnabled().assign(other.isGuiEnabled());
        isWidgetsEnabled().assign(other.isWidgetsEnabled());
        isNetworkEnabled().assign(other.isNetworkEnabled());
        isOpenglEnabled().assign(other.isOpenglEnabled());
        isPhononEnabled().assign(other.isPhononEnabled());
        isQt3SupportEnabled().assign(other.isQt3SupportEnabled());
        isPrintSupportEnabled().assign(other.isPrintSupportEnabled());
        isSqlEnabled().assign(other.isSqlEnabled());
        isSvgEnabled().assign(other.isSvgEnabled());
        isXmlEnabled().assign(other.isXmlEnabled());
        isWebkitEnabled().assign(other.isWebkitEnabled());
        getMocDir().assign(other.getMocDir());
        getRccDir().assign(other.getRccDir());
        getUiDir().assign(other.getUiDir());
        getCustomDefs().assign(other.getCustomDefs());
        getQmakeSpec().assign(other.getQmakeSpec());
    }

    @Override
    public QmakeConfiguration clone() {
        try {
            QmakeConfiguration clone = (QmakeConfiguration) super.clone();
            clone.setDestdir(getDestdir().clone());
            clone.setTarget(getTarget().clone());
            clone.setVersion(getVersion().clone());
            clone.setBuildMode(getBuildMode().clone());
            clone.setCoreEnabled(isCoreEnabled().clone());
            clone.setGuiEnabled(isGuiEnabled().clone());
            clone.setWidgetsEnabled(isWidgetsEnabled().clone());
            clone.setNetworkEnabled(isNetworkEnabled().clone());
            clone.setOpenglEnabled(isOpenglEnabled().clone());
            clone.setPhononEnabled(isPhononEnabled().clone());
            clone.setPrintSupportEnabled(isPrintSupportEnabled().clone());
            clone.setQt3SupportEnabled(isQt3SupportEnabled().clone());
            clone.setSqlEnabled(isSqlEnabled().clone());
            clone.setSvgEnabled(isSvgEnabled().clone());
            clone.setXmlEnabled(isXmlEnabled().clone());
            clone.setWebkitEnabled(isWebkitEnabled().clone());
            clone.setMocDir(getMocDir().clone());
            clone.setRccDir(getRccDir().clone());
            clone.setUiDir(getUiDir().clone());
            clone.setCustomDefs(getCustomDefs().clone());
            clone.setQmakespec(getQmakeSpec().clone());
            return clone;
        } catch (CloneNotSupportedException ex) {
            // should not happen while this class implements Cloneable
            ex.printStackTrace(System.err);
            return null;
        }
    }

    private static void append(StringBuilder buf, String val) {
        if (0 < buf.length() && buf.charAt(buf.length() - 1) != ' ') { // NOI18N
            buf.append(' '); // NOI18N
        }
        buf.append(val);
    }
}
