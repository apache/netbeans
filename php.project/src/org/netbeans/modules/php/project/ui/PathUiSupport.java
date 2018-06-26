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

package org.netbeans.modules.php.project.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;
import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.ButtonModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.classpath.BasePathSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;

/**
 * @author Petr Hrebejk, Tomas Mysik
 */
public final class PathUiSupport {

    private PathUiSupport() {
    }

    public static DefaultListModel<BasePathSupport.Item> createListModel(Iterator<BasePathSupport.Item> it) {
        DefaultListModel<BasePathSupport.Item> model = new DefaultListModel<>();
        while (it.hasNext()) {
            model.addElement(it.next());
        }
        return model;
    }

    public static Iterator<BasePathSupport.Item> getIterator(DefaultListModel<BasePathSupport.Item> model) {
        // XXX Better performing impl. would be nice
        return getList(model).iterator();
    }

    public static List<BasePathSupport.Item> getList(DefaultListModel<BasePathSupport.Item> model) {
        return Collections.list(NbCollections.checkedEnumerationByFilter(model.elements(),
                BasePathSupport.Item.class, true));
    }

    /** Moves items up in the list. The indices array will contain
     * indices to be selected after the change was done.
     */
    public static int[] moveUp(DefaultListModel<BasePathSupport.Item> listModel, int[] indices) {

        if (indices == null || indices.length == 0) {
            assert false : "MoveUp button should be disabled";
        }

        // Move the items up
        for (int i = 0; i < indices.length; i++) {
            BasePathSupport.Item item = listModel.get(indices[i]);
            listModel.remove(indices[i]);
            listModel.add(indices[i] - 1, item);
        }

        // Keep the selection a before
        for (int i = 0; i < indices.length; i++) {
            indices[i] -= 1;
        }
        return indices;

    }

    public static boolean canMoveUp(ListSelectionModel selectionModel) {
        return selectionModel.getMinSelectionIndex() > 0;
    }

    /** Moves items down in the list. The indices array will contain
     * indices to be selected after the change was done.
     */
    public static int[] moveDown(DefaultListModel<BasePathSupport.Item> listModel, int[] indices) {

        assert indices != null;
        if (indices.length == 0) {
            assert false : "MoveDown button should be disabled";
        }

        // Move the items up
        for (int i = indices.length - 1; i >= 0; i--) {
            BasePathSupport.Item item = listModel.get(indices[i]);
            listModel.remove(indices[i]);
            listModel.add(indices[i] + 1, item);
        }

        // Keep the selection a before
        for (int i = 0; i < indices.length; i++) {
            indices[i] += 1;
        }
        return indices;

    }

    public static boolean canMoveDown(ListSelectionModel selectionModel, int modelSize) {
        int iMax = selectionModel.getMaxSelectionIndex();
        return iMax != -1 && iMax < modelSize - 1;
    }

    /** Removes selected indices from the model. Returns the index to be selected
     */
    public static int[] remove(DefaultListModel<BasePathSupport.Item> listModel, int[] indices) {

        assert indices != null;
        if (indices.length == 0) {
            assert false : "Remove button should be disabled";
        }

        // Remove the items
        for (int i = indices.length - 1; i >= 0; i--) {
            listModel.remove(indices[i]);
        }

        if (!listModel.isEmpty()) {
            // Select reasonable item
            int selectedIndex = indices[indices.length - 1] - indices.length  + 1;
            if (selectedIndex > listModel.size() - 1) {
                selectedIndex = listModel.size() - 1;
            }
            return new int[] {selectedIndex};
        }
        return new int[] {};
    }

    public static int[] addFolders(DefaultListModel<BasePathSupport.Item> listModel, int[] indices, String[] files) {

        int lastIndex = indices == null || indices.length == 0 ? listModel.getSize() - 1 : indices[indices.length - 1];
        int[] indexes = new int[files.length];
        for (int i = 0, delta = 0; i + delta < files.length;) {
            int current = lastIndex + 1 + i;
            BasePathSupport.Item item = BasePathSupport.Item.create(files[i + delta], null);
            if (!listModel.contains(item)) {
                listModel.add(current, item);
                indexes[delta + i] = current;
                i++;
            } else {
                indexes[i + delta] = listModel.indexOf(item);
                delta++;
            }
        }
        return indexes;
    }

    public static class ClassPathListCellRenderer implements ListCellRenderer<BasePathSupport.Item> {

        private static final long serialVersionUID = 78866546546546546L;

        @StaticResource
        private static final String RESOURCE_ICON_BROKEN_BADGE
                = "org/netbeans/modules/php/project/ui/resources/brokenProjectBadge.gif"; //NOI18N

