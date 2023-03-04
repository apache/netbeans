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
package org.netbeans.modules.debugger.jpda.visual.breakpoints;

import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LongValue;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import java.awt.event.HierarchyEvent;
import java.util.List;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.api.debugger.jpda.ObjectVariable;
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.expr.JDIVariable;
import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IllegalThreadStateExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.IntegerValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InvalidStackFrameExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.LongValueWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.StackFrameWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ThreadReferenceWrapper;
import org.netbeans.modules.debugger.jpda.jdi.TypeComponentWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VirtualMachineWrapper;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;
import org.openide.util.Exceptions;

/**
 *
 * @author martin
 */
public class AWTComponentBreakpointImpl extends BaseComponentBreakpointImpl {
    public AWTComponentBreakpointImpl(ComponentBreakpoint cb, JPDADebugger debugger) {
        super(cb, debugger);
    }

    @Override
    protected void initServiceBreakpoints() {
        //MethodBreakpoint mb = MethodBreakpoint.create("", "");
        ObjectReference component = cb.getComponent().getComponent(debugger);
        Variable variableComponent = ((JPDADebuggerImpl) debugger).getVariable(component);
        //mb.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
        
        final int type = cb.getType();
        if (((type & AWTComponentBreakpoint.TYPE_ADD) != 0) || ((type & AWTComponentBreakpoint.TYPE_REMOVE) != 0)) {
            MethodBreakpoint mb = MethodBreakpoint.create("java.awt.Component", "createHierarchyEvents");   // NOI18N
            mb.setHidden(true);
            mb.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
            mb.addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    ObjectReference[] parentPtr = null;
                    if ((type & AWTComponentBreakpoint.TYPE_ADD) == 0) {
                        parentPtr = new ObjectReference[] { null };
                    }
                    if ((type & AWTComponentBreakpoint.TYPE_REMOVE) == 0) {
                        parentPtr = new ObjectReference[] { null };
                    }
                    ObjectReference component = getComponentOfParentChanged(event, parentPtr);
                    if (component == null ||
                        (type & AWTComponentBreakpoint.TYPE_ADD) == 0 && parentPtr == null ||
                        (type & AWTComponentBreakpoint.TYPE_REMOVE) == 0 && parentPtr != null) {
                        event.resume();
                    } else {
                        navigateToCustomCode(event.getThread());
                    }
                }
            });
            if (!cb.isEnabled()) {
                mb.disable();
            }
            DebuggerManager.getDebuggerManager().addBreakpoint(mb);
            serviceBreakpoints.add(mb);
        }
        if (((type & AWTComponentBreakpoint.TYPE_SHOW) != 0) || ((type & AWTComponentBreakpoint.TYPE_HIDE) != 0)) {
            MethodBreakpoint mbShow = MethodBreakpoint.create("java.awt.Component", "show");
            mbShow.setMethodSignature("()V");
            addMethodBreakpoint(mbShow, (ObjectVariable) variableComponent);
            MethodBreakpoint mbHide = MethodBreakpoint.create("java.awt.Component", "hide");
            mbHide.setMethodSignature("()V");
            addMethodBreakpoint(mbHide, (ObjectVariable) variableComponent);
            if (!cb.isEnabled()) {
                mbShow.disable();
                mbHide.disable();
            }
        }
        if (((type & AWTComponentBreakpoint.TYPE_REPAINT) != 0)) {
            String componentClassName = null;
            if (variableComponent instanceof JDIVariable) {
                Value value = ((JDIVariable) variableComponent).getJDIValue();
                if (value instanceof ObjectReference) {
                    componentClassName = findClassWithMethod((ObjectReference) value, "repaint", "(JIIII)V");
                }
            }
            if (componentClassName == null) {
                componentClassName = "java.awt.Component";  // NOI18N
            }
            MethodBreakpoint mbShow = MethodBreakpoint.create(componentClassName, "repaint");
            // void repaint(long tm, int x, int y, int width, int height)
            mbShow.setMethodSignature("(JIIII)V");
            mbShow.setHidden(true);
            mbShow.setInstanceFilters(debugger, new ObjectVariable[] { (ObjectVariable) variableComponent });
            mbShow.addJPDABreakpointListener(new JPDABreakpointListener() {
                @Override
                public void breakpointReached(JPDABreakpointEvent event) {
                    navigateToCustomCode(event.getThread());
                }
            });
            if (!cb.isEnabled()) {
                mbShow.disable();
            }
            DebuggerManager.getDebuggerManager().addBreakpoint(mbShow);
            serviceBreakpoints.add(mbShow);
        }
    }
    
    public static ObjectReference getComponentOfParentChanged(JPDABreakpointEvent event,
                                                              ObjectReference[] parentPtr) {
        ThreadReference tr = ((JPDAThreadImpl) event.getThread()).getThreadReference();
        try {
            StackFrame frame = ThreadReferenceWrapper.frame(tr, 0);
            //com.sun.jdi.Method m = LocationWrapper.method(StackFrameWrapper.location(frame));
            List<Value> argumentValues = StackFrameWrapper.getArgumentValues0(frame);
            // int createHierarchyEvents(int id, Component changed,
            //                  Container changedParent, long changeFlags,
            //                  boolean enabledOnToolkit) {
            if (argumentValues.size() < 4) {
                return null;
            }
            Value idValue = argumentValues.get(0);
            if (!(idValue instanceof IntegerValue &&
                  HierarchyEvent.HIERARCHY_CHANGED == IntegerValueWrapper.value((IntegerValue) idValue))) {
                return null;
            }
            Value changeFlags = argumentValues.get(3);
            if (!(changeFlags instanceof LongValue &&
                  (LongValueWrapper.value((LongValue) changeFlags) & HierarchyEvent.PARENT_CHANGED) != 0)) {
                return null;
            }
            ObjectReference component = (ObjectReference) argumentValues.get(1);
            if (parentPtr != null) {
                List<ReferenceType> componentClassesByName = VirtualMachineWrapper.classesByName(tr.virtualMachine(), "java.awt.Component");
                try {
                    Field parent = ReferenceTypeWrapper.fieldByName(componentClassesByName.get(0), "parent");
                    if (parent != null) {
                        parentPtr[0] = (ObjectReference) ObjectReferenceWrapper.getValue(component, parent);
                    }
                } catch (ClassNotPreparedExceptionWrapper ex) {
                    // Should not ever happen
                    Exceptions.printStackTrace(ex);
                }
            }
            return component;
        } catch (IncompatibleThreadStateException e) {
            return null;
        } catch (IllegalThreadStateExceptionWrapper e) {
            return null;
        } catch (InvalidStackFrameExceptionWrapper e) {
            return null;
        } catch (InternalExceptionWrapper e) {
            return null;
        } catch (ObjectCollectedExceptionWrapper ocex) {
            return null;
        } catch (VMDisconnectedExceptionWrapper e) {
            return null;
        }
        
    }

    private String findClassWithMethod(ObjectReference value, String name, String signature) {
        try {
            ReferenceType referenceType = ObjectReferenceWrapper.referenceType(value);
            List<Method> methods = ReferenceTypeWrapper.methodsByName(referenceType, name, signature);
            if (!methods.isEmpty()) {
                return ReferenceTypeWrapper.name(TypeComponentWrapper.declaringType(methods.get(0)));
            }
        } catch (ClassNotPreparedExceptionWrapper ex) {
        } catch (InternalExceptionWrapper iex) {
        } catch (ObjectCollectedExceptionWrapper ocex) {
        } catch (VMDisconnectedExceptionWrapper vmdex) {
        }
        return null;
    }
}
