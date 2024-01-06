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

package org.netbeans.modules.javaee.project.api.ant.ui.wizard;

import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.spi.java.project.support.PreferredProjectPlatform;
import org.openide.modules.SpecificationVersion;
import org.openide.util.NbBundle;

/**
 * Displays a warning that the project's Java platform will be set to JDK 1.4 or
 * the source level will be set to 1.4. See issue #55797.
 *
 * @author Andrei Badea
 */
final class J2eeVersionWarningPanel extends javax.swing.JPanel {

    public static final String WARN_SET_JDK_15 = "warnSetJdk15"; // NOI18N
    public static final String WARN_SET_JDK_6 = "warnSetJdk6"; // NOI18N
    public static final String WARN_SET_JDK_7 = "warnSetJdk7"; // NOI18N
    public static final String WARN_SET_JDK_8 = "warnSetJdk8"; // NOI18N
    public static final String WARN_SET_JDK_11 = "warnSetJdk11"; // NOI18N
    public static final String WARN_SET_JDK_21 = "warnSetJdk21"; // NOI18N

    public static final String WARN_SET_SOURCE_LEVEL_15 = "warnSetSourceLevel15"; // NOI18N
    public static final String WARN_SET_SOURCE_LEVEL_6 = "warnSetSourceLevel6"; // NOI18N
    public static final String WARN_SET_SOURCE_LEVEL_7 = "warnSetSourceLevel7"; // NOI18N
    public static final String WARN_SET_SOURCE_LEVEL_8 = "warnSetSourceLevel8"; // NOI18N
    public static final String WARN_SET_SOURCE_LEVEL_11 = "warnSetSourceLevel11"; // NOI18N
    public static final String WARN_SET_SOURCE_LEVEL_21 = "warnSetSourceLevel21"; // NOI18N

    public static final String WARN_JDK_6_REQUIRED = "warnJdk6Required"; // NOI18N
    public static final String WARN_JDK_7_REQUIRED = "warnJdk7Required"; // NOI18N
    public static final String WARN_JDK_8_REQUIRED = "warnJdk8Required"; // NOI18N
    public static final String WARN_JDK_11_REQUIRED = "warnJdk11Required"; // NOI18N
    public static final String WARN_JDK_21_REQUIRED = "warnJdk21Required"; // NOI18N

    private String warningType;

    public J2eeVersionWarningPanel(String warningType) {
        initComponents();
        setWarningType(warningType);
    }

    public String getWarningType() {
        return warningType;
    }

    public void setWarningType(String warningType) {
        String labelText = "";
        this.warningType = warningType;
        if (null != warningType) {
            switch (warningType) {
                case WARN_SET_JDK_15:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk15");
                    break;
                case WARN_SET_JDK_6:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk6");
                    break;
                case WARN_SET_JDK_7:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk7");
                    break;
                case WARN_SET_JDK_8:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk8");
                    break;
                case WARN_SET_JDK_11:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk11");
                    break;
                case WARN_SET_JDK_21:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk21");
                    break;
                case WARN_SET_SOURCE_LEVEL_15:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel15");
                    break;
                case WARN_SET_SOURCE_LEVEL_6:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel6");
                    break;
                case WARN_SET_SOURCE_LEVEL_7:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel7");
                    break;
                case WARN_SET_SOURCE_LEVEL_8:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel8");
                    break;
                case WARN_SET_SOURCE_LEVEL_11:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel11");
                    break;
                case WARN_SET_SOURCE_LEVEL_21:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetSourceLevel21");
                    break;
                case WARN_JDK_6_REQUIRED:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationJDK6");
                    break;
                case WARN_JDK_7_REQUIRED:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationJDK7");
                    break;
                case WARN_JDK_8_REQUIRED:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationJDK8");
                    break;
                case WARN_JDK_11_REQUIRED:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationJDK11");
                    break;
                case WARN_JDK_21_REQUIRED:
                    labelText = NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationJDK21");
                    break;
                default:
                    break;
            }
        }
        jLabel.setText(labelText);
    }

