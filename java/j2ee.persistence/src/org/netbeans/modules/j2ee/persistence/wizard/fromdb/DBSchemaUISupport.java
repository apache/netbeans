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

package org.netbeans.modules.j2ee.persistence.wizard.fromdb;

import java.awt.Component;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Andrei Badea
 */
public class DBSchemaUISupport {

    private DBSchemaUISupport() {
    }

    /**
     * Connects a combo box with the list of dbschemas in a project, making
     * the combo box display these dbschemas.
     */
    public static void connect(JComboBox comboBox, DBSchemaFileList dbschemaFileList) {
        comboBox.setModel(new DBSchemaModel(dbschemaFileList));
        comboBox.setRenderer(new DBSchemaRenderer(comboBox));
    }

    /**
     * Model for database schemas. Contains either a list of schemas (FileObject's)
     * or a single "no schemas" item.
     */
    private static final class DBSchemaModel extends AbstractListModel implements ComboBoxModel {

        private final DBSchemaFileList dbschemaFileList;
        private Object selectedItem;

        public DBSchemaModel(DBSchemaFileList dbschemaFileList) {
            this.dbschemaFileList = dbschemaFileList;
        }

        @Override
        public void setSelectedItem(Object anItem) {
            if (!dbschemaFileList.getFileList().isEmpty()) {
                selectedItem = anItem;
            }
        }

        @Override
        public Object getElementAt(int index) {
            List<FileObject> dbschemaFiles = dbschemaFileList.getFileList();
            if (!dbschemaFiles.isEmpty()) {
                return dbschemaFiles.get(index);
            } else {
                return NbBundle.getMessage(DBSchemaUISupport.class, "LBL_NoSchemas");
            }
        }

        @Override
        public int getSize() {
            int dbschemaCount = dbschemaFileList.getFileList().size();
            return dbschemaCount > 0 ? dbschemaCount : 1;
        }

        @Override
        public Object getSelectedItem() {
            return !dbschemaFileList.getFileList().isEmpty() ? selectedItem : NbBundle.getMessage(DBSchemaUISupport.class, "LBL_NoSchemas");
        }
    }

    private static final class DBSchemaRenderer extends DefaultListCellRenderer {

        private JComboBox comboBox;

        public DBSchemaRenderer(JComboBox comboBox) {
            this.comboBox = comboBox;
        }

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName = null;
            ComboBoxModel model = comboBox.getModel();

            if (model instanceof DBSchemaModel && value instanceof FileObject) {
                displayName = ((DBSchemaModel)model).dbschemaFileList.getDisplayName((FileObject)value);
            } else {
                displayName = value;
            }

            return super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
        }
    }
}
