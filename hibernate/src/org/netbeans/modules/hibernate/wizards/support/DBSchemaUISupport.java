/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.hibernate.wizards.support;

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
 * @author Andrei Badea, gowri
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

        public void setSelectedItem(Object anItem) {
            if (dbschemaFileList.getFileList().size() > 0) {
                selectedItem = anItem;
            }
        }

        public Object getElementAt(int index) {
            List<FileObject> dbschemaFiles = dbschemaFileList.getFileList();
            if (dbschemaFiles.size() > 0) {
                return dbschemaFiles.get(index);
            } else {
                return NbBundle.getMessage(DBSchemaUISupport.class, "LBL_NoSchemas");
            }
        }

        public int getSize() {
            int dbschemaCount = dbschemaFileList.getFileList().size();
            return dbschemaCount > 0 ? dbschemaCount : 1;
        }

        public Object getSelectedItem() {
            return dbschemaFileList.getFileList().size() > 0 ? selectedItem : NbBundle.getMessage(DBSchemaUISupport.class, "LBL_NoSchemas");
        }
    }

    private static final class DBSchemaRenderer extends DefaultListCellRenderer {

        private JComboBox comboBox;

        public DBSchemaRenderer(JComboBox comboBox) {
            this.comboBox = comboBox;
        }

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
