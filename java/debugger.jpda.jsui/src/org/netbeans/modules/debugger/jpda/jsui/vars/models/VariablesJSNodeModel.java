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

package org.netbeans.modules.debugger.jpda.jsui.vars.models;

import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.api.debugger.jpda.Field;
import org.netbeans.api.debugger.jpda.InvalidExpressionException;
import org.netbeans.api.debugger.jpda.JPDAClassType;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Super;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.js.vars.JSThis;
import org.netbeans.modules.debugger.jpda.js.vars.JSVariable;
import org.netbeans.modules.debugger.jpda.js.vars.ScopeVariable;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/LocalsView",  types=ExtendedNodeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/ResultsView", types=ExtendedNodeModelFilter.class),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/ToolTipView", types=ExtendedNodeModelFilter.class, position = 500),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/JS/WatchesView", types=ExtendedNodeModelFilter.class, position = 250),
})
public class VariablesJSNodeModel implements ExtendedNodeModelFilter {
    
    @StaticResource(searchClasspath = true)
    private static final String GLOBAL = "org/netbeans/modules/javascript2/debug/ui/resources/global_variable_16.png"; // NOI18N

    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canRename(node);
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSThis) {
            return original.getIconBaseWithExtension(new EmptyThis());
        }
        if (node instanceof JSVariable) {
            return original.getIconBaseWithExtension(new EmptyVar());
        }
        if (node instanceof JSWatchVar) {
            return original.getIconBaseWithExtension(((JSWatchVar) node).getWatch());
        }
        if (node instanceof ScopeVariable) {
            return GLOBAL;
        }
        return original.getIconBaseWithExtension(node);
    }

    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSVariable) {
            return ((JSVariable) node).getKey();
        }
        if (node instanceof ScopeVariable) {
            return ((ScopeVariable) node).getName();
        }
        if (node instanceof JSWatchVar) {
            JSWatchVar jswv = (JSWatchVar) node;
            node = jswv.getWatch();
        }
        return original.getDisplayName(node);
    }

    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof JSVariable) {
            JSVariable var = (JSVariable) node;
            return var.getKey() + " = " + var.getValue();
        }
        if (node instanceof ScopeVariable) {
            return ((ScopeVariable) node).getName();
        }
        if (node instanceof JSWatchVar) {
            JSWatchVar jswv = (JSWatchVar) node;
            JSVariable jsVar = jswv.getJSVar();
            if (jsVar != null) {
                return jswv.getWatch().getExpression() + " = " + jsVar.getValue();
            } else {
                node = jswv.getWatch();
            }
        }
        return original.getShortDescription(node);
    }

    @Override
    public void addModelListener(ModelListener l) {
        
    }

    @Override
    public void removeModelListener(ModelListener l) {
        
    }
    
    private static final class EmptyThis implements This {

        @Override
        public String getToStringValue() throws InvalidExpressionException { return "empty"; }

        @Override
        public Variable invokeMethod(String methodName, String signature, Variable[] arguments) throws NoSuchMethodException, InvalidExpressionException {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        public int getFieldsCount() { return  0; }

        @Override
        public Field getField(String name) { return null; }

        @Override
        public Field[] getFields(int from, int to) { return null; }

        @Override
        public Field[] getAllStaticFields(int from, int to) { return null; }

        @Override
        public Field[] getInheritedFields(int from, int to) { return null; }

        @Override
        public List<ObjectVariable> getReferringObjects(long maxReferrers) throws UnsupportedOperationException {
            return null;
        }

        @Override
        public Super getSuper() { return null; }

        @Override
        public JPDAClassType getClassType() { return null; }

        @Override
        public long getUniqueID() { return 0l; }

        @Override
        public String getType() { return "empty"; }

        @Override
        public String getValue() { return ""; }

        @Override
        public Object createMirrorObject() { return null; }
        
    }
    
    private static final class EmptyVar implements Variable {

        @Override
        public String getType() { return "empty"; }

        @Override
        public String getValue() { return ""; }

        @Override
        public Object createMirrorObject() { return null; }
        
    }
    
}
