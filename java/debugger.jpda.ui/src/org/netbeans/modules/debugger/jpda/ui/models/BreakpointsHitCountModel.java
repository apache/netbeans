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
package org.netbeans.modules.debugger.jpda.ui.models;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.JToolTip;
import org.netbeans.api.debugger.LazyActionsManagerListener;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointImpl;
import org.netbeans.modules.debugger.jpda.breakpoints.BreakpointsEngineListener;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.ui.ColumnModelRegistration;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableModelFilter;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.NbBundle;

/**
 * Displays the breakpoints hit count.
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/BreakpointsView", types={ TableModelFilter.class })
public class BreakpointsHitCountModel implements TableModelFilter, BreakpointImpl.HitCountListener {
    
    public static final String HC_COLUMN_ID = "HitCountBreakCount";             // NOI18N
    
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private final ContextProvider lookupProvider;
    private BreakpointsEngineListener breakpointsEngineListener;
    private final Set<BreakpointImpl> bpImplListeningOn = Collections.newSetFromMap(new WeakHashMap<>());
    
    public BreakpointsHitCountModel(ContextProvider lookupProvider) {
        this.lookupProvider = lookupProvider;
    }

    @NbBundle.Messages({"# {0} and {1} represent two numbers.",
                        "# {0} - breakpoint hit count,",
                        "# {1} - break count (how many times the breakpoint actually broke the execution)",
                        "MSG_HitCountWithBreakCount={0} / {1}",
                        "# {0}, {1} and {2} represent numbers:",
                        "# {0} - breakpoint hit count," +
                        "# {1} - break count (how many times the breakpoint actually broke the execution),",
                        "# {2} - remaining number of hits till the execution is broken.",
                        "MSG_HitCountWithBreakCountToBreak={0} / {1}, {2} hits to break",
                        "MSG_HitCountUnknown=?",
                        "# {0} and {1} represent two numbers.",
                        "# {0} - breakpoint hit count,",
                        "# {1} - break count (how many times the breakpoint actually broke the execution)",
                        "TT_HitCount=Current hit count = {0}, actual break count = {1}",
                        "# {0}, {1} and {2} represent numbers:",
                        "# {0} - breakpoint hit count,",
                        "# {1} - break count (how many times the breakpoint actually broke the execution),",
                        "# {2} - remaining number of hits till the execution is broken.",
                        "TT_HitCountToBreak=Current hit count = {0}, actual break count = {1}, number of hits remaining to the next break = {2}",
                        "TT_HitCountUnknown=Unknown"
                       })
    @Override
    public Object getValueAt(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (HC_COLUMN_ID.equals(columnID)) {
            if (node instanceof JPDABreakpoint) {
                JPDABreakpoint b = (JPDABreakpoint) node;
                BreakpointImpl bpImpl = getBPImpl(b);
                if (bpImpl == null) {
                    return Bundle.MSG_HitCountUnknown();
                }
                int hc = bpImpl.getCurrentHitCount();
                int bc = bpImpl.getCurrentBreakCounts();
                boolean isHCFilterSet = b.getHitCountFilter() != 0;
                int hctb = bpImpl.getHitCountsTillBreak();
                if (isHCFilterSet && hctb >= 0) {
                    //return hc2String(hc) + " / " + hc2String(bc) + " / " + hc2String(hctb);
                    return Bundle.MSG_HitCountWithBreakCountToBreak(
                                    hc2String(hc), hc2String(bc), hc2String(hctb));
                } else {
                    if (hc == bc) {
                        return hc2String(hc);
                    } else {
                        //return hc2String(hc) + " / " + hc2String(bc);
                        return Bundle.MSG_HitCountWithBreakCount(
                                        hc2String(hc), hc2String(bc));
                    }
                }
            } else if (node instanceof JToolTip) {
                node = ((JToolTip) node).getClientProperty("getShortDescription");     // NOI18N
                if (node instanceof JPDABreakpoint) {
                    JPDABreakpoint b = (JPDABreakpoint) node;
                    BreakpointImpl bpImpl = getBPImpl(b);
                    if (bpImpl == null) {
                        return Bundle.TT_HitCountUnknown();
                    }
                    int hc = bpImpl.getCurrentHitCount();
                    int bc = bpImpl.getCurrentBreakCounts();
                    int hctb = bpImpl.getHitCountsTillBreak();
                    String tt;
                    if (hctb >= 0) {
                        tt = Bundle.TT_HitCountToBreak(
                                        hc2String(hc), hc2String(bc), hc2String(hctb));
                    } else {
                        tt = Bundle.TT_HitCount(
                                        hc2String(hc), hc2String(bc));
                    }
                    return tt;
                } else {
                    throw new UnknownTypeException(node);
                }
            } else {
                throw new UnknownTypeException(node);
            }
        } else {
            return original.getValueAt(node, columnID);
        }
    }
    
    private BreakpointImpl getBPImpl(JPDABreakpoint b) {
        BreakpointsEngineListener bel = getBreakpointsEngineListener();
        if (bel == null) {
            return null;
        }
        BreakpointImpl bpImpl = bel.getBreakpointImpl(b);
        if (bpImpl != null) {
            synchronized (bpImplListeningOn) {
                if (!bpImplListeningOn.contains(bpImpl)) {
                    bpImpl.addHitCountListener(this);
                    bpImplListeningOn.add(bpImpl);
                }
            }
        }
        return bpImpl;
    }
    
    private static final String hc2String(int hc) {
        return (hc >= 0) ? Integer.toString(hc) : "?";
    }

    @Override
    public void setValueAt(TableModel original, Object node, String columnID, Object value) throws UnknownTypeException {
        original.setValueAt(node, columnID, value);
    }

    @Override
    public boolean isReadOnly(TableModel original, Object node, String columnID) throws UnknownTypeException {
        if (HC_COLUMN_ID.equals(columnID)) {
            return true;
        } else {
            return original.isReadOnly(node, columnID);
        }
    }
    
    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private void fireTableModelEvent(Object node) {
        ModelEvent me = new ModelEvent.TableValueChanged(this, node, HC_COLUMN_ID);
        for (ModelListener l : listeners) {
            l.modelChanged(me);
        }
    }
    
    private BreakpointsEngineListener getBreakpointsEngineListener() {
        synchronized (this) {
            if (breakpointsEngineListener != null) {
                return breakpointsEngineListener;
            }
        }
        List<? extends LazyActionsManagerListener> lamls = lookupProvider.lookup(null, LazyActionsManagerListener.class);
        for (LazyActionsManagerListener laml : lamls) {
            if (laml instanceof BreakpointsEngineListener) {
                BreakpointsEngineListener bel = (BreakpointsEngineListener) laml;
                synchronized (this) {
                    if (breakpointsEngineListener == null) {
                        breakpointsEngineListener = bel;
                    }
                }
                return bel;
            }
        }
        return null;
    }

    @Override
    public void hitCountChanged(JPDABreakpoint bp) {
        fireTableModelEvent(bp);
    }
    
    
    @NbBundle.Messages({ "TTL_HC_Column=Hit Count",
                         "TTL_HC_Column_Tooltip=<Current hit count>/<Actual Break Count>/<Number of Hits remaining to the next break>" })
    @ColumnModelRegistration(path="netbeans-JPDASession/BreakpointsView")
    public static final class HCColumn extends ColumnModel {
        
        private final Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views").getProperties(HC_COLUMN_ID);

        @Override
        public String getID() {
            return HC_COLUMN_ID;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getMessage(BreakpointsHitCountModel.class, "TTL_HC_Column");
        }

        @Override
        public String getShortDescription() {
            return NbBundle.getMessage(BreakpointsHitCountModel.class, "TTL_HC_Column_Tooltip");
        }

        @Override
        public int getColumnWidth() {
            return properties.getInt("columnWidth", 80);
        }

        @Override
        public void setColumnWidth(int newColumnWidth) {
            properties.setInt("columnWidth", newColumnWidth);
        }

        @Override
        public int getCurrentOrderNumber() {
            return properties.getInt("currentOrderNumber", super.getCurrentOrderNumber());
        }

        @Override
        public void setCurrentOrderNumber(int newOrderNumber) {
            properties.setInt("currentOrderNumber", newOrderNumber);
        }

        @Override
        public boolean isSortable() {
            return true;
        }

        @Override
        public boolean isSorted() {
            return properties.getBoolean("sorted", super.isSorted());
        }

        @Override
        public boolean isSortedDescending() {
            return properties.getBoolean("sortedDescending", super.isSortedDescending());
        }

        @Override
        public void setSorted(boolean sorted) {
            properties.setBoolean("sorted", sorted);
        }

        @Override
        public void setSortedDescending(boolean sortedDescending) {
            properties.setBoolean("sortedDescending", sortedDescending);
        }

        @Override
        public boolean isVisible() {
            return properties.getBoolean("visible", false);
        }

        @Override
        public void setVisible(boolean visible) {
            properties.setBoolean("visible", visible);
        }
        
        @Override
        public Class getType() {
            return String.class;
        }
        
    }
}
