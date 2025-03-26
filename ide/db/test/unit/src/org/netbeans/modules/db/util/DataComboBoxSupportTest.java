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
            this.items = (Object[])items.toArray(new Object[0]);
            fireContentsChanged(this, 0, this.items.length);
        }

        private void setItems(List items, Object selectedItem) {
            this.items = (Object[])items.toArray(new Object[0]);
            this.selectedItem = selectedItem;
            fireContentsChanged(this, 0, this.items.length);
        }
    }
}
