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

package org.netbeans.modules.bugtracking.ui.repository;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Marian Petras
 */
public class RepositoryComboModelTest {

    public RepositoryComboModelTest() {
    }

    @Test
    public void testNewModel() {
        RepositoryComboModel model = new RepositoryComboModel();
        assertTrue(model.isEmpty());
        assertTrue(model.getSize() == 0);
        assertNull(model.getSelectedItem());
    }

    @Test
    public void testSetData() {
        RepositoryComboModel model = new RepositoryComboModel();

        Object[] data = new Object[] {"a", "b", "c"};

        model.setData(data);
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == data.length);
        for (int i = 0; i < data.length; i++) {
            assertTrue(model.getElementAt(i) == data[i]);
        }
        assertTrue(model.getSelectedItem() == data[0]);
    }

    @Test
    public void testSetDataSelectionUnchanged() {
        RepositoryComboModel model = new RepositoryComboModel();

        Object[] data = new Object[] {"a", "b", "c"};
        Object[] newData = new Object[] {"b", "c", "d", "e"};

        /*
         * if possible, retain the selection:
         */
        model.setData(data);
        model.setSelectedItem("b");
        model.setData(newData);
        assertTrue(model.getSelectedItem() == "b");

        /*
         * if not possible and the selection has not been cleared,
         * select the first item:
         */
        model.setData(data);
        model.setSelectedItem("a");
        model.setData(newData);
        assertTrue(model.getSelectedItem() == "b");

        /*
         * if setting data after empty data and the selection has NOT been cleared,
         * select the first item:
         */
        model.setData(data);
        model.setSelectedItem("c");
        assertTrue(model.getSelectedItem() == "c");
        model.setData(null);
        model.setData(newData);
        assertTrue(model.getSelectedItem() == "b");

        /*
         * if setting data after empty data and the selection has been cleared,
         * leave the selection empty:
         */
        model.setData(data);
        model.setSelectedItem("c");
        assertTrue(model.getSelectedItem() == "c");
        model.setData(null);
        model.setSelectedItem(null);        //clear the selection
        model.setData(newData);
        assertTrue(model.getSelectedItem() == null);

        /*
         * again, check that if the selection has been set to non-null,
         * the first item is selected after some data is provided:
         */
        model.setData(null);
        model.setSelectedItem(new Object());
        model.setData(newData);
        assertTrue(model.getSelectedItem() == "b");
    }

    @Test
    public void testCleanData() {
        RepositoryComboModel model = new RepositoryComboModel();

        Object[] data = new Object[] {"a", "b", "c"};
        Object[] emptyData = new Object[0];

        model.setData(data);
        assertFalse(model.isEmpty());

        model.setData(emptyData);
        assertTrue(model.isEmpty());
        assertTrue(model.getSize() == 0);
        assertTrue(model.getSelectedItem() == null);
        assertTrue(model.getElementAt(-1) == null);
        assertTrue(model.getElementAt(0) == null);
        assertTrue(model.getElementAt(1) == null);

        model.setData(data);
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == data.length);
        assertTrue(model.getSelectedItem() == data[0]);
        assertTrue(model.getElementAt(-1) == null);
        assertTrue(model.getElementAt(0) == data[0]);
        assertTrue(model.getElementAt(1) == data[1]);
        assertTrue(model.getElementAt(2) == data[2]);
        assertTrue(model.getElementAt(3) == null);

        model.setData(null);
        assertTrue(model.isEmpty());
        assertTrue(model.getSize() == 0);
        assertTrue(model.getSelectedItem() == null);
    }

    @Test
    public void testSelectedItem() {
        Object[] data = new Object[] {"a", "b", "c"};
        Object strangeItem = "something strange";

        RepositoryComboModel model = new RepositoryComboModel();
        Object lastItem = data[data.length - 1];

        assertTrue(model.getSelectedItem() == null);

        model.setSelectedItem(lastItem);
        assertTrue(model.getSelectedItem() == lastItem);

        model.setSelectedItem(null);
        assertTrue(model.getSelectedItem() == null);

        model.setSelectedItem(strangeItem);
        assertTrue(model.getSelectedItem() == strangeItem);
    }

    @Test
    public void testGetElementOutOfScope() {
        RepositoryComboModel model;
        
        model = new RepositoryComboModel();
        assertNull(model.getElementAt(-2));
        assertNull(model.getElementAt(-1));
        assertNull(model.getElementAt( 0));
        assertNull(model.getElementAt( 1));
        assertNull(model.getElementAt( 2));

        Object[] data = new Object[] {"a", "b", "c"};

        model.setData(data);

        assertNull(model.getElementAt(-2));
        assertNull(model.getElementAt(-1));
        int index = data.length;
        assertNull(model.getElementAt(index++));
        assertNull(model.getElementAt(index++));
    }

    @Test
    public void testAddElement() {
        RepositoryComboModel model = new RepositoryComboModel();
        assertTrue(model.isEmpty());

        model.addElement(null);
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 1);
        assertTrue(model.getElementAt(0) == null);
        assertNull(model.getSelectedItem());
        model.addElement("1");
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 2);
        assertTrue(model.getElementAt(0) == null);
        assertTrue(model.getElementAt(1) == "1");
        assertNull(model.getSelectedItem());

        model.setData(null);
        assertTrue(model.isEmpty());

        model.addElement("a");
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 1);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getSelectedItem() == "a");

        model.addElement("b");
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 2);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == "b");
        assertTrue(model.getSelectedItem() == "a");

        model.setSelectedItem(null);
        model.setData(null);
        assertTrue(model.isEmpty());
        model.addElement("x");
        assertFalse(model.isEmpty());
        assertTrue(model.getElementAt(0) == "x");
        assertTrue(model.getSelectedItem() == null);
    }

    @Test
    public void testInsertElementAt() {
        RepositoryComboModel model = new RepositoryComboModel();

        assertTrue(model.isEmpty());
        assertNull(model.getSelectedItem());

        model.insertElementAt(null, 0);
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 1);
        assertNull(model.getSelectedItem());
        assertTrue(model.getElementAt(0) == null);

        model.insertElementAt("a", 0);
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 2);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == null);
        assertNull(model.getSelectedItem());

        model.insertElementAt("b", 0);
        assertFalse(model.isEmpty());
        assertTrue(model.getSize() == 3);
        assertTrue(model.getElementAt(0) == "b");
        assertTrue(model.getElementAt(1) == "a");
        assertTrue(model.getElementAt(2) == null);
        assertNull(model.getSelectedItem());

        model.setSelectedItem("a");
        assertTrue(model.getSelectedItem() == "a");

        model.insertElementAt("x", 1);
        assertTrue(model.getSize() == 4);
        assertTrue(model.getElementAt(0) == "b");
        assertTrue(model.getElementAt(1) == "x");
        assertTrue(model.getElementAt(2) == "a");
        assertTrue(model.getElementAt(3) == null);
        assertTrue(model.getSelectedItem() == "a");

        model.insertElementAt("z", 4);
        assertTrue(model.getSize() == 5);
        assertTrue(model.getElementAt(0) == "b");
        assertTrue(model.getElementAt(1) == "x");
        assertTrue(model.getElementAt(2) == "a");
        assertTrue(model.getElementAt(3) == null);
        assertTrue(model.getElementAt(4) == "z");
        assertTrue(model.getSelectedItem() == "a");
    }

    @Test
    public void testRemoveElementAt() {
        RepositoryComboModel model = new RepositoryComboModel();

        model.addElement("b");
        model.addElement("x");
        model.addElement("a");
        model.addElement(null);
        model.addElement("z");

        assertTrue(model.getSelectedItem() == "b");
        assertTrue(model.getSize() == 5);

        model.removeElementAt(0);   //"b"
        assertTrue(model.getSize() == 4);
        assertTrue(model.getElementAt(0) == "x");
        assertTrue(model.getElementAt(1) == "a");
        assertTrue(model.getElementAt(2) == null);
        assertTrue(model.getElementAt(3) == "z");
        assertNull(model.getSelectedItem());

        model.removeElementAt(3);   //"z"
        assertTrue(model.getSize() == 3);
        assertTrue(model.getElementAt(0) == "x");
        assertTrue(model.getElementAt(1) == "a");
        assertTrue(model.getElementAt(2) == null);
        assertNull(model.getSelectedItem());

        model.addElement("*");
        model.setSelectedItem("a");

        assertTrue(model.getSize() == 4);
        assertTrue(model.getElementAt(0) == "x");
        assertTrue(model.getElementAt(1) == "a");
        assertTrue(model.getElementAt(2) == null);
        assertTrue(model.getElementAt(3) == "*");
        assertTrue(model.getSelectedItem() == "a");

        model.removeElementAt(2);   //null
        assertTrue(model.getSize() == 3);
        assertTrue(model.getElementAt(0) == "x");
        assertTrue(model.getElementAt(1) == "a");
        assertTrue(model.getElementAt(2) == "*");
        assertTrue(model.getSelectedItem() == "a");

        model.removeElementAt(0);
        model.removeElementAt(0);
        model.removeElementAt(0);
        assertTrue(model.isEmpty());
        assertTrue(model.getSize() == 0);
        assertTrue(model.getSelectedItem() == null);

        model.setData(null);
        try {
            model.removeElementAt(0);
            fail("an exception should be thrown");
        } catch (IllegalStateException ex) {
        } catch (IndexOutOfBoundsException ex) {
        }

        model.setData(new Object[] {"1", "2", "3", "4"});
        try {
            model.removeElementAt(-1);
            fail("an exception should be thrown");
        } catch (IllegalStateException ex) {
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            model.removeElementAt(4);
            fail("an exception should be thrown");
        } catch (IllegalStateException ex) {
        } catch (IndexOutOfBoundsException ex) {
        }
        try {
            model.removeElementAt(5);
            fail("an exception should be thrown");
        } catch (IllegalStateException ex) {
        } catch (IndexOutOfBoundsException ex) {
        }
    }

    @Test
    public void testRemoveElement() {
        RepositoryComboModel model = new RepositoryComboModel();

        model.setData(new Object[] {"a", "b", "c", null, "e", null});
        assertTrue(model.getSize() == 6);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == "b");
        assertTrue(model.getElementAt(2) == "c");
        assertTrue(model.getElementAt(3) == null);
        assertTrue(model.getElementAt(4) == "e");
        assertTrue(model.getElementAt(5) == null);

        model.removeElement(null);
        assertTrue(model.getSize() == 5);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == "b");
        assertTrue(model.getElementAt(2) == "c");
        assertTrue(model.getElementAt(3) == "e");
        assertTrue(model.getElementAt(4) == null);

        model.setSelectedItem("c");

        model.removeElement(null);
        assertTrue(model.getSize() == 4);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == "b");
        assertTrue(model.getElementAt(2) == "c");
        assertTrue(model.getElementAt(3) == "e");

        model.removeElement(null);
        assertTrue(model.getSize() == 4);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == "b");
        assertTrue(model.getElementAt(2) == "c");
        assertTrue(model.getElementAt(3) == "e");

        model.removeElement("x");
        assertTrue(model.getSize() == 4);
        assertTrue(model.getElementAt(0) == "a");
        assertTrue(model.getElementAt(1) == "b");
        assertTrue(model.getElementAt(2) == "c");
        assertTrue(model.getElementAt(3) == "e");

        assertTrue(model.getSelectedItem() == "c");

        model.removeElement("e");
        model.removeElement("a");
        model.removeElement("c");
        model.removeElement("b");
        assertTrue(model.isEmpty());
        assertTrue(model.getSelectedItem() == null);

        model.setData(null);
        try {
            model.removeElement(null);
        } catch (Exception ex) {
            fail("no exception should be thrown");
        }
        try {
            model.removeElement("x");
        } catch (Exception ex) {
            fail("no exception should be thrown");
        }

    }

}
