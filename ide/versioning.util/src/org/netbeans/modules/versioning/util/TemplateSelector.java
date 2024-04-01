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

package org.netbeans.modules.versioning.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class TemplateSelector implements ActionListener {

    private static final String KEY_AUTO_FILL     = "vcstemplate.autofill";     // NOI18N
    private static final String KEY_TEMPLATE      = "vcstemplate.value";        // NOI18N
    private static final String KEY_TEMPLATE_FILE = "vcstemplate.templatefile"; // NOI18N

    private TemplatesPanel panel;
    private final Preferences preferences;

    public TemplateSelector(Preferences preferences) {
        this.preferences = preferences;
    }

    public boolean show (String helpCtxId) {
        getPanel().autoFillInCheckBox.setSelected(isAutofill());
        getPanel().templateTextArea.setText(getTemplate());
        if(showPanel(helpCtxId)) {
            setAutofill(getPanel().autoFillInCheckBox.isSelected());
            setTemplate(getPanel().templateTextArea.getText());
            return true;
        }
        return false;
    }

    private boolean showPanel (String helpCtxId) {
        DialogDescriptor descriptor = new DialogDescriptor (
                getPanel(),
                NbBundle.getMessage(TemplateSelector.class, "CTL_TemplateTitle"),   // NOI18N
                true,
                new Object[] {DialogDescriptor.OK_OPTION, DialogDescriptor.CANCEL_OPTION},
                DialogDescriptor.OK_OPTION,
                DialogDescriptor.DEFAULT_ALIGN,
                new HelpCtx(helpCtxId),
                null);
        return DialogDisplayer.getDefault().notify(descriptor) == DialogDescriptor.OK_OPTION;
    }

    private TemplatesPanel getPanel() {
        if(panel == null) {
            panel = new TemplatesPanel();
            panel.openButton.addActionListener(this);
            panel.saveButton.addActionListener(this);
        }
        return panel;
    }

    public boolean isAutofill() {
        return preferences.getBoolean(KEY_AUTO_FILL, false);
    }

    public String getTemplate() {
        return preferences.get(KEY_TEMPLATE, ""); // NOI18N
    }

    private void setAutofill(boolean bl) {
        preferences.putBoolean(KEY_AUTO_FILL, bl);
    }

    private void setTemplate(String template) {
        preferences.put(KEY_TEMPLATE, template);
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == getPanel().openButton) {
            onOpen();
        } else if(e.getSource() == getPanel().saveButton) {
            onSave();
        }
    }
    
    private void onOpen() {
        File file = selectFile(JFileChooser.OPEN_DIALOG, NbBundle.getMessage(TemplateSelector.class, "CTL_Load")); // NOI18N
        if (file == null) {
            return;
        }

        try {
            byte[] bytes = getFileContentsAsByteArray(file);
            if (bytes != null) {
                getPanel().templateTextArea.setText(new String(bytes));
            }
        } catch (IOException ex) {
            Utils.logError(TemplatesPanel.class, ex);
        }
        preferences.put(KEY_TEMPLATE_FILE, file.getAbsolutePath());
    }

    private void onSave() {
        File file = selectFile(JFileChooser.SAVE_DIALOG, NbBundle.getMessage(TemplateSelector.class, "CTL_Save")); // NOI18N
        if (file == null) {
            return;
        }

        String template = getPanel().templateTextArea.getText();
        try {
            FileUtils.copyStreamToFile(new ByteArrayInputStream(template.getBytes()), file);
        } catch (IOException ex) {
            Utils.logError(TemplatesPanel.class, ex);
        }
        preferences.put(KEY_TEMPLATE_FILE, file.getAbsolutePath());
    }

    private File selectFile(int dialogType, String approveButtonText) {
        JFileChooser fileChooser = new AccessibleJFileChooser(NbBundle.getMessage(TemplateSelector.class, "ACSD_SelectTemplate")/*, defaultDir*/);// NOI18N
        fileChooser.setDialogTitle(NbBundle.getMessage(TemplateSelector.class, "CTL_SelectTemplate"));// NOI18N
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogType(dialogType);

        File file = getTemplateFile();

//        if(file.isFile() && dialogType == JFileChooser.OPEN_DIALOG) {
//            fileChooser.setSelectedFile(file);
//        } else {
//            fileChooser.setCurrentDirectory(file.isFile() ? file.getParentFile() : file);
//        }
        if(file.isFile() ) {
            fileChooser.setSelectedFile(file);
        } else {
            fileChooser.setCurrentDirectory(file);
        }

        fileChooser.showDialog(getPanel(), approveButtonText);
        File f = fileChooser.getSelectedFile();
        return f;
    }

    private File getTemplateFile() {
        File file = null;

        String tmpFile = preferences.get(KEY_TEMPLATE_FILE, null);
        if(tmpFile != null) {
            file = new File(tmpFile);
        }

        if (file == null) {
            file = new File(System.getProperty("user.home"));  // NOI18N
        }
        return file;
    }

    private static byte[] getFileContentsAsByteArray (File file) throws IOException {
        long length = file.length();
        if(length > 1024 * 10) {
            NotifyDescriptor nd =
                new NotifyDescriptor(
                    NbBundle.getMessage(TemplateSelector.class, "MSG_FileTooBig"),
                    NbBundle.getMessage(TemplateSelector.class, "LBL_FileTooBig"),    // NOI18N
                    NotifyDescriptor.DEFAULT_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION},
                    NotifyDescriptor.OK_OPTION);
            if(DialogDisplayer.getDefault().notify(nd) != NotifyDescriptor.OK_OPTION) {
                return null;
            }
        }

        return FileUtils.getFileContentsAsByteArray(file);
    }
}
