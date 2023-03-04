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

package org.netbeans.modules.viewmodel;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.spi.viewmodel.CheckNodeModel;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

/**
 * Counted model
 *
 * @author Martin Entlicher
 */
class CountedModel implements TreeModel, ExtendedNodeModel, CheckNodeModel, TableModel {

    private static final String COLUMN_UC = "uppercase";
    private static final String COLUMN_LC = "lowercase";
    private static final String COLUMN_BYTES = "bytes";

    private final String[] rootChildren;
    private final int depth;
    private final Map<Object, Object[]> cachedChildren = new HashMap<Object, Object[]>();
    private final List<ModelListener> listeners = new ArrayList<ModelListener>();
    private final Map<Object, Boolean> selectedNodes = new HashMap<Object, Boolean>();
    private final Map<Object, Map<String, Object>> changedValues = new HashMap<Object, Map<String, Object>>();
    private final Set<CountedCall> countedCalls = new HashSet<CountedCall>();

    public CountedModel(String[] children, int depth) {
        rootChildren = children;
        this.depth = depth;
    }

    public ColumnModel[] createColumns() {
        return new ColumnModel[] {
            new CountedColumn(COLUMN_LC),
            new CountedColumn(COLUMN_UC),
            new CountedColumn(COLUMN_BYTES),
        };
    }

    public Object getRoot() {
        return ROOT;
    }