        private static final ImageIcon ICON_BROKEN_BADGE = ImageUtilities.loadImageIcon(RESOURCE_ICON_BROKEN_BADGE, false);
        private static ImageIcon ICON_FOLDER = null;
        private static ImageIcon ICON_BROKEN_FOLDER = null;

        private final DefaultListCellRenderer delegate = new DefaultListCellRenderer();
        private final PropertyEvaluator evaluator;
        private final FileObject projectFolder;

        private static final Map<String, String> WELL_KNOWN_PATHS_NAMES = new HashMap<>();
        static {
            WELL_KNOWN_PATHS_NAMES.put(PhpProjectProperties.GLOBAL_INCLUDE_PATH,
                    NbBundle.getMessage(PathUiSupport.class, "LBL_GlobalIncludePath_DisplayName"));
        };

        // used for global include path (no evaluator, no project folder)
        public ClassPathListCellRenderer() {
            this(null, null);
        }

        public ClassPathListCellRenderer(PropertyEvaluator evaluator, FileObject projectFolder) {
            super();

            this.evaluator = evaluator;
            this.projectFolder = projectFolder;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends BasePathSupport.Item> list, BasePathSupport.Item value, int index, boolean isSelected,
                boolean cellHasFocus) {
            BasePathSupport.Item item = value;
            JLabel component = (JLabel) delegate.getListCellRendererComponent(list, getDisplayName(item), index, isSelected, cellHasFocus);
            component.setIcon(getIcon(item));
            component.setToolTipText(getToolTipText(item));
            return component;
        }

        private String getDisplayName(BasePathSupport.Item item) {
            switch (item.getType()) {
                case CLASSPATH:
                    String name = WELL_KNOWN_PATHS_NAMES.get(BasePathSupport.getAntPropertyName(item.getReference()));
                    return name == null ? item.getReference() : name;
                    //break;
                default:
                    if (item.isBroken()) {
                        if (new File(item.getFilePath()).isAbsolute()) {
                            // absolute file path
                            return NbBundle.getMessage(PathUiSupport.class, "LBL_MissingFile", getFileRefName(item));
                        }
                        // just reference
                        return NbBundle.getMessage(PathUiSupport.class, "LBL_BrokenReference", getFileRefName(item));
                    }
                    return item.getAbsoluteFilePath(projectFolder);
                    //break;
            }
        }

        private static Icon getIcon(BasePathSupport.Item item) {
            switch (item.getType()) {
                case CLASSPATH:
                    return ImageUtilities.image2Icon(Utils.getIncludePathIcon(false));
                    //break;
                default:
                    if (item.isBroken()) {
                        if (ICON_BROKEN_FOLDER == null) {
                            ICON_BROKEN_FOLDER = new ImageIcon(ImageUtilities.mergeImages(getFolderIcon().getImage(),
                                    ICON_BROKEN_BADGE.getImage(), 7, 7));
                        }
                        return ICON_BROKEN_FOLDER;
                    }
                    return getFolderIcon();
                    //break;
            }
        }

        private String getToolTipText(BasePathSupport.Item item) {
            switch (item.getType()) {
                case FOLDER:
                    if (item.isBroken()) {
                        if (evaluator != null) {
                            return evaluator.evaluate(item.getReference());
                        }
                        return item.getReference();
                    }
                    return item.getAbsoluteFilePath(projectFolder);
                    //break;
                default:
                    // noop
            }
            return null;
        }

        private static ImageIcon getFolderIcon() {
            if (ICON_FOLDER == null) {
                DataFolder dataFolder = DataFolder.findFolder(FileUtil.getConfigRoot());
                ICON_FOLDER = new ImageIcon(dataFolder.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16));
            }
            return ICON_FOLDER;
        }

