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
package org.netbeans.modules.debugger.jpda.ui.models;

import java.beans.PropertyEditor;
import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.DebuggerServiceRegistrations;
import org.netbeans.spi.viewmodel.TablePropertyEditorsModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import static org.netbeans.spi.debugger.ui.Constants.*;

/**
 * 
 * @author Martin Entlicher
 */
@DebuggerServiceRegistrations({
    @DebuggerServiceRegistration(path="netbeans-JPDASession/LocalsView",
                                 types=TablePropertyEditorsModel.class,
                                 position=25000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ToolTipView",
                                 types=TablePropertyEditorsModel.class,
                                 position=25000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/WatchesView",
                                 types=TablePropertyEditorsModel.class,
                                 position=25000),
    @DebuggerServiceRegistration(path="netbeans-JPDASession/ResultsView",
                                 types=TablePropertyEditorsModel.class,
                                 position=25000),
})
public class VariablesPropertyEditorsModel implements TablePropertyEditorsModel {
    
    private static final Map<Variable, PropertyEditorRef> propertyEditors = new WeakHashMap<Variable, PropertyEditorRef>();
    
    private ContextProvider contextProvider;
    
    public VariablesPropertyEditorsModel(ContextProvider contextProvider) {
        this.contextProvider = contextProvider;
    }

    @Override
    public PropertyEditor getPropertyEditor(Object node, String columnID) throws UnknownTypeException {
        if (LOCALS_VALUE_COLUMN_ID.equals (columnID) ||
            WATCH_VALUE_COLUMN_ID.equals (columnID)) {
            
            if (node instanceof Variable) {
                //Variable var = (Variable) node;
                PropertyEditor pe = null;
                synchronized (propertyEditors) {
                    PropertyEditorRef ref = propertyEditors.get((Variable) node);
                    if (ref != null) {
                        pe = ref.get();
                    }
                    if (pe == null) {
                        pe = new ValuePropertyEditor(contextProvider);
                        propertyEditors.put((Variable) node, new PropertyEditorRef(pe));
                    }
                    return pe;
                }
            }
        }
        return null;
    }
    
    static ValuePropertyEditor getExistingValuePropertyEditor(Variable var) {
        synchronized (propertyEditors) {
            PropertyEditorRef ref = propertyEditors.get(var);
            if (ref != null) {
                return (ValuePropertyEditor) ref.getFromEDT();
            }
        }
        return null;
    }
    
    private static final class PropertyEditorRef extends SoftReference<PropertyEditor> {
        private final Thread createdBy;
        private final boolean isEDT;
        
        public PropertyEditorRef(PropertyEditor referent) {
            super(referent);
            createdBy = Thread.currentThread();
            isEDT = SwingUtilities.isEventDispatchThread();
        }

        @Override
        public PropertyEditor get() {
            if (Thread.currentThread() != createdBy) {
                return null;
            }
            return super.get();
        }
        
        PropertyEditor getFromEDT() {
            if (isEDT) {
                return super.get();
            } else {
                return null;
            }
        }
    }
}