    public String getSuggestedJavaPlatformName() {
        if (null == warningType ) {
            return JavaPlatform.getDefault().getDisplayName();
        }
        else {
            switch (warningType) {
                case WARN_SET_JDK_15: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.5");
                    return getPreferredPlatform(javaPlatforms).getDisplayName();
                }
                case WARN_SET_JDK_6: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.6");
                    return getPreferredPlatform(javaPlatforms).getDisplayName();
                }
                case WARN_SET_JDK_7: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.7");
                    return getPreferredPlatform(javaPlatforms).getDisplayName();
                }
                case WARN_SET_JDK_8: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.8");
                    return getPreferredPlatform(javaPlatforms).getDisplayName();
                }
                case WARN_SET_JDK_11: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("11");
                    return getPreferredPlatform(javaPlatforms).getDisplayName();
                }
                case WARN_SET_JDK_21: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("21");
                    return getPreferredPlatform(javaPlatforms).getDisplayName();
                }
                default:
                    return JavaPlatform.getDefault().getDisplayName();
            }
        }
    }

    public Specification getSuggestedJavaPlatformSpecification() {
        if (null == warningType ) {
            return JavaPlatform.getDefault().getSpecification();
        }
        else {
            switch (warningType) {
                case WARN_SET_JDK_15: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.5");
                    return getPreferredPlatform(javaPlatforms).getSpecification();
                }
                case WARN_SET_JDK_6: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.6");
                    return getPreferredPlatform(javaPlatforms).getSpecification();
                }
                case WARN_SET_JDK_7: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.7");
                    return getPreferredPlatform(javaPlatforms).getSpecification();
                }
                case WARN_SET_JDK_8: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("1.8");
                    return getPreferredPlatform(javaPlatforms).getSpecification();
                }
                case WARN_SET_JDK_11: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("11");
                    return getPreferredPlatform(javaPlatforms).getSpecification();
                }
                case WARN_SET_JDK_21: {
                    JavaPlatform[] javaPlatforms = getJavaPlatforms("21");
                    return getPreferredPlatform(javaPlatforms).getSpecification();
                }
                default:
                    return JavaPlatform.getDefault().getSpecification();
            }
        }
    }

    private static JavaPlatform getPreferredPlatform(@NullAllowed final JavaPlatform[] platforms) {
        final JavaPlatform pp = PreferredProjectPlatform.getPreferredPlatform(JavaPlatform.getDefault().getSpecification().getName());
        if (platforms == null) {
            return pp;
        }
        for (JavaPlatform jp : platforms) {
            if (jp.equals(pp)) {
                return jp;
            }
        }
        return platforms[0];
    }

    public static String findWarningType(Profile j2eeProfile, Set acceptableSourceLevels) {
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion version = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = version.toString();
        
        // no warning if 1.5 is the default for jee5
        if (j2eeProfile == Profile.JAVA_EE_5 && isAcceptableSourceLevel("1.5", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 1.6 is the default for jee6
        if ((j2eeProfile == Profile.JAVA_EE_6_FULL || j2eeProfile == Profile.JAVA_EE_6_WEB) &&
                isAcceptableSourceLevel("1.6", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 1.7 is the default for jee7
        if ((j2eeProfile == Profile.JAVA_EE_7_FULL || j2eeProfile == Profile.JAVA_EE_7_WEB) &&
                isAcceptableSourceLevel("1.7", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 1.8 is the default for jee8
        if ((j2eeProfile == Profile.JAVA_EE_8_FULL || j2eeProfile == Profile.JAVA_EE_8_WEB) &&
                isAcceptableSourceLevel("1.8", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 1.8 is the default for jakartaee8
        if ((j2eeProfile == Profile.JAKARTA_EE_8_FULL || j2eeProfile == Profile.JAKARTA_EE_8_WEB) &&
                isAcceptableSourceLevel("1.8", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 1.8 is the default for jakartaee9
        if ((j2eeProfile == Profile.JAKARTA_EE_9_FULL || j2eeProfile == Profile.JAKARTA_EE_9_WEB) &&
                isAcceptableSourceLevel("1.8", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 11 is the default for jakartaee9.1
        if ((j2eeProfile == Profile.JAKARTA_EE_9_1_FULL || j2eeProfile == Profile.JAKARTA_EE_9_1_WEB) &&
                isAcceptableSourceLevel("11", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        // no warning if 11 is the default for jakartaee10
        if ((j2eeProfile == Profile.JAKARTA_EE_10_FULL || j2eeProfile == Profile.JAKARTA_EE_10_WEB) &&
                isAcceptableSourceLevel("11", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        
        // no warning if 21 is the default for jakartaee11
        if ((j2eeProfile == Profile.JAKARTA_EE_11_FULL || j2eeProfile == Profile.JAKARTA_EE_11_WEB) &&
                isAcceptableSourceLevel("21", sourceLevel, acceptableSourceLevels)) { // NOI18N
            return null;
        }
        
        if (j2eeProfile == Profile.JAVA_EE_5) {
            JavaPlatform[] java15Platforms = getJavaPlatforms("1.5"); //NOI18N
            if (java15Platforms.length > 0) {
                return WARN_SET_JDK_15;
            } else {
                return WARN_SET_SOURCE_LEVEL_15;
            }
        } else if (j2eeProfile == Profile.JAVA_EE_6_FULL || j2eeProfile == Profile.JAVA_EE_6_WEB) {
            JavaPlatform[] java16Platforms = getJavaPlatforms("1.6"); //NOI18N
            if (java16Platforms.length > 0) {
                return WARN_SET_JDK_6;
            } else {
                if (canSetSourceLevel("1.6")) {
                    return WARN_SET_SOURCE_LEVEL_6;
                } else {
                    return WARN_JDK_6_REQUIRED;
                }
            }
        } else if (j2eeProfile == Profile.JAVA_EE_7_FULL || j2eeProfile == Profile.JAVA_EE_7_WEB) {
            JavaPlatform[] java17Platforms = getJavaPlatforms("1.7"); //NOI18N
            if (java17Platforms.length > 0) {
                return WARN_SET_JDK_7;
            } else {
                if (canSetSourceLevel("1.7")) {
                    return WARN_SET_SOURCE_LEVEL_7;
                } else {
                    return WARN_JDK_7_REQUIRED;
                }
            }
        } else if (j2eeProfile == Profile.JAVA_EE_8_FULL || j2eeProfile == Profile.JAVA_EE_8_WEB ||
                    j2eeProfile == Profile.JAKARTA_EE_8_FULL || j2eeProfile == Profile.JAKARTA_EE_8_WEB ||
                    j2eeProfile == Profile.JAKARTA_EE_9_FULL || j2eeProfile == Profile.JAKARTA_EE_9_WEB) {
            JavaPlatform[] java18Platforms = getJavaPlatforms("1.8"); //NOI18N
            if (java18Platforms.length > 0) {
                return WARN_SET_JDK_8;
            } else {
                if (canSetSourceLevel("1.8")) {
                    return WARN_SET_SOURCE_LEVEL_8;
                } else {
                    return WARN_JDK_8_REQUIRED;
                }
            }
        } else if (j2eeProfile == Profile.JAKARTA_EE_9_1_FULL || j2eeProfile == Profile.JAKARTA_EE_9_1_WEB
                || j2eeProfile == Profile.JAKARTA_EE_10_FULL || j2eeProfile == Profile.JAKARTA_EE_10_WEB) {
            JavaPlatform[] java11Platforms = getJavaPlatforms("11"); //NOI18N
            if (java11Platforms.length > 0) {
                return WARN_SET_JDK_11;
            } else {
                if (canSetSourceLevel("11")) {
                    return WARN_SET_SOURCE_LEVEL_11;
                } else {
                    return WARN_JDK_11_REQUIRED;
                }
            }
        } else if (j2eeProfile == Profile.JAKARTA_EE_11_FULL || j2eeProfile == Profile.JAKARTA_EE_11_WEB) {
            JavaPlatform[] java21Platforms = getJavaPlatforms("21"); //NOI18N
            if (java21Platforms.length > 0) {
                return WARN_SET_JDK_21;
            } else {
                if (canSetSourceLevel("21")) {
                    return WARN_SET_SOURCE_LEVEL_21;
                } else {
                    return WARN_JDK_21_REQUIRED;
                }
            }
        } else {
            return null;
        }
    }

    private static boolean canSetSourceLevel(String sourceLevel) {
        SpecificationVersion spec = JavaPlatformManager.getDefault().getDefaultPlatform().getSpecification().getVersion();
        return spec.compareTo(new SpecificationVersion(sourceLevel)) >= 0;
    }

    private static boolean isAcceptableSourceLevel(String minSourceLevel, String sourceLevel, Set<String> acceptableSourceLevels) {
        if (minSourceLevel.equals(sourceLevel)) {
            return true;
        }
        SpecificationVersion minSpec = new SpecificationVersion(minSourceLevel);
        SpecificationVersion spec = new SpecificationVersion(sourceLevel);
        if (minSpec.compareTo(spec) > 0) {
            return false;
        }
        return acceptableSourceLevels.contains(sourceLevel);
    }

    private static JavaPlatform[] getJavaPlatforms(String level) {
        return JavaPlatformManager.getDefault().getPlatforms(null, new Specification("J2SE", new SpecificationVersion(level))); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel = new javax.swing.JLabel();

        jLabel.setText(org.openide.util.NbBundle.getMessage(J2eeVersionWarningPanel.class, "MSG_RecommendationSetJdk7")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 275, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel;
    // End of variables declaration//GEN-END:variables

}
