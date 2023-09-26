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

package org.netbeans.swing.tabcontrol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent;
import org.openide.util.ChangeSupport;

/**
 * Default implementation of TabDataModel.
 *
 * @author Tim Boudreau
 */
public class DefaultTabDataModel implements TabDataModel {
    /**
     * Utility field holding list of ChangeListeners.
     */
    private transient ArrayList<ComplexListDataListener> listenerList;

    private class L extends ArrayList<TabData> {
        @Override
        public void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
        }
    }

    private L list = new L();

    private final ChangeSupport cs = new ChangeSupport(this);

    private final Object LOCK = new Object();

    /**
     * Creates a new instance of DefaultTabDataModel
     */
    public DefaultTabDataModel() {
    }

    /**
     * Testing constructor
     */
    public DefaultTabDataModel(TabData[] data) {
        list.addAll(Arrays.asList(data));
    }

    @Override
    public java.util.List<TabData> getTabs() {
        return Collections.unmodifiableList(list);
    }

    @Override
    public TabData getTab(int index) {
        return (TabData) list.get(index);
    }

    @Override
    public void setTabs(TabData[] data) {

        TabData[] oldContents = new TabData[list.size()];
        oldContents = (TabData[]) list.toArray(oldContents);
        
        //No change, Peter says this will be the typical case
        if (Arrays.equals(data, oldContents)) {
            return;
        }

        list.clear();
        list.addAll(Arrays.asList(data));

        VeryComplexListDataEvent vclde = new VeryComplexListDataEvent(this,
                                                                      oldContents,
                                                                      data);
        fireIndicesChanged(vclde);
    }

    @Override
    public void setIcon(int index, Icon i) {
        boolean[] widthChanged = new boolean[]{false};

        boolean fireChange = _setIcon(index, i, widthChanged);
        if (fireChange) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 index, index,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    private boolean _setIcon(int index, Icon i, final boolean[] widthChanged) {
        if (i == null) {
            i = TabData.NO_ICON;
        }
        TabData data = getTab(index);
        if (i != data.getIcon()) {
            widthChanged[0] = data.getIcon().getIconWidth()
                    != i.getIconWidth();
            data.icon = i;
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Adjust the tooltip at given index.
     * @param index
     * @param tooltip 
     * @since 1.49
     */
    public void setToolTipTextAt(int index, String tooltip) {
        TabData data = getTab(index);
        data.tip = tooltip;
    }

    @Override
    public void setText(int index, String txt) {
        boolean[] widthChanged = new boolean[]{false};
        boolean fireChange = _setText(index, txt, widthChanged);
        if (fireChange) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 index, index,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    private int[] _setText(int[] indices, String[] txt,
                           final boolean[] widthChanged) {
        widthChanged[0] = false;
        boolean fireChange = false;
        boolean[] changed = new boolean[indices.length];
        int changedCount = 0;
        Arrays.fill(changed, false);
        for (int i = 0; i < indices.length; i++) {
            boolean[] currWidthChanged = new boolean[]{false};
            fireChange |=
                    _setText(indices[i], txt[i], currWidthChanged);
            widthChanged[0] |= currWidthChanged[0];
            if (currWidthChanged[0])
                changedCount++;
            changed[i] = currWidthChanged[0];
        }
        int[] toFire;
        if (widthChanged[0] || fireChange) {
            if (changedCount == indices.length) {
                toFire = indices;
            } else {
                toFire = new int[changedCount];
                int idx = 0;
                for (int i = 0; i < indices.length; i++) {
                    if (changed[i]) {
                        toFire[idx] = indices[i];
                        idx++;
                    }
                }
            }
            return toFire;
        }
        return null;
    }

    private int[] _setIcon(int[] indices, Icon[] icons,
                           final boolean[] widthChanged) {
        widthChanged[0] = false;
        boolean fireChange = false;
        boolean[] changed = new boolean[indices.length];
        int changedCount = 0;
        Arrays.fill(changed, false);
        boolean[] currWidthChanged = new boolean[]{false};
        boolean currChanged = false;
        for (int i = 0; i < indices.length; i++) {
            currChanged =
                    _setIcon(indices[i], icons[i], currWidthChanged);
            fireChange |= currChanged;
            widthChanged[0] |= currWidthChanged[0];
            if (currChanged)
                changedCount++;
            changed[i] = currChanged;
        }
        int[] toFire;
        if (widthChanged[0] || fireChange) {
            if (changedCount == indices.length) {
                toFire = indices;
            } else {
                toFire = new int[changedCount];
                int idx = 0;
                for (int i = 0; i < indices.length; i++) {
                    if (changed[i]) {
                        toFire[idx] = indices[i];
                        idx++;
                    }
                }
            }
            return toFire;
        }
        return null;
    }

    @Override
    public void setIconsAndText(int[] indices, String[] txt, Icon[] icons) {
        boolean[] iconWidthsChanged = new boolean[]{false};
        boolean[] txtWidthsChanged = new boolean[]{false};
        int[] iconsToFire = _setIcon(indices, icons, iconWidthsChanged);
        int[] txtToFire = _setText(indices, txt, txtWidthsChanged);
        boolean widthChanged = iconWidthsChanged[0] || txtWidthsChanged[0];
        boolean fire = widthChanged || iconsToFire != null
                || txtToFire != null;
        if (fire) {
            if ((indices == iconsToFire) && (indices == txtToFire)) {
                //if all icons/txt changed, optimize and don't calculate a merge
                ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                     ComplexListDataEvent.CONTENTS_CHANGED,
                                                                     indices,
                                                                     widthChanged);
                fireContentsChanged(clde);
            } else {
                //okay, there are differences in what was set to what.  Build a 
                //merge of the change data and fire that
                int size = (iconsToFire != null ? iconsToFire.length : 0)
                        + (txtToFire != null ? txtToFire.length : 0);
                Set<Integer> allIndicesToFire = new HashSet<Integer>(size);
                Integer[] o;
                if (iconsToFire != null) {
                    o = toObjectArray(iconsToFire);
                    allIndicesToFire.addAll(Arrays.asList(o));
                }
                if (txtToFire != null) {
                    o = toObjectArray(txtToFire);
                    allIndicesToFire.addAll(Arrays.asList(o));
                }
                Integer[] all = new Integer[allIndicesToFire.size()];
                all = (Integer[]) allIndicesToFire.toArray(all);
                int[] allPrimitive = toPrimitiveArray(all);
                ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                     ComplexListDataEvent.CONTENTS_CHANGED,
                                                                     allPrimitive,
                                                                     widthChanged);
                fireContentsChanged(clde);
            }
        }
    }

    @Override
    public void setIcon(int[] indices, Icon[] icons) {
        boolean[] widthChanged = new boolean[]{false};
        int[] toFire = _setIcon(indices, icons, widthChanged);
        if (toFire != null) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 toFire,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    @Override
    public void setText(int[] indices, String[] txt) {
        boolean[] widthChanged = new boolean[]{false};
        int[] toFire = _setText(indices, txt, widthChanged);
        if (toFire != null) {
            ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                                 ComplexListDataEvent.CONTENTS_CHANGED,
                                                                 toFire,
                                                                 widthChanged[0]);
            fireContentsChanged(clde);
        }
    }

    private boolean _setText(int index, String txt,
                             final boolean[] widthChanged) {
        TabData data = getTab(index);
        if (txt != data.txt) {
            widthChanged[0] = data.getText() != txt;
            data.txt = txt;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void setTab(int index, TabData data) {
        if (!data.equals(getTab(index))) {
            TabData olddata = getTab(index);
            boolean txtChg = data.getText().equals(olddata.getText());
            boolean compChg = data.getUserObject() != olddata.getUserObject();
            list.set(index, data);
            ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                                ListDataEvent.CONTENTS_CHANGED,
                                                                index, index,
                                                                txtChg,
                                                                compChg);
            lde.setAffectedItems(new TabData[]{data});
            fireContentsChanged(lde);
        }
    }

    @Override
    public void addTab(int index, TabData data) {
        list.add(index, data);
        ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                            ComplexListDataEvent.INTERVAL_ADDED,
                                                            index, index, true);
        lde.setAffectedItems(new TabData[]{data});
        fireIntervalAdded(lde);
    }

    @Override
    public void addTabs(int start, TabData[] data) {
        list.addAll(start, Arrays.asList(data));
        ComplexListDataEvent lde = new ComplexListDataEvent(this, ListDataEvent.INTERVAL_ADDED, start, start
                                                                                                       + data.length
                                                                                                       - 1, true);
        lde.setAffectedItems(data);
        fireIntervalAdded(lde);
    }

    @Override
    public void removeTab(int index) {
        TabData[] td = new TabData[]{(TabData) list.get(index)};
        list.remove(index);
        ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                            ListDataEvent.INTERVAL_REMOVED,
                                                            index, index);
        lde.setAffectedItems(td);
        fireIntervalRemoved(lde);
    }

    /**
     * Remove a range of tabs from <code>start</code> up to <i>and including</i> 
     * <code>finish</code>.
     */
    @Override
    public void removeTabs(int start, int end) {
        List<TabData> affected = new ArrayList<>(list.subList(start, end));
        if (start == end) {
            list.remove(start);
        } else {
            list.removeRange(start, end);
        }
        ComplexListDataEvent lde = new ComplexListDataEvent(this,
                                                            ListDataEvent.INTERVAL_REMOVED,
                                                            start, end);
        lde.setAffectedItems(affected.toArray(new TabData[0]));
        fireIntervalRemoved(lde);
    }

    @Override
    public void addTabs(int[] indices, TabData[] data) {
        Map<Integer,TabData> m = new HashMap<>(data.length);
        for (int i = 0; i < data.length; i++) {
            m.put(new Integer(indices[i]), data[i]);
        }
        Arrays.sort(indices);
        for (int i = 0; i < indices.length; i++) {
            Integer key = new Integer(indices[i]);
            TabData currData = m.get(key);
            list.add(indices[i], currData);
        }
        ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                             ComplexListDataEvent.ITEMS_ADDED,
                                                             indices, true);
        clde.setAffectedItems(data);
        fireIndicesAdded(clde);
    }

    @Override
    public void removeTabs(int[] indices) {
        Arrays.sort(indices);
        TabData[] affected = new TabData[indices.length];
        for (int i = indices.length - 1; i >= 0; i--) {
            affected[i] = (TabData) list.remove(indices[i]);
        }
        ComplexListDataEvent clde = new ComplexListDataEvent(this,
                                                             ComplexListDataEvent.ITEMS_REMOVED,
                                                             indices, true);
        clde.setAffectedItems(affected);
        fireIndicesRemoved(clde);
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public synchronized void addComplexListDataListener(
            ComplexListDataListener listener) {
        synchronized( LOCK ) {
            if (listenerList == null) {
                listenerList = new ArrayList<ComplexListDataListener>();
            }
            listenerList.add(listener);
        }
    }

    @Override
    public synchronized void removeComplexListDataListener(
            ComplexListDataListener listener) {
        synchronized( LOCK ) {
            listenerList.remove(listener);
        }
    }

    private void fireIntervalAdded(ListDataEvent event) {
        List<ComplexListDataListener> listeners = null;
        synchronized( LOCK ) {
            if (listenerList == null) {
                return;
            }
            listeners = new ArrayList<ComplexListDataListener>(listenerList);
        }
        for( ComplexListDataListener l : listeners ) {
            l.intervalAdded(event);
        }
        cs.fireChange();
    }

    private void fireIntervalRemoved(ListDataEvent event) {
        List<ComplexListDataListener> listeners = null;
        synchronized( LOCK ) {
            if (listenerList == null) {
                return;
            }
            listeners = new ArrayList<ComplexListDataListener>(listenerList);
        }
        for( ComplexListDataListener l : listeners ) {
            l.intervalRemoved(event);
        }
        cs.fireChange();
    }

    private void fireContentsChanged(ListDataEvent event) {
        List<ComplexListDataListener> listeners = null;
        synchronized( LOCK ) {
            if (listenerList == null) {
                return;
            }
            listeners = new ArrayList<ComplexListDataListener>(listenerList);
        }
        for( ComplexListDataListener l : listeners ) {
            l.contentsChanged(event);
        }
        cs.fireChange();
    }

    private void fireIndicesAdded(ComplexListDataEvent event) {
        List<ComplexListDataListener> listeners = null;
        synchronized( LOCK ) {
            if (listenerList == null) {
                return;
            }
            listeners = new ArrayList<ComplexListDataListener>(listenerList);
        }
        for( ComplexListDataListener l : listeners ) {
            l.indicesAdded(event);
        }
        cs.fireChange();
    }

    private void fireIndicesRemoved(ComplexListDataEvent event) {
        List<ComplexListDataListener> listeners = null;
        synchronized( LOCK ) {
            if (listenerList == null) {
                return;
            }
            listeners = new ArrayList<ComplexListDataListener>(listenerList);
        }
        for( ComplexListDataListener l : listeners ) {
            l.indicesRemoved(event);
        }
        cs.fireChange();
    }

    private void fireIndicesChanged(ComplexListDataEvent event) {
        List<ComplexListDataListener> listeners = null;
        synchronized( LOCK ) {
            if (listenerList == null) {
                return;
            }
            listeners = new ArrayList<ComplexListDataListener>(listenerList);
        }
        for( ComplexListDataListener l : listeners ) {
            l.indicesChanged(event);
        }
        cs.fireChange();
    }

    @Override
    public String toString() {
        StringBuffer out = new StringBuffer(getClass().getName());
        out.append(" size =");
        int max = size();
        out.append(max);
        out.append(" - ");
        for (int i = 0; i < max; i++) {
            TabData td = getTab(i);
            out.append(td.toString());
            if (i != max - 1) {
                out.append(',');
            }
        }
        return out.toString();
    }
    
    //===========================
    //XXX remove ChangeListener support and handle the ComplexNNN events so nothing is repainted
    //if not displayed on screen!
    
    
    /**
     * Registers ChangeListener to receive events.
     *
     * @param listener The listener to register.
     */
    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    /**
     * Removes ChangeListener from the list of listeners.
     *
     * @param listener The listener to remove.
     */
    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public int indexOf(TabData td) {
        return list.indexOf(td);
    }

    private Integer[] toObjectArray(int[] o) {
        Integer[] result = new Integer[o.length];
        for (int i = 0; i < o.length; i++) {
            result[i] = new Integer(o[i]);
        }
        return result;
    }

    private int[] toPrimitiveArray(Integer[] o) {
        int[] result = new int[o.length];
        for (int i = 0; i < o.length; i++) {
            result[i] = o[i].intValue();
        }
        return result;
    }

}
