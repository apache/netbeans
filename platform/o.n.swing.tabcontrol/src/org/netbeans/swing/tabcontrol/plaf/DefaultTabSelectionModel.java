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

package org.netbeans.swing.tabcontrol.plaf;

import java.util.Iterator;
import java.util.Set;
import javax.swing.SingleSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListDataEvent;
import org.netbeans.swing.tabcontrol.TabDataModel;
import org.netbeans.swing.tabcontrol.event.ArrayDiff;
import org.netbeans.swing.tabcontrol.event.ComplexListDataEvent;
import org.netbeans.swing.tabcontrol.event.ComplexListDataListener;
import org.netbeans.swing.tabcontrol.event.VeryComplexListDataEvent;
import org.openide.util.ChangeSupport;

/**
 * Default implementation of tab selection model.  Listens to the supplied data
 * model and updates the selection appropriately on all add/remove events so that
 * the actual selection does not change if items are inserted into the model ahead
 * of the current selection, etc.
 *
 * @author Tim Boudreau
 */
final class DefaultTabSelectionModel implements SingleSelectionModel,
        ComplexListDataListener {
    TabDataModel dataModel;
    int sel = -1;
    private final ChangeSupport cs = new ChangeSupport(this);

    /**
     * Creates a new instance of DefaultTabSelectionModel
     */
    public DefaultTabSelectionModel(TabDataModel tdm) {
        dataModel = tdm;
        attach();
    }

    public void attach() {
        dataModel.addComplexListDataListener(this);
    }

    public void detach() {
        dataModel.removeComplexListDataListener(this);
    }

    public void clearSelection() {
        sel = -1;
        cs.fireChange();
    }

    public int getSelectedIndex() {
        return sel;
    }

    public boolean isSelected() {
        return sel != -1;
    }

    public void setSelectedIndex(int index) {
        if (index != sel) {
            int oldIndex = sel;
            if ((index < -1) || (index >= dataModel.size())) {
                throw new IllegalArgumentException("Selected index set to "
                   + index
                   + " but model size is only " + dataModel.size());
            }
            sel = index;
            cs.fireChange();
        }
    }

    private void adjustSelectionForEvent(ListDataEvent e) {
        if (e.getType() == e.CONTENTS_CHANGED || sel == -1) {
            return;
        }
        int start = e.getIndex0();
        int end = e.getIndex1() + 1;
        if (e.getType() == e.INTERVAL_REMOVED) {
            if (sel < start) {
                return;
            } else {
                if (sel >= start) {
                    if (sel > end) {
                        sel -= end - start;
                    } else {
                        sel = start;
                        if (sel >= dataModel.size()) {
                            sel = dataModel.size() - 1;
                        }
                    }
                    cs.fireChange();
                }
            }
        } else {
            if (sel < start) {
                //not affected, do nothing
                return;
            }
            if (sel >= start) {
                if (end - 1 == start) {
                    sel++;
                } else if (sel < end) {
                    sel = (end + (sel - start)) - 1;
                } else {
                    sel += (end - start) - 1;
                }
                cs.fireChange();
            }
        }
    }

    public void contentsChanged(ListDataEvent e) {
        adjustSelectionForEvent(e);
    }

    public void intervalAdded(ListDataEvent e) {
        adjustSelectionForEvent(e);
    }

    public void intervalRemoved(ListDataEvent e) {
        adjustSelectionForEvent(e);
    }

    public void indicesAdded(ComplexListDataEvent e) {
        if (sel < 0) return;
        int[] indices = e.getIndices();
        java.util.Arrays.sort(indices);
        int offset = 0;
        for (int i = 0; i < indices.length; i++) {
            if (sel >= indices[i]) {
                offset++;
            } else {
                break;
            }
        }
        if (offset > 0) {
            sel += offset;
            cs.fireChange();
        }
    }

    public void indicesRemoved(ComplexListDataEvent e) {
        if (sel < 0) return;
        int[] indices = e.getIndices();
        java.util.Arrays.sort(indices);
        int offset = -1;
        for (int i = 0; i < indices.length; i++) {
            if (sel > indices[i]) {
                offset--;
            } else {
                break;
            }
        }
        if (sel == dataModel.size()) {
            sel -= 1;
            cs.fireChange();
            return;
        }
        if (dataModel.size() == 0) {
            sel = -1;
            cs.fireChange();
        } else if (offset != 0) {
            sel = Math.max( -1, Math.min (sel + offset, -1));
            cs.fireChange();
        }
    }

    public void indicesChanged(ComplexListDataEvent e) {
        if (sel < 0) return;
        if (e instanceof VeryComplexListDataEvent) { //it always will be

            ArrayDiff dif = ((VeryComplexListDataEvent) e).getDiff();

            boolean changed = false;
            
            if (dif == null) {
                //no differences
                return;
            }
            
            //Get the deleted and added indices
            Set<Integer> deleted = dif.getDeletedIndices();
            Set<Integer> added = dif.getAddedIndices();
            
            //create an Integer to compare
            Integer idx = new Integer(getSelectedIndex());
            
            //Don't iterate if everything was closed, we know what to do
            if (dataModel.size() == 0) {
                sel = -1;
                cs.fireChange();
                return;
            }
            
            //Iterate all of the deleted items, and count how many were
            //removed at indices lower than the selection, so we can subtract
            //that from the selected index to keep selection on the same tab
            Iterator<Integer> i = deleted.iterator();
            int offset = 0;
            Integer curr;
            while (i.hasNext()) {
                curr = i.next();
                if (curr.compareTo(idx) <= 0) {
                    offset++;
                }
            }
            
            //Iterate all of the added items, and count how many were added at
            //indices below the selected index, so we can add that to the selected
            //index
            i = added.iterator();
            while (i.hasNext()) {
                curr = i.next();
                if (curr.compareTo(idx) >= 0) {
                    offset--;
                }
            }

            sel -= offset;
            if (sel < 0) {
                //The tab at index 0 was closed, but we always want to show
                //something if we can, so change it to 0 if possible
                sel = dataModel.size() > 0 ? 0 : -1;
            }
            
            //Make sure we're not off the end of the array - we could be if the
            //selection was the last and it and others were removed
            if (sel >= dataModel.size()) {
                sel = dataModel.size() - 1;
            }
            
            if (offset != 0) {
                cs.fireChange();
            }
        }
        //do nothing
    }

    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    public synchronized void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

}
