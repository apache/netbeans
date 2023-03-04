/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import static javax.swing.event.ListDataEvent.CONTENTS_CHANGED;

/**
 * Unmodifiable {@code ComboBoxModel} built on a {@link java.util.List}
 * of elements.
 * This implementation is very simple and assumes that the passed list
 * of elements is not empty and is not modified during this model's lifetime.
 *
 * @author  Marian Petras
 */
public final class ListComboBoxModel<E> implements ComboBoxModel<E> {

    private final List<? extends E> elements;
    private final int maxIndex;
    private final boolean reverseOrder;
    private Object selectedItem;
    private Collection<ListDataListener> listeners;
    private ListDataEvent event
                          = new ListDataEvent(this, CONTENTS_CHANGED, -1, -1);

    public ListComboBoxModel(List<E> elements) {
        this(elements, false);
    }

    public ListComboBoxModel(List<? extends E> elements,
                             final boolean reverseOrder) {
        if (elements == null) {
            throw new IllegalArgumentException(
                    "the list of elements must not be null");           //NOI18N
        }
        if (elements.isEmpty()) {
            throw new IllegalArgumentException(
                    "empty list of elements is not allowed");           //NOI18N
        }
        this.elements = elements;
        this.maxIndex = elements.size() - 1;
        this.reverseOrder = reverseOrder;
    }

    @Override
    public void setSelectedItem(Object item) {
        if ((selectedItem != null) && !selectedItem.equals(item)
                || (selectedItem == null) && (item != null)) {
            this.selectedItem = item;
            fireSelectionChange();
        }
    }

    @Override
    public Object getSelectedItem() {
        return selectedItem;
    }

    @Override
    public int getSize() {
        return maxIndex + 1;
    }

    @Override
    public E getElementAt(int index) {
        return elements.get(reverseOrder ? maxIndex - index
                                         : index);
    }

    @Override
    public void addListDataListener(ListDataListener l) {
        if (listeners == null) {
            listeners = new ArrayList<>(3);
            event = new ListDataEvent(this, CONTENTS_CHANGED, -1, -1);
        }
        listeners.add(l);
    }

    @Override
    public void removeListDataListener(ListDataListener l) {
        if ((listeners != null) && listeners.remove(l) && listeners.isEmpty()) {
            listeners = null;
            event = null;
        }
    }

    private void fireSelectionChange() {
        if (listeners == null) {
            return;
        }

        ListDataListener[] arrayListeners = listeners.toArray(new ListDataListener[0]);
        for (ListDataListener l : arrayListeners) {
            l.contentsChanged(event);
        }
    }

}
