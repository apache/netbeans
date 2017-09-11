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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.ui.repository;

import javax.swing.AbstractListModel;
import javax.swing.MutableComboBoxModel;

/**
 * A custom combo-box model whose main feature is method
 * {@code setData(Object[])}. This method retains selection if the data is
 * changed, if possible. Methods {@code addElement()},
 * {@code insertElementAt()}, {@code removeElement()} and
 * {@code removeElementAt()} retain the selection, too.
 * <p>
 * The main motivation for creating this model was a need to work-around a bug
 * that {@code ItemListener}s are not notified of item selection change
 * if a model of a combo-box is changed. Method {@link #setData(Object[])}
 * allows to work-around this bug by changing the model's data instead of
 * changing the model itself. The ability to retain item selection is just
 * an extra (but welcome) bonus.
 *
 * @author Marian Petras
 */
final class RepositoryComboModel extends AbstractListModel implements MutableComboBoxModel {

    private Object[] data;
    private Object selectedItem;

    /**
     * Holds information whether the last invocation (if any) of
     * {@code setSelectedItem(Object)} was passed {@code null}.
     * Value of this field has impact on behavior of method {@link #setData}.
     * 
     * @see  #setSelectedItem
     */
    private boolean clearSelection = false;

    /**
     * Creates an empty model.
     */
    RepositoryComboModel() {
        data = null;
    }

    /**
     * Changes all data of this model.
     * <p>
     * The initial selection is given by the following rules:
     * <ul>
     *     <li>If the new array is {@code null} or empty,
     *         then the selected item is reset to {@code null}.</li>
     *     <li>If both the original array and the new array are non-empty,
     *         there has been some item selected in the original array
     *         and the item is also present in the new array, then the
     *         original selection is retained.</li>
     *     <li>If there is no item currently selected because of the last
     *         invocation of method {@link #setSelectedItem} was passed
     *         {@code null} as an argument, then the initial selection will
     *         remain {@code null}.</li>
     *     <li>Otherwise, the first item of the given array becomes the initial
     *         selected item.</li>
     * </ul>
     * 
     * @param  data  new data to be displayed in the combo-box,
     *               or {@code null} to clear the data
     */
    void setData(Object[] data) {
        if ((data != null) && (data.length == 0)) {
            data = null;
        }
        if (data == this.data) {
            return;
        }

        final Object originalSelectedItem = selectedItem;

        final int originalSize = (this.data != null) ? this.data.length : 0;
        final int newSize      = (     data != null) ?      data.length : 0;

        if (data == null) {
            this.data = null;

            selectedItem = null;
        } else {
            this.data = new Object[data.length];
            System.arraycopy(data, 0, this.data, 0, data.length);

            if (clearSelection) {
                selectedItem = null;
            } else if (originalSelectedItem == null) {
                selectedItem = this.data[0];
            } else if (indexOf(originalSelectedItem, data) == -1) {
                selectedItem = this.data[0];
            } else {
                selectedItem = originalSelectedItem;
            }
        }

        fireContentsChanged(this, 0, Math.max(originalSize, newSize));

        assert (this.data == null) || (this.data.length > 0);
    }

    public int getSize() {
        if (data == null) {
            return 0;
        }

        return data.length;
    }

    public Object getElementAt(int index) {
        if (data == null) {
            return null;
        }

        if ((index < 0) || (index >= data.length)) {
            return null;
        }

        return data[index];
    }

    /**
     * {@inheritDoc}
     * The value of the argument also influences behavior of method
     * {@link #setData}.
     *
     * @see  #setData
     */
    public void setSelectedItem(Object item) {
        selectedItem = item;
        clearSelection = (item == null);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public void addElement(Object newObj) {
        assert (data == null) || (data.length > 0);

        if (data == null) {
            data = new Object[] {newObj};
            if (!clearSelection) {
                selectedItem = newObj;
            }
        } else {
            Object[] newData = new Object[data.length + 1];
            System.arraycopy(data, 0, newData, 0, data.length);
            newData[data.length] = newObj;
            data = newData;
        }
        int lastIndex = data.length - 1;
        fireIntervalAdded(this, lastIndex, lastIndex);
    }

    public void insertElementAt(Object obj, int index) {
        final int size = getSize();

        checkIndex(index, size);

        if (index == size) {        //this includes the case that (size == 0)
            addElement(obj);
        } else {
            Object[] newData = new Object[data.length + 1];
            System.arraycopy(data,    index,
                             newData, index + 1,
                             size - index);
            if (index != 0) {
                System.arraycopy(data,    0,
                                 newData, 0,
                                 index);
            }
            newData[index] = obj;
            data = newData;
            fireIntervalAdded(this, index, index);
        }
    }

    public void removeElement(Object obj) {
        int index = indexOf(obj);
        if (index != -1) {
            removeElementAt(index);
        }
    }

    public void removeElementAt(int index) {
        if (isEmpty()) {
            throw new IllegalStateException(
                    "Cannot remove items from an empty model.");        //NOI18N
        }

        final int size = getSize();

        checkIndex(index, size - 1);

        final Object removedItem = data[index];

        if (size == 1) {
            data = null;
        } else {
            Object[] newData = new Object[size - 1];
            if (index != 0) {
                System.arraycopy(data,    0,
                                 newData, 0,
                                 index);
            }
            if (index != (size - 1)) {
                System.arraycopy(data,    index + 1,
                                 newData, index,
                                 size - index - 1);
            }
            data = newData;
        }

        if (removedItem == selectedItem) {
            selectedItem = null;
        }

        fireIntervalRemoved(this, index, index);
    }

    public boolean isEmpty() {
        assert (data == null) || (data.length > 0);
        return (data == null);
    }

    private int indexOf(Object obj) {
        return indexOf(obj, data);
    }

    private static int indexOf(Object obj, Object[] data) {
        if ((data == null) || (data.length == 0)) {
            return -1;
        }

        for (int i = 0; i < data.length; i++) {
            if (data[i] == obj) {
                return i;
            }
        }
        return -1;
    }

    private static void checkIndex(int index, int upperBound) {
        if ((index < 0) || (index > upperBound)) {
            throw new IndexOutOfBoundsException(
               "index: " + index + "; bounds: (0, " + upperBound + ')');//NOI18N
        }
    }

}
