/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

/*
 * ConfirmationPanel.java
 *
 * Created on 26-Mar-2009, 18:07:26
 */

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.IOException;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.client.JAXWSClientSupport;
import org.netbeans.modules.websvc.api.jaxws.project.CatalogUtils;
import org.netbeans.modules.websvc.api.jaxws.project.WSUtils;
import org.netbeans.modules.websvc.api.jaxws.project.config.Client;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.spi.jaxws.client.JAXWSClientSupportImpl;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author mkuchtiak
 */
public class JaxWsCatalogPanel extends javax.swing.JPanel {
    static final String GENERATE_JAXWS_CATALOG = "generate_jax_ws_catalog"; //NOI18N

    /** Creates new JaxWsCatalogPanel */
    public JaxWsCatalogPanel() {
        initComponents();
    }

    boolean notAskAgain() {
        return notAskAgain.isSelected();
    }

    private static boolean generateJaxWsCatalog(String projectRoot) {
        Preferences prefs = NbPreferences.forModule(JaxWsCatalogPanel.class);
        if (prefs == null || prefs.get(JaxWsCatalogPanel.GENERATE_JAXWS_CATALOG , null) == null) {
            JaxWsCatalogPanel panel =
                new JaxWsCatalogPanel();
            DialogDescriptor dd = new DialogDescriptor(
                    panel,
                    NbBundle.getMessage(JaxWsCatalogPanel.class,"TTL_GenerateJaxWsCatalog", projectRoot),
                    true,
                    DialogDescriptor.YES_NO_OPTION,
                    null,null);
            Object result = DialogDisplayer.getDefault().notify(dd);
            if (panel.notAskAgain()) {
                if (prefs != null) {
                    if (NotifyDescriptor.YES_OPTION.equals(result)) {
                        prefs.put(JaxWsCatalogPanel.GENERATE_JAXWS_CATALOG, "true"); //NOI18N
                    } else if ((NotifyDescriptor.NO_OPTION.equals(result))) {
                        prefs.put(JaxWsCatalogPanel.GENERATE_JAXWS_CATALOG, "false"); //NOI18N
                    }
                }
            }
            return NotifyDescriptor.YES_OPTION.equals(result);
        } else {
            return "true".equals(prefs.get(JaxWsCatalogPanel.GENERATE_JAXWS_CATALOG , null)); //NOI18N
        }
    }

    static void generateJaxWsCatalog(Project project, JAXWSClientSupport clientSupport) throws IOException {
        if (JaxWsCatalogPanel.generateJaxWsCatalog(project.getProjectDirectory().getName())) {
            FileObject wsdlFolder = clientSupport.getWsdlFolder(true);
            if (wsdlFolder != null) {
                FileObject jaxWsCatalog = WSUtils.retrieveJaxWsCatalogFromResource(wsdlFolder.getParent());
                FileObject catalog = project.getProjectDirectory().getFileObject(JAXWSClientSupportImpl.CATALOG_FILE);
                if (catalog != null) {
                    JaxWsModel jaxWsModel = project.getLookup().lookup(JaxWsModel.class);
                    if (jaxWsModel != null) {
                        CatalogUtils.copyCatalogEntriesForAllClients(catalog, jaxWsCatalog, jaxWsModel);
                        for (Client client : jaxWsModel.getClients()) {
                            FileObject wsdlSourceFolder = clientSupport.getLocalWsdlFolderForClient(client.getName(), false);
                            if (wsdlSourceFolder != null) {
                                WSUtils.copyFiles(wsdlSourceFolder, wsdlFolder);
                            }
                        }
                    }
                }
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        textLabel = new javax.swing.JLabel();
        notAskAgain = new javax.swing.JCheckBox();

        textLabel.setText(org.openide.util.NbBundle.getMessage(JaxWsCatalogPanel.class, "MSG_CreateJaxWs")); // NOI18N

        notAskAgain.setText(org.openide.util.NbBundle.getMessage(JaxWsCatalogPanel.class, "MSG_DontAskAgain")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(26, 26, 26)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(notAskAgain)
                    .addComponent(textLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(textLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(notAskAgain)
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox notAskAgain;
    private javax.swing.JLabel textLabel;
    // End of variables declaration//GEN-END:variables

}
