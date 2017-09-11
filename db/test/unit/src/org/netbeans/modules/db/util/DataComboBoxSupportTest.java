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

package org.netbeans.modules.db.util;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.ListModel;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Andrei Badea
 */
public class DataComboBoxSupportTest extends NbTestCase {

    public DataComboBoxSupportTest(String testName) {
        super(testName);
    }

    public boolean runInEQ() {
        return true;
    }

    public void testBasic() {
        JComboBox comboBox = new JComboBox();
        ListModelImpl listModel = new ListModelImpl();
        DataModelImpl dataModel = new DataModelImpl(listModel);
        DataComboBoxSupport support = new DataComboBoxSupport(comboBox, dataModel, true);

        assertSame(support.NEW_ITEM, comboBox.getItemAt(0));
        assertEquals("Add", comboBox.getItemAt(0).toString());
        assertEquals(-1, comboBox.getSelectedIndex());

        List items = new ArrayList();
        items.add("foo");
        items.add("bar");
        listModel.setItems(items);

        assertEquals("foo", comboBox.getItemAt(0));
        assertEquals("bar", comboBox.getItemAt(1));
        assertEquals("Add", comboBox.getItemAt(2).toString());
        assertEquals("The old selected item was removed, nothing should be selected now", -1, comboBox.getSelectedIndex());

        comboBox.setSelectedIndex(1); // bar
        items.remove("foo");
        listModel.setItems(items);

        assertEquals("bar", comboBox.getItemAt(0));
        assertEquals("Add", comboBox.getItemAt(1).toString());
        assertEquals("Bar should still be selected", 0, comboBox.getSelectedIndex());

        items.add("new");
        listModel.setItems(items, "new");

        assertEquals("bar", comboBox.getItemAt(0));
        assertEquals("new", comboBox.getItemAt(1));
        assertEquals("Add", comboBox.getItemAt(2).toString());
        assertEquals("new", comboBox.getSelectedItem());
        assertEquals("New should be selected", 1, comboBox.getSelectedIndex());
    }

    public void testNoAddition() {
        JComboBox comboBox = new JComboBox();
        ListModelImpl listModel = new ListModelImpl();
        DataModelImpl dataModel = new DataModelImpl(listModel);
        DataComboBoxSupport support = new DataComboBoxSupport(comboBox, dataModel, false);

        assertEquals(0, comboBox.getItemCount());

        List items = new ArrayList();
        items.add("foo");
        items.add("bar");
        listModel.setItems(items);

        assertEquals(2, comboBox.getItemCount());
        assertEquals("foo", comboBox.getItemAt(0));
        assertEquals("bar", comboBox.getItemAt(1));
    }

    private static final class DataModelImpl implements DataComboBoxModel {

        private ComboBoxModel listModel;

        public DataModelImpl(ComboBoxModel listModel) {
            this.listModel = listModel;
        }

        public String getItemTooltipText(Object item) {
            return null;
        }

        public String getItemDisplayName(Object item) {
            return (String)item + "-display";
        }

        public void newItemActionPerformed() {
            System.out.println("action performed");
        }

        public String getNewItemDisplayName() {
            return "Add";
        }

        public ComboBoxModel getListModel() {
            return listModel;
        }
    }

    private static final class ListModelImpl extends AbstractListModel implements ComboBoxModel {

        Object[] items = new Object[0];
        Object selectedItem;

        public Object getElementAt(int index) {
            return items[index];
        }

        public int getSize() {
            return items.length;
        }

        public Object getSelectedItem() {
            return selectedItem;
        }

        public void setSelectedItem(Object selectedItem) {
            this.selectedItem = selectedItem;
        }

        private void setItems(List items) {
            this.items = (Object[])items.toArray(new Object[items.size()]);
            fireContentsChanged(this, 0, this.items.length);
        }

        private void setItems(List items, Object selectedItem) {
            this.items = (Object[])items.toArray(new Object[items.size()]);
            this.selectedItem = selectedItem;
            fireContentsChanged(this, 0, this.items.length);
        }
    }
}
