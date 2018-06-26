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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.project.ui.customizer;

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.classpath.IncludePathSupport;
import org.netbeans.modules.php.project.ui.LastUsedFolders;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.spi.project.ui.support.ProjectCustomizer.Category;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

public final class CustomizerIncludePath extends JPanel implements HelpCtx.Provider {

    private static final Logger LOGGER = Logger.getLogger(CustomizerIncludePath.class.getName());

    private static final long serialVersionUID = 1564768521234L;

    private final Category category;
    private final PhpProjectProperties uiProps;
    private final PhpProject project;
    private final DefaultListModel<BasePathSupport.Item> publicIncludePathListModel;
    private final DefaultListModel<BasePathSupport.Item> privateIncludePathListModel;


    public CustomizerIncludePath(Category category, PhpProjectProperties uiProps) {
        assert category != null;
        assert uiProps != null;

        this.category = category;
        this.uiProps = uiProps;
        project = uiProps.getProject();
        assert project != null;

        publicIncludePathListModel = uiProps.getIncludePathListModel();
        privateIncludePathListModel = uiProps.getPrivateIncludePathListModel();

        initComponents();
        init();
    }

    @NbBundle.Messages({
        "CustomizerIncludePath.tab.public.title=Shared",
        "CustomizerIncludePath.tab.private.title=Private",
    })
    private void init() {
        ListCellRenderer<BasePathSupport.Item> includePathListRenderer = uiProps.getIncludePathListRenderer();
        ChangeListener defaultChangeListener = new DefaultChangeListener();
        // include paths
        CustomizerIncludePathInternal publicIncludePath = new CustomizerIncludePathInternal(project, publicIncludePathListModel,
                includePathListRenderer, LastUsedFolders.PROJECT_INCLUDE_PATH);
        CustomizerIncludePathInternal privateIncludePath = new CustomizerIncludePathInternal(project, privateIncludePathListModel,
                includePathListRenderer, LastUsedFolders.PROJECT_PRIVATE_INCLUDE_PATH);
        // listeners
        publicIncludePath.addChangeListener(defaultChangeListener);
        privateIncludePath.addChangeListener(defaultChangeListener);
        // tabs
        includePathTabbedPane.add(Bundle.CustomizerIncludePath_tab_public_title(), publicIncludePath);
        includePathTabbedPane.add(Bundle.CustomizerIncludePath_tab_private_title(), privateIncludePath);
        // initial validation
        validateData();
    }

    void validateData() {
        assert EventQueue.isDispatchThread();
        // errors
        ValidationResult publicResult = new IncludePathSupport.Validator()
                .validatePaths(project, convertToList(publicIncludePathListModel))
                .getResult();
        if (publicResult.hasErrors()) {
            processErrors(Bundle.CustomizerIncludePath_tab_public_title(), publicResult.getErrors(), publicIncludePathListModel);
            return;
        }
        ValidationResult privateResult = new IncludePathSupport.Validator()
                .validatePaths(project, convertToList(privateIncludePathListModel))
                .getResult();
        if (privateResult.hasErrors()) {
            processErrors(Bundle.CustomizerIncludePath_tab_private_title(), privateResult.getErrors(), privateIncludePathListModel);
            return;
        }
        // warnings
        if (publicResult.hasWarnings()) {
            processWarnings(Bundle.CustomizerIncludePath_tab_public_title(), publicResult.getWarnings());
            return;
        }
        if (privateResult.hasWarnings()) {
            processWarnings(Bundle.CustomizerIncludePath_tab_private_title(), privateResult.getWarnings());
            return;
        }
        // everything ok
        assert publicResult.isFaultless() : publicResult;
        assert privateResult.isFaultless() : privateResult;
        category.setErrorMessage(null);
        category.setValid(true);
    }

