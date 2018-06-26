/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.codeception.ui;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

public final class CodeceptRunParametersPanel extends JPanel {

    // <source directory path, parameters>
    private static final Map<String, ParameterContainer> PARAM_HISTORY = new HashMap<>();

    private final PhpModule phpModule;

    private CodeceptRunParametersPanel(@NonNull PhpModule phpModule) {
        assert EventQueue.isDispatchThread();
        assert phpModule != null;
        this.phpModule = phpModule;
        initComponents();
        init();
    }

    @CheckForNull
    @NbBundle.Messages("CodeceptRunParametersPanel.dialog.title=Additional parameters")
    public static String showDialog(@NonNull final PhpModule phpModule) {
        return Mutex.EVENT.readAccess(new Mutex.Action<String>() {

            @Override
            public String run() {
                assert EventQueue.isDispatchThread();
                CodeceptRunParametersPanel panel = new CodeceptRunParametersPanel(phpModule);
                NotifyDescriptor descriptor = new NotifyDescriptor(
                        panel,
                        Bundle.CodeceptRunParametersPanel_dialog_title(),
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.PLAIN_MESSAGE,
                        null,
                        NotifyDescriptor.OK_OPTION);
                if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.OK_OPTION) {
                    return null;
                }
                panel.storeParameters();
                return panel.getSelectedParameters();
            }
        });
    }

    private void init() {
        List<String> params = new ArrayList<>();
        params.add(""); // NOI18N
        params.addAll(getStoredParameters());
        parametersComboBox.setModel(new DefaultComboBoxModel<>(params.toArray(new String[params.size()])));
        preselectLastSelectedParam();
    }

    private void preselectLastSelectedParam() {
        ParameterContainer params = getParameters();
        if (params == null) {
            return;
        }
        String lastSelected = params.getLastSelected();
        if (lastSelected == null) {
            parametersComboBox.setSelectedItem(""); // NOI18N
            return;
        }
        for (int i = 0; i < parametersComboBox.getItemCount(); i++) {
            Object item = parametersComboBox.getItemAt(i);
            if (item.equals(lastSelected)) {
                parametersComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private String getSelectedParameters() {
        return parametersComboBox.getSelectedItem().toString().trim();
    }

    @CheckForNull
    private ParameterContainer getParameters() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return null;
        }
        String sourceDirectoryPath = sourceDirectory.getPath();
        return PARAM_HISTORY.get(sourceDirectoryPath);
    }

    private List<String> getStoredParameters() {
        ParameterContainer stored = getParameters();
        if (stored == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(stored.getParams());
        Collections.sort(result);
        return result;
    }

    private void storeParameters() {
        FileObject sourceDirectory = phpModule.getSourceDirectory();
        if (sourceDirectory == null) {
            // broken project
            return;
        }
        String sourceDirectoryPath = sourceDirectory.getPath();
        ParameterContainer params = PARAM_HISTORY.get(sourceDirectoryPath);
        if (params == null) {
            params = new ParameterContainer();
            PARAM_HISTORY.put(sourceDirectoryPath, params);
        }
        String currentParam = getSelectedParameters();

        params.addParam(currentParam);
        params.setLastSelected(currentParam);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        parametersComboBox = new JComboBox<String>();

        parametersComboBox.setEditable(true);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(parametersComboBox, GroupLayout.Alignment.TRAILING, 0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addComponent(parametersComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JComboBox<String> parametersComboBox;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    /**
     * Holds a set of parameters and maintains info on what parameter was the
     * last one selected.
     */
    private static final class ParameterContainer {

        private final Set<String> params = new HashSet<>();

        // @GuardedBy("EDT")
        private String lastSelected;


        public void addParam(String param) {
            params.add(param);
        }

        public String getLastSelected() {
            assert EventQueue.isDispatchThread();
            return lastSelected;
        }

        public void setLastSelected(String lastSelected) {
            assert EventQueue.isDispatchThread();
            this.lastSelected = lastSelected;
        }

        public Set<String> getParams() {
            return Collections.unmodifiableSet(params);
        }

    }

}
