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

package org.netbeans.lib.profiler.ui.components;

import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.util.Objects;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

/**
 *
 * @author Jiri Sedlacek
 */
public class LazyComboBox<E> extends JComboBox<E> {
        
    public LazyComboBox(Populator<E> populator) {
        super(new LazyComboBoxModel<E>(populator));
        setPrototypeDisplayValue(populator.initial());
    }


    public final void setModel(ComboBoxModel<E> model) {
        if (!(model instanceof LazyComboBoxModel))
            throw new IllegalArgumentException("Can only use private LazyComboBoxModel"); // NOI18N
        super.setModel(model);
    }

    private LazyComboBoxModel<E> _getModel() {
        return (LazyComboBoxModel<E>)super.getModel();
    }

    public final int getSelectedIndex() {
        return _getModel().isPopulated() ? super.getSelectedIndex() : -1;
    }
    
    public final void setSelectedItem(Object newSelected) {
        LazyComboBoxModel m = _getModel();
        Object oldSelected = m.getSelectedItem();
        
        m.setSelectedItem(newSelected);
        
        if (!Objects.equals(oldSelected, newSelected)) {
            repaint();
            selectionChanged();
        }
    }
    
    protected void fireItemStateChanged(ItemEvent e) {
        super.fireItemStateChanged(e);
        if (e.getStateChange() == ItemEvent.SELECTED) selectionChanged();
    }
    
    // Override this method to get notifications about selection changes
    protected void selectionChanged() {}
    
    public final void resetModel() {
        LazyComboBoxModel m = _getModel();
        Object oldSelected = m.getSelectedItem();
        
        m.reset(true);
        
        if (!Objects.equals(oldSelected, m.getSelectedItem())) {
            repaint();
            selectionChanged();
        }
    }
    
    public final Dimension getPreferredSize() {
        Dimension d;
        if (getPrototypeDisplayValue() != null || _getModel().isPopulated()) d = super.getPreferredSize();
        else d = new JComboBox(new Object[] { _getModel().getSelectedItem() }).getPreferredSize();
        if (getPreferredWidth() >= 0) d.width = Math.max(d.width, getPreferredWidth());
        return d;
    }
    
    public final Dimension getMinimumSize() {
        Dimension d = getPreferredSize();
        if (getPreferredWidth() < 0) d.width = 0;
        else d.width = Math.max(0, getPreferredWidth());
        return d;
    }
    
    public final Dimension getMaximumSize() {
        Dimension d = getPreferredSize();
        if (getPreferredWidth() < 0) d.width = Integer.MAX_VALUE;
        else d.width = Math.max(d.width, getPreferredWidth());
        return d;
    }
    
    // Override this method to define preferred width of the component
    protected int getPreferredWidth() { return -1; }


    public abstract static class Populator<E> {
        
        protected abstract E initial();

        protected abstract E[] populate();

    }

    private static class LazyComboBoxModel<E> extends AbstractListModel<E> implements ComboBoxModel<E> {

        private E[] items;
        private E selected;

        private final Populator<E> populator;


        LazyComboBoxModel(Populator<E> populator) {
            this.populator = populator;
            this.selected = populator.initial();
        }


        public void setSelectedItem(Object selected) { this.selected = (E)selected; }

        public E getSelectedItem() { return selected; }

        public int getSize() { return getItems().length; }

        public E getElementAt(int index) { return getItems()[index]; }


        boolean isPopulated() { return items != null; }

        private E[] getItems() {
            if (items == null) {
                items = populator.populate();
                if (items == null) throw new UnsupportedOperationException("Populator returned null"); // NOI18N
                fireContentsChanged(this, -1, -1);
            }
            return items;
        }
        
        void reset(boolean selection) {
            items = null;
            if (selection && !Objects.equals(selected, populator.initial()) && !containsItem(selected))
                selected = populator.initial();
        }
        
        private boolean containsItem(E item) {
            for (E e : populator.populate())
                if (Objects.equals(e, item)) return true;
            return false;
        }

    }

}
