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
package org.netbeans.modules.java.project.ui;

import java.awt.Color;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.UIManager;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.PlatformsCustomizer;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class FixPlatform extends javax.swing.JPanel {
    private static final RequestProcessor WORKER = new RequestProcessor(FixPlatform.class);

    private final String propertName;
    private final String requiredPlatformId;
    private final PropertyEvaluator eval;
    private final AntProjectHelper helper;
    private final BrokenReferencesSupport.PlatformUpdatedCallBack callback;
    private final JButton ok;

    /**
     * Creates new form FixPlatform
     */
    FixPlatform(
            @NonNull final String propertyName,
            @NonNull final String requiredPlatformId,
            @NullAllowed final String platformType,
            @NonNull final PropertyEvaluator eval,
            @NonNull final AntProjectHelper helper,
            @NullAllowed final BrokenReferencesSupport.PlatformUpdatedCallBack callback,
            @NonNull final JButton ok) {
        Parameters.notNull("propertyName", propertyName);   //NOI18N
        Parameters.notNull("requiredPlatformId", requiredPlatformId);   //NOI18N
        Parameters.notNull("eval", eval);   //NOI18N
        Parameters.notNull("helper", helper);   //NOI18N
        Parameters.notNull("ok", ok);   //NOI18N
        this.propertName = propertyName;
        this.requiredPlatformId = requiredPlatformId;
        this.eval = eval;
        this.helper = helper;
        this.callback = callback;
        this.ok = ok;
        initComponents();
        ok.setEnabled(false);
        this.platforms.setModel(new PlatformsModel(requiredPlatformId, platformType));
        this.platforms.setRenderer(new PlatformsRenderer());
        this.platforms.addActionListener((ae) -> {
            final JavaPlatform jp = (JavaPlatform) this.platforms.getSelectedItem();
            ok.setEnabled(jp != null && !jp.getInstallFolders().isEmpty());
        });
    }

    final Future<ProjectProblemsProvider.Result> resolve() {
        final JavaPlatform selected = (JavaPlatform) platforms.getSelectedItem();
        if (selected == null) {
            return ProjectProblemsProviders.future(
                    ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
        }
        final String antName = getJavaPlatformAntName(selected);
        if (requiredPlatformId.equals(antName)) {
            return ProjectProblemsProviders.future(
                    ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED));
        }
        return WORKER.submit(() -> {
            return ProjectManager.mutex().writeAccess(() -> {
                try {
                    final EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.setProperty(propertName, antName);
                    helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                    if (callback != null) {
                        callback.platformPropertyUpdated(selected);
                    }
                    final Project p = FileOwnerQuery.getOwner(helper.getProjectDirectory());
                    ProjectManager.getDefault().saveProject(p);
                    return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.RESOLVED);
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                    return ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED);
                }
            });
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label1 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        create = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        platforms = new javax.swing.JComboBox<>();
        hint = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(label1, NbBundle.getMessage(FixPlatform.class, "LBL_MissingPlatform", getHtmlColor(getErrorForeground()), requiredPlatformId));

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(FixPlatform.class, "LBL_CreateNewPlatform")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(create, org.openide.util.NbBundle.getMessage(FixPlatform.class, "FixPlatform.create.text")); // NOI18N
        create.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createActionPerformed(evt);
            }
        });

        jLabel2.setLabelFor(platforms);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(FixPlatform.class, "LBL_UseExistingPlatform")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(hint, NbBundle.getMessage(FixPlatform.class, "LBL_PlatformHint", requiredPlatformId));
        hint.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(hint, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(label1)
                                .addGap(0, 0, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(create)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(platforms, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(label1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(create))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(platforms, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(22, 22, 22)
                .addComponent(hint)
                .addContainerGap(67, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void createActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createActionPerformed
        PlatformsCustomizer.showCustomizer(null);
    }//GEN-LAST:event_createActionPerformed

    private static String getJavaPlatformAntName(@NonNull final JavaPlatform jp) {
        return jp.getProperties().get(ProjectProblemsProviders.PLAT_PROP_ANT_NAME);
    }

    @NonNull
    private static Color getErrorForeground() {
        Color result = UIManager.getDefaults().getColor("nb.errorForeground");  //NOI18N
        if (result == null) {
            result = Color.RED;
        }
        return result;
    }

    @NonNull
    private static String getHtmlColor(@NonNull final Color c) {
        final int r = c.getRed();
        final int g = c.getGreen();
        final int b = c.getBlue();
        final StringBuilder result = new StringBuilder();
        result.append ("#");        //NOI18N
        final String rs = Integer.toHexString (r);
        final String gs = Integer.toHexString (g);
        final String bs = Integer.toHexString (b);
        if (r < 0x10)
            result.append('0');
        result.append(rs);
        if (g < 0x10)
            result.append ('0');
        result.append(gs);
        if (b < 0x10)
            result.append ('0');
        result.append(bs);
        return result.toString();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton create;
    private javax.swing.JLabel hint;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel label1;
    private javax.swing.JComboBox<JavaPlatform> platforms;
    // End of variables declaration//GEN-END:variables

    private static final class PlatformsModel extends AbstractListModel<JavaPlatform>
            implements ComboBoxModel<JavaPlatform>, PropertyChangeListener {
        private final String requiredPlatformId;
        private final String platformType;
        private final JavaPlatformManager pm;
        private final BrokenPlatform broken;
        private JavaPlatform[] platforms;
        private JavaPlatform selected;

        PlatformsModel(
                @NonNull final String requiredPlatformId,
                @NullAllowed final String platformType) {
            Parameters.notNull("requiredPlatformId", requiredPlatformId);   //NOI18N
            this.requiredPlatformId = requiredPlatformId;
            this.platformType = platformType;
            this.broken = new BrokenPlatform(requiredPlatformId);
            this.pm = JavaPlatformManager.getDefault();
            this.pm.addPropertyChangeListener(WeakListeners.propertyChange(this, this.pm));
            init();
        }

        @Override
        public int getSize() {
            return platforms.length;
        }

        @Override
        public JavaPlatform getElementAt(int index) {
            return platforms[index];
        }

        private void init () {
            final List<JavaPlatform> newPlatfs = new ArrayList<>();
            Arrays.stream(this.pm.getPlatforms(null, new Specification(
                        this.platformType,
                        null)))
                    .filter((jp) -> !jp.getInstallFolders().isEmpty())
                    .forEach(newPlatfs::add);
            final Optional<JavaPlatform> resolved = newPlatfs.stream()
                    .filter((jp) -> requiredPlatformId.equals(getJavaPlatformAntName(jp)))
                    .findAny();
            JavaPlatform toSelect;
            if (resolved.isPresent()) {
                toSelect = resolved.get();
            } else {
                toSelect = broken;
                newPlatfs.add(broken);
            }
            newPlatfs.sort((p1,p2) -> {
                if (p1 == broken) {
                    return -1;
                }
                if (p2 == broken) {
                    return 1;
                }
                return p1.getDisplayName().compareTo(p2.getDisplayName());
            });
            platforms  = newPlatfs.toArray(new JavaPlatform[0]);
            setSelectedItem(toSelect);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selected = (JavaPlatform) anItem;
        }

        @Override
        public Object getSelectedItem() {
            return selected;
        }

        @Override
        public void propertyChange(@NonNull final PropertyChangeEvent evt) {
            Mutex.EVENT.readAccess(()-> {
                int up = platforms == null ?
                        0 :
                        platforms.length;
                init();
                up = Math.max(up, platforms.length);
                fireContentsChanged(this, 0, up);
            });
        }
    }

    private static final class PlatformsRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(
                @NonNull final JList<?> list,
                @NonNull Object value,
                final int index,
                final boolean isSelected,
                final boolean cellHasFocus) {
            if (value instanceof JavaPlatform) {
                final JavaPlatform jp = (JavaPlatform) value;
                value = jp.getDisplayName();
                if (jp.getInstallFolders().isEmpty()) {
                    value = String.format(
                            "<html><font color=\"%s\">%s</font>", //NOI18N
                            getHtmlColor(getErrorForeground()),
                            value);
                }

            }
            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static final class BrokenPlatform extends JavaPlatform {
        private final String id;

        BrokenPlatform(@NonNull final String id) {
            this.id = id;
        }

        @Override
        public String getDisplayName() {
            return this.id;
        }

        @Override
        public Map<String, String> getProperties() {
            return Collections.singletonMap(
                    ProjectProblemsProviders.PLAT_PROP_ANT_NAME, id);
        }

        @Override
        public ClassPath getBootstrapLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public ClassPath getStandardLibraries() {
            return ClassPath.EMPTY;
        }

        @Override
        public String getVendor() {
            return "";  //NOI18N
        }

        @Override
        public Specification getSpecification() {
            return new Specification(null, null);
        }

        @Override
        public Collection<FileObject> getInstallFolders() {
            return Collections.emptySet();
        }

        @Override
        public FileObject findTool(String toolName) {
            return null;
        }

        @Override
        public ClassPath getSourceFolders() {
            return ClassPath.EMPTY;
        }

        @Override
        public List<URL> getJavadocFolders() {
            return Collections.emptyList();
        }
    }
}