    @NbBundle.Messages({
        "# {0} - include path type (shared/private)",
        "# {1} - error message",
        "CustomizerIncludePath.error={0}: {1}",
    })
    private void processErrors(String prefix, List<ValidationResult.Message> errors, final DefaultListModel<BasePathSupport.Item> includePathListModel) {
        assert EventQueue.isDispatchThread();
        assert !errors.isEmpty();
        final ValidationResult.Message error = errors.get(0);
        if (error.isType(IncludePathSupport.Validator.ANOTHER_PROJECT_MESSAGE_TYPE)) {
            // postpone dialog so customizer is shown first
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    askUserToFixPath(includePathListModel, (BasePathSupport.Item) error.getSource());
                }
            });
        }
        category.setErrorMessage(Bundle.CustomizerIncludePath_error(prefix, error.getMessage()));
        category.setValid(false);
    }

    @NbBundle.Messages({
        "# {0} - include path type (shared/private)",
        "# {1} - warning message",
        "CustomizerIncludePath.warning={0}: {1}",
    })
    private void processWarnings(String prefix, List<ValidationResult.Message> warnings) {
        assert EventQueue.isDispatchThread();
        assert !warnings.isEmpty();
        category.setErrorMessage(Bundle.CustomizerIncludePath_warning(prefix, warnings.get(0).getMessage()));
        category.setValid(true);
    }

    private List<BasePathSupport.Item> convertToList(DefaultListModel<BasePathSupport.Item> listModel) {
        List<BasePathSupport.Item> items = new ArrayList<>(listModel.getSize());
        for (int i = 0; i < listModel.getSize(); i++) {
            items.add(listModel.get(i));
        }
        return items;
    }

    @NbBundle.Messages({
        "# {0} - file path",
        "# {1} - project name",
        "CustomizerPhpIncludePath.error.anotherProjectSubFile=Path {0} belongs to project {1}. Remove it and add Source Files of that project?",
        "# {0} - project name",
        "CustomizerPhpIncludePath.error.brokenProject=Project {0} is broken, open and repair it manually.",
    })
    private void askUserToFixPath(DefaultListModel<BasePathSupport.Item> includePathListModel, BasePathSupport.Item item) {
        PhpProject currentProject = uiProps.getProject();
        FileObject fileObject = item.getFileObject(currentProject.getProjectDirectory());
        assert fileObject != null;
        PhpProject owningProject = PhpProjectUtils.getPhpProject(fileObject);
        assert owningProject != null;
        String owningProjectDisplayName = ProjectUtils.getInformation(owningProject).getDisplayName();
        NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(
                Bundle.CustomizerPhpIncludePath_error_anotherProjectSubFile(item.getAbsoluteFilePath(currentProject.getProjectDirectory()), owningProjectDisplayName),
                NotifyDescriptor.YES_NO_OPTION);
        if (DialogDisplayer.getDefault().notify(descriptor) != NotifyDescriptor.YES_OPTION) {
            return;
        }
        // fix path
        FileObject sourcesDirectory = ProjectPropertiesSupport.getSourcesDirectory(owningProject);
        if (sourcesDirectory == null) {
            // #245388
            LOGGER.log(Level.INFO, "Source files of project {0} not found, Include Path cannot be fixed", owningProject.getName());
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(
                    Bundle.CustomizerPhpIncludePath_error_brokenProject(owningProjectDisplayName),
                    NotifyDescriptor.WARNING_MESSAGE));
            return;
        }
        int index = includePathListModel.indexOf(item);
        assert index != -1;
        includePathListModel.set(index, BasePathSupport.Item.create(FileUtil.toFile(sourcesDirectory).getAbsolutePath(), null));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        includePathLabel = new JLabel();
        includePathTabbedPane = new JTabbedPane();
        includePathInfoLabel = new JLabel();

        Mnemonics.setLocalizedText(includePathLabel, NbBundle.getMessage(CustomizerIncludePath.class, "CustomizerIncludePath.includePathLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(includePathInfoLabel, NbBundle.getMessage(CustomizerIncludePath.class, "CustomizerIncludePath.includePathInfoLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(includePathLabel)
            .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
            .addComponent(includePathTabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(includePathLabel)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(includePathTabbedPane)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(includePathInfoLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JLabel includePathInfoLabel;
    private JLabel includePathLabel;
    private JTabbedPane includePathTabbedPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.php.project.ui.customizer.CustomizerPhpIncludePath"); // NOI18N
    }

    //~ Inner classes

    private final class DefaultChangeListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            validateData();
        }

    }

}
