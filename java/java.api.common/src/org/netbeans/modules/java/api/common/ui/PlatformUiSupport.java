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

package org.netbeans.modules.java.api.common.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.project.support.PreferredProjectPlatform;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlRenderer;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Support class for {@link JavaPlatform} manipulation in project customizer.
 * @author Tomas Zezula, Tomas Mysik, Petr Somol
 */
public final class PlatformUiSupport {

    private static final SpecificationVersion JDK_1_5 = new SpecificationVersion("1.5"); //NOI18N
    private static final Logger LOGGER = Logger.getLogger(PlatformUiSupport.class.getName());

    private PlatformUiSupport() {
    }

    /**
     * Create a {@link ComboBoxModel} of Java platforms.
     * The model listens on the {@link JavaPlatformManager} and update its
     * state according to the changes.
     * @param activePlatform the active project's platform, can be <code>null</code>.
     * @return {@link ComboBoxModel}.
     */
    public static ComboBoxModel createPlatformComboBoxModel(String activePlatform) {
        return createPlatformComboBoxModel(activePlatform, null);
    }


    /**
     * Create a {@link ComboBoxModel} of Java platforms.
     * The model listens on the {@link JavaPlatformManager} and update its
     * state according to the changes.
     * @param activePlatform the active project's platform, can be <code>null</code>.
     * @param filters project specific filter to filter-out platforms that are not usable in given context
     * @return {@link ComboBoxModel}.
     */
    public static ComboBoxModel createPlatformComboBoxModel(String activePlatform, Collection<? extends PlatformFilter> filters) {
        return new PlatformComboBoxModel(activePlatform, null, null, filters);
    }

    /**
     * Create a {@link ComboBoxModel} of Java platforms.
     * The model listens on the {@link JavaPlatformManager} and update its
     * state according to the changes.
     * @param owner the owning project
     * @param eval the project's {@link PropertyEvaluator}
     * @param activePlatform the active project's platform, can be <code>null</code>.
     * @param filters project specific filter to filter-out platforms that are not usable in given context
     * @return {@link ComboBoxModel}.
     * @since 1.80
     */
    @NonNull
    public static ComboBoxModel createPlatformComboBoxModel(
            @NonNull Project owner,
            @NonNull PropertyEvaluator eval,
            @NullAllowed final String activePlatform,
            @NullAllowed final Collection<? extends PlatformFilter> filters) {
        Parameters.notNull("owner", owner); //NOI18N
        Parameters.notNull("eval", eval);   //NOI18N
        return new PlatformComboBoxModel(activePlatform, owner, eval, filters);
    }

    /**
     * Create a {@link ListCellRenderer} for rendering items of the {@link ComboBoxModel}
     * created by the {@link PlatformUiSupport#createPlatformComboBoxModel(String)} method.
     * @return {@link ListCellRenderer}.
     */
    public static ListCellRenderer createPlatformListCellRenderer() {
        return new PlatformListCellRenderer();
    }

    /**
     * Like {@link #storePlatform(EditableProperties, UpdateHelper, String, Object, Object)}, but platform name may be
     * <code>null</code> (in such case the default platform is used).
     * @param props project's shared properties.
     * @param helper {@link UpdateHelper} that is capable to upgrade project metadata if needed.
     * @param projectConfigurationNamespace project configuration namespace.
     * @param platformName platform name to store, can be <code>null</code>.
     * @param sourceLevel specification version to store.
     */
    public static void storePlatform(EditableProperties props, UpdateHelper helper,
            String projectConfigurationNamespace, String platformName, SpecificationVersion sourceLevel) {
        Parameters.notNull("props", props); //NOI18N
        Parameters.notNull("helper", helper); //NOI18N
        Parameters.notNull("projectConfigurationNamespace", projectConfigurationNamespace); //NOI18N
        Parameters.notNull("sourceLevel", sourceLevel); //NOI18N

        PlatformKey platformKey = null;
        if (platformName != null) {
            final JavaPlatform platform = PlatformUiSupport.findPlatform(platformName);
            if (platform != null) {
                platformKey = new PlatformKey(platform);
            }
        }
        if (platformKey == null) {
            platformKey = new PlatformKey(JavaPlatformManager.getDefault().getDefaultPlatform());
        }
        storePlatform(props, helper, projectConfigurationNamespace, platformKey, new SourceLevelKey(sourceLevel));
    }
    
    
    /**
     * Stores active platform, <i>javac.source</i> and <i>javac.target</i> into the project's metadata.
     * @param props project's shared properties
     * @param helper {@link UpdateHelper} that is capable to upgrade project metadata if needed.
     * @param projectConfigurationNamespace project configuration namespace.
     * @param platformKey the {@code PlatformKey} got from the platform model.
     * @param sourceLevelKey {@code SourceLevelKey} representing source level; can be <code>null</code>.
     */
    public static void storePlatform(
            @NonNull final EditableProperties props,
            @NonNull final UpdateHelper helper,
            @NonNull final String projectConfigurationNamespace,
            @NonNull final Object platformKey,
            @NullAllowed final Object sourceLevelKey) {
        storePlatform(
                props,
                helper,
                projectConfigurationNamespace,
                platformKey,
                sourceLevelKey,
                null,
                true);
    }

