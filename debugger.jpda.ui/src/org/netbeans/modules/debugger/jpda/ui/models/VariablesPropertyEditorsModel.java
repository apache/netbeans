/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
