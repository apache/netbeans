/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.netbeans.modules.python.project2.ui.customizer;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.python.project2.PythonProject2;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

public class PythonCustomizerProvider implements CustomizerProvider {

    private static final String CUSTOMIZER_FOLDER_PATH = "Projects/org-netbeans-modules-python-project2//Customizer"; //NO18N

    private static final Map<Project, Dialog> PROJECT_2_DIALOG = new HashMap<>();

    private final PythonProject2 project;

    public PythonCustomizerProvider(final PythonProject2 project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public void showCustomizer() {
        showCustomizer(null);
    }

    public void showCustomizer(String preselectedCategory) {
        Dialog dialog = PROJECT_2_DIALOG.get(project);
        if (dialog != null) {
            dialog.setVisible(true);
            return;
        }

        final Lookup context = Lookups.fixed(project);
        final OptionListener optionListener = new OptionListener(project);
        final StoreListener storeListener = new StoreListener(project);
        dialog = ProjectCustomizer.createCustomizerDialog(CUSTOMIZER_FOLDER_PATH, context, preselectedCategory,
                optionListener, storeListener, null);
        dialog.addWindowListener(optionListener);
        dialog.setTitle(MessageFormat.format(
                NbBundle.getMessage(PythonCustomizerProvider.class, "LBL_Customizer_Title"),
                ProjectUtils.getInformation(project).getDisplayName()));

        PROJECT_2_DIALOG.put(project, dialog);
        dialog.setVisible(true);
    }

    private class StoreListener implements ActionListener {

        private final PythonProject2 uiProperties;

        StoreListener(PythonProject2 uiProperties) {
            this.uiProperties = uiProperties;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
//            uiProperties.save();
        }
    }

    private static class OptionListener extends WindowAdapter implements ActionListener {

        private final Project project;

        OptionListener(Project project) {
            this.project = project;
        }

        // Listening to OK button ----------------------------------------------
        @Override
        public void actionPerformed(ActionEvent e) {
            // Close & dispose the the dialog
            Dialog dialog = PROJECT_2_DIALOG.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }

        // Listening to window events ------------------------------------------
        @Override
        public void windowClosed(WindowEvent e) {
            PROJECT_2_DIALOG.remove(project);
        }

        @Override
        public void windowClosing(WindowEvent e) {
            //Dispose the dialog otherwsie the {@link WindowAdapter#windowClosed}
            //may not be called
            Dialog dialog = PROJECT_2_DIALOG.get(project);
            if (dialog != null) {
                dialog.setVisible(false);
                dialog.dispose();
            }
        }
    }

}