    public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
        //System.err.println("\n\nget CHILDREN("+this+", on "+parent);
        //System.err.println("  counted calls before = "+getCountedCalls("getChildren", parent)+"\n\n");
        countCall("getChildren", parent);
        //System.err.println("  counted calls after  = "+getCountedCalls("getChildren", parent)+"\n\n");
        if (parent == ROOT) {
            return rootChildren;
        } else {
            Object[] ch = cachedChildren.get(parent);
            if (ch == null) {
                ch = new String[rootChildren.length];
                for (int i = 0; i < ch.length; i++) {
                    ch[i] = parent + "/" + rootChildren[i];
                }
                cachedChildren.put(parent, ch);
            }
            return ch;
        }
    }

    public boolean isLeaf(Object node) throws UnknownTypeException {
        countCall("isLeaf", node);
        String s = (String) node;
        int d = 0;
        for (int i = 0; (i = s.indexOf('/', i+1)) > 0; d++) ;
        return d >= depth;
    }

    public int getChildrenCount(Object node) throws UnknownTypeException {
        countCall("getChildrenCount", node);
        return rootChildren.length;
    }

    public void addModelListener(ModelListener l) {
        //System.err.println(this+".addModelListener("+l+")");
        //Thread.dumpStack();
        synchronized (listeners) {
            listeners.add(l);
        }
        //System.err.println("  listeners = "+listeners);
    }

    public void removeModelListener(ModelListener l) {
        //System.err.println(this+".removeModelListener("+l+")");
        //Thread.dumpStack();
        synchronized (listeners) {
            listeners.remove(l);
        }
        //System.err.println("  listeners = "+listeners);
    }

    public boolean canRename(Object node) throws UnknownTypeException {
        countCall("canRename", node);
        return true;
    }

    public boolean canCopy(Object node) throws UnknownTypeException {
        countCall("canCopy", node);
        return true;
    }

    public boolean canCut(Object node) throws UnknownTypeException {
        countCall("canCut", node);
        return true;
    }

    public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
        countCall("clipboardCopy", node);
        return null;
    }

    public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
        countCall("clipboardCut", node);
        return null;
    }

    public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
        countCall("getPasteTypes", node, t);
        return new PasteType[] {};
    }

    public void setName(Object node, String name) throws UnknownTypeException {
        countCall("setName", node, name);
    }

    public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
        countCall("getIconBaseWithExtension", node);
        return null;
    }

    public String getDisplayName(Object node) throws UnknownTypeException {
        countCall("getDisplayName", node);
        return node.toString().replace('/', '-');
    }

    public String getIconBase(Object node) throws UnknownTypeException {
        countCall("getIconBase", node);
        return null;
    }

    public String getShortDescription(Object node) throws UnknownTypeException {
        countCall("getShortDescription", node);
        return node.toString() + " => " + node.toString().replace('/', '-');
    }

    public boolean isCheckable(Object node) throws UnknownTypeException {
        countCall("isCheckable", node);
        return true;
    }

    public boolean isCheckEnabled(Object node) throws UnknownTypeException {
        countCall("isCheckEnabled", node);
        return true;
    }

    public Boolean isSelected(Object node) throws UnknownTypeException {
        countCall("isSelected", node);
        return selectedNodes.containsKey(node) && selectedNodes.get(node);
    }

    public void setSelected(Object node, Boolean selected) throws UnknownTypeException {
        countCall("setSelected", node);
        selectedNodes.put(node, selected);
    }

    public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
        countCall("getValueAt", node, columnID);
        Map<String, Object> values = changedValues.get(node);
        if (values != null) {
            Object value = values.get(columnID);
            if (value != null) {
                return value;
            }
        }
        if (columnID.equals(COLUMN_LC)) {
            return node.toString().toLowerCase();
        }
        if (columnID.equals(COLUMN_UC)) {
            return node.toString().toUpperCase();
        }
        if (columnID.equals(COLUMN_BYTES)) {
            return Arrays.toString(node.toString().getBytes());
        }
        throw new UnknownTypeException(node+".getValue("+columnID+")");
    }

    public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
        countCall("isReadOnly", node, columnID);
        return false;
    }

    public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
        countCall("setValueAt", node, columnID, value);
        Map<String, Object> values = changedValues.get(node);
        if (values == null) {
            values = new HashMap<String, Object>();
            changedValues.put(node, values);
        }
        values.put(columnID, value);
    }

    public void fireModelChangeEvent(ModelEvent event) {
        for (ModelListener l : listeners) {
            l.modelChanged(event);
        }
    }

    protected void countCall(String methodName, Object... params) {
        CountedCall cc = new CountedCall(methodName, params);
        if (!countedCalls.add(cc)) {
            for (CountedCall ecc : countedCalls) {
                if (ecc.equals(cc)) {
                    ecc.addCall();
                }
            }
        }
    }

    public CountedCall getMaxCountedCall() {
        CountedCall mcc = null;
        for (CountedCall ecc : countedCalls) {
            if (mcc == null) {
                mcc = ecc;
            } else if (mcc.numCalls() < ecc.numCalls()) {
                mcc = ecc;
            }
        }
        return mcc;
    }

    public CountedCall[] getCountedCalls(String methodName) {
        List<CountedCall> ccs = new ArrayList<CountedCall>();
        for (CountedCall cc : countedCalls) {
            if (methodName.equals(cc.methodName)) {
                ccs.add(cc);
            }
        }
        return ccs.toArray(new CountedCall[] {});
    }

    public CountedCall getCountedCalls(String methodName, Object... params) {
        for (CountedCall cc : countedCalls) {
            if (methodName.equals(cc.methodName) && Arrays.equals(params, cc.params)) {
                return cc;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()+"("+Arrays.toString(rootChildren)+")";
    }

    static class CountedCall {

        private final String methodName;
        private final Object[] params;
        private final List<Throwable> calls = new ArrayList<Throwable>();

        CountedCall(String methodName, Object... params) {
            this.methodName = methodName;
            this.params = params;
            addCall();
        }

        void addCall() {
            calls.add(new Counter().fillInStackTrace());
        }

        int numCalls() {
            return calls.size();
        }

        Throwable[] callStacks() {
            return calls.toArray(new Throwable[] {});
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CountedCall) {
                CountedCall cc = (CountedCall) obj;
                if (!cc.methodName.equals(methodName)) {
                    return false;
                }
                if (!Arrays.equals(cc.params, params)) {
                    return false;
                }
                return true;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return methodName.hashCode() + (Arrays.hashCode(params) >>> 16);
        }

        @Override
        public String toString() {
            String str = "CountedCall("+methodName+", "+Arrays.toString(params)+") called "+numCalls()+" times.\n";
            for (int i = 0; i < calls.size(); i++) {
                Throwable t = calls.get(i);
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                pw.flush();
                str = str + " " + (i+1) + ".: " + sw.toString() + "\n";
            }
            return str;
        }

        private static class Counter extends Exception {}
    }

    static class CountedColumn extends ColumnModel {
        
        private final String id;

        public CountedColumn(String id) {
            this.id = id;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public String getDisplayName() {
            return "Column "+id;
        }

        @Override
        public Class getType() {
            return String.class;
        }
        
    }
}