    /**
     * Stores active platform, <i>javac.source</i> and <i>javac.target</i> into the project's metadata.
     * @param props project's shared properties
     * @param helper {@link UpdateHelper} that is capable to upgrade project metadata if needed.
     * @param projectConfigurationNamespace project configuration namespace.
     * @param platformKey the {@code PlatformKey} got from the platform model.
     * @param sourceLevelKey {@code SourceLevelKey} representing source level; can be <code>null</code>.
     * @param updatePreferredPlatform if true the {@link PreferredProjectPlatform} will be updated
     * @since 1.37
     */
    public static void storePlatform(
            @NonNull final EditableProperties props,
            @NonNull final UpdateHelper helper,
            @NonNull final String projectConfigurationNamespace,
            @NonNull final Object platformKey,
            @NullAllowed final Object sourceLevelKey,
            final boolean updatePreferredPlatform) {
        storePlatform(
            props,
            helper,
            projectConfigurationNamespace,
            platformKey,
            sourceLevelKey,
            null,
            updatePreferredPlatform);
    }

    /**
     * Stores active platform, <i>javac.source</i>,<i>javac.target</i> and <i>javac.profile</i> into the project's metadata.
     * @param props project's shared properties
     * @param helper {@link UpdateHelper} that is capable to upgrade project metadata if needed.
     * @param projectConfigurationNamespace project configuration namespace.
     * @param platformKey the {@code PlatformKey} got from the platform model.
     * @param sourceLevelKey {@code SourceLevelKey} representing source level; can be <code>null</code>.
     * @param profileKey {@link org.netbeans.api.java.queries.SourceLevelQuery.Profile} representing required profile; can be <code>null</code> for full JRE.
     * @param updatePreferredPlatform if true the {@link PreferredProjectPlatform} will be updated
     * @since 1.45
     */
    public static void storePlatform(
            @NonNull final EditableProperties props,
            @NonNull final UpdateHelper helper,
            @NonNull final String projectConfigurationNamespace,
            @NonNull final Object platformKey,
            @NullAllowed final Object sourceLevelKey,
            @NullAllowed final Object profileKey,
            final boolean updatePreferredPlatform) {
        Parameters.notNull("props", props); //NOI18N
        Parameters.notNull("helper", helper); //NOI18N
        Parameters.notNull("projectConfigurationNamespace", projectConfigurationNamespace); //NOI18N
        Parameters.notNull("platformKey", platformKey); //NOI18N
        if (!(platformKey instanceof PlatformKey)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported platform key: %s of type: %s", //NOI18N
                    platformKey,
                    platformKey.getClass()));
        }
        if (sourceLevelKey != null && !(sourceLevelKey instanceof SourceLevelKey)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported source level key: %s of type: %s", //NOI18N
                    sourceLevelKey,
                    sourceLevelKey.getClass()));
        }
        if (profileKey != null && !(profileKey instanceof Union2)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported profile key: %s of type: %s", //NOI18N
                    profileKey,
                    profileKey.getClass()));
        }

        final String javaPlatformKey = "platform.active"; //NOI18N
        final String javacSourceKey = "javac.source"; //NOI18N
        final String javacTargetKey = "javac.target"; //NOI18N
        final String javacProfileKey = "javac.profile";  //NOI18N

        PlatformKey pk = (PlatformKey) platformKey;
        JavaPlatform platform = getPlatform(pk);
        // null means active broken (unresolved) platform, no need to do anything
        if (platform == null) {
            return;
        }
        if (updatePreferredPlatform && !isProjectLocalPlatform(platform)) {
            PreferredProjectPlatform.setPreferredPlatform(platform);
        }
        SpecificationVersion jdk13 = new SpecificationVersion("1.3"); //NOI18N
        String platformAntName = platform.getProperties().get("platform.ant.name"); //NOI18N
        assert platformAntName != null;
        props.put(javaPlatformKey, platformAntName);
        Element root = helper.getPrimaryConfigurationData(true);
        boolean changed = false;
        NodeList explicitPlatformNodes =
                root.getElementsByTagNameNS(projectConfigurationNamespace, "explicit-platform"); //NOI18N

        if (pk.isDefaultPlatform()) {
            if (explicitPlatformNodes.getLength() == 1) {
                root.removeChild(explicitPlatformNodes.item(0));
                changed = true;
            }
        } else {
            Element explicitPlatform;
            switch (explicitPlatformNodes.getLength()) {
                case 0:
                    explicitPlatform = root.getOwnerDocument().createElementNS(
                            projectConfigurationNamespace, "explicit-platform"); //NOI18N
                    // insert node after <name> and optional <minimum-ant-version>
                    NodeList nodes = root.getChildNodes();
                    Node insertBeforeMe = null;
                    for (int i=0; i<nodes.getLength(); i++) {
                        Node n = nodes.item(i);
                        if (n.getNodeType() != Node.ELEMENT_NODE) {
                            continue; // ignore TEXT nodes etc.
                        }
                        if (!n.getNodeName().equals("name") &&  // NOI18N
                            !n.getNodeName().equals("minimum-ant-version")) { // NOI18N
                            insertBeforeMe = n;
                            break;
                        }
                    }
                    if (insertBeforeMe != null) {
                        root.insertBefore(explicitPlatform, insertBeforeMe);
                    } else {
                        root.appendChild(explicitPlatform);
                    }
                    changed = true;
                    break;
                case 1:
                    explicitPlatform = (Element) explicitPlatformNodes.item(0);
                    break;
                default:
                    throw new AssertionError("Broken project.xml file");
            }
            String explicitSourceAttrValue = explicitPlatform.getAttribute("explicit-source-supported"); //NOI18N
            if (jdk13.compareTo(platform.getSpecification().getVersion()) >= 0
                    && !"false".equals(explicitSourceAttrValue)) { //NOI18N
                explicitPlatform.setAttribute("explicit-source-supported", "false"); //NOI18N
                changed = true;
            } else if (jdk13.compareTo(platform.getSpecification().getVersion()) < 0
                    && !"true".equals(explicitSourceAttrValue)) { //NOI18N
                explicitPlatform.setAttribute("explicit-source-supported", "true"); //NOI18N
                changed = true;
            }
        }

        SpecificationVersion sourceLevel;
        if (sourceLevelKey == null) {
            sourceLevel = platform.getSpecification().getVersion();
        } else {
            sourceLevel = ((SourceLevelKey) sourceLevelKey).getSourceLevel();
        }
        String javacSource = sourceLevel.toString();
        String javacTarget = javacSource;

        //Issue #116490
        // Customizer value | -source | -target
        // JDK 1.2            1.2        1.1
        // JDK 1.3            1.3        1.1
        // JDK 1.4            1.4        1.4
        // JDK 5              1.5        1.5
        // JDK 6              1.6        1.6  - java 1.6 brings JLS changes - @Override, encoding
        // JDK 7              1.7        1.7  - coin
        // JDK 8              1.8        1.8  - lambdas
        // JDK 9              1.9        1.9  - jigsaw
        if (jdk13.compareTo(sourceLevel) >= 0) {
            javacTarget = "1.1"; //NOI18N
        }

        if (!javacSource.equals(props.getProperty(javacSourceKey))) {
            props.setProperty(javacSourceKey, javacSource);
        }
        if (!javacTarget.equals(props.getProperty(javacTargetKey))) {
            props.setProperty(javacTargetKey, javacTarget);
        }

        String javacProfile = null;
        if (profileKey != null) {
            Union2<SourceLevelQuery.Profile,String> tv = (Union2<SourceLevelQuery.Profile,String>) profileKey;
            if (tv.hasFirst()) {
                final SourceLevelQuery.Profile profile = tv.first();
                if (profile != SourceLevelQuery.Profile.DEFAULT) {
                    javacProfile = profile.getName();
                }
            } else {
                javacProfile = tv.second();
            }
        }
        if (javacProfile != null) {
            if(!javacProfile.equals(props.getProperty(javacProfileKey))) {
                props.setProperty(javacProfileKey, javacProfile);
            }
        } else {
            props.remove(javacProfileKey);
        }

        if (changed) {
            helper.putPrimaryConfigurationData(root, true);
        }
    }

    /**
     * Returns a {@link SpecificationVersion} for an item obtained from the {@link ComboBoxModel} created by
     * the {@link PlatformUiSupport#createSourceLevelComboBoxModel} method. This method
     * can return <code>null</code> if the source level is broken.
     * @param sourceLevelKey  an item obtained from {@link ComboBoxModel} created by
     *                    {@link PlatformUiSupport#createSourceLevelComboBoxModel}.
     * @return {@link SpecificationVersion} or <code>null</code> in case when source level is broken.
     * @throws IllegalArgumentException if the input parameter is not an object created by source level combobox model.
     * @since 1.45
     */
    public static SpecificationVersion getSourceLevel(@NonNull final Object sourceLevelKey) {
        Parameters.notNull("sourceLevelKey", sourceLevelKey);   //NOI18N
        if (!(sourceLevelKey instanceof SourceLevelKey)) {
            throw new IllegalArgumentException(String.format(
                    "Unsupported source level key: %s of type: %s", //NOI18N
                    sourceLevelKey,
                    sourceLevelKey.getClass()));
        }
        return ((SourceLevelKey)sourceLevelKey).getSourceLevel();
    }


    /**
     * Return a {@link JavaPlatform} for an item obtained from the ComboBoxModel created by
     * the {@link PlatformUiSupport#createPlatformComboBoxModel(String)} method. This method
     * can return <code>null</code> if the platform is broken.
     * @param platformKey an item obtained from {@link ComboBoxModel} created by
     *                    {@link PlatformUiSupport#createPlatformComboBoxModel(String)}.
     * @return {@link JavaPlatform} or <code>null</code> in case when platform is broken.
     * @throws IllegalArgumentException if the input parameter is not an object created by platform combobox model.
     */
    public static JavaPlatform getPlatform(Object platformKey) {
        Parameters.notNull("platformKey", platformKey); //NOI18N

        if (platformKey instanceof PlatformKey) {
            return getPlatform((PlatformKey) platformKey);
        }
        throw new IllegalArgumentException();
    }

    /**
     * Return a {@link org.netbeans.api.java.queries.SourceLevelQuery.Profile} for an item obtained from the ComboBoxModel created by
     * the {@link PlatformUiSupport#createProfileComboBoxModel} method.
     * This method can return <code>null</code> if the profile is broken.
     * @param profileKey an item obtained from {@link ComboBoxModel} created by
     *                    {@link PlatformUiSupport#createProfileComboBoxModel}.
     * @return {@link org.netbeans.api.java.queries.SourceLevelQuery.Profile} or <code>null</code> in case when profile is broken.
     * @throws IllegalArgumentException if the input parameter is not an object created by profile combobox model.
     * @since 1.57
     */
    @CheckForNull
    public static SourceLevelQuery.Profile getProfile(@NonNull final Object profileKey) {
        Parameters.notNull("profileKey", profileKey);   //NOI18N
        if (profileKey instanceof Union2) {
            final Union2 u2 = (Union2)profileKey;
            if (u2.hasFirst()) {
                final Object profile = u2.first();
                if (profile instanceof SourceLevelQuery.Profile) {
                    return (SourceLevelQuery.Profile) profile;
                } else {
                    throw new IllegalArgumentException(profile.getClass().getName());
                }
            } else {
                return null;
            }
        } else {
            throw  new IllegalArgumentException(profileKey.getClass().getName());
        }
    }

    /**
     * Create {@link ComboBoxModel} of source levels for active platform.
     * The model listens on the platform's {@link ComboBoxModel} and update its
     * state according to the changes. It is possible to define minimal JDK version.
     * @param platformComboBoxModel the platform's model used for listenning.
     * @param initialSourceLevel initial source level value, null if unknown.
     * @param initialTargetLevel initial target level value, null if unknown.
     * @param minimalSpecificationVersion minimal JDK version to be displayed. It can be <code>null</code> if all the JDK versions
     *                          should be displayed (typically for Java SE project).
     * @return {@link ComboBoxModel} of {@code SourceLevelKey}.
     * @see #createSourceLevelComboBoxModel(ComboBoxModel, String, String)
     */
    public static ComboBoxModel createSourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel,
            String initialSourceLevel, String initialTargetLevel, SpecificationVersion minimalSpecificationVersion) {
        Parameters.notNull("platformComboBoxModel", platformComboBoxModel); // NOI18N
        return new SourceLevelComboBoxModel(platformComboBoxModel, initialSourceLevel, initialTargetLevel,
                minimalSpecificationVersion);
    }

    /**
     * Exactly like {@link #createSourceLevelComboBoxModel(ComboBoxModel, String, String, SpecificationVersion)}
     * but without any minimal JDK version.
     * @param platformComboBoxModel the platform's model used for listenning.
     * @param initialSourceLevel initial source level value, null if unknown.
     * @param initialTargetLevel initial target level value, null if unknown.
     * @return {@link ComboBoxModel} of {@code SourceLevelKey}.
     * @see #createSourceLevelComboBoxModel(ComboBoxModel, String, String, SpecificationVersion)
     */
    public static ComboBoxModel createSourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel,
            String initialSourceLevel, String initialTargetLevel) {
        Parameters.notNull("platformComboBoxModel", platformComboBoxModel); // NOI18N

        return new SourceLevelComboBoxModel(platformComboBoxModel, initialSourceLevel, initialTargetLevel, null);
    }

    /**
     * Create {@link ListCellRenderer} for source levels. This method could be used when highlighting
     * of illegal source levels is needed.
     * @return {@link ListCellRenderer} for source levels.
     */
    public static ListCellRenderer createSourceLevelListCellRenderer() {
        return new SourceLevelListCellRenderer();
    }

    /**
     * Create {@link ComboBoxModel} of JRE profiles for active source level.
     * The model listens on the source level {@link ComboBoxModel} and update its
     * state according to the changes. It is possible to define minimal required
     * JRE profile.
     * @param sourceLevelModel the source level model used for listening.
     * @param initialProfileName initial profile name, null if unknown.
     * @param minimalProfile minimal JRE profile to be displayed.
     * It can be <code>null</code> if all the JRE profiles should be displayed.
     * @return {@link ComboBoxModel}.
     * @since 1.45
     */
    public static ComboBoxModel createProfileComboBoxModel(
            @NonNull final ComboBoxModel sourceLevelModel,
            @NullAllowed final String initialProfileName,
            @NullAllowed final SourceLevelQuery.Profile minimalProfile) {
        return new ProfileComboBoxModel(sourceLevelModel, initialProfileName, minimalProfile);
    }

    /**
     * Create {@link ListCellRenderer} for JRE profiles.
     * This renderer highlights incorrect profile names.
     * @return {@link ListCellRenderer} for JRE profiles.
     * @since 1.45
     */
    public static ListCellRenderer createProfileListCellRenderer() {
        return new ProfileListCellRenderer();
    }

    /**
     * This class represents a JavaPlatform in the {@link ListModel}
     * created by the {@link PlatformUiSupport#createPlatformComboBoxModel(String)} method.
     */
    private static final class PlatformKey implements Comparable {

        private String name;
        private boolean equalsDefaultPlatformName;
        private JavaPlatform platform;

        /**
         * Create a PlatformKey for a broken platform.
         * @param name the ant name of the broken platform.
         */
        public PlatformKey(String name) {
            assert name != null;
            this.name = name;
        }

        /**
         * Create a PlatformKey for a broken platform.
         * @param name the ant name of the broken platform.
         * @param equalsDefaultPlatformName true if name is the same as name of default platform.
         */
        public PlatformKey(String name, boolean equalsDefaultPlatformName) {
            assert name != null;
            this.name = name;
            this.equalsDefaultPlatformName = equalsDefaultPlatformName;
        }
        
        /**
         * Create a PlatformKey for a platform.
         * @param platform the {@link JavaPlatform}.
         */
        public PlatformKey(JavaPlatform platform) {
            this(platform, null);
        }
        
        /**
         * Create a PlatformKey for a platform with given name.
         * @param platform the {@link JavaPlatform}.
         * @param name the name
         */
        public PlatformKey(
                @NonNull final JavaPlatform platform,
                @NullAllowed final String name) {
            assert platform != null;
            this.platform = platform;
            this.name = name;
        }

        public int compareTo(Object o) {
            return this.getDisplayName().compareTo(((PlatformKey) o).getDisplayName());
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof PlatformKey) {
                PlatformKey otherKey = (PlatformKey) other;
                boolean equals;
                if (this.platform == null) {
                    equals = otherKey.platform == null;
                } else {
                    equals = this.platform.equals(otherKey.platform);
                }
                return equals && otherKey.getDisplayName().equals(this.getDisplayName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getDisplayName().hashCode();
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        public synchronized String getDisplayName() {
            if (this.name == null) {
                this.name = this.platform.getDisplayName();
            }
            return this.name;
        }

        public boolean isDefaultPlatform() {
            if (this.platform == null) {
                return false;
            }
            return this.platform.equals(JavaPlatformManager.getDefault().getDefaultPlatform());
        }

        public boolean hasDefaultPlatformName() {
            return this.equalsDefaultPlatformName;
        }

        public boolean isBroken() {
            return this.platform == null;
        }
    }

    private static final class SourceLevelKey implements Comparable {

        private final SpecificationVersion sourceLevel;
        private final boolean broken;

        public SourceLevelKey(final SpecificationVersion sourceLevel) {
            this(sourceLevel, false);
        }

        public SourceLevelKey(final SpecificationVersion sourceLevel, final boolean broken) {
            assert sourceLevel != null : "Source level cannot be null";
            this.sourceLevel = sourceLevel;
            this.broken = broken;
        }

        public SpecificationVersion getSourceLevel() {
            return this.sourceLevel;
        }

        public boolean isBroken() {
            return this.broken;
        }

        public int compareTo(final Object other) {
            assert other instanceof SourceLevelKey : "Illegal argument of SourceLevelKey.compareTo()";
            SourceLevelKey otherKey = (SourceLevelKey) other;
            return this.sourceLevel.compareTo(otherKey.sourceLevel);
        }

        @Override
        public boolean equals(final Object other) {
            return (other instanceof SourceLevelKey)
                    && ((SourceLevelKey) other).sourceLevel.equals(this.sourceLevel);
        }

        @Override
        public int hashCode() {
            return this.sourceLevel.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder buffer = new StringBuilder();
            if (this.broken) {
                buffer.append("Broken: "); //NOI18N
            }
            buffer.append(this.sourceLevel.toString());
            return buffer.toString();
        }

        public String getDisplayName() {
            String tmp = sourceLevel.toString();
            if (JDK_1_5.compareTo(sourceLevel) <= 0) {
                tmp = tmp.replaceFirst("^1\\.([5-9]|\\d\\d+)$", "$1"); //NOI18N
            }
            return NbBundle.getMessage(PlatformUiSupport.class, "LBL_JDK", tmp);
        }
    }

    private static final class PlatformComboBoxModel extends AbstractListModel
            implements ComboBoxModel, PropertyChangeListener {
        private static final long serialVersionUID = 1L;

        private final JavaPlatformManager pm;
        private final Project project;
        private final PropertyEvaluator eval;
        private PlatformKey[] platformNamesCache;
        private String initialPlatform;
        private PlatformKey selectedPlatform;
        private boolean inUpdate;
        private Collection<? extends PlatformFilter> filters;

        public PlatformComboBoxModel(
                @NullAllowed String initialPlatform,
                @NullAllowed Project owner,
                @NullAllowed PropertyEvaluator eval,
                @NullAllowed Collection<? extends PlatformFilter> filters) {
            assert owner == null ? eval == null : eval != null;
            this.pm = JavaPlatformManager.getDefault();
            this.initialPlatform = initialPlatform;
            this.filters = filters;
            this.project = owner;
            this.eval = eval;
            this.pm.addPropertyChangeListener(WeakListeners.propertyChange(this, this.pm));
        }

        @Override
        public int getSize() {
            PlatformKey[] platformNames = getPlatformNames();
            return platformNames.length;
        }

        @Override
        public Object getElementAt(int index) {
            PlatformKey[] platformNames = getPlatformNames();
            assert index >= 0 && index < platformNames.length;
            return platformNames[index];
        }

        @Override
        public Object getSelectedItem() {
            getPlatformNames(); // force setting of selectedPlatform if it is not already done
            return selectedPlatform;
        }

        @Override
        public void setSelectedItem(Object obj) {
            //Guard from null on Mac OS X
            if (obj != null) {
                selectedPlatform = (PlatformKey) obj;
                fireContentsChanged(this, -1, -1);
            }
        }

        @Override
        public void propertyChange(PropertyChangeEvent event) {
            if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
                synchronized (this) {
                    platformNamesCache = null;
                }
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        inUpdate = true;
                        try {
                            fireContentsChanged(PlatformComboBoxModel.this, -1, -1);
                        } finally {
                            inUpdate = false;
                        }
                    }
                });
            }
        }

        private synchronized PlatformKey[] getPlatformNames() {
            if (platformNamesCache == null) {
                final List<JavaPlatform> platforms = new ArrayList<>();
                Collections.addAll(platforms, pm.getPlatforms(null, new Specification(CommonProjectUtils.J2SE_PLATFORM_TYPE, null)));
                JavaPlatform projectPlatform = null;
                final EditableProperties globalProps = PropertyUtils.getGlobalProperties();
                if (project != null) {
                    final String active = eval.getProperty(ProjectProperties.PLATFORM_ACTIVE);
                    if (active != null) {
                        final String activeHomeKey = String.format("platforms.%s.home", active);    //NOI18N
                        if (eval.getProperty(activeHomeKey) != null && !globalProps.containsKey(activeHomeKey)) {
                            projectPlatform = project != null ?
                                ProjectPlatform.forProject(
                                    project,
                                    eval,
                                    CommonProjectUtils.J2SE_PLATFORM_TYPE) :
                                null;
                            if (projectPlatform != null) {
                                platforms.add(projectPlatform);
                            }
                        }
                    }
                }
                Set<PlatformKey> orderedNames = new TreeSet<>();
                boolean activeFound = false;
                for (JavaPlatform platform : platforms) {
                    boolean accepted = true;
                    if(filters != null) {
                        for(PlatformFilter filter : filters) {
                            if(!filter.accept(platform)) {
                                accepted = false;
                                break;
                            }
                        }
                    }
                    if (accepted && platform.isValid()) {
                        final PlatformKey pk = platform == projectPlatform ?
                                new PlatformKey(platform, NbBundle.getMessage(PlatformUiSupport.class, "TXT_ProjectPlatformName", platform.getDisplayName())) :
                                new PlatformKey(platform);
                        orderedNames.add(pk);
                        if (!activeFound && initialPlatform != null) {
                            String antName = platform.getProperties().get("platform.ant.name"); //NOI18N
                            if (initialPlatform.equals(antName)) {
                                if (selectedPlatform == null) {
                                    selectedPlatform = pk;
                                    initialPlatform = null;
                                }
                                activeFound = true;
                            }
                        }
                    }
                }                
                if (!activeFound) {
                    if(orderedNames.isEmpty()) {
                        LOGGER.warning("PlatformComboBoxModel: All platforms filtered out. Adding default platform although it is not accepted by all PlatformFilters."); // NOI18N
                        selectedPlatform = new PlatformKey(JavaPlatformManager.getDefault().getDefaultPlatform());
                        orderedNames.add(selectedPlatform);
                    } else {
                        if (initialPlatform == null) {
                            if (selectedPlatform == null || !orderedNames.contains(selectedPlatform)) {
                                selectedPlatform = new PlatformKey(JavaPlatformManager.getDefault().getDefaultPlatform());
                            }
                        } else {
                            String defaultPlatformName = JavaPlatformManager.getDefault().getDefaultPlatform().getDisplayName();
                            if(defaultPlatformName != null && 
                                    defaultPlatformName.endsWith(" " //NOI18N
                                    + NbBundle.getMessage(PlatformUiSupport.class, "TXT_BrokenPlatformDefault"))) { //NOI18N
                                defaultPlatformName = defaultPlatformName.substring(0, 
                                        defaultPlatformName.length() - NbBundle.getMessage(PlatformUiSupport.class, "TXT_BrokenPlatformDefault").length() - 1) //NOI18N
                                        .replace(" ", "_"); //NOI18N
                            }
                            PlatformKey pk = new PlatformKey(initialPlatform, defaultPlatformName != null && initialPlatform.equals(defaultPlatformName));
                            orderedNames.add(pk);
                            if (selectedPlatform == null) {
                                selectedPlatform = pk;
                            }
                        }
                    }
                }
                platformNamesCache = orderedNames.toArray(new PlatformKey[0]);
                assert selectedPlatform != null : "platformNamesCache: " + orderedNames +" initialPlatform: " + initialPlatform;   //NOI18N
            }            
            return platformNamesCache;
        }

    }

    private static final class PlatformListCellRenderer implements ListCellRenderer {

        private final ListCellRenderer delegate;

        public PlatformListCellRenderer() {
            delegate = HtmlRenderer.createRenderer();
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            String name;
            if (value == null || " ".equals(value)) { // NOI18N
                name = " "; //NOI18N
            } else {
                assert value instanceof PlatformKey : "Wrong model";
                PlatformKey key = (PlatformKey) value;
                if (key.isBroken()) {
                    name = "<html><font color=\"#A40000\">" //NOI18N
                            + NbBundle.getMessage(
                                    PlatformUiSupport.class, "TXT_BrokenPlatformFmt", key.getDisplayName())
                            + (key.hasDefaultPlatformName() ?
                                    " " + NbBundle.getMessage(PlatformUiSupport.class, "TXT_BrokenPlatformCustom") //NOI18N
                                    : ""); //NOI18N
                } else {
                    name = key.getDisplayName();
                }
            }
            return delegate.getListCellRendererComponent(list, name, index, isSelected, cellHasFocus);
        }
    }

    private static final class SourceLevelComboBoxModel extends AbstractListModel
            implements ComboBoxModel, ListDataListener {
        private static final long serialVersionUID = 1L;

        private final ComboBoxModel platformComboBoxModel;
        private final SpecificationVersion minimalSpecificationVersion;
        private SpecificationVersion selectedSourceLevel;
        private SpecificationVersion originalSourceLevel;
        private SourceLevelKey[] sourceLevelCache;
        private PlatformKey activePlatform;

        public SourceLevelComboBoxModel(ComboBoxModel platformComboBoxModel, String initialSourceLevel,
                String initialTargetLevel, SpecificationVersion minimalSpecificationVersion) {
            this.platformComboBoxModel = platformComboBoxModel;
            activePlatform = (PlatformKey) this.platformComboBoxModel.getSelectedItem();
            this.platformComboBoxModel.addListDataListener(this);
            if (initialSourceLevel != null && initialSourceLevel.length() > 0) {
                try {
                    originalSourceLevel = new SpecificationVersion(initialSourceLevel);
                } catch (NumberFormatException nfe) {
                    // if the javac.source has invalid value, do not preselect and log it.
                    LOGGER.warning("Invalid javac.source: " + initialSourceLevel);
                }
            }
            if (initialTargetLevel != null && initialTargetLevel.length() > 0) {
                try {
                    SpecificationVersion originalTargetLevel = new SpecificationVersion(initialTargetLevel);
                    if (originalSourceLevel == null || originalSourceLevel.compareTo(originalTargetLevel)<0) {
                        originalSourceLevel = originalTargetLevel;
                    }
                } catch (NumberFormatException nfe) {
                    // if the javac.target has invalid value, do not preselect and log it
                    LOGGER.warning("Invalid javac.target: "+initialTargetLevel);
                }
            }
            selectedSourceLevel = originalSourceLevel;
            this.minimalSpecificationVersion = minimalSpecificationVersion;
        }

        public int getSize() {
            SourceLevelKey[] sLevels = getSourceLevels();
            return sLevels.length;
        }

        public Object getElementAt(int index) {
            SourceLevelKey[] sLevels = getSourceLevels();
            assert index >= 0 && index < sLevels.length;
            return sLevels[index];
        }

        public Object getSelectedItem() {
            for (SourceLevelKey key : getSourceLevels()) {
                if (key.getSourceLevel().equals(selectedSourceLevel)) {
                    return key;
                }
            }
            return null;
        }

        public void setSelectedItem(Object obj) {
            selectedSourceLevel = (obj == null ? null : ((SourceLevelKey) obj).getSourceLevel());
            fireContentsChanged(this, -1, -1);
        }

        public void intervalAdded(ListDataEvent e) {
        }

        public void intervalRemoved(ListDataEvent e) {
        }

        public void contentsChanged(ListDataEvent e) {
            PlatformKey selectedPlatform = (PlatformKey) platformComboBoxModel.getSelectedItem();
            JavaPlatform platform = getPlatform(selectedPlatform);
            if (platform != null &&
                !((platformComboBoxModel instanceof PlatformComboBoxModel) && ((PlatformComboBoxModel)platformComboBoxModel).inUpdate)) {
                SpecificationVersion version = platform.getSpecification().getVersion();
                if (selectedSourceLevel != null
                        && selectedSourceLevel.compareTo(version) > 0
                        && !shouldChangePlatform(selectedSourceLevel, version)
                        && !selectedPlatform.equals(activePlatform)) {
                    // restore original
                    platformComboBoxModel.setSelectedItem(activePlatform);
                    return;
                } else {
                    originalSourceLevel = null;
                }
            }
            activePlatform = selectedPlatform;
            resetCache();
        }

        private void resetCache() {
            synchronized (this) {
                sourceLevelCache = null;
            }
            fireContentsChanged(this, -1, -1);
        }

        private SourceLevelKey[] getSourceLevels() {
            if (sourceLevelCache == null) {
                PlatformKey selectedPlatform = (PlatformKey) platformComboBoxModel.getSelectedItem();
                JavaPlatform platform = getPlatform(selectedPlatform);
                List<SourceLevelKey> sLevels = new ArrayList<SourceLevelKey>();
                // if platform == null => broken platform, the source level range is unknown
                // the source level combo box should be empty and disabled
                boolean selSourceLevelValid = false;
                if (platform != null) {
                    SpecificationVersion version = platform.getSpecification().getVersion();
                    SpecificationVersion current = getMinimalIndex(version);
                    boolean origSourceLevelValid = false;

                    while (current.compareTo(version) <= 0) {
                        if (current.equals(originalSourceLevel)) {
                            origSourceLevelValid = true;
                        }
                        if (current.equals(selectedSourceLevel)) {
                            selSourceLevelValid = true;
                        }
                        sLevels.add(new SourceLevelKey(current));
                        current = incJavaSpecVersion(current);
                    }
                    if (originalSourceLevel != null && !origSourceLevelValid) {
                        if (originalSourceLevel.equals(selectedSourceLevel)) {
                            selSourceLevelValid = true;
                        }
                        sLevels.add(new SourceLevelKey(originalSourceLevel, true));
                    }
                }
                sourceLevelCache = sLevels.toArray(new SourceLevelKey[0]);
                if (!selSourceLevelValid) {
                    selectedSourceLevel = sourceLevelCache.length == 0
                            ? null : sourceLevelCache[sourceLevelCache.length - 1].getSourceLevel();
                }
            }
            return sourceLevelCache;
        }

        private SpecificationVersion getMinimalIndex(SpecificationVersion platformVersion) {
            SpecificationVersion min = SourceLevelQuery.MINIMAL_SOURCE_LEVEL;
            if (minimalSpecificationVersion != null) {
                while (min.compareTo(platformVersion) <= 0) {
                    if (min.compareTo(minimalSpecificationVersion) >= 0) {
                        return min;
                    }
                    min = incJavaSpecVersion(min);
                }
            }
            return min;
        }

        private boolean shouldChangePlatform(SpecificationVersion selectedSourceLevel,
                SpecificationVersion platformSourceLevel) {
            JButton changeOption = new JButton(NbBundle.getMessage(PlatformUiSupport.class, "CTL_ChangePlatform"));
            changeOption.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(PlatformUiSupport.class, "AD_ChangePlatform"));
            String message = MessageFormat.format(
                    NbBundle.getMessage(PlatformUiSupport.class, "TXT_ChangePlatform"),
                    new Object[] {selectedSourceLevel.toString(), platformSourceLevel.toString()});
            return DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                    message,
                    NbBundle.getMessage(PlatformUiSupport.class, "TXT_ChangePlatformTitle"),
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    new Object[] {
                        changeOption,
                        NotifyDescriptor.CANCEL_OPTION
                    },
                    changeOption)) == changeOption;
        }

        private static int minor(@NonNull final SpecificationVersion specVer) {
            final String s = specVer.toString();
            final int split = s.indexOf('.');   //NOI18N
            return split < 0 ? -1 : Integer.parseInt(s.substring(split+1));
        }

        private static int major(@NonNull final SpecificationVersion specVer) {
            final String s = specVer.toString();
            final int split = s.indexOf('.');   //NOI18N
            return Integer.parseInt(split < 0 ? s : s.substring(0, split));
        }

        private static SpecificationVersion incJavaSpecVersion(@NonNull final SpecificationVersion version) {
            int major = major (version);
            int minor = minor (version);
            if (major == 1) {
                if (minor == 8) {
                    major = minor + 1;
                    minor = -1;
                } else {
                    minor += 1;
                }
            } else {
                major += 1;
            }
            return minor == -1 ?
                    new SpecificationVersion(Integer.toString(major)) :
                    new SpecificationVersion(String.format(
                        "%d.%d",    //NOI18N
                        major,
                        minor));
        }
    }

    private static final class SourceLevelListCellRenderer implements ListCellRenderer {

        private ListCellRenderer delegate;

        public SourceLevelListCellRenderer() {
            delegate = HtmlRenderer.createRenderer();
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            String message;
            if (value == null || " ".equals(value)) { // NOI18N
                message = " ";   //NOI18N
            } else {
                assert value instanceof SourceLevelKey;
                SourceLevelKey key = (SourceLevelKey) value;
                if (key.isBroken()) {
                    message = "<html><font color=\"#A40000\">" //NOI18N
                            + NbBundle.getMessage(
                                    PlatformUiSupport.class, "TXT_InvalidSourceLevel", key.getDisplayName());
                } else {
                    message = key.getDisplayName();
                }
            }
            return delegate.getListCellRendererComponent(list, message, index, isSelected, cellHasFocus);
        }
    }
           
    private static final class ProfileComboBoxModel extends AbstractListModel implements ComboBoxModel, ListDataListener {

        private final ComboBoxModel sourceLevelModel;
        private final String initialProfileName;
        private final SourceLevelQuery.Profile minimalProfile;

        private List<Union2<SourceLevelQuery.Profile,String>> profiles;
        private Union2<SourceLevelQuery.Profile,String> selectedItem;

        ProfileComboBoxModel(
                @NonNull final ComboBoxModel sourceLevelModel,
                @NullAllowed final String initialProfileName,
                @NullAllowed final SourceLevelQuery.Profile minimalProfile) {
            this.sourceLevelModel = sourceLevelModel;
            this.initialProfileName = initialProfileName;
            this.minimalProfile = minimalProfile;
            this.sourceLevelModel.addListDataListener(this);
        }

        @Override
        public int getSize() {
            final Collection<? extends Union2<SourceLevelQuery.Profile,String>> p = init();
            return p.size();
        }

        @Override
        @CheckForNull
        public Object getElementAt(final int index) {
            final List<? extends Union2<SourceLevelQuery.Profile,String>> p = init();
            if (index < 0 || index >= p.size()) {
                throw new IndexOutOfBoundsException(String.format(
                    "Index: %d, Profiles count: %d",    //NOI18N
                    index,
                    p.size()));
            }
            return p.get(index);
        }

        @Override
        public void setSelectedItem(@NullAllowed final Object anItem) {
            assert anItem == null || anItem instanceof Union2 : anItem;
            init();
            selectedItem = (Union2<SourceLevelQuery.Profile,String>) anItem;
            fireContentsChanged(this, -1, -1);
        }

        @Override
        @CheckForNull
        public Object getSelectedItem() {
            init();
            return selectedItem;
        }

        @Override
        public void intervalAdded(ListDataEvent e) {
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            final int oldSize = getSize();
            profiles = null;
            fireContentsChanged(this, 0, oldSize);
        }

        private List<? extends Union2<SourceLevelQuery.Profile,String>> init() {
            if (profiles == null) {                           
                final List<Union2<SourceLevelQuery.Profile,String>> pc = new ArrayList<>();
                final Object slk = sourceLevelModel.getSelectedItem();
                final SpecificationVersion sl;
                if (slk instanceof SourceLevelKey) {
                    sl = ((SourceLevelKey)slk).getSourceLevel();
                } else {
                    sl = null;
                }
                for (SourceLevelQuery.Profile p : SourceLevelQuery.Profile.values()) {
                    if (minimalProfile != null && minimalProfile.compareTo(p) > 0) {
                            continue;
                    }
                    if (sl != null && !p.isSupportedIn(sl.toString())) {
                        continue;
                    }
                    pc.add(Union2.<SourceLevelQuery.Profile,String>createFirst(p));
                }
                if (selectedItem == null) {
                    if (initialProfileName != null) {
                        SourceLevelQuery.Profile initialProfile = SourceLevelQuery.Profile.forName(initialProfileName);
                        if (initialProfile != null) {
                            selectedItem = Union2.<SourceLevelQuery.Profile,String>createFirst(initialProfile);
                        } else {
                            selectedItem = Union2.<SourceLevelQuery.Profile,String>createSecond(initialProfileName);
                            pc.add(selectedItem);
                        }
                    } else {
                        selectedItem = Union2.<SourceLevelQuery.Profile,String>createFirst(SourceLevelQuery.Profile.DEFAULT);
                    }
                }
                this.profiles = Collections.unmodifiableList(pc);
            }
            return profiles;
        }        
    }

    private static final class ProfileListCellRenderer implements ListCellRenderer {

        private final ListCellRenderer delegate;

        ProfileListCellRenderer() {
            delegate = HtmlRenderer.createRenderer();
        }

        @Override
        public Component getListCellRendererComponent(
                @NonNull final JList list,
                @NullAllowed Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            if (value instanceof Union2) {
                final Union2<SourceLevelQuery.Profile,String> tv  =
                        (Union2<SourceLevelQuery.Profile,String>) value;
                if (tv.hasFirst()) {
                    value = tv.first().getDisplayName();
                } else {
                    value = "<html><font color=\"#A40000\">" + //NOI18N
                        tv.second();
                }
            }
            return delegate.getListCellRendererComponent(
                    list, value, index, isSelected, cellHasFocus);
        }

    }

    /**
     * Retturns a {@link JavaPlatform} for given {@code PlatformKey}
     * or null when the platformKey is either null or not bound to a platform.
     * @param platformKey
     * @return java platform
     */
    private static JavaPlatform getPlatform(PlatformKey platformKey) {
        return platformKey == null ? null : platformKey.platform;
    }

    private static JavaPlatform findPlatform(String displayName) {
        JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(
                displayName, new Specification(CommonProjectUtils.J2SE_PLATFORM_TYPE, null));
        if (platforms.length == 0) {
            return null;
        }
        return platforms[0];
    }
    
    private static boolean isProjectLocalPlatform(@NonNull final JavaPlatform jp) {
        for (JavaPlatform p : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            if (p.equals(jp)) {
                return false;
            }
        }
        return true;
    }
}