        private String getFileRefName(BasePathSupport.Item item) {
            switch (item.getType()) {
                case FOLDER:
                    return item.getFilePath();
                    //break;
                default:
                    return item.getReference();
                    //break;
            }
        }
    }

    public static final class EditMediator implements ActionListener, ListSelectionListener {

        private final PhpProject project;
        private final JList<BasePathSupport.Item> list;
        private final DefaultListModel<BasePathSupport.Item> listModel;
        private final ListSelectionModel selectionModel;
        private final ButtonModel addFolder;
        private final ButtonModel remove;
        private final ButtonModel moveUp;
        private final ButtonModel moveDown;
        private final FileChooserDirectoryHandler directoryHandler;

        private EditMediator(JList<BasePathSupport.Item> list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {
            this(null, list, addFolder, remove, moveUp, moveDown, directoryHandler);
        }

        private EditMediator(PhpProject project, JList<BasePathSupport.Item> list, ButtonModel addFolder, ButtonModel remove, FileChooserDirectoryHandler directoryHandler) {
            this(project, list, addFolder, remove, null, null, directoryHandler);
        }

        private EditMediator(PhpProject project, JList<BasePathSupport.Item> list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {
            assert directoryHandler != null;

            this.list = list;
            if (!(list.getModel() instanceof DefaultListModel)) {
                throw new IllegalArgumentException("The list's model has to be of class DefaultListModel");
            }

            this.listModel = (DefaultListModel<BasePathSupport.Item>) list.getModel();
            this.selectionModel = list.getSelectionModel();

            this.addFolder = addFolder;
            this.remove = remove;
            this.moveUp = moveUp;
            this.moveDown = moveDown;

            this.project = project;
            this.directoryHandler = directoryHandler;
        }

        public static void register(PhpProject project, JList<BasePathSupport.Item> list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {

            EditMediator em = new EditMediator(project, list, addFolder, remove, moveUp, moveDown, directoryHandler);

            // Register the listener on all buttons
            addFolder.addActionListener(em);
            remove.addActionListener(em);
            moveUp.addActionListener(em);
            moveDown.addActionListener(em);
            // On list selection
            em.selectionModel.addListSelectionListener(em);
            // Set the initial state of the buttons
            em.valueChanged(null);
        }

        public static void register(PhpProject project, JList<BasePathSupport.Item> list, ButtonModel addFolder,
                ButtonModel remove, FileChooserDirectoryHandler directoryHandler) {

            EditMediator em = new EditMediator(project, list, addFolder, remove, directoryHandler);

            // Register the listener on all buttons
            addFolder.addActionListener(em);
            remove.addActionListener(em);
            // On list selection
            em.selectionModel.addListSelectionListener(em);
            // Set the initial state of the buttons
            em.valueChanged(null);
        }

        // for global include path (no project available)
        public static void register(JList<BasePathSupport.Item> list, ButtonModel addFolder,
                ButtonModel remove, ButtonModel moveUp, ButtonModel moveDown,
                FileChooserDirectoryHandler directoryHandler) {

            EditMediator em = new EditMediator(list, addFolder, remove, moveUp, moveDown, directoryHandler);

            // Register the listener on all buttons
            addFolder.addActionListener(em);
            remove.addActionListener(em);
            moveUp.addActionListener(em);
            moveDown.addActionListener(em);
            // On list selection
            em.selectionModel.addListSelectionListener(em);
            // Set the initial state of the buttons
            em.valueChanged(null);
        }

        /**
         * Handles button events.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            Object source = e.getSource();
            if (source == addFolder) {
                addFolders();
            } else if (source == remove) {
                int[] newSelection = PathUiSupport.remove(listModel, list.getSelectedIndices());
                list.setSelectedIndices(newSelection);
            } else if (moveUp != null && source == moveUp) {
                int[] newSelection = PathUiSupport.moveUp(listModel, list.getSelectedIndices());
                list.setSelectedIndices(newSelection);
            } else if (moveDown != null && source == moveDown) {
                int[] newSelection = PathUiSupport.moveDown(listModel, list.getSelectedIndices());
                list.setSelectedIndices(newSelection);
            }
        }

        /**
         * Handles changes in the selection.
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            // addFolder allways enabled
            remove.setEnabled(selectionModel.getMinSelectionIndex() != -1);
            if (moveUp != null) {
                moveUp.setEnabled(PathUiSupport.canMoveUp(selectionModel));
            }
            if (moveDown != null) {
                moveDown.setEnabled(PathUiSupport.canMoveDown(selectionModel, listModel.getSize()));
            }
        }

        private void addFolders() {
            FileChooserBuilder builder = new FileChooserBuilder(directoryHandler.getDirKey())
                    .setDirectoriesOnly(true)
                    .setTitle(NbBundle.getMessage(PathUiSupport.class, "LBL_AddFolders_DialogTitle"));
            File currentDirectory = directoryHandler.getCurrentDirectory();
            if (currentDirectory != null) {
                builder.forceUseOfDefaultWorkingDirectory(true)
                        .setDefaultWorkingDirectory(currentDirectory);
            }
            File[] selectedFiles = builder.showMultiOpenDialog();
            if (selectedFiles != null
                    && selectedFiles.length > 0) {
                String[] paths = new String[selectedFiles.length];
                for (int i = 0; i < selectedFiles.length; i++) {
                    paths[i] = selectedFiles[i].getAbsolutePath();
                }
                int[] newSelection = PathUiSupport.addFolders(listModel, list.getSelectedIndices(), paths);
                list.setSelectedIndices(newSelection);
            }
        }

        public interface FileChooserDirectoryHandler {
            String getDirKey();
            File getCurrentDirectory();
        }

    }
}
